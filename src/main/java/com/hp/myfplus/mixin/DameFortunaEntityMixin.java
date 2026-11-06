package com.hp.myfplus.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hp.myfplus.boss.fortuna.FortunaBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;

import lykrast.meetyourfight.entity.BossEntity;
import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Mixin(DameFortunaEntity.class)
public abstract class DameFortunaEntityMixin extends BossEntity implements MyfBossHost {
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_SKILL_START = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> MYFPLUS_PHASE = SynchedEntityData.defineId(DameFortunaEntity.class, EntityDataSerializers.INT);
    @Unique
    @Nullable
    private FortunaBrain myfplus$brain;

    protected DameFortunaEntityMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void myfplus$init(EntityType<? extends DameFortunaEntity> type, Level level, CallbackInfo ci) {
        if (level.isClientSide || !MyfConfig.FORTUNA.enabled()) return;
        FortunaBrain brain = new FortunaBrain((DameFortunaEntity) (Object) this);
        myfplus$brain = brain;
        brain.install(goalSelector);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void myfplus$defineData(CallbackInfo ci) {
        entityData.define(MYFPLUS_SKILL, 0);
        entityData.define(MYFPLUS_SKILL_START, 0);
        entityData.define(MYFPLUS_PHASE, 1);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void myfplus$bet(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        FortunaBrain brain = myfplus$brain;
        if (brain == null || !brain.betting || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (source.getEntity() instanceof LivingEntity attacker && attacker != (Object) this && amount > 0) {
            brain.bet += amount;
            playSound(SoundEvents.CHAIN_PLACE, 1.0f, 1.2f + Math.min(0.8f, brain.bet / Math.max(1f, getMaxHealth() * 0.15f)));
        }
        cir.setReturnValue(false);
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
