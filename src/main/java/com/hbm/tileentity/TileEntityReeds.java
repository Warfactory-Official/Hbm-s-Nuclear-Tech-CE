package com.hbm.tileentity;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.block.Block;
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

    public void invalidateRenderBB() {
        bb = null;
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            int depth = 1;
            if (world != null) {
                for (int i = pos.getY() - 1; i > 0; i--) {
                    Block block = world.getBlockState(new BlockPos(pos.getX(), i, pos.getZ())).getBlock();
                    depth = pos.getY() - i;
                    if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) break;
                }
            }
            bb = new AxisAlignedBB(pos.getX(), pos.getY() - depth, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        }
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
