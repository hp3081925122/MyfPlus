package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.MudPitEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LurkSkill extends BossSkill {
    private static final int AMBUSH_WINDUP = 20;
    private final SwampjawBrain b;
    @Nullable
    private MudPitEntity entry;
    @Nullable
    private FxEntity fin;
    private Vec3 strike = Vec3.ZERO;
    private int stage;
    private int st;

    public LurkSkill(SwampjawBrain b) {
        super("lurk", 300);
        this.b = b;
    }

    @Override
    public double cooldownScale() {
        return b.phase2 ? 0.6 : 1.0;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return b.nearestPit(b.boss.position(), 24) != null;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        fin = null;
        entry = b.nearestPit(b.boss.position(), 24);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                if (entry == null || entry.isRemoved()) return true;
                Vec3 p = entry.position();
                b.flight.steer(p.add(0, -0.5, 0), 1.0);
                b.flight.face(p);
                if (fish.position().distanceTo(p) < 1.6 || st >= 24) {
                    FxEntity.at(fish.level(), FxType.SJ_GEYSER_SMALL, p).spawn();
                    fish.playSound(SoundEvents.PLAYER_SPLASH, 2.0f, 0.6f);
                    b.submerged = true;
                    b.setSkill(SwampjawBrain.SKILL_SUBMERGED);
                    fin = FxEntity.at(fish.level(), FxType.SJ_FIN, fish.position()).follow(fish, 0, 1.2, 0).life(200).spawn();
                    b.flight.manual();
                    b.flight.face(null);
                    stage = 1;
                    st = 0;
                }
            }
            case 1 -> {
                if (target == null) {
                    emerge(fish.position());
                    stage = 4;
                    st = 0;
                    return false;
                }
                MudPitEntity dest = b.nearestPit(target.position(), 64);
                Vec3 goal = dest != null ? dest.position() : target.position();
                swimToward(goal, 0.5);
                if (st % 4 == 0) FxEntity.at(fish.level(), FxType.SJ_RIPPLE, fish.getX(), b.ground(fish.getX(), fish.getZ(), fish.getY() + 1.2), fish.getZ()).spawn();
                if (b.nearAnyPit(target, 0) || st >= 60) {
                    beginAmbush(target);
                }
                else if (st >= 30 && dest != null && Combat.horizontalDist(fish.position(), goal) < 1.5) {
                    emerge(goal);
                    stage = 4;
                    st = 0;
                }
            }
            case 2 -> {
                Vec3 under = strike.add(0, -1.5, 0);
                Vec3 d = under.subtract(fish.position());
                double len = d.length();
                fish.setDeltaMovement(len > 0.05 ? d.scale(Math.min(0.8, len) / len) : Vec3.ZERO);
                if (st >= AMBUSH_WINDUP) {
                    stage = 3;
                    st = 0;
                    surface();
                    FxEntity.at(fish.level(), FxType.SJ_GEYSER, strike).spawn();
                    FxEntity.at(fish.level(), FxType.SJ_CHOMP, strike.add(0, 1.8, 0)).size(1.8f).spawn();
                    Fx.rockBurst(fish.level(), strike, 1.6f, 14);
                    Fx.rockRing(fish.level(), strike, 3.5f, 0.5f);
                    Fx.shake(fish.level(), strike, 0.9f);
                    fish.playSound(SoundEvents.GENERIC_SPLASH, 3.0f, 0.5f);
                    fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 2.0f, 0.6f);
                    AABB box = new AABB(strike.x - 1.75, strike.y - 0.5, strike.z - 1.75, strike.x + 1.75, strike.y + 3.5, strike.z + 1.75);
                    for (LivingEntity v : Combat.foes(fish, box, e -> Combat.horizontalDist(e.position(), strike) <= 1.75)) {
                        if (Combat.hit(fish, b.cfg, v, 1.1, 0)) Combat.push(v, new Vec3(0, 0.5, 0));
                    }
                    fish.setPos(strike.x, strike.y - 1, strike.z);
                }
            }
            case 3 -> {
                fish.setDeltaMovement(0, 0.9, 0);
                if (st >= 8) return true;
            }
            case 4 -> {
                fish.setDeltaMovement(0, 0.8, 0);
                if (st >= 6) {
                    b.director.force(b.dash);
                    return true;
                }
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void swimToward(Vec3 goal, double speed) {
        SwampjawEntity fish = b.boss;
        Vec3 h = new Vec3(goal.x - fish.getX(), 0, goal.z - fish.getZ());
        double len = h.length();
        Vec3 v = len > 0.05 ? h.scale(Math.min(speed, len) / len) : Vec3.ZERO;
        double gy = b.ground(fish.getX() + v.x, fish.getZ() + v.z, fish.getY() + 1.2);
        fish.setDeltaMovement(v.x, (gy - 1.2 - fish.getY()) * 0.5, v.z);
    }

    private void beginAmbush(LivingEntity target) {
        strike = new Vec3(target.getX(), b.ground(target.getX(), target.getZ(), target.getY() + 0.5), target.getZ());
        FxEntity.at(b.boss.level(), FxType.SJ_AMBUSH_RING, strike).life(AMBUSH_WINDUP).palette(0).spawn();
        b.boss.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 2.0f, 0.6f);
        stage = 2;
        st = 0;
    }

    private void emerge(Vec3 at) {
        SwampjawEntity fish = b.boss;
        surface();
        FxEntity.at(fish.level(), FxType.SJ_GEYSER_SMALL, at.x, b.ground(at.x, at.z, at.y + 1.2), at.z).spawn();
        fish.playSound(SoundEvents.PLAYER_SPLASH, 2.0f, 0.7f);
    }

    private void surface() {
        b.submerged = false;
        b.setSkill(0);
        if (fin != null) fin.discard();
        fin = null;
    }

    @Override
    public void stop() {
        surface();
        b.flight.brake();
    }
}
