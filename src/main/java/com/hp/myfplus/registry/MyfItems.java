package com.hp.myfplus.registry;

import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.entity.boss.EnhancedBellringerEntity;
import com.hp.myfplus.entity.boss.EnhancedFortunaEntity;
import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;
import com.hp.myfplus.entity.boss.EnhancedSwampjawEntity;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.item.AccessoryItem;
import com.hp.myfplus.item.EnhancedSummonItem;
import com.hp.myfplus.item.TrophyItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyfItems {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, Myfplus.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Myfplus.MOD_ID);

    public static final RegistryObject<Item> ANCIENT_FOSSIL_BAIT = summon("ancient_fossil_bait", EnhancedSwampjawEntity::spawn);
    public static final RegistryObject<Item> RESONANT_HAUNTED_BELL = summon("resonant_haunted_bell", EnhancedBellringerEntity::spawn);
    public static final RegistryObject<Item> ALL_IN_DEVILS_ANTE = summon("all_in_devils_ante", EnhancedFortunaEntity::spawn);
    public static final RegistryObject<Item> SUNSET_DUSK_KEY = summon("sunset_dusk_key", EnhancedRosalyneEntity::spawn);
    public static final RegistryObject<Item> ANCIENT_JAWBONE = trophy("ancient_jawbone");
    public static final RegistryObject<Item> RESONANT_CLAPPER = trophy("resonant_clapper");
    public static final RegistryObject<Item> HOUSE_GILDED_DIE = trophy("house_gilded_die");
    public static final RegistryObject<Item> SUNSET_ROSE = trophy("sunset_rose");
    public static final RegistryObject<Item> FOSSIL_FANG_BRACELET = accessory("fossil_fang_bracelet", () -> new Object[]{pct(MyfConfig.FANG_CHANCE.get()), fmt(MyfConfig.FANG_RADIUS.get()), fmt(MyfConfig.FANG_BASE.get()), pct(MyfConfig.FANG_SCALE.get()), fmt(MyfConfig.FANG_COOLDOWN.get() / 20.0)}, null);
    public static final RegistryObject<Item> RESONANT_BELT_BELL = accessory("resonant_belt_bell", () -> new Object[]{MyfConfig.BELL_HITS.get(), pct(MyfConfig.BELL_RATIO.get())}, null);
    public static final RegistryObject<Item> HOUSE_DIE_RING = accessory("house_die_ring", () -> new Object[]{pct(MyfConfig.DIE_CHANCE.get()), fmt(MyfConfig.DIE_BASE.get()), pct(MyfConfig.DIE_SCALE.get()), fmt(MyfConfig.DIE_COOLDOWN.get() / 20.0), fmt(MyfConfig.DIE_LUCK.get())}, MyfConfig.DIE_LUCK::get);
    public static final RegistryObject<Item> SUNSET_ROSE_CLOAK = accessory("sunset_rose_cloak", () -> new Object[]{fmt(MyfConfig.CLOAK_BASE.get()), pct(MyfConfig.CLOAK_SCALE.get()), fmt(MyfConfig.CLOAK_STUN.get() / 20.0), fmt(MyfConfig.CLOAK_COOLDOWN.get() / 20.0)}, null);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.myfplus"))
            .icon(() -> ALL_IN_DEVILS_ANTE.get().getDefaultInstance())
            .displayItems((params, out) -> {
                out.accept(ANCIENT_FOSSIL_BAIT.get());
                out.accept(RESONANT_HAUNTED_BELL.get());
                out.accept(ALL_IN_DEVILS_ANTE.get());
                out.accept(SUNSET_DUSK_KEY.get());
                out.accept(ANCIENT_JAWBONE.get());
                out.accept(RESONANT_CLAPPER.get());
                out.accept(HOUSE_GILDED_DIE.get());
                out.accept(SUNSET_ROSE.get());
                out.accept(FOSSIL_FANG_BRACELET.get());
                out.accept(RESONANT_BELT_BELL.get());
                out.accept(HOUSE_DIE_RING.get());
                out.accept(SUNSET_ROSE_CLOAK.get());
            })
            .build());

    private MyfItems() {
    }

    private static String pct(double v) {
        return String.valueOf(Math.round(v * 100));
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(Math.round(v * 10) / 10.0);
    }

    private static RegistryObject<Item> accessory(String name, Supplier<Object[]> args, @Nullable DoubleSupplier luck) {
        return REG.register(name, () -> new AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), args, luck));
    }

    private static RegistryObject<Item> trophy(String name) {
        return REG.register(name, () -> new TrophyItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    }

    private static RegistryObject<Item> summon(String name, BiConsumer<Player, ServerLevel> spawner) {
        return REG.register(name, () -> new EnhancedSummonItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), spawner));
    }
}
