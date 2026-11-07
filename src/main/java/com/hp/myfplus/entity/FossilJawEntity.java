package com.hp.myfplus.entity;

import com.hp.myfplus.core.Combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 化石巨颌：一组从地下升起再合拢的獠牙。只负责外观与时间轴，伤害由技能按 {@link #tooth} 的几何结算。
 * ROWS：前后两排獠牙向中线咬合（地颌）；RING：半径 10 的獠牙环，留一个缺口后向圆心合拢（噬界巨颌）。
 */
public class FossilJawEntity extends OwnedEntity {
    public static final int ROWS = 0, RING = 1;

    public static final int ROW_TEETH = 6;
    public static final float ROW_DEPTH = 3.5f, ROW_HALF = 2.6f, ROW_SHUT = 0.45f;
    public static final int ROW_WARN = 20, ROW_RISE = 5, ROW_CLOSE = 5;
    public static final int ROW_CLAMP = ROW_WARN + ROW_RISE + ROW_CLOSE;
    public static final int ROW_LIFE = ROW_CLAMP + 14;

    public static final int RING_TEETH = 32;
    public static final float RING_RADIUS = 10f, RING_GAP = 26f, RING_SHUT = 1.2f;
    public static final int RING_WARN = 10, RING_RISE = 40, RING_CLAMP = 72, RING_CLOSE = 6;
    public static final int RING_LIFE = RING_CLAMP + RING_CLOSE + 14;

    public int mode;
    /** ROWS：咬合轴朝向；RING：缺口朝向。 */
    public float yaw;

    public FossilJawEntity(EntityType<? extends FossilJawEntity> type, Level level) {
        super(type, level);
    }

    public int teeth() {
        return mode == RING ? RING_TEETH : ROW_TEETH * 2;
    }

    public float scale() {
        return mode == RING ? 1.7f : 1f;
    }

    /** out = {相对 x, 相对 z, 升起程度 0..1}；该位置是缺口时返回 false。 */
    public boolean tooth(int i, float age, float[] out) {
        if (mode == RING) return ringTooth(i, age, out);
        int side = i < ROW_TEETH ? 1 : -1;
        float s = -ROW_HALF + (i % ROW_TEETH) * (2 * ROW_HALF / (ROW_TEETH - 1));
        float f0 = side * (ROW_DEPTH - 0.1f * s * s);
        float f = Mth.lerp(ease((age - ROW_WARN - ROW_RISE) / ROW_CLOSE), f0, side * ROW_SHUT);
        float rise = Mth.clamp((age - ROW_WARN) / ROW_RISE, 0f, 1f) - Mth.clamp((age - ROW_CLAMP - 8) / 6f, 0f, 1f);
        Vec3 d = Combat.dirFromYaw(yaw);
        out[0] = (float) (d.x * f - d.z * s);
        out[1] = (float) (d.z * f + d.x * s);
        out[2] = rise;
        return true;
    }

    private boolean ringTooth(int i, float age, float[] out) {
        float a = i * 360f / RING_TEETH;
        if (Math.abs(Mth.wrapDegrees(a - yaw)) < RING_GAP) return false;
        float order = Math.abs(Mth.wrapDegrees(a - yaw - 180f)) / (180f - RING_GAP);
        float start = RING_WARN + order * RING_RISE;
        float rise = Mth.clamp((age - start) / 4f, 0f, 1f) - Mth.clamp((age - RING_CLAMP - RING_CLOSE - 6) / 6f, 0f, 1f);
        float r = Mth.lerp(ease((age - RING_CLAMP) / RING_CLOSE), RING_RADIUS, RING_SHUT);
        Vec3 d = Combat.dirFromYaw(a);
        out[0] = (float) d.x * r;
        out[1] = (float) d.z * r;
        out[2] = rise;
        return true;
    }

    private static float ease(float k) {
        k = Mth.clamp(k, 0f, 1f);
        return k * k;
    }

    /** 两排獠牙之间的咬合区：横向 sidePad、纵向 depthPad 为额外判定宽度。 */
    public static boolean inRows(Vec3 center, float yaw, Vec3 p, double sidePad, double depthPad) {
        Vec3 d = Combat.dirFromYaw(yaw);
        double dx = p.x - center.x, dz = p.z - center.z;
        double f = dx * d.x + dz * d.z;
        double s = -dx * d.z + dz * d.x;
        return Math.abs(s) <= ROW_HALF + sidePad && Math.abs(f) <= ROW_DEPTH + depthPad;
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeVarInt(mode);
        buf.writeFloat(yaw);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        mode = buf.readVarInt();
        yaw = buf.readFloat();
    }
}
