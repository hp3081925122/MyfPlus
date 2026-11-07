package com.hp.myfplus.entity;

import java.util.HashMap;
import java.util.Map;

import com.hp.myfplus.core.Combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DuelRingEntity extends OwnedEntity {
    public static final int RISE = 20;
    public static final float HEIGHT = 2.2f;
    public float radius = 9f;
    private final Map<Integer, Integer> cooldown = new HashMap<>();

    public DuelRingEntity(EntityType<? extends DuelRingEntity> type, Level level) {
        super(type, level);
        life = Integer.MAX_VALUE;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        Mob boss = owner();
        if (boss == null || !boss.isAlive()) {
            discard();
            return;
        }
        if (tickCount < RISE) return;
        cooldown.replaceAll((k, v) -> v - 1);
        cooldown.values().removeIf(v -> v <= 0);
        AABB box = new AABB(getX() - radius - 2, getY() - 2, getZ() - radius - 2, getX() + radius + 2, getY() + 5, getZ() + radius + 2);
        for (LivingEntity v : Combat.foes(boss, box)) {
            double dx = getX() - v.getX(), dz = getZ() - v.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d < radius - 0.7 || d > radius + 1.2 || v.getY() > getY() + HEIGHT + 1) continue;
            Combat.setHorizontalVelocity(v, dx / d * 0.5, dz / d * 0.5);
            if (!cooldown.containsKey(v.getId())) {
                v.hurt(boss.damageSources().thorns(boss), damage);
                cooldown.put(v.getId(), 10);
            }
        }
    }

    public Vec3 center() {
        return position();
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
