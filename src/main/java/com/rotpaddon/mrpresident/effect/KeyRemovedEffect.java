package com.rotpaddon.mrpresident.effect;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

/**
 * Marker for "the key is out of the shell". A hidden, practically endless vanilla effect: it is saved with
 * the entity and synced to clients by vanilla, and milk can't remove it.
 */
public class KeyRemovedEffect extends Effect {

    public KeyRemovedEffect() {
        super(EffectType.NEUTRAL, 0x808080);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }
}
