package com.hp.myfplus.client;

import com.hp.myfplus.boss.swampjaw.SwampjawBrain;
import com.hp.myfplus.core.MyfBossHost;
import com.hp.myfplus.mixin.client.SwampjawModelAccessor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class BossPoses {
    private BossPoses() {
    }

    public static void apply(LivingEntity entity, EntityModel<?> model, float partial) {
        if (!(entity instanceof MyfBossHost host) || host.myfplus$skill() == 0) return;
        if (model instanceof SwampjawModelAccessor sj && host.myfplus$skill() == SwampjawBrain.SKILL_SPIT) {
            float k = Mth.clamp((host.myfplus$skillAge() + partial) / 5f, 0f, 1f);
            ModelPart jaw = sj.myfplus$jaw();
            jaw.xRot = Math.max(jaw.xRot, 45f * k * Mth.DEG_TO_RAD);
            sj.myfplus$head().xRot -= 12f * k * Mth.DEG_TO_RAD;
        }
    }
}
