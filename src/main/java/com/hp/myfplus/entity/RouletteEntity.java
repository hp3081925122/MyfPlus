package com.hp.myfplus.entity;

import java.util.ArrayList;
import java.util.List;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.hp.myfplus.registry.MyfEffects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RouletteEntity extends OwnedEntity {
    public static final int SECTORS = 13;
    public static final float RADIUS = 10f;
    public static final float HOUSE = 1.5f;
    public static final float BALL_R = 9.2f;
    public static final int SPIN = 50;
    public static final int RESOLVE = SPIN + 15;
    public static final int GREEN = 0, RED = 1, BLACK = 2;
    public int sector;
    public boolean resolved;
    private int lastClick = -1;
    private final List<LivingEntity> bound = new ArrayList<>();

    public RouletteEntity(EntityType<? extends RouletteEntity> type, Level level) {
        super(type, level);
        life = RESOLVE + 12;
    }

    public static int color(int sector) {
        return sector == 0 ? GREEN : (sector % 2 == 1 ? RED : BLACK);
    }

    public static int sectorAt(double dx, double dz) {
        double a = Math.atan2(dz, dx);
        if (a < 0) a += Mth.TWO_PI;
        return Math.min(SECTORS - 1, (int) (a / (Mth.TWO_PI / SECTORS)));
    }

    public float ballAngle(float age) {
        float target = (sector + 0.5f) * Mth.TWO_PI / SECTORS;
        float k = Math.max(0f, 1f - Math.min(age, SPIN) / SPIN);
        return target - (Mth.TWO_PI * 4.5f) * k * k;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || isRemoved()) return;
        if (tickCount == 1) bind();
        if (tickCount <= RESOLVE) confine();
        if (tickCount <= SPIN) {
            float a = ballAngle(tickCount);
            int s = sectorAt(Math.cos(a), Math.sin(a));
            if (s != lastClick) {
                lastClick = s;
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_HAT.get(), SoundSource.HOSTILE, 0.8f, 1.6f);
            }
        }
        if (tickCount == SPIN) level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.HOSTILE, 1.5f, color(sector) == GREEN ? 2.0f : 0.8f);
        if (tickCount == RESOLVE) resolve();
    }

    /** 转盘展开时站在盘面上的玩家都被禁锢，持续到结算。 */
    private void bind() {
        Mob boss = owner();
        if (boss == null) return;
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(RADIUS, 3, RADIUS))) {
            if (Combat.horizontalDist(v.position(), position()) > RADIUS || Math.abs(v.getY() - getY()) > 3) continue;
            v.addEffect(new MobEffectInstance(MyfEffects.ROULETTE_BOUND.get(), RESOLVE + 6, 0, false, true, true), boss);
            bound.add(v);
        }
    }

    /** 被禁锢者贴近盘缘时抵消向外的速度并推回盘内；用末影珍珠等方式离开盘面 4 格以上视为脱身，解除禁锢。 */
    private void confine() {
        bound.removeIf(v -> !v.isAlive() || v.level() != level() || !v.hasEffect(MyfEffects.ROULETTE_BOUND.get()));
        for (LivingEntity v : bound) {
            double dx = v.getX() - getX(), dz = v.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01) continue;
            if (dist > RADIUS + 4) {
                v.removeEffect(MyfEffects.ROULETTE_BOUND.get());
                continue;
            }
            double nx = dx / dist, nz = dz / dist;
            Vec3 dm = v.getDeltaMovement();
            double radial = dm.x * nx + dm.z * nz;
            if (dist >= RADIUS - 0.8 && radial > -0.2) {
                double pull = dist >= RADIUS ? 0.35 : 0.25;
                v.setDeltaMovement(dm.x - nx * (radial + pull), dm.y, dm.z - nz * (radial + pull));
                v.hurtMarked = true;
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) for (LivingEntity v : bound) if (v.isAlive()) v.removeEffect(MyfEffects.ROULETTE_BOUND.get());
        bound.clear();
        super.remove(reason);
    }

    private void resolve() {
        resolved = true;
        Mob boss = owner();
        int c = color(sector);
        if (boss == null || c == GREEN) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.HOSTILE, 1.5f, 1.2f);
            FxEntity.at(level(), FxType.FT_JACKPOT, getX(), getY() + 1.5, getZ()).size(1.6f).spawn();
            return;
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0f, 0.9f);
        Fx.shake(level(), position(), 0.7f);
        for (int i = 0; i < SECTORS; i++) {
            if (color(i) != c) continue;
            float a = (i + 0.5f) * Mth.TWO_PI / SECTORS;
            Vec3 p = position().add(Math.cos(a) * 6.5, 0, Math.sin(a) * 6.5);
            Fx.rockBurst(level(), p, 1.5f, 4);
            FxEntity.at(level(), FxType.FT_BLAST, p.x, p.y + 1, p.z).size(2.5f).spawn();
        }
        for (LivingEntity v : Combat.foes(boss, getBoundingBox().inflate(RADIUS, 3, RADIUS))) {
            double dx = v.getX() - getX(), dz = v.getZ() - getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > RADIUS || Math.abs(v.getY() - getY()) > 3) continue;
            if (d > HOUSE && color(sectorAt(dx, dz)) != c) continue;
            if (v.hurt(boss.damageSources().mobAttack(boss), damage)) Combat.push(v, new Vec3(0, 0.3, 0));
        }
    }

    public float fade(float partial) {
        float a = age(partial);
        return Math.min(1f, Math.min(a / 5f, (life - a) / 8f));
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(sector);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        sector = buf.readVarInt();
    }
}
