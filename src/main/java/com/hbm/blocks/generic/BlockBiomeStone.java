package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnums.EnumBiomeType;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IModelRegister;
import com.hbm.util.EnumUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;
import java.util.Random;

public class BlockBiomeStone extends Block implements ICustomBlockItem {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, EnumBiomeType.VALUES.length - 1);
	public static final PropertyBool COVERED = PropertyBool.create("covered");

	public BlockBiomeStone(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0).withProperty(COVERED, false));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META, COVERED);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, Math.abs(meta) % EnumBiomeType.VALUES.length);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public IBlockState getActualState(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) {
		IBlockState above = world.getBlockState(pos.up());
		boolean covered = above.getBlock() == this && above.getValue(META).equals(state.getValue(META));
		return state.withProperty(COVERED, covered);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new BlockBiomeStoneItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	private static class BlockBiomeStoneItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {

		private BlockBiomeStoneItem(Block block) {
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
				for(int i = 0; i < EnumBiomeType.VALUES.length; i++) {
					list.add(new ItemStack(this, 1, i));
				}
			}
		}

		@Override
		public String getTranslationKey(ItemStack stack) {
			EnumBiomeType type = EnumUtil.grabEnumSafely(EnumBiomeType.VALUES, stack.getMetadata());
			return this.block.getTranslationKey() + "." + type.name().toLowerCase(Locale.US);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void registerModels() {
			ResourceLocation loc = this.block.getRegistryName();
			for(int meta = 0; meta < EnumBiomeType.VALUES.length; meta++) {
				ModelLoader.setCustomModelResourceLocation(this, meta,
						new ModelResourceLocation(loc, "covered=false,meta=" + meta));
			}
		}
	}
}
