package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.entity.boss.EnhancedBellringerEntity;

import lykrast.meetyourfight.entity.BellringerEntity;

/** 仅对强化敲钟人生效：每轮攻击次数增加、攻击间隔缩短。 */
@Mixin(targets = "lykrast.meetyourfight.entity.BellringerEntity$BurstAttack")
public abstract class BellringerBurstMixin {
    @Shadow(remap = false)
    private BellringerEntity ringer;
    @Shadow(remap = false)
    private int attackRemaining;

    @Inject(method = "start", at = @At("TAIL"))
    private void myfplus$moreHits(CallbackInfo ci) {
        if (ringer instanceof EnhancedBellringerEntity e) attackRemaining += e.myfplus$extraHits();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int myfplus$interval(int value) {
        return ringer instanceof EnhancedBellringerEntity e ? e.myfplus$interval() : value;
    }
}
