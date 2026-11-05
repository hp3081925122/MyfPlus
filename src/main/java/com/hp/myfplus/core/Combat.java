package com.hp.myfplus.core;

import java.util.List;
import java.util.function.Predicate;

import com.hp.myfplus.config.MyfConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    private Combat() {
    }

    public static float damage(Mob boss, MyfConfig.BossCfg cfg, double mult) {
        double atk = boss.getAttribute(Attributes.ATTACK_DAMAGE) != null ? boss.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1.0;
        return (float) (atk * mult * cfg.damage());
    }

    public static boolean isFoe(Mob boss, LivingEntity e) {
        if (e == boss || !e.isAlive() || e.isInvulnerable()) return false;
        if (e instanceof Player p) return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p);
        return e == boss.getTarget();
    }

    public static List<LivingEntity> foes(Mob boss, AABB box) {
        return boss.level().getEntitiesOfClass(LivingEntity.class, box, e -> isFoe(boss, e));
    }

    public static List<LivingEntity> foes(Mob boss, AABB box, Predicate<LivingEntity> filter) {
        return boss.level().getEntitiesOfClass(LivingEntity.class, box, e -> isFoe(boss, e) && filter.test(e));
    }

    public static boolean hit(Mob boss, MyfConfig.BossCfg cfg, LivingEntity target, double mult, double knockback) {
        return hit(boss, boss.damageSources().mobAttack(boss), damage(boss, cfg, mult), target, knockback, boss.position());
    }

    public static boolean hit(Mob boss, DamageSource source, float amount, LivingEntity target, double knockback, Vec3 from) {
        boolean ok = target.hurt(source, amount);
        if (ok && knockback > 0) {
            double dx = from.x - target.getX();
            double dz = from.z - target.getZ();
            if (dx * dx + dz * dz < 1.0E-4) {
                dx = boss.getRandom().nextDouble() - 0.5;
                dz = boss.getRandom().nextDouble() - 0.5;
            }
            target.knockback(Math.min(knockback, 0.8), dx, dz);
            target.hurtMarked = true;
        }
        return ok;
    }

    public static void push(LivingEntity target, Vec3 velocity) {
        double res = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (res <= 0) return;
        target.setDeltaMovement(target.getDeltaMovement().add(velocity.scale(res)));
        target.hurtMarked = true;
    }

    public static void setHorizontalVelocity(LivingEntity target, double vx, double vz) {
        double res = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 dm = target.getDeltaMovement();
        target.setDeltaMovement(vx * res, dm.y, vz * res);
        target.hurtMarked = true;
    }

    public static double horizontalDist(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static float yawTo(Vec3 from, Vec3 to) {
        return (float) (Math.atan2(to.z - from.z, to.x - from.x) * (180F / Math.PI)) - 90.0F;
    }

    public static Vec3 dirFromYaw(float yaw) {
        double r = Math.toRadians(yaw);
        return new Vec3(-Math.sin(r), 0, Math.cos(r));
    }

    public static boolean inGroundBand(Level level, LivingEntity e, double groundY, double height) {
        return e.getY() >= groundY - 0.6 && e.getY() <= groundY + height;
    }
}
