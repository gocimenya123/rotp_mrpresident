package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

/** Ability 5 (YBA): drinks from the fridge for everybody in the room. */
public class RestockSuppliesAction extends RoomAction {
    public static final float HEAL = 8.0F;

    public RestockSuppliesAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        for (Entity occupant : RoomService.getOccupants(world.getServer(), user.getUUID())) {
            LivingEntity living = (LivingEntity) occupant;
            living.heal(HEAL);
            living.addEffect(new EffectInstance(Effects.REGENERATION, 20 * 10, 1));
            living.addEffect(new EffectInstance(Effects.ABSORPTION, 20 * 30, 0));
            living.addEffect(new EffectInstance(Effects.SATURATION, 1, 1));

            ServerWorld roomWorld = (ServerWorld) living.level;
            roomWorld.sendParticles(ParticleTypes.HEART, living.getX(), living.getY(1.0), living.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
            roomWorld.playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }
}
