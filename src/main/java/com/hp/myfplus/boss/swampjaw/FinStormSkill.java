package com.hp.myfplus.boss.swampjaw;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.FinBladeEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 骨鳍回旋：玩家超过 5 格时先游近（最多 40 tick），再压低身位原地高速自转，甩出 6 片（P2 为 10 片，正反交替）骨鳍刃，螺旋飞出后再回旋收回。 */
public class FinStormSkill extends BossSkill {
    private static final int WINDUP = 16;
    private static final double CLOSE = 5.0;
    private static final int APPROACH_MAX = 40;
    private final SwampjawBrain b;
    private final List<FinBladeEntity> blades = new ArrayList<>();
    private double groundY;
    private float spinYaw;
    private boolean thrown;
    private boolean approaching;
    private int st;
    private int windStart;

    public FinStormSkill(SwampjawBrain b) {
        super("fin_storm", 160);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 14;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return b.phase2 ? 3 : 2;
    }

    @Override
    public void start(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st = 0;
        thrown = false;
        blades.clear();
        approaching = Combat.horizontalDist(fish.position(), target.position()) > CLOSE;
        if (approaching) fish.playSound(SoundEvents.DOLPHIN_SWIM, 2.0f, 0.5f);
        else windup(target);
    }

    private void windup(@Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        approaching = false;
        windStart = st;
        spinYaw = fish.getYRot();
        double ref = target != null ? Math.max(fish.getY(), target.getY() + 1) : fish.getY();
        groundY = b.ground(fish.getX(), fish.getZ(), ref);
        fish.playSound(SoundEvents.TRIDENT_RIPTIDE_1, 2.0f, 0.6f);
    }

    private boolean approach(@Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        if (target == null) return false;
        Vec3 to = target.position();
        double h = Combat.horizontalDist(fish.position(), to);
        if (h <= CLOSE || st >= APPROACH_MAX) return false;
        Vec3 dir = new Vec3(to.x - fish.getX(), 0, to.z - fish.getZ()).normalize();
        double x = to.x - dir.x * CLOSE * 0.8, z = to.z - dir.z * CLOSE * 0.8;
        double gy = b.ground(x, z, Math.max(fish.getY(), target.getY() + 1));
        b.flight.steer(new Vec3(x, gy + 2.5, z), b.phase2 ? 1.1 : 0.9);
        b.flight.face(to);
        return true;
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        if (approaching) {
            if (approach(target)) return false;
            windup(target);
        }
        int ws = st - windStart;
        b.flight.steer(new Vec3(fish.getX(), groundY + 2.5, fish.getZ()), 0.5);
        b.flight.face(null);
        spinYaw += thrown ? 36f : 4f + ws * 2f;
        fish.setYRot(spinYaw);
        fish.yBodyRot = spinYaw;
        fish.yHeadRot = spinYaw;
        if (!thrown && ws >= WINDUP) throwBlades(target);
        if (thrown) {
            for (FinBladeEntity f : blades) {
                if (f.isRemoved()) continue;
                Vec3 p = f.position();
                for (LivingEntity v : Combat.foes(fish, new AABB(p, p).inflate(0.9, 1.2, 0.9))) Combat.hit(fish, b.cfg, v, 0.5, 0.3);
            }
            if (ws % 8 == 0) fish.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
        }
        return thrown && ws >= WINDUP + FinBladeEntity.LIFE;
    }

    private void throwBlades(@Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        int n = b.phase2 ? 10 : 6;
        double cy = (target != null ? Math.max(groundY, target.getY()) : groundY) + 1.0;
        for (int i = 0; i < n; i++) {
            FinBladeEntity f = new FinBladeEntity(MyfEntities.FIN_BLADE.get(), fish.level());
            f.cx = fish.getX();
            f.cy = cy;
            f.cz = fish.getZ();
            f.angle = i * 360f / n;
            f.spin = b.phase2 && i % 2 == 1 ? -1 : 1;
            f.setOwner(fish);
            Vec3 p = f.at(0);
            f.setPos(p.x, p.y, p.z);
            fish.level().addFreshEntity(f);
            blades.add(f);
        }
        Fx.shake(fish.level(), fish.position(), 0.3f);
        Fx.rockRing(fish.level(), new Vec3(fish.getX(), groundY, fish.getZ()), 3f, 0.6f);
        fish.playSound(SoundEvents.TRIDENT_THROW, 2.0f, 0.5f);
        thrown = true;
    }

    @Override
    public void stop() {
        blades.clear();
        b.flight.brake();
        b.flight.face(null);
    }
}
