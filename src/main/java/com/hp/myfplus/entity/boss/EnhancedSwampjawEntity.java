package com.hp.myfplus.entity.boss;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.SwampjawSkills;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.EnhancedBoss;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.core.SkillRunner;
import com.hp.myfplus.mixin.SwampjawAccessor;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnhancedSwampjawEntity extends SwampjawEntity implements EnhancedBoss {
    public static final int CIRCLE = 0, BOMB = 1, SWOOP = 2, STUNNED = 3, SKILL = 5;
    private final SkillRunner runner;
    private boolean phase2;
    private int lastBehavior;
    private double accel;
    private int tailCooldown;
    private int chain;
    private Vec3 steerVel = Vec3.ZERO;

    public EnhancedSwampjawEntity(EntityType<? extends EnhancedSwampjawEntity> type, Level level) {
        super(type, level);
        runner = new SkillRunner(this, this::onSkillEnd);
        SwampjawSkills.register(this, runner);
        goalSelector.addGoal(0, runner);
    }

    @SuppressWarnings("deprecation")
    public static void spawn(Player player, ServerLevel level) {
        EnhancedSwampjawEntity fish = MyfEntities.ENHANCED_SWAMPJAW.get().create(level);
        if (fish == null) return;
        RandomSource r = player.getRandom();
        fish.moveTo(player.getX() + r.nextInt(5) - 2, player.getY() + r.nextInt(10) + 5, player.getZ() + r.nextInt(5) - 2, r.nextFloat() * 360 - 180, 0);
        if (!player.getAbilities().instabuild) fish.setTarget(player);
        fish.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        fish.finalizeSpawn(level, level.getCurrentDifficultyAt(fish.blockPosition()), MobSpawnType.EVENT, null, null);
        level.addFreshEntity(fish);
        level.playSound(null, fish.getX(), fish.getY(), fish.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.5f, 0.6f);
    }

    private SwampjawAccessor acc() {
        return (SwampjawAccessor) (Object) this;
    }

    public int behavior() {
        return acc().myfplus$getBehavior();
    }

    public void behavior(int value) {
        acc().myfplus$setBehavior(value);
    }

    public int attackDelay() {
        return acc().myfplus$getAttackDelay();
    }

    public void attackDelay(int value) {
        acc().myfplus$setAttackDelay(value);
    }

    public boolean myfplus$steering() {
        return behavior() == SKILL;
    }

    public void myfplus$steer() {
        Vec3 v = getDeltaMovement();
        setDeltaMovement(v.add(steerVel.subtract(v).scale(0.25)));
        LivingEntity t = getTarget();
        Vec3 face = steerVel.lengthSqr() > 0.01 ? steerVel : t != null ? t.position().subtract(position()) : Vec3.ZERO;
        if (face.lengthSqr() > 1.0E-4) {
            setYRot(Mth.approachDegrees(getYRot(), (float) (Mth.atan2(face.z, face.x) * Mth.RAD_TO_DEG) - 90f, 12f));
            yBodyRot = getYRot();
            setXRot((float) -(Mth.atan2(face.y, face.horizontalDistance()) * Mth.RAD_TO_DEG) * 0.5f);
        }
    }

    public void steerTo(Vec3 point, double maxSpeed) {
        Vec3 d = point.subtract(position());
        double len = d.length();
        steerVel = len > maxSpeed ? d.scale(maxSpeed / len) : d.scale(0.5);
    }

    public void brake() {
        steerVel = Vec3.ZERO;
    }

    public Vec3 mouth() {
        return position().add(Vec3.directionFromRotation(0, getYRot()).scale(1.6)).add(0, 0.6, 0);
    }

    public float dmg(double mult) {
        return (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) * mult * myfplus$cfg().damage());
    }

    public float myfplus$swoopSpeed() {
        return phase2 ? 1.5f : 1.25f;
    }

    public int myfplus$bombs() {
        return phase2 ? 1 : 2;
    }

    private void onSkillEnd() {
        brake();
        if (behavior() == SKILL) {
            behavior(CIRCLE);
            setAnimation(ANIM_NEUTRAL);
            attackDelay(Math.max(attackDelay(), 30));
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int b = behavior();
        if (b == STUNNED && lastBehavior != STUNNED) {
            attackDelay(Math.min(attackDelay(), phase2 ? 20 : 35));
            chain = 0;
        }
        if (b == BOMB) chain = 0;
        if (tailCooldown > 0) tailCooldown--;
        LivingEntity target = getTarget();
        if (!runner.busy() && target != null && target.isAlive()) {
            if (b == CIRCLE || b == BOMB) {
                accel += (phase2 ? 1.0 : 0.6) / myfplus$cfg().cooldown();
                while (accel >= 1) {
                    attackDelay(attackDelay() - 1);
                    accel -= 1;
                }
            }
            if (lastBehavior == SWOOP && b == CIRCLE && chain < 2 && random.nextFloat() < (phase2 ? 0.8f : 0.5f)) {
                chain++;
                runner.queue(SwampjawSkills.REBITE);
            }
            else if (lastBehavior == CIRCLE && b == BOMB && random.nextFloat() < myfplus$cfg().chance()) {
                runner.pick(target, phase2);
            }
            else if ((b == CIRCLE || b == BOMB) && tailCooldown <= 0 && distanceToSqr(target) < 16) {
                tailCooldown = phase2 ? 60 : 100;
                runner.queue(SwampjawSkills.TAIL);
            }
        }
        lastBehavior = b;
    }

    @Override
    public MyfConfig.BossCfg myfplus$cfg() {
        return MyfConfig.SWAMPJAW;
    }

    @Override
    public boolean myfplus$phase2() {
        return phase2;
    }

    @Override
    public void myfplus$enterPhase2() {
        phase2 = true;
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4));
        playSound(MYFSounds.swampjawCharge.get(), 4.0f, 0.6f);
        playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 3.0f, 0.5f);
        Shots.particles(level(), ParticleTypes.SPLASH, position().add(0, 1, 0), 120, 2.5, 0.4);
        Shots.particles(level(), ParticleTypes.BUBBLE_POP, position().add(0, 1, 0), 60, 2.0, 0.1);
        Shots.particles(level(), ParticleTypes.SOUL, position().add(0, 1, 0), 30, 1.5, 0.05);
        attackDelay(Math.min(attackDelay(), 20));
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
        if (behavior() == SKILL) behavior(CIRCLE);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation(Myfplus.MOD_ID, "entities/enhanced_swampjaw");
    }
}
