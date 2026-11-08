package com.hp.myfplus.core;

import javax.annotation.Nullable;

import com.hp.myfplus.config.MyfConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;

public abstract class BossBrain<T extends Mob> {
    public final T boss;
    public final MyfConfig.BossCfg cfg;
    public final SkillGoal director;

    protected BossBrain(T boss, MyfConfig.BossCfg cfg) {
        this.boss = boss;
        this.cfg = cfg;
        this.director = new SkillGoal(this);
    }

    public long now() {
        return boss.level().getGameTime();
    }

    @Nullable
    public LivingEntity target() {
        LivingEntity t = boss.getTarget();
        return t != null && t.isAlive() ? t : null;
    }

    public void setSkill(int id) {
        ((MyfBossHost) boss).myfplus$setSkill(id);
    }

    public void setPhase(int phase) {
        ((MyfBossHost) boss).myfplus$setPhase(phase);
    }

    public abstract int globalGap();

    public boolean firstPhase() {
        return false;
    }

    protected void enterSecondPhase() {
    }

    /** 一阶段受到致命伤时不死，回满血并转入二阶段；/kill、虚空等无视无敌的伤害照常致死。 */
    public final boolean interceptLethal(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !firstPhase()) return false;
        boss.setHealth(boss.getMaxHealth());
        director.interrupt();
        enterSecondPhase();
        return true;
    }

    public void serverTick() {
    }

    public boolean blocksAttack(DamageSource source) {
        return false;
    }

    public boolean deflects(Projectile projectile) {
        return false;
    }

    public float onHurt(DamageSource source, float amount) {
        return amount;
    }

    public float onDealDamage(LivingEntity victim, float amount) {
        return amount;
    }

    public void hover(double x, double y, double z, double speed) {
        boss.getMoveControl().setWantedPosition(x, y, z, speed);
    }

    public void save(CompoundTag tag) {
    }

    public void load(CompoundTag tag) {
    }

    public float hpRatio() {
        return boss.getHealth() / boss.getMaxHealth();
    }
}
