package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LaunchPadRusted extends BlockDummyable implements IBomb {

    public LaunchPadRusted(Material mat, String s) {
        super(mat, s);
        this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D));
        this.bounding.add(new AxisAlignedBB(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D));
        this.bounding.add(new AxisAlignedBB(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D));
        this.bounding.add(new AxisAlignedBB(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D));
        this.bounding.add(new AxisAlignedBB(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D));
        this.bounding.add(new AxisAlignedBB(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityLaunchPadRusted();
        return null;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

        if(!world.isRemote) {

            int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
            if(corePos != null){
                TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
                if(core instanceof TileEntityLaunchPadRusted entity){
                    return entity.launch();
                }
            }
        }

        return BombReturnCode.UNDEFINED;
    }

    @Override
    public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        if(!world.isRemote){

            int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
            if(corePos != null){
                TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
                if(core instanceof TileEntityLaunchPadRusted launchpad){
                    launchpad.updateRedstonePower(pos);
                }
            }
        }
        super.neighborChanged(state, world, pos, blockIn, fromPos);
    }
}
