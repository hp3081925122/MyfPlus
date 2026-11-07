package com.hp.myfplus.registry;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.item.AncientBaitItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyfItems {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, Myfplus.MOD_ID);
    private static final ResourceKey<CreativeModeTab> MYF_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("meetyourfight", "meetyourfight"));

    public static final RegistryObject<Item> ANCIENT_FOSSIL_BAIT = REG.register("ancient_fossil_bait",
            () -> new AncientBaitItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    private MyfItems() {
    }

    public static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (MYF_TAB.equals(e.getTabKey())) e.accept(ANCIENT_FOSSIL_BAIT);
    }
}
