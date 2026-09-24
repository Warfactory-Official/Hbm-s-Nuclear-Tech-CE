package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockForgottenLock extends Block implements ICustomBlockItem {

	public static final int META_DEFAULT = 0;
	public static final int META_BW = 1;
	public static final int META_NULLSTONE = 2;
	public static final int META_THE_BLOCK_THAT_KILLS_YOU = 3;

	public static final int SUB_COUNT = 3;

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 3);

	public BlockForgottenLock(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, META_DEFAULT));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, Math.abs(meta) % 4);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack held = player.getHeldItem(hand);
		if(held.isEmpty())
			return false;

		boolean cracked = held.getItem() == ModItems.key_red_cracked;

		if((held.getItem() != ModItems.key_red && !cracked) || facing.getAxis() == EnumFacing.Axis.Y)
			return false;

		if(cracked) held.shrink(1);
		if(world.isRemote) return true;

		generate(world, pos, facing);

		world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
		return true;
	}

	public static void generate(World world, BlockPos pos, EnumFacing dir) {

		EnumFacing rot = dir.rotateY();
		IBlockState brick = ModBlocks.brick_forgotten.getDefaultState();
		IBlockState air = Blocks.AIR.getDefaultState();

		int len = 15;
		for(int w = -2; w <= 2; w++) for(int h = -2; h <= 2; h++) for(int d = 0; d < len; d++) {
			boolean shell = w == -2 || w == 2 || h == -2 || h == 2 || d == len - 1;
			BlockPos target = pos.add(-dir.getXOffset() * d + rot.getXOffset() * w, h, -dir.getZOffset() * d + rot.getZOffset() * w);
			world.setBlockState(target, shell ? brick : air);
		}
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new BlockForgottenLockItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	private static class BlockForgottenLockItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {

		private BlockForgottenLockItem(Block block) {
			super(block);
		}

		@Override
		public int getMetadata(int damage) {
			return damage;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
			if(this.isInCreativeTab(tab)) {
				for(int i = 0; i < SUB_COUNT; i++) {
					list.add(new ItemStack(this, 1, i));
				}
			}
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void registerModels() {
			ResourceLocation loc = this.block.getRegistryName();
			for(int meta = 0; meta < 4; meta++) {
				ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(loc, "meta=" + meta));
			}
		}
	}
}
