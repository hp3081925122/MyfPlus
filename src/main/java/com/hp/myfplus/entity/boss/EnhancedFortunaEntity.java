package com.hp.myfplus.entity.boss;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.FortunaSkills;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.EnhancedBoss;
import com.hp.myfplus.core.Pacer;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.mixin.DameFortunaAccessor;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedFortunaEntity extends DameFortunaEntity implements EnhancedBoss {
    private final SkillRunner runner;
    private final Pacer pacer = new Pacer();
    private boolean phase2;

    public EnhancedFortunaEntity(EntityType<? extends EnhancedFortunaEntity> type, Level level) {
        super(type, level);
        runner = new SkillRunner(this, () -> {
            setAnimation(ANIM_IDLE);
            acc().myfplus$setAttackCooldown(50 + random.nextInt(21));
        });
        FortunaSkills.register(this, runner);
        goalSelector.addGoal(3, runner);
    }

    public static void spawn(Player player, ServerLevel level) {
        EnhancedFortunaEntity dame = MyfEntities.ENHANCED_FORTUNA.get().create(level);
        if (dame == null) return;
        RandomSource r = player.getRandom();
        dame.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(3) + 3, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        dame.acc().myfplus$setAttackCooldown(80);
        dame.acc().myfplus$setNextAttack(2);
        if (!player.getAbilities().instabuild) dame.setTarget(player);
        dame.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        ForgeEventFactory.onFinalizeSpawn(dame, level, level.getCurrentDifficultyAt(dame.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(dame);
    }

    private DameFortunaAccessor acc() {
        return (DameFortunaAccessor) (Object) this;
    }

    public int myfplus$extraDice() {
        return phase2 ? 2 : 1;
    }

    public double myfplus$spinSpeed() {
        return phase2 ? 1.6 : 1.4;
    }

    public double myfplus$spinInterval() {
        return phase2 ? 0.6 : 0.7;
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        int cd = acc().myfplus$getAttackCooldown();
        int next = pacer.step(this, runner, cd, Pacer.extra(phase2 ? 0.42 : 0.6, myfplus$cfg().cooldown()), isAttackPhase(), myfplus$cfg().chance(), phase2);
        if (next != cd) acc().myfplus$setAttackCooldown(next);
    }

    @Override
    public MyfConfig.BossCfg myfplus$cfg() {
        return MyfConfig.FORTUNA;
    }

    @Override
    public boolean myfplus$phase2() {
        return phase2;
    }

    @Override
    public void myfplus$enterPhase2() {
        phase2 = true;
        setPhase(PHASE_3);
        acc().myfplus$setPhaseField(PHASE_3);
        setAnimation(ANIM_IDLE);
        acc().myfplus$setAttackCooldown(40);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4));
        playSound(MYFSounds.dameFortunaClap.get(), 3.0f, 0.8f);
        playSound(SoundEvents.TOTEM_USE, 1.5f, 1.2f);
        Shots.particles(level(), ParticleTypes.ENCHANTED_HIT, position().add(0, 1.2, 0), 60, 1.5, 0.3);
        Shots.particles(level(), ParticleTypes.HAPPY_VILLAGER, position().add(0, 1.2, 0), 30, 1.5, 0.1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("myfplus_phase2", phase2);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase2 = tag.getBoolean("myfplus_phase2");
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_dame_fortuna");
    }
}
