package com.hp.myfplus.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.boss.rosalyne.RosalyneBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;

import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.RosalyneEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Mixin(RosalyneEntity.class)
public abstract class RosalyneEntityMixin extends BossEntity implements MyfBossHost {
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL_START = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_PHASE = SynchedEntityData.defineId(RosalyneEntity.class, EntityDataSerializers.INT);
    @Shadow(remap = false)
    private int phase;
    @Shadow(remap = false)
    public int attackCooldown;
    @Shadow(remap = false)
    @Final
    private TargetingConditions spiritCountTargeting;
    @Unique
    @Nullable
    private RosalyneBrain myfplus$brain;

    protected RosalyneEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Shadow(remap = false)
    public abstract int getPhase();

    @Shadow(remap = false)
    public abstract void setPhase(int phase);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void myfplus$init(EntityType<? extends RosalyneEntity> type, Level level, CallbackInfo ci) {
        if (level.isClientSide || !MyfConfig.ROSALYNE.enabled()) return;
        RosalyneBrain brain = new RosalyneBrain((RosalyneEntity) (Object) this);
        myfplus$brain = brain;
        brain.install(goalSelector);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void myfplus$defineData(CallbackInfo ci) {
        entityData.define(MYFPLUS_SKILL, 0);
        entityData.define(MYFPLUS_SKILL_START, 0);
        entityData.define(MYFPLUS_PHASE, 1);
    }

    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void myfplus$phases(CallbackInfo ci) {
        if (myfplus$brain == null) return;
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

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void myfplus$save(CompoundTag tag, CallbackInfo ci) {
        if (myfplus$brain == null) return;
        CompoundTag t = new CompoundTag();
        myfplus$brain.save(t);
        tag.put("myfplus", t);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void myfplus$load(CompoundTag tag, CallbackInfo ci) {
        if (myfplus$brain != null && tag.contains("myfplus")) myfplus$brain.load(tag.getCompound("myfplus"));
    }

    @Override
    public int myfplus$skill() {
        return entityData.get(MYFPLUS_SKILL);
    }

    @Override
    public int myfplus$skillAge() {
        return (int) level().getGameTime() - entityData.get(MYFPLUS_SKILL_START);
    }

    @Override
    public void myfplus$setSkill(int skill) {
        if (entityData.get(MYFPLUS_SKILL) == skill) return;
        entityData.set(MYFPLUS_SKILL, skill);
        entityData.set(MYFPLUS_SKILL_START, (int) level().getGameTime());
    }

    @Override
    public int myfplus$phase() {
        return entityData.get(MYFPLUS_PHASE);
    }

    @Override
    public void myfplus$setPhase(int phase) {
        entityData.set(MYFPLUS_PHASE, phase);
    }

    @Override
    @Nullable
    public BossBrain<?> myfplus$brain() {
        return myfplus$brain;
    }
}
