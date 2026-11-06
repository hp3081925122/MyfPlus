package com.hp.myfplus.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class AllInEntity extends OwnedEntity {
    public static final int LAYERS = 12;
    private static final EntityDataAccessor<Float> PROGRESS = SynchedEntityData.defineId(AllInEntity.class, EntityDataSerializers.FLOAT);

    public AllInEntity(EntityType<? extends AllInEntity> type, Level level) {
        super(type, level);
        life = 260;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(PROGRESS, 0f);
    }

    public float progress() {
        return entityData.get(PROGRESS);
    }

    public void setProgress(float p) {
        entityData.set(PROGRESS, Math.max(0f, Math.min(1f, p)));
    }

    @Override
    public void tick() {
        super.tick();
        Mob boss = owner();
        if (boss != null) setPos(boss.getX(), boss.getY() + boss.getBbHeight() + 0.6, boss.getZ());
        else if (!level().isClientSide) discard();
    }
}
