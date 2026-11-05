package com.hp.myfplus.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import lykrast.meetyourfight.renderer.SwampjawModel;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(SwampjawModel.class)
public interface SwampjawModelAccessor {
    @Accessor(value = "jaw", remap = false)
    ModelPart myfplus$jaw();

    @Accessor(value = "head", remap = false)
    ModelPart myfplus$head();
}
