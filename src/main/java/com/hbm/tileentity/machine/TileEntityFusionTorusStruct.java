package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.fusion.MachineFusionTorus;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityFusionTorusStruct extends TileEntity implements ITickable {

    @Override
    public void update() {

        if(world.isRemote) return;
        if(world.getTotalWorldTime() % 20 != 0) return;

        for(int y = 0; y < 5; y++) {
            for(int x = 0; x < MachineFusionTorus.layout[0].length; x++) {
                for(int z = 0; z < MachineFusionTorus.layout[0][0].length; z++) {

                    int ly = y > 2 ? 4 - y : y;
                    int i = MachineFusionTorus.layout[ly][x][z];

                    if(i == 0) continue; // ignore air
                    if(x == 7 && y == 0 && z == 7) continue; // ignore core component position
                    if(!cbr(ModBlocks.fusion_component, i, x - 7, y, z - 7)) return;
                }
            }
        }

        MachineFusionTorus block = (MachineFusionTorus) ModBlocks.fusion_torus;
        BlockDummyable.safeRem = true;
        world.setBlockState(pos, ModBlocks.fusion_torus.getDefaultState().withProperty(BlockDummyable.META, 12), 3);
        block.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.NORTH, 0);
        BlockDummyable.safeRem = false;
    }

    /** [G]et [B]lock at [R]elative position */
    private Block gbr(int x, int y, int z) {
        return world.getBlockState(pos.add(x, y, z)).getBlock();
    }

    /** [G]et [M]eta at [R]elative position */
    private int gmr(int x, int y, int z) {
        IBlockState state = world.getBlockState(pos.add(x, y, z));
        return state.getBlock().getMetaFromState(state);
    }

    /** [C]heck [B]lock at [R]elative position */
    private boolean cbr(Block b, int meta, int x, int y, int z) {
        return b == gbr(x, y, z) && meta == gmr(x, y, z);
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 7,
                    pos.getY(),
                    pos.getZ() - 7,
                    pos.getX() + 8,
                    pos.getY() + 5,
                    pos.getZ() + 8
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

}
