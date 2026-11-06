package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BellDomeEntity extends OwnedEntity {
    public static final int DESCEND = 15;
    public static final float HEIGHT = 6f;
    public float radius = 7f;

    public BellDomeEntity(EntityType<? extends BellDomeEntity> type, Level level) {
        super(type, level);
        life = 400;
    }

    public boolean landed() {
        return tickCount >= DESCEND;
    }

    public float dropOffset(float age) {
        float k = Math.min(1f, age / DESCEND);
        return 14f * (1 - k) * (1 - k);
    }

    public int insideCount() {
        Mob boss = owner();
        if (boss == null) return 0;
        return Combat.foes(boss, getBoundingBox().inflate(radius, HEIGHT, radius), v -> Combat.horizontalDist(v.position(), position()) < radius).size();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved() || !landed()) return;
        Mob boss = owner();
        if (boss == null) {
            discard();
            return;
        }
        AABB box = getBoundingBox().inflate(radius + 2, HEIGHT, radius + 2);
        for (LivingEntity v : Combat.foes(boss, box)) {
            if (v.getY() > getY() + HEIGHT || v.getY() < getY() - 2) continue;
            double dx = v.getX() - getX(), dz = v.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01) continue;
            double nx = dx / dist, nz = dz / dist;
            Vec3 dm = v.getDeltaMovement();
            double radial = dm.x * nx + dm.z * nz;
            if (dist < radius && dist > radius - 0.8 && radial > -0.2) {
                v.setDeltaMovement(dm.x - nx * (radial + 0.25), dm.y, dm.z - nz * (radial + 0.25));
                v.hurtMarked = true;
            }
            else if (dist >= radius && dist < radius + 0.8 && radial < 0.2) {
                v.setDeltaMovement(dm.x + nx * (0.25 - radial), dm.y, dm.z + nz * (0.25 - radial));
                v.hurtMarked = true;
            }
        }
    }

    public float fade(float partial) {
        return Math.min(1f, Math.min(age(partial) / 4f, (life - age(partial)) / 6f));
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
