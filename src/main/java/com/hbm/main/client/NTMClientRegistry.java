package com.hbm.main.client;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.Balefire;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.network.FluidDuctBox;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.forgefluid.SpecialContainerFillLists;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.items.RBMKItemRenderers;
import com.hbm.items.gear.RedstoneSword;
import com.hbm.items.machine.*;
import com.hbm.items.special.*;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.items.tool.ItemCanister;
import com.hbm.items.tool.ItemGasCanister;
import com.hbm.items.tool.ItemGuideBook;
import com.hbm.items.weapon.IMetaItemTesr;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.GuiCTMWarning;
import com.hbm.render.icon.RegistrationUtils;
import com.hbm.render.item.*;
import com.hbm.render.item.weapon.B92BakedModel;
import com.hbm.render.item.weapon.ItemRedstoneSwordRender;
import com.hbm.render.item.weapon.ItemRenderGunAnim;
import com.hbm.render.item.weapon.ItemRenderRedstoneSword;
import com.hbm.render.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;


/**
 * Handles all loadtime/registry clientside stuff.
 *
 */
public class NTMClientRegistry {


    public static TextureAtlasSprite contrail;
    public static TextureAtlasSprite particle_base;
    public static TextureAtlasSprite fog;
    public static TextureAtlasSprite uv_debug;
    public static TextureAtlasSprite debugPower;
    public static TextureAtlasSprite debugFluid;
    //Lazy, I know
    // 0 - CTM exists
    // 1 - No CTM, Player didn't acknowledge
    // 2 - No CTM, Player acknowledge
    public static boolean ctmWarning = false;

    @SubscribeEvent
    public void itemColorsEvent(ColorHandlerEvent.Item evt) {
        IItemColor fluidMetaHandler = (stack, tintIndex) -> {
            if (tintIndex == 1) {
                return Fluids.fromID(stack.getMetadata()).getColor();
            }
            return 0xFFFFFF;
        };
        evt.getItemColors().registerItemColorHandler((ItemStack stack, int tintIndex) -> {
            if (tintIndex == 1) {
                int j = ItemCassette.TrackType.byIndex(stack.getItemDamage()).getColor();
                if (j < 0) j = 0xFFFFFF;
                return j;
            }
            return 0xFFFFFF;
        }, ModItems.siren_track);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 0) {
                ItemICFPellet.EnumICFFuel type1 = ItemICFPellet.getType(stack, true);
                ItemICFPellet.EnumICFFuel type2 = ItemICFPellet.getType(stack, false);
                int r = (((type1.color & 0xff0000) >> 16) + ((type2.color & 0xff0000) >> 16)) / 2;
                int g = (((type1.color & 0x00ff00) >> 8) + ((type2.color & 0x00ff00) >> 8)) / 2;
                int b = ((type1.color & 0x0000ff) + (type2.color & 0x0000ff)) / 2;
                return (r << 16) | (g << 8) | b;
            }
            return 0xFFFFFF;
        }, ModItems.icf_pellet);
        evt.getItemColors().registerItemColorHandler(fluidMetaHandler, ModItems.fluid_tank_full);
        evt.getItemColors().registerItemColorHandler(fluidMetaHandler, ModItems.fluid_tank_lead_full);
        evt.getItemColors().registerItemColorHandler(fluidMetaHandler, ModItems.fluid_barrel_full);
        evt.getItemColors().registerItemColorHandler(fluidMetaHandler, ModItems.disperser_canister);
        evt.getItemColors().registerItemColorHandler(fluidMetaHandler, ModItems.glyphid_gland);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 0) {
                return ItemFluidIcon.getFluidType(stack).getColor();
            }
            return 0xFFFFFF;
        }, ModItems.fluid_icon);
        evt.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFF;
            if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("liquid")) {
                NTMMaterial mat = Mats.matById.get(stack.getMetadata());
                if (mat != null) {
                    return mat.moltenColor;
                }
            }
            return 0xFFFFFF;
        }, ModItems.scraps);
        //TODO: Move to IDynamicModels
        ItemDepletedFuel.registerColorHandlers(evt);
        ItemBedrockOreNew.registerColorHandlers(evt);
        ItemFFFluidDuct.registerColorHandlers(evt);
        ItemGasCanister.registerColorHandler(evt);
        ItemAutogen.registerColorHandlers(evt);
        IDynamicModels.registerItemColorHandlers(evt);
        ItemChemicalDye.registerColorHandlers(evt);
        ItemKitCustom.registerColorHandlers(evt);
    }

    @SubscribeEvent
    public void blockColorsEvent(ColorHandlerEvent.Block evt) {
        FluidDuctBox.registerColorHandler(evt);
        FluidDuctStandard.registerColorHandler(evt);
        IDynamicModels.registerBlockColorHandlers(evt);
        Balefire.registerColorHandler(evt);
    }

    @SubscribeEvent
    public void textureStitch(TextureStitchEvent.Pre evt) {
        TextureMap map = evt.getMap();
        ItemBedrockOreNew.registerSprites(map);
        ItemMold.registerSprites(map);
        ItemAutogen.registerSprites(map);

        IDynamicModels.registerSprites(map);

        //Debug stuff
        debugPower = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/debug_power"));
        debugFluid = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/debug_fluid"));
        uv_debug = map.registerSprite(new ResourceLocation(Tags.MODID, "misc/uv_debug"));
        contrail = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/contrail"));
        particle_base = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/particle_base"));
        fog = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/fog"));

        RegistrationUtils.registerInFolder(map,"textures/blocks/forgefluid");


        map.registerSprite(new ResourceLocation(Tags.MODID, "items/ore_bedrock_layer"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_identifier_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_barrel_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_tank_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_tank_lead_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/chemical_dye_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/crayon_overlay"));
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post evt) {


        RenderStructureMarker.fusion[0][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/block_steel");
        RenderStructureMarker.fusion[0][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_conductor_side_alt3");
        RenderStructureMarker.fusion[1][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_heater_top");
        RenderStructureMarker.fusion[1][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_heater_side");
        RenderStructureMarker.fusion[2][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/block_tungsten");
        RenderStructureMarker.fusion[2][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_hatch");
        RenderStructureMarker.fusion[3][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_motor_top_alt");
        RenderStructureMarker.fusion[3][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_motor_side_alt");
        RenderStructureMarker.fusion[4][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_center_top_alt");
        RenderStructureMarker.fusion[4][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_center_side_alt");
        RenderStructureMarker.fusion[5][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_center_top_alt");
        RenderStructureMarker.fusion[5][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/fusion_core_side_alt");
        RenderStructureMarker.fusion[6][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/block_tungsten");
        RenderStructureMarker.fusion[6][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/block_tungsten");

        RenderStructureMarker.watz[0][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[0][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[1][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/reinforced_brick");
        RenderStructureMarker.watz[1][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_hatch");
        RenderStructureMarker.watz[2][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_control_top");
        RenderStructureMarker.watz[2][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_control_side");
        RenderStructureMarker.watz[3][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_end");
        RenderStructureMarker.watz[3][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_end");
        RenderStructureMarker.watz[4][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_conductor_top");
        RenderStructureMarker.watz[4][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_conductor_side");
        RenderStructureMarker.watz[5][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_computer");
        RenderStructureMarker.watz[5][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_computer");
        RenderStructureMarker.watz[6][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_cooler");
        RenderStructureMarker.watz[6][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_cooler");
        RenderStructureMarker.watz[7][0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_element_top");
        RenderStructureMarker.watz[7][1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_element_side");

        RenderMultiblock.structLauncher = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/struct_launcher");
        RenderMultiblock.structScaffold = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/struct_scaffold");

        RenderSoyuzMultiblock.blockIcons[0] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/struct_launcher");
        RenderSoyuzMultiblock.blockIcons[1] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/concrete");
        RenderSoyuzMultiblock.blockIcons[2] = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/struct_scaffold");

        RenderWatzMultiblock.casingSprite = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_casing_tooled");
        RenderWatzMultiblock.coolerSpriteSide = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_cooler_side");
        RenderWatzMultiblock.coolerSpriteTop = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_cooler_top");
        RenderWatzMultiblock.elementSpriteSide = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_element_side");
        RenderWatzMultiblock.elementSpriteTop = evt.getMap().getAtlasSprite(Tags.MODID + ":blocks/watz_element_top");

        RenderICFMultiblock.componentSprite0 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component");
        RenderICFMultiblock.componentSprite2 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component.vessel_welded");
        RenderICFMultiblock.componentSprite4 = evt.getMap().getAtlasSprite("hbm:blocks/icf_component.structure_bolted");

        RenderFusionTorusMultiblock.componentSprites[1] = evt.getMap().getAtlasSprite("hbm:blocks/fusion_component.bscco_welded");
        RenderFusionTorusMultiblock.componentSprites[2] = evt.getMap().getAtlasSprite("hbm:blocks/fusion_component.blanket");
        RenderFusionTorusMultiblock.componentSprites[3] = evt.getMap().getAtlasSprite("hbm:blocks/fusion_component.motor");
    }

    public static void swapModels(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        IBakedModel model = reg.getObject(loc);
        TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
        if (render instanceof TEISRBase) {
            ((TEISRBase) render).itemModel = model;
            reg.putObject(loc, new BakedModelCustom((TEISRBase) render));
        }
    }

    public static void swapModelsNoGui(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        IBakedModel model = reg.getObject(loc);
        TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
        if (render instanceof TEISRBase) {
            ((TEISRBase) render).itemModel = model;
            reg.putObject(loc, new BakedModelNoGui((TEISRBase) render));
        }
    }

    public static void swapModelsNoFPV(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        IBakedModel model = reg.getObject(loc);
        TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
        if (render instanceof TEISRBase) {
            ((TEISRBase) render).itemModel = model;
            reg.putObject(loc, new BakedModelNoFPV((TEISRBase) render, model));
        }
    }
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!ctmWarning) return;
        if (event.getGui() instanceof net.minecraft.client.gui.GuiMainMenu) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiCTMWarning());
            ctmWarning = false;
        }
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        int i = 0;
        ResourceLocation[] list = new ResourceLocation[SpecialContainerFillLists.EnumCell.VALUES.length];
        for (SpecialContainerFillLists.EnumCell e : SpecialContainerFillLists.EnumCell.VALUES) {
            list[i] = e.getResourceLocation();
            i++;
        }
        ModelLoader.registerItemVariants(ModItems.cell, list);

        FluidType[] order = Fluids.getInNiceOrder();
        for (i = 0; i < order.length; i++) {
            if (!order[i].hasNoID()) {
                ModelLoader.setCustomModelResourceLocation(ModItems.fluid_duct, order[i].getID(), ItemFFFluidDuct.ductLoc);
                if (order[i].getContainer(Fluids.CD_Gastank.class) != null) {
                    ModelLoader.setCustomModelResourceLocation(ModItems.gas_full, order[i].getID(), ItemGasCanister.gasCanisterFullModel);
                }
            }
        }
        ModelLoader.setCustomModelResourceLocation(ModItems.canister_empty, 0, ItemCanister.fluidCanisterModel);
        ModelLoader.setCustomModelResourceLocation(ModItems.icf_pellet, 0, new ModelResourceLocation(ModItems.icf_pellet.getRegistryName(), "inventory"));

        ModelResourceLocation clayTabletModel = new ModelResourceLocation(ModItems.clay_tablet.getRegistryName(), "inventory");

        ModelLoader.setCustomModelResourceLocation(ModItems.clay_tablet, 0, clayTabletModel);
        ModelLoader.setCustomModelResourceLocation(ModItems.clay_tablet, 1, clayTabletModel);

        for (Item item : ModItems.ALL_ITEMS) {
            try {
                registerModel(item, 0);
            } catch (NullPointerException e) {
                e.printStackTrace();
                MainRegistry.logger.info("Failed to register model for " + item.getRegistryName());
            }
        }
        for (Block block : ModBlocks.ALL_BLOCKS) {
            if (block instanceof IDynamicModels && IDynamicModels.INSTANCES.contains(block)) continue;
            registerBlockModel(block, 0);
        }

        IDynamicModels.registerModels();
        IDynamicModels.registerCustomStateMappers();
        IMetaItemTesr.redirectModels();

        ModelLoader.setCustomModelResourceLocation(ModItems.conveyor_wand, 0, new ModelResourceLocation(ModBlocks.conveyor.getRegistryName(),
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.conveyor_wand, 1, new ModelResourceLocation(ModBlocks.conveyor_express.getRegistryName(),
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.conveyor_wand, 2, new ModelResourceLocation(ModBlocks.conveyor_double.getRegistryName(),
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(ModItems.conveyor_wand, 3, new ModelResourceLocation(ModBlocks.conveyor_triple.getRegistryName(),
                "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.fence_metal), 1, new ModelResourceLocation("hbm:fence_metal_post", "inventory"));
    }

    private void registerBlockModel(Block block, int meta) {
        registerModel(Item.getItemFromBlock(block), meta);
    }

    private void registerModel(Item item, int meta) {
        if (item == Items.AIR)
            return;

        //Drillgon200: I hate myself for making this
        //Th3_Sl1ze: Don't worry, I hate myself too
        if (item == ModItems.chemistry_template) {
            ChemplantRecipes.register();
        }


        if (item instanceof ItemDepletedFuel) {
            for (int i = 0; i <= 1; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i,
                        new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
            }
            return;
        }
        if (item instanceof IModelRegister) {
            ((IModelRegister) item).registerModels();
            return;
        }

        if (item == ModItems.chemistry_icon) {
            for (int i : ChemplantRecipes.recipeNames.keySet()) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(Tags.MODID + ":chem_icon_" + ChemplantRecipes.getName(i).toLowerCase(), "inventory"));
            }
        } else if (item == ModItems.chemistry_template) {
            for (int i : ChemplantRecipes.recipeNames.keySet()) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.crucible_template) {
            for (int i = 0; i < 32; i++) { // FIXME: figure out a better way of doing this
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.siren_track) {
            for (int i = 0; i < ItemCassette.TrackType.VALUES.size(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.ingot_u238m2) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 1, new ModelResourceLocation(Tags.MODID + ":hs-elements", "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 2, new ModelResourceLocation(Tags.MODID + ":hs-arsenic", "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 3, new ModelResourceLocation(Tags.MODID + ":hs-vault", "inventory"));
        } else if (item == ModItems.polaroid || item == ModItems.glitch) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName() + "_" + MainRegistry.polaroidID, "inventory"));
        } else if (item == Item.getItemFromBlock(ModBlocks.brick_jungle_glyph)) {
            for (int i = 0; i < 16; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName().toString() + i, "inventory"));
        } else if (item == Item.getItemFromBlock(ModBlocks.brick_jungle_trap)) {
            for (int i = 0; i < TrappedBrick.Trap.VALUES.length; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof ItemGuideBook) {
            for (int i = 0; i < ItemGuideBook.BookType.VALUES.length; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof ItemHot) {
            for (int i = 0; i < 15; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(item, 15, new ModelResourceLocation(item.getRegistryName() + "_hot", "inventory"));
        } else if (item instanceof ItemRBMKPellet) {
            for (int xe = 0; xe < 2; xe++) {
                for (int en = 0; en < 5; en++) {
                    ModelLoader.setCustomModelResourceLocation(item, en + xe * 5, new ModelResourceLocation(item.getRegistryName() + "_e" + en + (xe > 0 ? "_xe" : ""), "inventory"));
                }
            }
        } else if (item instanceof ItemWasteLong) {
            for (int i = 0; i < ItemWasteLong.WasteClass.VALUES.length; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item instanceof ItemWasteShort) {
            for (int i = 0; i < ItemWasteShort.WasteClass.VALUES.length; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.coin_siege) {
            for (int i = 0; i < SiegeTier.getLength(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(Tags.MODID + ":coin_siege_" + SiegeTier.tiers[i].name, "inventory"));
            }
        } else if (item == Item.getItemFromBlock(ModBlocks.volcano_core)) {
            for (int i = 0; i < 4; i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        } else if (item == ModItems.fluid_identifier_multi) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        } else if (item instanceof IHasCustomModel) {
            ModelLoader.setCustomModelResourceLocation(item, meta, ((IHasCustomModel) item).getResourceLocation());
        } else if (item instanceof IDynamicModels && IDynamicModels.INSTANCES.contains(item)) { // we are literally registering them manually, why do it twice?..
        } else {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onModelBake(ModelBakeEvent evt) {
        ItemBedrockOreNew.bakeModels(evt);
        ItemAutogen.bakeModels(evt);
        ItemMold.bakeModels(evt);
        IDynamicModels.bakeModels(evt);


        for (SpecialContainerFillLists.EnumCanister e : SpecialContainerFillLists.EnumCanister.VALUES) {
            Object o = evt.getModelRegistry().getObject(e.getResourceLocation());
            if (o instanceof IBakedModel)
                e.putRenderModel((IBakedModel) o);
        }
        for (SpecialContainerFillLists.EnumCell cellType : SpecialContainerFillLists.EnumCell.VALUES) {
            FluidType fluid = cellType.getFluid();
            int meta = (fluid == null) ? 0 : fluid.getID();
            ModelLoader.setCustomModelResourceLocation(
                    ModItems.cell,
                    meta,
                    cellType.getResourceLocation()
            );
        }
        for (SpecialContainerFillLists.EnumGasCanister e : SpecialContainerFillLists.EnumGasCanister.VALUES) {
            Object o = evt.getModelRegistry().getObject(e.getResourceLocation());
            if (o instanceof IBakedModel)
                e.putRenderModel((IBakedModel) o);
        }

        ResourceManager.init();
        Object object1 = evt.getModelRegistry().getObject(RedstoneSword.rsModel);
        if (object1 instanceof IBakedModel) {
            IBakedModel model = (IBakedModel) object1;
            ItemRedstoneSwordRender.INSTANCE.itemModel = model;
            evt.getModelRegistry().putObject(RedstoneSword.rsModel, new ItemRenderRedstoneSword());
        }
        wrapModel(evt, ItemAssemblyTemplate.location);
        wrapModel(evt, ItemChemistryTemplate.location);
        wrapModel(evt, ItemCrucibleTemplate.location);
        Object object3 = evt.getModelRegistry().getObject(GunB92.b92Model);
        if (object3 instanceof IBakedModel) {
            IBakedModel model = (IBakedModel) object3;
            ItemRenderGunAnim.INSTANCE.b92ItemModel = model;
            evt.getModelRegistry().putObject(GunB92.b92Model, new B92BakedModel());
        }

        IRegistry<ModelResourceLocation, IBakedModel> reg = evt.getModelRegistry();
        swapModelsNoGui(ModItems.gun_b93, reg);
        swapModelsNoGui(ModItems.gun_supershotgun, reg);
        swapModels(ModItems.cell, reg);
        swapModels(ModItems.gas_empty, reg);
        swapModelsNoGui(ModItems.multitool_dig, reg);
        swapModelsNoGui(ModItems.multitool_silk, reg);
        swapModelsNoGui(ModItems.multitool_ext, reg);
        swapModelsNoGui(ModItems.multitool_miner, reg);
        swapModelsNoGui(ModItems.multitool_hit, reg);
        swapModelsNoGui(ModItems.multitool_beam, reg);
        swapModelsNoGui(ModItems.multitool_sky, reg);
        swapModelsNoGui(ModItems.multitool_mega, reg);
        swapModelsNoGui(ModItems.multitool_joule, reg);
        swapModelsNoGui(ModItems.multitool_decon, reg);
        swapModelsNoGui(ModItems.big_sword, reg);
        swapModelsNoGui(ModItems.shimmer_sledge, reg);
        swapModelsNoGui(ModItems.shimmer_axe, reg);
        swapModels(ModItems.fluid_duct, reg);
        swapModelsNoGui(ModItems.stopsign, reg);
        swapModelsNoGui(ModItems.sopsign, reg);
        swapModelsNoGui(ModItems.chernobylsign, reg);
        swapModels(Item.getItemFromBlock(ModBlocks.radiorec), reg);
        swapModels(ModItems.gun_vortex, reg);
        swapModelsNoGui(ModItems.wood_gavel, reg);
        swapModelsNoGui(ModItems.lead_gavel, reg);
        swapModelsNoGui(ModItems.diamond_gavel, reg);
        swapModelsNoGui(ModItems.mese_gavel, reg);
        swapModels(ModItems.ingot_steel_dusted, reg);
        swapModels(ModItems.ingot_chainsteel, reg);
        swapModels(ModItems.ingot_meteorite, reg);
        swapModels(ModItems.ingot_meteorite_forged, reg);
        swapModels(ModItems.blade_meteorite, reg);
        swapModels(ModItems.crucible, reg);
        swapModels(ModItems.hs_sword, reg);
        swapModels(ModItems.hf_sword, reg);
        swapModels(ModItems.gun_egon, reg);
        swapModels(ModItems.jshotgun, reg);

        swapModels(ModItems.meteorite_sword_seared, reg);
        swapModels(ModItems.meteorite_sword_reforged, reg);
        swapModels(ModItems.meteorite_sword_hardened, reg);
        swapModels(ModItems.meteorite_sword_alloyed, reg);
        swapModels(ModItems.meteorite_sword_machined, reg);
        swapModels(ModItems.meteorite_sword_treated, reg);
        swapModels(ModItems.meteorite_sword_etched, reg);
        swapModels(ModItems.meteorite_sword_bred, reg);
        swapModels(ModItems.meteorite_sword_irradiated, reg);
        swapModels(ModItems.meteorite_sword_fused, reg);
        swapModels(ModItems.meteorite_sword_baleful, reg);

        swapModelsNoGui(ModItems.bedrock_ore, reg);
        swapModels(ModItems.detonator_laser, reg);
        swapModels(ModItems.boltgun, reg);

        swapModels(ModItems.fluid_barrel_full, reg);
        swapModels(ModItems.fluid_tank_full, reg);
        swapModels(ModItems.fluid_tank_lead_full, reg);

        swapModels(ModItems.ammo_himars, reg);
        swapModels(ModItems.jetpack_glider, reg);
        swapModels(ModItems.gear_large, reg);
        swapModels(ModItems.battery_pack, reg);

        swapModels(Item.getItemFromBlock(ModBlocks.boat), reg);

        for (Item item : ItemGunBaseNT.INSTANCES) {
            swapModelsNoFPV(item, reg);
        }

        for (Item item : RBMKItemRenderers.itemRenderers.keySet()) {
            swapModels(item, reg);
        }

        for (TileEntitySpecialRenderer<? extends TileEntity> renderer : TileEntityRendererDispatcher.instance.renderers.values()) {
            if (renderer instanceof IItemRendererProvider prov) {
                for (Item item : prov.getItemsForRenderer()) {
                    swapModels(item, reg);
                }
            }
        }

        for (Item renderer : Item.REGISTRY) {
            if (renderer instanceof IItemRendererProvider provider) {
                for (Item item : provider.getItemsForRenderer()) {
                    swapModels(item, reg);
                }
            }
        }

        MainRegistry.proxy.registerMissileItems(reg);
    }

    private void wrapModel(ModelBakeEvent event, ModelResourceLocation location) {
        IBakedModel existingModel = event.getModelRegistry().getObject(location);
        if (existingModel != null && !(existingModel instanceof TemplateBakedModel)) {
            TemplateBakedModel wrapper = new TemplateBakedModel(existingModel);
            event.getModelRegistry().putObject(location, wrapper);
        }
    }

}
