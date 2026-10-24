package com.rotpaddon.mrpresident.network;

import java.util.function.Supplier;

import com.rotpaddon.mrpresident.client.ClientShake;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/** Server to client: shake the camera (the room being rattled by Shake Key). */
public class ShakePacket {
    private final int ticks;
    private final float intensity;

    public ShakePacket(int ticks, float intensity) {
        this.ticks = ticks;
        this.intensity = intensity;
    }

    public static void encode(ShakePacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.ticks);
        buf.writeFloat(msg.intensity);
    }

    public static ShakePacket decode(PacketBuffer buf) {
        return new ShakePacket(buf.readVarInt(), buf.readFloat());
    }

    public static void handle(ShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientShake.start(msg.ticks, msg.intensity)));
        ctx.get().setPacketHandled(true);
    }
}
