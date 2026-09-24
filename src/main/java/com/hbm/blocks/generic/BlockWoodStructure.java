package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IModelRegister;
import com.hbm.util.EnumUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

public class BlockWoodStructure extends Block implements ICustomBlockItem {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, EnumWoodStructure.VALUES.length - 1);

	private static final AxisAlignedBB ROOF_BOX = new AxisAlignedBB(0, 0, 0, 1, 0.1875D, 1);
	private static final AxisAlignedBB SCAFFOLD_BOX = new AxisAlignedBB(0.0625D, 0, 0.0625D, 0.9375D, 1, 0.9375D);
	private static final AxisAlignedBB CEILING_BOX = new AxisAlignedBB(0, 0.875D, 0, 1, 1, 1);

	public BlockWoodStructure(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, Math.abs(meta) % EnumWoodStructure.VALUES.length);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state);
	}

	private EnumWoodStructure typeOf(IBlockState state) {
		return EnumUtil.grabEnumSafely(EnumWoodStructure.VALUES, state.getValue(META));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return switch(typeOf(state)) {
			case ROOF -> ROOF_BOX;
			case SCAFFOLD -> SCAFFOLD_BOX;
			case CEILING -> CEILING_BOX;
		};
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return typeOf(state) == EnumWoodStructure.SCAFFOLD;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		if(typeOf(state) == EnumWoodStructure.SCAFFOLD && face == EnumFacing.UP)
			return BlockFaceShape.SOLID;
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new BlockWoodStructureItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	private static class BlockWoodStructureItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {

		private BlockWoodStructureItem(Block block) {
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
				for(int i = 0; i < EnumWoodStructure.VALUES.length; i++) {
					list.add(new ItemStack(this, 1, i));
				}
			}
		}

		@Override
		public String getTranslationKey(ItemStack stack) {
			EnumWoodStructure type = EnumUtil.grabEnumSafely(EnumWoodStructure.VALUES, stack.getMetadata());
			return this.block.getTranslationKey() + "." + type.name().toLowerCase(Locale.US);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void registerModels() {
			ResourceLocation loc = this.block.getRegistryName();
			for(int meta = 0; meta < EnumWoodStructure.VALUES.length; meta++) {
				ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(loc, "meta=" + meta));
			}
		}
	}

	public enum EnumWoodStructure {
		ROOF, SCAFFOLD, CEILING;

		public static final EnumWoodStructure[] VALUES = values();
	}
}
