package com.hbm.items.special;

import com.hbm.Tags;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemSiegeCoin extends Item implements IDynamicModels {
	
	public ItemSiegeCoin(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.hasSubtypes = true;
		this.setMaxDamage(0);
		
		ModItems.ALL_ITEMS.add(this);
        IDynamicModels.INSTANCES.add(this);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.UNCOMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab())
			for(int i = 0; i < SiegeTier.getLength(); i++) {
				items.add(new ItemStack(this, 1, i));
			}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		tooltip.add(TextFormatting.YELLOW + "Tier " + (stack.getItemDamage() + 1));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
        for (int i = 0; i < SiegeTier.getLength(); i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(Tags.MODID + ":coin_siege_" + SiegeTier.tiers[i].name, "inventory"));
        }
    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}