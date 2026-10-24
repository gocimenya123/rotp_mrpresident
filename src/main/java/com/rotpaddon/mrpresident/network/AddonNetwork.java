package com.rotpaddon.mrpresident.network;

import com.rotpaddon.mrpresident.AddonMain;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class AddonNetwork {
    private static final String PROTOCOL = "1";
    private static SimpleChannel channel;

    public static void init() {
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(AddonMain.MOD_ID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        channel.registerMessage(0, ShakePacket.class, ShakePacket::encode, ShakePacket::decode, ShakePacket::handle);
    }

    public static void sendToPlayer(Object msg, ServerPlayerEntity player) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
