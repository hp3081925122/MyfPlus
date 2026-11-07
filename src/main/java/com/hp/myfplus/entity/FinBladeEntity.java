package com.hp.myfplus.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 骨鳍刃：螺旋飞出再回旋飞回。轨迹完全由生成参数决定，客户端自己算，不需要位置同步。 */
public class FinBladeEntity extends OwnedEntity {
    public static final int OUT = 22, HOLD = 6, BACK = 22, LIFE = OUT + HOLD + BACK;
    public static final float REACH = 9f;

    public double cx, cy, cz;
    public float angle;
    public int spin = 1;

    public FinBladeEntity(EntityType<? extends FinBladeEntity> type, Level level) {
        super(type, level);
        life = LIFE;
    }

    public Vec3 at(float age) {
        float r;
        if (age < OUT) {
            float k = age / OUT;
            r = Mth.lerp(1 - (1 - k) * (1 - k), 1.5f, REACH);
        }
        else if (age < OUT + HOLD) r = REACH;
        else {
            float k = Mth.clamp((age - OUT - HOLD) / BACK, 0f, 1f);
            r = Mth.lerp(k * k, REACH, 0.8f);
        }
        double a = Math.toRadians(angle + spin * age * 8f);
        return new Vec3(cx + Math.cos(a) * r, cy, cz + Math.sin(a) * r);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 p = at(tickCount);
        setPos(p.x, p.y, p.z);
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeDouble(cx);
        buf.writeDouble(cy);
        buf.writeDouble(cz);
        buf.writeFloat(angle);
        buf.writeVarInt(spin);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        cx = buf.readDouble();
        cy = buf.readDouble();
        cz = buf.readDouble();
        angle = buf.readFloat();
        spin = buf.readVarInt();
        Vec3 p = at(0);
        setPos(p.x, p.y, p.z);
        xo = p.x;
        yo = p.y;
        zo = p.z;
    }
}
