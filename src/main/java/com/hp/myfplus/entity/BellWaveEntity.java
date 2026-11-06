package com.hp.myfplus.entity;

import java.util.HashSet;
import java.util.Set;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BellWaveEntity extends OwnedEntity {
    public static final float GAP_HALF = 15f;
    public float startRadius;
    public float speed = 0.35f;
    public float endRadius = 18f;
    public boolean tall;
    public float[] gaps = new float[0];
    public double knockback = 0.4;
    private final Set<Integer> hit = new HashSet<>();

    public BellWaveEntity(EntityType<? extends BellWaveEntity> type, Level level) {
        super(type, level);
    }

    public void setup(float start, float end, float speed, boolean tall, float[] gaps) {
        this.startRadius = start;
        this.endRadius = end;
        this.speed = speed;
        this.tall = tall;
        this.gaps = gaps;
        this.life = Math.max(2, (int) Math.ceil(Math.abs(end - start) / Math.abs(speed)));
    }

    public float radius(float age) {
        return startRadius + speed * age;
    }

    public float height() {
        return tall ? 2.5f : 0.9f;
    }

    public boolean inGap(float yawDeg) {
        for (float g : gaps) if (Math.abs(Mth.wrapDegrees(yawDeg - g)) <= GAP_HALF) return true;
        return false;
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
        float r = radius(tickCount);
        float reach = Math.max(r, startRadius) + 1.5f;
        AABB box = new AABB(getX() - reach, getY() - 2, getZ() - reach, getX() + reach, getY() + 4, getZ() + reach);
        for (LivingEntity v : Combat.foes(boss, box)) {
            if (hit.contains(v.getId())) continue;
            double dx = v.getX() - getX(), dz = v.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dist - r) > 0.6 + Math.abs(speed) * 0.5) continue;
            float yaw = Combat.yawTo(position(), v.position());
            if (inGap(yaw)) continue;
            double ground = GroundProbe.under(level(), v.getX(), v.getZ(), v.getY() + 0.5);
            if (v.getY() - ground > height() || v.getY() < getY() - 2) continue;
            hit.add(v.getId());
            Vec3 from = speed >= 0 ? position() : position().add(dx / Math.max(dist, 0.01) * 2 * dist, 0, dz / Math.max(dist, 0.01) * 2 * dist);
            Combat.hit(boss, boss.damageSources().mobAttack(boss), damage, v, knockback, from);
        }
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeFloat(startRadius);
        buf.writeFloat(endRadius);
        buf.writeFloat(speed);
        buf.writeBoolean(tall);
        buf.writeVarInt(gaps.length);
        for (float g : gaps) buf.writeFloat(g);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        startRadius = buf.readFloat();
        endRadius = buf.readFloat();
        speed = buf.readFloat();
        tall = buf.readBoolean();
        gaps = new float[buf.readVarInt()];
        for (int i = 0; i < gaps.length; i++) gaps[i] = buf.readFloat();
    }
}
