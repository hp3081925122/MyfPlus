package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.entity.MudGlobEntity;
import com.hp.myfplus.entity.SwampBubbleEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class SimpleSpriteRenderers {
    private SimpleSpriteRenderers() {
    }

    public static class Glob extends OwnedRenderer<MudGlobEntity> {
        public Glob(EntityRendererProvider.Context ctx) {
            super(ctx, 1f);
        }

        @Override
        public void render(MudGlobEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            ps.pushPose();
            ps.translate(0, 0.3, 0);
            bill(ps, buf, "swampjaw/mud_glob", 0.8f, FxDraw.frame(2, 3, true, e.age(partial)), 2, 0xFFFFFFFF, light);
            ps.popPose();
        }
    }

    public static class Bubble extends OwnedRenderer<SwampBubbleEntity> {
        public Bubble(EntityRendererProvider.Context ctx) {
            super(ctx, 1f);
        }

        @Override
        public void render(SwampBubbleEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            ps.pushPose();
            ps.translate(0, e.getBbHeight() / 2, 0);
            bill(ps, buf, "swampjaw/bubble", 1.0f, FxDraw.frame(4, 4, true, e.age(partial) + e.getId()), 4, 0xFFFFFFFF, light);
            ps.popPose();
        }
    }
}
