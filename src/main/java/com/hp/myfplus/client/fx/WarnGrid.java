package com.hp.myfplus.client.fx;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.fx.FxEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WarnGrid {
    private static final int[][] PALETTES = {
            {0xEDE3C4, 0x5E9C86, 0x2A2116},
            {0xA8F0F8, 0x5FD8E8, 0x1A1030},
            {0xF0C040, 0xE84848, 0x1A1A1A},
            {0xF0B050, 0x8A4AB8, 0x14101E}
    };
    private static final int EDGE = 0, FILL = 1, OUTLINE = 2;

    private final float cell;
    private final float[] cells;

    private WarnGrid(float cell, float[] cells) {
        this.cell = cell;
        this.cells = cells;
    }

    private interface Shape {
        boolean in(double x, double z);

        int pat(double x, double z);
    }

    private static Shape lineShape(FxEntity e, float cell) {
        Vec3 d = Combat.dirFromYaw(e.getYRot());
        double len = e.length, hw = e.width / 2;
        return new Shape() {
            public boolean in(double x, double z) {
                double f = x * d.x + z * d.z;
                double s = x * d.z - z * d.x;
                return f >= 0 && f <= len && Math.abs(s) <= hw;
            }

            public int pat(double x, double z) {
                double f = x * d.x + z * d.z;
                double s = x * d.z - z * d.x;
                return Mth.floor((f - Math.abs(s)) / cell);
            }
        };
    }

    private static Shape roundShape(FxEntity e, float cell, boolean sector) {
        double r = e.size, halfAngle = e.width / 2;
        float yaw = e.getYRot();
        return new Shape() {
            public boolean in(double x, double z) {
                double dist = Math.sqrt(x * x + z * z);
                if (dist > r) return false;
                if (!sector || dist < 0.3) return true;
                float a = Combat.yawTo(Vec3.ZERO, new Vec3(x, 0, z));
                return Math.abs(Mth.wrapDegrees(a - yaw)) <= halfAngle;
            }

            public int pat(double x, double z) {
                return Mth.floor(Math.sqrt(x * x + z * z) / cell);
            }
        };
    }

    public static WarnGrid build(Level level, FxEntity e, float cell) {
        Shape shape;
        double reach;
        switch (e.fx.kind) {
            case WARN_LINE -> {
                shape = lineShape(e, cell);
                reach = e.length + e.width + 1;
            }
            case WARN_SECTOR -> {
                shape = roundShape(e, cell, true);
                reach = e.size + 1;
            }
            default -> {
                shape = roundShape(e, cell, false);
                reach = e.size + 1;
            }
        }
        double cx = e.getX(), cy = e.getY(), cz = e.getZ();
        Long2FloatOpenHashMap heights = new Long2FloatOpenHashMap();
        FloatArrayList out = new FloatArrayList();
        int i0 = Mth.floor((cx - reach) / cell), i1 = Mth.floor((cx + reach) / cell);
        int k0 = Mth.floor((cz - reach) / cell), k1 = Mth.floor((cz + reach) / cell);
        for (int i = i0; i <= i1; i++) {
            for (int k = k0; k <= k1; k++) {
                double wx = (i + 0.5) * cell, wz = (k + 0.5) * cell;
                double lx = wx - cx, lz = wz - cz;
                boolean in = shape.in(lx, lz);
                boolean n = shape.in(lx + cell, lz), s = shape.in(lx - cell, lz), ea = shape.in(lx, lz + cell), w = shape.in(lx, lz - cell);
                int kind;
                if (in) kind = (n && s && ea && w) ? FILL : EDGE;
                else if (n || s || ea || w) kind = OUTLINE;
                else continue;
                int bx = Mth.floor(wx), bz = Mth.floor(wz);
                long key = ((long) bx << 32) ^ (bz & 0xFFFFFFFFL);
                float y;
                if (heights.containsKey(key)) y = heights.get(key);
                else {
                    double t = GroundProbe.top(level, bx + 0.5, bz + 0.5, cy + 0.5, 1, 3);
                    y = Double.isNaN(t) || Math.abs(t - cy) > 2.5 ? Float.NaN : (float) (t - cy);
                    heights.put(key, y);
                }
                if (Float.isNaN(y)) continue;
                out.add((float) (i * cell - cx));
                out.add((float) (k * cell - cz));
                out.add(y);
                out.add(kind);
                out.add(in ? shape.pat(lx, lz) : 0);
            }
        }
        return new WarnGrid(cell, out.toFloatArray());
    }

    public void draw(PoseStack ps, VertexConsumer vc, int palette, float age, int life, float alpha) {
        int[] pal = PALETTES[Math.floorMod(palette, PALETTES.length)];
        boolean blink = age > life * 0.7f && ((int) age / 2) % 2 == 0;
        int march = (int) (age / 2);
        int edge = FxDraw.alpha(blink ? 0xFFFFFF : pal[0], alpha);
        int fill = FxDraw.alpha(pal[1], alpha);
        int dark = FxDraw.alpha(pal[2], alpha * 0.85f);
        PoseStack.Pose p = ps.last();
        for (int i = 0; i < cells.length; i += 5) {
            int kind = (int) cells[i + 3];
            int color;
            if (kind == EDGE) color = edge;
            else if (kind == OUTLINE) color = dark;
            else if (Math.floorMod((int) cells[i + 4] - march, 4) == 0) color = fill;
            else continue;
            float x0 = cells[i], z0 = cells[i + 1], y = cells[i + 2] + 0.02f;
            float x1 = x0 + cell, z1 = z0 + cell;
            FxDraw.vertex(vc, p, x0, y, z0, 0, 0, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x0, y, z1, 0, 1, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y, z1, 1, 1, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y, z0, 1, 0, color, FxDraw.FULL);
        }
    }
}
