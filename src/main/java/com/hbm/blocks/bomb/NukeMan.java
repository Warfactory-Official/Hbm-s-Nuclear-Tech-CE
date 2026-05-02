package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityNukeMan;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import java.util.List;
import java.util.Random;

public class NukeMan extends BlockContainer implements IBomb {

	public static final PropertyInteger FACING = PropertyInteger.create("facing", 2, 5);
	
	private static boolean keepInventory = false;
	
	public NukeMan(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.nukeTab);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityNukeMan();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.nuke_man);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!keepInventory)
        {
			TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityNukeMan)
            {
                InventoryHelper.dropInventoryItems(world, pos, (TileEntityNukeMan)tileentity);
                
                world.updateComparatorOutputLevel(pos, this);
            }
        }
		super.breakBlock(world, pos, state);
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
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityNukeMan entity = (TileEntityNukeMan) world.getTileEntity(pos);
        if (world.isBlockPowered(pos) && !world.isRemote)
        {
        	
        	if(entity.isReady())
        	{
        		this.onPlayerDestroy(world, pos, state);
            	entity.clearSlots();
            	world.setBlockToAir(pos);
            	igniteTestBomb(world, null, pos.getX(), pos.getY(), pos.getZ());
        	}
        }
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
	
	
	public boolean igniteTestBomb(World world, Entity detonator, int x, int y, int z)
	{
		if (!world.isRemote) {

			if(world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityNukeMan)
				((TileEntityNukeMan)world.getTileEntity(new BlockPos(x, y, z))).clearSlots();
			world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, world.rand.nextFloat() * 0.1F + 0.9F);
			EntityNukeExplosionMK5 mk5 = EntityNukeExplosionMK5.statFac(world, BombConfig.manRadius, x + 0.5, y + 0.5, z + 0.5).setDetonator(detonator);
			if (detonator == null && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityNukeMan man)
				mk5.detonator = man.placerID;
	    	world.spawnEntity(mk5);

	    	if (BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.manRadius);
			}
		}
    	
		return false;
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		
		if(i == 0)
		{
			return this.getDefaultState().withProperty(FACING, 5);
		}
		if(i == 1)
		{
			return this.getDefaultState().withProperty(FACING, 3);
		}
		if(i == 2)
		{
			return this.getDefaultState().withProperty(FACING, 4);
		}
		return this.getDefaultState().withProperty(FACING, 2);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		if (world.getTileEntity(pos) instanceof TileEntityNukeMan man)
			man.placerID = player.getUniqueID();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if(meta >= 2 && meta <=5)
			return this.getDefaultState().withProperty(FACING, meta);
		return this.getDefaultState().withProperty(FACING, 2);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

		if (!world.isRemote) {
			if (!(world.getTileEntity(pos) instanceof TileEntityNukeMan))
				return BombReturnCode.UNDEFINED;
			TileEntityNukeMan entity = (TileEntityNukeMan) world.getTileEntity(pos);
			//if (p_149695_1_.getStrongPower(x, y, z))
			if (entity.isReady()) {
				this.onPlayerDestroy(world, pos, world.getBlockState(pos));
				entity.clearSlots();
				world.setBlockToAir(pos);
				igniteTestBomb(world, detonator, pos.getX(), pos.getY(), pos.getZ());
				return BombReturnCode.DETONATED;
			}

			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}

		return BombReturnCode.UNDEFINED;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§2["+ I18nUtil.resolveKey("trait.nuclearbomb")+"]"+"§r");
		tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", BombConfig.manRadius)+"§r");
		if(!BombConfig.disableNuclear){
			tooltip.add("§2["+ I18nUtil.resolveKey("trait.fallout")+"]"+"§r");
			tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", (int)BombConfig.manRadius*(1+BombConfig.falloutRange/100))+"§r");
		}
	}
}
