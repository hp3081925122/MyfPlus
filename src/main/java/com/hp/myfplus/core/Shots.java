package com.hp.myfplus.core;

import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.ProjectileTargetedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Shots {
    public static final String MULT = "myfplus_mult";
    public static final String FLAT = "myfplus_flat";
    public static final String STUN = "myfplus_stun";

    private Shots() {
    }

    public static ProjectileLineEntity line(LivingEntity owner, int variant, Vec3 start, Vec3 dir, int delay) {
        ProjectileLineEntity p = new ProjectileLineEntity(owner.level(), owner);
        p.setOwner(owner);
        p.setVariant(variant);
        p.setPos(start.x, start.y, start.z);
        p.setUp(delay, dir.x, dir.y, dir.z, start.x, start.y, start.z);
        return p;
    }

    public static void fireLine(LivingEntity owner, int variant, double mult, Vec3 start, Vec3 dir, int delay) {
        ProjectileLineEntity p = line(owner, variant, start, dir, delay);
        p.getPersistentData().putDouble(MULT, mult);
        owner.level().addFreshEntity(p);
    }

    public static void fireChip(LivingEntity owner, double mult, LivingEntity target, Vec3 from, Vec3 hold, int delay, int setup, double speed, double angle) {
        ProjectileTargetedEntity p = new ProjectileTargetedEntity(owner.level(), owner);
        p.setOwner(owner);
        p.setPos(from.x, from.y, from.z);
        p.setUp(delay, setup, target, speed, hold.x, hold.y, hold.z, angle);
        p.getPersistentData().putDouble(MULT, mult);
        owner.level().addFreshEntity(p);
    }

    public static Vec3 flat(Vec3 v) {
        Vec3 f = new Vec3(v.x, 0, v.z);
        return f.lengthSqr() < 1.0E-6 ? new Vec3(1, 0, 0) : f.normalize();
    }

    public static Vec3 toward(Entity from, Entity to) {
        return flat(to.position().subtract(from.position()));
    }

    public static Vec3 dir(float yawRad) {
        return new Vec3(Mth.cos(yawRad), 0, Mth.sin(yawRad));
    }

    public static Vec3 lob(Vec3 from, Vec3 to, int ticks, double gravity) {
        double a = 0;
        double b = 0;
        double v = 0;
        double k = 1;
        for (int i = 0; i < ticks; i++) {
            a += k;
            v = (v - gravity);
            b += v;
            v *= 0.98;
            k *= 0.98;
        }
        Vec3 d = to.subtract(from);
        return new Vec3(d.x / a, (d.y - b) / a, d.z / a);
    }

    public static double groundY(Level level, double x, double y, double z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        for (int i = 0; i < 8; i++) {
            if (!level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) return p.getY();
            p.move(0, -1, 0);
        }
        return y;
    }

    public static Vec3 feet(LivingEntity e) {
        double y = e.onGround() ? e.getY() : groundY(e.level(), e.getX(), e.getY() + 0.5, e.getZ());
        return new Vec3(e.getX(), y + 0.1, e.getZ());
    }

    public static void particles(Level level, ParticleOptions type, Vec3 at, int count, double spread, double speed) {
        if (level instanceof ServerLevel s) s.sendParticles(type, at.x, at.y, at.z, count, spread, spread * 0.5, spread, speed);
    }

    public static boolean foe(LivingEntity owner, LivingEntity v) {
        if (v == owner || !v.isAlive() || v.isAlliedTo(owner)) return false;
        if (owner instanceof Player) {
            if (v instanceof Player) return false;
            return !(v instanceof OwnableEntity o && owner.getUUID().equals(o.getOwnerUUID()));
        }
        return true;
    }
}
