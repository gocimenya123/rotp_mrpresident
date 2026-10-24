package com.rotpaddon.mrpresident;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rotpaddon.mrpresident.init.InitEntities;
import com.rotpaddon.mrpresident.init.InitStands;
import com.rotpaddon.mrpresident.init.InitStatusEffects;
import com.rotpaddon.mrpresident.network.AddonNetwork;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Mr. President addon for Ripples of the Past.
 * Builds on the room systems RotP already ships in jojo.mrpresident (pocket dimension, room, teleporters, key, exit gem).
 */
@Mod(AddonMain.MOD_ID)
public class AddonMain {
    public static final String MOD_ID = "rotp_mrpresident";
    public static final Logger LOGGER = LogManager.getLogger();

    public AddonMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        InitEntities.ENTITIES.register(modEventBus);
        InitStatusEffects.EFFECTS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AddonNetwork::init);
    }
}
