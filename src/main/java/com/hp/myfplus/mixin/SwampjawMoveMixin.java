package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.entity.boss.EnhancedSwampjawEntity;

import lykrast.meetyourfight.entity.SwampjawEntity;

/** 仅对强化沼牙生效：技能期间由强化实体自行控制速度；俯冲速度上限按阶段提高。 */
@Mixin(targets = "lykrast.meetyourfight.entity.SwampjawEntity$MoveHelperController")
public abstract class SwampjawMoveMixin {
    @Shadow(remap = false)
    private SwampjawEntity swampjaw;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void myfplus$steer(CallbackInfo ci) {
        if (swampjaw instanceof EnhancedSwampjawEntity e && e.myfplus$steering()) {
            e.myfplus$steer();
            ci.cancel();
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 3.0F, ordinal = 1))
    private float myfplus$swoopSpeed(float value) {
        return swampjaw instanceof EnhancedSwampjawEntity e ? value * e.myfplus$swoopSpeed() : value;
    }
}
