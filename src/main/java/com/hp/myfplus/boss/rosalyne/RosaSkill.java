package com.hp.myfplus.boss.rosalyne;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;

import lykrast.meetyourfight.entity.RosalyneEntity;
import net.minecraft.world.entity.LivingEntity;

public abstract class RosaSkill extends BossSkill {
    protected final RosalyneBrain b;
    protected int stage;
    protected int st;

    protected RosaSkill(RosalyneBrain b, String name, int cooldown) {
        super(name, cooldown);
        this.b = b;
    }

    @Override
    public final boolean canUse(LivingEntity target, double dist) {
        return b.fighting() && !b.invulnerable && usable(target, dist);
    }

    protected boolean usable(LivingEntity target, double dist) {
        return true;
    }

    @Override
    public void start(LivingEntity target) {
        stage = 0;
        st = 0;
        begin(target);
    }

    protected void begin(LivingEntity target) {
    }

    protected void next() {
        stage++;
        st = 0;
    }

    @Override
    public final boolean tick(int t, @Nullable LivingEntity target) {
        st++;
        return step(t, target);
    }

    protected abstract boolean step(int t, @Nullable LivingEntity target);

    @Override
    public void stop() {
        b.boss.setAnimation(RosalyneEntity.ANIM_NEUTRAL);
    }
}
