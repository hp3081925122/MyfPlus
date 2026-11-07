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

/** 破沼跃袭：扎进地里，顶着一道方块浪游向玩家，破土跃起后沿弧线砸下咬合。P2 连跃 3 次。 */
public class BreachSkill extends BossSkill {
    private static final int LEAP = 18;
    private static final float LAND_R = 2.6f;
    private final SwampjawBrain b;
    private Vec3 from = Vec3.ZERO;
    private Vec3 to = Vec3.ZERO;
    private double entryY;
    private int stage;
    private int st;
    private int leaps;
    private int burrowMin;
    @Nullable
    private FxEntity fin;

    public BreachSkill(SwampjawBrain b) {
        super("breach", 80);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist >= 4 && dist <= 28 && b.grounded(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        leaps = b.phase2 ? 3 : 1;
        burrowMin = 6;
        fin = null;
        entryY = b.ground(b.boss.getX(), b.boss.getZ(), b.boss.getY());
        b.boss.setAnimation(SwampjawEntity.ANIM_SWOOP);
        b.boss.playSound(SoundEvents.PHANTOM_SWOOP, 2.0f, 0.6f);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                b.flight.manual();
                b.flight.face(null);
                Vec3 aim = target != null ? target.position() : fish.position();
                Vec3 h = new Vec3(aim.x - fish.getX(), 0, aim.z - fish.getZ());
                Vec3 hv = h.lengthSqr() > 0.01 ? h.normalize().scale(0.35) : Vec3.ZERO;
                fish.setDeltaMovement(hv.x, -1.0, hv.z);
                if (fish.getY() <= entryY - 0.6 || st >= 16) submerge(entryY);
            }
            case 1 -> {
                if (target == null) return true;
                Vec3 h = new Vec3(target.getX() - fish.getX(), 0, target.getZ() - fish.getZ());
                double len = h.length();
                double sp = b.phase2 ? 0.95 : 0.75;
                Vec3 v = len > 0.05 ? h.scale(Math.min(sp, len) / len) : Vec3.ZERO;
                double gy = b.ground(fish.getX() + v.x, fish.getZ() + v.z, target.getY() + 1);
                fish.setDeltaMovement(v.x, (gy - 1.6 - fish.getY()) * 0.5, v.z);
                if (st % 3 == 0 && len > 0.05) Fx.rockLine(fish.level(), new Vec3(fish.getX(), gy, fish.getZ()), Combat.yawTo(Vec3.ZERO, v), 2.4f, 1.6f, 0.8f);
                if (st % 6 == 0) fish.playSound(SoundEvents.ROOTED_DIRT_BREAK, 1.8f, 0.5f);
                if ((len <= 6.5 && st >= burrowMin) || st >= 40) leap(target);
            }
            case 2 -> {
                float k = Math.min(1f, st / (float) LEAP);
                Vec3 p = from.lerp(to, k).add(0, 6.5 * 4 * k * (1 - k), 0);
                fish.setDeltaMovement(p.subtract(fish.position()));
                if (st >= LEAP) land();
            }
            case 3 -> {
                fish.setDeltaMovement(0, 0.55, 0);
                if (st >= 12) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void submerge(double gy) {
        SwampjawEntity fish = b.boss;
        Vec3 p = new Vec3(fish.getX(), gy, fish.getZ());
        FxEntity.at(fish.level(), FxType.SJ_BREACH, p).size(0.8f).spawn();
        Fx.rockBurst(fish.level(), p, 1.2f, 8);
        fish.playSound(SoundEvents.GENERIC_SPLASH, 2.0f, 0.6f);
        fish.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
        b.dive();
        fin = FxEntity.at(fish.level(), FxType.SJ_FIN, fish.position()).follow(fish, 0, 1.6, 0).life(200).spawn();
        stage = 1;
        st = 0;
    }

    private void leap(LivingEntity target) {
        SwampjawEntity fish = b.boss;
        double gy = b.ground(fish.getX(), fish.getZ(), target.getY() + 1);
        from = new Vec3(fish.getX(), gy - 0.4, fish.getZ());
        to = b.groundAt(target.getX(), target.getZ(), target.getY() + 0.5);
        dropFin();
        b.surface();
        fish.setPos(from.x, from.y, from.z);
        fish.setAnimation(SwampjawEntity.ANIM_SWOOP);
        Vec3 g = new Vec3(from.x, gy, from.z);
        FxEntity.at(fish.level(), FxType.SJ_BREACH, g).spawn();
        Fx.rockBurst(fish.level(), g, 1.6f, 12);
        Fx.rockRing(fish.level(), g, 2.5f, 0.6f);
        Fx.shake(fish.level(), g, 0.45f);
        FxEntity.at(fish.level(), FxType.SJ_DROP, to).life(LEAP).spawn();
        fish.playSound(SoundEvents.GENERIC_SPLASH, 2.5f, 0.5f);
        fish.playSound(SoundEvents.RAVAGER_ROAR, 1.2f, 1.4f);
        for (LivingEntity v : Combat.foes(fish, new AABB(g, g).inflate(2, 3, 2), e -> Combat.horizontalDist(e.position(), g) <= 2)) {
            if (Combat.hit(fish, b.cfg, v, 0.6, 0)) Combat.push(v, new Vec3(0, 0.45, 0));
        }
        leaps--;
        stage = 2;
        st = 0;
    }

    private void land() {
        SwampjawEntity fish = b.boss;
        fish.setPos(to.x, to.y, to.z);
        FxEntity.at(fish.level(), FxType.SJ_MAW, to.add(0, 1.3, 0)).size(1.5f).spawn();
        Fx.rockRing(fish.level(), to, 3.8f, 0.7f);
        Fx.rockBurst(fish.level(), to, 1.8f, 14);
        Fx.shake(fish.level(), to, 0.9f);
        fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 2.5f, 0.6f);
        fish.playSound(SoundEvents.GENERIC_EXPLODE, 1.2f, 0.8f);
        for (LivingEntity v : Combat.foes(fish, new AABB(to, to).inflate(LAND_R, 3, LAND_R),
                e -> Combat.horizontalDist(e.position(), to) <= LAND_R && Combat.inGroundBand(fish.level(), e, to.y, 3))) {
            Combat.hit(fish, b.cfg, v, 1.2, 0.6);
        }
        fish.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
        if (leaps > 0) {
            fish.setPos(to.x, to.y - 1.6, to.z);
            b.dive();
            fin = FxEntity.at(fish.level(), FxType.SJ_FIN, fish.position()).follow(fish, 0, 1.6, 0).life(200).spawn();
            burrowMin = 10;
            stage = 1;
        }
        else stage = 3;
        st = 0;
    }

    private void dropFin() {
        if (fin != null) fin.discard();
        fin = null;
    }

    @Override
    public void stop() {
        dropFin();
        b.surface();
        b.flight.brake();
        b.flight.face(null);
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
    }
}
