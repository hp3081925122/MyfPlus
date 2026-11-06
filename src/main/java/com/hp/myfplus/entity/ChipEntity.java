package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ChipEntity extends OwnedEntity {
    public int bounces = 1;
    public float aoe;

    public ChipEntity(EntityType<? extends ChipEntity> type, Level level) {
        super(type, level);
        life = 60;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        Mob boss = owner();
        if (boss == null) {
            discard();
            return;
        }
        Vec3 pos = position();
        Vec3 dm = getDeltaMovement();
        Vec3 next = pos.add(dm);
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().expandTowards(dm).inflate(0.25))) {
            v.hurt(damageSources().mobProjectile(this, boss), damage);
            pop(boss);
            return;
        }
        BlockHitResult hit = level().clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            if (bounces > 0) {
                bounces--;
                Direction.Axis ax = hit.getDirection().getAxis();
                dm = new Vec3(ax == Direction.Axis.X ? -dm.x : dm.x, ax == Direction.Axis.Y ? -dm.y : dm.y, ax == Direction.Axis.Z ? -dm.z : dm.z);
                setDeltaMovement(dm);
                setPos(hit.getLocation().add(dm.normalize().scale(0.1)));
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 0.6f, 1.8f);
                return;
            }
            setPos(hit.getLocation());
            pop(boss);
            return;
        }
        setPos(next);
    }

    private void pop(Mob boss) {
        if (aoe > 0) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.6f, 1.7f);
            FxEntity.at(level(), FxType.FT_BLAST, getX(), getY() + 0.4, getZ()).size(aoe * 2f).spawn();
            for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(aoe, 1.5, aoe))) {
                if (Combat.horizontalDist(v.position(), position()) <= aoe) v.hurt(damageSources().mobProjectile(this, boss), damage);
            }
        }
        else level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 0.5f, 1.8f);
        discard();
    }
}
