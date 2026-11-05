package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.entity.GasCloudEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class GasCloudRenderer extends OwnedRenderer<GasCloudEntity> {
    public GasCloudRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, 3f);
    }

    @Override
    public void render(GasCloudEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        float age = e.age(partial);
        int color = FxDraw.alpha(0xFFFFFF, e.fade(partial));
        int f = FxDraw.frame(4, 4, true, age);
        decal(e, e.radius).draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("swampjaw/gas_cloud"))), f, 4, color, light, 0.02f + (e.getId() & 7) * 0.002f);
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2 / 3 + (e.getId() % 7);
            float rise = ((age * 0.02f + i / 3f) % 1f) * 0.8f;
            ps.pushPose();
            ps.translate(Math.cos(a) * 1.0, rise, Math.sin(a) * 1.0);
            vbill(ps, buf, "swampjaw/gas_wisp", 0.75f, 1.5f, FxDraw.frame(3, 4, true, age + i * 4), 3, color, light);
            ps.popPose();
        }
    }
}
