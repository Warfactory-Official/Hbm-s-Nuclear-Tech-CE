package com.hbm.items.special;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static com.hbm.inventory.material.Mats.*;
import static com.hbm.items.special.ItemBedrockOreNew.ProcessingTrait.*;

//TODO: fix IDynamicModels
public class ItemBedrockOreNew extends Item implements IModelRegister {

    private static final List<ItemBedrockOreNew> INSTANCES = new ArrayList<>();

    public ItemBedrockOreNew(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.partsTab);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);

        ModItems.ALL_ITEMS.add(this);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            INSTANCES.add(this);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int j = 0; j < BedrockOreType.VALUES.length; j++) {
                BedrockOreType type = BedrockOreType.VALUES[j];
                for (int i = 0; i < BedrockOreGrade.VALUES.length; i++) {
                    BedrockOreGrade grade = BedrockOreGrade.VALUES[i];
                    items.add(this.make(grade, type));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for(int i = 0; i < BedrockOreGrade.VALUES.length; i++) {
            BedrockOreGrade grade = BedrockOreGrade.VALUES[i];
            for (int j = 0; j < BedrockOreType.VALUES.length; j++) {
                BedrockOreType type = BedrockOreType.VALUES[j];
                String placeholderName = Tags.MODID + ":items/bedrock_ore_" + grade.prefix + "_" + type.suffix + "-" + (i * BedrockOreType.VALUES.length + j);
                ModelLoader.setCustomModelResourceLocation(this, grade.ordinal() << 4 | type.ordinal(), new ModelResourceLocation(placeholderName, "inventory"));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void bakeModels(ModelBakeEvent event){
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft",  "item/generated"));

            Map<ProcessingTrait, ResourceLocation> overlayLocations = new HashMap<>();
            for (ProcessingTrait trait : VALUES) {
                String overlayName = Tags.MODID + ":items/bedrock_ore_overlay." + trait.name().toLowerCase(Locale.US);
                overlayLocations.put(trait, new ResourceLocation(overlayName));
            }

            for(int i = 0; i < BedrockOreGrade.VALUES.length; i++) {
                BedrockOreGrade grade = BedrockOreGrade.VALUES[i];
                for (int j = 0; j < BedrockOreType.VALUES.length; j++) {
                    BedrockOreType type = BedrockOreType.VALUES[j];
                    String placeholderName = Tags.MODID + ":items/bedrock_ore_" + grade.prefix + "_" + type.suffix + "-" + (i * BedrockOreType.VALUES.length + j);
                    ResourceLocation spriteLoc = new ResourceLocation(placeholderName);
                    ImmutableMap.Builder<String, String> textureMapBuilder = ImmutableMap.builder();
                    textureMapBuilder.put("layer0", spriteLoc.toString());
                    int layerIndex = 1;
                    for (ProcessingTrait trait : grade.traits) {
                        ResourceLocation overlayLoc = overlayLocations.get(trait);
                        textureMapBuilder.put("layer" + layerIndex, overlayLoc.toString());
                        layerIndex++;
                    }

                    IModel retexturedModel = baseModel.retexture(textureMapBuilder.build());
                    IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation bakedModelLocation = new ModelResourceLocation(new ResourceLocation(placeholderName), "inventory");
                    event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

                }
            }
        }   catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerSprites(TextureMap map) {
        for(int i = 0; i < BedrockOreGrade.VALUES.length; i++) {
            BedrockOreGrade grade = BedrockOreGrade.VALUES[i];
            for (int j = 0; j < BedrockOreType.VALUES.length; j++) {
                BedrockOreType type = BedrockOreType.VALUES[j];
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID + ":items/bedrock_ore_" + grade.prefix + "_" + type.suffix + "-" + (i * BedrockOreType.VALUES.length + j));
                TextureAtlasSprite sprite = new TextureAtlasSpriteMutatable(spriteLoc.toString(), new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, type.light, type.dark));
                map.setTextureEntry(sprite);
            }
        }

        for (ProcessingTrait trait : ProcessingTrait.VALUES) {
            String overlayName = Tags.MODID + ":items/bedrock_ore_overlay." + trait.name().toLowerCase(Locale.US);
            map.registerSprite(new ResourceLocation(overlayName));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandlers(ColorHandlerEvent.Item evt) {
        ItemColors itemColors = evt.getItemColors();
        IItemColor handler = new BedrockOreColorHandler();

        for (ItemBedrockOreNew item : INSTANCES) {
            itemColors.registerItemColorHandler(handler, item);
        }
    }

    @SideOnly(Side.CLIENT)
    private static class BedrockOreColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex != 0) return 0xFFFFFF;

            ItemBedrockOreNew item = (ItemBedrockOreNew) stack.getItem();
            BedrockOreGrade grade = item.getGrade(stack.getItemDamage());
            return grade.tint;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();
        String type = I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".type." + this.getType(meta).suffix + ".name");
        return I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".grade." + this.getGrade(meta).name().toLowerCase(Locale.US) + ".name", type);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        for(ProcessingTrait trait : this.getGrade(stack.getItemDamage()).traits) {
            list.add(I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".trait." + trait.name().toLowerCase(Locale.US)));
        }
    }

    public static class BedrockOreOutput {
        public NTMMaterial mat;
        public int amount;
        public BedrockOreOutput(NTMMaterial mat, int amount) {
            this.mat = mat;
            this.amount = amount;
        }
    }

    public static BedrockOreOutput o(NTMMaterial mat, int amount) {
        return new BedrockOreOutput(mat, amount);
    }

    public static enum BedrockOreType {
        //												primary									sulfuric															solvent																		radsolvent
        LIGHT_METAL(	0xFFFFFF, 0x353535, "light",	o(MAT_IRON, 9),		o(MAT_COPPER, 9),	o(MAT_TITANIUM, 6),	o(MAT_BAUXITE, 9),	o(MAT_CRYOLITE, 3),	o(MAT_CHLOROCALCITE, 5),	o(MAT_LITHIUM, 5),		o(MAT_SODIUM, 3),		o(MAT_CHLOROCALCITE, 6),	o(MAT_LITHIUM, 6),		o(MAT_SODIUM, 6)),
        HEAVY_METAL(	0x868686, 0x000000, "heavy",	o(MAT_TUNGSTEN, 9),	o(MAT_LEAD, 9),		o(MAT_GOLD, 2),		o(MAT_GOLD, 2),			o(MAT_BERYLLIUM, 3),	o(MAT_TUNGSTEN, 9),			o(MAT_LEAD, 9),			o(MAT_GOLD, 5),			o(MAT_BISMUTH, 1),			o(MAT_BISMUTH, 1),		o(MAT_GOLD, 6)),
        RARE_EARTH(		0xE6E6B6, 0x1C1C00, "rare",		o(MAT_COBALT, 5),	o(MAT_RAREEARTH, 5),o(MAT_BORON, 5),	o(MAT_LANTHANIUM, 3),	o(MAT_NIOBIUM, 4),		o(MAT_NEODYMIUM, 3),		o(MAT_STRONTIUM, 3),	o(MAT_ZIRCONIUM, 3),	o(MAT_NIOBIUM, 5),			o(MAT_NEODYMIUM, 5),	o(MAT_STRONTIUM, 3)),
        ACTINIDE(		0xC1C7BD, 0x2B3227, "actinide",	o(MAT_URANIUM, 4),	o(MAT_THORIUM, 4),	o(MAT_RADIUM, 2),	o(MAT_RADIUM, 2),		o(MAT_POLONIUM, 2),		o(MAT_RADIUM, 2),			o(MAT_RADIUM, 2),		o(MAT_POLONIUM, 2),		o(MAT_TECHNETIUM, 1),		o(MAT_TECHNETIUM, 1),	o(MAT_U238, 1)),
        NON_METAL(		0xAFAFAF, 0x0F0F0F, "nonmetal",	o(MAT_COAL, 9),		o(MAT_SULFUR, 9),	o(MAT_LIGNITE, 9),	o(MAT_KNO, 6),			o(MAT_FLUORITE, 6),		o(MAT_PHOSPHORUS, 5),		o(MAT_FLUORITE, 6),		o(MAT_SULFUR, 6),		o(MAT_CHLOROCALCITE, 6),	o(MAT_SILICON, 2),		o(MAT_SILICON, 2)),
        CRYSTALLINE(	0xE2FFFA, 0x1E8A77, "crystal",	o(MAT_REDSTONE, 9),	o(MAT_CINNABAR, 4),	o(MAT_SODALITE, 9),	o(MAT_ASBESTOS, 6),		o(MAT_DIAMOND, 3),		o(MAT_CINNABAR, 3),			o(MAT_ASBESTOS, 5),		o(MAT_EMERALD, 3),		o(MAT_BORAX, 3),			o(MAT_MOLYSITE, 3),		o(MAT_SODALITE, 9));
        //sediment

        public static final BedrockOreType[] VALUES = values();

        public int light;
        public int dark;
        public String suffix;
        public BedrockOreOutput primary1, primary2;
        public BedrockOreOutput byproductAcid1, byproductAcid2, byproductAcid3;
        public BedrockOreOutput byproductSolvent1, byproductSolvent2, byproductSolvent3;
        public BedrockOreOutput byproductRad1, byproductRad2, byproductRad3;

        private BedrockOreType(int light, int dark, String suffix, BedrockOreOutput p1, BedrockOreOutput p2, BedrockOreOutput bA1, BedrockOreOutput bA2, BedrockOreOutput bA3, BedrockOreOutput bS1, BedrockOreOutput bS2, BedrockOreOutput bS3, BedrockOreOutput bR1, BedrockOreOutput bR2, BedrockOreOutput bR3) {
            this.light = light;
            this.dark = dark;
            this.suffix = suffix;
            this.primary1 = p1; this.primary2 = p2;
            this.byproductAcid1 = bA1; this.byproductAcid2 = bA2; this.byproductAcid3 = bA3;
            this.byproductSolvent1 = bS1; this.byproductSolvent2 = bS2; this.byproductSolvent3 = bS3;
            this.byproductRad1 = bR1; this.byproductRad2 = bR2; this.byproductRad3 = bR3;
        }
    }

    public static MaterialStack toFluid(BedrockOreOutput o, double amount) {
        if(o.mat != null && o.mat.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE) {
            return new MaterialStack(o.mat, (int) Math.ceil(MaterialShapes.FRAGMENT.q(o.amount) * amount));
        }
        return null;
    }

    public static ItemStack extract(BedrockOreOutput o, double amount) {
        return new ItemStack(ModItems.bedrock_ore_fragment, Math.min((int) Math.ceil(o.amount * amount), 64), o.mat.id);
    }

    public static final int none = 0xFFFFFF;
    public static final int roasted = 0xCFCFCF;
    public static final int arc = 0xC3A2A2;
    public static final int washed = 0xDBE2CB;

    public static enum ProcessingTrait {
        ROASTED,
        ARC,
        WASHED,
        CENTRIFUGED,
        SULFURIC,
        SOLVENT,
        RAD;

        public static final ProcessingTrait[] VALUES = values();
    }

    public static enum BedrockOreGrade {
        BASE(none, "base"),												//from the slopper
        BASE_ROASTED(roasted, "base", ROASTED),							//optional combination oven step, yields vitriol
        BASE_WASHED(washed, "base", WASHED),							//primitive-ass acidizer with water
        PRIMARY(none, "primary", CENTRIFUGED),							//centrifuging for more primary
        PRIMARY_ROASTED(roasted, "primary", ROASTED),					//optional comboven
        PRIMARY_SULFURIC(0xFFFFD3, "primary", SULFURIC),				//sulfuric acid
        PRIMARY_NOSULFURIC(0xD3D4FF, "primary", CENTRIFUGED, SULFURIC),	//from centrifuging, sulfuric byproduct removed
        PRIMARY_SOLVENT(0xD3F0FF, "primary", SOLVENT),					//solvent
        PRIMARY_NOSOLVENT(0xFFDED3, "primary", CENTRIFUGED, SOLVENT),	//solvent byproduct removed
        PRIMARY_RAD(0xECFFD3, "primary", RAD),							//radsolvent
        PRIMARY_NORAD(0xEBD3FF, "primary", CENTRIFUGED, RAD),			//radsolvent byproduct removed
        PRIMARY_FIRST(0xFFD3D4, "primary", CENTRIFUGED),				//higher first material yield
        PRIMARY_SECOND(0xD3FFEB, "primary", CENTRIFUGED),				//higher second material yield
        CRUMBS(none, "crumbs", CENTRIFUGED),							//endpoint for primary, recycling

        SULFURIC_BYPRODUCT(none, "sulfuric", CENTRIFUGED, SULFURIC),	//from centrifuging
        SULFURIC_ROASTED(roasted, "sulfuric", ROASTED, SULFURIC),		//comboven again
        SULFURIC_ARC(arc, "sulfuric", ARC, SULFURIC),					//alternate step
        SULFURIC_WASHED(washed, "sulfuric", WASHED, SULFURIC),			//sulfuric endpoint

        SOLVENT_BYPRODUCT(none, "solvent", CENTRIFUGED, SOLVENT),		//from centrifuging
        SOLVENT_ROASTED(roasted, "solvent", ROASTED, SOLVENT),			//comboven again
        SOLVENT_ARC(arc, "solvent", ARC, SOLVENT),						//alternate step
        SOLVENT_WASHED(washed, "solvent", WASHED, SOLVENT),				//solvent endpoint

        RAD_BYPRODUCT(none, "rad", CENTRIFUGED, RAD),					//from centrifuging
        RAD_ROASTED(roasted, "rad", ROASTED, RAD),						//comboven again
        RAD_ARC(arc, "rad", ARC, RAD),									//alternate step
        RAD_WASHED(washed, "rad", WASHED, RAD);							//rad endpoint

        public static final BedrockOreGrade[] VALUES = values();

        public int tint;
        public String prefix;
        public ProcessingTrait[] traits;

        private BedrockOreGrade(int tint, String prefix, ProcessingTrait... traits) {
            this.tint = tint;
            this.prefix = prefix;
            this.traits = traits;
        }
    }

    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type) {
        return make(grade, type, 1);
    }

    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type, int amount) {
        return new ItemStack(ModItems.bedrock_ore, amount, grade.ordinal() << 4 | type.ordinal());
    }

    public BedrockOreGrade getGrade(int meta) {
        return EnumUtil.grabEnumSafely(BedrockOreGrade.VALUES, meta >> 4);
    }

    public BedrockOreType getType(int meta) {
        return EnumUtil.grabEnumSafely(BedrockOreType.VALUES, meta & 15);
    }
}
