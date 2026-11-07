package com.hp.myfplus.entity;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PetalBladeEntity extends OwnedEntity {
    public static final int ORBIT = 0, HOMING = 1, STRAIGHT = 2;
    public static final float ORBIT_R = 2.2f;
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(PetalBladeEntity.class, EntityDataSerializers.INT);
    public float slot;
    public float contactDamage;
    @Nullable
    public LivingEntity target;
    private int modeStart;

    public PetalBladeEntity(EntityType<? extends PetalBladeEntity> type, Level level) {
        super(type, level);
        life = 100000;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, ORBIT);
    }

    public int mode() {
        return entityData.get(MODE);
    }

    public void launch(int mode, @Nullable LivingEntity target, Vec3 velocity) {
        entityData.set(MODE, mode);
        this.target = target;
        setDeltaMovement(velocity);
        modeStart = tickCount;
        life = tickCount + (mode == HOMING ? 40 : 30);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1.0f, 1.6f);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        Mob boss = owner();
        if (boss == null || !boss.isAlive()) {
            discard();
            return;
        }
        if (mode() == ORBIT) {
            float a = (tickCount + slot * 40f / 8f) * Mth.TWO_PI / 40f;
            setPos(boss.getX() + Mth.cos(a) * ORBIT_R, boss.getY() + 1.1 + Mth.sin(tickCount * 0.15f + slot) * 0.2, boss.getZ() + Mth.sin(a) * ORBIT_R);
            for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(0.2))) {
                v.hurt(boss.damageSources().mobAttack(boss), contactDamage);
                shatter();
                return;
            }
            return;
        }
        Vec3 dm = getDeltaMovement();
        if (mode() == HOMING && target != null && target.isAlive()) {
            Vec3 want = target.getEyePosition().subtract(position());
            if (want.lengthSqr() > 1.0E-4) dm = turn(dm, want.normalize().scale(dm.length()), 6f);
        }
        setDeltaMovement(dm);
        setPos(getX() + dm.x, getY() + dm.y, getZ() + dm.z);
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().expandTowards(dm).inflate(0.2))) {
            v.hurt(damageSources().mobProjectile(this, boss), damage);
            shatter();
            return;
        }
        if (tickCount - modeStart > 2 && !level().noCollision(this, getBoundingBox())) shatter();
    }

    private static Vec3 turn(Vec3 cur, Vec3 want, float maxDeg) {
        double len = cur.length();
        if (len < 1.0E-4) return want;
        Vec3 a = cur.normalize(), b = want.normalize();
        double dot = Mth.clamp(a.dot(b), -1, 1);
        double ang = Math.acos(dot);
        double max = Math.toRadians(maxDeg);
        if (ang <= max) return want;
        double k = max / ang;
        return a.scale(1 - k).add(b.scale(k)).normalize().scale(len);
    }

    @Override
    protected void expire() {
        shatter();
    }

    public void shatter() {
        if (level().isClientSide || isRemoved()) return;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 0.8f, 1.4f);
        FxEntity.at(level(), FxType.RS_PETAL_BREAK, position()).spawn();
        discard();
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity src = source.getEntity();
        if (level().isClientSide || src == null || src == owner()) return false;
        shatter();
        return true;
    }
}
