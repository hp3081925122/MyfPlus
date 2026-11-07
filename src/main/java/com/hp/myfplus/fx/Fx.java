package com.hp.myfplus.fx;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Fx {
    public static final int POP_TICKS = 8;
    public static final int BOSS_SWAMPJAW = 0;
    public static final int BOSS_BELLRINGER = 1;
    public static final int BOSS_FORTUNA = 2;
    public static final int BOSS_ROSALYNE = 3;

    public static FxEntity arc(Level level, Vec3 origin, float yaw, float radius, float sweep, float height, boolean vertical) {
        FxEntity e = FxEntity.at(level, FxType.RS_ARC, origin).yaw(yaw).size(radius).line(radius, sweep).palette(vertical ? 1 : 0);
        e.offY = height;
        return e.spawn();
    }

    public static FxEntity thrust(Level level, Vec3 origin, float yaw, float length, float width, float height) {
        FxEntity e = FxEntity.at(level, FxType.RS_THRUST, origin).yaw(yaw).line(length, width);
        e.offY = height;
        return e.spawn();
    }

    private Fx() {
    }

    public static FxEntity rockRing(Level level, Vec3 center, float radius, float speed) {
        return FxEntity.at(level, FxType.ROCK_RING, center).size(radius).speed(speed).life((int) Math.ceil(radius / speed) + POP_TICKS + 4).spawn();
    }

    public static FxEntity rockLine(Level level, Vec3 start, float yaw, float length, float width, float speed) {
        return FxEntity.at(level, FxType.ROCK_LINE, start).yaw(yaw).line(length, width).speed(speed).life((int) Math.ceil(length / speed) + POP_TICKS + 4).spawn();
    }

    public static FxEntity rockBurst(Level level, Vec3 center, float spread, int count) {
        return FxEntity.at(level, FxType.ROCK_BURST, center).size(spread).count(count).spawn();
    }

    public static FxEntity shake(Level level, Vec3 center, float strength) {
        return FxEntity.at(level, FxType.SHAKE, center).shake(strength).spawn();
    }

    public static FxEntity banner(Level level, Vec3 center, int boss, int phase) {
        return FxEntity.at(level, FxType.BANNER, center).palette(boss).count(phase).spawn();
    }
}
