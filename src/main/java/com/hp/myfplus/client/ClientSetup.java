package com.hp.myfplus.client;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.client.fx.FxEntityRenderer;
import com.hp.myfplus.client.render.BellRenderers;
import com.hp.myfplus.client.render.FortunaRenderers;
import com.hp.myfplus.client.render.RosaRenderers;
import com.hp.myfplus.client.render.SwampjawRenderers;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.renderer.BellringerRenderer;
import lykrast.meetyourfight.renderer.DameFortunaRenderer;
import lykrast.meetyourfight.renderer.RosalyneRenderer;
import lykrast.meetyourfight.renderer.SwampjawRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        FxEntity.clientSpawnHook = ClientFx::onFxSpawn;
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("boss_banner", ClientFx::renderBanner);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(MyfEntities.ENHANCED_SWAMPJAW.get(), SwampjawRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_BELLRINGER.get(), BellringerRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_FORTUNA.get(), DameFortunaRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_ROSALYNE.get(), RosalyneRenderer::new);
        e.registerEntityRenderer(MyfEntities.FX.get(), FxEntityRenderer::new);
        e.registerEntityRenderer(MyfEntities.FOSSIL_JAW.get(), SwampjawRenderers.Jaw::new);
        e.registerEntityRenderer(MyfEntities.FIN_BLADE.get(), SwampjawRenderers.Fin::new);
        e.registerEntityRenderer(MyfEntities.BELL_WAVE.get(), BellRenderers.Wave::new);
        e.registerEntityRenderer(MyfEntities.ECHO_MARK.get(), BellRenderers.Mark::new);
        e.registerEntityRenderer(MyfEntities.PHANTOM_BELL.get(), BellRenderers.Bell::new);
        e.registerEntityRenderer(MyfEntities.BELL_DOME.get(), BellRenderers.Dome::new);
        e.registerEntityRenderer(MyfEntities.DICE.get(), FortunaRenderers.Dice::new);
        e.registerEntityRenderer(MyfEntities.CHIP.get(), FortunaRenderers.Chip::new);
        e.registerEntityRenderer(MyfEntities.ROULETTE.get(), FortunaRenderers.Roulette::new);
        e.registerEntityRenderer(MyfEntities.SLOT_MACHINE.get(), FortunaRenderers.Slot::new);
        e.registerEntityRenderer(MyfEntities.ALL_IN.get(), FortunaRenderers.AllIn::new);
        e.registerEntityRenderer(MyfEntities.THORN.get(), RosaRenderers.Thorn::new);
        e.registerEntityRenderer(MyfEntities.PETAL_BLADE.get(), RosaRenderers.Petal::new);
        e.registerEntityRenderer(MyfEntities.DUEL_RING.get(), RosaRenderers.Ring::new);
    }
}
