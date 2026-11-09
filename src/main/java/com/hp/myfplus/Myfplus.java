package com.hp.myfplus;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.registry.MyfEffects;
import com.hp.myfplus.registry.MyfEntities;
import com.hp.myfplus.registry.MyfItems;

import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
        MyfItems.REG.register(bus);
        MyfEffects.REG.register(bus);
        MyfItems.TABS.register(bus);
        bus.addListener(Myfplus::attributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MyfConfig.COMMON_SPEC, "myfplus-common.toml");
    }

    private static void attributes(EntityAttributeCreationEvent e) {
        e.put(MyfEntities.ENHANCED_SWAMPJAW.get(), SwampjawEntity.createAttributes().build());
        e.put(MyfEntities.ENHANCED_BELLRINGER.get(), BellringerEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 10).build());
        e.put(MyfEntities.ENHANCED_FORTUNA.get(), DameFortunaEntity.createAttributes().add(Attributes.MAX_HEALTH, DameFortunaEntity.HP * 2).add(Attributes.ATTACK_DAMAGE, 16).build());
        e.put(MyfEntities.ENHANCED_ROSALYNE.get(), RosalyneEntity.createAttributes().build());
    }
}
