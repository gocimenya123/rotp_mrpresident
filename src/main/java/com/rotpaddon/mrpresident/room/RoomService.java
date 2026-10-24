package com.rotpaddon.mrpresident.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentBackTeleporter;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentInsideTeleporter;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.rotpaddon.mrpresident.entity.KeyMarkerEntity;
import com.rotpaddon.mrpresident.init.InitEntities;
import com.rotpaddon.mrpresident.init.InitStands;
import com.rotpaddon.mrpresident.init.InitStatusEffects;
import com.rotpaddon.mrpresident.room.OwnerSavedData.Room;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/**
 * All room logic of the addon. The pocket dimension, the room structure, the room allocation table and the
 * teleporters are RotP's own (jojo.mrpresident); this class drives them for a player-owned room, where the owner
 * can be inside the room dimension himself. The owner is identified by his UUID, like a turtle is in the base mod.
 */
public final class RoomService {
    /** How long a kidnapped entity stays in the room (ticks). */
    public static final int KIDNAP_TICKS = 20 * 20;

    private RoomService() {}

    // ------------------------------------------------------------------ queries

    public static boolean isRoomDimension(Entity entity) {
        return entity.level.dimension() == ModDimensions.MR_PRESIDENT;
    }

    public static boolean hasMrPresident(IStandPower power) {
        return power.hasPower() && power.getType() == InitStands.MR_PRESIDENT.get();
    }

    /** True while the key is out of the shell. Works on both sides (the marker effect is synced by vanilla). */
    public static boolean isLocked(LivingEntity owner) {
        return owner.hasEffect(InitStatusEffects.KEY_REMOVED.get());
    }

    @Nullable
    public static ServerWorld roomWorld(MinecraftServer server) {
        return server.getLevel(ModDimensions.MR_PRESIDENT);
    }

    private static AxisAlignedBB roomBox(BlockPos corner) {
        return new AxisAlignedBB(corner, corner.offset(MrPresidentInsideTeleporter.ROOM_SIZE));
    }

    public static boolean isInsideRoom(ServerWorld roomWorld, UUID owner, Entity entity) {
        BlockPos corner = MrPresidentInsideTeleporter.getLowerCornerRoomPos(roomWorld, owner);
        return corner != null && entity.level == roomWorld && roomBox(corner).contains(entity.position());
    }

    /** Everything alive standing in the owner's room (the owner included, if he is there). */
    public static List<Entity> getOccupants(MinecraftServer server, UUID owner) {
        ServerWorld roomWorld = roomWorld(server);
        if (roomWorld == null) return Collections.emptyList();
        BlockPos corner = MrPresidentInsideTeleporter.getLowerCornerRoomPos(roomWorld, owner);
        if (corner == null) return Collections.emptyList();
        roomWorld.getChunk(corner.getX() >> 4, corner.getZ() >> 4); // make sure mobs left in there are loaded
        return roomWorld.getEntities((Entity) null, roomBox(corner),
                e -> e instanceof LivingEntity && !(e instanceof ArmorStandEntity) && e.isAlive());
    }

    // ------------------------------------------------------------------ entering

    /**
     * Allocates the room slot in the base mod's table and builds the room the first time, furniture included.
     * Afterwards the base inside-teleporter sees an existing room and only places the entity.
     */
    private static void ensureRoomBuilt(ServerWorld roomWorld, UUID owner) {
        MrPresidentWorldData rooms = roomWorld.getCapability(MrPresidentWorldDataProvider.CAPABILITY).orElse(null);
        if (rooms == null || rooms.getAllocatedRoom(owner) != null) return;

        ChunkSectionPos section = rooms.posForNewRoom(owner);
        BlockPos corner = new BlockPos(section.x << 4, section.y << 4, section.z << 4);
        roomWorld.getChunk(corner.getX() >> 4, corner.getZ() >> 4);
        ModStructures.CONFIGURED_MR_PRESIDENT_ROOM.get().place(roomWorld,
                roomWorld.getChunkSource().getGenerator(), roomWorld.getRandom(), corner);
        RoomFurnisher.furnish(roomWorld, corner);
    }

    /**
     * Moves an entity into the owner's room. Dimension changes can't happen while entities are ticking,
     * so the work is deferred to the server's task queue, as the base mod does it.
     */
    public static void teleportInto(Entity entity, UUID owner, MinecraftServer server, int kidnapTicks) {
        ServerWorld roomWorld = roomWorld(server);
        if (roomWorld == null) return;
        server.tell(new TickDelayedTask(server.getTickCount(), () -> {
            if (!entity.isAlive()) return;
            ensureRoomBuilt(roomWorld, owner);
            if (kidnapTicks > 0) {
                Room room = OwnerSavedData.get(roomWorld).getOrCreate(owner);
                room.kidnapped.put(entity.getUUID(), kidnapTicks);
                room.markDirty();
            }
            entityFx(entity);
            if (entity.getUUID().equals(owner) && !isRoomDimension(entity)) {
                placeMarker(entity, owner, roomWorld);
            }
            entity.changeDimension(roomWorld, new MrPresidentInsideTeleporter(owner));
        }));
    }

    /** A floating 3D key where the owner went in; it stays until he comes out. */
    private static void placeMarker(Entity owner, UUID ownerId, ServerWorld roomWorld) {
        if (!(owner.level instanceof ServerWorld)) return;
        removeMarker(roomWorld.getServer(), ownerId);
        KeyMarkerEntity marker = InitEntities.KEY_MARKER.get().create(owner.level);
        if (marker == null) return;
        marker.setOwner(ownerId);
        marker.moveTo(owner.getX(), owner.getY(), owner.getZ(), 0.0F, 0.0F);
        owner.level.addFreshEntity(marker);
        Room room = OwnerSavedData.get(roomWorld).getOrCreate(ownerId);
        room.markerId = marker.getUUID();
        room.markDirty();
    }

    private static void removeMarker(MinecraftServer server, UUID ownerId) {
        ServerWorld roomWorld = roomWorld(server);
        if (roomWorld == null) return;
        Room room = OwnerSavedData.get(roomWorld).getRoom(ownerId);
        if (room == null || room.markerId == null) return;
        for (ServerWorld world : server.getAllLevels()) {
            Entity marker = world.getEntity(room.markerId);
            if (marker != null) {
                marker.remove();
            }
        }
        room.markerId = null; // a marker in an unloaded chunk removes itself when it ticks again
        room.markDirty();
    }

    // ------------------------------------------------------------------ leaving

    /** Moves an entity out of the owner's room to where the owner last was outside it. */
    public static void teleportOut(Entity entity, UUID owner, MinecraftServer server) {
        ServerWorld roomWorld = roomWorld(server);
        if (roomWorld == null) return;
        Room room = OwnerSavedData.get(roomWorld).getRoom(owner);

        ServerWorld destination = room == null || room.getOuterDimension() == null ? null : server.getLevel(room.getOuterDimension());
        Vector3d pos = room == null ? null : room.getOuterPos();
        if (destination == null || pos == null) { // owner never was outside: fall back to the world spawn
            destination = server.overworld();
            BlockPos spawn = destination.getSharedSpawnPos();
            pos = new Vector3d(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }
        if (entity.getUUID().equals(owner)) {
            removeMarker(server, owner);
        }
        ServerWorld dest = destination;
        Vector3d destPos = pos;
        server.tell(new TickDelayedTask(server.getTickCount(), () -> {
            if (!entity.isAlive()) return;
            entityFx(entity);
            entity.changeDimension(dest, new MrPresidentBackTeleporter(dest, destPos, null));
        }));
    }

    /** Throws everybody out; {@code includeOwner} decides whether the owner is moved too. */
    public static void expelAll(MinecraftServer server, UUID owner, boolean includeOwner) {
        for (Entity occupant : getOccupants(server, owner)) {
            if (!includeOwner && occupant.getUUID().equals(owner)) continue;
            teleportOut(occupant, owner, server);
        }
    }

    /** Is this room owned by a player-user of this addon (as opposed to a turtle)? */
    public static boolean isAddonRoom(ServerWorld roomWorld, UUID owner) {
        return OwnerSavedData.get(roomWorld).getRoom(owner) != null;
    }

    // ------------------------------------------------------------------ ticking

    /** Remember where the owner stands outside the room: that is where everybody gets released. */
    public static void trackOuter(ServerPlayerEntity player) {
        if (isRoomDimension(player)) return;
        ServerWorld roomWorld = roomWorld(player.server);
        if (roomWorld == null) return;
        OwnerSavedData.get(roomWorld).getOrCreate(player.getUUID())
                .setOuter(player.level.dimension(), new Vector3d(player.getX(), player.getY(), player.getZ()));
    }

    /** Counts down kidnapped entities and releases the ones whose time is up. */
    public static void tickKidnapTimers(@Nullable MinecraftServer server) {
        if (server == null) return;
        ServerWorld roomWorld = roomWorld(server);
        if (roomWorld == null) return;
        for (Room room : new ArrayList<>(OwnerSavedData.get(roomWorld).getRooms())) {
            if (room.kidnapped.isEmpty()) continue;
            Iterator<Map.Entry<UUID, Integer>> it = room.kidnapped.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> entry = it.next();
                int left = entry.getValue() - 1;
                if (left > 0) {
                    entry.setValue(left);
                    continue;
                }
                it.remove();
                room.markDirty();
                Entity inside = roomWorld.getEntity(entry.getKey());
                if (inside != null && isInsideRoom(roomWorld, room.owner, inside)) {
                    teleportOut(inside, room.owner, server);
                }
            }
        }
    }

    /** Turtle form: whatever falls onto the shell drops into the room. */
    public static void captureFalling(ServerPlayerEntity turtle) {
        if (isLocked(turtle)) return;
        List<Entity> falling = turtle.level.getEntities(turtle, turtle.getBoundingBox().move(0, 0.5, 0).inflate(0.25),
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                        && e.getBbWidth() < 4 && e.getBbHeight() < 4
                        && !e.isOnGround() && e.getDeltaMovement().y < 0 && e.getY() > turtle.getY(1)
                        && !(e.getType() == EntityType.PLAYER && e.tickCount < 20)
                        && !e.isPassengerOfSameVehicle(turtle) && !turtle.hasPassenger(e));
        for (Entity entity : falling) {
            teleportInto(entity, turtle.getUUID(), turtle.server, 0);
        }
    }

    // ------------------------------------------------------------------ fx

    /** The "falling into the shell" effect: a swirl of portal particles and a muffled thud. */
    public static void entityFx(Entity entity) {
        if (entity.level instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) entity.level;
            world.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY(0.5), entity.getZ(),
                    40, 0.3, 0.6, 0.3, 0.15);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.8F, 0.7F);
        }
    }
}
