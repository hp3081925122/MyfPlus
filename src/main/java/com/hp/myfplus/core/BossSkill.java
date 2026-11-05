package com.hp.myfplus.core;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;

public abstract class BossSkill {
    public final String name;
    public final int cooldown;
    public long lastUsed = -1000000L;

    protected BossSkill(String name, int cooldown) {
        this.name = name;
        this.cooldown = cooldown;
    }

    public boolean canUse(LivingEntity target, double dist) {
        return true;
    }

    public int weight(LivingEntity target, double dist) {
        return 1;
    }

    public double cooldownScale() {
        return 1.0;
    }

    public void start(LivingEntity target) {
    }

    public abstract boolean tick(int t, @Nullable LivingEntity target);

    public void stop() {
    }
}
