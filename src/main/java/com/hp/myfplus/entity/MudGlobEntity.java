package com.hp.myfplus.entity;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MudGlobEntity extends OwnedEntity {
    private static final double ARC = 0.06;
    public Vec3 from = Vec3.ZERO;
    public Vec3 to = Vec3.ZERO;
    @Nullable
    public Consumer<MudGlobEntity> onLand;

    public MudGlobEntity(EntityType<? extends MudGlobEntity> type, Level level) {
        super(type, level);
    }

    public void launch(Vec3 start, Vec3 end, int ticks) {
        from = start;
        to = end;
        life = Math.max(4, ticks);
        setPos(start);
        xo = start.x;
        yo = start.y;
        zo = start.z;
    }

    public Vec3 posAt(float t) {
        float k = Math.min(1f, t / life);
        double arc = 0.5 * ARC * t * Math.max(0, life - t);
        return new Vec3(from.x + (to.x - from.x) * k, from.y + (to.y - from.y) * k + arc, from.z + (to.z - from.z) * k);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        setPos(posAt(tickCount));
        if (!level().isClientSide && tickCount >= life) {
            if (onLand != null) onLand.accept(this);
            discard();
        }
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buf) {
        buf.writeDouble(from.x);
        buf.writeDouble(from.y);
        buf.writeDouble(from.z);
        buf.writeDouble(to.x);
        buf.writeDouble(to.y);
        buf.writeDouble(to.z);
    }

    @Override
    protected void readExtra(FriendlyByteBuf buf) {
        from = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        to = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
