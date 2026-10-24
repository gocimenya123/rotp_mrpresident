package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;

/** Ability 1: step into the shell, or back out when already inside. */
public class EnterExitRoom extends RoomAction {

    public EnterExitRoom(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        return RoomService.isRoomDimension(user) ? ActionConditionResult.POSITIVE : requireUnlocked(user);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (RoomService.isRoomDimension(user)) {
            RoomService.teleportOut(user, user.getUUID(), world.getServer());
        }
        else if (!RoomService.isLocked(user)) {
            RoomService.teleportInto(user, user.getUUID(), world.getServer(), 0);
        }
    }

    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        return RoomService.isRoomDimension(power.getUser()) ? key + ".exit" : key;
    }
}
