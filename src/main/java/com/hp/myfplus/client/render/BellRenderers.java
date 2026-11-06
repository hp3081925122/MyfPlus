package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.client.fx.GroundDecal;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.entity.BellDomeEntity;
import com.hp.myfplus.entity.BellWaveEntity;
import com.hp.myfplus.entity.EchoMarkEntity;
import com.hp.myfplus.entity.PhantomBellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class BellRenderers {
    private static final float SEG = 0.5f;

    private BellRenderers() {
    }

    private static float groundAt(Long2FloatOpenHashMap cache, OwnedRenderer<?> r, net.minecraft.world.level.Level level, double x, double z, double refY) {
        int bx = Mth.floor(x), bz = Mth.floor(z);
        long key = ((long) bx << 32) ^ (bz & 0xFFFFFFFFL);
        if (cache.containsKey(key)) return cache.get(key);
        double t = GroundProbe.top(level, bx + 0.5, bz + 0.5, refY + 0.5, 2, 4);
        float y = Double.isNaN(t) || Math.abs(t - refY) > 3 ? (float) refY : (float) t;
        cache.put(key, y);
        return y;
    }

    static void ring(VertexConsumer vc, PoseStack ps, Long2FloatOpenHashMap cache, OwnedRenderer<?> owner, net.minecraft.world.level.Level level,
                     double cx, double cy, double cz, float radius, float height, float lift, float tile, int frame, int frames,
                     float[] gaps, float gapHalf, int color, boolean followGround) {
        if (radius < 0.2f) return;
        int n = Math.max(8, (int) Math.ceil(radius * Mth.TWO_PI / SEG));
        PoseStack.Pose p = ps.last();
        float v0 = frame / (float) frames, v1 = (frame + 1) / (float) frames;
        for (int i = 0; i < n; i++) {
            double a0 = i * Mth.TWO_PI / n, a1 = (i + 1) * Mth.TWO_PI / n;
            double am = (a0 + a1) / 2;
            float yaw = (float) Math.toDegrees(Math.atan2(Math.sin(am), Math.cos(am))) - 90f;
            boolean skip = false;
            for (float g : gaps) if (Math.abs(Mth.wrapDegrees(yaw - g)) <= gapHalf) skip = true;
            if (skip) continue;
            float x0 = (float) (Math.cos(a0) * radius), z0 = (float) (Math.sin(a0) * radius);
            float x1 = (float) (Math.cos(a1) * radius), z1 = (float) (Math.sin(a1) * radius);
            float y = lift;
            if (followGround) y += groundAt(cache, owner, level, cx + Math.cos(am) * radius, cz + Math.sin(am) * radius, cy) - (float) cy;
            float u1 = SEG / tile;
            FxDraw.vertex(vc, p, x0, y, z0, 0, v1, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x0, y + height, z0, 0, v0, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y + height, z1, u1, v0, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y, z1, u1, v1, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y, z1, u1, v1, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x1, y + height, z1, u1, v0, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x0, y + height, z0, 0, v0, color, FxDraw.FULL);
            FxDraw.vertex(vc, p, x0, y, z0, 0, v1, color, FxDraw.FULL);
        }
    }

    public static class Wave extends OwnedRenderer<BellWaveEntity> {
        public Wave(EntityRendererProvider.Context ctx) {
            super(ctx, 20f);
        }

        @Override
        public boolean shouldRender(BellWaveEntity e, Frustum f, double x, double y, double z) {
            return e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z)) && f.isVisible(e.getBoundingBox().inflate(Math.max(e.startRadius, e.endRadius) + 1, 4, Math.max(e.startRadius, e.endRadius) + 1));
        }

        @Override
        public void render(BellWaveEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            if (!(e.clientCache instanceof Long2FloatOpenHashMap)) e.clientCache = new Long2FloatOpenHashMap();
            float age = e.age(partial);
            float r = e.radius(age);
            float fade = Math.min(1f, Math.min(age / 2f, (e.life - age) / 4f + 0.25f));
            String tex = e.tall ? "bellringer/wave_tall" : "bellringer/wave_low";
            int f = FxDraw.frame(2, 2, true, age);
            ring(buf.getBuffer(FxDraw.type(FxDraw.tex(tex))), ps, (Long2FloatOpenHashMap) e.clientCache, this, e.level(), e.getX(), e.getY(), e.getZ(),
                    r, e.height(), 0.02f, e.tall ? 1.0f : 1.0f, f, 2, e.gaps, BellWaveEntity.GAP_HALF, FxDraw.alpha(0xFFFFFF, fade), true);
        }
    }

    public static class Mark extends OwnedRenderer<EchoMarkEntity> {
        public Mark(EntityRendererProvider.Context ctx) {
            super(ctx, 2f);
        }

        @Override
        public void render(EchoMarkEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            int stage = e.stage(age);
            boolean flash = stage >= EchoMarkEntity.BEATS - 1 && ((int) age / 2) % 2 == 0;
            int f = flash ? 4 : Math.min(3, stage);
            decal(e, e.radius).draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("bellringer/echo_mark"))), f, 5, FxDraw.alpha(0xFFFFFF, Math.min(1f, age / 3f)), FxDraw.FULL, 0.02f + (e.getId() & 7) * 0.002f);
        }
    }

    public static class Bell extends OwnedRenderer<PhantomBellEntity> {
        private static final float[] SHADOW = {0.375f, 0.5f, 0.75f, 1f};

        public Bell(EntityRendererProvider.Context ctx) {
            super(ctx, 14f);
        }

        @Override
        public void render(PhantomBellEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            if (!(e.clientCache instanceof GroundDecal[])) {
                GroundDecal[] arr = new GroundDecal[4];
                for (int i = 0; i < 4; i++) arr[i] = GroundDecal.build(e.level(), e.getX(), e.getY(), e.getZ(), PhantomBellEntity.RADIUS * SHADOW[i]);
                e.clientCache = arr;
            }
            if (!e.landed()) {
                int idx = Math.min(3, (int) (age * 4 / PhantomBellEntity.FALL));
                ((GroundDecal[]) e.clientCache)[idx].draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("bellringer/bell_shadow_" + idx))), 0, 1, 0xFFFFFFFF, FxDraw.FULL, 0.03f);
            }
            int color = FxDraw.alpha(0xFFFFFF, e.fade(partial));
            ps.pushPose();
            ps.translate(0, e.fallOffset(age), 0);
            vbill(ps, buf, "bellringer/phantom_bell", 3.2f, 3.2f, 0, 1, color, FxDraw.FULL);
            ps.popPose();
        }
    }

    public static class Dome extends OwnedRenderer<BellDomeEntity> {
        public Dome(EntityRendererProvider.Context ctx) {
            super(ctx, 10f);
        }

        @Override
        public boolean shouldRender(BellDomeEntity e, Frustum f, double x, double y, double z) {
            return e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z)) && f.isVisible(e.getBoundingBox().inflate(e.radius + 1, 20, e.radius + 1));
        }

        @Override
        public void render(BellDomeEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            int color = FxDraw.alpha(0xFFFFFF, 0.85f * e.fade(partial));
            float drop = e.dropOffset(age);
            int f = FxDraw.frame(2, 4, true, age);
            ring(buf.getBuffer(FxDraw.type(FxDraw.tex("bellringer/dome_wall"))), ps, null, this, e.level(), e.getX(), e.getY(), e.getZ(),
                    e.radius, BellDomeEntity.HEIGHT, drop, 2.0f, f, 2, new float[0], 0, color, false);
            ps.pushPose();
            ps.translate(0, drop + BellDomeEntity.HEIGHT, 0);
            vbill(ps, buf, "bellringer/dome_crown", 3f, 1.5f, 0, 1, color, FxDraw.FULL);
            ps.popPose();
        }
    }
}
