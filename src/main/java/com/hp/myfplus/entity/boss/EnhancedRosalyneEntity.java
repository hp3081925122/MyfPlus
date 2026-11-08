package com.hp.myfplus.entity.boss;

import javax.annotation.Nullable;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.rosalyne.RosalyneBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.mixin.RosalyneAccessor;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedRosalyneEntity extends RosalyneEntity implements MyfBossHost {
    private static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(EnhancedRosalyneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL_START = SynchedEntityData.defineId(EnhancedRosalyneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(EnhancedRosalyneEntity.class, EntityDataSerializers.INT);
    @Nullable
    private RosalyneBrain brain;

    public EnhancedRosalyneEntity(EntityType<? extends EnhancedRosalyneEntity> type, Level level) {
        super(type, level);
        if (level.isClientSide || !MyfConfig.ROSALYNE.enabled()) return;
        brain = new RosalyneBrain(this);
        brain.install(goalSelector);
    }

    public static void spawn(Player player, ServerLevel level) {
        EnhancedRosalyneEntity rosa = MyfEntities.ENHANCED_ROSALYNE.get().create(level);
        if (rosa == null) return;
        RandomSource r = player.getRandom();
        rosa.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(3) + 3, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        rosa.attackCooldown = 100;
        if (!player.getAbilities().instabuild) rosa.setTarget(player);
        ForgeEventFactory.onFinalizeSpawn(rosa, level, level.getCurrentDifficultyAt(rosa.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(rosa);
        ((RosalyneAccessor) rosa).myfplus$createSpirits();
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
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_rosalyne");
    }
}
