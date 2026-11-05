package com.hp.myfplus.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class MudPitEntity extends OwnedEntity {
    public float radius = 2.5f;

    public MudPitEntity(EntityType<? extends MudPitEntity> type, Level level) {
        super(type, level);
    }

    public boolean contains(LivingEntity e) {
        double dx = e.getX() - getX();
        double dz = e.getZ() - getZ();
        return dx * dx + dz * dz <= radius * radius && e.getY() >= getY() - 0.75 && e.getY() <= getY() + 1.5;
    }

    public float fade(float partial) {
        float a = age(partial);
        return Math.min(1f, Math.min(a / 6f, (life - a) / 20f));
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
