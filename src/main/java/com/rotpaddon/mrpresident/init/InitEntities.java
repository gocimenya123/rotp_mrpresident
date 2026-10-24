package com.rotpaddon.mrpresident.init;

import com.rotpaddon.mrpresident.AddonMain;
import com.rotpaddon.mrpresident.entity.KeyMarkerEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, AddonMain.MOD_ID);

    public static final RegistryObject<EntityType<KeyMarkerEntity>> KEY_MARKER = ENTITIES.register("key_marker",
            () -> EntityType.Builder.<KeyMarkerEntity>of(KeyMarkerEntity::new, EntityClassification.MISC)
                    .sized(0.5F, 0.5F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(20)
                    .build(AddonMain.MOD_ID + ":key_marker"));
}
