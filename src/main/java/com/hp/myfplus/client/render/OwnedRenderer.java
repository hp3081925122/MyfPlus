package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.client.fx.GroundDecal;
import com.hp.myfplus.entity.OwnedEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class OwnedRenderer<T extends OwnedEntity> extends EntityRenderer<T> {
    protected final float cullPad;

    protected OwnedRenderer(EntityRendererProvider.Context ctx, float cullPad) {
        super(ctx);
        this.cullPad = cullPad;
        shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(T e) {
        return FxDraw.WHITE;
    }

    @Override
    public boolean shouldRender(T e, Frustum frustum, double x, double y, double z) {
        if (!e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z))) return false;
        return frustum.isVisible(e.getBoundingBox().inflate(cullPad));
    }

    protected GroundDecal decal(T e, float half) {
        if (!(e.clientCache instanceof GroundDecal)) e.clientCache = GroundDecal.build(e.level(), e.getX(), e.getY(), e.getZ(), half);
        return (GroundDecal) e.clientCache;
    }

    protected void bill(PoseStack ps, MultiBufferSource buf, String tex, float size, int frame, int frames, int color, int light) {
        FxDraw.billboard(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(tex))), entityRenderDispatcher.cameraOrientation(), size, size, frame, frames, color, light);
    }

    protected void vbill(PoseStack ps, MultiBufferSource buf, String tex, float w, float h, int frame, int frames, int color, int light) {
        FxDraw.vbillboard(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(tex))), entityRenderDispatcher.camera.getYRot(), w, h, frame, frames, color, light);
    }

    protected static int hash(int a, int b) {
        int h = a * 0x9E3779B1 + b * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        return h ^ (h >>> 12);
    }
}
