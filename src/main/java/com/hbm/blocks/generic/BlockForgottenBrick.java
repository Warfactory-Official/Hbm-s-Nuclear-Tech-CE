package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockForgottenBrick extends Block implements ICustomBlockItem {

	public static final int META_DEFAULT = 0;
	public static final int META_BW = 1;
	public static final int META_NULLSTONE = 2;
	public static final int META_HOLE = 3;
	public static final int META_HOLE_EMPTY = 4;
	public static final int META_NULLROOM_WOOD = 5;
	public static final int META_NULLROOM_STONE = 6;

	public static final int SUB_COUNT = 7;

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, SUB_COUNT - 1);

	public BlockForgottenBrick(Material materialIn, String s) {
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
		return this.getDefaultState().withProperty(META, Math.abs(meta) % SUB_COUNT);
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

		if(state.getValue(META) != META_HOLE)
			return false;

		if(!player.getHeldItem(hand).isEmpty())
			return false;

		player.setHeldItem(hand, new ItemStack(ModItems.coal_eternal));
		world.setBlockState(pos, state.withProperty(META, META_HOLE_EMPTY), 3);
		return true;
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new BlockForgottenBrickItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	private static class BlockForgottenBrickItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {

		private BlockForgottenBrickItem(Block block) {
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
			for(int meta = 0; meta < SUB_COUNT; meta++) {
				ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(loc, "meta=" + meta));
			}
		}
	}
}
