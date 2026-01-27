package com.hbm.handler.jei;

import com.hbm.Tags;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.*;
import com.hbm.inventory.recipes.MagicRecipes.MagicRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBatterySC;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.lib.Library;
import com.hbm.util.I18nUtil;
import com.hbm.util.WeightedRandomObject;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

public class JeiRecipes {

	private static List<ChemRecipe> chemRecipes = null;
	private static List<CyclotronRecipe> cyclotronRecipes = null;
	private static List<AlloyFurnaceRecipe> alloyFurnaceRecipes = null;
    private static List<CMBFurnaceRecipe> cmbRecipes = null;
	private static List<GasCentrifugeRecipe> gasCentRecipes = null;
    private static List<StorageDrumRecipe> storageDrumRecipes = null;
	private static List<RBMKFuelRecipe> rbmkFuelRecipes = null;
	private static List<RefineryRecipe> refineryRecipes = null;
	private static List<FluidRecipe> fluidEquivalences = null;
	private static List<BookRecipe> bookRecipes = null;
	private static List<BreederRecipe> breederRecipes = null;
    private static List<HadronRecipe> hadronRecipes = null;
	private static List<SILEXRecipe> silexRecipes = null;
	private static final Map<EnumWavelengths, List<SILEXRecipe>> waveSilexRecipes = new HashMap<>();
    private static List<TransmutationRecipe> transmutationRecipes = null;
	
	private static List<ItemStack> batteries = null;
    private static List<ItemStack> blades = null;
	private static List<ItemStack> alloyFuels = null;

	public static final IIngredientType<FluidStack> FluidNTM = () -> FluidStack.class;
	
	
	public static class ChemRecipe implements IRecipeWrapper {
		
		private final List<List<ItemStack>> inputs;
		private final List<ItemStack> outputs;
		
		public ChemRecipe(List<AStack> inputs, List<ItemStack> outputs) {
			List<List<ItemStack>> list = new ArrayList<>(inputs.size());
			for(AStack s : inputs)
				list.add(s.getStackList());
			this.inputs = list;
			this.outputs = outputs; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			List<List<ItemStack>> in = Library.copyItemStackListList(inputs); // list of inputs and their list of possible items
			ingredients.setInputLists(VanillaTypes.ITEM, in);
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}
	}
	
	public static class CyclotronRecipe implements IRecipeWrapper {
		
		private final List<ItemStack> inputs;
		private final ItemStack output;
		
		public CyclotronRecipe(List<ItemStack> inputs, ItemStack output) {
			this.inputs = inputs;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputs(VanillaTypes.ITEM, inputs);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}
	
	public static class AlloyFurnaceRecipe implements IRecipeWrapper {
		
		private final List<List<ItemStack>> inputs;
		private final ItemStack output;
		
		public AlloyFurnaceRecipe(List<ItemStack>[] list, ItemStack output) {
			this.inputs = Arrays.asList(list);
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			List<List<ItemStack>> in = Library.copyItemStackListList(inputs);
			ingredients.setInputLists(VanillaTypes.ITEM, in);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}

	public static class BoilerRecipe implements IRecipeWrapper {
		public final FluidStack input;
		public final FluidStack output;

		public BoilerRecipe(FluidStack input, FluidStack output) {
			this.input = input;
			this.output = output;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(FluidNTM, input);
			ingredients.setOutput(FluidNTM, output);
		}
	}
	
	public static class CMBFurnaceRecipe implements IRecipeWrapper {
		
		private final List<ItemStack> inputs;
		private final ItemStack output;
		
		public CMBFurnaceRecipe(List<ItemStack> inputs, ItemStack output) {
			this.inputs = inputs;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputs(VanillaTypes.ITEM, inputs);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}

	public static class GasCentrifugeRecipe implements IRecipeWrapper {
		private final ItemStack input;
		private final List<ItemStack> outputs;
		private final boolean isHighSpeed;
		private final int centNumber;

		public GasCentrifugeRecipe(ItemStack input, ItemStack[] outputs, boolean isHighSpeed, int centNumber) {
			this.input = input.copy();
			this.input.setCount(1);
			this.outputs = Arrays.asList(outputs);
			this.isHighSpeed = isHighSpeed;
			this.centNumber = centNumber;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}

		public ItemStack getInput() {
			return input;
		}

		public List<ItemStack> getOutputs() {
			return outputs;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			String centrifuges = centNumber + " G. Cents";
			int x = (50 - minecraft.fontRenderer.getStringWidth(centrifuges) / 2) - 3;
			int y = 4;
			minecraft.fontRenderer.drawString(centrifuges, x, y, 0x00FF00);

			if (isHighSpeed) {
				minecraft.getTextureManager().bindTexture(new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_jei_gas_centrifuge.png"));
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(23, 19, 184, 37, 16, 16, 256, 256);
			}
		}
	}

	public static class WasteDrumRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final ItemStack output;
		
		public WasteDrumRecipe(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
	}

	public static class StorageDrumRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final ItemStack output;
		
		public StorageDrumRecipe(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
	}

	public static class TransmutationRecipe implements IRecipeWrapper {
		
		private final List<List<ItemStack>> inputs;
		private final ItemStack output;
		
		public TransmutationRecipe(List<ItemStack> inputs, ItemStack output) {
			this.inputs = new ArrayList<>();
			this.inputs.add(inputs);
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, inputs);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
	}

	public static class RBMKFuelRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final ItemStack output;
		
		public RBMKFuelRecipe(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
	}
	
	public static class RefineryRecipe implements IRecipeWrapper {
		
		private final ItemStack input;
		private final List<ItemStack> outputs;
		
		public RefineryRecipe(ItemStack input, List<ItemStack> outputs) {
			this.input = input;
			this.outputs = outputs; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}
		
	}
	
	public static class FluidRecipe implements IRecipeWrapper {
		
		protected final ItemStack input;
		protected final ItemStack output;
		
		public FluidRecipe(ItemStack input, ItemStack output) {
			this.input = input;
			this.output = output; 
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}
	
	public static class FluidRecipeInverse extends FluidRecipe implements IRecipeWrapper {
		
		public FluidRecipeInverse(ItemStack input, ItemStack output) {
			super(input, output);
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, output);
			ingredients.setOutput(VanillaTypes.ITEM, input);
		}
		
	}

    public static class BookRecipe implements IRecipeWrapper {

		List<ItemStack> inputs;
		ItemStack output;
		
		public BookRecipe(MagicRecipe recipe) {
			inputs = new ArrayList<>(4);
			for(int i = 0; i < recipe.in.size(); i ++)
				inputs.add(recipe.in.get(i).getStack());
			while(inputs.size() < 4)
				inputs.add(new ItemStack(ModItems.nothing));
			output = recipe.getResult();
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputs(VanillaTypes.ITEM, inputs);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
		
	}

	public static class BreederRecipe implements IRecipeWrapper {

		ItemStack input;
		ItemStack output;
		public int flux;

		public BreederRecipe(ItemStack input, ItemStack output, int flux) {
			input.setCount(1);
			this.input = input.copy();
			this.output = output.copy();
			this.flux = flux;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			String fluxText = "" + flux;
			int x = (88 - minecraft.fontRenderer.getStringWidth(fluxText) / 2) - 34;
			int y = 2;
			minecraft.fontRenderer.drawString(fluxText, x, y, 0x00FF00);
		}

	}
	
	public static class FusionRecipe implements IRecipeWrapper {
		ItemStack input;
		ItemStack output;
		
		public FusionRecipe(FluidStack input, ItemStack output) {
			this.input = ItemFluidIcon.make(input);
			this.output = output;
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInput(VanillaTypes.ITEM, input);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}
	}
	
	public static class HadronRecipe implements IRecipeWrapper {

		public ItemStack in1, in2, out1, out2;
		public int momentum;
		public boolean analysisOnly;
		
		public HadronRecipe(ItemStack in1, ItemStack in2, ItemStack out1, ItemStack out2, int momentum, boolean analysis) {
			this.in1 = in1;
			this.in2 = in2;
			this.out1 = out1;
			this.out2 = out2;
			this.momentum = momentum;
			this.analysisOnly = analysis;
		}
		
		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(in1, in2));
			ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(out1, out2));
		}
		
		@Override
		public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			if(analysisOnly)
				HadronRecipeHandler.analysis.draw(minecraft, 117, 17);
			FontRenderer fontRenderer = minecraft.fontRenderer;
	    	
	    	String mom = "" + momentum;
	    	fontRenderer.drawString(mom, -fontRenderer.getStringWidth(mom) / 2 + 19, 36, 0x404040);
	    	GlStateManager.color(1, 1, 1, 1);
		}
		
	}
	
	public static class SILEXRecipe implements IRecipeWrapper {

		List<List<ItemStack>> input;
		List<Double> chances;
		List<ItemStack> outputs;
		double produced;
		EnumWavelengths laserStrength;
		
		public SILEXRecipe(List<ItemStack> inputs, List<Double> chances, List<ItemStack> outputs, double produced, EnumWavelengths laserStrength){
			input = new ArrayList<>(1);
			input.add(inputs);
			this.chances = chances;
			this.outputs = outputs;
			this.produced = produced;
			this.laserStrength = laserStrength;
		}
		
		@Override
		public void getIngredients(IIngredients ingredients){
			ingredients.setInputLists(VanillaTypes.ITEM, input);
			ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY){
			FontRenderer fontRenderer = minecraft.fontRenderer;

			int output_size = this.outputs.size();
			int sep = output_size > 6 ? 4 : output_size > 4 ? 3 : 2;
			for(int i = 0; i < output_size; i ++){
				double chance = this.chances.get(i);
				if(i < sep) {
					fontRenderer.drawString(((int)(chance * 100D) / 100D)+"%", 90, 33 + i * 18 - 9 * ((Math.min(output_size, sep) + 1) / 2), 0x404040);
				} else {
					fontRenderer.drawString(((int)(chance * 100D) / 100D)+"%", 138, 33 + (i - sep) * 18 - 9 * ((Math.min(output_size - sep, sep) + 1)/2), 0x404040);
				}
			}
			
			String am = ((int)(this.produced * 10D) / 10D) + "x";
			fontRenderer.drawString(am, 52 - fontRenderer.getStringWidth(am) / 2, 51, 0x404040);

			String wavelength = (this.laserStrength == EnumWavelengths.NULL) ? TextFormatting.WHITE + "N/A" : this.laserStrength.textColor + I18nUtil.resolveKey(this.laserStrength.name);
			fontRenderer.drawString(wavelength, (35 - fontRenderer.getStringWidth(wavelength) / 2), 17, 0x404040);
		}
	}

	public static class JeiUniversalRecipe implements IRecipeWrapper {
		protected final List<List<ItemStack>> inputs;
		protected final ItemStack[] outputs;
		protected final ItemStack[] machines;

		public JeiUniversalRecipe(List<List<ItemStack>> inputs, ItemStack[] outputs, ItemStack[] machine) {
			this.inputs = inputs;
			this.outputs = Arrays.stream(outputs).map(ItemStack::copy).toArray(ItemStack[]::new);
			this.machines = Arrays.stream(machine).map(ItemStack::copy).toArray(ItemStack[]::new);
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputLists(VanillaTypes.ITEM, inputs);
			ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(outputs));
		}

		public ItemStack[] getMachines() {
			return machines;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			ResourceLocation GUI_TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/jei/gui_nei.png");
			minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

			Gui.drawModalRectWithCustomSizedTexture(74, 14, 59, 87, 18, 36, 256, 256);

			int[][] inCoords = JEIUniversalHandler.getInputCoords(inputs.size());
			for (int[] coords : inCoords) {
				Gui.drawModalRectWithCustomSizedTexture(coords[0], coords[1], 5, 87, 18, 18, 256, 256);
			}

			int[][] outCoords = JEIUniversalHandler.getOutputCoords(outputs.length);
			for (int[] coords : outCoords) {
				Gui.drawModalRectWithCustomSizedTexture(coords[0], coords[1], 5, 87, 18, 18, 256, 256);
			}
		}
	}

	public static class CrystallizerRecipe extends JeiUniversalRecipe {
		private final int productivity;
		public CrystallizerRecipe(List<List<ItemStack>> inputs, ItemStack[] outputs, ItemStack[] machine, int productivity) {
			super(inputs, outputs, machine);
			this.productivity = productivity;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
			if (productivity > 0) {
				FontRenderer fontRenderer = minecraft.fontRenderer;
				String momentum = "Effectiveness: +" + Math.min(productivity, 99) + "% per level";
				int side = 8;
				fontRenderer.drawString(momentum, side, 52, 0x404040);
			}
		}
	}

	public static class SolderingRecipe extends JeiUniversalRecipe {
		private final int duration;
		private final int consumption;
		public SolderingRecipe(List<List<ItemStack>> inputs, ItemStack[] outputs, ItemStack[] machine, int duration, int consumption) {
			super(inputs, outputs, machine);
			this.duration = duration;
			this.consumption = consumption;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
			// I honestly don't know why Bob decided to compare outputs here, we'll see later, I think..
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
			String duration = String.format(Locale.US, "%,d", this.duration) + " ticks";
			String consumption = String.format(Locale.US, "%,d", this.consumption) + " HE/t";
			int side = 160;
			fontRenderer.drawString(duration, side - fontRenderer.getStringWidth(duration), 43, 0x404040);
			fontRenderer.drawString(consumption, side - fontRenderer.getStringWidth(consumption), 55, 0x404040);
        }
	}

    public static class ArcWelderRecipe extends JeiUniversalRecipe {
        private final int duration;
        private final int consumption;
        private final boolean hasStats;

        public ArcWelderRecipe(List<List<ItemStack>> inputs, ItemStack[] outputs, ItemStack[] machine, int duration, int consumption, boolean hasStats) {
            super(inputs, outputs, machine);
            this.duration = duration;
            this.consumption = consumption;
            this.hasStats = hasStats;
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
            if (!hasStats) {
                return;
            }

            FontRenderer fontRenderer = minecraft.fontRenderer;
            String durationText = String.format(Locale.US, "%,d", duration) + " ticks";
            String consumptionText = String.format(Locale.US, "%,d", consumption) + " HE/t";
            int side = 160;
            fontRenderer.drawString(durationText, side - fontRenderer.getStringWidth(durationText), 43, 0x404040);
            fontRenderer.drawString(consumptionText, side - fontRenderer.getStringWidth(consumptionText), 55, 0x404040);
        }
    }
	
	public static List<ChemRecipe> getChemistryRecipes() {
		if(chemRecipes != null)
			return chemRecipes;
		chemRecipes = new ArrayList<>();
		
       for(int i: ChemplantRecipes.recipeNames.keySet()){

        	List<AStack> inputs = new ArrayList<>(7);
        	for(int j = 0; j < 7; j ++)
        		inputs.add(j, new ComparableStack(ModItems.nothing));

        	List<ItemStack> outputs = new ArrayList<>(6);
        	for(int j = 0; j < 6; j ++)
        		outputs.add(j, new ItemStack(ModItems.nothing));
        	
        	//Adding template item
        	ItemStack template = new ItemStack(ModItems.chemistry_template, 1, i);

		   ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(template.getItemDamage());

        	List<AStack> listIn = ChemplantRecipes.ChemRecipe.getChemInputFromTempate(recipe, template);
        	FluidStack[] fluidIn = ChemplantRecipes.ChemRecipe.getFluidInputFromTempate(recipe, template);
        	ItemStack[] listOut = ChemplantRecipes.ChemRecipe.getChemOutputFromTempate(recipe, template);
        	FluidStack[] fluidOut = ChemplantRecipes.ChemRecipe.getFluidOutputFromTempate(recipe, template);

        	inputs.set(6, new ComparableStack(template));

        	if(listIn != null)
        		for(int j = 0; j < listIn.size(); j++)
        			if(listIn.get(j) != null)
        				inputs.set(j + 2, listIn.get(j).copy());

        	if(fluidIn != null)
	        	for(int j = 0; j < fluidIn.length; j++)
	        		if(fluidIn[j] != null)
	        			inputs.set(j, new NbtComparableStack(ItemFluidIcon.make(fluidIn[j].type, fluidIn[j].fill)));
        	
        	if(listOut != null)
	        	for(int j = 0; j < listOut.length; j++)
	        		if(listOut[j] != null)
	        			outputs.set(j + 2, listOut[j].copy());
        	
        	if(fluidOut != null)
	        	for(int j = 0; j < fluidOut.length; j++)
	        		if(fluidOut[j] != null)
	        			outputs.set(j, ItemFluidIcon.make(fluidOut[j].type, fluidOut[j].fill));
        	
        	chemRecipes.add(new ChemRecipe(inputs, outputs));
        }
		
		return chemRecipes;
	}

	public static List<CyclotronRecipe> getCyclotronRecipes() {
		if (cyclotronRecipes != null)
			return cyclotronRecipes;
		Map<Object[], Object> recipes = CyclotronRecipes.getRecipes();
		cyclotronRecipes = new ArrayList<>(recipes.size());
		for (Entry<Object[], Object> e : recipes.entrySet()) {
			Object[] key = e.getKey();
			ItemStack[] stacks;
			if (key instanceof ItemStack[]) {
				stacks = (ItemStack[]) key;
			} else {
				stacks = new ItemStack[key.length];
				for (int i = 0; i < key.length; i++) {
					Object obj = key[i];
					stacks[i] = obj instanceof ItemStack ? (ItemStack) obj : ItemStack.EMPTY;
				}
			}
			cyclotronRecipes.add(new CyclotronRecipe(Arrays.asList(stacks), (ItemStack) e.getValue()));
		}

		return cyclotronRecipes;
	}
	
	
	public static List<AlloyFurnaceRecipe> getAlloyRecipes() {
		if(alloyFurnaceRecipes != null)
			return alloyFurnaceRecipes;
		alloyFurnaceRecipes = new ArrayList<>();

		for(Entry<List<ItemStack>[], ItemStack> pairEntry : BlastFurnaceRecipes.getRecepiesforJEI().entrySet()){
			alloyFurnaceRecipes.add(new AlloyFurnaceRecipe(pairEntry.getKey(), pairEntry.getValue()));
		}
		return alloyFurnaceRecipes;
	}

	public static List<RBMKFuelRecipe> getRBMKFuelRecipes() {
		if(rbmkFuelRecipes != null)
			return rbmkFuelRecipes;
		rbmkFuelRecipes = new ArrayList<>();

		for(Map.Entry<ItemStack, ItemStack> pairEntry : RBMKFuelRecipes.recipes.entrySet()){
			rbmkFuelRecipes.add(new RBMKFuelRecipe(pairEntry.getKey(), pairEntry.getValue()));
		}
		return rbmkFuelRecipes;
	}
	
	public static List<ItemStack> getAlloyFuels() {
		if(alloyFuels != null)
			return alloyFuels;
		alloyFuels = BlastFurnaceRecipes.getAlloyFuels();
		return alloyFuels;
	}

    public static List<ItemStack> getBatteries() {
		if(batteries != null)
			return batteries;
		batteries = new ArrayList<>();
		batteries.add(new ItemStack(ModItems.battery_potato));
		batteries.add(new ItemStack(ModItems.battery_potatos));
		batteries.add(new ItemStack(ModItems.battery_generic));
		batteries.add(new ItemStack(ModItems.battery_red_cell));
		batteries.add(new ItemStack(ModItems.battery_red_cell_6));
		batteries.add(new ItemStack(ModItems.battery_red_cell_24));
		batteries.add(new ItemStack(ModItems.battery_advanced));
		batteries.add(new ItemStack(ModItems.battery_advanced_cell));
		batteries.add(new ItemStack(ModItems.battery_advanced_cell_4));
		batteries.add(new ItemStack(ModItems.battery_advanced_cell_12));
		batteries.add(new ItemStack(ModItems.battery_lithium));
		batteries.add(new ItemStack(ModItems.battery_lithium_cell));
		batteries.add(new ItemStack(ModItems.battery_lithium_cell_3));
		batteries.add(new ItemStack(ModItems.battery_lithium_cell_6));
		batteries.add(new ItemStack(ModItems.battery_schrabidium));
		batteries.add(new ItemStack(ModItems.battery_schrabidium_cell));
		batteries.add(new ItemStack(ModItems.battery_schrabidium_cell_2));
		batteries.add(new ItemStack(ModItems.battery_schrabidium_cell_4));
		batteries.add(new ItemStack(ModItems.battery_spark));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_6));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_25));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_100));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_1000));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_10000));
		batteries.add(new ItemStack(ModItems.battery_spark_cell_power));
		batteries.add(new ItemStack(ModItems.fusion_core));
		batteries.add(new ItemStack(ModItems.energy_core));
		for(ItemBatteryPack.EnumBatteryPack num : ItemBatteryPack.EnumBatteryPack.VALUES) batteries.add(new ItemStack(ModItems.battery_pack, 1, num.ordinal()));
		for(ItemBatterySC.EnumBatterySC num : ItemBatterySC.EnumBatterySC.VALUES) batteries.add(new ItemStack(ModItems.battery_sc, 1, num.ordinal()));
		return batteries;
	}
	
	public static List<CMBFurnaceRecipe> getCMBRecipes() {
		if(cmbRecipes != null)
			return cmbRecipes;
		cmbRecipes = new ArrayList<>();
		
		cmbRecipes.add(new CMBFurnaceRecipe(Arrays.asList(new ItemStack(ModItems.ingot_advanced_alloy), new ItemStack(ModItems.ingot_magnetized_tungsten)), new ItemStack(ModItems.ingot_combine_steel, 4)));
		cmbRecipes.add(new CMBFurnaceRecipe(Arrays.asList(new ItemStack(ModItems.powder_advanced_alloy), new ItemStack(ModItems.powder_magnetized_tungsten)), new ItemStack(ModItems.ingot_combine_steel, 4)));
		
		return cmbRecipes;
	}
	
	public static List<GasCentrifugeRecipe> getGasCentrifugeRecipes() {
		if(gasCentRecipes != null)
			return gasCentRecipes;
		gasCentRecipes = new ArrayList<>();

		Map<Object, Object[]> recipes = GasCentrifugeRecipes.getGasCentrifugeRecipes();

		for(Map.Entry<Object, Object[]> recipe : recipes.entrySet()) {
			gasCentRecipes.add(new GasCentrifugeRecipe((ItemStack) recipe.getKey(), (ItemStack[]) recipe.getValue()[0], (boolean) recipe.getValue()[1], (int) recipe.getValue()[2]));
		}
		
		return gasCentRecipes;
	}
	
	public static List<BookRecipe> getBookRecipes(){
		if(bookRecipes != null)
			return bookRecipes;
		bookRecipes = new ArrayList<>();
		for(MagicRecipe m : MagicRecipes.getRecipes()){
			bookRecipes.add(new BookRecipe(m));
		}
		return bookRecipes;
	}

	public static List<BreederRecipe> getBreederRecipes(){
		if(breederRecipes != null)
			return breederRecipes;
		breederRecipes = new ArrayList<>();
		HashMap<ItemStack, BreederRecipes.BreederRecipe> recipes = BreederRecipes.getAllRecipes();
		for(Map.Entry<ItemStack, BreederRecipes.BreederRecipe> recipe : recipes.entrySet()) {
			breederRecipes.add(new BreederRecipe(recipe.getKey(), recipe.getValue().output, recipe.getValue().flux));
		}

		return breederRecipes;
	}

    public static List<StorageDrumRecipe> getStorageDrumRecipes(){
		if(storageDrumRecipes != null)
			return storageDrumRecipes;
		storageDrumRecipes = new ArrayList<>();
		
		for(Map.Entry<ComparableStack, ItemStack> entry : StorageDrumRecipes.recipeOutputs.entrySet()){
			storageDrumRecipes.add(new StorageDrumRecipe(entry.getKey().getStack(), entry.getValue()));
		}
		
		return storageDrumRecipes;
	}

	public static List<TransmutationRecipe> getTransmutationRecipes(){
		if(transmutationRecipes != null)
			return transmutationRecipes;
		transmutationRecipes = new ArrayList<>();
		
		for(Map.Entry<AStack, ItemStack> entry : NuclearTransmutationRecipes.recipesOutput.entrySet()){
			transmutationRecipes.add(new TransmutationRecipe(entry.getKey().getStackList(), entry.getValue()));
		}
		
		return transmutationRecipes;
	}
	

	public static List<RefineryRecipe> getRefineryRecipe() {
		if(refineryRecipes != null)
			return refineryRecipes;
		refineryRecipes = new ArrayList<>();
		
		for(FluidType fluid : RefineryRecipes.refinery.keySet()){
			FluidStack[] outputFluids = new FluidStack[]{RefineryRecipes.refinery.get(fluid).getX(), RefineryRecipes.refinery.get(fluid).getY(), RefineryRecipes.refinery.get(fluid).getV(), RefineryRecipes.refinery.get(fluid).getW()};
			ItemStack outputItem = RefineryRecipes.refinery.get(fluid).getZ();
			refineryRecipes.add(new RefineryRecipe(
					ItemFluidIcon.make(fluid, 1000),
					Arrays.asList(
						ItemFluidIcon.make(outputFluids[0].type, outputFluids[0].fill * 10),
						ItemFluidIcon.make(outputFluids[1].type, outputFluids[1].fill * 10),
						ItemFluidIcon.make(outputFluids[2].type, outputFluids[2].fill * 10),
						ItemFluidIcon.make(outputFluids[3].type, outputFluids[3].fill * 10),
						outputItem.copy()
					)
				)
			);
		}
		return refineryRecipes;
	}
	
	public static List<ItemStack> getBlades() {
		if(blades != null)
			return blades;
		
		blades = new ArrayList<>();
		blades.add(new ItemStack(ModItems.blades_advanced_alloy));
		blades.add(new ItemStack(ModItems.blades_steel));
		blades.add(new ItemStack(ModItems.blades_titanium));
		return blades;
	}
	
	public static List<FluidRecipe> getFluidEquivalences(){
		if(fluidEquivalences != null)
			return fluidEquivalences;
		fluidEquivalences = new ArrayList<>();
		
		for(FluidContainerRegistry.FluidContainer container : FluidContainerRegistry.allContainers){
			if (container.emptyContainer() == null || container.emptyContainer().isEmpty()) {
				continue;
			}
			FluidType fluidType = container.type();
			ItemStack fullContainerStack = container.fullContainer();
			ItemStack fluidIconStack = ItemFluidIcon.make(fluidType, container.content());
			fluidEquivalences.add(new FluidRecipe(fluidIconStack, fullContainerStack.copy()));
			fluidEquivalences.add(new FluidRecipeInverse(fluidIconStack, fullContainerStack.copy()));
		}
		
		return fluidEquivalences;
	}

    public static List<HadronRecipe> getHadronRecipes(){
		if(hadronRecipes != null)
			return hadronRecipes;
		hadronRecipes = new ArrayList<>();
		for(HadronRecipes.HadronRecipe recipe : HadronRecipes.getRecipes()){
			hadronRecipes.add(new HadronRecipe(recipe.in1.toStack(), recipe.in2.toStack(), recipe.out1, recipe.out2, recipe.momentum, recipe.analysisOnly));
		}
		return hadronRecipes;
	}
	

	public static List<SILEXRecipe> getSILEXRecipes(EnumWavelengths wavelength){
		if(waveSilexRecipes.containsKey(wavelength))
			return waveSilexRecipes.get(wavelength);
		ArrayList<SILEXRecipe> wSilexRecipes = new ArrayList<>();
		for(Entry<List<ItemStack>, SILEXRecipes.SILEXRecipe> e : SILEXRecipes.getRecipes().entrySet()){
			SILEXRecipes.SILEXRecipe out = e.getValue();
			if(out.laserStrength == wavelength){
				double weight = 0;
				for(WeightedRandomObject obj : out.outputs) {
					weight += obj.itemWeight;
				}
				List<Double> chances = new ArrayList<>(out.outputs.size());
				List<ItemStack> outputs = new ArrayList<>(0);
				for(int i = 0; i < out.outputs.size(); i++) {
					WeightedRandomObject obj = out.outputs.get(i);
					outputs.add(obj.asStack());
					chances.add(100 * obj.itemWeight / weight);
				}
				wSilexRecipes.add(new SILEXRecipe(e.getKey(), chances, outputs, (double)out.fluidProduced/out.fluidConsumed, out.laserStrength));
			}
		}
		waveSilexRecipes.put(wavelength, wSilexRecipes);
		return wSilexRecipes;
	}


	public static List<SILEXRecipe> getSILEXRecipes(){
		if(silexRecipes != null)
			return silexRecipes;
		silexRecipes = new ArrayList<>();
		for(Entry<List<ItemStack>, SILEXRecipes.SILEXRecipe> e : SILEXRecipes.getRecipes().entrySet()){
			SILEXRecipes.SILEXRecipe out = e.getValue();
			double weight = 0;
			for(WeightedRandomObject obj : out.outputs) {
				weight += obj.itemWeight;
			}
			List<Double> chances = new ArrayList<>(out.outputs.size());
			List<ItemStack> outputs = new ArrayList<>(0);
			for(int i = 0; i < out.outputs.size(); i++) {
				WeightedRandomObject obj = out.outputs.get(i);
				outputs.add(obj.asStack());
				chances.add(100 * obj.itemWeight / weight);
			}
			silexRecipes.add(new SILEXRecipe(e.getKey(), chances, outputs, (double)out.fluidProduced/out.fluidConsumed, out.laserStrength));
		}
		return silexRecipes;
	}

	public static class AssemblerRecipeWrapper implements IRecipeWrapper {

		private final ItemStack output;
		private final List<List<ItemStack>> inputs;
		private final int time;
		private final ComparableStack outputComparable;

		AssemblerRecipeWrapper(ComparableStack output, AssemblerRecipes.AssemblerRecipe recipe) {
			this.outputComparable = output;
			this.output = output.toStack();
			this.time = recipe.time;

			List<List<ItemStack>> list = new ArrayList<>(recipe.ingredients.length);
			for (AStack s : recipe.ingredients) {
				list.add(s.getStackList());
			}
			this.inputs = list;
		}

		@Override
		public void getIngredients(@NotNull IIngredients ingredients) {
			List<List<ItemStack>> jeiInputs = Library.copyItemStackListList(inputs);
			while (jeiInputs.size() < 12) {
				jeiInputs.add(Collections.singletonList(new ItemStack(ModItems.nothing)));
			}
			ItemStack templateStack = ItemAssemblyTemplate.writeType(new ItemStack(ModItems.assembly_template), this.outputComparable);
			jeiInputs.add(Collections.singletonList(templateStack));

			ingredients.setInputLists(VanillaTypes.ITEM, jeiInputs);
			ingredients.setOutput(VanillaTypes.ITEM, output);
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
			String timeString = (time / 20.0) + "s";
			minecraft.fontRenderer.drawString(timeString, 112, 38, 0x404040);
		}
	}

	public static List<RadiolysisRecipeHandler.RadiolysisRecipe> getRadiolysisRecipes() {
		List<RadiolysisRecipeHandler.RadiolysisRecipe> jeiRecipes = new ArrayList<>();
		Map<Object, Object[]> recipes = RadiolysisRecipes.getRecipesForNEI();

		for (Map.Entry<Object, Object[]> entry : recipes.entrySet()) {
			ItemStack input = (ItemStack) entry.getKey();
			ItemStack output1 = (ItemStack) entry.getValue()[0];
			ItemStack output2 = (ItemStack) entry.getValue()[1];
			jeiRecipes.add(new RadiolysisRecipeHandler.RadiolysisRecipe(input, output1, output2));
		}
		return jeiRecipes;
	}
}
