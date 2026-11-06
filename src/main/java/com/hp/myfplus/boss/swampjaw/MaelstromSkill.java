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

public class MaelstromSkill extends BossSkill {
    private static final float RADIUS = 9f;
    private static final float CORE = 3f;
    private static final int PULL = 70;
    private final SwampjawBrain b;
    private Vec3 center = Vec3.ZERO;
    private int stage;
    private int st;

    public MaelstromSkill(SwampjawBrain b) {
        super("maelstrom", 520);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return b.phase2 && dist <= 22;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        center = target.position();
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        st++;
        switch (stage) {
            case 0 -> {
                Vec3 base = target != null ? target.position() : center;
                b.flight.steer(new Vec3(base.x, base.y + 9, base.z), 0.7);
                b.flight.face(base);
                if (st == 1) fish.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0f, 0.5f);
                if (st >= 20) {
                    center = new Vec3(base.x, b.ground(base.x, base.z, base.y + 0.5), base.z);
                    FxEntity.at(fish.level(), FxType.WARN_CIRCLE, center).size(RADIUS).life(24).palette(0).spawn();
                    stage = 1;
                    st = 0;
                }
            }
            case 1 -> {
                b.flight.manual();
                b.flight.face(center);
                Vec3 d = center.add(0, -1.5, 0).subtract(fish.position());
                double len = d.length();
                fish.setDeltaMovement(len > 0.05 ? d.scale(Math.min(1.4, len) / len) : Vec3.ZERO);
                if (len < 1.8 || st >= 24) {
                    b.submerged = true;
                    b.setSkill(SwampjawBrain.SKILL_SUBMERGED);
                    b.flight.face(null);
                    fish.playSound(SoundEvents.GENERIC_SPLASH, 3.0f, 0.4f);
                    FxEntity.at(fish.level(), FxType.SJ_GEYSER, center).spawn();
                    FxEntity.at(fish.level(), FxType.SJ_VORTEX, center).size(RADIUS * 2).life(PULL + 6).spawn();
                    Fx.rockRing(fish.level(), center, RADIUS, 0.45f);
                    Fx.shake(fish.level(), center, 0.7f);
                    stage = 2;
                    st = 0;
                }
            }
            case 2 -> {
                fish.setDeltaMovement(Vec3.ZERO);
                fish.setPos(center.x, center.y - 2.5, center.z);
                AABB box = new AABB(center.x - RADIUS - 2, center.y - 2, center.z - RADIUS - 2, center.x + RADIUS + 2, center.y + 4, center.z + RADIUS + 2);
                for (LivingEntity v : Combat.foes(fish, box)) {
                    double dx = center.x - v.getX(), dz = center.z - v.getZ();
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > RADIUS + 1 || dist < 0.4) continue;
                    double pull = 0.07 * (0.6 + 0.4 * (1 - dist / (RADIUS + 1)));
                    Combat.push(v, new Vec3(dx / dist * pull - dz / dist * 0.025, 0, dz / dist * pull + dx / dist * 0.025));
                }
                if (st % 12 == 0) {
                    Fx.shake(fish.level(), center, 0.25f);
                    fish.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 2.5f, 0.6f);
                }
                if (st % 18 == 9) {
                    double a = fish.getRandom().nextDouble() * Math.PI * 2;
                    double r = 3 + fish.getRandom().nextDouble() * (RADIUS - 4);
                    double x = center.x + Math.cos(a) * r, z = center.z + Math.sin(a) * r;
                    Fx.rockBurst(fish.level(), new Vec3(x, b.ground(x, z, center.y + 1), z), 1.0f, 5);
                }
                if (st == PULL - 22) {
                    FxEntity.at(fish.level(), FxType.WARN_CIRCLE, center).size(CORE).life(22).palette(0).spawn();
                    fish.playSound(SoundEvents.RAVAGER_ROAR, 2.0f, 0.6f);
                }
                if (st >= PULL) {
                    erupt();
                    stage = 3;
                    st = 0;
                }
            }
            case 3 -> {
                b.flight.manual();
                fish.setDeltaMovement(0, 0.9 * Math.max(0, 1 - st / 12.0), 0);
                if (st >= 14) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void erupt() {
        SwampjawEntity fish = b.boss;
        b.submerged = false;
        b.setSkill(0);
        fish.setPos(center.x, center.y - 1, center.z);
        fish.setXRot(-60);
        fish.setAnimation(SwampjawEntity.ANIM_SWOOP);
        fish.playSound(SoundEvents.GENERIC_EXPLODE, 2.5f, 0.6f);
        fish.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 3.0f, 0.5f);
        FxEntity.at(fish.level(), FxType.SJ_GEYSER, center).size(1.6f).spawn();
        FxEntity.at(fish.level(), FxType.SJ_CHOMP, center.add(0, 2.2, 0)).size(2.4f).life(10).spawn();
        FxEntity.at(fish.level(), FxType.SJ_SLAM, center).size(CORE * 3).spawn();
        Fx.rockBurst(fish.level(), center, 2.5f, 22);
        Fx.rockRing(fish.level(), center, 4.5f, 0.8f);
        Fx.shake(fish.level(), center, 1.3f);
        AABB box = new AABB(center.x - CORE, center.y - 1, center.z - CORE, center.x + CORE, center.y + 4, center.z + CORE);
        for (LivingEntity v : Combat.foes(fish, box, e -> Combat.horizontalDist(e.position(), center) <= CORE)) {
            if (Combat.hit(fish, b.cfg, v, 1.6, 0)) Combat.push(v, new Vec3(0, 0.8, 0));
        }
    }

    @Override
    public void stop() {
        b.submerged = false;
        b.setSkill(0);
        b.flight.brake();
        b.flight.face(null);
        b.boss.setAnimation(SwampjawEntity.ANIM_NEUTRAL);
    }
}
