package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Shared plumbing for every Mr. President ability: server-only execution and common conditions. */
public abstract class RoomAction extends StandAction {

    protected RoomAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (world.isClientSide()) return;
        performServer((ServerWorld) world, user, power, target);
    }

    protected abstract void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target);

    protected static ActionConditionResult requireOutside(LivingEntity user) {
        return RoomService.isRoomDimension(user) ? conditionMessage("mr_president_outside_only") : ActionConditionResult.POSITIVE;
    }

    protected static ActionConditionResult requireUnlocked(LivingEntity user) {
        return RoomService.isLocked(user) ? conditionMessage("mr_president_room_locked") : ActionConditionResult.POSITIVE;
    }
}
