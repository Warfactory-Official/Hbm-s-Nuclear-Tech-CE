package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class MachineDiFurnace extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool EXT = PropertyBool.create("ext");

	private final boolean isActive;
	private static boolean keepInventory;
	
	public MachineDiFurnace(Material materialIn, String s, boolean state) {
		super(materialIn);
		isActive = state;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.machineTab);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(EXT, false));
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.machine_difurnace_off);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityDiFurnace();
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.setDefaultFacing(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!keepInventory)
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityDiFurnace)
            {
                InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityDiFurnace)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }
        }
        super.breakBlock(worldIn, pos, state);
	}
	
	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state){
		if (!worldIn.isRemote)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos.north());
            IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
            IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
            IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
            EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);

            if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock())
            {
                enumfacing = EnumFacing.SOUTH;
            }
            else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock())
            {
                enumfacing = EnumFacing.NORTH;
            }
            else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock())
            {
                enumfacing = EnumFacing.EAST;
            }
            else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock())
            {
                enumfacing = EnumFacing.WEST;
            }

            worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
        }
	}
	
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING, EXT});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() | (state.getValue(EXT) ? 0b1000 : 0);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta & 0b111);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

		boolean ext = (meta & 0b1000) != 0;

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(EXT, ext);
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
	
	public static void updateBlockState(boolean isProcessing, boolean ext, World world, BlockPos pos) {
		IBlockState cur = world.getBlockState(pos);
		TileEntity te = world.getTileEntity(pos);
		Block target = isProcessing ? ModBlocks.machine_difurnace_on : ModBlocks.machine_difurnace_off;
		IBlockState ns = (cur.getBlock() == target) ? cur : target.getDefaultState();
		if (cur.getPropertyKeys().contains(FACING) && ns.getPropertyKeys().contains(FACING))
			ns = ns.withProperty(FACING, cur.getValue(FACING));
		if (ns.getPropertyKeys().contains(EXT)) ns = ns.withProperty(EXT, ext);
		boolean oldKeep = keepInventory;
		keepInventory = true;
		if (ns != cur) {
			world.setBlockState(pos, ns, 2);
		}
		keepInventory = oldKeep;
		if (te != null) {
			te.validate();
			world.setTileEntity(pos, te);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		 if (this.isActive)
	        {
	            EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
	            double x0 = (double)pos.getX() + 0.5D;
	            double y0 = (double)pos.getY() + rand.nextDouble() * 6.0D / 16.0D + 0.2D;
	            double z0 = (double)pos.getZ() + 0.5D;
	            double sideOff = 0.52F;
				double sideRand = rand.nextFloat() * 0.5 - 0.25;
				double xOff = rand.nextFloat() * 0.375 - 0.1875;
				double zOff = rand.nextFloat() * 0.375 - 0.1875;

	            double smokeY = pos.getY() + 1;
				if (worldIn.getBlockState(pos.offset(EnumFacing.UP, 1)).getBlock() == ModBlocks.machine_difurnace_ext) {
					smokeY += 1;
				}

	            if (rand.nextDouble() < 0.1D) {
	                worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
	            }

	            switch (enumfacing)
	            {
	                case WEST:
	                    worldIn.spawnParticle(EnumParticleTypes.FLAME, x0 - sideOff, y0, z0 + sideRand, 0.0D, 0.0D, 0.0D);
	                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x0 + xOff, smokeY, z0 + zOff, 0.0D, 0.0D, 0.0D);
	                    break;
	                case EAST:
	                    worldIn.spawnParticle(EnumParticleTypes.FLAME, x0 + sideOff, y0, z0 + sideRand, 0.0D, 0.0D, 0.0D);
	                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x0 + xOff, smokeY, z0 + zOff, 0.0D, 0.0D, 0.0D);
	                    break;
	                case NORTH:
	                    worldIn.spawnParticle(EnumParticleTypes.FLAME, x0 + sideRand, y0, z0 - sideOff, 0.0D, 0.0D, 0.0D);
	                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x0 + xOff, smokeY, z0 + zOff, 0.0D, 0.0D, 0.0D);
	                    break;
	                case SOUTH:
	                    worldIn.spawnParticle(EnumParticleTypes.FLAME, x0 + sideRand, y0, z0 + sideOff, 0.0D, 0.0D, 0.0D);
						worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x0 + xOff, smokeY, z0 + zOff, 0.0D, 0.0D, 0.0D);
	            default:    
					break;
	            }
	        }
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(ModBlocks.machine_difurnace_off));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
