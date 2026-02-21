package com.hbm.blocks.gas;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockGasFlammable extends BlockGasBase {

    public BlockGasFlammable(String s) {
        super(0.8F, 0.8F, 0.2F, s);
    }

    @Override
    public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

        if (world.rand.nextInt(3) == 0)
            return ForgeDirection.getOrientation(world.rand.nextInt(2));

        return this.randomHorizontal(world);
    }

    @Override
    public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
        return this.randomHorizontal(world);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        if (!world.isChunkGeneratedAt(pos.getX() >> 4, pos.getZ() >> 4)) return;

        if(!GeneralConfig.enableFlammableGas){
            world.setBlockToAir(pos);
            return;
        }

        MutableBlockPos posN = new BlockPos.MutableBlockPos();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            posN.setPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
            if (!world.isBlockLoaded(posN)) continue;
            IBlockState b = world.getBlockState(posN);

            if (isFireSource(b)) {
                combust(world, pos);
                return;
            }
        }
        if (rand.nextInt(20) == 0 && world.isAirBlock(pos.down())) {
            world.setBlockToAir(pos);
            return;
        }

        super.updateTick(world, pos, state, rand);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isRemote) return;

        if (!world.isBlockLoaded(pos) || !world.isBlockLoaded(fromPos)) return;

        IBlockState changedState = world.getBlockState(fromPos);
        if (isFireSource(changedState)) combust(world, pos);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if(!GeneralConfig.enableFlammableGas){
            return;
        }

        if (!worldIn.isRemote && entityIn.isBurning()) this.combust(worldIn, pos);
    }

    protected void combust(World world, BlockPos p) {
        world.setBlockState(p, Blocks.FIRE.getDefaultState(), 2);

        MutableBlockPos posN = new BlockPos.MutableBlockPos();
        for (EnumFacing dir : EnumFacing.VALUES) {
            posN.setPos(p.getX() + dir.getXOffset(), p.getY() + dir.getYOffset(), p.getZ() + dir.getZOffset());
            if (world.isBlockLoaded(posN)) {
                IBlockState neighborState = world.getBlockState(posN);
                if (neighborState.getBlock() instanceof BlockGasFlammable) {
                    world.scheduleUpdate(posN, neighborState.getBlock(), 2);
                }
            }
        }
    }

    public boolean isFireSource(IBlockState b) {
        return b.getMaterial() == Material.FIRE || b.getMaterial() == Material.LAVA || b.getBlock() == Blocks.TORCH;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public int getDelay(World world) {
        return world.rand.nextInt(5) + 16;
    }
}