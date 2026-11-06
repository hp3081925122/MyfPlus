package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DiceEntity extends OwnedEntity {
    public static final double GRAVITY = 0.06;
    public static final int FUSE = 15;
    private static final EntityDataAccessor<Integer> REST = SynchedEntityData.defineId(DiceEntity.class, EntityDataSerializers.INT);
    public int value = 1;
    private int bounces = 3;

    public DiceEntity(EntityType<? extends DiceEntity> type, Level level) {
        super(type, level);
        life = 200;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(REST, -1);
    }

    public int restTick() {
        return entityData.get(REST);
    }

    public float radius() {
        return 0.5f + value * 0.5f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        int rest = restTick();
        if (rest >= 0) {
            if (tickCount - rest >= FUSE) explode();
            return;
        }
        Vec3 dm = getDeltaMovement().add(0, -GRAVITY, 0);
        Vec3 next = position().add(dm);
        double ground = GroundProbe.top(level(), next.x, next.z, getY() + 1, 2, 8);
        if (!Double.isNaN(ground) && next.y <= ground) {
            next = new Vec3(next.x, ground, next.z);
            bounces--;
            level().playSound(null, next.x, next.y, next.z, SoundEvents.BONE_BLOCK_HIT, SoundSource.HOSTILE, 1.2f, 1.4f);
            if (bounces <= 0) {
                dm = Vec3.ZERO;
                entityData.set(REST, tickCount);
                FxEntity.at(level(), FxType.WARN_CIRCLE, next).size(radius()).life(FUSE).palette(2).spawn();
            }
            else dm = new Vec3(dm.x * 0.6, -dm.y * 0.5, dm.z * 0.6);
        }
        setDeltaMovement(dm);
        setPos(next);
        if (tickCount > 120 && restTick() < 0) explode();
    }

    private void explode() {
        float r = radius();
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8f + value * 0.1f, 1.6f - value * 0.1f);
        FxEntity.at(level(), FxType.FT_BLAST, getX(), getY() + r * 0.5, getZ()).size(r * 2.2f).spawn();
        if (value >= 4) Fx.rockRing(level(), position(), r + 0.5f, 0.6f);
        Fx.shake(level(), position(), 0.1f * value);
        Mob boss = owner();
        if (boss != null) {
            for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(r, 2, r))) {
                if (Combat.horizontalDist(v.position(), position()) <= r) Combat.hit(boss, boss.damageSources().mobAttack(boss), damage, v, 0.5, position());
            }
        }
        discard();
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(value);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        value = buf.readVarInt();
    }
}
