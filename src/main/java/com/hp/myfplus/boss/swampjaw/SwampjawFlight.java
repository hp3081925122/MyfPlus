package com.hp.myfplus.boss.swampjaw;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class SwampjawFlight extends MoveControl {
    private final Mob fish;
    @Nullable
    private Vec3 goal;
    private double speed;
    private boolean manual;
    @Nullable
    private Vec3 face;

    public SwampjawFlight(Mob fish) {
        super(fish);
        this.fish = fish;
    }

    public void steer(Vec3 point, double speed) {
        goal = point;
        this.speed = speed;
        manual = false;
    }

    public void brake() {
        goal = null;
        manual = false;
    }

    public void manual() {
        manual = true;
    }

    public void face(@Nullable Vec3 point) {
        face = point;
    }

    @Override
    public void tick() {
        Vec3 dm = fish.getDeltaMovement();
        if (!manual) {
            if (goal == null) dm = dm.scale(0.7);
            else {
                Vec3 d = goal.subtract(fish.position());
                double len = d.length();
                Vec3 want = len < 0.05 ? Vec3.ZERO : d.scale(Math.min(speed, len * 0.35) / len);
                dm = dm.add(want.subtract(dm).scale(0.3));
            }
            fish.setDeltaMovement(dm);
        }
        orient(dm);
    }

    private void orient(Vec3 dm) {
        float yaw = fish.getYRot();
        float pitch;
        if (face != null) {
            Vec3 p = fish.position();
            double h = Combat.horizontalDist(p, face);
            if (h > 0.2) yaw = Combat.yawTo(p, face);
            pitch = (float) Mth.clamp(Math.toDegrees(Math.atan2(face.y - p.y, Math.max(h, 0.5))), -40, 40);
        }
        else {
            double h = Math.sqrt(dm.x * dm.x + dm.z * dm.z);
            if (h > 0.03) yaw = (float) (Math.atan2(dm.z, dm.x) * (180F / Math.PI)) - 90.0F;
            pitch = h + Math.abs(dm.y) > 0.03 ? (float) Mth.clamp(Math.toDegrees(Math.atan2(dm.y, h)), -50, 50) : 0;
        }
        float ny = Mth.approachDegrees(fish.getYRot(), yaw, 20);
        fish.setYRot(ny);
        fish.yBodyRot = ny;
        fish.yHeadRot = ny;
        fish.setXRot(Mth.approach(fish.getXRot(), pitch, 8));
    }
}
