package com.hp.myfplus.boss.rosalyne;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WaltzSkill extends RosaSkill {
    private static final int DASHES = 4;
    private static final int WARN = 14;
    private static final float SPEED = 1.8f;
    private static final float FINAL_R = 4f;
    private final Set<Integer> hit = new HashSet<>();
    private Vec3 center = Vec3.ZERO;
    private float radius;
    private double base;
    private int dash;
    private Vec3 from = Vec3.ZERO;
    private Vec3 to = Vec3.ZERO;
    private float yaw;
    private int dashTicks;

    public WaltzSkill(RosalyneBrain b) {
        super(b, "waltz", 600);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return b.act >= 3 && dist <= 24;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    protected void begin(LivingEntity target) {
        if (b.ring != null && !b.ring.isRemoved()) {
            center = b.ring.position();
            radius = b.ring.radius - 1.2f;
        }
        else {
            center = b.groundAt(target.position());
            radius = 8f;
        }
        base = Math.toRadians(Combat.yawTo(center, target.position()));
        dash = 0;
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        switch (stage) {
            case 0 -> {
                Vec3 up = center.add(0, 6, 0);
                b.hover(up.x, up.y, up.z, 1.0);
                if (st == 1) {
                    boss.setAnimation(RosalyneEntity.ANIM_SUMMONING);
                    boss.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 2.5f, 0.6f);
                    boss.playSound(SoundEvents.BEACON_ACTIVATE, 2.0f, 0.5f);
                }
                if (st == 10) FxEntity.at(boss.level(), FxType.RS_SUNSET, center.add(0, 7.5, 0)).size(2.4f).life(DASHES * 34 + 50).spawn();
                if (st >= 22) next();
            }
            case 1 -> {
                if (st == 1) {
                    hit.clear();
                    double a = base + dash * Math.PI * 3 / 4;
                    Vec3 d = new Vec3(Math.cos(a), 0, Math.sin(a));
                    from = b.groundAt(center.add(d.scale(radius))).add(0, 0.1, 0);
                    to = b.groundAt(center.subtract(d.scale(radius))).add(0, 0.1, 0);
                    yaw = Combat.yawTo(from, to);
                    dashTicks = Mth.ceil(Combat.horizontalDist(from, to) / SPEED);
                    b.blink(from);
                    b.face(yaw);
                    boss.setAnimation(RosalyneEntity.ANIM_PREPARE_DASH);
                    FxEntity.at(boss.level(), FxType.WARN_LINE, from).yaw(yaw).line((float) Combat.horizontalDist(from, to), 1.6f).life(WARN).palette(3).spawn();
                    FxEntity.at(boss.level(), FxType.RS_GLINT, from.add(Combat.dirFromYaw(yaw).scale(1.2)).add(0, 1.3, 0)).spawn();
                }
                b.face(yaw);
                if (st < WARN) b.place(from);
                if (st == WARN) {
                    b.swingSound();
                    boss.playSound(SoundEvents.TRIDENT_RIPTIDE_1, 2.0f, 1.2f);
                    Fx.thrust(boss.level(), from, yaw, (float) Combat.horizontalDist(from, to), 1.3f, 1.2f);
                    Fx.rockLine(boss.level(), from, yaw, (float) Combat.horizontalDist(from, to), 1.4f, SPEED);
                    Fx.shake(boss.level(), center, 0.35f);
                }
                if (st >= WARN && st < WARN + dashTicks) {
                    float k0 = (st - WARN) / (float) dashTicks, k1 = (st - WARN + 1) / (float) dashTicks;
                    Vec3 p0 = from.lerp(to, k0), p1 = from.lerp(to, k1);
                    b.place(p1);
                    b.hitSegment(p0, p1, 1.3, 1.0, 0.6, hit);
                }
                if (st >= WARN + dashTicks + 4) {
                    dash++;
                    if (dash >= DASHES) next();
                    else st = 0;
                }
            }
            case 2 -> {
                if (st == 1) {
                    b.blink(center.add(0, 8, 0));
                    boss.setAnimation(RosalyneEntity.ANIM_PREPARE_CRASH);
                    FxEntity.at(boss.level(), FxType.WARN_CIRCLE, center).size(FINAL_R).life(20).palette(3).spawn();
                    boss.playSound(SoundEvents.ILLUSIONER_PREPARE_MIRROR, 2.5f, 0.6f);
                }
                if (st < 20) b.place(center.add(0, 8, 0));
                if (st == 20) {
                    b.place(center.add(0, 0.1, 0));
                    boss.setAnimation(RosalyneEntity.ANIM_SWING_CRASH);
                    impact();
                }
                if (st >= 34) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void impact() {
        RosalyneEntity boss = b.boss;
        b.swingSound();
        boss.playSound(SoundEvents.GENERIC_EXPLODE, 3.0f, 0.7f);
        boss.playSound(SoundEvents.BELL_RESONATE, 2.0f, 1.4f);
        Fx.shake(boss.level(), center, 1.4f);
        Fx.rockRing(boss.level(), center, radius + 1, 0.9f);
        Fx.rockBurst(boss.level(), center, 3f, 20);
        FxEntity.at(boss.level(), FxType.RS_PETAL_BURST, center.add(0, 1.2, 0)).size(3f).spawn();
        for (int i = 0; i < 8; i++) {
            Vec3 d = Combat.dirFromYaw(i * 45f);
            FxEntity.at(boss.level(), FxType.RS_SUN_BLADE, b.groundAt(center.add(d.scale(2.5)))).size(1.3f).spawn();
        }
        AABB box = new AABB(center, center).inflate(FINAL_R, 3, FINAL_R);
        for (LivingEntity v : Combat.foes(boss, box, e -> Combat.horizontalDist(e.position(), center) <= FINAL_R)) {
            if (Combat.hit(boss, b.cfg, v, 1.5, 0)) Combat.push(v, new Vec3(0, 0.5, 0));
        }
    }
}
