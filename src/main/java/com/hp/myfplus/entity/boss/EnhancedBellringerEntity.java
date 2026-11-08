package com.hp.myfplus.entity.boss;

import javax.annotation.Nullable;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.bellringer.BellringerBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.BellringerEntity;
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
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedBellringerEntity extends BellringerEntity implements MyfBossHost {
    private static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(EnhancedBellringerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL_START = SynchedEntityData.defineId(EnhancedBellringerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(EnhancedBellringerEntity.class, EntityDataSerializers.INT);
    @Nullable
    private BellringerBrain brain;

    public EnhancedBellringerEntity(EntityType<? extends EnhancedBellringerEntity> type, Level level) {
        super(type, level);
        if (level.isClientSide || !MyfConfig.BELLRINGER.enabled()) return;
        brain = new BellringerBrain(this);
        brain.install(goalSelector);
    }

    public static void spawn(Player player, ServerLevel level) {
        EnhancedBellringerEntity ringer = MyfEntities.ENHANCED_BELLRINGER.get().create(level);
        if (ringer == null) return;
        RandomSource r = player.getRandom();
        ringer.moveTo(player.getX() + r.nextInt(15) - 7, player.getY() + r.nextInt(9) - 1, player.getZ() + r.nextInt(15) - 7, r.nextFloat() * 360 - 180, 0);
        ringer.attackCooldown = 100;
        if (!player.getAbilities().instabuild) ringer.setTarget(player);
        ringer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        ForgeEventFactory.onFinalizeSpawn(ringer, level, level.getCurrentDifficultyAt(ringer.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(ringer);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 1);
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
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_bellringer");
    }
}
