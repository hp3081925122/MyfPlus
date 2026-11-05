package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class GasCloudEntity extends OwnedEntity {
    public float radius = 2f;

    public GasCloudEntity(EntityType<? extends GasCloudEntity> type, Level level) {
        super(type, level);
        life = 80;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || tickCount % 20 != 10) return;
        Mob boss = owner();
        if (boss == null) return;
        for (LivingEntity e : Combat.foes(boss, getBoundingBox().inflate(radius, 2.5, radius))) {
            double dx = e.getX() - getX();
            double dz = e.getZ() - getZ();
            if (dx * dx + dz * dz > radius * radius || e.getY() < getY() - 0.75 || e.getY() > getY() + 2.5) continue;
            e.hurt(damageSources().indirectMagic(this, boss), damage);
        }
    }

    public float fade(float partial) {
        float a = age(partial);
        return Math.min(1f, Math.min(a / 4f, (life - a) / 12f));
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeFloat(radius);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        radius = buf.readFloat();
    }
}
