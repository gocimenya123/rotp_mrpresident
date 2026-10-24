package com.rotpaddon.mrpresident.room;

import java.util.UUID;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.MrPresidentKeyItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

/** The base mod's Turtle Key item, assigned to a player the same way the base mod assigns it to a turtle. */
public final class RoomKeys {
    private static final String OWNER_TAG = "TurtleEntity";
    /** Set only on keys pulled out by the Remove Key ability, so turtle keys and the master key stay untouched. */
    private static final String PLAYER_KEY_TAG = "MrPresidentAddonKey";

    private RoomKeys() {}

    public static ItemStack createFor(LivingEntity owner) {
        ItemStack key = new ItemStack(ModItems.MR_PRESIDENT_KEY.get());
        key.getOrCreateTag().putUUID(OWNER_TAG, owner.getUUID());
        key.getOrCreateTag().putBoolean(PLAYER_KEY_TAG, true);
        key.setHoverName(new TranslationTextComponent(key.getDescriptionId() + ".named", owner.getDisplayName()));
        return key;
    }

    /** A key made by the Remove Key ability (it is a weapon: 9 damage, 1.8 attack speed). */
    public static boolean isPlayerKey(ItemStack stack) {
        if (!(stack.getItem() instanceof MrPresidentKeyItem) || ((MrPresidentKeyItem) stack.getItem()).masterKey) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getBoolean(PLAYER_KEY_TAG);
    }

    /** The owner's own key, or the creative master key. */
    public static boolean fits(ItemStack stack, UUID owner) {
        if (!(stack.getItem() instanceof MrPresidentKeyItem)) return false;
        if (((MrPresidentKeyItem) stack.getItem()).masterKey) return true;
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.hasUUID(OWNER_TAG) && tag.getUUID(OWNER_TAG).equals(owner);
    }
}
