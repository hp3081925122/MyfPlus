package com.hp.myfplus;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.registry.MYFEntities;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Myfplus.MOD_ID)
public final class Myfplus {
    public static final String MOD_ID = "myfplus";

    public Myfplus(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        MyfEntities.REG.register(bus);
        bus.addListener(Myfplus::attributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MyfConfig.COMMON_SPEC, "myfplus-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MyfConfig.CLIENT_SPEC, "myfplus-client.toml");
    }

    private static void attributes(EntityAttributeModificationEvent e) {
        if (!e.has(MYFEntities.BELLRINGER.get(), Attributes.ATTACK_DAMAGE)) e.add(MYFEntities.BELLRINGER.get(), Attributes.ATTACK_DAMAGE, 10);
        if (!e.has(MYFEntities.DAME_FORTUNA.get(), Attributes.ATTACK_DAMAGE)) e.add(MYFEntities.DAME_FORTUNA.get(), Attributes.ATTACK_DAMAGE, 16);
    }
}
