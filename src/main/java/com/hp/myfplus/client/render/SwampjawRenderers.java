package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.client.fx.GroundDecal;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.entity.FinBladeEntity;
import com.hp.myfplus.entity.FossilJawEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class SwampjawRenderers {
    private SwampjawRenderers() {
    }

    public static class Jaw extends OwnedRenderer<FossilJawEntity> {
        private static final class Cache {
            final Long2FloatOpenHashMap ground = new Long2FloatOpenHashMap();
            float[][] start;
            GroundDecal[] marks;
        }

        public Jaw(EntityRendererProvider.Context ctx) {
            super(ctx, 14f);
        }

        private static float groundAt(Cache c, FossilJawEntity e, double x, double z) {
            int bx = Mth.floor(x), bz = Mth.floor(z);
            long key = ((long) bx << 32) ^ (bz & 0xFFFFFFFFL);
            if (c.ground.containsKey(key)) return c.ground.get(key);
            double y = GroundProbe.top(e.level(), bx + 0.5, bz + 0.5, e.getY() + 0.5, 2, 4);
            float v = Double.isNaN(y) ? 0f : (float) (y - e.getY());
            c.ground.put(key, v);
            return v;
        }

        @Override
        public void render(FossilJawEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            Cache c;
            if (e.clientCache instanceof Cache cc) c = cc;
            else {
                c = new Cache();
                e.clientCache = c;
            }
            int n = e.teeth();
            float sc = e.scale();
            float[] o = new float[3];
            if (c.marks == null) {
                c.start = new float[n][];
                c.marks = new GroundDecal[n];
                for (int i = 0; i < n; i++) {
                    if (!e.tooth(i, 0, o)) continue;
                    c.start[i] = new float[]{o[0], o[1]};
                    c.marks[i] = GroundDecal.build(e.level(), e.getX() + o[0], e.getY(), e.getZ() + o[1], 0.5f * sc);
                }
            }
            float age = e.age(partial);
            VertexConsumer mark = buf.getBuffer(FxDraw.type(FxDraw.tex("swampjaw/fang_mark")));
            for (int i = 0; i < n; i++) {
                if (c.marks[i] == null || !e.tooth(i, age, o) || o[2] >= 1f) continue;
                ps.pushPose();
                ps.translate(c.start[i][0], 0, c.start[i][1]);
                c.marks[i].draw(ps, mark, 0, 1, 0xFFFFFFFF, FxDraw.FULL, 0.02f + (i & 3) * 0.002f);
                ps.popPose();
            }
            VertexConsumer fang = buf.getBuffer(FxDraw.type(FxDraw.tex("swampjaw/fang")));
            float h = 2f * sc;
            float camYaw = entityRenderDispatcher.camera.getYRot();
            for (int i = 0; i < n; i++) {
                if (!e.tooth(i, age, o) || o[2] <= 0f) continue;
                float gy = groundAt(c, e, e.getX() + o[0], e.getZ() + o[1]);
                ps.pushPose();
                ps.translate(o[0], gy - h * (1 - o[2]) - 0.15f * sc, o[1]);
                FxDraw.vbillboard(ps, fang, camYaw, sc, h, 0, 1, 0xFFFFFFFF, light);
                ps.popPose();
            }
        }
    }

    public static class Fin extends OwnedRenderer<FinBladeEntity> {
        public Fin(EntityRendererProvider.Context ctx) {
            super(ctx, 1.5f);
        }

        @Override
        public void render(FinBladeEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            bill(ps, buf, "swampjaw/fin_blade", 1.25f, FxDraw.frame(4, 1, true, age), 4, 0xFFFFFFFF, light);
        }
    }
}
