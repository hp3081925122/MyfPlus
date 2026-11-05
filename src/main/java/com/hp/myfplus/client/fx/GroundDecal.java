package com.hp.myfplus.client.fx;

import com.hp.myfplus.core.GroundProbe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.world.level.Level;

public final class GroundDecal {
    private final float half;
    private final float[] cols;

    private GroundDecal(float half, float[] cols) {
        this.half = half;
        this.cols = cols;
    }

    public static GroundDecal build(Level level, double cx, double cy, double cz, float half) {
        FloatArrayList out = new FloatArrayList();
        int minX = (int) Math.floor(cx - half), maxX = (int) Math.floor(cx + half);
        int minZ = (int) Math.floor(cz - half), maxZ = (int) Math.floor(cz + half);
        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                double x0 = Math.max(bx, cx - half), x1 = Math.min(bx + 1, cx + half);
                double z0 = Math.max(bz, cz - half), z1 = Math.min(bz + 1, cz + half);
                if (x1 - x0 < 1.0E-4 || z1 - z0 < 1.0E-4) continue;
                double y = GroundProbe.top(level, bx + 0.5, bz + 0.5, cy + 0.5, 1, 3);
                if (Double.isNaN(y) || Math.abs(y - cy) > 2.5) continue;
                out.add((float) (x0 - cx));
                out.add((float) (z0 - cz));
                out.add((float) (x1 - cx));
                out.add((float) (z1 - cz));
                out.add((float) (y - cy));
            }
        }
        return new GroundDecal(half, out.toFloatArray());
    }

    public void draw(PoseStack ps, VertexConsumer vc, int frame, int frames, int argb, int light, float lift) {
        PoseStack.Pose p = ps.last();
        float v0 = frame / (float) frames, vs = 1f / frames;
        float span = half * 2;
        for (int i = 0; i < cols.length; i += 5) {
            float x0 = cols[i], z0 = cols[i + 1], x1 = cols[i + 2], z1 = cols[i + 3], y = cols[i + 4] + lift;
            float u0 = (x0 + half) / span, u1 = (x1 + half) / span;
            float a0 = v0 + (z0 + half) / span * vs, a1 = v0 + (z1 + half) / span * vs;
            FxDraw.vertex(vc, p, x0, y, z0, u0, a0, argb, light);
            FxDraw.vertex(vc, p, x0, y, z1, u0, a1, argb, light);
            FxDraw.vertex(vc, p, x1, y, z1, u1, a1, argb, light);
            FxDraw.vertex(vc, p, x1, y, z0, u1, a0, argb, light);
        }
    }
}
