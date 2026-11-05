package com.hp.myfplus.registry;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.entity.BoneHookEntity;
import com.hp.myfplus.entity.GasCloudEntity;
import com.hp.myfplus.entity.MudGlobEntity;
import com.hp.myfplus.entity.MudPitEntity;
import com.hp.myfplus.entity.SwampBubbleEntity;
import com.hp.myfplus.fx.FxEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyfEntities {
    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Myfplus.MOD_ID);

    public static final RegistryObject<EntityType<FxEntity>> FX = misc("fx", FxEntity::new, 0.2f, 0.2f, 20);
    public static final RegistryObject<EntityType<MudPitEntity>> MUD_PIT = misc("mud_pit", MudPitEntity::new, 0.5f, 0.2f, 40);
    public static final RegistryObject<EntityType<MudGlobEntity>> MUD_GLOB = misc("mud_glob", MudGlobEntity::new, 0.6f, 0.6f, 20);
    public static final RegistryObject<EntityType<BoneHookEntity>> BONE_HOOK = misc("bone_hook", BoneHookEntity::new, 0.5f, 0.5f, 1);
    public static final RegistryObject<EntityType<SwampBubbleEntity>> SWAMP_BUBBLE = misc("swamp_bubble", SwampBubbleEntity::new, 0.8f, 0.8f, 2);
    public static final RegistryObject<EntityType<GasCloudEntity>> GAS_CLOUD = misc("gas_cloud", GasCloudEntity::new, 0.5f, 0.2f, 40);

    private MyfEntities() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> misc(String name, EntityType.EntityFactory<T> factory, float w, float h, int interval) {
        return REG.register(name, () -> EntityType.Builder.of(factory, MobCategory.MISC).sized(w, h).clientTrackingRange(8).updateInterval(interval)
                .noSave().fireImmune().build(Myfplus.MOD_ID + ":" + name));
    }
}
