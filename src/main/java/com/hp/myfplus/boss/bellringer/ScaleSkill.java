package com.hp.myfplus.boss.bellringer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.BellringerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ScaleSkill extends BeatSkill {
    private static final int LINES = 4;
    private static final float SPACING = 2.8f;
    private static final float LENGTH = 16f;
    private static final float WIDTH = 1.6f;
    private static final float STEP = 2.3f;
    private static final float[] PITCH = {0.7f, 0.8f, 0.9f, 1.05f};
    private final List<Line> lines = new ArrayList<>();
    private Vec3 anchor = Vec3.ZERO;

    private record Line(Vec3 start, float yaw, int beat) {
    }

    public ScaleSkill(BellringerBrain b) {
        super(b, "scale", 300);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 4 && dist <= 24;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return b.phase2 ? 3 : 2;
    }

    @Override
    protected void begin(LivingEntity target) {
        BellringerEntity boss = b.boss;
        anchor = boss.position();
        lines.clear();
        Vec3 c = b.groundAt(target.position());
        float yaw = Combat.yawTo(boss.position(), target.position());
        build(c, yaw, false);
        if (b.phase2) build(c, yaw + 90, true);
    }

    private void build(Vec3 c, float yaw, boolean reverse) {
        Vec3 fwd = Combat.dirFromYaw(yaw);
        float lineYaw = yaw + 90;
        Vec3 along = Combat.dirFromYaw(lineYaw);
        for (int i = 0; i < LINES; i++) {
            double off = (i - (LINES - 1) / 2.0) * SPACING;
            Vec3 mid = c.add(fwd.scale(off));
            Vec3 start = b.groundAt(mid.subtract(along.scale(LENGTH / 2)).add(0, 1, 0));
            lines.add(new Line(new Vec3(start.x, c.y, start.z), lineYaw, 2 + (reverse ? LINES - 1 - i : i)));
        }
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        BellringerEntity boss = b.boss;
        if (beat == 1) {
            b.ding();
            boss.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0f, 0.6f);
            for (Line l : lines) {
                int life = b.interval() * (l.beat - 1);
                FxEntity.at(boss.level(), FxType.WARN_LINE, l.start).yaw(l.yaw).line(LENGTH, WIDTH).life(life).palette(1).spawn();
            }
            return false;
        }
        boolean any = false;
        for (Line l : lines) {
            if (l.beat == beat) {
                erupt(l);
                any = true;
            }
            else if (l.beat > beat) any = true;
        }
        if (!any) return true;
        b.ding();
        boss.playSound(SoundEvents.BELL_BLOCK, 2.0f, PITCH[Math.min(PITCH.length - 1, beat - 2)]);
        return false;
    }

    private void erupt(Line l) {
        BellringerEntity boss = b.boss;
        Vec3 d = Combat.dirFromYaw(l.yaw);
        for (float f = STEP / 2; f < LENGTH; f += STEP) {
            Vec3 p = b.groundAt(l.start.add(d.scale(f)).add(0, 1.5, 0));
            FxEntity.at(boss.level(), FxType.BR_PILLAR, p).size(0.8f).spawn();
        }
        Fx.rockLine(boss.level(), l.start, l.yaw, LENGTH, WIDTH, 4f);
        Vec3 mid = l.start.add(d.scale(LENGTH / 2));
        Fx.shake(boss.level(), mid, 0.15f);
        double hw = WIDTH / 2;
        for (LivingEntity v : Combat.foes(boss, new AABB(mid, mid).inflate(LENGTH / 2 + 1, 4, LENGTH / 2 + 1))) {
            double x = v.getX() - l.start.x, z = v.getZ() - l.start.z;
            double f = x * d.x + z * d.z;
            double s = Math.abs(x * d.z - z * d.x);
            if (f < -0.3 || f > LENGTH + 0.3 || s > hw + v.getBbWidth() / 2) continue;
            if (!Combat.inGroundBand(boss.level(), v, l.start.y, 3.0)) continue;
            Combat.hit(boss, boss.damageSources().mobAttack(boss), Combat.damage(boss, b.cfg, 0.7), v, 0.3, mid);
        }
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        b.hover(anchor, 0.5);
        return false;
    }
}
