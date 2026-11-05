package com.hp.myfplus.entity;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEntities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SwampBubbleEntity extends OwnedEntity {
    @Nullable
    public LivingEntity target;
    public float cloudDamage;
    private boolean popped;

    public SwampBubbleEntity(EntityType<? extends SwampBubbleEntity> type, Level level) {
        super(type, level);
        life = 160;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 dm = getDeltaMovement();
        if (level().isClientSide) {
            setPos(getX() + dm.x, getY() + dm.y, getZ() + dm.z);
            return;
        }
        if (isRemoved()) return;
        Mob boss = owner();
        if (boss == null) {
            discard();
            return;
        }
        if (target != null && target.isAlive()) {
            Vec3 goal = new Vec3(target.getX(), target.getY() + 1.2, target.getZ());
            Vec3 want = goal.subtract(position());
            want = want.lengthSqr() > 1.0E-4 ? want.normalize().scale(0.06) : Vec3.ZERO;
            dm = dm.add(want.subtract(dm).scale(0.25));
        }
        else dm = dm.scale(0.9);
        setDeltaMovement(dm);
        setPos(getX() + dm.x, getY() + dm.y, getZ() + dm.z);
        if (!Combat.foes(boss, getBoundingBox().inflate(0.15)).isEmpty()) pop();
    }

    @Override
    protected void expire() {
        pop();
    }

    public void pop() {
        if (popped || level().isClientSide) return;
        popped = true;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.HOSTILE, 1.5f, 0.7f);
        FxEntity.at(level(), FxType.SJ_BUBBLE_POP, getX(), getY() - 0.5, getZ()).spawn();
        Mob boss = owner();
        if (boss != null) {
            GasCloudEntity cloud = new GasCloudEntity(MyfEntities.GAS_CLOUD.get(), level());
            double gy = GroundProbe.topOr(level(), getX(), getZ(), getY(), getY() - 1);
            cloud.setPos(getX(), gy, getZ());
            cloud.setOwner(boss);
            cloud.damage = cloudDamage;
            level().addFreshEntity(cloud);
        }
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
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide) pop();
        return true;
    }
}
