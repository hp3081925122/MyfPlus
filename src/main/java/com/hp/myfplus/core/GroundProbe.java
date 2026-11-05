package com.hp.myfplus.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GroundProbe {
    private GroundProbe() {
    }

    public static double top(BlockGetter level, double x, double z, double refY, int up, int down) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        int start = (int) Math.floor(refY) + up;
        int end = (int) Math.floor(refY) - down;
        for (int y = start; y >= end; y--) {
            pos.set(bx, y, bz);
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) return y + fluid.getHeight(level, pos);
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) return y + shape.max(Direction.Axis.Y);
        }
        return Double.NaN;
    }

    public static double under(BlockGetter level, double x, double z, double refY) {
        double y = top(level, x, z, refY, 1, 12);
        return Double.isNaN(y) ? refY : y;
    }

    public static double topOr(BlockGetter level, double x, double z, double refY, double fallback) {
        double y = top(level, x, z, refY, 1, 12);
        return Double.isNaN(y) ? fallback : y;
    }
}
