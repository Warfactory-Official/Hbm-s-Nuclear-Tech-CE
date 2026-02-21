package com.hbm.items.tool;

import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemGasCanister extends Item {

	public static final ModelResourceLocation gasCanisterFullModel = new ModelResourceLocation(
			Tags.MODID + ":gas_full", "inventory");
	

	public ItemGasCanister(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		String s = (I18n.format(this.getTranslationKey() + ".name")).trim();
		String s1 = (I18n.format(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

		if(s1 != null) {
			s = s + ": " + s1;
		}

		return s;
	}

	@SideOnly(Side.CLIENT)
	public static void registerColorHandler(ColorHandlerEvent.Item evt) {
		ItemColors itemColors = evt.getItemColors();
		IItemColor handler = new ItemGasCanister.GasCanisterColorHandler();
		itemColors.registerItemColorHandler(handler, ModItems.gas_full);
	}

	@SideOnly(Side.CLIENT)
	private static class GasCanisterColorHandler implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if(tintIndex != 0){
				Fluids.CD_Gastank tank = Fluids.fromID(stack.getItemDamage()).getContainer(Fluids.CD_Gastank.class);
				if(tank == null) return 0xffffff;
				return tintIndex == 1 ? tank.bottleColor : tank.labelColor;
			}
			return 0xFFFFFF;
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("1000/1000 mB");
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			FluidType[] order = Fluids.getInNiceOrder();
			for (int i = 1; i < order.length; ++i) {
				FluidType type = order[i];

				if (type.getContainer(Fluids.CD_Gastank.class) != null) {
					items.add(new ItemStack(this, 1, type.getID()));
				}
			}
		}
	}
}