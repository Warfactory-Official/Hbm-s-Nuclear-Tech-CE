package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnums.PlatemetalType;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IModelRegister;
import com.hbm.util.EnumUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

public class BlockPlatemetal extends Block implements ICustomBlockItem {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, PlatemetalType.VALUES.length - 1);

	public BlockPlatemetal(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, Math.abs(meta) % PlatemetalType.VALUES.length);
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
	public void registerItem() {
		ItemBlock itemBlock = new BlockPlatemetalItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	private static class BlockPlatemetalItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {

		private BlockPlatemetalItem(Block block) {
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
				for(int i = 0; i < PlatemetalType.VALUES.length; i++) {
					list.add(new ItemStack(this, 1, i));
				}
			}
		}

		@Override
		public String getTranslationKey(ItemStack stack) {
			PlatemetalType type = EnumUtil.grabEnumSafely(PlatemetalType.VALUES, stack.getMetadata());
			return this.block.getTranslationKey() + "." + type.name().toLowerCase(Locale.US);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void registerModels() {
			ResourceLocation loc = this.block.getRegistryName();
			for(int meta = 0; meta < PlatemetalType.VALUES.length; meta++) {
				ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(loc, "meta=" + meta));
			}
		}
	}
}
