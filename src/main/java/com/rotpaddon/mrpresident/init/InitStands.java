package com.rotpaddon.mrpresident.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.NoSummonStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.rotpaddon.mrpresident.AddonMain;
import com.rotpaddon.mrpresident.action.EmptyRoomAction;
import com.rotpaddon.mrpresident.action.EnterExitRoom;
import com.rotpaddon.mrpresident.action.KeyAction;
import com.rotpaddon.mrpresident.action.KidnapAction;
import com.rotpaddon.mrpresident.action.RestockSuppliesAction;
import com.rotpaddon.mrpresident.action.ShakeKeyAction;
import com.rotpaddon.mrpresident.action.TurtleFormAction;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), AddonMain.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), AddonMain.MOD_ID);

    // ======================================== Mr. President ========================================

    public static final RegistryObject<StandAction> ENTER_ROOM = ACTIONS.register("mr_president_enter_room",
            () -> new EnterExitRoom(new StandAction.Builder().cooldown(20)));

    public static final RegistryObject<StandAction> KIDNAP = ACTIONS.register("mr_president_kidnap",
            () -> new KidnapAction(new StandAction.Builder().cooldown(20 * 6).staminaCost(20).swingHand()));

    public static final RegistryObject<StandAction> EMPTY_ROOM = ACTIONS.register("mr_president_empty_room",
            () -> new EmptyRoomAction(new StandAction.Builder().cooldown(20 * 3)));

    public static final RegistryObject<StandAction> SHAKE_KEY = ACTIONS.register("mr_president_shake_key",
            () -> new ShakeKeyAction(new StandAction.Builder().cooldown(20 * 20).staminaCost(40)));

    public static final RegistryObject<StandAction> RESTOCK_SUPPLIES = ACTIONS.register("mr_president_restock",
            () -> new RestockSuppliesAction(new StandAction.Builder().cooldown(20 * 30).staminaCost(30)));

    public static final RegistryObject<StandAction> TURTLE_FORM = ACTIONS.register("mr_president_turtle_form",
            () -> new TurtleFormAction(new StandAction.Builder().cooldown(20 * 5)));

    public static final RegistryObject<StandAction> KEY = ACTIONS.register("mr_president_key",
            () -> new KeyAction(new StandAction.Builder().cooldown(20)));

    /** A "no summon" stand: there is no Stand entity, the turtle's shell is the Stand. */
    public static final RegistryObject<StandType<StandStats>> MR_PRESIDENT = STANDS.register("mr_president",
            () -> new NoSummonStandType.Builder<StandStats>()
                    .color(0x00AFAF)
                    .storyPartName(ModStandsInit.PART_5_NAME)
                    .leftClickHotbar(
                            KIDNAP.get(),
                            SHAKE_KEY.get(),
                            EMPTY_ROOM.get())
                    .rightClickHotbar(
                            ENTER_ROOM.get(),
                            RESTOCK_SUPPLIES.get(),
                            TURTLE_FORM.get(),
                            KEY.get())
                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .power(0)
                            .speed(0)
                            .range(0)
                            .durability(14)
                            .precision(0)
                            .build())
                    .build());
}
