package com.hp.myfplus.entity.boss;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.RosalyneSkills;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.EnhancedBoss;
import com.hp.myfplus.core.Pacer;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.mixin.RosalyneAccessor;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.RosalyneEntity;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class EnhancedRosalyneEntity extends RosalyneEntity implements EnhancedBoss {
    private final SkillRunner runner;
    private final Pacer pacer = new Pacer();
    private boolean phase2;
    private boolean crashNext;
    public int corollaCooldown;

    public EnhancedRosalyneEntity(EntityType<? extends EnhancedRosalyneEntity> type, Level level) {
        super(type, level);
        runner = new SkillRunner(this, this::onSkillEnd);
        RosalyneSkills.register(this, runner);
        goalSelector.addGoal(2, runner);
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
        rosa.acc().myfplus$createSpirits();
    }

    private RosalyneAccessor acc() {
        return (RosalyneAccessor) (Object) this;
    }

    public float dmg(double mult) {
        return (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) * mult * myfplus$cfg().damage());
    }

    public void crashNext() {
        crashNext = true;
    }

    public boolean attackPhase() {
        int p = getPhase();
        return p == PHASE_1 || p == PHASE_2 || p == PHASE_3;
    }

    public void myfplus$afterCrash() {
        if (phase2 && !runner.busy()) runner.queue(RosalyneSkills.FAN);
    }

    private void onSkillEnd() {
        setInvisible(false);
        setAnimation(ANIM_NEUTRAL);
        if (crashNext) {
            crashNext = false;
            acc().myfplus$setNextAttack(2);
            attackCooldown = 0;
            pacer.skip();
        }
        else attackCooldown = 60 + random.nextInt(21);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (corollaCooldown > 0) corollaCooldown--;
        attackCooldown = pacer.step(this, runner, attackCooldown, Pacer.extra(phase2 ? 0.5 : 0.7, myfplus$cfg().cooldown()), attackPhase(), myfplus$cfg().chance(), phase2);
    }

    @Override
    public MyfConfig.BossCfg myfplus$cfg() {
        return MyfConfig.ROSALYNE;
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
        setAnimation(ANIM_NEUTRAL);
        attackCooldown = 40;
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4));
        playSound(MYFSounds.rosalyneCrack.get(), 2.0f, 0.8f);
        playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.9f);
        Shots.particles(level(), ParticleTypes.EXPLOSION_EMITTER, position().add(0, 1, 0), 1, 0, 0);
        Shots.particles(level(), ParticleTypes.CHERRY_LEAVES, position().add(0, 1, 0), 80, 3.0, 0.1);
        Shots.particles(level(), ParticleTypes.FLAME, position().add(0, 1, 0), 40, 2.0, 0.1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("myfplus_phase2", phase2);
        tag.putInt("myfplus_corolla", corollaCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase2 = tag.getBoolean("myfplus_phase2");
        corollaCooldown = tag.getInt("myfplus_corolla");
        setInvisible(false);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_rosalyne");
    }
}
