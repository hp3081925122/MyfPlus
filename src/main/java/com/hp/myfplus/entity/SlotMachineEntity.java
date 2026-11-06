package com.hp.myfplus.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class SlotMachineEntity extends OwnedEntity {
    public static final int DICE = 0, CHIP = 1, HEART = 2, SKULL = 3, SEVEN = 4;
    public static final int SYMBOLS = 5;
    public static final int CELL = 12;
    public static final int SPEED = 4;
    public static final int[] AUTO_STOP = {20, 35, 50};
    public static final double LIFT = 2.6;
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer>[] STOP = new EntityDataAccessor[]{
            SynchedEntityData.defineId(SlotMachineEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(SlotMachineEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(SlotMachineEntity.class, EntityDataSerializers.INT)};

    public SlotMachineEntity(EntityType<? extends SlotMachineEntity> type, Level level) {
        super(type, level);
        life = 200;
    }

    @Override
    protected void defineSynchedData() {
        for (EntityDataAccessor<Integer> s : STOP) entityData.define(s, -1);
    }

    public int stopTick(int reel) {
        return entityData.get(STOP[reel]);
    }

    public static int offset(int reel) {
        return reel * 20;
    }

    public static int symbolAt(int reel, int tick) {
        return Math.floorMod(Math.round((tick * SPEED + offset(reel)) / (float) CELL), SYMBOLS);
    }

    public int symbol(int reel) {
        int s = stopTick(reel);
        return s < 0 ? -1 : symbolAt(reel, s);
    }

    public boolean allStopped() {
        return stopTick(2) >= 0 && stopTick(1) >= 0 && stopTick(0) >= 0;
    }

    public int lastStop() {
        return Math.max(stopTick(0), Math.max(stopTick(1), stopTick(2)));
    }

    private void stop(int reel) {
        if (stopTick(reel) >= 0) return;
        entityData.set(STOP[reel], tickCount);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_BASEDRUM.get(), SoundSource.HOSTILE, 1.2f, 0.8f + reel * 0.2f);
    }

    @Override
    public void tick() {
        super.tick();
        Mob boss = owner();
        if (boss != null) setPos(boss.getX(), boss.getY() + boss.getBbHeight() + LIFT - 1.6, boss.getZ());
        if (level().isClientSide || isRemoved()) return;
        if (boss == null || !boss.isAlive()) {
            discard();
            return;
        }
        for (int i = 0; i < 3; i++) if (tickCount >= AUTO_STOP[i]) stop(i);
        if (tickCount % 3 == 0 && !allStopped()) level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.HOSTILE, 0.5f, 1.8f);
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
        if (level().isClientSide || isRemoved()) return false;
        Entity src = source.getEntity();
        if (!(src instanceof LivingEntity) || src == owner()) return false;
        for (int i = 0; i < 3; i++) {
            if (stopTick(i) < 0) {
                stop(i);
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_HIT, SoundSource.HOSTILE, 0.8f, 1.6f);
                return true;
            }
        }
        return false;
    }
}
