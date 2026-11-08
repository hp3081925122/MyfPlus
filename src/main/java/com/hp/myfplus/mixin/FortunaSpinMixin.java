package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.hp.myfplus.entity.boss.EnhancedFortunaEntity;

import lykrast.meetyourfight.entity.DameFortunaEntity;

/** 仅对强化命运夫人生效：旋转追击更快、撒筹码更密。 */
@Mixin(targets = "lykrast.meetyourfight.entity.DameFortunaEntity$SpinAttack")
public abstract class FortunaSpinMixin {
    @Shadow(remap = false)
    private DameFortunaEntity dame;

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 0.4))
    private double myfplus$chaseSpeed(double value) {
        return dame instanceof EnhancedFortunaEntity e ? value * e.myfplus$spinSpeed() : value;
    }

    @ModifyConstant(method = "tick", constant = {@Constant(intValue = 6, ordinal = 0), @Constant(intValue = 12), @Constant(intValue = 20)})
    private int myfplus$chipInterval(int value) {
        return dame instanceof EnhancedFortunaEntity e ? Math.max(3, (int) Math.round(value * e.myfplus$spinInterval())) : value;
    }
}
