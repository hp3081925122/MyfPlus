package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;

import lykrast.meetyourfight.entity.RosalyneEntity;

/** 仅对二阶段的强化罗莎琳生效：垂直下劈结束后接一次玫瑰扇刃。 */
@Mixin(targets = "lykrast.meetyourfight.entity.RosalyneEntity$VerticalCrashAttack")
public abstract class RosalyneCrashMixin {
    @Shadow(remap = false)
    private RosalyneEntity rosalyne;

    @Inject(method = "stop", at = @At("TAIL"))
    private void myfplus$followUp(CallbackInfo ci) {
        if (rosalyne instanceof EnhancedRosalyneEntity e) e.myfplus$afterCrash();
    }
}
