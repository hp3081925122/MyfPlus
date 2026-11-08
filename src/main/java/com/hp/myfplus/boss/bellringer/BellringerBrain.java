package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.BossBrain;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.entity.BellWaveEntity;
import com.hp.myfplus.entity.PhantomBellEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.BellringerEntity;
import lykrast.meetyourfight.entity.ai.MoveFrontOfTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BellringerBrain extends BossBrain<BellringerEntity> {
    private static final float[] PITCH = {1.0f, 1.19f, 1.33f, 1.5f};

    public final TollSkill toll;
    public final EchoSkill echo;
    public final KnellSkill knell;
    public final ScaleSkill scale;
    public final ResonanceSkill resonance;
    public final GrandTollSkill grandToll;

    public boolean beatThisTick;
    public boolean beatPaused;
    public int beatCount;
    private int beatTimer = 20;
    public boolean phase2;
    private boolean transitionQueued;
    private boolean introDone;
    public boolean invulnerable;
    public int exhausted;

    public BellringerBrain(BellringerEntity boss) {
        super(boss, MyfConfig.BELLRINGER);
        toll = director.add(new TollSkill(this));
        echo = director.add(new EchoSkill(this));
        knell = director.add(new KnellSkill(this));
        scale = director.add(new ScaleSkill(this));
        resonance = director.add(new ResonanceSkill(this));
        grandToll = director.add(new GrandTollSkill(this));
    }

    public void install(GoalSelector goals) {
        goals.removeAllGoals(g -> true);
        goals.addGoal(0, new FloatGoal(boss));
        goals.addGoal(1, director);
        goals.addGoal(7, new MoveFrontOfTarget(boss, 1));
        goals.addGoal(8, new VexMoveRandomGoal(boss, 0.25));
        goals.addGoal(9, new LookAtPlayerGoal(boss, Player.class, 3.0F, 1.0F));
    }

    public int interval() {
        return phase2 ? MyfConfig.BEAT_P2.get() : MyfConfig.BEAT_P1.get();
    }

    @Override
    public int globalGap() {
        return interval();
    }

    @Override
    public void serverTick() {
        beatThisTick = false;
        if (exhausted > 0) exhausted--;
        int phase = phase2 ? 2 : 1;
        if (((MyfBossHost) boss).myfplus$phase() != phase) setPhase(phase);
        LivingEntity target = target();
        if (!introDone && target != null) {
            introDone = true;
            Fx.banner(boss.level(), boss.position(), Fx.BOSS_BELLRINGER, 1);
        }
        if (!beatPaused && target != null && --beatTimer <= 0) {
            beatTimer = interval();
            beatThisTick = true;
            beatCount++;
            FxEntity.at(boss.level(), FxType.BR_NOTE, boss.position()).follow(boss, 0, boss.getBbHeight() + 0.9, 0).spawn();
            boss.playSound(SoundEvents.NOTE_BLOCK_BELL.get(), 1.0f, PITCH[beatCount & 3]);
        }
    }

    @Override
    public boolean firstPhase() {
        return !phase2 && !transitionQueued;
    }

    @Override
    protected void enterSecondPhase() {
        transitionQueued = true;
        director.force(resonance);
    }

    public void forceBeat() {
        beatTimer = 1;
    }

    @Override
    public boolean blocksAttack(DamageSource source) {
        return invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public float onHurt(DamageSource source, float amount) {
        return exhausted > 0 ? amount * 1.4f : amount;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("phase2", phase2 || transitionQueued);
        tag.putBoolean("intro", introDone);
    }

    @Override
    public void load(CompoundTag tag) {
        phase2 = tag.getBoolean("phase2");
        introDone = tag.getBoolean("intro");
        transitionQueued = phase2;
    }

    public void ding() {
        boss.swing(InteractionHand.MAIN_HAND);
        boss.playSound(SoundEvents.BELL_BLOCK, 2.0f, 1.0f);
    }

    public double ground(double x, double z, double refY) {
        return GroundProbe.under(boss.level(), x, z, refY);
    }

    public Vec3 groundAt(Vec3 p) {
        return new Vec3(p.x, ground(p.x, p.z, p.y + 0.5), p.z);
    }

    public void hover(Vec3 p, double speed) {
        boss.getMoveControl().setWantedPosition(p.x, p.y, p.z, speed);
    }

    public BellWaveEntity wave(Vec3 center, float start, float end, float speed, boolean tall, float[] gaps, double mult) {
        BellWaveEntity w = new BellWaveEntity(MyfEntities.BELL_WAVE.get(), boss.level());
        w.setPos(center.x, center.y, center.z);
        w.setOwner(boss);
        w.setup(start, end, speed, tall, gaps);
        w.damage = Combat.damage(boss, cfg, mult);
        boss.level().addFreshEntity(w);
        return w;
    }

    public PhantomBellEntity dropBell(Vec3 at, boolean echoWave) {
        PhantomBellEntity bell = new PhantomBellEntity(MyfEntities.PHANTOM_BELL.get(), boss.level());
        bell.setPos(at.x, at.y, at.z);
        bell.setOwner(boss);
        bell.damage = Combat.damage(boss, cfg, 1.3);
        bell.echoDelay = interval() * 2;
        if (echoWave) bell.onEcho = pb -> wave(pb.position(), 1.5f, 6f, 0.35f, false, new float[0], 0.5);
        boss.level().addFreshEntity(bell);
        boss.playSound(SoundEvents.BELL_RESONATE, 2.0f, 0.6f);
        return bell;
    }

    @Nullable
    public FxEntity countdown(@Nullable LivingEntity target, int n) {
        if (target == null) return null;
        FxType t = n >= 3 ? FxType.BR_COUNT3 : n == 2 ? FxType.BR_COUNT2 : FxType.BR_COUNT1;
        target.playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 1.0f, 0.5f + 0.25f * (3 - n));
        if (target instanceof ServerPlayer sp) {
            String num = n >= 3 ? "III" : n == 2 ? "II" : "I";
            sp.displayClientMessage(Component.translatable("message.myfplus.knell", Component.literal(num).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).withStyle(ChatFormatting.AQUA), true);
            sp.playNotifySound(SoundEvents.BELL_BLOCK, boss.getSoundSource(), 0.8f, 1.4f + 0.2f * (3 - n));
        }
        FxEntity.at(boss.level(), FxType.WARN_CIRCLE, groundAt(target.position())).size(PhantomBellEntity.RADIUS).life(interval()).palette(1).spawn();
        return FxEntity.at(boss.level(), t, target.position()).follow(target, 0, target.getBbHeight() + 0.8, 0).life(interval()).spawn();
    }
}
