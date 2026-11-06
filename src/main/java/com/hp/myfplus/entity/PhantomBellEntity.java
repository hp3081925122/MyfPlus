package com.hp.myfplus.entity;

import java.util.function.Consumer;

import javax.annotation.Nullable;

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

public class PhantomBellEntity extends OwnedEntity {
    public static final int FALL = 8;
    public static final float DROP = 12f;
    public static final float RADIUS = 2.5f;
    public int echoDelay = 40;
    @Nullable
    public Consumer<PhantomBellEntity> onEcho;

    public PhantomBellEntity(EntityType<? extends PhantomBellEntity> type, Level level) {
        super(type, level);
        life = FALL + 60;
    }

    public boolean landed() {
        return tickCount >= FALL;
    }

    public float fallOffset(float age) {
        float k = Math.min(1f, age / FALL);
        return DROP * (1 - k * k);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        if (tickCount == FALL) impact();
        if (tickCount == FALL + echoDelay && onEcho != null) onEcho.accept(this);
    }

    private void impact() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 3.0f, 0.5f);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.5f, 0.6f);
        FxEntity.at(level(), FxType.BR_IMPACT, position()).size(RADIUS * 3f).spawn();
        Fx.rockRing(level(), position(), RADIUS + 1.5f, 0.7f);
        Fx.rockBurst(level(), position(), 1.5f, 12);
        Fx.shake(level(), position(), 0.9f);
        Mob boss = owner();
        if (boss == null) return;
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(RADIUS, 1, RADIUS))) {
            if (Combat.horizontalDist(v.position(), position()) <= RADIUS) Combat.hit(boss, boss.damageSources().mobAttack(boss), damage, v, 0.7, position());
        }
    }

    public float fade(float partial) {
        return Math.min(1f, (life - age(partial)) / 10f);
    }

    @Override
    public boolean canBeCollidedWith() {
        return landed() && !isRemoved();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(echoDelay);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        echoDelay = buf.readVarInt();
    }
}
