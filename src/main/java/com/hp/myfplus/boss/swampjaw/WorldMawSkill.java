package com.hp.myfplus.boss.swampjaw;

import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.FossilJawEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * P2 大招「噬界巨颌」：玩家脚下展开半径 10 的獠牙环，獠牙从远端开始依次升起，只在背对沼牙的一侧留缺口。
 * 已升起的獠牙把人推回圈内。合拢时沼牙从圆心钻出，圈内所有人受到重击。
 */
public class WorldMawSkill extends BossSkill {
    private static final int RISE = 16;
    private static final float R = FossilJawEntity.RING_RADIUS;
    private final SwampjawBrain b;
    @Nullable
    private FossilJawEntity jaw;
    private Vec3 center = Vec3.ZERO;
    private int stage;
    private int st;

    public WorldMawSkill(SwampjawBrain b) {
        super("world_maw", 560);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return b.phase2 && dist <= 20 && b.grounded(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        jaw = null;
        center = target.position();
        b.setSkill(SwampjawBrain.SKILL_JAW);
        b.boss.playSound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.45f);
        b.boss.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0f, 0.5f);
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                Vec3 base = target != null ? target.position() : center;
                b.flight.steer(base.add(0, 10, 0), 0.7);
                b.flight.face(base);
                if (st >= RISE) {
                    open(base);
                    stage = 1;
                    st = 0;
                }
            }
            case 1 -> {
                int age = st;
                if (age < FossilJawEntity.RING_CLAMP - 14) {
                    b.flight.steer(center.add(0, 9, 0), 0.5);
                    b.flight.face(center);
                }
                else if (!b.submerged()) {
                    b.flight.manual();
                    Vec3 d = center.add(0, -2, 0).subtract(fish.position());
                    double len = d.length();
                    fish.setDeltaMovement(len > 0.05 ? d.scale(Math.min(1.6, len) / len) : Vec3.ZERO);
                    if (len < 1.5) {
                        b.dive();
                        FxEntity.at(fish.level(), FxType.SJ_BREACH, center).size(1.4f).spawn();
                        fish.playSound(SoundEvents.GENERIC_SPLASH, 3.0f, 0.4f);
                        Fx.shake(fish.level(), center, 0.5f);
                    }
                }
                else {
                    fish.setDeltaMovement(Vec3.ZERO);
                    fish.setPos(center.x, center.y - 2.5, center.z);
                }
                wall(age);
                if (age == FossilJawEntity.RING_CLAMP) {
                    fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 3.0f, 0.4f);
                    fish.playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.6f);
                }
                if (age >= FossilJawEntity.RING_CLAMP + FossilJawEntity.RING_CLOSE) {
                    erupt();
                    stage = 2;
                    st = 0;
                }
            }
            case 2 -> {
                b.flight.manual();
                fish.setDeltaMovement(0, 0.9 * Math.max(0, 1 - st / 12.0), 0);
                if (st >= 16) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void open(Vec3 base) {
        SwampjawEntity fish = b.boss;
        center = b.groundAt(base.x, base.z, base.y + 0.5);
        FossilJawEntity j = new FossilJawEntity(MyfEntities.FOSSIL_JAW.get(), fish.level());
        j.setPos(center.x, center.y, center.z);
        j.setOwner(fish);
        j.mode = FossilJawEntity.RING;
        j.yaw = Combat.yawTo(fish.position(), center) + (fish.getRandom().nextFloat() - 0.5f) * 80f;
        j.life = FossilJawEntity.RING_LIFE;
        fish.level().addFreshEntity(j);
        jaw = j;
        FxEntity.at(fish.level(), FxType.WARN_CIRCLE, center).size(R).life(FossilJawEntity.RING_CLAMP + FossilJawEntity.RING_CLOSE).palette(0).spawn();
        Fx.shake(fish.level(), center, 0.4f);
        fish.playSound(SoundEvents.ROOTED_DIRT_BREAK, 3.0f, 0.4f);
    }

    /** 已升起的獠牙算作墙：碰到的人被推回圆心方向，并受到少量伤害。 */
    private void wall(int age) {
        if (jaw == null || age >= FossilJawEntity.RING_CLAMP) return;
        SwampjawEntity fish = b.boss;
        List<LivingEntity> foes = Combat.foes(fish, new AABB(center, center).inflate(R + 2, 4, R + 2));
        if (foes.isEmpty()) return;
        float[] o = new float[3];
        for (int i = 0; i < jaw.teeth(); i++) {
            if (!jaw.tooth(i, age, o) || o[2] < 0.9f) continue;
            double tx = center.x + o[0], tz = center.z + o[1];
            for (LivingEntity v : foes) {
                double dx = v.getX() - tx, dz = v.getZ() - tz;
                if (dx * dx + dz * dz > 1.2 * 1.2 || !Combat.inGroundBand(fish.level(), v, center.y, 3.5)) continue;
                Vec3 in = new Vec3(center.x - v.getX(), 0, center.z - v.getZ());
                if (in.lengthSqr() > 1.0E-4) in = in.normalize();
                Combat.setHorizontalVelocity(v, in.x * 0.6, in.z * 0.6);
                Combat.hit(fish, fish.damageSources().mobAttack(fish), Combat.damage(fish, b.cfg, 0.3), v, 0, center);
            }
        }
    }

    private void erupt() {
        SwampjawEntity fish = b.boss;
        b.surface();
        fish.setPos(center.x, center.y - 1, center.z);
        fish.setXRot(-60);
        fish.setAnimation(SwampjawEntity.ANIM_SWOOP);
        FxEntity.at(fish.level(), FxType.SJ_MAW, center.add(0, 2.6, 0)).size(2.6f).life(12).spawn();
        FxEntity.at(fish.level(), FxType.SJ_BREACH, center).size(1.6f).spawn();
        FxEntity.at(fish.level(), FxType.SJ_ROAR, center.add(0, 1.0, 0)).size(1.4f).spawn();
        Fx.rockRing(fish.level(), center, R, 0.9f);
        Fx.rockBurst(fish.level(), center, 3f, 22);
        Fx.shake(fish.level(), center, 1.4f);
        fish.playSound(SoundEvents.GENERIC_EXPLODE, 3.0f, 0.5f);
        fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 3.0f, 0.5f);
        for (LivingEntity v : Combat.foes(fish, new AABB(center, center).inflate(R + 1.5, 4, R + 1.5),
                e -> Combat.horizontalDist(e.position(), center) <= R + 1.0 && Combat.inGroundBand(fish.level(), e, center.y, 4))) {
            if (Combat.hit(fish, b.cfg, v, 1.8, 0)) Combat.push(v, new Vec3(0, 0.6, 0));
        }
    }

    @Override
    public void stop() {
        jaw = null;
        b.surface();
        b.flight.brake();
        b.flight.face(null);
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
    }
}
