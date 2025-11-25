package com.hbm.items.special;

import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;

import java.util.List;

public class ItemPolaroid extends Item implements IDynamicModels {

	public ItemPolaroid(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.consumableTab);
		
		ModItems.ALL_ITEMS.add(this);
        IDynamicModels.INSTANCES.add(this);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
		if(entity instanceof EntityPlayer)
    		if(((EntityPlayer)entity).getHealth() < 10F) {
    			((EntityPlayer) entity).addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 10, 2));
    		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add("Fate chosen");
		list.add("");
		switch(MainRegistry.polaroidID) {
		case 1: 
			list.add("...");
			break;
		case 2: 
			list.add("Clear as glass.");
			break;
		case 3: 
			list.add("'M");
			break;
		case 4: 
			list.add("It's about time.");
			break;
		case 5: 
			list.add("If you stare long into the abyss, the abyss stares back.");
			break;
		case 6: 
			list.add("public Party celebration = new Party();");
			break;
		case 7: 
			list.add("V urnerq lbh yvxr EBG13!");
			break;
		case 8: 
			list.add("11011100");
			break;
		case 9: 
			list.add("Vg'f nobhg gvzr.");
			break;
		case 10: 
			list.add("Schrabidium dislikes the breeding reactor.");
			break;
		case 11: 
			list.add("yss stares back.6public Party cel");
			break;
		case 12: 
			list.add("Red streaks.");
			break;
		case 13: 
			list.add("Q1");
			break;
		case 14: 
			list.add("Q4");
			break;
		case 15: 
			list.add("Q3");
			break;
		case 16: 
			list.add("Q2");
			break;
		case 17: 
			list.add("Two friends before christmas.");
			break;
		case 18: 
			list.add("Duchess of the boxcars.");
			list.add("");
			list.add("\"P.S.: Thirty-one.\"");
			list.add("\"Huh, what does thirty-one mean?\"");
			break;
		}
	}

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName() + "_" + MainRegistry.polaroidID, "inventory"));

    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}
