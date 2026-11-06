package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.LivingEntity;

public class StunSkill extends FortunaSkill {
    public int duration = 40;
    @Nullable
    private FxEntity stars;

    public StunSkill(FortunaBrain b) {
        super(b, "stun", -1, true);
    }

    @Override
    public void start(LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        boss.setAnimation(DameFortunaEntity.ANIM_CARD_WAIT);
        stars = FxEntity.at(boss.level(), FxType.FT_STARS, boss.position()).follow(boss, 0, boss.getBbHeight() + 0.4, 0).life(duration).spawn();
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        b.hover(boss.getX(), boss.getY() - 0.05, boss.getZ(), 0.2);
        return t >= duration;
    }

    @Override
    public void stop() {
        super.stop();
        if (stars != null) stars.discard();
        stars = null;
    }
}
