package com.hp.myfplus.boss.swampjaw;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DashSkill extends BossSkill {
    private static final int WINDUP = 15;
    private final SwampjawBrain b;
    private Vec3 start = Vec3.ZERO;
    private Vec3 dir = new Vec3(0, 0, 1);
    private double speed;
    private double groundY;
    private int dashTicks;
    private int stage;
    private int st;
    private final Set<Integer> hit = new HashSet<>();

    public DashSkill(SwampjawBrain b) {
        super("dash", 60);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 6 && dist <= 22;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    public void start(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        Vec3 tp = target.position();
        groundY = b.ground(fish.getX(), fish.getZ(), tp.y + 0.5);
        start = new Vec3(fish.getX(), Math.max(tp.y, groundY) + 1.0, fish.getZ());
        Vec3 h = new Vec3(tp.x - start.x, 0, tp.z - start.z);
        double len = h.length();
        dir = len < 0.5 ? Combat.dirFromYaw(fish.getYRot()) : h.scale(1 / len);
        double total = len + 4;
        dashTicks = Mth.clamp((int) Math.round(total / 1.2), 8, 18);
        speed = total / dashTicks;
        stage = 0;
        st = 0;
        hit.clear();
        float yaw = Combat.yawTo(Vec3.ZERO, dir);
        FxEntity.at(fish.level(), FxType.WARN_LINE, start.x, groundY, start.z).yaw(yaw).line((float) total, 1.5f).life(WINDUP + dashTicks).palette(0).spawn();
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                b.flight.steer(start, 0.6);
                b.flight.face(start.add(dir.scale(6)));
                if (t >= WINDUP) {
                    stage = 1;
                    st = 0;
                    fish.setAnimation(SwampjawEntity.ANIM_SWOOP);
                    b.dashing = true;
                    b.flight.manual();
                    b.flight.face(null);
                    fish.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.7f);
                }
            }
            case 1 -> {
                fish.setDeltaMovement(dir.x * speed, (start.y - fish.getY()) * 0.4, dir.z * speed);
                if (st % 2 == 0) {
                    Vec3 back = fish.position().subtract(dir.scale(1.5));
                    FxEntity.at(fish.level(), FxType.SJ_SPLASH, back.x, b.ground(back.x, back.z, start.y), back.z).spawn();
                }
                for (LivingEntity v : Combat.foes(fish, fish.getBoundingBox().inflate(0.4))) {
                    if (hit.add(v.getId())) Combat.hit(fish, b.cfg, v, 1.0, 0.6);
                }
                if (b.stunRequested) {
                    b.dashing = false;
                    b.stunRequested = false;
                    b.stunned = true;
                    stage = 3;
                    st = 0;
                    fish.setAnimation(SwampjawEntity.ANIM_STUN);
                    fish.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 1.5f, 0.6f);
                    return false;
                }
                if (st >= dashTicks) {
                    b.dashing = false;
                    fish.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
                    if (!b.phase2) return true;
                    stage = 2;
                    st = 0;
                }
            }
            case 2 -> {
                b.flight.brake();
                if (st == 1) fish.setAnimation(SwampjawEntity.ANIM_SWIPE);
                if (st == 6) {
                    fish.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.8f);
                    for (LivingEntity v : Combat.foes(fish, fish.getBoundingBox().inflate(1.2, 0.6, 1.2))) Combat.hit(fish, b.cfg, v, 0.6, 0.5);
                }
                if (st >= 12) return true;
            }
            case 3 -> {
                b.flight.manual();
                double gy = b.ground(fish.getX(), fish.getZ(), fish.getY());
                fish.setDeltaMovement(0, (gy + 0.1 - fish.getY()) * 0.3, 0);
                if (st >= 40) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        b.dashing = false;
        b.stunned = false;
        b.stunRequested = false;
        b.flight.brake();
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
    }
}
