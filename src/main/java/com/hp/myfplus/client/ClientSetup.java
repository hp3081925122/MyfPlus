package com.hp.myfplus.client;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.client.fx.FxEntityRenderer;
import com.hp.myfplus.client.render.BellRenderers;
import com.hp.myfplus.client.render.BoneHookRenderer;
import com.hp.myfplus.client.render.FortunaRenderers;
import com.hp.myfplus.client.render.GasCloudRenderer;
import com.hp.myfplus.client.render.MudPitRenderer;
import com.hp.myfplus.client.render.SimpleSpriteRenderers;
import com.hp.myfplus.fx.FxEntity;
import com.hp.myfplus.registry.MyfEntities;

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
        e.registerEntityRenderer(MyfEntities.FX.get(), FxEntityRenderer::new);
        e.registerEntityRenderer(MyfEntities.MUD_PIT.get(), MudPitRenderer::new);
        e.registerEntityRenderer(MyfEntities.GAS_CLOUD.get(), GasCloudRenderer::new);
        e.registerEntityRenderer(MyfEntities.MUD_GLOB.get(), SimpleSpriteRenderers.Glob::new);
        e.registerEntityRenderer(MyfEntities.SWAMP_BUBBLE.get(), SimpleSpriteRenderers.Bubble::new);
        e.registerEntityRenderer(MyfEntities.BONE_HOOK.get(), BoneHookRenderer::new);
        e.registerEntityRenderer(MyfEntities.BELL_WAVE.get(), BellRenderers.Wave::new);
        e.registerEntityRenderer(MyfEntities.ECHO_MARK.get(), BellRenderers.Mark::new);
        e.registerEntityRenderer(MyfEntities.PHANTOM_BELL.get(), BellRenderers.Bell::new);
        e.registerEntityRenderer(MyfEntities.BELL_DOME.get(), BellRenderers.Dome::new);
        e.registerEntityRenderer(MyfEntities.DICE.get(), FortunaRenderers.Dice::new);
        e.registerEntityRenderer(MyfEntities.CHIP.get(), FortunaRenderers.Chip::new);
        e.registerEntityRenderer(MyfEntities.ROULETTE.get(), FortunaRenderers.Roulette::new);
        e.registerEntityRenderer(MyfEntities.SLOT_MACHINE.get(), FortunaRenderers.Slot::new);
        e.registerEntityRenderer(MyfEntities.ALL_IN.get(), FortunaRenderers.AllIn::new);
    }
}
