package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.effect.TurtleFormEffect;
import com.rotpaddon.mrpresident.init.InitStatusEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability 6 (YBA): become Coco Jumbo. Heavy damage reduction, very slow, small hitbox, drops falling
 * entities into the room. Expires on its own, or toggle it off.
 */
public class TurtleFormAction extends RoomAction {

    public TurtleFormAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return requireOutside(user);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        Effect turtleForm = InitStatusEffects.TURTLE_FORM.get();
        if (user.hasEffect(turtleForm)) {
            user.removeEffect(turtleForm);
        }
        else {
            user.addEffect(new EffectInstance(turtleForm, TurtleFormEffect.DEFAULT_DURATION, 0, false, false, true));
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.TURTLE_AMBIENT_LAND, SoundCategory.PLAYERS, 1.0F, 0.6F);
        }
    }

    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        return power.getUser().hasEffect(InitStatusEffects.TURTLE_FORM.get()) ? key + ".off" : key;
    }
}
