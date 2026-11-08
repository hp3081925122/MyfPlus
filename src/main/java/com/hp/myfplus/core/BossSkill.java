package com.hp.myfplus.core;

import net.minecraft.world.entity.LivingEntity;

public abstract class BossSkill {
    public final String id;
    public final int weight;
    public final boolean phase2Only;
    protected int t;

    protected BossSkill(String id, int weight, boolean phase2Only) {
        this.id = id;
        this.weight = weight;
        this.phase2Only = phase2Only;
    }

    public boolean usable(LivingEntity target) {
        return true;
    }

    public void start(LivingEntity target) {
    }

    public abstract boolean tick(LivingEntity target);

    public void stop() {
    }
}
