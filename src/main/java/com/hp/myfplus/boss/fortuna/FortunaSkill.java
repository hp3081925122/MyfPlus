package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.world.entity.LivingEntity;

public abstract class FortunaSkill extends BossSkill {
    protected final FortunaBrain b;
    private final boolean anyPhase;

    protected FortunaSkill(FortunaBrain b, String name, int cooldown, boolean anyPhase) {
        super(name, cooldown);
        this.b = b;
        this.anyPhase = anyPhase;
    }

    @Override
    public final boolean canUse(LivingEntity target, double dist) {
        return b.boss.isAttackPhase() && usable(target, dist);
    }

    protected boolean usable(LivingEntity target, double dist) {
        return true;
    }

    @Override
    public final boolean tick(int t, @Nullable LivingEntity target) {
        if (!anyPhase && !b.boss.isAttackPhase()) return true;
        return step(t, target);
    }

    protected abstract boolean step(int t, @Nullable LivingEntity target);

    @Override
    public void stop() {
        b.boss.setAnimation(DameFortunaEntity.ANIM_IDLE);
    }
}
