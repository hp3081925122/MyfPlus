package com.hp.myfplus.client;

import java.util.ArrayList;
import java.util.List;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.client.fx.FxDraw;
import com.hp.myfplus.config.MyfConfig;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.fx.FxType;
import com.mojang.blaze3d.systems.RenderSystem;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID, value = Dist.CLIENT)
public final class ClientFx {
    private static final String[] BOSS_KEYS = {"swampjaw", "bellringer", "dame_fortuna", "rosalyne"};
    private static final int BANNER_TICKS = 90;
    private static final List<float[]> SHAKES = new ArrayList<>();
    private static int bannerBoss = -1;
    private static int bannerPhase;
    private static long bannerStart;
    private static float fog;
    private static float fogTarget;

    private ClientFx() {
    }

    public static void onFxSpawn(FxEntity e) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.level == null) return;
        double d = p.distanceTo(e);
        if (e.fx == FxType.SHAKE) {
            if (!MyfConfig.SCREEN_SHAKE.get()) return;
            float s = e.shake * (float) Mth.clamp(1 - (d - 8) / 24, 0, 1);
            if (s > 0.01f) SHAKES.add(new float[]{s, mc.level.getGameTime(), 8 + s * 14});
        }
        else if (e.fx == FxType.BANNER && d < 48 && MyfConfig.PHASE_BANNER.get()) {
            bannerBoss = e.palette;
            bannerPhase = e.count;
            bannerStart = mc.level.getGameTime();
        }
    }

    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        Minecraft mc = Minecraft.getInstance();
        if (SHAKES.isEmpty() || mc.level == null) return;
        double now = mc.level.getGameTime() + e.getPartialTick();
        SHAKES.removeIf(s -> now - s[1] > s[2] || now < s[1] - 40);
        float amp = 0;
        for (float[] s : SHAKES) {
            float k = (float) (1 - (now - s[1]) / s[2]);
            amp += s[0] * k * k;
        }
        amp = Math.min(amp, 1.5f);
        if (amp <= 0) return;
        e.setPitch(e.getPitch() + amp * 2.2f * (float) Math.sin(now * 2.9));
        e.setYaw(e.getYaw() + amp * 1.6f * (float) Math.sin(now * 3.7 + 1));
        e.setRoll(e.getRoll() + amp * 2.0f * (float) Math.sin(now * 2.3 + 2));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            fog = 0;
            fogTarget = 0;
            SHAKES.clear();
            bannerBoss = -1;
            return;
        }
        if (mc.level.getGameTime() % 10 == 0) {
            fogTarget = mc.level.getEntitiesOfClass(SwampjawEntity.class, mc.player.getBoundingBox().inflate(40),
                    s -> s.isAlive() && s instanceof MyfBossHost h && h.myfplus$phase() >= 2).isEmpty() ? 0 : 1;
        }
        fog = Mth.approach(fog, fogTarget, 0.015f);
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog e) {
        if (fog <= 0.001f || !MyfConfig.BOSS_FOG.get() || e.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
        float far = Mth.lerp(fog, e.getFarPlaneDistance(), Math.min(e.getFarPlaneDistance(), 34f));
        float near = Mth.lerp(fog, e.getNearPlaneDistance(), 2f);
        e.setFarPlaneDistance(far);
        e.setNearPlaneDistance(near);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor e) {
        if (fog <= 0.001f || !MyfConfig.BOSS_FOG.get()) return;
        float k = fog * 0.75f;
        e.setRed(Mth.lerp(k, e.getRed(), 0.25f));
        e.setGreen(Mth.lerp(k, e.getGreen(), 0.31f));
        e.setBlue(Mth.lerp(k, e.getBlue(), 0.20f));
    }

    public static void renderBanner(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = gui.getMinecraft();
        if (bannerBoss < 0 || mc.level == null) return;
        float age = mc.level.getGameTime() - bannerStart + partial;
        if (age > BANNER_TICKS || age < 0) {
            bannerBoss = -1;
            return;
        }
        float alpha = Mth.clamp(Math.min(age / 6f, (BANNER_TICKS - age) / 14f), 0f, 1f);
        int slide = (int) ((1 - Mth.clamp(age / 8f, 0f, 1f)) * 24);
        String key = BOSS_KEYS[Math.floorMod(bannerBoss, BOSS_KEYS.length)];
        ResourceLocation tex = FxDraw.tex("banner/" + key);
        int bw = 160, bh = 32;
        int x = w / 2 - bw / 2;
        int y = h / 5 - slide;
        RenderSystem.enableBlend();
        g.setColor(1f, 1f, 1f, alpha);
        g.blit(tex, x, y, bw, bh, 0, 0, bw, bh, bw, bh);
        g.setColor(1f, 1f, 1f, 1f);
        int a = Math.max(8, (int) (alpha * 255)) << 24;
        g.drawCenteredString(mc.font, Component.translatable("myfplus.banner." + key), w / 2, y + 12, a | 0xF4EED0);
        g.drawCenteredString(mc.font, Component.translatable("myfplus.banner." + key + ".phase" + bannerPhase), w / 2, y + bh + 4, a | 0xC8D89A);
        RenderSystem.disableBlend();
    }
}
