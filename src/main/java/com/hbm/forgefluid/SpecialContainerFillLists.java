package com.hbm.forgefluid;

import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecialContainerFillLists {
	
	//Drillgon200: I don't even know what I'm trying to do here, but hopefully it works.
	public enum EnumCanister {
		EMPTY(null, new ModelResourceLocation(Tags.MODID + ":canister_empty", "inventory")),
		DIESEL(Fluids.DIESEL, new ModelResourceLocation(Tags.MODID + ":canister_fuel", "inventory")),
		OIL(Fluids.OIL, new ModelResourceLocation(Tags.MODID + ":canister_oil", "inventory")),
		PETROIL(Fluids.PETROIL, new ModelResourceLocation(Tags.MODID + ":canister_petroil", "inventory")),
		FRACKSOL(Fluids.FRACKSOL, new ModelResourceLocation(Tags.MODID + ":canister_fracksol", "inventory")),
		KEROSENE(Fluids.KEROSENE, new ModelResourceLocation(Tags.MODID + ":canister_kerosene", "inventory")),
		NITAN(Fluids.NITAN, new ModelResourceLocation(Tags.MODID + ":canister_superfuel", "inventory")),
		BIOFUEL(Fluids.BIOFUEL, new ModelResourceLocation(Tags.MODID + ":canister_biofuel", "inventory")),
		CANOLA(Fluids.LUBRICANT, new ModelResourceLocation(Tags.MODID + ":canister_canola", "inventory")),
		REOIL(Fluids.RECLAIMED, new ModelResourceLocation(Tags.MODID + ":canister_reoil", "inventory")),
		HEAVYOIL(Fluids.HEAVYOIL, new ModelResourceLocation(Tags.MODID + ":canister_heavyoil", "inventory")),
		BITUMEN(Fluids.BITUMEN, new ModelResourceLocation(Tags.MODID + ":canister_bitumen", "inventory")),
		SMEAR(Fluids.SMEAR, new ModelResourceLocation(Tags.MODID + ":canister_smear", "inventory")),
		HEATINGOIL(Fluids.HEATINGOIL, new ModelResourceLocation(Tags.MODID + ":canister_heatingoil", "inventory")),
		NAPHTHA(Fluids.NAPHTHA, new ModelResourceLocation(Tags.MODID + ":canister_naphtha", "inventory")),
		LIGHTOIL(Fluids.LIGHTOIL, new ModelResourceLocation(Tags.MODID + ":canister_lightoil", "inventory")),
		GASOLINE(Fluids.GASOLINE, new ModelResourceLocation(Tags.MODID + ":canister_gasoline", "inventory"));

        public static final EnumCanister[] VALUES = values();

		private FluidType fluid;
		private Pair<ModelResourceLocation, IBakedModel> renderPair;
		private String translateKey;
		
		private EnumCanister(FluidType f, ModelResourceLocation r){
			this.fluid = f;
			this.renderPair = MutablePair.of(r, null);
			this.translateKey = "item." + r.getPath() + ".name";
		}
		public FluidType getFluid(){
			return fluid;
		}
		public String getTranslateKey(){
			return translateKey;
		}
		public IBakedModel getRenderModel(){
			return renderPair.getRight();
		}
		public void putRenderModel(IBakedModel model){
			renderPair.setValue(model);
		}
		public ModelResourceLocation getResourceLocation(){
			return renderPair.getLeft();
		}
		public static boolean contains(FluidType f){
			if(f == null)
				return false;
			for(EnumCanister e : EnumCanister.VALUES){
				if(e.getFluid() == f)
					return true;
			}
			return false;
		}
		public static EnumCanister getEnumFromFluid(FluidType f){
			if(f == null || f == Fluids.NONE)
				return EnumCanister.EMPTY;
			for(EnumCanister e : EnumCanister.VALUES){
				if(e.getFluid() == f){
					return e;
				}
			}
			return null;
		}
		public static FluidType[] getFluids() {
			FluidType[] f = new FluidType[EnumCanister.VALUES.length];
			for(int i = 0; i < EnumCanister.VALUES.length; i ++){
				f[i] = EnumCanister.VALUES[i].getFluid();
			}
			return f;
		}
	}

	public enum EnumCell {
		EMPTY(null, new ModelResourceLocation(Tags.MODID + ":cell_empty", "inventory")),
		UF6(Fluids.UF6, new ModelResourceLocation(Tags.MODID + ":cell_uf6", "inventory")),
		PUF6(Fluids.PUF6, new ModelResourceLocation(Tags.MODID + ":cell_puf6", "inventory")),
		ANTIMATTER(Fluids.AMAT, new ModelResourceLocation(Tags.MODID + ":cell_antimatter", "inventory")),
		DEUTERIUM(Fluids.DEUTERIUM, new ModelResourceLocation(Tags.MODID + ":cell_deuterium", "inventory")),
		TRITIUM(Fluids.TRITIUM, new ModelResourceLocation(Tags.MODID + ":cell_tritium", "inventory")),
		SAS3(Fluids.SAS3, new ModelResourceLocation(Tags.MODID + ":cell_sas3", "inventory")),
		ANTISCHRABIDIUM(Fluids.ASCHRAB, new ModelResourceLocation(Tags.MODID + ":cell_anti_schrabidium", "inventory"));
        public static final EnumCell[] VALUES = values();

		private static final Map<FluidType, EnumCell> FLUID_TO_CELL_MAP;
		private static final Set<FluidType> VALID_FLUIDS;
		private static final FluidType[] FLUID_ARRAY;

		static {
			FLUID_TO_CELL_MAP = Arrays.stream(VALUES).filter(e -> e.fluid != null)
					.collect(Collectors.toMap(EnumCell::getFluid, e -> e));

			VALID_FLUIDS = FLUID_TO_CELL_MAP.keySet();
			FLUID_ARRAY = Arrays.stream(VALUES).map(EnumCell::getFluid).toArray(FluidType[]::new);
		}
		private final FluidType fluid;
		private final Pair<ModelResourceLocation, IBakedModel> renderPair;
		private final String translateKey;

		EnumCell(FluidType f, ModelResourceLocation r) {
			this.fluid = f;
			this.renderPair = MutablePair.of(r, null);
			this.translateKey = "item." + r.getPath() + ".name";
		}

		public FluidType getFluid() {
			return fluid;
		}

		public String getTranslateKey() {
			return translateKey;
		}

		public IBakedModel getRenderModel() {
			return renderPair.getRight();
		}

		public void putRenderModel(IBakedModel model) {
			renderPair.setValue(model);
		}

		public ModelResourceLocation getResourceLocation() {
			return renderPair.getLeft();
		}

		public static boolean contains(FluidType f) {
			return f != null && VALID_FLUIDS.contains(f);
		}

		public static EnumCell getEnumFromFluid(FluidType f) {
			if (f == null) {
				return EMPTY;
			}
			return FLUID_TO_CELL_MAP.getOrDefault(f, EMPTY); // Return EMPTY as a safe default
		}

		public static FluidType[] getFluids() {
			return FLUID_ARRAY;
		}
	}

	public enum EnumGasCanister {
		NATURAL(Fluids.GAS, new ModelResourceLocation(Tags.MODID + ":gas_full", "inventory")),
		PETROLEUM(Fluids.PETROLEUM, new ModelResourceLocation(Tags.MODID + ":gas_petroleum", "inventory")),
		BIOGAS(Fluids.BIOGAS, new ModelResourceLocation(Tags.MODID + ":gas_biogas", "inventory")),
		HYDROGEN(Fluids.HYDROGEN, new ModelResourceLocation(Tags.MODID + ":gas_hydrogen", "inventory")),
		DEUTERIUM(Fluids.DEUTERIUM, new ModelResourceLocation(Tags.MODID + ":gas_deuterium", "inventory")),
		TRITIUM(Fluids.TRITIUM, new ModelResourceLocation(Tags.MODID + ":gas_tritium", "inventory")),
		OXYGEN(Fluids.OXYGEN, new ModelResourceLocation(Tags.MODID + ":gas_oxygen", "inventory"));
        public static final EnumGasCanister[] VALUES = values();

		private FluidType fluid;
		private Pair<ModelResourceLocation, IBakedModel> renderPair;
		private String translateKey;
		
		private EnumGasCanister(FluidType f, ModelResourceLocation r){
			this.fluid = f;
			this.renderPair = MutablePair.of(r, null);
			this.translateKey = "item." + r.getPath() + ".name";
		}
		public FluidType getFluid(){
			return fluid;
		}
		public String getTranslateKey(){
			return translateKey;
		}
		public IBakedModel getRenderModel(){
			return renderPair.getRight();
		}
		public void putRenderModel(IBakedModel model){
			renderPair.setValue(model);
		}
		public ModelResourceLocation getResourceLocation(){
			return renderPair.getLeft();
		}
		public static boolean contains(FluidType f){
			if(f == null)
				return false;
			for(EnumGasCanister e : EnumGasCanister.VALUES){
				if(e.getFluid() == f)
					return true;
			}
			return false;
		}
		public static FluidType[] getFluids() {
			FluidType[] f = new FluidType[EnumGasCanister.VALUES.length];
			for(int i = 0; i < EnumGasCanister.VALUES.length; i ++){
				f[i] = EnumGasCanister.VALUES[i].getFluid();
			}
			return f;
		}
	}

	
}
