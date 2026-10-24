package com.rotpaddon.mrpresident.action;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;

/** Ability 3: throws absolutely everybody out of the room, the user included if he is inside. */
public class EmptyRoomAction extends RoomAction {

    public EmptyRoomAction(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void performServer(ServerWorld world, LivingEntity user, IStandPower power, ActionTarget target) {
        RoomService.expelAll(world.getServer(), user.getUUID(), true);
    }
}
