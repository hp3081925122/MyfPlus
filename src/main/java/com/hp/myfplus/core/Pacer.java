package com.hp.myfplus.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/** 共用本体攻击冷却：加快冷却流逝，并在冷却归零时按几率把这一轮攻击换成新技能。 */
public final class Pacer {
    private double accel;
    private boolean rolled;

    public static double extra(double factor, double cfg) {
        return Math.max(0, 1.0 / (factor * cfg) - 1.0);
    }

    public void skip() {
        rolled = true;
    }

    public int step(Mob boss, SkillRunner runner, int cd, double extra, boolean canAttack, double chance, boolean phase2) {
        if (runner.busy()) return Math.max(cd, 2);
        if (cd > 2) {
            accel += extra;
            while (accel >= 1 && cd > 1) {
                cd--;
                accel -= 1;
            }
        }
        if (cd > 0) {
            rolled = false;
            return cd;
        }
        if (rolled || !canAttack) return cd;
        rolled = true;
        LivingEntity t = boss.getTarget();
        if (t != null && t.isAlive() && boss.getRandom().nextDouble() < chance && runner.pick(t, phase2)) return 2;
        return cd;
    }
}
