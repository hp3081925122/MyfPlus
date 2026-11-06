package com.hp.myfplus.client.fx;

import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FxEntityRenderer extends EntityRenderer<FxEntity> {
    private static final float[] SHRINK = {1f, 0.75f, 0.5f, 0.375f};
    private final BlockRenderDispatcher blocks;

    public FxEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0;
        blocks = ctx.getBlockRenderDispatcher();
    }

    @Override
    public ResourceLocation getTextureLocation(FxEntity e) {
        return FxDraw.WHITE;
    }

    @Override
    public boolean shouldRender(FxEntity e, Frustum frustum, double x, double y, double z) {
        if (e.fx.kind == FxType.Kind.EVENT || !e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z))) return false;
        float r = Math.max(Math.max(e.size, e.length), e.fx.height) * Math.max(1f, e.fx.width) + 2;
        return frustum.isVisible(e.getBoundingBox().inflate(r));
    }

    private static float fade(FxEntity e, float age) {
        float in = e.fx.kind.name().startsWith("WARN") ? Math.min(1f, age / 3f) : 1f;
        return Math.max(0f, Math.min(in, (e.life - age) / 3f + 0.34f));
    }

    @Override
    public void render(FxEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        FxType t = e.fx;
        if (t == FxType.NONE) return;
        float age = e.age(partial);
        int lt = t.bright ? FxDraw.FULL : light;
        int color = FxDraw.alpha(0xFFFFFF, fade(e, age));
        switch (t.kind) {
            case ROCK_RING, ROCK_LINE, ROCK_BURST -> {
                if (!(e.clientCache instanceof RockFx)) {
                    e.clientCache = switch (t.kind) {
                        case ROCK_RING -> RockFx.ring(e.level(), e);
                        case ROCK_LINE -> RockFx.line(e.level(), e);
                        default -> RockFx.burst(e.level(), e);
                    };
                }
                ((RockFx) e.clientCache).draw(ps, buf, blocks, age, e.life);
            }
            case WARN_LINE, WARN_SECTOR, WARN_CIRCLE -> {
                if (!(e.clientCache instanceof WarnGrid)) e.clientCache = WarnGrid.build(e.level(), e, MyfConfig.WARN_CELL.get().floatValue());
                ((WarnGrid) e.clientCache).draw(ps, buf.getBuffer(FxDraw.type(FxDraw.WHITE)), e.palette, age, e.life, fade(e, age));
            }
            case DECAL -> {
                float half = t.width * e.size / 2;
                if (!(e.clientCache instanceof GroundDecal)) e.clientCache = GroundDecal.build(e.level(), e.getX(), e.getY(), e.getZ(), half);
                int f = FxDraw.frame(t.frames, t.ticksPerFrame, t.loop, age);
                ((GroundDecal) e.clientCache).draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), f, t.frames, color, lt, 0.015f + (e.getId() & 7) * 0.002f);
            }
            case DECAL_SHRINK -> {
                if (!(e.clientCache instanceof GroundDecal[])) {
                    GroundDecal[] arr = new GroundDecal[SHRINK.length];
                    for (int i = 0; i < arr.length; i++) arr[i] = GroundDecal.build(e.level(), e.getX(), e.getY(), e.getZ(), t.width * e.size * SHRINK[i] / 2);
                    e.clientCache = arr;
                }
                int idx = Math.min(SHRINK.length - 1, (int) (age * SHRINK.length / Math.max(1, e.life)));
                VertexConsumer vc = buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture + idx)));
                ((GroundDecal[]) e.clientCache)[idx].draw(ps, vc, 0, 1, color, lt, 0.03f);
            }
            case BILL -> {
                int f = FxDraw.frame(t.frames, t.ticksPerFrame, t.loop, age);
                FxDraw.billboard(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), entityRenderDispatcher.cameraOrientation(), t.width * e.size, t.height * e.size, f, t.frames, color, lt);
            }
            case VBILL_DROP -> {
                float k = Math.min(1f, age / 8f);
                float a = Math.max(0f, Math.min(1f, (e.life - age) / 6f));
                ps.pushPose();
                ps.translate(0, 12f * (1 - k) * (1 - k), 0);
                FxDraw.vbillboard(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), entityRenderDispatcher.camera.getYRot(), t.width * e.size, t.height * e.size,
                        Math.floorMod(e.count, t.frames), t.frames, FxDraw.alpha(0xFFFFFF, a), lt);
                ps.popPose();
            }
            case VBILL -> {
                int f = FxDraw.frame(t.frames, t.ticksPerFrame, t.loop, age);
                FxDraw.vbillboard(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), entityRenderDispatcher.camera.getYRot(), t.width * e.size, t.height * e.size, f, t.frames, color, lt);
            }
            default -> {
            }
        }
    }
}
