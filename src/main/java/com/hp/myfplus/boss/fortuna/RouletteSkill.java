package com.hp.myfplus.boss.fortuna;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.RouletteEntity;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.DameFortunaEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RouletteSkill extends FortunaSkill {
    @Nullable
    private RouletteEntity wheel;
    private Vec3 center = Vec3.ZERO;

    public RouletteSkill(FortunaBrain b) {
        super(b, "roulette", 260, false);
    }

    @Override
    protected boolean usable(LivingEntity target, double dist) {
        return dist <= 9;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 3;
    }

    @Override
    public void start(LivingEntity target) {
        wheel = null;
        center = b.groundAt(b.boss.position());
    }

    @Override
    protected boolean step(int t, @Nullable LivingEntity target) {
        DameFortunaEntity boss = b.boss;
        b.hover(center.x, center.y + 2.5, center.z, 0.4);
        if (t == 1) boss.setAnimation(DameFortunaEntity.ANIM_SNAP_PRE);
        if (t == 16) {
            boss.setAnimation(DameFortunaEntity.ANIM_SNAP_POST);
            boss.playSound(SoundEvents.NOTE_BLOCK_SNARE.get(), 2.0f, 1.4f);
            RouletteEntity w = new RouletteEntity(MyfEntities.ROULETTE.get(), boss.level());
            w.setPos(center.x, center.y, center.z);
            w.setOwner(boss);
            w.sector = boss.getRandom().nextInt(RouletteEntity.SECTORS);
            w.damage = Combat.damage(boss, b.cfg, 0.9);
            boss.level().addFreshEntity(w);
            wheel = w;
        }
        if (t == 24) boss.setAnimation(DameFortunaEntity.ANIM_CARD_WAIT);
        if (wheel != null && wheel.resolved) {
            if (RouletteEntity.color(wheel.sector) == RouletteEntity.GREEN) b.stunFor(40);
            return true;
        }
        return t > 120 || (t > 16 && (wheel == null || wheel.isRemoved()));
    }
}
