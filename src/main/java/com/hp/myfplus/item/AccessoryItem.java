package com.hp.myfplus.item;

import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class AccessoryItem extends Item implements ICurioItem {
    private final Supplier<Object[]> args;
    @Nullable
    private final DoubleSupplier luck;

    public AccessoryItem(Properties properties, Supplier<Object[]> args, @Nullable DoubleSupplier luck) {
        super(properties);
        this.args = args;
        this.luck = luck;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        if (luck != null && luck.getAsDouble() != 0) map.put(Attributes.LUCK, new AttributeModifier(uuid, "myfplus accessory luck", luck.getAsDouble(), AttributeModifier.Operation.ADDITION));
        return map;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc", args.get()).withStyle(ChatFormatting.GRAY));
    }
}
