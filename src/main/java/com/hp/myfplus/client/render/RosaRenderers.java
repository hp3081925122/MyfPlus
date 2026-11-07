package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.entity.DuelRingEntity;
import com.hp.myfplus.entity.PetalBladeEntity;
import com.hp.myfplus.entity.ThornEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public final class RosaRenderers {
    private RosaRenderers() {
    }

    public static class Thorn extends OwnedRenderer<ThornEntity> {
        private static final int FRAMES = 8;

        public Thorn(EntityRendererProvider.Context ctx) {
            super(ctx, 2f);
        }

        @Override
        public void render(ThornEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial) - e.delay;
            if (age < 0) return;
            int f;
            float wither = e.witherAge(partial);
            if (wither >= 0) f = 5 + Math.min(2, (int) (wither / 3));
            else f = Math.min(4, (int) (age / 2));
            VertexConsumer vc = buf.getBuffer(FxDraw.type(FxDraw.tex(e.variant == 0 ? "rosalyne/thorn_a" : "rosalyne/thorn_b")));
            float v0 = f / (float) FRAMES, v1 = (f + 1) / (float) FRAMES;
            for (int i = 0; i < 2; i++) {
                ps.pushPose();
                ps.mulPose(Axis.YP.rotationDegrees(45 + i * 90 + (e.getId() % 4) * 11));
                PoseStack.Pose p = ps.last();
                FxDraw.rect(p, vc, -0.5f, 0, 0.5f, 1.5f, 0, 0, v0, 1, v1, 0xFFFFFFFF, light);
                FxDraw.rect(p, vc, 0.5f, 0, -0.5f, 1.5f, 0, 1, v0, 0, v1, 0xFFFFFFFF, light);
                ps.popPose();
            }
        }
    }

    public static class Petal extends OwnedRenderer<PetalBladeEntity> {
        private static final int[] TRAIL = {0xF0B050, 0xC83C5A, 0x8A4AB8};

        public Petal(EntityRendererProvider.Context ctx) {
            super(ctx, 1f);
        }

        @Override
        public void render(PetalBladeEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            if (e.mode() != PetalBladeEntity.ORBIT) {
                Vec3 vel = new Vec3(e.getX() - e.xo, e.getY() - e.yo, e.getZ() - e.zo);
                if (vel.lengthSqr() > 1.0E-4) {
                    Vec3 back = vel.normalize().scale(-0.35);
                    Vec3 cam = entityRenderDispatcher.camera.getPosition().subtract(e.getPosition(partial));
                    VertexConsumer vc = buf.getBuffer(FxDraw.type(FxDraw.WHITE));
                    Vec3 at = new Vec3(0, 0.3, 0);
                    for (int i = 0; i < 3; i++) {
                        Vec3 next = at.add(back);
                        FxDraw.strip(ps, vc, at, next, cam, 0.3f - i * 0.08f, 1f, FxDraw.alpha(TRAIL[i], 0.8f - i * 0.22f), FxDraw.FULL);
                        at = next.add(back.scale(0.15));
                    }
                }
            }
            ps.pushPose();
            ps.translate(0, 0.3, 0);
            bill(ps, buf, "rosalyne/petal_blade", 0.8f, FxDraw.frame(4, 2, true, e.age(partial) + e.getId()), 4, 0xFFFFFFFF, FxDraw.FULL);
            ps.popPose();
        }
    }

    public static class Ring extends OwnedRenderer<DuelRingEntity> {
        public Ring(EntityRendererProvider.Context ctx) {
            super(ctx, 12f);
        }

        @Override
        public boolean shouldRender(DuelRingEntity e, Frustum f, double x, double y, double z) {
            return e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z)) && f.isVisible(e.getBoundingBox().inflate(e.radius + 1, 4, e.radius + 1));
        }

        @Override
        public void render(DuelRingEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            if (!(e.clientCache instanceof Long2FloatOpenHashMap)) e.clientCache = new Long2FloatOpenHashMap();
            float age = e.age(partial);
            float k = Math.min(1f, age / DuelRingEntity.RISE);
            float h = DuelRingEntity.HEIGHT * k;
            int f = FxDraw.frame(4, 5, true, age);
            BellRenderers.ring(buf.getBuffer(FxDraw.type(FxDraw.tex("rosalyne/duel_ring"))), ps, (Long2FloatOpenHashMap) e.clientCache, this, e.level(),
                    e.getX(), e.getY(), e.getZ(), e.radius, h, h - DuelRingEntity.HEIGHT, 1.0f, f, 4, new float[0], 0, 0xFFFFFFFF, true);
        }
    }
}
