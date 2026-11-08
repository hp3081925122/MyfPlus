package com.hp.myfplus.client;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.renderer.BellringerRenderer;
import lykrast.meetyourfight.renderer.DameFortunaRenderer;
import lykrast.meetyourfight.renderer.RosalyneRenderer;
import lykrast.meetyourfight.renderer.SwampMineRenderer;
import lykrast.meetyourfight.renderer.SwampjawRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(MyfEntities.ENHANCED_SWAMPJAW.get(), SwampjawRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_BELLRINGER.get(), BellringerRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_FORTUNA.get(), DameFortunaRenderer::new);
        e.registerEntityRenderer(MyfEntities.ENHANCED_ROSALYNE.get(), RosalyneRenderer::new);
        e.registerEntityRenderer(MyfEntities.MINE.get(), SwampMineRenderer::new);
    }
}
