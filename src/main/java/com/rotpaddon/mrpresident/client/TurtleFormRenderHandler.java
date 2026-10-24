package com.rotpaddon.mrpresident.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rotpaddon.mrpresident.AddonMain;
import com.rotpaddon.mrpresident.init.InitStatusEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Draws a player in turtle form as a turtle: a client-only dummy turtle is posed from the player every frame. */
@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID, value = Dist.CLIENT)
public class TurtleFormRenderHandler {
    private static final Map<UUID, TurtleEntity> DUMMIES = new HashMap<>();

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        if (!player.hasEffect(InitStatusEffects.TURTLE_FORM.get())) return;

        TurtleEntity turtle = DUMMIES.get(player.getUUID());
        if (turtle == null || turtle.level != player.level) {
            turtle = EntityType.TURTLE.create(player.level);
            if (turtle == null) return;
            DUMMIES.put(player.getUUID(), turtle);
        }
        pose(turtle, player);

        event.setCanceled(true);
        EntityRenderer<? super TurtleEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(turtle);
        float yaw = MathHelper.rotLerp(event.getPartialRenderTick(), player.yRotO, player.yRot);
        event.getMatrixStack().pushPose();
        event.getMatrixStack().scale(1.3F, 1.3F, 1.3F);
        renderer.render(turtle, yaw, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
        event.getMatrixStack().popPose();
    }

    private static void pose(TurtleEntity turtle, PlayerEntity player) {
        turtle.setPosRaw(player.getX(), player.getY(), player.getZ());
        turtle.xo = player.xo; turtle.yo = player.yo; turtle.zo = player.zo;
        turtle.yRot = player.yRot; turtle.yRotO = player.yRotO;
        turtle.xRot = player.xRot; turtle.xRotO = player.xRotO;
        turtle.yBodyRot = player.yBodyRot; turtle.yBodyRotO = player.yBodyRotO;
        turtle.yHeadRot = player.yHeadRot; turtle.yHeadRotO = player.yHeadRotO;
        turtle.animationPosition = player.animationPosition;
        turtle.animationSpeed = player.animationSpeed;
        turtle.animationSpeedOld = player.animationSpeedOld;
        turtle.tickCount = player.tickCount;
    }
}
