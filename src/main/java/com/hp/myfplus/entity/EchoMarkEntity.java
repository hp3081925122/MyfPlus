package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EchoMarkEntity extends OwnedEntity {
    public static final int BEATS = 4;
    public int interval = 20;
    public float radius = 1.75f;

    public EchoMarkEntity(EntityType<? extends EchoMarkEntity> type, Level level) {
        super(type, level);
    }

    public void setup(int interval) {
        this.interval = interval;
        this.life = interval * BEATS;
    }

    public int stage(float age) {
        return Math.min(BEATS, (int) (age / Math.max(1, interval)));
    }

    @Override
    protected void expire() {
        Mob boss = owner();
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 1.6f, 1.6f);
        FxEntity.at(level(), FxType.BR_PILLAR, position()).spawn();
        FxEntity.at(level(), FxType.BR_IMPACT, position()).size(radius * 2.2f).spawn();
        Fx.shake(level(), position(), 0.3f);
        if (boss != null) {
            for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(radius, 6, radius))) {
                if (Combat.horizontalDist(v.position(), position()) > radius || v.getY() < getY() - 1 || v.getY() > getY() + 6) continue;
                if (v.hurt(boss.damageSources().mobAttack(boss), damage)) Combat.push(v, new Vec3(0, 0.3, 0));
            }
        }
        discard();
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(interval);
        buf.writeFloat(radius);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        interval = buf.readVarInt();
        radius = buf.readFloat();
    }
}
