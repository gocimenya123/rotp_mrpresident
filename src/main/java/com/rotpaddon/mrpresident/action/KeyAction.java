package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.init.InitStatusEffects;
import com.rotpaddon.mrpresident.room.RoomKeys;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

/**
 * Ability 7 (canon): the key in the shell. Pulling it out throws everybody out and locks the room and gives
 * the user the Turtle Key item; holding it and using the ability again puts it back.
 */
public class KeyAction extends RoomAction {

    public KeyAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ActionConditionResult outside = requireOutside(user);
        if (!outside.isPositive()) return outside;
        if (RoomService.isLocked(user) && !RoomKeys.fits(user.getMainHandItem(), user.getUUID())) {
            return conditionMessage("mr_president_no_key");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (RoomService.isRoomDimension(user)) return;

        if (!RoomService.isLocked(user)) {
            user.addEffect(new EffectInstance(InitStatusEffects.KEY_REMOVED.get(), Integer.MAX_VALUE, 0, false, false, false));
            RoomService.expelAll(world.getServer(), user.getUUID(), false);
            giveKey(user);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.CHAIN_BREAK, SoundCategory.PLAYERS, 1.0F, 0.8F);
        }
        else if (RoomKeys.fits(user.getMainHandItem(), user.getUUID())) {
            user.getMainHandItem().shrink(1);
            user.removeEffect(InitStatusEffects.KEY_REMOVED.get());
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.CHAIN_PLACE, SoundCategory.PLAYERS, 1.0F, 0.8F);
        }
    }

    private static void giveKey(LivingEntity user) {
        ItemStack key = RoomKeys.createFor(user);
        boolean added = user instanceof PlayerEntity && ((PlayerEntity) user).inventory.add(key);
        if (!added) {
            user.spawnAtLocation(key);
        }
    }

    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        return RoomService.isLocked(power.getUser()) ? key + ".insert" : key;
    }
}
