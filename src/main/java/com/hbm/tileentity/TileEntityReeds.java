package com.hbm.tileentity;

import com.hbm.config.ClientConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.chunk.SectionGeometry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityReeds extends TileEntity {
    private AxisAlignedBB bb;
    private int depth = 1;
    private long lastDepthCheck = Long.MIN_VALUE;

    public int getDepth() {
        if (!ClientConfig.RENDER_REEDS.get()) {
            return 1;
        }

        long time = world.getTotalWorldTime();

        if (lastDepthCheck == Long.MIN_VALUE || time - lastDepthCheck >= 20) {
            lastDepthCheck = time;
            calculateDepth();
        }

        return depth;
    }

    private void calculateDepth() {
        int newDepth = 1;

        BlockPos.MutableBlockPos mutablePos =
                new BlockPos.MutableBlockPos(pos.getX(), pos.getY() - 1, pos.getZ());

        while (mutablePos.getY() >= 0) {
            IBlockState state = world.getBlockState(mutablePos);
            Block block = state.getBlock();

            if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                break;
            }

            newDepth++;
            mutablePos.setY(mutablePos.getY() - 1);
        }

        if (newDepth != depth) {
            depth = newDepth;
            invalidateRenderBB();
            SectionGeometry.renderBoundsChanged(this);
        }
    }

    public void invalidateRenderBB() {
        bb = null;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, pos.getX(), pos.getY() - depth + 1, pos.getZ());
        }

        return bb;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        invalidateRenderBB();
    }

    @Override
    public void invalidate() {
        invalidateRenderBB();
        super.invalidate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
