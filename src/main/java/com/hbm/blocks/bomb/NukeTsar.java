package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityNukeTsar;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import java.util.List;

public class NukeTsar extends BlockContainer implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public NukeTsar(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityNukeTsar();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityNukeTsar entity = (TileEntityNukeTsar) worldIn.getTileEntity(pos);
		if(entity != null && worldIn.isBlockPowered(pos) && !worldIn.isRemote) {
			boolean ready = entity.isReady();
			boolean filled = entity.isFilled();
			if (ready && !filled) {
				this.onPlayerDestroy(worldIn, pos, worldIn.getBlockState(pos));
				entity.clearSlots();
				worldIn.setBlockToAir(pos);
				igniteTestBomb(worldIn, null, pos.getX(), pos.getY(), pos.getZ(), BombConfig.manRadius);
			} else if(filled) {
				this.onPlayerDestroy(worldIn, pos, worldIn.getBlockState(pos));
				entity.clearSlots();
				worldIn.setBlockToAir(pos);
				igniteTestBomb(worldIn, null, pos.getX(), pos.getY(), pos.getZ(), BombConfig.tsarRadius);
			}
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (worldIn.getTileEntity(pos) instanceof TileEntityNukeTsar tsar && placer instanceof EntityPlayerMP playerMP)
			tsar.placerID = placer.getUniqueID();
	}

	public boolean igniteTestBomb(World world, Entity detonator, int x, int y, int z, int r) {
		if(!world.isRemote) {
			world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, world.rand.nextFloat() * 0.1F + 0.9F);
			var mk5 = EntityNukeExplosionMK5.statFac(world, r, x + 0.5, y + 0.5, z + 0.5).setDetonator(detonator);
			if (detonator == null && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityNukeTsar tsar)
				mk5.detonator = tsar.placerID;
			world.spawnEntity(mk5);
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, r);
			}
		}
		return false;
	}

	@Override
	public BombReturnCode explode(World worldIn, BlockPos pos, Entity detonator) {
		if(!worldIn.isRemote) {
			TileEntityNukeTsar entity = (TileEntityNukeTsar) worldIn.getTileEntity(pos);
            if (entity == null || !entity.isReady()) return BombReturnCode.ERROR_MISSING_COMPONENT;
            boolean ready = entity.isReady();
            boolean filled = entity.isFilled();
            if (ready && !filled) {
                this.onPlayerDestroy(worldIn, pos, worldIn.getBlockState(pos));
                entity.clearSlots();
                worldIn.setBlockToAir(pos);
                igniteTestBomb(worldIn, null, pos.getX(), pos.getY(), pos.getZ(), BombConfig.manRadius);
            } else if(filled) {
                this.onPlayerDestroy(worldIn, pos, worldIn.getBlockState(pos));
                entity.clearSlots();
                worldIn.setBlockToAir(pos);
                igniteTestBomb(worldIn, null, pos.getX(), pos.getY(), pos.getZ(), BombConfig.tsarRadius);
            }
			return BombReturnCode.DETONATED;
		}

		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
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

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§6["+ I18nUtil.resolveKey("trait.thermobomb")+"]"+"§r");
		tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", BombConfig.tsarRadius)+"§r");
		if(!BombConfig.disableNuclear){
			tooltip.add("§2["+ I18nUtil.resolveKey("trait.fallout")+"]"+"§r");
			tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", (int)BombConfig.tsarRadius*(1+BombConfig.falloutRange/100))+"§r");
		}
	}
}
