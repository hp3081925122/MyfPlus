package com.hp.myfplus.client.fx;

import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.core.GroundProbe;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void ghost(FxEntity e, float age, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        if (!(e.level().getEntity(e.count) instanceof LivingEntity src)) return;
        if (!(entityRenderDispatcher.getRenderer(src) instanceof LivingEntityRenderer r)) return;
        EntityModel model = r.getModel();
        float a = Math.max(0f, Math.min(1f, (e.life - age) / e.life)) * 0.55f;
        if (a <= 0.01f) return;
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180f - e.getYRot()));
        ps.scale(-1f, -1f, 1f);
        ps.translate(0f, -1.501f, 0f);
        model.prepareMobModel(src, 0, 0, partial);
        model.setupAnim(src, 0, 0, src.tickCount + partial, 0, 0);
        VertexConsumer vc = new TintedConsumer(buf.getBuffer(RenderType.entityTranslucent(r.getTextureLocation(src))), 0.54f, 0.29f, 0.72f, a);
        model.renderToBuffer(ps, vc, FxDraw.FULL, OverlayTexture.NO_OVERLAY, 0.54f, 0.29f, 0.72f, a);
        ps.popPose();
    }

    /** 从跟随实体的嘴部射向地面的光束；yaw 为扫射中心，width 为带符号的扫射角，size 为射程。 */
    private void beam(FxEntity e, float age, float partial, PoseStack ps, MultiBufferSource buf, int color) {
        Entity src = e.level().getEntity(e.follow);
        if (src == null) return;
        float k = Mth.clamp(age / Math.max(1, e.life), 0f, 1f);
        Vec3 dir = Combat.dirFromYaw(e.getYRot() + e.width * (k - 0.5f));
        Vec3 base = e.getPosition(partial);
        Vec3 from = src.getPosition(partial);
        double ex = from.x + dir.x * e.size, ez = from.z + dir.z * e.size;
        double ey = GroundProbe.under(e.level(), ex, ez, from.y);
        Vec3 mouth = SwampjawBrain.mouth(src, partial).subtract(base);
        Vec3 end = new Vec3(ex, ey + 0.1, ez).subtract(base);
        Vec3 cam = entityRenderDispatcher.camera.getPosition().subtract(base);
        FxType t = e.fx;
        String tex = t.texture + FxDraw.frame(t.frames, t.ticksPerFrame, true, age);
        FxDraw.strip(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(tex))), mouth, end, cam, t.width, 1f, color, FxDraw.FULL);
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
            case ARC -> {
                int f = FxDraw.frame(t.frames, t.ticksPerFrame, t.loop, age);
                SlashDraw.arc(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), e.getYRot(), e.size, e.width, (float) e.offY, e.palette == 1, f, t.frames, color);
            }
            case SLASH_LINE -> {
                int f = FxDraw.frame(t.frames, t.ticksPerFrame, t.loop, age);
                SlashDraw.line(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(t.texture))), e.getYRot(), e.length, e.width, (float) e.offY, f, t.frames, color);
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
            case GHOST -> ghost(e, age, partial, ps, buf, light);
            case BEAM -> beam(e, age, partial, ps, buf, color);
            default -> {
            }
        }
    }
}
