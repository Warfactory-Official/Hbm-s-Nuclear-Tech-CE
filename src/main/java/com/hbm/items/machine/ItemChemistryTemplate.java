package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIChemfac;
import com.hbm.inventory.gui.GUIMachineChemplant;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hbm.items.machine.ItemAssemblyTemplate.checkAndConsume;
import static com.hbm.items.machine.ItemAssemblyTemplate.countItem;

public class ItemChemistryTemplate extends Item implements IHasCustomModel {

	public static final ModelResourceLocation location = new ModelResourceLocation(Tags.MODID + ":chemistry_template", "inventory");
	
	public ItemChemistryTemplate(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(MainRegistry.templateTab);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public @NotNull String getItemStackDisplayName(ItemStack stack) {
		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(stack.getItemDamage());
		if(recipe == null) {
			return TextFormatting.RED + "Broken Template" + TextFormatting.RESET;
		} else {
			String s = (I18n.format(this.getTranslationKey() + ".name")).trim();
			String s1 = (I18n.format("chem." + recipe.name)).trim();
            return s + " " + s1;
		}
	}
	
	@Override
	public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
		if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH){
			for(int i: ChemplantRecipes.recipeNames.keySet()) {
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		if(!(stack.getItem() instanceof ItemChemistryTemplate)) return;
		ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(stack.getItemDamage());
		if(recipe == null) return;
		list.add("§6" + I18nUtil.resolveKey("info.templatefolder",  I18nUtil.resolveKey("item.template_folder.name")));
		list.add("");
		Map<ComparableStack, Integer> availableCounts = null;
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		FluidTankNTM[] chemTanks = null;
		if (screen instanceof GUIMachineChemplant guiChemplant && guiChemplant.getSlotUnderMouse()!= null) {
			IItemHandler chemPlantInventory = guiChemplant.getInventory();
			chemTanks = guiChemplant.getTanks();
			if (chemPlantInventory != null) {
				availableCounts = new HashMap<>();
				for (int slot = 6; slot < 18; slot++) {
					countItem(availableCounts, chemPlantInventory, slot);
				}
			}
		} else if (screen instanceof GUIChemfac chemfacGUI && chemfacGUI.getSlotUnderMouse() != null) {
			IItemHandler chemfacInventory = chemfacGUI.getInventory();
			final int slotIndex = chemfacGUI.getSlotUnderMouse().slotNumber;
			FluidTankNTM[] chemfacTanks = chemfacGUI.getTanks();
			if (slotIndex >= 13 && slotIndex <= 76 && (slotIndex - 13) % 9 == 0) {
				int[] mapping = new int[]{(slotIndex - 13) / 9 * 4, (slotIndex - 13) / 9 * 4 + 1};
				chemTanks = new FluidTankNTM[]{chemfacTanks[mapping[0]], chemfacTanks[mapping[1]]};
			}
			if (chemfacInventory != null && slotIndex >= 13 && slotIndex <= 76 && (slotIndex - 13) % 9 == 0) {
				int[] inputSlots = new int[]{slotIndex - 8, slotIndex - 7, slotIndex - 6, slotIndex - 5};
				availableCounts = new HashMap<>();
				for(int idx : inputSlots) {
					countItem(availableCounts, chemfacInventory, idx);
				}
			}
		}
		list.add("");
		try {
			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_out_p"));
			for(int i = 0; i < 4; i++) {
				if(recipe.outputs[i] != null) {
					list.add(recipe.outputs[i].getCount() + "x " + recipe.outputs[i].getDisplayName());
				}
			}

			for(int i = 0; i < 2; i++) {
				if(recipe.outputFluids[i] != null) {
					int p = recipe.outputFluids[i].pressure;
					list.add(recipe.outputFluids[i].fill + "mB " + recipe.outputFluids[i].type.getLocalizedName() + (p != 0 ? (" at " + p + "PU") : ""));
				}
			}

			list.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_in_p"));

			Map<ComparableStack, Integer> workingItemCounts = availableCounts != null ? new HashMap<>(availableCounts) : null;
			for(AStack ingredient : recipe.inputs) {
				if (ingredient == null) continue;
				String prefix = "§c";
				if (workingItemCounts != null) {
					ItemAssemblyTemplate.CheckResult result = checkAndConsume(ingredient, workingItemCounts);
					prefix = result.color();
				}
				if(ingredient instanceof ComparableStack input)  {
					list.add(" " + prefix + input.toStack().getCount() + "x " + input.toStack().getDisplayName());
				} else if(ingredient instanceof OreDictStack input)  {
					NonNullList<ItemStack> ores = OreDictionary.getOres(input.name);
					if(!ores.isEmpty()) {
						ItemStack inStack = ores.get((int) (Math.abs(System.currentTimeMillis() / 1000) % ores.size()));
						list.add(" " + prefix + input.count() + "x " + inStack.getDisplayName());
					} else {
						list.add("I AM ERROR - No OrdDict match found for " + ingredient);
					}
				}
			}

			int[] workingFluidLevels = null;
			if (chemTanks != null) {
				workingFluidLevels = new int[]{chemTanks[0].getFill(), chemTanks[1].getFill()};
			}

			for(int i = 0; i < 2; i++) {
				if(recipe.inputFluids[i] != null) {
					int p = recipe.inputFluids[i].pressure;
					String prefix = "§c";
					if (workingFluidLevels != null) {
						int requiredFill = recipe.inputFluids[i].fill;
						if (workingFluidLevels[i] >= requiredFill) {
							prefix = "§a";
							workingFluidLevels[i] -= requiredFill;
						} else if (workingFluidLevels[i] > 0) {
							prefix = "§6";
						}
					}
					list.add(prefix + recipe.inputFluids[i].fill + "mB " + recipe.inputFluids[i].type.getLocalizedName() + (p != 0 ? (" at " + p + "PU") : ""));
				}
			}
			list.add("§l" + I18nUtil.resolveKey("info.template_time"));
			list.add(" §3"+ Math.floor((float)(recipe.getDuration()) / 20 * 100) / 100 + " " + I18nUtil.resolveKey("info.template_seconds"));
		} catch(Exception e) {
			list.add("###INVALID###");
			list.add("0x334077-0x6A298F-0xDF3795-0x334077");
		}
	}

	@Override
	public ModelResourceLocation getResourceLocation() {
		return location;
	}
}
