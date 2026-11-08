package com.hp.myfplus.boss.swampjaw;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 化石骨雨：悬停仰头蓄力，把一簇化石骨刺吐上高空；骨刺按顺序砸向玩家脚下、预判位置和周围，每个落点先出现收缩阴影。 */
public class BoneRainSkill extends BossSkill {
    private static final int CHARGE = 16;
    private static final int WARN = 20;
    private static final int FALL = 8;
    private static final int STAY = 24;
    private static final double RADIUS = 1.7;
    private final SwampjawBrain b;
    private final List<Vec3> points = new ArrayList<>();
    private final List<Integer> impacts = new ArrayList<>();
    private int st;
    private int end;

    public BoneRainSkill(SwampjawBrain b) {
        super("bone_rain", 140);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 5 && dist <= 16 && b.grounded(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st = 0;
        points.clear();
        impacts.clear();
        b.setSkill(SwampjawBrain.SKILL_JAW);
        fish.playSound(SoundEvents.ELDER_GUARDIAN_AMBIENT, 2.0f, 0.6f);
        Vec3 f = Combat.dirFromYaw(Combat.yawTo(fish.position(), target.position()));
        FxEntity.at(fish.level(), FxType.SJ_CHARGE, fish.position()).follow(fish, f.x * 1.7, 0.7, f.z * 1.7).life(CHARGE).spawn();
    }

    private void spit(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        RandomSource r = fish.getRandom();
        int count = b.phase2 ? 9 : 6;
        int step = b.phase2 ? 3 : 4;
        double refY = target.getY() + 2;
        Vec3 here = target.position();
        Vec3 v = target.getDeltaMovement();
        Vec3 lead = here.add(v.x * 12, 0, v.z * 12);
        List<Vec3> flat = new ArrayList<>();
        flat.add(here);
        if (Combat.horizontalDist(here, lead) > 1.5) flat.add(lead);
        Vec3 mid = here.add(lead).scale(0.5);
        for (int tries = 0; flat.size() < count && tries < 80; tries++) {
            double a = r.nextDouble() * Math.PI * 2;
            double d = 2.2 + r.nextDouble() * 2.8;
            Vec3 p = new Vec3(mid.x + Math.cos(a) * d, here.y, mid.z + Math.sin(a) * d);
            boolean ok = true;
            for (Vec3 q : flat) {
                if (Combat.horizontalDist(p, q) < 1.8) {
                    ok = false;
                    break;
                }
            }
            if (ok) flat.add(p);
        }
        for (int i = 0; i < flat.size(); i++) {
            Vec3 p = b.groundAt(flat.get(i).x, flat.get(i).z, refY);
            int warn = WARN + i * step;
            points.add(p);
            impacts.add(st + warn);
            FxEntity.at(fish.level(), FxType.SJ_DROP, p).size((float) (RADIUS * 2 / FxType.SJ_DROP.width)).life(warn).spawn();
        }
        end = impacts.get(impacts.size() - 1) + 6;
        Vec3 mouth = SwampjawBrain.mouth(fish);
        FxEntity.at(fish.level(), FxType.SJ_SPOUT, mouth).size(0.7f).spawn();
        Fx.rockBurst(fish.level(), mouth, 0.6f, 5);
        fish.playSound(SoundEvents.SKELETON_HURT, 2.0f, 0.5f);
        fish.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.7f);
        Fx.shake(fish.level(), fish.position(), 0.2f);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        b.flight.brake();
        if (st <= CHARGE) {
            b.flight.face(target != null ? target.position() : null);
            if (st < CHARGE) return false;
            if (target == null) return true;
            spit(target);
            return false;
        }
        if (st == CHARGE + 6) b.setSkill(0);
        for (int i = 0; i < points.size(); i++) {
            int hitAt = impacts.get(i);
            Vec3 p = points.get(i);
            if (st == hitAt - FALL) {
                FxEntity spike = FxEntity.at(fish.level(), FxType.SJ_SPIKE, p).count(i).life(FALL + STAY);
                spike.offY = -0.6;
                spike.spawn();
            }
            if (st == hitAt) land(p);
        }
        return st >= end;
    }

    private void land(Vec3 p) {
        SwampjawEntity fish = b.boss;
        Fx.rockBurst(fish.level(), p, 0.7f, 5);
        fish.level().playSound(null, p.x, p.y, p.z, SoundEvents.BONE_BLOCK_BREAK, fish.getSoundSource(), 1.6f, 0.6f);
        fish.level().playSound(null, p.x, p.y, p.z, SoundEvents.MUD_BREAK, fish.getSoundSource(), 1.4f, 0.8f);
        float dmg = Combat.damage(fish, b.cfg, 0.8);
        for (LivingEntity v : Combat.foes(fish, new AABB(p, p).inflate(RADIUS, 2.5, RADIUS))) {
            if (Combat.horizontalDist(v.position(), p) > RADIUS || Mth.abs((float) (v.getY() - p.y)) > 2.5f) continue;
            Combat.hit(fish, fish.damageSources().mobAttack(fish), dmg, v, 0.35, p);
        }
    }

    @Override
    public void stop() {
        b.flight.face(null);
    }
}
