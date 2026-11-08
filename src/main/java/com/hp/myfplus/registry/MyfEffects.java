package com.hp.myfplus.registry;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.effect.RouletteBoundEffect;
import com.hp.myfplus.effect.StunnedEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyfEffects {
    public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Myfplus.MOD_ID);

    public static final RegistryObject<MobEffect> ROULETTE_BOUND = REG.register("roulette_bound", RouletteBoundEffect::new);
    public static final RegistryObject<MobEffect> STUNNED = REG.register("stunned", StunnedEffect::new);

    private MyfEffects() {
    }
}
