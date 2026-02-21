package com.hbm.blocks.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IDynamicModels;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

public class FluidDuctGauge extends FluidDuctBase implements ILookOverlay, ITooltipProvider, IDynamicModels, INBTBlockTransformable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", Arrays.asList(EnumFacing.VALUES));

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite baseSprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlaySprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite gaugeSprite;

    public FluidDuctGauge(String name) {
        super(Material.IRON);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setSoundType(ModSoundTypes.pipe);
        useNeighborBrightness = true;
        IDynamicModels.INSTANCES.add(this);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new TileEntityPipeGauge();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPipeGauge();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityPipeGauge duct)) {
            return;
        }

        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        text.add(String.format(Locale.US, "%,d", duct.deltaTick) + " mB/t");
        text.add(String.format(Locale.US, "%,d", duct.deltaLastSecond) + " mB/s");
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xFFFF00, 0x404000, text);
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        baseSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/deco_steel"));
        overlaySprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/fluid_duct_paintable_overlay"));
        gaugeSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe_gauge"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        TextureAtlasSprite base = baseSprite != null ? baseSprite : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        TextureAtlasSprite overlay = overlaySprite != null ? overlaySprite : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        TextureAtlasSprite gauge = gaugeSprite != null ? gaugeSprite : Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

        FluidDuctGaugeModel model = new FluidDuctGaugeModel(base, overlay, gauge);
        ModelResourceLocation inventory = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
        ModelResourceLocation normal = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "normal");

        event.getModelRegistry().putObject(inventory, model);
        event.getModelRegistry().putObject(normal, model);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
                new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
        ModelLoader.setCustomStateMapper(this, new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "normal");
            }
        });
    }

    @Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
    @AutoRegister
    public static class TileEntityPipeGauge extends TileEntityPipeBaseNT implements SimpleComponent, CompatHandler.OCComponent {

        private long deltaTick = 0;
        private long deltaSecond = 0;
        private long deltaLastSecond = 0;

        @Override
        public void update() {
            super.update();

            if (!world.isRemote) {
                if (node != null && node.net != null && getType() != Fluids.NONE) {
                    deltaTick = node.net.fluidTracker;
                    if (world.getTotalWorldTime() % 20L == 0) {
                        deltaLastSecond = deltaSecond;
                        deltaSecond = 0;
                    }
                    deltaSecond += deltaTick;
                }
                networkPackNT(25);
            }
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeLong(deltaTick);
            buf.writeLong(deltaLastSecond);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            deltaTick = Math.max(buf.readLong(), 0);
            deltaLastSecond = Math.max(buf.readLong(), 0);
        }

        @Optional.Method(modid = "opencomputers")
        public String getComponentName() {
            return "ntm_fluid_gauge";
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getTransfer(Context context, Arguments args) {
            return new Object[]{deltaTick, deltaLastSecond};
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getFluid(Context context, Arguments args) {
            return new Object[]{getType().getName()};
        }

        @Callback(direct = true)
        @Optional.Method(modid = "opencomputers")
        public Object[] getInfo(Context context, Arguments args) {
            return new Object[]{deltaTick, deltaLastSecond, getType().getName(), pos.getX(), pos.getY(), pos.getZ()};
        }

        /*@Override
        public String[] getFunctionInfo() {
            return new String[]{
                    PREFIX_VALUE + "deltatick",
                    PREFIX_VALUE + "deltasecond"
            };
        }

        @Override
        public String provideRORValue(String name) {
            if ((PREFIX_VALUE + "deltatick").equals(name)) {
                return Long.toString(deltaTick);
            }
            if ((PREFIX_VALUE + "deltasecond").equals(name)) {
                return Long.toString(deltaLastSecond);
            }
            return null;
        }*/
    }

    @SideOnly(Side.CLIENT)
    public static class FluidDuctGaugeModel implements IBakedModel {

        private static final FaceBakery FACE_BAKERY = new FaceBakery();

        private final TextureAtlasSprite particle;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> baseFaces;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> overlayFaces;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> gaugeFaces;
        private final ImmutableList<BakedQuad> baseGeneral;
        private final ImmutableList<BakedQuad> overlayGeneral;

        public FluidDuctGaugeModel(TextureAtlasSprite base, TextureAtlasSprite overlay, TextureAtlasSprite gauge) {
            this.particle = base;
            this.baseFaces = buildFaceMap(base, -1, false);
            this.overlayFaces = buildFaceMap(overlay, -1, true);
            this.gaugeFaces = buildFaceMap(gauge, -1, true);
            this.baseGeneral = flatten(baseFaces);
            this.overlayGeneral = flatten(overlayFaces);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            if (layer != null && layer != BlockRenderLayer.CUTOUT_MIPPED) {
                return Collections.emptyList();
            }

            EnumFacing facing = EnumFacing.NORTH;
            if (state != null && state.getPropertyKeys().contains(FACING)) {
                facing = state.getValue(FACING);
            }

            List<BakedQuad> quads = new ArrayList<>();
            if (side == null) {
                quads.addAll(baseGeneral);
                quads.addAll(overlayGeneral);
                quads.addAll(gaugeFaces.get(facing));
            } else {
                quads.addAll(baseFaces.get(side));
                quads.addAll(overlayFaces.get(side));
                if (side == facing) {
                    quads.addAll(gaugeFaces.get(side));
                }
            }
            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public @NotNull TextureAtlasSprite getParticleTexture() {
            return particle;
        }

        @Override
        public @NotNull ItemCameraTransforms getItemCameraTransforms() {
            return BakedModelTransforms.standardBlock();
        }

        @Override
        public @NotNull ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        private static ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> buildFaceMap(TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            ImmutableMap.Builder<EnumFacing, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                builder.put(face, ImmutableList.of(createQuad(face, sprite, tintIndex, offset)));
            }
            return builder.build();
        }

        private static ImmutableList<BakedQuad> flatten(Map<EnumFacing, ImmutableList<BakedQuad>> map) {
            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                builder.addAll(map.get(face));
            }
            return builder.build();
        }

        private static BakedQuad createQuad(EnumFacing face, TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            float eps = 0.001F;
            Vector3f from = new Vector3f(0F, 0F, 0F);
            Vector3f to = new Vector3f(16F, 16F, 16F);

            if (offset) {
                switch (face) {
                    case DOWN -> from.setY(-eps);
                    case UP -> to.setY(16F + eps);
                    case NORTH -> from.setZ(-eps);
                    case SOUTH -> to.setZ(16F + eps);
                    case WEST -> from.setX(-eps);
                    case EAST -> to.setX(16F + eps);
                }
            }

            BlockFaceUV uv = new BlockFaceUV(new float[]{0F, 0F, 16F, 16F}, 0);
            BlockPartFace partFace = new BlockPartFace(null, tintIndex, "", uv);
            return FACE_BAKERY.makeBakedQuad(from, to, partFace, sprite, face, ModelRotation.X0_Y0, null, false, true);
        }
    }
}
