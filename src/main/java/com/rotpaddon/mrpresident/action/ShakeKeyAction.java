package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.network.AddonNetwork;
import com.rotpaddon.mrpresident.network.ShakePacket;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability 4 (YBA): shaking the key rattles the room. Everybody inside, the user too, takes damage and is stunned.
 * The user can never kill himself with it.
 */
public class ShakeKeyAction extends RoomAction {
    public static final float DAMAGE = 6.0F;
    public static final int STUN_TICKS = 60;
    public static final int SHAKE_TICKS = 40;

    public ShakeKeyAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        DamageSource source = new EntityDamageSource("mr_president_shake", user);
        for (Entity occupant : RoomService.getOccupants(world.getServer(), user.getUUID())) {
            LivingEntity living = (LivingEntity) occupant;
            float damage = living == user ? Math.min(DAMAGE, Math.max(0, living.getHealth() - 1)) : DAMAGE;
            if (damage > 0) {
                living.hurt(source, damage);
            }
            living.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), STUN_TICKS));

            ServerWorld roomWorld = (ServerWorld) living.level;
            roomWorld.sendParticles(ParticleTypes.CRIT, living.getX(), living.getY(0.5), living.getZ(), 12, 0.4, 0.5, 0.4, 0.2);
            roomWorld.playSound(null, living.getX(), living.getY(), living.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundCategory.PLAYERS, 0.8F, 0.6F);
            if (living instanceof ServerPlayerEntity) {
                AddonNetwork.sendToPlayer(new ShakePacket(SHAKE_TICKS, 3.0F), (ServerPlayerEntity) living);
            }
        }
        // the shell itself rattles outside
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.CHAIN_HIT, SoundCategory.PLAYERS, 1.0F, 0.7F);
    }
}
