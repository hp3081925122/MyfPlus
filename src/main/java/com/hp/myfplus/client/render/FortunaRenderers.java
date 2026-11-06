package com.hp.myfplus.client.render;

import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.client.fx.GroundDecal;
import com.hp.myfplus.entity.AllInEntity;
import com.hp.myfplus.entity.ChipEntity;
import com.hp.myfplus.entity.DiceEntity;
import com.hp.myfplus.entity.RouletteEntity;
import com.hp.myfplus.entity.SlotMachineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class FortunaRenderers {
    private static final String[] HI = {"fortuna/roulette_green", "fortuna/roulette_red", "fortuna/roulette_black"};

    private FortunaRenderers() {
    }

    public static class Dice extends OwnedRenderer<DiceEntity> {
        public Dice(EntityRendererProvider.Context ctx) {
            super(ctx, 1f);
        }

        @Override
        public void render(DiceEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            boolean rest = e.restTick() >= 0;
            int face = rest ? e.value - 1 : Math.floorMod((int) (age / 2) + e.getId(), 6);
            boolean blink = rest && (e.tickCount - e.restTick()) > DiceEntity.FUSE - 6 && ((int) age / 2) % 2 == 0;
            ps.pushPose();
            ps.translate(0, 0.35, 0);
            bill(ps, buf, "fortuna/dice_face", 0.7f, face, 6, blink ? 0xFFFF9090 : 0xFFFFFFFF, FxDraw.FULL);
            ps.popPose();
        }
    }

    public static class Chip extends OwnedRenderer<ChipEntity> {
        public Chip(EntityRendererProvider.Context ctx) {
            super(ctx, 1f);
        }

        @Override
        public void render(ChipEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            ps.pushPose();
            ps.translate(0, 0.2, 0);
            bill(ps, buf, "fortuna/chip", e.aoe > 0 ? 0.8f : 0.55f, FxDraw.frame(8, 1, true, e.age(partial) + e.getId()), 8, 0xFFFFFFFF, FxDraw.FULL);
            ps.popPose();
        }
    }

    public static class Roulette extends OwnedRenderer<RouletteEntity> {
        public Roulette(EntityRendererProvider.Context ctx) {
            super(ctx, 11f);
        }

        @Override
        public boolean shouldRender(RouletteEntity e, Frustum f, double x, double y, double z) {
            return e.shouldRenderAtSqrDistance(e.distanceToSqr(x, y, z)) && f.isVisible(e.getBoundingBox().inflate(RouletteEntity.RADIUS, 3, RouletteEntity.RADIUS));
        }

        @Override
        public void render(RouletteEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            int color = FxDraw.alpha(0xFFFFFF, e.fade(partial));
            GroundDecal d = decal(e, RouletteEntity.RADIUS);
            d.draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex("fortuna/roulette"))), 0, 1, color, FxDraw.FULL, 0.02f);
            if (age >= RouletteEntity.SPIN && ((int) (age - RouletteEntity.SPIN) / 3) % 2 == 0) {
                d.draw(ps, buf.getBuffer(FxDraw.type(FxDraw.tex(HI[RouletteEntity.color(e.sector)]))), 0, 1, color, FxDraw.FULL, 0.035f);
            }
            float a = e.ballAngle(age);
            ps.pushPose();
            ps.translate(Mth.cos(a) * RouletteEntity.BALL_R, 0.3, Mth.sin(a) * RouletteEntity.BALL_R);
            bill(ps, buf, "fortuna/roulette_ball", 0.5f, 0, 1, color, FxDraw.FULL);
            ps.popPose();
        }
    }

    public static class Slot extends OwnedRenderer<SlotMachineEntity> {
        private static final float W = 2.4f, H = 2.0f;

        public Slot(EntityRendererProvider.Context ctx) {
            super(ctx, 2f);
        }

        @Override
        public void render(SlotMachineEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            float pop = Math.min(1f, age / 5f);
            ps.pushPose();
            ps.translate(0, H / 2, 0);
            ps.mulPose(entityRenderDispatcher.cameraOrientation());
            ps.scale(pop, pop, pop);
            PoseStack.Pose p = ps.last();
            float px = W / 48f;
            FxDraw.rect(p, buf.getBuffer(FxDraw.type(FxDraw.tex("fortuna/slot_frame"))), -W / 2, -H / 2, W / 2, H / 2, 0, 0, 0, 1, 1, 0xFFFFFFFF, FxDraw.FULL);
            VertexConsumer vc = buf.getBuffer(FxDraw.type(FxDraw.tex("fortuna/slot_strip")));
            for (int i = 0; i < 3; i++) {
                int stop = e.stopTick(i);
                float off = stop >= 0 ? e.symbol(i) * SlotMachineEntity.CELL : (age * SlotMachineEntity.SPEED + SlotMachineEntity.offset(i)) % 60f;
                float v0 = off / 72f, v1 = (off + SlotMachineEntity.CELL) / 72f;
                float wx0 = (6 + i * 13) * px - W / 2;
                float wx1 = wx0 + 12 * px;
                float wy1 = H / 2 - 14 * px * (H / W) * (48f / 40f);
                float wy0 = wy1 - 12 * px * (H / W) * (48f / 40f);
                FxDraw.rect(p, vc, -wx1, wy0, -wx0, wy1, -0.01f, 0, v0, 1, v1, 0xFFFFFFFF, FxDraw.FULL);
            }
            ps.popPose();
        }
    }

    public static class AllIn extends OwnedRenderer<AllInEntity> {
        public AllIn(EntityRendererProvider.Context ctx) {
            super(ctx, 3f);
        }

        @Override
        public void render(AllInEntity e, float yaw, float partial, PoseStack ps, MultiBufferSource buf, int light) {
            float age = e.age(partial);
            float prog = e.progress();
            ps.pushPose();
            ps.mulPose(entityRenderDispatcher.cameraOrientation());
            PoseStack.Pose p = ps.last();
            VertexConsumer chip = buf.getBuffer(FxDraw.type(FxDraw.tex("fortuna/allin_chip")));
            float lw = 0.9f, lh = 0.225f;
            int shown = Math.min(AllInEntity.LAYERS, (int) (age / 2) + 1);
            for (int i = 0; i < shown; i++) {
                float wob = Mth.sin(age * 0.6f + i * 0.9f) * 0.03f * i * (0.3f + prog);
                float y0 = i * lh;
                FxDraw.rect(p, chip, -lw / 2 + wob, y0, lw / 2 + wob, y0 + lh, 0, 0, 0, 1, 1, 0xFFFFFFFF, FxDraw.FULL);
            }
            int frame = Math.min(12, (int) (prog * 12));
            float bw = 1.8f, bh = 0.3f, by = -0.45f;
            FxDraw.rect(p, buf.getBuffer(FxDraw.type(FxDraw.tex("fortuna/allin_bar"))), -bw / 2, by, bw / 2, by + bh, -0.01f, 0, frame / 13f, 1, (frame + 1) / 13f, 0xFFFFFFFF, FxDraw.FULL);
            ps.popPose();
        }
    }
}
