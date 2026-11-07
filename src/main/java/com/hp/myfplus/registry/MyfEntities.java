package com.hp.myfplus.registry;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.entity.BellDomeEntity;
import com.hp.myfplus.entity.BellWaveEntity;
import com.hp.myfplus.entity.AllInEntity;
import com.hp.myfplus.entity.FinBladeEntity;
import com.hp.myfplus.entity.FossilJawEntity;
import com.hp.myfplus.entity.ChipEntity;
import com.hp.myfplus.entity.DiceEntity;
import com.hp.myfplus.entity.DuelRingEntity;
import com.hp.myfplus.entity.PetalBladeEntity;
import com.hp.myfplus.entity.ThornEntity;
import com.hp.myfplus.entity.RouletteEntity;
import com.hp.myfplus.entity.SlotMachineEntity;
import com.hp.myfplus.entity.EchoMarkEntity;
import com.hp.myfplus.entity.PhantomBellEntity;
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
    public static final RegistryObject<EntityType<FossilJawEntity>> FOSSIL_JAW = misc("fossil_jaw", FossilJawEntity::new, 0.5f, 0.2f, 100);
    public static final RegistryObject<EntityType<FinBladeEntity>> FIN_BLADE = misc("fin_blade", FinBladeEntity::new, 0.8f, 0.4f, 100);
    public static final RegistryObject<EntityType<BellWaveEntity>> BELL_WAVE = misc("bell_wave", BellWaveEntity::new, 0.5f, 0.2f, 40);
    public static final RegistryObject<EntityType<EchoMarkEntity>> ECHO_MARK = misc("echo_mark", EchoMarkEntity::new, 0.5f, 0.2f, 40);
    public static final RegistryObject<EntityType<PhantomBellEntity>> PHANTOM_BELL = misc("phantom_bell", PhantomBellEntity::new, 2.5f, 3.0f, 40);
    public static final RegistryObject<EntityType<BellDomeEntity>> BELL_DOME = misc("bell_dome", BellDomeEntity::new, 0.5f, 0.5f, 40);
    public static final RegistryObject<EntityType<DiceEntity>> DICE = misc("dice", DiceEntity::new, 0.6f, 0.6f, 1);
    public static final RegistryObject<EntityType<ChipEntity>> CHIP = misc("chip", ChipEntity::new, 0.4f, 0.4f, 1);
    public static final RegistryObject<EntityType<RouletteEntity>> ROULETTE = misc("roulette", RouletteEntity::new, 0.5f, 0.2f, 40);
    public static final RegistryObject<EntityType<SlotMachineEntity>> SLOT_MACHINE = misc("slot_machine", SlotMachineEntity::new, 2.0f, 1.6f, 40);
    public static final RegistryObject<EntityType<AllInEntity>> ALL_IN = misc("all_in", AllInEntity::new, 0.5f, 0.5f, 40);
    public static final RegistryObject<EntityType<ThornEntity>> THORN = misc("thorn", ThornEntity::new, 0.9f, 1.6f, 40);
    public static final RegistryObject<EntityType<PetalBladeEntity>> PETAL_BLADE = misc("petal_blade", PetalBladeEntity::new, 0.6f, 0.6f, 1);
    public static final RegistryObject<EntityType<DuelRingEntity>> DUEL_RING = misc("duel_ring", DuelRingEntity::new, 0.5f, 0.5f, 40);

    private MyfEntities() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> misc(String name, EntityType.EntityFactory<T> factory, float w, float h, int interval) {
        return REG.register(name, () -> EntityType.Builder.of(factory, MobCategory.MISC).sized(w, h).clientTrackingRange(8).updateInterval(interval)
                .noSave().fireImmune().build(Myfplus.MOD_ID + ":" + name));
    }
}
