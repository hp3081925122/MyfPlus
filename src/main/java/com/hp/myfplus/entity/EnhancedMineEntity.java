package com.hp.myfplus.entity;

import javax.annotation.Nullable;

import com.hp.myfplus.core.EnhancedBoss;
import com.hp.myfplus.core.Shots;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnhancedMineEntity extends SwampMineEntity {
    @Nullable
    private LivingEntity owner;
    private float damage;
    private float radius = 2.5f;

    public EnhancedMineEntity(EntityType<? extends EnhancedMineEntity> type, Level level) {
        super(type, level);
    }

    public static EnhancedMineEntity create(LivingEntity owner, Vec3 pos, Vec3 motion, float damage, float radius, int fuse) {
        EnhancedMineEntity m = new EnhancedMineEntity(MyfEntities.MINE.get(), owner.level());
        m.setPos(pos.x, pos.y, pos.z);
        m.xo = pos.x;
        m.yo = pos.y;
        m.zo = pos.z;
        m.setDeltaMovement(motion);
        m.setFuse(fuse);
        m.owner = owner;
        m.damage = damage;
        m.radius = radius;
        return m;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void explode() {
        Vec3 c = position();
        Shots.particles(level(), ParticleTypes.EXPLOSION, c.add(0, 0.3, 0), 2, radius * 0.3, 0);
        Shots.particles(level(), ParticleTypes.SPLASH, c.add(0, 0.3, 0), 30, radius * 0.4, 0.2);
        Shots.particles(level(), ParticleTypes.BUBBLE_POP, c.add(0, 0.5, 0), 16, radius * 0.4, 0.05);
        level().playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.1f + random.nextFloat() * 0.2f);
        if (owner == null || damage <= 0) return;
        DamageSource src = damageSources().explosion(this, owner);
        for (LivingEntity v : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, radius * 0.6, radius))) {
            if (v.distanceToSqr(c) > radius * radius + 1 || !Shots.foe(owner, v) || (owner instanceof EnhancedBoss && v instanceof EnhancedBoss)) continue;
            v.hurt(src, damage);
            Vec3 d = Shots.flat(v.position().subtract(c));
            v.knockback(0.4, -d.x, -d.z);
        }
    }
}
