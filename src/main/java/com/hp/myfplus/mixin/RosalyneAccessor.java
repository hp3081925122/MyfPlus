package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import lykrast.meetyourfight.entity.RosalyneEntity;

@Mixin(RosalyneEntity.class)
public interface RosalyneAccessor {
    @Invoker(value = "createSpirits", remap = false)
    void myfplus$createSpirits();
}
