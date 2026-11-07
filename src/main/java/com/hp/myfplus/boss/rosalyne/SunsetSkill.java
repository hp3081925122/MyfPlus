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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SunsetSkill extends RosaSkill {
    private static final int RISE = 10;
    private static final int WINDUP = 20;
    private static final float SPEED = 2f;
    private final Set<Integer> hit = new HashSet<>();
    private Vec3 start = Vec3.ZERO;
    private Vec3 crossStart = Vec3.ZERO;
    private float yaw;
    private float crossYaw;
    private float length;
    private boolean cross;
    private Vec3 hoverAt = Vec3.ZERO;

    public SunsetSkill(RosalyneBrain b) {
        super(b, "sunset", 200);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist >= 6 && dist <= 20;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    protected void begin(LivingEntity target) {
        hit.clear();
        cross = b.act >= 3;
        hoverAt = b.boss.position().add(0, 6, 0);
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        switch (stage) {
            case 0 -> {
                b.hover(hoverAt.x, hoverAt.y, hoverAt.z, 1.2);
                if (st == 1) {
                    boss.setAnimation(RosalyneEntity.ANIM_PREPARE_CRASH);
                    boss.playSound(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, 2.0f, 0.7f);
                }
                if (st >= RISE) {
                    if (target == null) return true;
                    Vec3 ground = b.groundAt(boss.position());
                    yaw = Combat.yawTo(ground, target.position());
                    length = (float) Combat.horizontalDist(ground, target.position()) + 6;
                    start = ground;
                    Vec3 dir = Combat.dirFromYaw(yaw);
                    FxEntity.at(boss.level(), FxType.RS_SUNSET, boss.position().subtract(dir.scale(1.5)).add(0, 1.2, 0)).size(1.4f).life(WINDUP + 6).spawn();
                    FxEntity.at(boss.level(), FxType.WARN_LINE, start).yaw(yaw).line(length, 2f).life(WINDUP).palette(3).spawn();
                    if (cross) {
                        crossYaw = yaw + 90;
                        Vec3 tc = b.groundAt(target.position());
                        crossStart = tc.subtract(Combat.dirFromYaw(crossYaw).scale(length / 2));
                        FxEntity.at(boss.level(), FxType.WARN_LINE, crossStart).yaw(crossYaw).line(length, 2f).life(WINDUP).palette(3).spawn();
                    }
                    next();
                }
            }
            case 1 -> {
                b.face(yaw);
                b.hover(boss.getX(), boss.getY(), boss.getZ(), 0.05);
                if (st >= WINDUP) {
                    b.place(start.add(0, 0.1, 0));
                    boss.setAnimation(RosalyneEntity.ANIM_SWING_CRASH);
                    boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 1.2f);
                    b.swingSound();
                    Fx.rockBurst(boss.level(), start, 1.5f, 10);
                    Fx.rockLine(boss.level(), start, yaw, length, 2f, SPEED);
                    if (cross) Fx.rockLine(boss.level(), crossStart, crossYaw, length, 2f, SPEED);
                    Fx.shake(boss.level(), start, 0.8f);
                    next();
                }
            }
            case 2 -> {
                b.hover(boss.getX(), boss.getY(), boss.getZ(), 0.05);
                int n = (int) Math.ceil(length / SPEED);
                if (st <= n) {
                    blade(start, yaw);
                    if (cross) blade(crossStart, crossYaw);
                }
                if (st >= n + 8) return true;
            }
            default -> {
                return true;
            }
        }
        return false;
    }

    private void blade(Vec3 origin, float dirYaw) {
        Vec3 dir = Combat.dirFromYaw(dirYaw);
        Vec3 a = origin.add(dir.scale(SPEED * (st - 1)));
        Vec3 c = origin.add(dir.scale(SPEED * st));
        FxEntity.at(b.boss.level(), FxType.RS_SUN_BLADE, b.groundAt(c)).spawn();
        b.hitSegment(a, c, 1.2, 1.3, 0.5, hit);
    }
}
