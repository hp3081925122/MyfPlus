package com.hp.myfplus.boss.bellringer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ResonanceSkill extends BeatSkill {
    private static final int BEATS = 15;
    private static final int EXHAUST = 60;
    private Vec3 center = Vec3.ZERO;
    private final List<Vec3> bells = new ArrayList<>();
    private final List<FxEntity> bellFx = new ArrayList<>();
    private int endAt = -1;

    public ResonanceSkill(BellringerBrain b) {
        super(b, "resonance", -1);
    }

    @Override
    protected void begin(LivingEntity target) {
        BellringerEntity boss = b.boss;
        center = b.groundAt(new Vec3(boss.getX(), target.getY(), boss.getZ()));
        b.invulnerable = true;
        b.phase2 = true;
        b.setPhase(2);
        endAt = -1;
        bells.clear();
        bellFx.clear();
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2;
            Vec3 p = b.groundAt(center.add(Math.cos(a) * 8, 1, Math.sin(a) * 8));
            bells.add(p);
            bellFx.add(FxEntity.at(boss.level(), FxType.BR_ECHO_BELL, p.add(0, 1.0, 0)).life(BEATS * b.interval() + 60).spawn());
            Fx.rockBurst(boss.level(), p, 1.0f, 6);
        }
        Fx.banner(boss.level(), center, Fx.BOSS_BELLRINGER, 2);
        Fx.rockRing(boss.level(), center, 9f, 0.5f);
        Fx.shake(boss.level(), center, 0.8f);
        boss.playSound(SoundEvents.BELL_RESONATE, 3.0f, 0.5f);
        b.forceBeat();
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        if (beat > BEATS) return false;
        Vec3 src = beat % 5 == 0 ? center : bells.get(beat % 4);
        if (beat % 5 == 0) b.ding();
        else b.boss.level().playSound(null, src.x, src.y, src.z, SoundEvents.BELL_BLOCK, b.boss.getSoundSource(), 1.5f, 1.2f + 0.1f * (beat % 4));
        b.wave(src, 0.8f, 11f, 0.35f, false, new float[0], 0.5);
        FxEntity.at(b.boss.level(), FxType.BR_IMPACT, src).size(2.5f).spawn();
        if (target != null && beat == 7) b.countdown(target, 1);
        if (target != null && beat == 8) b.dropBell(b.groundAt(target.position()), false);
        if (beat == BEATS) finish();
        return false;
    }

    private void finish() {
        BellringerEntity boss = b.boss;
        for (int i = 0; i < bells.size(); i++) {
            FxEntity.at(boss.level(), FxType.BR_ECHO_BREAK, bells.get(i).add(0, 1.0, 0)).size(1.4f).spawn();
            Fx.rockBurst(boss.level(), bells.get(i), 1.0f, 6);
            bellFx.get(i).discard();
        }
        boss.playSound(SoundEvents.GLASS_BREAK, 2.5f, 0.5f);
        b.invulnerable = false;
        b.exhausted = EXHAUST;
        b.beatPaused = true;
        endAt = 0;
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        BellringerEntity boss = b.boss;
        if (endAt < 0) {
            b.hover(center.add(0, 6, 0), 0.5);
            return false;
        }
        endAt++;
        b.hover(center.add(0, 0.2, 0), 0.8);
        if (endAt == 12) {
            Fx.shake(boss.level(), boss.position(), 0.5f);
            Fx.rockBurst(boss.level(), b.groundAt(boss.position()), 1.5f, 10);
            boss.playSound(SoundEvents.ANVIL_LAND, 1.5f, 0.5f);
        }
        return endAt >= EXHAUST;
    }

    @Override
    public void stop() {
        for (FxEntity f : bellFx) f.discard();
        b.invulnerable = false;
        b.beatPaused = false;
        b.phase2 = true;
    }
}
