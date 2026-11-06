package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.LivingEntity;

public class DiceSkill extends FortunaSkill {
    public DiceSkill(FortunaBrain b) {
        super(b, "dice", 70, false);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 20;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 4;
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        if (target == null) return true;
        b.hoverHere(target, 1.5);
        if (t == 1) b.boss.setAnimation(DameFortunaEntity.ANIM_DICE_WINDUP);
        if (t == 15) {
            b.boss.setAnimation(DameFortunaEntity.ANIM_DICE_LAUNCH);
            b.throwDice(target, b.act() + 1, 1);
        }
        return t >= 26;
    }
}
