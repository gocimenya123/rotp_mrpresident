package com.rotpaddon.mrpresident.init;

import com.rotpaddon.mrpresident.AddonMain;
import com.rotpaddon.mrpresident.effect.KeyRemovedEffect;
import com.rotpaddon.mrpresident.effect.TurtleFormEffect;

import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitStatusEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, AddonMain.MOD_ID);

    public static final RegistryObject<Effect> TURTLE_FORM = EFFECTS.register("turtle_form", TurtleFormEffect::new);
    public static final RegistryObject<Effect> KEY_REMOVED = EFFECTS.register("key_removed", KeyRemovedEffect::new);
}
