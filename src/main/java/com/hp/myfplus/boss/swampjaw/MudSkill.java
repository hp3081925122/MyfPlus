package com.hp.myfplus.boss.swampjaw;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.BossSkill;
import com.hp.myfplus.entity.MudGlobEntity;
import com.hp.myfplus.registry.MyfEntities;

import lykrast.meetyourfight.entity.SwampjawEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MudSkill extends BossSkill {
    private static final int WINDUP = 10;
    private final SwampjawBrain b;

    public MudSkill(SwampjawBrain b) {
        super("mud", 160);
        this.b = b;
    }

    @Override
    public boolean canUse(LivingEntity target, double dist) {
        return dist <= 30;
    }

    @Override
    public int weight(LivingEntity target, double dist) {
        return b.pits.size() < 2 ? 4 : 2;
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
            for (Vec3 p : landing(target, n, fish.getRandom())) {
                MudGlobEntity glob = new MudGlobEntity(MyfEntities.MUD_GLOB.get(), fish.level());
                glob.setOwner(fish);
                glob.launch(m, p, 25);
                glob.onLand = g -> b.addPit(g.to);
                fish.level().addFreshEntity(glob);
            }
            fish.playSound(SoundEvents.LLAMA_SPIT, 2.0f, 0.5f);
        }
        return t >= WINDUP + 4;
    }

    private List<Vec3> landing(LivingEntity target, int n, RandomSource rand) {
        List<Vec3> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Vec3 best = null;
            for (int tries = 0; tries < 12; tries++) {
                double a = rand.nextDouble() * Math.PI * 2;
                double r = 3 + rand.nextDouble() * 4;
                double x = target.getX() + Math.cos(a) * r;
                double z = target.getZ() + Math.sin(a) * r;
                Vec3 p = new Vec3(x, b.ground(x, z, target.getY() + 0.5), z);
                best = p;
                boolean ok = true;
                for (Vec3 o : out) if (o.distanceToSqr(p) < 16) ok = false;
                if (ok) break;
            }
            out.add(best);
        }
        return out;
    }

    @Override
    public void stop() {
        b.setSkill(0);
        b.flight.face(null);
    }
}
