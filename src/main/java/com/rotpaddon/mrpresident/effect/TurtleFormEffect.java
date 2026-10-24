package com.rotpaddon.mrpresident.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * Turtle Form marker effect. Being a vanilla effect it is saved with the entity, expires on its own
 * and is synced to every tracking client for free.
 */
public class TurtleFormEffect extends Effect {
    /** Fraction of incoming damage that is absorbed while in turtle form. */
    public static final float DAMAGE_REDUCTION = 0.75F;
    public static final int DEFAULT_DURATION = 20 * 45;

    public TurtleFormEffect() {
        super(EffectType.NEUTRAL, 0x00AFAF);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "5d1b5f5e-6c4a-4a0e-9f7e-3a6c1c0a7101", -0.7D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "5d1b5f5e-6c4a-4a0e-9f7e-3a6c1c0a7102", 1.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        entity.refreshDimensions();
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        entity.refreshDimensions();
    }
}
