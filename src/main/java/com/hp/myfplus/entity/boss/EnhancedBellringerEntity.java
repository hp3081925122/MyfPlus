package com.hp.myfplus.entity.boss;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.BellringerSkills;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.EnhancedBoss;
import com.hp.myfplus.core.Pacer;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedBellringerEntity extends BellringerEntity implements EnhancedBoss {
    private final SkillRunner runner;
    private final Pacer pacer = new Pacer();
    private boolean phase2;
    private boolean rageForced;

    public EnhancedBellringerEntity(EntityType<? extends EnhancedBellringerEntity> type, Level level) {
        super(type, level);
        goalSelector.removeAllGoals(g -> g instanceof MoveFrontOfTarget);
        goalSelector.addGoal(7, new MoveFrontOfTarget(this, 1.5));
        runner = new SkillRunner(this, () -> attackCooldown = 40 + random.nextInt(21));
        BellringerSkills.register(this, runner);
        goalSelector.addGoal(1, runner);
    }

    public static void spawn(Player player, ServerLevel level) {
        EnhancedBellringerEntity boss = MyfEntities.ENHANCED_BELLRINGER.get().create(level);
        if (boss == null) return;
        RandomSource r = player.getRandom();
        boss.moveTo(player.getX() + r.nextInt(15) - 7, player.getY() + r.nextInt(9) - 1, player.getZ() + r.nextInt(15) - 7, r.nextFloat() * 360 - 180, 0);
        boss.attackCooldown = 80;
        if (!player.getAbilities().instabuild) boss.setTarget(player);
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        ForgeEventFactory.onFinalizeSpawn(boss, level, level.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(boss);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2, 0.7f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 2, 0.8f);
    }

    public void ding() {
        swing(InteractionHand.MAIN_HAND);
        playSound(SoundEvents.BELL_BLOCK, 2, 1);
    }

    public int myfplus$extraHits() {
        return phase2 ? 2 : 1;
    }

    public int myfplus$interval() {
        return phase2 ? 12 : 15;
    }

    public boolean myfplus$rageForced() {
        return rageForced;
    }

    public void myfplus$rageStarted() {
        rageForced = false;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        attackCooldown = pacer.step(this, runner, attackCooldown, Pacer.extra(phase2 ? 0.4 : 0.65, myfplus$cfg().cooldown()), !rageForced, myfplus$cfg().chance(), phase2);
    }

    @Override
    public MyfConfig.BossCfg myfplus$cfg() {
        return MyfConfig.BELLRINGER;
    }

    @Override
    public boolean myfplus$phase2() {
        return phase2;
    }

    @Override
    public void myfplus$enterPhase2() {
        phase2 = true;
        rageForced = true;
        attackCooldown = Math.max(attackCooldown, 30);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4));
        playSound(SoundEvents.BELL_RESONATE, 4, 0.6f);
        ding();
        Shots.particles(level(), ParticleTypes.SOUL, position().add(0, 1, 0), 60, 2.0, 0.08);
        Shots.particles(level(), ParticleTypes.END_ROD, position().add(0, 1, 0), 40, 2.5, 0.15);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("myfplus_phase2", phase2);
        tag.putBoolean("myfplus_rage", rageForced);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase2 = tag.getBoolean("myfplus_phase2");
        rageForced = tag.getBoolean("myfplus_rage");
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_bellringer");
    }
}
