package com.hp.myfplus.boss.rosalyne;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.ThornEntity;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CorridorSkill extends RosaSkill {
    private static final int WINDUP = 15;
    private static final int LENGTH = 14;
    private static final double HALF_GAP = 1.5;
    private static final int GROWN = WINDUP + LENGTH * 2 + ThornEntity.GROW;
    private static final int DASH_AT = GROWN + 10;
    private static final int DASH_TICKS = 10;
    private final Set<Integer> hit = new HashSet<>();
    private Vec3 start = Vec3.ZERO;
    private Vec3 dir = Vec3.ZERO;
    private float yaw;

    public CorridorSkill(RosalyneBrain b) {
        super(b, "corridor", 220);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist >= 5 && dist <= 16;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    protected void begin(LivingEntity target) {
        RosalyneEntity boss = b.boss;
        hit.clear();
        yaw = Combat.yawTo(boss.position(), target.position());
        dir = Combat.dirFromYaw(yaw);
        start = b.groundAt(boss.position());
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        List<ThornEntity> group = new ArrayList<>();
        for (int s = -1; s <= 1; s += 2) {
            for (int i = 0; i < LENGTH; i++) {
                Vec3 p = b.groundAt(start.add(dir.scale(1 + i)).add(side.scale(s * HALF_GAP)));
                ThornEntity th = new ThornEntity(MyfEntities.THORN.get(), boss.level());
                th.setPos(p.x, p.y, p.z);
                th.setOwner(boss);
                th.delay = WINDUP + i * 2;
                th.variant = (i + (s > 0 ? 1 : 0)) & 1;
                th.life = th.delay + 200;
                th.damage = Combat.damage(boss, b.cfg, 0.25);
                boss.level().addFreshEntity(th);
                group.add(th);
                FxEntity.at(boss.level(), FxType.RS_SPROUT, p).yaw(yaw).life(th.delay + 2).spawn();
            }
        }
        b.thornGroups.add(group);
        while (b.thornGroups.size() > 2) for (ThornEntity th : b.thornGroups.remove(0)) th.discard();
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        RosalyneEntity boss = b.boss;
        b.face(yaw);
        if (t == 1) {
            boss.setAnimation(RosalyneEntity.ANIM_PREPARE_CRASH);
            boss.playSound(SoundEvents.ROOTED_DIRT_BREAK, 2.0f, 0.6f);
        }
        if (t == WINDUP) {
            boss.setAnimation(RosalyneEntity.ANIM_SWING_CRASH);
            Fx.rockBurst(boss.level(), start, 1.2f, 8);
            Fx.shake(boss.level(), start, 0.4f);
        }
        if (t < DASH_AT) b.place(start.add(0, 0.1, 0));
        if (t == DASH_AT - 12) {
            boss.setAnimation(RosalyneEntity.ANIM_PREPARE_DASH);
            FxEntity.at(boss.level(), FxType.WARN_LINE, start).yaw(yaw).line(LENGTH + 1, 1.4f).life(12).palette(3).spawn();
            FxEntity.at(boss.level(), FxType.RS_GLINT, start.add(dir.scale(1.2)).add(0, 1.3, 0)).spawn();
        }
        if (t == DASH_AT) {
            b.swingSound();
            Fx.thrust(boss.level(), start, yaw, LENGTH + 1, 1.2f, 1.2f);
            Fx.rockLine(boss.level(), start, yaw, LENGTH + 1, 1.2f, (LENGTH + 1f) / DASH_TICKS);
            Fx.shake(boss.level(), start, 0.4f);
        }
        if (t >= DASH_AT && t < DASH_AT + DASH_TICKS) {
            Vec3 from = start.add(dir.scale((LENGTH + 1.0) * (t - DASH_AT) / DASH_TICKS));
            Vec3 to = start.add(dir.scale((LENGTH + 1.0) * (t - DASH_AT + 1) / DASH_TICKS));
            b.place(to.add(0, 0.1, 0));
            b.hitSegment(from, to, 0.9, 0.9, 0.6, hit);
        }
        return t >= DASH_AT + DASH_TICKS + 8;
    }
}
