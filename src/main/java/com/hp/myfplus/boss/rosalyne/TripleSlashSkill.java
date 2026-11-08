package com.hp.myfplus.boss.rosalyne;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import com.hp.myfplus.core.GroundProbe;
import com.mojang.logging.LogUtils;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TripleSlashSkill extends RosaSkill {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private final Set<Integer> hit = new HashSet<>();
    @Nullable
    private Vec3 spot;
    private boolean blocked;
    private float yaw;
    private Vec3 dashFrom = Vec3.ZERO;

    public TripleSlashSkill(RosalyneBrain b) {
        super(b, "triple", 60);
    }

    @Override
    protected void begin(LivingEntity target) {
        spot = null;
        blocked = false;
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 10;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 5;
    }

    private void lock(LivingEntity target) {
        yaw = Combat.yawTo(b.boss.position(), target.position());
        b.face(yaw);
        hit.clear();
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        if (target == null) return true;
        Vec3 here = boss.position();
        Vec3 ground = b.groundAt(here);
        switch (stage) {
            case 0 -> {
                if (st == 1 || st % 10 == 0) {
                    Vec3 s = b.meleeSpot(target, 2.5);
                    if (s != null) spot = s;
                }
                if (spot == null) {
                    LOGGER.debug("Rosalyne triple slash skipped: no standable spot near target at {}", target.position());
                    return true;
                }
                b.hover(spot.x, spot.y + 0.05, spot.z, 0.9);
                if (here.distanceTo(spot) < 1.0) {
                    b.place(spot);
                    next();
                }
                else if (st >= 30) {
                    LOGGER.debug("Rosalyne triple slash approach timed out at {}, blinking to {}", here, spot);
                    b.blink(spot);
                    next();
                }
            }
            case 1 -> {
                if (st == 1) {
                    lock(target);
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_DN);
                    FxEntity.at(boss.level(), FxType.RS_GLINT, here.add(Combat.dirFromYaw(yaw + 50).scale(0.9)).add(0, 1.6, 0)).spawn();
                    FxEntity.at(boss.level(), FxType.WARN_SECTOR, ground).yaw(yaw).size(4f).line(4f, 150f).life(10).palette(3).spawn();
                }
                b.face(yaw);
                b.hover(here.x, here.y, here.z, 0.1);
                if (st == 10) {
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_IN_UP);
                    Fx.arc(boss.level(), here, yaw, 4f, 150f, 1.1f, false);
                    b.swingSound();
                    b.hitSector(here, yaw, 4f, 75f, 0.6, 0.3, hit);
                    Fx.shake(boss.level(), here, 0.15f);
                }
                if (st >= 16) next();
            }
            case 2 -> {
                if (st == 1) {
                    lock(target);
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_IN_DN);
                    FxEntity.at(boss.level(), FxType.WARN_SECTOR, ground).yaw(yaw).size(4.5f).line(4.5f, 90f).life(10).palette(3).spawn();
                }
                b.face(yaw);
                b.hover(here.x, here.y, here.z, 0.1);
                if (st == 10) {
                    boss.setAnimation(RosalyneEntity.ANIM_ARM_OUT_UP);
                    Fx.arc(boss.level(), here.add(Combat.dirFromYaw(yaw).scale(1.2)), yaw, 2.4f, 150f, 1.2f, true);
                    b.swingSound();
                    Set<Integer> before = new HashSet<>(hit);
                    b.hitSector(here, yaw, 4.5f, 45f, 0.6, 0, hit);
                    for (LivingEntity v : Combat.foes(boss, boss.getBoundingBox().inflate(5))) if (hit.contains(v.getId()) && !before.contains(v.getId())) Combat.push(v, new Vec3(0, 0.4, 0));
                    Fx.shake(boss.level(), here, 0.2f);
                }
                if (st >= 16) next();
            }
            case 3 -> {
                if (st == 1) {
                    lock(target);
                    dashFrom = here;
                    blocked = false;
                    boss.setAnimation(RosalyneEntity.ANIM_PREPARE_DASH);
                    FxEntity.at(boss.level(), FxType.RS_GLINT, here.add(Combat.dirFromYaw(yaw).scale(1.2)).add(0, 1.3, 0)).spawn();
                    FxEntity.at(boss.level(), FxType.WARN_LINE, ground).yaw(yaw).line(6f, 1.5f).life(12).palette(3).spawn();
                }
                b.face(yaw);
                if (st < 12) b.hover(here.x, here.y, here.z, 0.1);
                if (st == 12) {
                    b.swingSound();
                    Fx.thrust(boss.level(), dashFrom, yaw, 6f, 1.4f, 1.2f);
                    Fx.rockLine(boss.level(), b.groundAt(dashFrom), yaw, 6f, 1.4f, 1.5f);
                    Fx.shake(boss.level(), dashFrom, 0.35f);
                }
                if (st >= 12 && st < 16 && !blocked) {
                    Vec3 d = Combat.dirFromYaw(yaw);
                    double x = here.x + d.x * 1.5, z = here.z + d.z * 1.5;
                    double gy = GroundProbe.top(boss.level(), x, z, here.y + 1.2, 1, 3);
                    Vec3 p = new Vec3(x, Double.isNaN(gy) ? here.y : gy, z);
                    if (p.y - here.y > 1.1 || !b.fits(p)) {
                        blocked = true;
                        LOGGER.debug("Rosalyne dash stopped by terrain at {}", p);
                    }
                    else {
                        b.place(p);
                        b.hitSegment(here, p, 1.0, 0.9, 0.6, hit);
                    }
                }
                if (st >= 22) {
                    if (b.act >= 2 && b.now() >= b.stance.lastUsed + (long) (b.stance.cooldown * 0.7 * b.cfg.cooldown())) b.director.force(b.stance);
                    return true;
                }
            }
            default -> {
                return true;
            }
        }
        return false;
    }
}
