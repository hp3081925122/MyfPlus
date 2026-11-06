package com.hp.myfplus.client;

import com.hp.myfplus.Myfplus;
import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.core.MyfBossHost;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myfplus.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void hideBoss(RenderLivingEvent.Pre<?, ?> e) {
        if (e.getEntity() instanceof MyfBossHost host && host.myfplus$skill() == SwampjawBrain.SKILL_SUBMERGED) e.setCanceled(true);
    }
}
