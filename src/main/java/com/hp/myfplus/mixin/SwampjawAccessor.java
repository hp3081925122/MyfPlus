package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import lykrast.meetyourfight.entity.SwampjawEntity;

@Mixin(SwampjawEntity.class)
public interface SwampjawAccessor {
    @Accessor(value = "behavior", remap = false)
    int myfplus$getBehavior();

    @Accessor(value = "behavior", remap = false)
    void myfplus$setBehavior(int value);

    @Accessor(value = "attackDelay", remap = false)
    int myfplus$getAttackDelay();

    @Accessor(value = "attackDelay", remap = false)
    void myfplus$setAttackDelay(int value);
}
