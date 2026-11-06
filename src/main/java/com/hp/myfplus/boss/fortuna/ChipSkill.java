package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.LivingEntity;

public class ChipSkill extends FortunaSkill {
    public ChipSkill(FortunaBrain b) {
        super(b, "chips", 50, false);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 24 && b.boss.hasLineOfSight(target);
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        if (target == null) return true;
        b.hoverHere(target, 1.5);
        if (t == 1) b.boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_WINDUP);
        if (t == 8) {
            b.boss.setAnimation(DameFortunaEntity.ANIM_CHIPS_LAUNCH);
            b.fanChips(target, 3 + b.act() * 2);
        }
        return t >= 18;
    }
}
