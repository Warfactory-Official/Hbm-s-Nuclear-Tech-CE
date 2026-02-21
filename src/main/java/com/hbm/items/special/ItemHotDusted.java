package com.hbm.items.special;

import com.hbm.Tags;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemHotDusted extends ItemHot {

	public ItemHotDusted(int heat, String s){
		super(heat, s);
		this.setHasSubtypes(true);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		tooltip.add(I18nUtil.resolveKey("item.hot_dusted.forged", stack.getItemDamage()));
	}
	
	public static int getMaxHeat(ItemStack stack) {
		return heat - stack.getItemDamage() * 10;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		super.registerModel();
		ModelResourceLocation mrl = new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath), "inventory");
		for (int meta = 0; meta <= 9; meta++) {
			ModelLoader.setCustomModelResourceLocation(this, meta, mrl);
		}
	}

}
