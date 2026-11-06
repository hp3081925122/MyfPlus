package com.hp.myfplus.client.fx;

import org.joml.Quaternionf;

import com.hp.myfplus.Myfplus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class FxDraw {
    public static final int FULL = LightTexture.FULL_BRIGHT;
    public static final ResourceLocation WHITE = tex("white");

    private FxDraw() {
    }

    public static ResourceLocation tex(String path) {
        return new ResourceLocation(Myfplus.MOD_ID, "textures/fx/" + path + ".png");
    }

    public static RenderType type(ResourceLocation tex) {
        return RenderType.entityTranslucent(tex);
    }

    public static int frame(int frames, int ticksPerFrame, boolean loop, float age) {
        int f = (int) (age / Math.max(1, ticksPerFrame));
        return loop ? Math.floorMod(f, frames) : Math.min(frames - 1, Math.max(0, f));
    }

    public static int alpha(int rgb, float a) {
        int al = Math.max(0, Math.min(255, (int) (a * 255)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, int argb, int light) {
        vc.vertex(pose.pose(), x, y, z).color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, argb >>> 24).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    public static void billboard(PoseStack ps, VertexConsumer vc, Quaternionf cam, float w, float h, int frame, int frames, int argb, int light) {
        ps.pushPose();
        ps.mulPose(cam);
        PoseStack.Pose p = ps.last();
        float hw = w / 2, hh = h / 2;
        float v0 = frame / (float) frames, v1 = (frame + 1) / (float) frames;
        vertex(vc, p, -hw, -hh, 0, 1, v1, argb, light);
        vertex(vc, p, -hw, hh, 0, 1, v0, argb, light);
        vertex(vc, p, hw, hh, 0, 0, v0, argb, light);
        vertex(vc, p, hw, -hh, 0, 0, v1, argb, light);
        ps.popPose();
    }

    public static void rect(PoseStack.Pose p, VertexConsumer vc, float x0, float y0, float x1, float y1, float z, float u0, float v0, float u1, float v1, int argb, int light) {
        vertex(vc, p, x0, y0, z, u1, v1, argb, light);
        vertex(vc, p, x0, y1, z, u1, v0, argb, light);
        vertex(vc, p, x1, y1, z, u0, v0, argb, light);
        vertex(vc, p, x1, y0, z, u0, v1, argb, light);
    }

    public static void vbillboard(PoseStack ps, VertexConsumer vc, float camYaw, float w, float h, int frame, int frames, int argb, int light) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-camYaw));
        PoseStack.Pose p = ps.last();
        float hw = w / 2;
        float v0 = frame / (float) frames, v1 = (frame + 1) / (float) frames;
        vertex(vc, p, -hw, 0, 0, 1, v1, argb, light);
        vertex(vc, p, -hw, h, 0, 1, v0, argb, light);
        vertex(vc, p, hw, h, 0, 0, v0, argb, light);
        vertex(vc, p, hw, 0, 0, 0, v1, argb, light);
        ps.popPose();
    }

    public static void strip(PoseStack ps, VertexConsumer vc, Vec3 a, Vec3 b, Vec3 camRel, float width, float tile, int argb, int light) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 1.0E-3) return;
        Vec3 dir = d.scale(1 / len);
        Vec3 mid = a.add(b).scale(0.5);
        Vec3 side = dir.cross(camRel.subtract(mid));
        if (side.lengthSqr() < 1.0E-6) side = dir.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-6) side = new Vec3(1, 0, 0);
        side = side.normalize().scale(width / 2);
        PoseStack.Pose p = ps.last();
        int n = (int) Math.ceil(len / tile);
        for (int i = 0; i < n; i++) {
            double s0 = i * tile;
            double s1 = Math.min(len, (i + 1) * tile);
            Vec3 p0 = a.add(dir.scale(s0));
            Vec3 p1 = a.add(dir.scale(s1));
            float ve = (float) ((s1 - s0) / tile);
            vertex(vc, p, (float) (p0.x - side.x), (float) (p0.y - side.y), (float) (p0.z - side.z), 0, 0, argb, light);
            vertex(vc, p, (float) (p0.x + side.x), (float) (p0.y + side.y), (float) (p0.z + side.z), 1, 0, argb, light);
            vertex(vc, p, (float) (p1.x + side.x), (float) (p1.y + side.y), (float) (p1.z + side.z), 1, ve, argb, light);
            vertex(vc, p, (float) (p1.x - side.x), (float) (p1.y - side.y), (float) (p1.z - side.z), 0, ve, argb, light);
        }
    }
}
