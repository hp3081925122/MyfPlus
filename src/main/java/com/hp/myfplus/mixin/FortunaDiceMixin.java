package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hp.myfplus.entity.boss.EnhancedFortunaEntity;

import lykrast.meetyourfight.entity.DameFortunaEntity;

/** 仅对强化命运夫人生效：骰子炸弹数量增加。 */
@Mixin(targets = "lykrast.meetyourfight.entity.DameFortunaEntity$DiceAttack")
public abstract class FortunaDiceMixin {
    @Shadow(remap = false)
    private DameFortunaEntity dame;

    @Inject(method = "getAttackCount", at = @At("RETURN"), cancellable = true, remap = false)
    private void myfplus$moreDice(CallbackInfoReturnable<Integer> cir) {
        if (dame instanceof EnhancedFortunaEntity e) cir.setReturnValue(cir.getReturnValue() + e.myfplus$extraDice());
    }
}
