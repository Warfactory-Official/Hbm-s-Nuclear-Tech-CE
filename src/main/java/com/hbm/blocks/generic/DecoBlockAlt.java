package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.tileentity.deco.TileEntityDecoBlockAlt;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class DecoBlockAlt extends BlockContainer {

	public static PropertyDirection FACING = BlockHorizontal.FACING;

	public static final AxisAlignedBB STATUE_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 8*0.0625F, 1.0F);

	public DecoBlockAlt(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDecoBlockAlt();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.statue_elb);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			ItemStack stack = player.getHeldItem(hand);
			if(!stack.isEmpty())
			{
				if(this == ModBlocks.statue_elb)
				{
					if(stack.getItem() == ModItems.nothing) //gun_revolver_cursed
					{
						world.setBlockState(pos, ModBlocks.statue_elb_g.getDefaultState().withProperty(FACING, state.getValue(FACING)), 2);

                        if (!player.capabilities.isCreativeMode)
                        {
                        	stack.shrink(1);
                        }
						return true;
					}

					if(stack.getItem() == ModItems.watch)
					{
						world.setBlockState(pos, ModBlocks.statue_elb_w.getDefaultState().withProperty(FACING, state.getValue(FACING)), 2);

                        if (!player.capabilities.isCreativeMode)
                        {
                        	stack.shrink(1);
                        }
						return true;
					}
				}
				if(this == ModBlocks.statue_elb_g)
				{
					if(stack.getItem() == ModItems.watch)
					{
						world.setBlockState(pos, ModBlocks.statue_elb_f.getDefaultState().withProperty(FACING, state.getValue(FACING)), 2);

                        if (!player.capabilities.isCreativeMode)
                        {
                            stack.shrink(1);
                        }
						return true;
					}
				}
				if(this == ModBlocks.statue_elb_w)
				{
					if(stack.getItem() == ModItems.nothing) //gun_revolver_cursed
					{
						world.setBlockState(pos, ModBlocks.statue_elb_f.getDefaultState().withProperty(FACING, state.getValue(FACING)), 2);

                        if (!player.capabilities.isCreativeMode)
                        {
                            stack.shrink(1);
                        }
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return STATUE_BOX;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}



	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}
}
