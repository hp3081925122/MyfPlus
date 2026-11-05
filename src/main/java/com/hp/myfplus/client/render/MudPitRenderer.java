package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.entity.MudPitEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MudPitRenderer extends OwnedRenderer<MudPitEntity> {
    public MudPitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, 3f);
    }

    @Override
    public void render(MudPitEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
        float age = e.age(partial);
        int color = FxDraw.alpha(0xFFFFFF, e.fade(partial));
        int f = FxDraw.frame(4, 6, true, age);
        decal(e, e.radius).draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("swampjaw/mud_pit"))), f, 4, color, light, 0.015f + (e.getId() & 7) * 0.002f);
        for (int i = 0; i < 2; i++) {
            float local = age + i * 9;
            int cycle = (int) (local / 18);
            int bf = (int) ((local % 18) / 6);
            int h = hash(e.getId() * 31 + i, cycle);
            double a = (h & 1023) / 1024.0 * Math.PI * 2;
            double r = 0.4 + ((h >>> 10) & 1023) / 1024.0 * (e.radius - 1.0);
            ps.pushPose();
            ps.translate(Math.cos(a) * r, 0.03, Math.sin(a) * r);
            vbill(ps, buf, "swampjaw/mud_bubble", 0.5f, 0.5f, bf, 3, color, light);
            ps.popPose();
        }
    }
}
