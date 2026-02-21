package com.hbm.inventory.recipes;

import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RefineryRecipes {

	/// fractions in percent ///
	public static final int oil_frac_heavy = 50;
	public static final int oil_frac_naph = 25;
	public static final int oil_frac_light = 15;
	public static final int oil_frac_petro = 10;
	public static final int crack_frac_naph = 40;
	public static final int crack_frac_light = 30;
	public static final int crack_frac_aroma = 15;
	public static final int crack_frac_unsat = 15;

	public static final int oilds_frac_heavy = 30;
	public static final int oilds_frac_naph = 35;
	public static final int oilds_frac_light = 20;
	public static final int oilds_frac_unsat = 15;
	public static final int crackds_frac_naph = 35;
	public static final int crackds_frac_light = 35;
	public static final int crackds_frac_aroma = 15;
	public static final int crackds_frac_unsat = 15;

	public static final int vac_frac_heavy = 40;
	public static final int vac_frac_reform = 25;
	public static final int vac_frac_light = 20;
	public static final int vac_frac_sour = 15;

	public static LinkedHashMap<FluidType, Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack>> refinery = new LinkedHashMap<>();
	public static Map<FluidType, Tuple.Quartet<FluidStack, FluidStack, FluidStack, FluidStack>> vacuum = new HashMap<>();

	public static HashMap<Object, Object[]> getRefineryRecipe() {

		HashMap<Object, Object[]> recipes = new HashMap<>();

		for(Map.Entry<FluidType, Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack>> recipe : refinery.entrySet()) {

			Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack> fluids = recipe.getValue();

			recipes.put(ItemFluidIcon.make(recipe.getKey(), 1000),
					new ItemStack[] {
							ItemFluidIcon.make(fluids.getV().type, fluids.getV().fill * 10),
							ItemFluidIcon.make(fluids.getW().type, fluids.getW().fill * 10),
							ItemFluidIcon.make(fluids.getX().type, fluids.getX().fill * 10),
							ItemFluidIcon.make(fluids.getY().type, fluids.getY().fill * 10),
							ItemStackUtil.carefulCopy(fluids.getZ()) });
		}

		return recipes;
	}

	public static HashMap<Object, Object[]> getVacuumRecipe() {

		HashMap<Object, Object[]> recipes = new HashMap<>();

		for(Map.Entry<FluidType, Tuple.Quartet<FluidStack, FluidStack, FluidStack, FluidStack>> recipe : vacuum.entrySet()) {

			Tuple.Quartet<FluidStack, FluidStack, FluidStack, FluidStack> fluids = recipe.getValue();

			recipes.put(ItemFluidIcon.make(recipe.getKey(), 1000, 2),
					new ItemStack[] {
							ItemFluidIcon.make(fluids.getW().type, fluids.getW().fill * 10),
							ItemFluidIcon.make(fluids.getX().type, fluids.getX().fill * 10),
							ItemFluidIcon.make(fluids.getY().type, fluids.getY().fill * 10),
							ItemFluidIcon.make(fluids.getZ().type, fluids.getZ().fill * 10) });
		}

		return recipes;
	}

	public static void registerRefinery() {
		refinery.put(Fluids.HOTOIL, new Tuple.Quintet<>(
				new FluidStack(Fluids.HEAVYOIL,		oil_frac_heavy),
				new FluidStack(Fluids.NAPHTHA,		oil_frac_naph),
				new FluidStack(Fluids.LIGHTOIL,		oil_frac_light),
				new FluidStack(Fluids.PETROLEUM,	oil_frac_petro),
				new ItemStack(ModItems.sulfur)
		));
		refinery.put(Fluids.HOTCRACKOIL, new Tuple.Quintet<>(
				new FluidStack(Fluids.NAPHTHA_CRACK,	crack_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_CRACK,	crack_frac_light),
				new FluidStack(Fluids.AROMATICS,		crack_frac_aroma),
				new FluidStack(Fluids.UNSATURATEDS,		crack_frac_unsat),
				OreDictManager.DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.CRACK)
		));
		refinery.put(Fluids.HOTOIL_DS, new Tuple.Quintet<>(
				new FluidStack(Fluids.HEAVYOIL,		oilds_frac_heavy),
				new FluidStack(Fluids.NAPHTHA_DS,	oilds_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_DS,	oilds_frac_light),
				new FluidStack(Fluids.UNSATURATEDS,	oilds_frac_unsat),
				OreDictManager.DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.PARAFFIN)
		));
		refinery.put(Fluids.HOTCRACKOIL_DS, new Tuple.Quintet<>(
				new FluidStack(Fluids.NAPHTHA_DS,		crackds_frac_naph),
				new FluidStack(Fluids.LIGHTOIL_DS,		crackds_frac_light),
				new FluidStack(Fluids.AROMATICS,		crackds_frac_aroma),
				new FluidStack(Fluids.UNSATURATEDS,		crackds_frac_unsat),
				OreDictManager.DictFrame.fromOne(ModItems.oil_tar, ItemEnums.EnumTarType.PARAFFIN)
		));

		vacuum.put(Fluids.OIL, new Tuple.Quartet<>(
				new FluidStack(Fluids.HEAVYOIL_VACUUM,	vac_frac_heavy),
				new FluidStack(Fluids.REFORMATE,		vac_frac_reform),
				new FluidStack(Fluids.LIGHTOIL_VACUUM,	vac_frac_light),
				new FluidStack(Fluids.SOURGAS,			vac_frac_sour)
		));
		vacuum.put(Fluids.OIL_DS, new Tuple.Quartet<>(
				new FluidStack(Fluids.HEAVYOIL_VACUUM,	vac_frac_heavy),
				new FluidStack(Fluids.REFORMATE,		vac_frac_reform),
				new FluidStack(Fluids.LIGHTOIL_VACUUM,	vac_frac_light),
				new FluidStack(Fluids.REFORMGAS,		vac_frac_sour)
		));
	}

	public static Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack> getRefinery(FluidType oil) {
		return refinery.get(oil);
	}

	public static Tuple.Quartet<FluidStack, FluidStack, FluidStack, FluidStack> getVacuum(FluidType oil) {
		return vacuum.get(oil);
	}
}