package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.core.Combat;
import com.hp.myfplus.entity.SwampBubbleEntity;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BubbleSkill extends BossSkill {
    private static final int WINDUP = 10;
    private final SwampjawBrain b;

    public BubbleSkill(SwampjawBrain b) {
        super("bubble", 180);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 24;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return 2;
    }

    @Override
    public boolean tick(int t, @Nullable LivingEntity target) {
        SwampjawEntity fish = b.boss;
        b.flight.brake();
        if (target == null) return true;
        if (t <= WINDUP) {
            b.flight.face(target.getEyePosition());
            b.setSkill(SwampjawBrain.SKILL_SPIT);
        }
        if (t == WINDUP) {
            b.setSkill(0);
            int n = b.phase2 ? 5 : 3;
            Vec3 m = SwampjawBrain.mouth(fish);
            float baseYaw = fish.getYRot();
            for (int i = 0; i < n; i++) {
                float yaw = baseYaw + (i - (n - 1) / 2f) * 28f;
                Vec3 d = Combat.dirFromYaw(yaw);
                SwampBubbleEntity bub = new SwampBubbleEntity(MyfEntities.SWAMP_BUBBLE.get(), fish.level());
                bub.setPos(m.x + d.x * 0.6, m.y, m.z + d.z * 0.6);
                bub.setOwner(fish);
                bub.target = target;
                bub.cloudDamage = Combat.damage(fish, b.cfg, 0.15);
                bub.setDeltaMovement(d.x * 0.25, 0.08, d.z * 0.25);
                fish.level().addFreshEntity(bub);
            }
            fish.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 2.0f, 0.6f);
        }
        return t >= WINDUP + 4;
    }

    @Override
    public void stop() {
        b.setSkill(0);
        b.flight.face(null);
    }
}
