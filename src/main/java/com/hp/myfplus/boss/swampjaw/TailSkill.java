package com.hp.myfplus.boss.swampjaw;

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

/** 回身尾击：玩家贴到 5 格内时连甩两次尾，每次 160° 扇形，带出一片碎块。 */
public class TailSkill extends BossSkill {
    private static final int WARN = 12;
    private static final int SECOND = WARN + 2;
    private static final float REACH = 5.2f;
    private static final float ARC = 160f;
    private final SwampjawBrain b;
    private float yaw;
    private int st;

    public TailSkill(SwampjawBrain b) {
        super("tail", 50);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 5;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 8;
    }

    @Override
    public void start(LivingEntity target) {
        st = 0;
        warn(target);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        st++;
        b.flight.brake();
        b.flight.face(target != null ? target.position() : null);
        if (st == WARN || st == SECOND + WARN) swing();
        if (st == SECOND && target != null) warn(target);
        if (st == WARN + 1 || st == SECOND + WARN + 6) b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
        return st >= SECOND + WARN + 8;
    }

    private void warn(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        yaw = Combat.yawTo(fish.position(), target.position());
        double gy = b.ground(fish.getX(), fish.getZ(), fish.getY());
        FxEntity.at(fish.level(), FxType.WARN_SECTOR, fish.getX(), gy, fish.getZ()).yaw(yaw).size(REACH).line(REACH, ARC).life(WARN).palette(0).spawn();
        fish.playSound(SoundEvents.ELDER_GUARDIAN_FLOP, 1.5f, 0.6f);
    }

    private void swing() {
        SwampjawEntity fish = b.boss;
        fish.setAnimation(SwampjawEntity.ANIM_SWIPE);
        FxEntity arc = FxEntity.at(fish.level(), FxType.SJ_SWIPE, fish.position()).yaw(yaw).size(REACH - 0.4f).line(REACH, ARC);
        arc.offY = 0.4;
        arc.spawn();
        for (int i = -1; i <= 1; i++) {
            Vec3 d = Combat.dirFromYaw(yaw + i * 50f);
            double x = fish.getX() + d.x * 3.5, z = fish.getZ() + d.z * 3.5;
            Fx.rockBurst(fish.level(), b.groundAt(x, z, fish.getY()), 0.8f, 4);
        }
        fish.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.6f);
        fish.playSound(SoundEvents.GENERIC_SPLASH, 1.5f, 0.9f);
        for (LivingEntity v : Combat.foes(fish, fish.getBoundingBox().inflate(REACH, 3, REACH))) {
            if (Combat.horizontalDist(v.position(), fish.position()) > REACH) continue;
            if (Math.abs(Mth.wrapDegrees(Combat.yawTo(fish.position(), v.position()) - yaw)) > ARC / 2) continue;
            Combat.hit(fish, b.cfg, v, 0.7, 0.7);
        }
    }

    @Override
    public void stop() {
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
        b.flight.face(null);
    }
}
