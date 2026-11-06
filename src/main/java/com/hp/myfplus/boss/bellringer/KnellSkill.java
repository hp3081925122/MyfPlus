package com.hp.myfplus.boss.bellringer;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class KnellSkill extends BeatSkill {
    public KnellSkill(BellringerBrain b) {
        super(b, "knell", 220);
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 28;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 2;
    }

    @Override
    protected boolean onBeat(int beat, @Nullable LivingEntity target) {
        if (target == null) return true;
        switch (beat) {
            case 1 -> b.countdown(target, 3);
            case 2 -> b.countdown(target, 2);
            case 3 -> b.countdown(target, 1);
            case 4 -> drop(target);
            case 5 -> {
                if (!b.phase2) return true;
                b.countdown(target, 2);
            }
            case 6 -> b.countdown(target, 1);
            case 7 -> drop(target);
            default -> {
                return true;
            }
        }
        return false;
    }

    private void drop(LivingEntity target) {
        b.ding();
        b.dropBell(b.groundAt(target.position()), true);
    }

    @Override
    protected boolean update(int t, @Nullable LivingEntity target) {
        if (target != null) {
            Vec3 tp = target.position();
            Vec3 away = b.boss.position().subtract(tp);
            Vec3 h = new Vec3(away.x, 0, away.z);
            Vec3 p = h.lengthSqr() > 1 ? tp.add(h.normalize().scale(8)) : tp.add(8, 0, 0);
            b.hover(new Vec3(p.x, tp.y + 3, p.z), 0.5);
        }
        return false;
    }
}
