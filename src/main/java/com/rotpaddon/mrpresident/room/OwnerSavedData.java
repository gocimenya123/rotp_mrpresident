package com.rotpaddon.mrpresident.room;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.rotpaddon.mrpresident.AddonMain;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

/** What the addon remembers per room owner (the room itself is the base mod's): exit point and kidnap timers. */
public class OwnerSavedData extends WorldSavedData {
    public static final String NAME = AddonMain.MOD_ID + "_owners";
    private final Map<UUID, Room> rooms = new LinkedHashMap<>();

    public OwnerSavedData() {
        super(NAME);
    }

    public static OwnerSavedData get(ServerWorld roomWorld) {
        return roomWorld.getDataStorage().computeIfAbsent(OwnerSavedData::new, NAME);
    }

    @Nullable
    public Room getRoom(UUID owner) {
        return rooms.get(owner);
    }

    public Room getOrCreate(UUID owner) {
        Room room = rooms.get(owner);
        if (room == null) {
            room = new Room(owner);
            rooms.put(owner, room);
            setDirty();
        }
        return room;
    }

    /** The id of the key marker standing where this owner entered his room, if any. */
    @Nullable
    public UUID getMarkerId(UUID owner) {
        Room room = rooms.get(owner);
        return room == null ? null : room.markerId;
    }

    public Collection<Room> getRooms() {
        return rooms.values();
    }

    @Override
    public void load(CompoundNBT nbt) {
        rooms.clear();
        for (INBT tag : nbt.getList("Rooms", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT roomNbt = (CompoundNBT) tag;
            if (!roomNbt.hasUUID("Owner")) continue;
            Room room = new Room(roomNbt.getUUID("Owner"));
            room.read(roomNbt);
            rooms.put(room.owner, room);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Room room : rooms.values()) {
            list.add(room.write());
        }
        nbt.put("Rooms", list);
        return nbt;
    }

    public class Room {
        public final UUID owner;
        @Nullable private RegistryKey<World> outerDimension;
        @Nullable private Vector3d outerPos;
        /** Entities kidnapped into the room and the ticks they still have to stay. */
        public final Map<UUID, Integer> kidnapped = new HashMap<>();
        /** The 3D key shown at the place where the owner entered. */
        @Nullable public UUID markerId;

        private Room(UUID owner) {
            this.owner = owner;
        }

        @Nullable
        public RegistryKey<World> getOuterDimension() {
            return outerDimension;
        }

        @Nullable
        public Vector3d getOuterPos() {
            return outerPos;
        }

        public void setOuter(RegistryKey<World> dimension, Vector3d pos) {
            this.outerDimension = dimension;
            this.outerPos = pos;
            setDirty();
        }

        public void markDirty() {
            setDirty();
        }

        private CompoundNBT write() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putUUID("Owner", owner);
            if (outerDimension != null && outerPos != null) {
                nbt.putString("OuterDim", outerDimension.location().toString());
                nbt.putDouble("OuterX", outerPos.x);
                nbt.putDouble("OuterY", outerPos.y);
                nbt.putDouble("OuterZ", outerPos.z);
            }
            ListNBT list = new ListNBT();
            kidnapped.forEach((id, ticks) -> {
                CompoundNBT entry = new CompoundNBT();
                entry.putUUID("Id", id);
                entry.putInt("Ticks", ticks);
                list.add(entry);
            });
            if (markerId != null) {
                nbt.putUUID("Marker", markerId);
            }
            nbt.put("Kidnapped", list);
            return nbt;
        }

        private void read(CompoundNBT nbt) {
            if (nbt.contains("OuterDim", Constants.NBT.TAG_STRING)) {
                outerDimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("OuterDim")));
                outerPos = new Vector3d(nbt.getDouble("OuterX"), nbt.getDouble("OuterY"), nbt.getDouble("OuterZ"));
            }
            markerId = nbt.hasUUID("Marker") ? nbt.getUUID("Marker") : null;
            for (INBT tag : nbt.getList("Kidnapped", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT entry = (CompoundNBT) tag;
                if (entry.hasUUID("Id")) {
                    kidnapped.put(entry.getUUID("Id"), entry.getInt("Ticks"));
                }
            }
        }
    }
}
