package com.hp.myfplus.boss.rosalyne;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TripleSlashSkill extends RosaSkill {
    private final Set<Integer> hit = new HashSet<>();
    private float yaw;
    private Vec3 dashFrom = Vec3.ZERO;

    public TripleSlashSkill(RosalyneBrain b) {
        super(b, "triple", 60);
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
                Vec3 f = target.position().subtract(here);
                Vec3 to = target.position().subtract(new Vec3(f.x, 0, f.z).normalize().scale(2.5));
                b.hover(to.x, target.getY() + 0.2, to.z, 0.9);
                if (boss.distanceTo(target) < 3.6 || st >= 30) next();
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
                if (st >= 12 && st < 16) {
                    Vec3 p = dashFrom.add(Combat.dirFromYaw(yaw).scale(1.5 * (st - 11)));
                    b.place(p);
                    b.hitSegment(dashFrom, p, 1.0, 0.9, 0.6, hit);
                }
                if (st >= 22) {
                    if (b.act >= 3 && b.now() >= b.stance.lastUsed + (long) (b.stance.cooldown * 0.7 * b.cfg.cooldown())) b.director.force(b.stance);
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
