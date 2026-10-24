package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/** Ability 2 (YBA): a key strike that drags the target into the room for a while. Works on allies too. */
public class KidnapAction extends RoomAction {

    public KidnapAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    public double getMaxRangeSqEntityTarget() {
        return 16;
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ActionConditionResult outside = requireOutside(user);
        return outside.isPositive() ? requireUnlocked(user) : outside;
    }

    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity entity = target.getEntity();
        if (!(entity instanceof LivingEntity) || entity.getBbWidth() >= 4 || entity.getBbHeight() >= 4) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkTarget(target, user, power);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        Entity victim = target.getEntity();
        if (RoomService.isLocked(user) || !(victim instanceof LivingEntity) || victim == user) return;

        pullTrail(world, user, victim);
        world.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.ENDERMAN_SCREAM, SoundCategory.PLAYERS, 0.6F, 1.6F);
        RoomService.teleportInto(victim, user.getUUID(), world.getServer(), RoomService.KIDNAP_TICKS);
    }

    /** Particle line from the victim to the user: the visual of being sucked into the shell. */
    private static void pullTrail(ServerWorld world, LivingEntity user, Entity victim) {
        Vector3d from = victim.position().add(0, victim.getBbHeight() / 2, 0);
        Vector3d to = user.position().add(0, user.getBbHeight() / 2, 0);
        Vector3d step = to.subtract(from).scale(1.0 / 12);
        for (int i = 0; i <= 12; i++) {
            Vector3d p = from.add(step.scale(i));
            world.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
