package com.hp.myfplus.effect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** 轮盘禁锢：命运夫人强化版转盘施加，移速 -30%；带此效果的玩家被轮盘边缘挡住，走不出转盘（围困逻辑在 RouletteEntity）。牛奶无法解除。 */
public class RouletteBoundEffect extends MobEffect {
    public RouletteBoundEffect() {
        super(MobEffectCategory.HARMFUL, 0xC82828);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "8c1f4a2e-3d6b-4b7e-9f21-5a0c7e3d9b14", -0.30, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }
}
