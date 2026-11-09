package com.hp.myfplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import lykrast.meetyourfight.entity.DameFortunaEntity;

@Mixin(DameFortunaEntity.class)
public interface DameFortunaAccessor {
    @Accessor(value = "attackCooldown", remap = false)
    int myfplus$getAttackCooldown();

    @Accessor(value = "attackCooldown", remap = false)
    void myfplus$setAttackCooldown(int value);

    @Accessor(value = "nextAttack", remap = false)
    void myfplus$setNextAttack(int value);
}
