package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;

import lykrast.meetyourfight.entity.RosalyneEntity;

/** 仅对强化罗莎琳生效：前进挥砍接近更快、挥砍间隔更短。 */
@Mixin(targets = "lykrast.meetyourfight.entity.RosalyneEntity$AdvanceAndSwingAttack")
public abstract class RosalyneSwingMixin {
    @Shadow(remap = false)
    private RosalyneEntity rosalyne;

    @ModifyConstant(method = {"start", "tick"}, constant = @Constant(doubleValue = 4.0))
    private double myfplus$approach(double value) {
        return rosalyne instanceof EnhancedRosalyneEntity ? value * 1.3 : value;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int myfplus$swingInterval(int value) {
        return rosalyne instanceof EnhancedRosalyneEntity ? 15 : value;
    }
}
