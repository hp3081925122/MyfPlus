package com.hp.myfplus.entity;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public abstract class OwnedEntity extends Entity implements IEntityAdditionalSpawnData {
    public int ownerId = -1;
    @Nullable
    protected Mob owner;
    public int life = 100;
    public float damage;
    public Object clientCache;

    protected OwnedEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public void setOwner(Mob mob) {
        owner = mob;
        ownerId = mob.getId();
    }

    @Nullable
    public Mob owner() {
        if (owner != null && !owner.isRemoved()) return owner;
        if (ownerId >= 0 && level().getEntity(ownerId) instanceof Mob m) {
            owner = m;
            return m;
        }
        return null;
    }

    public float age(float partial) {
        return tickCount + partial;
    }

    protected void writeExtra(FriendlyByteBuf buf) {
    }

    protected void readExtra(FriendlyByteBuf buf) {
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        if (!level().isClientSide && tickCount >= life) expire();
    }

    protected void expire() {
        discard();
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
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 128 * 128;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeVarInt(ownerId);
        buf.writeVarInt(life);
        writeExtra(buf);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        ownerId = buf.readVarInt();
        life = buf.readVarInt();
        readExtra(buf);
    }
}
