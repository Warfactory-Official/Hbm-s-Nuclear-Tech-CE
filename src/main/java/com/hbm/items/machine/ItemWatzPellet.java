package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import com.hbm.util.EnumUtil;
import com.hbm.util.Function;
import com.hbm.util.Function.FunctionLinear;
import com.hbm.util.Function.FunctionQuadratic;
import com.hbm.util.Function.FunctionSqrt;
import com.hbm.util.Function.FunctionSqrtFalling;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

/*
 * Watz Isotropic Fuel, Oxidized
 */
public class ItemWatzPellet extends ItemEnumMulti<ItemWatzPellet.EnumWatzType> implements IDynamicModels {

    final boolean isDepleted;

    public ItemWatzPellet(String s, boolean isDepleted) {
        super(s, EnumWatzType.VALUES, true, true);
        this.setMaxStackSize(16);
        this.setTranslationKey(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.isDepleted = isDepleted;
    }

    public static int desaturate(int color) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);

        int avg = (r + g + b) / 3;
        double approach = 0.9;
        double mult = 0.75;

        r -= (r - avg) * approach;
        g -= (g - avg) * approach;
        b -= (b - avg) * approach;

        r *= mult;
        g *= mult;
        b *= mult;

        return (r << 16) | (g << 8) | b;
    }

    public static double getEnrichment(ItemStack stack) {
        EnumWatzType num = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());
        return getYield(stack) / num.yield;
    }

    public static double getYield(ItemStack stack) {
        return getDouble(stack, "yield");
    }

    public static void setYield(ItemStack stack, double yield) {
        setDouble(stack, "yield", yield);
    }

    public static void setDouble(ItemStack stack, String key, double yield) {
        if (!stack.hasTagCompound()) setNBTDefaults(stack);
        stack.getTagCompound().setDouble(key, yield);
    }

    public static double getDouble(ItemStack stack, String key) {
        if (!stack.hasTagCompound()) setNBTDefaults(stack);
        return stack.getTagCompound().getDouble(key);
    }

    private static void setNBTDefaults(ItemStack stack) {
        EnumWatzType num = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());
        stack.setTagCompound(new NBTTagCompound());
        setYield(stack, num.yield);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        for (int i = 0; i < EnumWatzType.VALUES.length; i++) {
            if (this.isDepleted) {
                ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(Tags.MODID + ":items/watz_pellet_depleted-" + i, "inventory"));
            } else {
                ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(Tags.MODID + ":items/watz_pellet-" + i, "inventory"));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        for (int j = 0; j <= 1; j++) {
            for (int i = 0; i < EnumWatzType.VALUES.length; i++) {
                EnumWatzType type = EnumWatzType.VALUES[i];
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, "items/watz_pellet" + (isDepleted ? "_depleted-" + i : "-" + i));
                int light = isDepleted ? desaturate(type.colorLight) : type.colorLight;
                int dark = isDepleted ? desaturate(type.colorDark) : type.colorDark;
                TextureAtlasSpriteMutatable mutableIcon = new TextureAtlasSpriteMutatable(spriteLoc.toString(), new RGBMutatorInterpolatedComponentRemap(0xD2D2D2, 0x333333, light, dark));
                map.setTextureEntry(mutableIcon);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        for (int j = 0; j <= 1; j++) {
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
                for (int i = 0; i < EnumWatzType.VALUES.length; i++) {
                    ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, "items/watz_pellet" + (isDepleted ? "_depleted-" + i : "-" + i));
                    IModel retexturedModel = baseModel.retexture(
                            ImmutableMap.of(
                                    "layer0", spriteLoc.toString()
                            )

                    );
                    IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation bakedModelLocation = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/watz_pellet" + (isDepleted ? "_depleted-" + i : "-" + i)), "inventory");
                    event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {

        if (this != ModItems.watz_pellet) return;

        EnumWatzType num = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());

        list.add(TextFormatting.GREEN + "Depletion: " + String.format(Locale.US, "%.1f", getDurabilityForDisplay(stack) * 100D) + "%");

        String color = TextFormatting.GOLD + "";
        String reset = TextFormatting.RESET + "";

        if (num.passive > 0) {
            list.add(color + "Base fission rate: " + reset + num.passive);
            list.add(TextFormatting.RED + "Self-igniting!");
        }
        if (num.heatEmission > 0) list.add(color + "Heat per flux: " + reset + num.heatEmission + " TU");
        if (num.burnFunc != null) {
            list.add(color + "Reaction function: " + reset + num.burnFunc.getLabelForFuel());
            list.add(color + "Fuel type: " + reset + num.burnFunc.getDangerFromFuel());
        }
        if (num.heatDiv != null)
            list.add(color + "Thermal multiplier: " + reset + num.heatDiv.getLabelForFuel() + " TU⁻¹");
        if (num.absorbFunc != null) list.add(color + "Flux capture: " + reset + num.absorbFunc.getLabelForFuel());
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return this == ModItems.watz_pellet && getDurabilityForDisplay(stack) > 0D;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1D - getEnrichment(stack);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        if (this != ModItems.watz_pellet) return;
        setNBTDefaults(stack); //minimize the window where NBT screwups can happen
    }

    public static enum EnumWatzType {

        SCHRABIDIUM(0x32FFFF, 0x005C5C, 2_000, 20D, 0.01D, new FunctionLinear(1.5D), new FunctionSqrtFalling(10D), null),
        HES(0x66DCD6, 0x023933, 1_750, 20D, 0.005D, new FunctionLinear(1.25D), new FunctionSqrtFalling(15D), null),
        MES(0xCBEADF, 0x28473C, 1_500, 15D, 0.0025D, new FunctionLinear(1.15D), new FunctionSqrtFalling(15D), null),
        LES(0xABB4A8, 0x0C1105, 1_250, 15D, 0.00125D, new FunctionLinear(1D), new FunctionSqrtFalling(20D), null),
        HEN(0xA6B2A6, 0x030F03, 0, 10D, 0.0005D, new FunctionSqrt(100), new FunctionSqrtFalling(10D), null),
        MEU(0xC1C7BD, 0x2B3227, 0, 10D, 0.0005D, new FunctionSqrt(75), new FunctionSqrtFalling(10D), null),
        MEP(0x9AA3A0, 0x111A17, 0, 15D, 0.0005D, new FunctionSqrt(150), new FunctionSqrtFalling(10D), null),
        LEAD(0xA6A6B2, 0x03030F, 0, 0, 0.0025D, null, null, new FunctionSqrt(10)), //standard absorber, negative coefficient
        BORON(0xBDC8D2, 0x29343E, 0, 0, 0.0025D, null, null, new FunctionLinear(10)), //improved absorber, linear
        DU(0xC1C7BD, 0x2B3227, 0, 0, 0.0025D, null, null, new FunctionQuadratic(1D, 1D).withDiv(100)), //absorber with positive coefficient
        NQD(0x4B4B4B, 0x121212, 2_000, 20, 0.01D, new FunctionLinear(2D), new FunctionSqrt(1D / 25D).withOff(25D * 25D), null),
        NQR(0x2D2D2D, 0x0B0B0B, 2_500, 30, 0.01D, new FunctionLinear(1.5D), new FunctionSqrt(1D / 25D).withOff(25D * 25D), null);

        public static final EnumWatzType[] VALUES = values();
        public final int colorLight;
        public final int colorDark;
        public final double mudContent;    //how much mud per reaction flux should be produced
        public final double passive;        //base flux emission
        public final double heatEmission;    //reactivity(1) to heat (heat per outgoing flux)
        public final Function burnFunc;    //flux to reactivity(0) (classic reactivity)
        public final Function heatDiv;    //reactivity(0) to reactivity(1) based on heat (temperature coefficient)
        public final Function absorbFunc;    //flux to heat (flux absobtion for non-active component)
        public double yield = 500_000_000;

        private EnumWatzType(int colorLight, int colorDark, double passive, double heatEmission, double mudContent, Function burnFunction, Function heatDivisor, Function absorbFunction) {
            this.colorLight = colorLight;
            this.colorDark = colorDark;
            this.passive = passive;
            this.heatEmission = heatEmission;
            this.mudContent = mudContent / 2D;
            this.burnFunc = burnFunction;
            this.heatDiv = heatDivisor;
            this.absorbFunc = absorbFunction;
        }
    }
}
