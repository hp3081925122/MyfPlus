package com.hp.myfplus.registry;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.entity.EnhancedMineEntity;
import com.hp.myfplus.entity.boss.EnhancedBellringerEntity;
import com.hp.myfplus.entity.boss.EnhancedFortunaEntity;
import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;
import com.hp.myfplus.entity.boss.EnhancedSwampjawEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyfEntities {
    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Myfplus.MOD_ID);

    public static final RegistryObject<EntityType<EnhancedSwampjawEntity>> ENHANCED_SWAMPJAW = boss("enhanced_swampjaw", EnhancedSwampjawEntity::new, 2.6f, 1.6f);
    public static final RegistryObject<EntityType<EnhancedBellringerEntity>> ENHANCED_BELLRINGER = boss("enhanced_bellringer", EnhancedBellringerEntity::new, 0.6f, 1.95f);
    public static final RegistryObject<EntityType<EnhancedFortunaEntity>> ENHANCED_FORTUNA = boss("enhanced_dame_fortuna", EnhancedFortunaEntity::new, 0.6f, 2.325f);
    public static final RegistryObject<EntityType<EnhancedRosalyneEntity>> ENHANCED_ROSALYNE = boss("enhanced_rosalyne", EnhancedRosalyneEntity::new, 0.6f, 1.95f);


    public static final RegistryObject<EntityType<EnhancedMineEntity>> MINE = misc("mine", EnhancedMineEntity::new, 0.75f, 0.75f, 2);

    private MyfEntities() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> boss(String name, EntityType.EntityFactory<T> factory, float w, float h) {
        return REG.register(name, () -> EntityType.Builder.of(factory, MobCategory.MONSTER).sized(w, h).setUpdateInterval(1).setTrackingRange(128)
                .setShouldReceiveVelocityUpdates(true).build(Myfplus.MOD_ID + ":" + name));
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> misc(String name, EntityType.EntityFactory<T> factory, float w, float h, int interval) {
        return REG.register(name, () -> EntityType.Builder.of(factory, MobCategory.MISC).sized(w, h).clientTrackingRange(8).updateInterval(interval)
                .noSave().fireImmune().build(Myfplus.MOD_ID + ":" + name));
    }
}
