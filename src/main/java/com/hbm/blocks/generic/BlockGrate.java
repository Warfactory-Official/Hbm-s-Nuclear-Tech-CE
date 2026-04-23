package com.hbm.blocks.generic;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockGrate extends Block implements ITooltipProvider {

    public static final PropertyInteger HEIGHT = PropertyInteger.create("height", 0, 9);

    public BlockGrate(Material material, String s) {
        super(material);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HEIGHT, 0));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public double getY(int meta) {
        if(meta == 9) return -0.125D;
        return meta * 0.125D;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int meta = state.getValue(HEIGHT);
        double fy = getY(meta);
        return new AxisAlignedBB(0.0D, fy, 0.0D, 1.0D, fy + 0.125D - (this == ModBlocks.steel_grate_wide ? 0.001D : 0.0D), 1.0D);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        int meta = state.getValue(HEIGHT);
        return (side == EnumFacing.UP && meta == 7) || (side == EnumFacing.DOWN && meta == 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        if(facing == EnumFacing.DOWN) return this.getDefaultState().withProperty(HEIGHT, 7);
        if(facing == EnumFacing.UP) return this.getDefaultState().withProperty(HEIGHT, 0);
        return this.getDefaultState().withProperty(HEIGHT, (int) Math.floor(hitY * 8D));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if(placer.isSneaking()) {
            int meta = state.getValue(HEIGHT);

            if(meta == 0) {
                IBlockState downState = world.getBlockState(pos.down());
                AxisAlignedBB otherBB = downState.getCollisionBoundingBox(world, pos.down());
                if(!downState.getBlock().isAir(downState, world, pos.down()) && (otherBB == null || (otherBB.maxY + pos.down().getY()) - (double)pos.getY() < -0.05)) {
                    world.setBlockState(pos, state.withProperty(HEIGHT, 9), 3);
                }
            } else if(meta == 7) {
                IBlockState upState = world.getBlockState(pos.up());
                AxisAlignedBB otherBB = upState.getCollisionBoundingBox(world, pos.up());
                if(!upState.getBlock().isAir(upState, world, pos.up()) && (otherBB == null || (otherBB.minY + pos.up().getY()) - (double)(pos.getY() + 1) > 0.05)) {
                    world.setBlockState(pos, state.withProperty(HEIGHT, 8), 3);
                }
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if(world.isRemote) return;

        int meta = state.getValue(HEIGHT);
        boolean breakIt = false;

        if(meta == 9) {
            IBlockState downState = world.getBlockState(pos.down());
            AxisAlignedBB otherBB = downState.getCollisionBoundingBox(world, pos.down());
            breakIt = !(!downState.getBlock().isAir(downState, world, pos.down()) && (otherBB == null || (otherBB.maxY + pos.down().getY()) - (double)pos.getY() < -0.05));
        } else if(meta == 8) {
            IBlockState upState = world.getBlockState(pos.up());
            AxisAlignedBB otherBB = upState.getCollisionBoundingBox(world, pos.up());
            breakIt = !(!upState.getBlock().isAir(upState, world, pos.up()) && (otherBB == null || (otherBB.minY + pos.up().getY()) - (double)(pos.getY() + 1) > 0.05));
        }

        if(breakIt) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if(this == ModBlocks.steel_grate_wide && (entityIn instanceof EntityItem || entityIn instanceof EntityXPOrb)) {
            int meta = state.getValue(HEIGHT);
            if(entityIn.posY < pos.getY() + getY(meta) + 0.375) {
                entityIn.motionX = 0;
                entityIn.motionY = -0.25;
                entityIn.motionZ = 0;
                entityIn.setPosition(entityIn.posX, entityIn.posY - 0.125, entityIn.posZ);
            }
            return;
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.translateToLocal(this.getTranslationKey() + ".desc"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HEIGHT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HEIGHT, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HEIGHT);
    }
}