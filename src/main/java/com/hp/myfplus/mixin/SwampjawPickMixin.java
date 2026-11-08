package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.entity.boss.EnhancedSwampjawEntity;

import lykrast.meetyourfight.entity.SwampjawEntity;

/** 仅对强化沼牙生效：减少每轮俯冲前投放的水雷数。 */
@Mixin(targets = "lykrast.meetyourfight.entity.SwampjawEntity$PickAttackGoal")
public abstract class SwampjawPickMixin {
    @Shadow(remap = false)
    private int bombLeft;
    @Shadow(remap = false)
    private SwampjawEntity swampjaw;

    @Inject(method = "tick", at = @At("HEAD"))
    private void myfplus$bombs(CallbackInfo ci) {
        if (swampjaw instanceof EnhancedSwampjawEntity e && bombLeft > e.myfplus$bombs()) bombLeft = e.myfplus$bombs();
    }
}
