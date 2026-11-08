package com.hp.myfplus.entity.boss;

import javax.annotation.Nullable;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.fortuna.FortunaBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.mixin.DameFortunaAccessor;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedFortunaEntity extends DameFortunaEntity implements MyfBossHost {
    private static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(EnhancedFortunaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL_START = SynchedEntityData.defineId(EnhancedFortunaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(EnhancedFortunaEntity.class, EntityDataSerializers.INT);
    private static final int ATK_CHIPS_CIRCLE = 2;
    @Nullable
    private FortunaBrain brain;

    public EnhancedFortunaEntity(EntityType<? extends EnhancedFortunaEntity> type, Level level) {
        super(type, level);
        if (level.isClientSide || !MyfConfig.FORTUNA.enabled()) return;
        brain = new FortunaBrain(this);
        brain.install(goalSelector);
    }

    public static void spawn(Player player, ServerLevel level) {
        EnhancedFortunaEntity dame = MyfEntities.ENHANCED_FORTUNA.get().create(level);
        if (dame == null) return;
        RandomSource r = player.getRandom();
        dame.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(3) + 3, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        ((DameFortunaAccessor) dame).myfplus$setAttackCooldown(100);
        ((DameFortunaAccessor) dame).myfplus$setNextAttack(ATK_CHIPS_CIRCLE);
        if (!player.getAbilities().instabuild) dame.setTarget(player);
        dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        ForgeEventFactory.onFinalizeSpawn(dame, level, level.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(dame);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        FortunaBrain b = brain;
        if (b == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (!b.betting) return super.hurt(source, b.director.isRunning(b.slot) ? amount * 0.5f : amount);
        if (source.getEntity() instanceof LivingEntity attacker && attacker != this && amount > 0) {
            b.bet += amount;
            playSound(SoundEvents.CHAIN_PLACE, 1.0f, 1.2f + Math.min(0.8f, b.bet / Math.max(1f, getMaxHealth() * 0.15f)));
        }
        return false;
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
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_dame_fortuna");
    }
}
