package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.*;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import java.util.List;
import java.util.Random;

public class BlockStorageCrate extends BlockContainer {
    public static final String CRATE_RAD_KEY = "cRads";
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockStorageCrate(Material materialIn, String s){
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.METAL);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(this == ModBlocks.crate_iron)
			return new TileEntityCrateIron();
		if(this == ModBlocks.crate_steel)
			return new TileEntityCrateSteel();
		if(this == ModBlocks.crate_tungsten)
			return new TileEntityCrateTungsten();
		if(this == ModBlocks.crate_desh)
			return new TileEntityCrateDesh();
		if(this == ModBlocks.crate_template)
			return new TileEntityCrateTemplate();
		if(this == ModBlocks.safe)
			return new TileEntitySafe();
		return null;
	}

	public int getSlots(){
		if(this == ModBlocks.crate_iron)
			return 36;
		if(this == ModBlocks.crate_steel)
			return 54;
		if(this == ModBlocks.crate_tungsten)
			return 27;
		if(this == ModBlocks.crate_desh)
			return 104;
		if(this == ModBlocks.crate_template)
			return 27;
		if(this == ModBlocks.safe)
			return 15;
		return 0;
	}


	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player){
		return true;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
		IPersistentNBT.breakBlock(worldIn, pos, state);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) { }

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		IPersistentNBT.onBlockHarvested(worldIn, pos, player);
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(!player.getHeldItemMainhand().isEmpty() && (player.getHeldItemMainhand().getItem() instanceof ItemLock || player.getHeldItemMainhand().getItem() == ModItems.key_kit)) {
			return false;

		} else if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(pos);
			if(entity instanceof TileEntityCrateIron && ((TileEntityCrateIron)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			if(entity instanceof TileEntityCrateSteel && ((TileEntityCrateSteel)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			if(entity instanceof TileEntityCrateTungsten && ((TileEntityCrateTungsten)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			if(entity instanceof TileEntityCrateTemplate && ((TileEntityCrateTemplate)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			if(entity instanceof TileEntityCrateDesh && ((TileEntityCrateDesh)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			if(entity instanceof TileEntitySafe && ((TileEntitySafe)entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){

		IPersistentNBT.onBlockPlacedBy(world, pos, stack);
		if(this == ModBlocks.safe)
			world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

		if(enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, list, flagIn);
		int totalSlots = getSlots();
		if(stack.hasTagCompound()){
			NBTTagCompound nbt = stack.getTagCompound();
			NBTTagCompound crateData = nbt.hasKey(IPersistentNBT.NBT_PERSISTENT_KEY) ? nbt.getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY) : nbt;
			int slotCount = 0;
			for(int i=0; i<totalSlots; i++){
				if(crateData.hasKey("slot"+i)){
					slotCount++;
				}
			}
			float percent = Library.roundFloat(slotCount * 100F/totalSlots, 1);
			String color = "§e";
			String color2 = "§6";
			if(percent >= 75){
				color = "§c";
				color2 = "§4";
			}else if(percent < 25){
				color = "§a";
				color2 = "§2";
			}
			list.add(color+slotCount+color2+"/"+totalSlots+" Slots used "+color+"("+percent+"%)§r");

		}else{
			list.add("§a0§2/" + totalSlots + " Slots used §a(0.0%)§r");
		}
	}
}
