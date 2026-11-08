package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.core.MyfBossHost;

import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** 仅对强化命运夫人生效：跳过本体按血量阈值洗牌的阶段推进，改由技能大脑在一阶段致死时转入二阶段，本体命运夫人不受影响。 */
@Mixin(DameFortunaEntity.class)
public abstract class DameFortunaEntityMixin extends BossEntity {
    @Shadow(remap = false)
    private int attackCooldown;
    @Shadow(remap = false)
    private int shuffleAttackWait;
    @Shadow(remap = false)
    private int phase;

    protected DameFortunaEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Shadow(remap = false)
    public abstract int getPhase();

    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void myfplus$phases(CallbackInfo ci) {
        if (!((Object) this instanceof MyfBossHost host) || host.myfplus$brain() == null) return;
        if (attackCooldown > 0) attackCooldown--;
        if (shuffleAttackWait > 0) shuffleAttackWait--;
        if (phase != getPhase()) phase = getPhase();
        super.customServerAiStep();
        ci.cancel();
    }
}
