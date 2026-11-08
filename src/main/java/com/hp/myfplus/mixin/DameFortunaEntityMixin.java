package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.core.EnhancedBoss;

import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** 仅对进入二阶段的强化命运夫人生效：不再按血量阈值洗牌，血量打空即死亡。 */
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
    private void myfplus$noShuffle(CallbackInfo ci) {
        if (!((Object) this instanceof EnhancedBoss boss) || !boss.myfplus$phase2()) return;
        if (attackCooldown > 0) attackCooldown--;
        if (shuffleAttackWait > 0) shuffleAttackWait--;
        if (phase != getPhase()) phase = getPhase();
        super.customServerAiStep();
        ci.cancel();
    }
}
