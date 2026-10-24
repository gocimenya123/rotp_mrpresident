package com.rotpaddon.mrpresident.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.rotpaddon.mrpresident.room.OwnerSavedData;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * The spot where the owner stepped into his room: a floating 3D key hangs there until he comes back out.
 * Purely visual: it can't be hit, pushed or picked up.
 */
public class KeyMarkerEntity extends Entity {
    @Nullable private UUID owner;

    public KeyMarkerEntity(EntityType<? extends KeyMarkerEntity> type, World world) {
        super(type, world);
        this.noPhysics = true;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide() || tickCount % 20 != 0) return;
        // a marker lives while its owner is in the room: remove stale ones (owner left the room, new marker made)
        ServerWorld world = (ServerWorld) level;
        if (owner == null) {
            remove();
            return;
        }
        ServerWorld roomWorld = RoomService.roomWorld(world.getServer());
        if (roomWorld == null) return;
        UUID current = OwnerSavedData.get(roomWorld).getMarkerId(owner);
        ServerPlayerEntity player = world.getServer().getPlayerList().getPlayer(owner);
        if (!getUUID().equals(current) || player != null && !RoomService.isRoomDimension(player)) {
            remove();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        owner = nbt.hasUUID("Owner") ? nbt.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        if (owner != null) {
            nbt.putUUID("Owner", owner);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
