package com.rotpaddon.mrpresident.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.github.standobyte.jojo.init.ModItems;
import com.rotpaddon.mrpresident.entity.KeyMarkerEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

/** Draws the Turtle Key item model, big, slowly spinning and bobbing. */
public class KeyMarkerRenderer extends EntityRenderer<KeyMarkerEntity> {
    private ItemStack stack;

    public KeyMarkerRenderer(EntityRendererManager manager) {
        super(manager);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(KeyMarkerEntity entity, float yaw, float partialTicks, MatrixStack matrix,
                       IRenderTypeBuffer buffer, int light) {
        if (stack == null) {
            stack = new ItemStack(ModItems.MR_PRESIDENT_KEY.get());
        }
        float time = entity.tickCount + partialTicks;
        matrix.pushPose();
        matrix.translate(0.0, 0.7 + MathHelper.sin(time / 10.0F) * 0.08, 0.0);
        matrix.mulPose(Vector3f.YP.rotationDegrees(time * 3.0F));
        matrix.scale(1.8F, 1.8F, 1.8F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemCameraTransforms.TransformType.FIXED,
                0xF000F0, OverlayTexture.NO_OVERLAY, matrix, buffer);
        matrix.popPose();
        super.render(entity, yaw, partialTicks, matrix, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(KeyMarkerEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
