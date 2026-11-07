package com.hp.myfplus.boss.swampjaw;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** 沼流吐息：悬停蓄力把水吸到嘴边，然后喷出高压沼水光束横扫 60° 扇形。起扫的一侧先用直线预警标出。 */
public class BreathSkill extends BossSkill {
    private static final int CHARGE = 24;
    private static final int SWEEP_T = 30;
    private static final float SWEEP = 60f;
    private final SwampjawBrain b;
    private final Set<Integer> hit = new HashSet<>();
    private float baseYaw;
    private float sign;
    private float reach;
    private int stage;
    private int st;

    public BreathSkill(SwampjawBrain b) {
        super("breath", 140);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 5 && dist <= 16;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        stage = 0;
        st = 0;
        hit.clear();
        sign = fish.getRandom().nextBoolean() ? 1f : -1f;
        baseYaw = Combat.yawTo(fish.position(), target.position());
        reach = (float) Mth.clamp(Combat.horizontalDist(fish.position(), target.position()) + 3, 8, 15);
        b.setSkill(SwampjawBrain.SKILL_JAW);
        fish.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0f, 1.3f);
        Vec3 f = Combat.dirFromYaw(baseYaw);
        FxEntity.at(fish.level(), FxType.SJ_CHARGE, fish.position()).follow(fish, f.x * 1.7, 0.7, f.z * 1.7).life(CHARGE).spawn();
        double gy = b.ground(fish.getX(), fish.getZ(), fish.getY());
        FxEntity.at(fish.level(), FxType.WARN_SECTOR, fish.getX(), gy, fish.getZ()).yaw(baseYaw).size(reach).line(reach, SWEEP).life(CHARGE + SWEEP_T).palette(0).spawn();
        FxEntity.at(fish.level(), FxType.WARN_LINE, fish.getX(), gy, fish.getZ()).yaw(baseYaw - sign * SWEEP / 2).line(reach, 1.2f).life(CHARGE).palette(0).spawn();
    }

    private Vec3 end(float yaw) {
        Vec3 d = Combat.dirFromYaw(yaw);
        double x = b.boss.getX() + d.x * reach, z = b.boss.getZ() + d.z * reach;
        return new Vec3(x, b.ground(x, z, b.boss.getY()), z);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        b.flight.brake();
        if (stage == 0) {
            b.flight.face(end(baseYaw - sign * SWEEP / 2));
            if (st >= CHARGE) {
                stage = 1;
                st = 0;
                FxEntity.at(fish.level(), FxType.SJ_BEAM, fish.position()).follow(fish, 0, 0, 0).yaw(baseYaw).size(reach).line(reach, sign * SWEEP).life(SWEEP_T).spawn();
                fish.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 2.5f, 0.5f);
                Fx.shake(fish.level(), fish.position(), 0.35f);
            }
            return false;
        }
        float k = Math.min(1f, st / (float) SWEEP_T);
        float yaw = baseYaw + sign * SWEEP * (k - 0.5f);
        Vec3 end = end(yaw);
        b.flight.face(end);
        if (st % 2 == 0) FxEntity.at(fish.level(), FxType.SJ_SPOUT, end).spawn();
        if (st % 5 == 0) Fx.rockBurst(fish.level(), end, 0.9f, 4);
        if (st % 6 == 0) fish.playSound(SoundEvents.GENERIC_SPLASH, 1.5f, 0.8f);
        Vec3 d = Combat.dirFromYaw(yaw);
        for (LivingEntity v : Combat.foes(fish, fish.getBoundingBox().inflate(reach + 1, 8, reach + 1))) {
            if (hit.contains(v.getId())) continue;
            double dx = v.getX() - fish.getX(), dz = v.getZ() - fish.getZ();
            double along = dx * d.x + dz * d.z;
            double side = Math.abs(-dx * d.z + dz * d.x);
            if (along < reach * 0.3 || along > reach + 1 || side > 1.1) continue;
            if (Combat.hit(fish, b.cfg, v, 0.9, 0.4)) hit.add(v.getId());
        }
        return st >= SWEEP_T;
    }

    @Override
    public void stop() {
        b.flight.face(null);
    }
}
