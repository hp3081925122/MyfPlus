package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.entity.boss.EnhancedRosalyneEntity;

import lykrast.meetyourfight.entity.RosalyneEntity;

/** 仅对二阶段的强化罗莎琳生效：绕圈冲刺次数 +2。 */
@Mixin(targets = "lykrast.meetyourfight.entity.RosalyneEntity$CircleAndDashAttack")
public abstract class RosalyneDashMixin {
    @Shadow(remap = false)
    private RosalyneEntity rosalyne;
    @Shadow(remap = false)
    private int swingsLeft;

    @Inject(method = "start", at = @At("TAIL"))
    private void myfplus$moreDashes(CallbackInfo ci) {
        if (rosalyne instanceof EnhancedRosalyneEntity e && e.myfplus$phase2()) swingsLeft += 2;
    }
}
