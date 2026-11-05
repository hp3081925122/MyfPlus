package com.hp.myfplus.fx;

import com.hp.myfplus.registry.MyfEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class FxEntity extends Entity implements IEntityAdditionalSpawnData {
    public FxType fx = FxType.NONE;
    public int life = 20;
    public float size = 1;
    public float length = 1;
    public float width = 1;
    public int palette;
    public int follow = -1;
    public double offX, offY, offZ;
    public Object clientCache;

    public FxEntity(EntityType<? extends FxEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static FxEntity at(Level level, FxType fx, double x, double y, double z) {
        FxEntity e = new FxEntity(MyfEntities.FX.get(), level);
        e.fx = fx;
        e.life = fx.life;
        e.setPos(x, y, z);
        e.xo = x;
        e.yo = y;
        e.zo = z;
        return e;
    }

    public static FxEntity at(Level level, FxType fx, Vec3 pos) {
        return at(level, fx, pos.x, pos.y, pos.z);
    }

    public FxEntity life(int ticks) {
        life = ticks;
        return this;
    }

    public FxEntity size(float s) {
        size = s;
        return this;
    }

    public FxEntity line(float len, float wid) {
        length = len;
        width = wid;
        return this;
    }

    public FxEntity yaw(float yaw) {
        setYRot(yaw);
        yRotO = yaw;
        return this;
    }

    public FxEntity palette(int p) {
        palette = p;
        return this;
    }

    public FxEntity follow(Entity e, double ox, double oy, double oz) {
        follow = e.getId();
        offX = ox;
        offY = oy;
        offZ = oz;
        setPos(e.getX() + ox, e.getY() + oy, e.getZ() + oz);
        return this;
    }

    public FxEntity spawn() {
        level().addFreshEntity(this);
        return this;
    }

    public float age(float partial) {
        return tickCount + partial;
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        if (follow >= 0) {
            Entity e = level().getEntity(follow);
            if (e != null && e.isAlive()) setPos(e.getX() + offX, e.getY() + offY, e.getZ() + offZ);
            else if (!level().isClientSide) discard();
        }
        else {
            Vec3 dm = getDeltaMovement();
            if (dm.lengthSqr() > 0) setPos(getX() + dm.x, getY() + dm.y, getZ() + dm.z);
        }
        if (!level().isClientSide && tickCount >= life) discard();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 128 * 128;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeVarInt(fx.ordinal());
        buf.writeVarInt(life);
        buf.writeFloat(size);
        buf.writeFloat(length);
        buf.writeFloat(width);
        buf.writeVarInt(palette);
        buf.writeVarInt(follow);
        buf.writeDouble(offX);
        buf.writeDouble(offY);
        buf.writeDouble(offZ);
        buf.writeFloat(getYRot());
        Vec3 dm = getDeltaMovement();
        buf.writeDouble(dm.x);
        buf.writeDouble(dm.y);
        buf.writeDouble(dm.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        fx = FxType.byId(buf.readVarInt());
        life = buf.readVarInt();
        size = buf.readFloat();
        length = buf.readFloat();
        width = buf.readFloat();
        palette = buf.readVarInt();
        follow = buf.readVarInt();
        offX = buf.readDouble();
        offY = buf.readDouble();
        offZ = buf.readDouble();
        float yaw = buf.readFloat();
        setYRot(yaw);
        yRotO = yaw;
        setDeltaMovement(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
