package com.hp.myfplus.client.render;

import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.entity.BoneHookEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BoneHookRenderer extends OwnedRenderer<BoneHookEntity> {
    public BoneHookRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, 1f);
    }

    @Override
    public boolean shouldRender(BoneHookEntity e, Frustum frustum, double x, double y, double z) {
        Entity owner = e.level().getEntity(e.ownerId);
        if (owner == null) return super.shouldRender(e, frustum, x, y, z);
        return frustum.isVisible(e.getBoundingBox().minmax(owner.getBoundingBox()).inflate(1));
    }

    @Override
    public void render(BoneHookEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        Vec3 origin = e.getPosition(partial);
        Entity owner = e.level().getEntity(e.ownerId);
        Vec3 hook = new Vec3(0, 0.25, 0);
        if (owner != null) {
            Vec3 mouth = SwampjawBrain.mouth(owner, partial).subtract(origin);
            Vec3 cam = entityRenderDispatcher.camera.getPosition().subtract(origin);
            FxDraw.strip(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("swampjaw/vine_line"))), mouth, hook, cam, 0.4f, 0.8f, 0xFFFFFFFF, light);
        }
        ps.pushPose();
        ps.translate(hook.x, hook.y, hook.z);
        bill(ps, buf, "swampjaw/hook", 0.8f, 0, 1, 0xFFFFFFFF, light);
        ps.popPose();
    }
}
