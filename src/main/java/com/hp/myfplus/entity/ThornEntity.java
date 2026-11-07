package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ThornEntity extends OwnedEntity {
    public static final int GROW = 8;
    public static final int WITHER = 9;
    public int delay;
    public int variant;
    public int group;
    private int hitCooldown;

    public ThornEntity(EntityType<? extends ThornEntity> type, Level level) {
        super(type, level);
        life = 200;
    }

    public boolean grown() {
        return tickCount >= delay + GROW;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        if (tickCount == delay) level().playSound(null, getX(), getY(), getZ(), SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.HOSTILE, 0.8f, 0.7f + random.nextFloat() * 0.3f);
        if (hitCooldown > 0) hitCooldown--;
        if (!grown() || hitCooldown > 0) return;
        Mob boss = owner();
        if (boss == null) return;
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(0.15))) {
            if (v.hurt(boss.damageSources().thorns(boss), damage)) {
                Vec3 away = new Vec3(v.getX() - getX(), 0, v.getZ() - getZ());
                if (away.lengthSqr() > 1.0E-4) Combat.push(v, away.normalize().scale(0.3));
            }
            hitCooldown = 10;
        }
    }

    @Override
    public boolean isPickable() {
        return grown() && !isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved() || source.getEntity() == owner()) return false;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.HOSTILE, 1.0f, 0.8f);
        FxEntity.at(level(), FxType.RS_PETAL_BREAK, getX(), getY() + 0.8, getZ()).spawn();
        discard();
        return true;
    }

    public float witherAge(float partial) {
        return age(partial) - (life - WITHER);
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(delay);
        buf.writeVarInt(variant);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        delay = buf.readVarInt();
        variant = buf.readVarInt();
    }
}
