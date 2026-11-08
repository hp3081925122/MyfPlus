package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.core.MyfBossHost;

import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** 仅对强化罗莎琳生效：跳过本体的阶段推进，改由技能大脑接管，本体罗莎琳不受影响。 */
@Mixin(RosalyneEntity.class)
public abstract class RosalyneEntityMixin extends BossEntity {
    @Shadow(remap = false)
    private int phase;
    @Shadow(remap = false)
    public int attackCooldown;
    @Shadow(remap = false)
    @Final
    private TargetingConditions spiritCountTargeting;

    protected RosalyneEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Shadow(remap = false)
    public abstract int getPhase();

    @Shadow(remap = false)
    public abstract void setPhase(int phase);

    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void myfplus$phases(CallbackInfo ci) {
        if (!((Object) this instanceof MyfBossHost host) || host.myfplus$brain() == null) return;
        if (attackCooldown > 0) attackCooldown--;
        if (phase != getPhase()) phase = getPhase();
        if (phase == RosalyneEntity.ENCASED && tickCount % 10 == 0
                && level().getNearbyEntities(RoseSpiritEntity.class, spiritCountTargeting, this, getBoundingBox().inflate(32)).isEmpty()) {
            setPhase(RosalyneEntity.BREAKING_OUT);
            phase = RosalyneEntity.BREAKING_OUT;
        }
        super.customServerAiStep();
        ci.cancel();
    }
}
