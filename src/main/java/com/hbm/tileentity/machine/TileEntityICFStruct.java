package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineICF;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityICFStruct extends TileEntity implements ITickable {

    @Override
    public void update() {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() % 20 != 0) return;

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

        for (int i = -8; i <= 8; i++) {

            if (!cbarp(ModBlocks.icf_component, 0, 1, 0, i, dir)) return;
            if (i != 0) if (!cbarp(ModBlocks.icf_component, 0, 0, 0, i, dir)) return;
            if (!cbarp(ModBlocks.icf_component, 0, -1, 0, i, dir)) return;
            if (!cbarp(ModBlocks.icf_component, 2, 0, 3, i, dir)) return;

            for (int j = -1; j <= 1; j++) if (!cbarp(ModBlocks.icf_component, Math.abs(i) <= 2 ? 2 : 4, j, 1, i, dir)) return;
            for (int j = -2; j <= 2; j++) if (!cbarp(ModBlocks.icf_component, Math.abs(i) <= 2 ? 2 : 4, j, 2, i, dir)) return;
            for (int j = -2; j <= 2; j++) if (j != 0) if (!cbarp(ModBlocks.icf_component, Math.abs(i) <= 2 ? 2 : 4, j, 3, i, dir)) return;
            for (int j = -2; j <= 2; j++) if (!cbarp(ModBlocks.icf_component, Math.abs(i) <= 2 ? 2 : 4, j, 4, i, dir)) return;
            for (int j = -1; j <= 1; j++) if (!cbarp(ModBlocks.icf_component, Math.abs(i) <= 2 ? 2 : 4, j, 5, i, dir)) return;
        }

        BlockDummyable.safeRem = true;
        world.setBlockState(pos, ModBlocks.icf.getDefaultState().withProperty(MachineICF.META, this.getBlockMetadata() + BlockDummyable.offset), 3);
        ((MachineICF) ModBlocks.icf).fillSpace(world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir,
                -((MachineICF) ModBlocks.icf).getOffset());
        BlockDummyable.safeRem = false;
    }

    /**
     * check block at relative position
     */
    public boolean cbarp(Block block, int meta, int widthwiseOffset, int y, int lengthwiseOffset, ForgeDirection dir) {
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        int ix = pos.getX() + rot.offsetX * lengthwiseOffset + dir.offsetX * widthwiseOffset;
        int iy = pos.getY() + y;
        int iz = pos.getZ() + rot.offsetZ * lengthwiseOffset + dir.offsetZ * widthwiseOffset;
        IBlockState state = world.getBlockState(new BlockPos(ix, iy, iz));
        return state.getBlock() == block && block.getMetaFromState(state) == meta;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
