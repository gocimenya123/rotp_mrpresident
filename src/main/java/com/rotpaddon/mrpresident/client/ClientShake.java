package com.rotpaddon.mrpresident.client;

import java.util.Random;

import com.rotpaddon.mrpresident.AddonMain;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client side camera shake used when the room is rattled. */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class ClientShake {
    private static final Random RANDOM = new Random();
    private static int ticksLeft;
    private static int ticksTotal = 1;
    private static float intensity;

    public static void start(int ticks, float shakeIntensity) {
        ticksLeft = ticks;
        ticksTotal = Math.max(1, ticks);
        intensity = shakeIntensity;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ticksLeft > 0) {
            ticksLeft--;
        }
    }

    @SubscribeEvent
    public static void cameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (ticksLeft <= 0) return;
        float strength = intensity * ticksLeft / ticksTotal;
        event.setYaw(event.getYaw() + (RANDOM.nextFloat() - 0.5F) * 2 * strength);
        event.setPitch(event.getPitch() + (RANDOM.nextFloat() - 0.5F) * 2 * strength);
        event.setRoll(event.getRoll() + (RANDOM.nextFloat() - 0.5F) * 2 * strength);
    }
}
