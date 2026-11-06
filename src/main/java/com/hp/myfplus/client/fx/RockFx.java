package com.hp.myfplus.client.fx;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hp.myfplus.core.Combat;
import com.hp.myfplus.fx.Fx;
import com.hp.myfplus.fx.FxEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class RockFx {
    private static final int STRIDE = 8;
    private final float[] data;
    private final BlockState[] states;
    private final int[] lights;
    private final boolean burst;

    private RockFx(float[] data, BlockState[] states, int[] lights, boolean burst) {
        this.data = data;
        this.states = states;
        this.lights = lights;
        this.burst = burst;
    }

    private static final class Builder {
        final Level level;
        final double cx, cy, cz;
        final FloatArrayList d = new FloatArrayList();
        final List<BlockState> s = new ArrayList<>();
        final IntArrayList l = new IntArrayList();

        Builder(Level level, FxEntity e) {
            this.level = level;
            cx = e.getX();
            cy = e.getY();
            cz = e.getZ();
        }

        void add(BlockPos p, float delay, float vx, float vy, float vz, float seed) {
            d.add((float) (p.getX() - cx));
            d.add((float) (p.getY() - cy));
            d.add((float) (p.getZ() - cz));
            d.add(delay);
            d.add(vx);
            d.add(vy);
            d.add(vz);
            d.add(seed);
            s.add(level.getBlockState(p));
            l.add(LevelRenderer.getLightColor(level, p.above()));
        }

        RockFx build(boolean burst) {
            return new RockFx(d.toFloatArray(), s.toArray(new BlockState[0]), l.toIntArray(), burst);
        }
    }

    @Nullable
    private static BlockPos surface(Level level, int bx, int bz, double refY) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int top = Mth.floor(refY) + 1;
        for (int y = top; y >= top - 5; y--) {
            p.set(bx, y, bz);
            BlockState st = level.getBlockState(p);
            if (st.isAir() || !st.getFluidState().isEmpty() || st.getCollisionShape(level, p).isEmpty()) continue;
            if (st.getRenderShape() != RenderShape.MODEL) return null;
            return p.immutable();
        }
        return null;
    }

    private static float seed(int a, int b) {
        int h = a * 0x9E3779B1 + b * 0x85EBCA6B;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65536f * Mth.TWO_PI;
    }

    public static RockFx ring(Level level, FxEntity e) {
        Builder b = new Builder(level, e);
        float r = e.size, sp = Math.max(0.05f, e.speed);
        for (int bx = Mth.floor(b.cx - r); bx <= Mth.floor(b.cx + r); bx++) {
            for (int bz = Mth.floor(b.cz - r); bz <= Mth.floor(b.cz + r); bz++) {
                double dist = Math.hypot(bx + 0.5 - b.cx, bz + 0.5 - b.cz);
                if (dist > r || dist < 1.2) continue;
                BlockPos p = surface(level, bx, bz, b.cy);
                if (p != null) b.add(p, (float) (dist / sp), 0, 0, 0, seed(bx, bz));
            }
        }
        return b.build(false);
    }

    public static RockFx line(Level level, FxEntity e) {
        Builder b = new Builder(level, e);
        Vec3 dir = Combat.dirFromYaw(e.getYRot());
        double len = e.length, hw = e.width / 2 + 0.3, reach = len + hw + 1;
        float sp = Math.max(0.05f, e.speed);
        for (int bx = Mth.floor(b.cx - reach); bx <= Mth.floor(b.cx + reach); bx++) {
            for (int bz = Mth.floor(b.cz - reach); bz <= Mth.floor(b.cz + reach); bz++) {
                double lx = bx + 0.5 - b.cx, lz = bz + 0.5 - b.cz;
                double f = lx * dir.x + lz * dir.z;
                double side = lx * dir.z - lz * dir.x;
                if (f < 0 || f > len || Math.abs(side) > hw) continue;
                BlockPos p = surface(level, bx, bz, b.cy);
                if (p != null) b.add(p, (float) (f / sp), 0, 0, 0, seed(bx, bz));
            }
        }
        return b.build(false);
    }

    public static RockFx burst(Level level, FxEntity e) {
        Builder b = new Builder(level, e);
        RandomSource r = RandomSource.create(e.getId() * 341873128712L + 132897987541L);
        for (int i = 0; i < e.count; i++) {
            double a = r.nextDouble() * Mth.TWO_PI;
            double off = r.nextDouble() * e.size;
            BlockPos p = surface(level, Mth.floor(b.cx + Math.cos(a) * off), Mth.floor(b.cz + Math.sin(a) * off), b.cy);
            if (p == null) continue;
            float hs = 0.12f + r.nextFloat() * 0.22f;
            b.add(p, 0, (float) Math.cos(a) * hs, 0.32f + r.nextFloat() * 0.3f, (float) Math.sin(a) * hs, r.nextFloat() * Mth.TWO_PI);
        }
        return b.build(true);
    }

    public void draw(PoseStack ps, MultiBufferSource buf, BlockRenderDispatcher br, float age, int life) {
        for (int i = 0, n = states.length; i < n; i++) {
            int o = i * STRIDE;
            float x = data[o], y = data[o + 1], z = data[o + 2], seed = data[o + 7];
            ps.pushPose();
            if (!burst) {
                float t = (age - data[o + 3]) / Fx.POP_TICKS;
                if (t < 0 || t > 1) {
                    ps.popPose();
                    continue;
                }
                float k = Mth.sin(t * Mth.PI);
                ps.translate(x + 0.5, y + 0.5 + k * 0.85, z + 0.5);
                ps.mulPose(Axis.XP.rotationDegrees(k * 14f * Mth.cos(seed)));
                ps.mulPose(Axis.ZP.rotationDegrees(k * 14f * Mth.sin(seed)));
                ps.scale(0.98f, 0.98f, 0.98f);
            }
            else {
                float py = y + 1.0f + data[o + 5] * age - 0.035f * age * age;
                if (py < y - 1.5f) {
                    ps.popPose();
                    continue;
                }
                float s = 0.45f * Math.min(1f, (life - age) / 6f);
                if (s <= 0.02f) {
                    ps.popPose();
                    continue;
                }
                float spin = age * (14f + seed * 3f);
                ps.translate(x + 0.5 + data[o + 4] * age, py, z + 0.5 + data[o + 6] * age);
                ps.mulPose(Axis.XP.rotationDegrees(spin));
                ps.mulPose(Axis.ZP.rotationDegrees(spin * 0.7f));
                ps.scale(s, s, s);
            }
            ps.translate(-0.5, -0.5, -0.5);
            br.renderSingleBlock(states[i], ps, buf, lights[i], OverlayTexture.NO_OVERLAY);
            ps.popPose();
        }
    }
}
