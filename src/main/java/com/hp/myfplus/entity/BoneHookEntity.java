package com.hp.myfplus.entity;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoneHookEntity extends OwnedEntity {
    private static final EntityDataAccessor<Integer> HOOKED = SynchedEntityData.defineId(BoneHookEntity.class, EntityDataSerializers.INT);
    public double maxDist = 18;
    private double traveled;
    public boolean missed;
    public boolean blocked;
    public int jumps;
    @Nullable
    public LivingEntity hooked;

    public BoneHookEntity(EntityType<? extends BoneHookEntity> type, Level level) {
        super(type, level);
        life = 120;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(HOOKED, -1);
    }

    @Nullable
    public Entity hookedClient() {
        int id = entityData.get(HOOKED);
        return id >= 0 ? level().getEntity(id) : null;
    }

    public boolean done() {
        return missed || blocked || isRemoved();
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;
        if (level().isClientSide) {
            Entity h = hookedClient();
            if (h != null) setPos(h.getX(), h.getY() + h.getBbHeight() * 0.6, h.getZ());
            else setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
            return;
        }
        Mob boss = owner();
        if (boss == null || !boss.isAlive()) {
            discard();
            return;
        }
        if (hooked != null) {
            if (!hooked.isAlive()) {
                missed = true;
                return;
            }
            setPos(hooked.getX(), hooked.getY() + hooked.getBbHeight() * 0.6, hooked.getZ());
            return;
        }
        if (missed || blocked) return;
        Vec3 pos = position();
        Vec3 dm = getDeltaMovement();
        Vec3 next = pos.add(dm);
        HitResult block = level().clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (block.getType() != HitResult.Type.MISS) next = block.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level(), this, pos, next, getBoundingBox().expandTowards(dm).inflate(0.6),
                e -> e instanceof LivingEntity le && Combat.isFoe(boss, le));
        if (hit != null && hit.getEntity() instanceof LivingEntity victim) {
            boolean ok = victim.hurt(damageSources().mobProjectile(this, boss), damage);
            if (!ok && victim.isBlocking()) {
                blocked = true;
                setDeltaMovement(Vec3.ZERO);
                return;
            }
            hooked = victim;
            entityData.set(HOOKED, victim.getId());
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        setPos(next);
        traveled += dm.length();
        if (block.getType() != HitResult.Type.MISS || traveled >= maxDist) {
            missed = true;
            setDeltaMovement(Vec3.ZERO);
        }
    }
}
