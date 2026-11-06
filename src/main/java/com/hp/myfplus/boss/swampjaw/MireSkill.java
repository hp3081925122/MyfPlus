package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MireSkill extends BossSkill {
    private static final float RADIUS = 3f;
    private final SwampjawBrain b;
    private Vec3 slam = Vec3.ZERO;
    private int impactAt;

    public MireSkill(SwampjawBrain b) {
        super("mire", -1);
        this.b = b;
    }

    @Override
    public void start(LivingEntity target) {
        impactAt = -1;
        slam = target.position();
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        if (t <= 20) {
            double baseY = target != null ? target.getY() : fish.getY();
            b.flight.steer(new Vec3(fish.getX(), baseY + 10, fish.getZ()), 0.8);
            b.flight.face(null);
            if (t == 20) {
                Vec3 p = target != null ? target.position() : fish.position();
                slam = new Vec3(p.x, b.ground(p.x, p.z, p.y + 0.5), p.z);
                FxEntity.at(fish.level(), FxType.WARN_CIRCLE, slam).size(RADIUS).life(32).palette(0).spawn();
                fish.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.0f, 1.6f);
            }
            return false;
        }
        if (t <= 40) {
            b.flight.steer(slam.add(0, 10, 0), 0.9);
            b.flight.face(slam);
            return false;
        }
        if (impactAt < 0) {
            b.flight.manual();
            Vec3 d = slam.add(0, 0.5, 0).subtract(fish.position());
            double len = d.length();
            fish.setDeltaMovement(len > 0.05 ? d.scale(Math.min(1.3, len) / len) : Vec3.ZERO);
            if (fish.getY() <= slam.y + 1.0 || t >= 58) {
                impactAt = t;
                impact();
            }
            return false;
        }
        b.flight.steer(slam.add(0, 6, 0), 0.6);
        b.flight.face(null);
        return t - impactAt >= 12;
    }

    private void impact() {
        SwampjawEntity fish = b.boss;
        fish.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.6f);
        fish.playSound(SoundEvents.MUD_BREAK, 3.0f, 0.5f);
        FxEntity.at(fish.level(), FxType.SJ_SLAM, slam).size(RADIUS * 2).spawn();
        FxEntity.at(fish.level(), FxType.SJ_GEYSER, slam).spawn();
        Fx.rockRing(fish.level(), slam, 9f, 0.6f);
        Fx.rockBurst(fish.level(), slam, 2.5f, 18);
        Fx.shake(fish.level(), slam, 1.2f);
        Fx.banner(fish.level(), slam, Fx.BOSS_SWAMPJAW, 2);
        b.setPhase(2);
        AABB box = new AABB(slam.x - RADIUS, slam.y - 1, slam.z - RADIUS, slam.x + RADIUS, slam.y + 3, slam.z + RADIUS);
        for (LivingEntity v : Combat.foes(fish, box, e -> Combat.horizontalDist(e.position(), slam) <= RADIUS)) Combat.hit(fish, b.cfg, v, 0.8, 0.5);
        b.phase2 = true;
        double[][] offs = {{5, 0}, {-5, 0}, {0, 5}, {0, -5}};
        for (double[] o : offs) {
            double x = slam.x + o[0];
            double z = slam.z + o[1];
            b.addPit(new Vec3(x, b.ground(x, z, slam.y + 1), z));
        }
        b.lurk.lastUsed = -1000000L;
    }

    @Override
    public void stop() {
        b.phase2 = true;
        b.flight.brake();
        b.flight.face(null);
    }
}
