package com.hp.myfplus.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;

import lykrast.meetyourfight.entity.BossFlyingEntity;
import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.level.Level;

@Mixin(SwampjawEntity.class)
public abstract class SwampjawEntityMixin extends BossFlyingEntity implements MyfBossHost {
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL = SynchedEntityData.defineId(SwampjawEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL_START = SynchedEntityData.defineId(SwampjawEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_PHASE = SynchedEntityData.defineId(SwampjawEntity.class, EntityDataSerializers.INT);
    @Unique
    @Nullable
    private SwampjawBrain myfplus$brain;

    protected SwampjawEntityMixin(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void myfplus$init(EntityType<? extends SwampjawEntity> type, Level level, CallbackInfo ci) {
        if (level.isClientSide || !MyfConfig.SWAMPJAW.enabled()) return;
        SwampjawBrain brain = new SwampjawBrain((SwampjawEntity) (Object) this);
        myfplus$brain = brain;
        moveControl = brain.flight;
        brain.install(goalSelector);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void myfplus$defineData(CallbackInfo ci) {
        entityData.define(MYFPLUS_SKILL, 0);
        entityData.define(MYFPLUS_SKILL_START, 0);
        entityData.define(MYFPLUS_PHASE, 1);
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
