package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFluidIcon extends ItemBakedBase {

	public final String texturePath;
	public ItemFluidIcon(String s) {
		super(s);
		texturePath = s;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		FluidType[] order = Fluids.getInNiceOrder();
		// Th3_Sl1ze: yes I know it makes fluid icon searchable in creative gui
		// it fixes shit with jei and it being searchable in gm1 isn't that big of a deal imo
		if(tab == CreativeTabs.SEARCH || this.isInCreativeTab(tab)) {
			for(int i = 1; i < order.length; ++i) {
				items.add(new ItemStack(this, 1, order[i].getID()));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ResourceLocation loc = new ResourceLocation(Tags.MODID, "items/" + texturePath);
		ModelResourceLocation mrl = new ModelResourceLocation(loc, "inventory");

		for (FluidType ft : Fluids.getInNiceOrder()) {
			ModelLoader.setCustomModelResourceLocation(this, ft.getID(), mrl);
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.hasTagCompound()) {
			if(getQuantity(stack) > 0) tooltip.add(getQuantity(stack) + "mB");
			if(getPressure(stack) > 0) tooltip.add(TextFormatting.RED + "" + getPressure(stack) + "PU");
		}

		Fluids.fromID(stack.getItemDamage()).addInfo(tooltip);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		FluidType fluidType = Fluids.fromID(stack.getMetadata());
		if (fluidType != null) {
			String unlocalizedName = fluidType.getTranslationKey();
			String localizedName = I18n.format(unlocalizedName).trim();

			if (!localizedName.isEmpty()) {
				return localizedName;
			}
		}

		return "Unknown";
	}

	public static ItemStack addQuantity(ItemStack stack, int i) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("fill", i);
		return stack;
	}

	public static ItemStack addPressure(ItemStack stack, int i) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("pressure", i);
		return stack;
	}

	public static ItemStack make(FluidStack stack) {
		return make(stack.type, stack.fill, stack.pressure);
	}

	public static ItemStack make(FluidType fluid, int i) {
		return make(fluid, i, 0);
	}

	public static ItemStack make(FluidType fluid, int i, int pressure) {
		return addPressure(addQuantity(new ItemStack(ModItems.fluid_icon, 1, fluid.getID()), i), pressure);
	}

	public static int getQuantity(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("fill");
	}

	public static int getPressure(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("pressure");
	}

	@Nullable
	public static FluidType getFluidType(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ItemFluidIcon)) {
			return null;
		}
		return Fluids.fromID(stack.getMetadata());
	}

}
