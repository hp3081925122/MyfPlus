package com.hp.myfplus.client.fx;

import com.hp.myfplus.core.Combat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.phys.Vec3;

public final class SlashDraw {
    private static final float BAND = 0.8f;

    private SlashDraw() {
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, Vec3 a0, Vec3 a1, Vec3 b1, Vec3 b0, float u0, float u1, float v0, float v1, int color) {
        FxDraw.vertex(vc, p, (float) a0.x, (float) a0.y, (float) a0.z, u0, v1, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) a1.x, (float) a1.y, (float) a1.z, u0, v0, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) b1.x, (float) b1.y, (float) b1.z, u1, v0, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) b0.x, (float) b0.y, (float) b0.z, u1, v1, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) b0.x, (float) b0.y, (float) b0.z, u1, v1, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) b1.x, (float) b1.y, (float) b1.z, u1, v0, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) a1.x, (float) a1.y, (float) a1.z, u0, v0, color, FxDraw.FULL);
        FxDraw.vertex(vc, p, (float) a0.x, (float) a0.y, (float) a0.z, u0, v1, color, FxDraw.FULL);
    }

    private static Vec3 point(float yaw, float a, float r, float h, boolean vertical) {
        if (!vertical) {
            Vec3 d = Combat.dirFromYaw(yaw + a);
            return new Vec3(d.x * r, h, d.z * r);
        }
        Vec3 f = Combat.dirFromYaw(yaw);
        double rad = Math.toRadians(a);
        return new Vec3(f.x * Math.cos(rad) * r, h + Math.sin(rad) * r, f.z * Math.cos(rad) * r);
    }

    public static void arc(PoseStack ps, VertexConsumer vc, float yaw, float radius, float sweep, float height, boolean vertical, int frame, int frames, int color) {
        PoseStack.Pose p = ps.last();
        int n = Math.max(6, (int) Math.ceil(sweep / 10f));
        float inner = Math.max(0.2f, radius - BAND);
        float v0 = frame / (float) frames, v1 = (frame + 1) / (float) frames;
        for (int i = 0; i < n; i++) {
            float a0 = -sweep / 2 + sweep * i / n, a1 = -sweep / 2 + sweep * (i + 1) / n;
            float u0 = i / (float) n, u1 = (i + 1) / (float) n;
            quad(vc, p, point(yaw, a0, inner, height, vertical), point(yaw, a0, radius, height, vertical),
                    point(yaw, a1, radius, height, vertical), point(yaw, a1, inner, height, vertical), u0, u1, v0, v1, color);
        }
    }

    public static void line(PoseStack ps, VertexConsumer vc, float yaw, float length, float width, float height, int frame, int frames, int color) {
        PoseStack.Pose p = ps.last();
        Vec3 d = Combat.dirFromYaw(yaw);
        Vec3 s = new Vec3(-d.z, 0, d.x).scale(width / 2);
        Vec3 up = new Vec3(0, width / 2, 0);
        Vec3 o = new Vec3(0, height, 0);
        Vec3 e = o.add(d.scale(length));
        float v0 = frame / (float) frames, v1 = (frame + 1) / (float) frames;
        quad(vc, p, o.subtract(s), o.add(s), e.add(s), e.subtract(s), 0, 1, v0, v1, color);
        quad(vc, p, o.subtract(up), o.add(up), e.add(up), e.subtract(up), 0, 1, v0, v1, color);
    }
}
