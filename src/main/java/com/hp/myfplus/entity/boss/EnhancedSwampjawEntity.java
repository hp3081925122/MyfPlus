package com.hp.myfplus.entity.boss;

import javax.annotation.Nullable;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EnhancedSwampjawEntity extends SwampjawEntity implements MyfBossHost {
    private static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(EnhancedSwampjawEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL_START = SynchedEntityData.defineId(EnhancedSwampjawEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(EnhancedSwampjawEntity.class, EntityDataSerializers.INT);
    @Nullable
    private SwampjawBrain brain;

    public EnhancedSwampjawEntity(EntityType<? extends EnhancedSwampjawEntity> type, Level level) {
        super(type, level);
        if (level.isClientSide || !MyfConfig.SWAMPJAW.enabled()) return;
        brain = new SwampjawBrain(this);
        moveControl = brain.flight;
        brain.install(goalSelector);
    }

    @SuppressWarnings("deprecation")
    public static void spawn(Player player, ServerLevel level) {
        EnhancedSwampjawEntity fish = MyfEntities.ENHANCED_SWAMPJAW.get().create(level);
        if (fish == null) return;
        RandomSource r = player.getRandom();
        fish.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(6) + 6, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) fish.setTarget(player);
        fish.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        fish.finalizeSpawn(level, level.getCurrentDifficultyAt(fish.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(fish);
        level.playSound(null, fish.getX(), fish.getY(), fish.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKILL, 0);
        entityData.define(SKILL_START, 0);
        entityData.define(PHASE, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (brain == null) return;
        CompoundTag t = new CompoundTag();
        brain.save(t);
        tag.put("myfplus", t);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (brain != null && tag.contains("myfplus")) brain.load(tag.getCompound("myfplus"));
    }

    @Override
    public int myfplus$skill() {
        return entityData.get(SKILL);
    }

    @Override
    public int myfplus$skillAge() {
        return (int) level().getGameTime() - entityData.get(SKILL_START);
    }

    @Override
    public void myfplus$setSkill(int skill) {
        if (entityData.get(SKILL) == skill) return;
        entityData.set(SKILL, skill);
        entityData.set(SKILL_START, (int) level().getGameTime());
    }

    @Override
    public int myfplus$phase() {
        return entityData.get(PHASE);
    }

    @Override
    public void myfplus$setPhase(int phase) {
        entityData.set(PHASE, phase);
    }

    @Override
    @Nullable
    public BossBrain<?> myfplus$brain() {
        return brain;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_swampjaw");
    }
}
