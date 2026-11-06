package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.BellDomeEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class GrandTollSkill extends BeatSkill {
    private static final float RADIUS = 7f;
    private Vec3 center = Vec3.ZERO;
    @Nullable
    private BellDomeEntity dome;
    private int landBeat = -1;
    private int shatterAt = -1;

    public GrandTollSkill(BellringerBrain b) {
        super(b, "grand_toll", 600);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return b.phase2 && dist <= 24;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    protected void begin(LivingEntity target) {
        dome = null;
        landBeat = -1;
        shatterAt = -1;
        center = b.groundAt(target.position());
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        BellringerEntity boss = b.boss;
        if (beat == 1) {
            if (target != null) center = b.groundAt(target.position());
            b.ding();
            boss.playSound(SoundEvents.BELL_RESONATE, 3.0f, 0.4f);
            FxEntity.at(boss.level(), FxType.WARN_CIRCLE, center).size(RADIUS).life(b.interval() * 2 + BellDomeEntity.DESCEND).palette(1).spawn();
        }
        else if (beat == 3) {
            BellDomeEntity d = new BellDomeEntity(MyfEntities.BELL_DOME.get(), boss.level());
            d.setPos(center.x, center.y, center.z);
            d.setOwner(boss);
            d.radius = RADIUS;
            d.life = BellDomeEntity.DESCEND + b.interval() * 6 + 40;
            boss.level().addFreshEntity(d);
            dome = d;
            landBeat = beat + 2;
        }
        else if (landBeat > 0 && beat >= landBeat && beat < landBeat + 3) {
            int n = beat - landBeat;
            boolean tall = n == 2;
            b.ding();
            float g0 = boss.getRandom().nextFloat() * 360;
            float[] gaps = tall ? new float[]{g0, g0 + 180} : new float[0];
            b.wave(center, RADIUS - 0.2f, 0.3f, -0.35f, tall, gaps, tall ? 0.9 : 0.7);
            for (float g : gaps) {
                Vec3 d = Combat.dirFromYaw(g);
                for (float r : new float[]{1.5f, 3f, 4.5f, 6f}) {
                    FxEntity.at(boss.level(), FxType.BR_GAP_MARK, b.groundAt(center.add(d.scale(r)))).life(24).spawn();
                }
            }
            Fx.shake(boss.level(), center, 0.35f);
            if (n == 2) shatterAt = 26;
        }
        return false;
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        BellringerEntity boss = b.boss;
        b.hover(center.add(0, 8, 0), 0.6);
        if (dome != null && dome.tickCount == BellDomeEntity.DESCEND) {
            boss.playSound(SoundEvents.ANVIL_LAND, 3.0f, 0.4f);
            boss.playSound(SoundEvents.BELL_BLOCK, 3.0f, 0.4f);
            Fx.rockRing(boss.level(), center, RADIUS + 1.5f, 0.8f);
            Fx.shake(boss.level(), center, 1.1f);
        }
        if (shatterAt > 0 && --shatterAt == 0) {
            shatter();
            return true;
        }
        return false;
    }

    private void shatter() {
        BellringerEntity boss = b.boss;
        if (dome != null) dome.discard();
        boss.playSound(SoundEvents.GLASS_BREAK, 3.0f, 0.4f);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            Vec3 p = center.add(Math.cos(a) * RADIUS, 2.0, Math.sin(a) * RADIUS);
            FxEntity.at(boss.level(), FxType.BR_ECHO_BREAK, p).size(2.0f).spawn();
        }
        Fx.rockBurst(boss.level(), center, RADIUS, 16);
        Fx.shake(boss.level(), center, 0.8f);
    }

    @Override
    public void stop() {
        if (dome != null && !dome.isRemoved()) dome.discard();
        dome = null;
    }
}
