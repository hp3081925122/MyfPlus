package com.hp.myfplus.client;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.client.fx.FxEntityRenderer;
import com.hp.myfplus.client.render.BoneHookRenderer;
import com.hp.myfplus.client.render.GasCloudRenderer;
import com.hp.myfplus.client.render.MudPitRenderer;
import com.hp.myfplus.client.render.SimpleSpriteRenderers;
import com.hp.myfplus.registry.MyfEntities;

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
        e.registerEntityRenderer(MyfEntities.FX.get(), FxEntityRenderer::new);
        e.registerEntityRenderer(MyfEntities.MUD_PIT.get(), MudPitRenderer::new);
        e.registerEntityRenderer(MyfEntities.GAS_CLOUD.get(), GasCloudRenderer::new);
        e.registerEntityRenderer(MyfEntities.MUD_GLOB.get(), SimpleSpriteRenderers.Glob::new);
        e.registerEntityRenderer(MyfEntities.SWAMP_BUBBLE.get(), SimpleSpriteRenderers.Bubble::new);
        e.registerEntityRenderer(MyfEntities.BONE_HOOK.get(), BoneHookRenderer::new);
    }
}
