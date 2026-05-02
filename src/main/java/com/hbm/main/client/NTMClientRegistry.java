package com.hbm.main.client;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.Balefire;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.machine.BlockSeal;
import com.hbm.blocks.network.FluidDuctBox;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.entity.siege.SiegeTier;
import com.hbm.forgefluid.SpecialContainerFillLists;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.items.gear.RedstoneSword;
import com.hbm.items.machine.*;
import com.hbm.items.special.*;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.items.tool.ItemCanister;
import com.hbm.items.tool.ItemGasCanister;
import com.hbm.items.tool.ItemGuideBook;
import com.hbm.items.weapon.IMetaItemTesr;
import com.hbm.main.AutoRegistry;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.GuiCTMWarning;
import com.hbm.render.entity.RenderBoat;
import com.hbm.render.icon.RegistrationUtils;
import com.hbm.render.item.BakedModelNoFPV;
import com.hbm.render.item.FancyMissingModelPerspective;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.item.WrappedTEISRModel;
import com.hbm.render.item.weapon.B92BakedModel;
import com.hbm.render.item.weapon.ItemRedstoneSwordRender;
import com.hbm.render.item.weapon.ItemRenderGunAnim;
import com.hbm.render.item.weapon.ItemRenderRedstoneSword;
import com.hbm.render.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;


/**
 * Handles all loadtime/registry clientside stuff.
 *
 */
public class NTMClientRegistry {
    private static boolean itemRendererBindingsInitialized = false;
    public static TextureAtlasSprite contrail;
    public static TextureAtlasSprite particle_base;
    public static TextureAtlasSprite fog;
    public static TextureAtlasSprite debugPower;
    public static TextureAtlasSprite debugFluid;
    //Lazy, I know
    // 0 - CTM exists
    // 1 - No CTM, Player didn't acknowledge
    // 2 - No CTM, Player acknowledge
    public static boolean ctmWarning = false;

    public static void bindTeisr(Item item, TileEntityItemStackRenderer renderer) {
        ClaimedModelLocationRegistry.unregisterTeisrBinding(item);
        item.setTileEntityItemStackRenderer(renderer);
        if (renderer instanceof TEISRBase teisr) {
            ClaimedModelLocationRegistry.registerTeisrBinding(new OwnedTeisrBinding(item, teisr));
        }
    }

    public static void bindTeisrs(Iterable<IItemRendererProvider> providers) {
        for (IItemRendererProvider provider : providers) {
            for (Item item : provider.getItemsForRenderer()) {
                bindTeisr(item, provider.getRenderer(item));
            }
        }
    }

    private static void initializeItemRendererBindings() {
        if (itemRendererBindingsInitialized) return;
        itemRendererBindingsInitialized = true;
        AutoRegistry.preInitClient();
        bindTeisrs(ItemRendererProviderRegistry.getTileEntityProviders());
        bindTeisrs(ItemRendererProviderRegistry.getItemProviders());
        // IItemRendererProvider is not applicable to Render<T extends Entity>
        bindTeisr(Item.getItemFromBlock(ModBlocks.boat), new RenderBoat.BoatItemRenderer());
        MainRegistry.proxy.registerMissileItems(null);
    }

    private static StateMapperBase fixedModelStateMapper(ModelResourceLocation location) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return location;
            }
        };
    }

    private static void registerNormalStateMapper(Block block) {
        ResourceLocation name = Objects.requireNonNull(block.getRegistryName());
        ModelLoader.setCustomStateMapper(block, fixedModelStateMapper(new ModelResourceLocation(name, "normal")));
    }

    private static void registerIgnoringStateMapper(Block block, IProperty<?>... ignored) {
        ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(ignored).build());
    }

    private static void registerLoggedStateMappers() {
        registerIgnoringStateMapper(ModBlocks.door_bunker, BlockDoor.POWERED);
        registerIgnoringStateMapper(ModBlocks.door_metal, BlockDoor.POWERED);
        registerIgnoringStateMapper(ModBlocks.door_office, BlockDoor.POWERED);
        registerIgnoringStateMapper(ModBlocks.door_red, BlockDoor.POWERED);
        registerIgnoringStateMapper(ModBlocks.acid_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.corium_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.mud_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.schrabidic_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.sulfuric_acid_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.toxic_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.volcanic_lava_block, BlockFluidBase.LEVEL);
        registerIgnoringStateMapper(ModBlocks.seal_controller, BlockSeal.ACTIVATED);

        registerNormalStateMapper(ModBlocks.bm_power_box);
        registerNormalStateMapper(ModBlocks.brick_jungle_trap);
        registerNormalStateMapper(ModBlocks.digamma_matter);
        registerNormalStateMapper(ModBlocks.fire_door);
        registerNormalStateMapper(ModBlocks.floodlight);
        registerNormalStateMapper(ModBlocks.frozen_grass);
        registerNormalStateMapper(ModBlocks.large_vehicle_door);
        registerNormalStateMapper(ModBlocks.machine_diesel);
        registerNormalStateMapper(ModBlocks.ntm_dirt);
        registerNormalStateMapper(ModBlocks.pribris_radiating);
        registerNormalStateMapper(ModBlocks.qe_containment);
        registerNormalStateMapper(ModBlocks.qe_sliding_door);
        registerNormalStateMapper(ModBlocks.red_connector);
        registerNormalStateMapper(ModBlocks.round_airlock_door);
        registerNormalStateMapper(ModBlocks.secure_access_door);
        registerNormalStateMapper(ModBlocks.silo_hatch);
        registerNormalStateMapper(ModBlocks.silo_hatch_drillgon);
        registerNormalStateMapper(ModBlocks.silo_hatch_large);
        registerNormalStateMapper(ModBlocks.sliding_blast_door);
        registerNormalStateMapper(ModBlocks.sliding_blast_door_2);
        registerNormalStateMapper(ModBlocks.sliding_blast_door_keypad);
        registerNormalStateMapper(ModBlocks.sliding_blast_door_legacy);
        registerNormalStateMapper(ModBlocks.sliding_gate_door);
        registerNormalStateMapper(ModBlocks.sliding_seal_door);
        registerNormalStateMapper(ModBlocks.small_hatch);
        registerNormalStateMapper(ModBlocks.spotlight_beam);
        registerNormalStateMapper(ModBlocks.transition_seal);
        registerNormalStateMapper(ModBlocks.turret_sentry);
        registerNormalStateMapper(ModBlocks.turret_sentry_damaged);
        registerNormalStateMapper(ModBlocks.vault_door);
        registerNormalStateMapper(ModBlocks.volcano_core);
        registerNormalStateMapper(ModBlocks.water_door);
    }

    public static void unbindTeisr(Item item) {
        ClaimedModelLocationRegistry.unregisterTeisrBinding(item);
        item.setTileEntityItemStackRenderer(null);
    }

    public static void unbindTeisrs(Item... items) {
        for (Item item : items) {
            unbindTeisr(item);
        }
    }

    public static ModelResourceLocation getSyntheticTeisrModelLocation(Item item) {
        return ClaimedModelLocationRegistry.getSyntheticTeisrModelLocation(item);
    }

    private static OwnedTeisrBinding getOwnedTeisrBinding(Item item) {
        ClaimedModelLocationRegistry.ITeisrBinding binding = ClaimedModelLocationRegistry.getTeisrBinding(item);
        return binding instanceof OwnedTeisrBinding owned ? owned : null;
    }

    private static ModelResourceLocation getBoundTeisrModelLocation(Item item, TEISRBase teisr) {
        OwnedTeisrBinding binding = getOwnedTeisrBinding(item);
        return binding != null ? binding.modelLocation : teisr.createModelBinding(item).getModelLocation();
    }

    private static final class OwnedTeisrBinding implements ClaimedModelLocationRegistry.ITeisrBinding {
        private final Item item;
        private final TEISRBase renderer;
        private final TEISRBase.ModelBinding modelBinding;
        private final ModelResourceLocation modelLocation;
        private final boolean syntheticLocation;
        private final boolean useIdentityTransform;

        private OwnedTeisrBinding(Item item, TEISRBase renderer) {
            this.item = item;
            this.renderer = renderer;
            this.modelBinding = renderer.createModelBinding(item);
            this.modelLocation = modelBinding.getModelLocation();
            this.syntheticLocation = modelLocation.getPath().startsWith("teisr/");
            this.useIdentityTransform = renderer.useIdentityTransform(item);
        }

        @Override
        public Item getItem() {
            return item;
        }

        @Override
        public ModelResourceLocation getModelLocation() {
            return modelLocation;
        }

        @Override
        public boolean isSyntheticLocation() {
            return syntheticLocation;
        }

        @Override
        public boolean ownsModelLocation(ModelResourceLocation location) {
            return modelLocation.equals(location);
        }

        @Override
        public IModel loadModel(ModelResourceLocation location) {
            return renderer.loadModel(item, modelLocation);
        }
    }

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
        //mlbv: please, no.
        ItemDepletedFuel.registerColorHandlers(evt);
        ItemBedrockOreNew.registerColorHandlers(evt);
        ItemFFFluidDuct.registerColorHandlers(evt);
        FluidDuctStandard.registerItemColorHandler(evt);
        FluidDuctBox.registerItemColorHandler(evt);
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
        StaticTesrBakedModels.registerSprites(map);
        StaticDecoBakedModels.registerSprites(map);

        //Debug stuff
        debugPower = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/debug_power"));
        debugFluid = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/debug_fluid"));
        contrail = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/contrail"));
        particle_base = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/particle_base"));
        fog = map.registerSprite(new ResourceLocation(Tags.MODID, "particle/fog"));

        RegistrationUtils.registerInFolder(map,"textures/blocks/forgefluid");


        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_identifier_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_barrel_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_tank_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/fluid_tank_lead_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/chemical_dye_overlay"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/crayon_overlay"));
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post evt) {
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

    public static void wrapAllTeisrModels(IRegistry<ModelResourceLocation, IBakedModel> reg) {
        for (ClaimedModelLocationRegistry.ITeisrBinding binding : ClaimedModelLocationRegistry.getTeisrBindings()) {
            if (binding instanceof OwnedTeisrBinding owned) {
                wrapTeisrBinding(owned, reg);
            }
        }
    }

    private static void wrapTeisrBinding(OwnedTeisrBinding owned, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        TEISRBase teisr = owned.renderer;
        TEISRBase.ModelBinding binding = owned.modelBinding;
        ModelResourceLocation targetLocation = owned.modelLocation;
        IBakedModel model = unwrapWrappedModel(reg.getObject(targetLocation));
        if (model == null) {
            try {
                model = owned.loadModel(targetLocation)
                        .bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                reg.putObject(targetLocation, model);
            } catch (Exception e) {
                MainRegistry.logger.warn("Skipping TEISR wrap for {} because {} could not be baked", owned.item.getRegistryName(), targetLocation, e);
                return;
            }
        }

        teisr.itemModel = model;
        if (teisr.useFMMPerspective(owned.item)) {
            reg.putObject(targetLocation, new FancyMissingModelPerspective(teisr, model));
            return;
        }
        if (owned.useIdentityTransform) {
            reg.putObject(targetLocation, new BakedModelNoFPV(teisr, model));
            return;
        }
        reg.putObject(targetLocation, new WrappedTEISRModel(teisr, model, null, binding, false));
    }

    private static void registerOwnedTeisrModelLocations() {
        for (ClaimedModelLocationRegistry.ITeisrBinding owned : ClaimedModelLocationRegistry.getTeisrBindings()) {
            ModelResourceLocation location = owned.getModelLocation();
            ModelBakery.registerItemVariants(owned.getItem(), location);
            ModelLoader.setCustomMeshDefinition(owned.getItem(), _ -> location);
        }
    }

    private static IBakedModel unwrapWrappedModel(IBakedModel model) {
        if (model instanceof WrappedTEISRModel wrapped) {
            return wrapped.getBaseModel();
        }
        return model;
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
        initializeItemRendererBindings();
        registerLoggedStateMappers();
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
        registerOwnedTeisrModelLocations();

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

        if (item instanceof ItemBlock itemBlock) {
            Block block = itemBlock.getBlock();
            if (block instanceof IDynamicModels && IDynamicModels.INSTANCES.contains(block)) {
                return;
            }
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

        if (item.getTileEntityItemStackRenderer() instanceof TEISRBase teisr) {
            ModelLoader.setCustomModelResourceLocation(item, meta, getBoundTeisrModelLocation(item, teisr));
            return;
        }

        if (item == ModItems.siren_track) {
            for (ItemCassette.TrackType track : ItemCassette.TrackType.VALUES.values()) {
                ModelLoader.setCustomModelResourceLocation(item, track.getId(), new ModelResourceLocation(item.getRegistryName(), "inventory"));
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
            ModelResourceLocation hotModel = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/" + item.getRegistryName().getPath()), "inventory");
            for (int i = 0; i < 16; i++)
                ModelLoader.setCustomModelResourceLocation(item, i, hotModel);
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
        } else if (item == ModItems.conveyor_wand) {
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


        IRegistry<ModelResourceLocation, IBakedModel> registry = evt.getModelRegistry();
        for (SpecialContainerFillLists.EnumCanister e : SpecialContainerFillLists.EnumCanister.VALUES) {
            IBakedModel o = registry.getObject(e.getResourceLocation());
            e.putRenderModel(o);
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
            IBakedModel o = registry.getObject(e.getResourceLocation());
            e.putRenderModel(o);
        }

        ResourceManager.init();
        ItemRedstoneSwordRender.INSTANCE.itemModel = registry.getObject(RedstoneSword.rsModel);
        registry.putObject(RedstoneSword.rsModel, new ItemRenderRedstoneSword());
        ItemRenderGunAnim.INSTANCE.b92ItemModel = registry.getObject(GunB92.b92Model);
        registry.putObject(GunB92.b92Model, new B92BakedModel());
        wrapAllTeisrModels(registry);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onModelBakeLast(ModelBakeEvent evt) {
        StaticTesrBakedModels.bakeModels(evt.getModelRegistry());
        StaticDecoBakedModels.bakeModels(evt.getModelRegistry());
    }

}
