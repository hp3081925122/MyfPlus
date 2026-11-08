package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hp.myfplus.entity.boss.EnhancedBellringerEntity;

import lykrast.meetyourfight.entity.BellringerEntity;

/** 仅对强化敲钟人生效：进入二阶段时强制再触发一次本体狂暴攻击。 */
@Mixin(targets = "lykrast.meetyourfight.entity.BellringerEntity$RageAttack")
public abstract class BellringerRageMixin {
    @Shadow(remap = false)
    private BellringerEntity ringer;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void myfplus$forced(CallbackInfoReturnable<Boolean> cir) {
        if (ringer instanceof EnhancedBellringerEntity e && e.myfplus$rageForced() && ringer.attackCooldown <= 0
                && ringer.getTarget() != null && ringer.getTarget().isAlive()) cir.setReturnValue(true);
    }

    @Inject(method = "start", at = @At("TAIL"))
    private void myfplus$started(CallbackInfo ci) {
        if (ringer instanceof EnhancedBellringerEntity e) e.myfplus$rageStarted();
    }
}
