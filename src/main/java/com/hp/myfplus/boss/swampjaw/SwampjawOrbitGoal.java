package com.hp.myfplus.boss.swampjaw;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class SwampjawOrbitGoal extends Goal {
    private final SwampjawBrain b;
    private double angle;
    private int sign = 1;

    public SwampjawOrbitGoal(SwampjawBrain b) {
        this.b = b;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private Vec3 anchor() {
        LivingEntity t = b.target();
        if (t != null) return t.position();
        return b.center != null ? b.center : b.boss.position();
    }

    @Override
    public void start() {
        Vec3 c = anchor();
        angle = Math.atan2(b.boss.getZ() - c.z, b.boss.getX() - c.x);
        sign = b.boss.getRandom().nextBoolean() ? 1 : -1;
    }

    @Override
    public void tick() {
        LivingEntity t = b.target();
        Vec3 c = anchor();
        double r = t != null ? 9 : 7;
        double h = t != null ? 5 : 6;
        angle += 0.035 * sign;
        if (b.boss.getRandom().nextInt(200) == 0) sign = -sign;
        double px = c.x + Math.cos(angle) * r;
        double pz = c.z + Math.sin(angle) * r;
        double gy = b.ground(px, pz, c.y + 2);
        double py = Math.max(c.y + h, gy + 3);
        b.flight.steer(new Vec3(px, py, pz), 0.55);
        b.flight.face(null);
    }
}
