package com.hbm.items.block;

import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ItemCustomMachine extends ItemBlock {

	public ItemCustomMachine(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {

		if(this.isInCreativeTab(tab)) {
			for(int i = 0; i < CustomMachineConfigJSON.niceList.size(); i++) {
				list.add(new ItemStack(this, 1, i + 100));
			}
		}
	}

	@Override
	public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {

		int id = stack.getItemDamage() - 100;

		if(id >= 0 && id < CustomMachineConfigJSON.niceList.size()) {
			MachineConfiguration conf = CustomMachineConfigJSON.niceList.get(id);

			if(conf != null) {
				String localized = conf.localization.get(MainRegistry.proxy.getLanguageCode());
				return localized != null ? localized : conf.localizedName;
			}
		}

		return "INVALID MACHINE CONTROLLER";
	}
}
