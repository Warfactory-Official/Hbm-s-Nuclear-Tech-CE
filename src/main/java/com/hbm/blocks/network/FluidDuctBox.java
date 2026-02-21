package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.interfaces.IBlockSpecialPlacementAABB;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IDynamicModels;
import com.hbm.items.block.ItemBlockSpecialAABB;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.DuctBakedModel;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.ColorUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctBox extends FluidDuctBase implements IDynamicModels, ILookOverlay, ICustomBlockItem, IBlockSpecialPlacementAABB {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 14);
    public static final IUnlistedProperty<Boolean> CONN_NORTH = new ConnectionProperty("north");
    public static final IUnlistedProperty<Boolean> CONN_SOUTH = new ConnectionProperty("south");
    public static final IUnlistedProperty<Boolean> CONN_WEST = new ConnectionProperty("west");
    public static final IUnlistedProperty<Boolean> CONN_EAST = new ConnectionProperty("east");
    public static final IUnlistedProperty<Boolean> CONN_UP = new ConnectionProperty("up");
    public static final IUnlistedProperty<Boolean> CONN_DOWN = new ConnectionProperty("down");

    private static final String[] materials = new String[] {"silver", "copper", "white"};

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconStraight;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconEnd;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveTL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveTR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveBL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveBR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[][] iconJunction;

    public FluidDuctBox(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setSoundType(ModSoundTypes.pipe);
        this.useNeighborBrightness = true;

        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            iconStraight = new TextureAtlasSprite[3];
            iconEnd = new TextureAtlasSprite[3];
            iconCurveTL = new TextureAtlasSprite[3];
            iconCurveTR = new TextureAtlasSprite[3];
            iconCurveBL = new TextureAtlasSprite[3];
            iconCurveBR = new TextureAtlasSprite[3];
            iconJunction = new TextureAtlasSprite[3][5];
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPipeBaseNT();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{META}, new IUnlistedProperty[]{CONN_NORTH, CONN_SOUTH, CONN_WEST, CONN_EAST, CONN_UP, CONN_DOWN});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) state;
        TileEntity te = world.getTileEntity(pos);
        ext = ext.withProperty(CONN_NORTH, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH, te));
        ext = ext.withProperty(CONN_SOUTH, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH, te));
        ext = ext.withProperty(CONN_WEST, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST, te));
        ext = ext.withProperty(CONN_EAST, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST, te));
        ext = ext.withProperty(CONN_UP, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP, te));
        ext = ext.withProperty(CONN_DOWN, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN, te));
        return ext;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta % 15);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 0; i < 15; ++i) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int meta = stack.getMetadata();
        worldIn.setBlockState(pos, this.getStateFromMeta(meta), 3);
    }

    protected boolean canConnectTo(IBlockAccess world, int x, int y, int z, EnumFacing dir, TileEntity tile) {
        if (tile instanceof TileEntityPipeBaseNT pipeBaseNT) {
            FluidType type = pipeBaseNT.getType();
            return canConnectTo(world, x, y, z, dir, type);
        }
        return false;
    }

    protected boolean canConnectTo(IBlockAccess world, int x, int y, int z, EnumFacing dir, FluidType type) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos offset = pos.offset(dir);
        return Library.canConnectFluid(world, offset, ForgeDirection.getOrientation(dir.getOpposite()), type);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) { // Adapt if needed for your ILookOverlay system
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityPipeBaseNT duct)) return;

        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        boolean nX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST, te);
        boolean pX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST, te);
        boolean nY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN, te);
        boolean pY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP, te);
        boolean nZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH, te);
        boolean pZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH, te);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = (pX ? 1 : 0) + (nX ? 1 : 0) + (pY ? 1 : 0) + (nY ? 1 : 0) + (pZ ? 1 : 0) + (nZ ? 1 : 0);

        int meta = getMetaFromState(state);
        double lower = 0.125D;
        double upper = 0.875D;
        double jLower = 0.0625D;
        double jUpper = 0.9375D;
        for (int i = 2; i < 13; i += 3) {
            if (meta > i) {
                lower += 0.0625D;
                upper -= 0.0625D;
                jLower += 0.0625D;
                jUpper -= 0.0625D;
            }
        }

        List<AxisAlignedBB> bbs = new ArrayList<>();

        if (mask == 0) {
            bbs.add(new AxisAlignedBB(pos.getX() + jLower, pos.getY() + jLower, pos.getZ() + jLower, pos.getX() + jUpper, pos.getY() + jUpper, pos.getZ() + jUpper));
        } else if (mask == 0b100000 || mask == 0b010000 || mask == 0b110000) {
            bbs.add(new AxisAlignedBB(pos.getX() + 0.0D, pos.getY() + lower, pos.getZ() + lower, pos.getX() + 1.0D, pos.getY() + upper, pos.getZ() + upper));
        } else if (mask == 0b001000 || mask == 0b000100 || mask == 0b001100) {
            bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + 0.0D, pos.getZ() + lower, pos.getX() + upper, pos.getY() + 1.0D, pos.getZ() + upper));
        } else if (mask == 0b000010 || mask == 0b000001 || mask == 0b000011) {
            bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + lower, pos.getZ() + 0.0D, pos.getX() + upper, pos.getY() + upper, pos.getZ() + 1.0D));
        } else {
            if (count != 2) {
                bbs.add(new AxisAlignedBB(pos.getX() + jLower, pos.getY() + jLower, pos.getZ() + jLower, pos.getX() + jUpper, pos.getY() + jUpper, pos.getZ() + jUpper));
            } else {
                bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + lower, pos.getZ() + lower, pos.getX() + upper, pos.getY() + upper, pos.getZ() + upper));
            }
            if (pX) bbs.add(new AxisAlignedBB(pos.getX() + upper, pos.getY() + lower, pos.getZ() + lower, pos.getX() + 1.0D, pos.getY() + upper, pos.getZ() + upper));
            if (nX) bbs.add(new AxisAlignedBB(pos.getX() + 0.0D, pos.getY() + lower, pos.getZ() + lower, pos.getX() + lower, pos.getY() + upper, pos.getZ() + upper));
            if (pY) bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + upper, pos.getZ() + lower, pos.getX() + upper, pos.getY() + 1.0D, pos.getZ() + upper));
            if (nY) bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + 0.0D, pos.getZ() + lower, pos.getX() + upper, pos.getY() + lower, pos.getZ() + upper));
            if (pZ) bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + lower, pos.getZ() + upper, pos.getX() + upper, pos.getY() + upper, pos.getZ() + 1.0D));
            if (nZ) bbs.add(new AxisAlignedBB(pos.getX() + lower, pos.getY() + lower, pos.getZ() + 0.0D, pos.getX() + upper, pos.getY() + upper, pos.getZ() + lower));
        }

        for (AxisAlignedBB bb : bbs) {
            if (entityBox.intersects(bb)) {
                collidingBoxes.add(bb);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);

        boolean nX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST, te);
        boolean pX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST, te);
        boolean nY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN, te);
        boolean pY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP, te);
        boolean nZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH, te);
        boolean pZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH, te);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = (pX ? 1 : 0) + (nX ? 1 : 0) + (pY ? 1 : 0) + (nY ? 1 : 0) + (pZ ? 1 : 0) + (nZ ? 1 : 0);

        int meta = getMetaFromState(state);
        float lower = 0.125F;
        float upper = 0.875F;
        float jLower = 0.0625F;
        float jUpper = 0.9375F;
        for (int i = 2; i < 13; i += 3) {
            if (meta > i) {
                lower += 0.0625F;
                upper -= 0.0625F;
                jLower += 0.0625F;
                jUpper -= 0.0625F;
            }
        }

        if (mask == 0) {
            return new AxisAlignedBB(jLower, jLower, jLower, jUpper, jUpper, jUpper);
        } else if (mask == 0b100000 || mask == 0b010000 || mask == 0b110000) {
            return new AxisAlignedBB(0F, lower, lower, 1F, upper, upper);
        } else if (mask == 0b001000 || mask == 0b000100 || mask == 0b001100) {
            return new AxisAlignedBB(lower, 0F, lower, upper, 1F, upper);
        } else if (mask == 0b000010 || mask == 0b000001 || mask == 0b000011) {
            return new AxisAlignedBB(lower, lower, 0F, upper, upper, 1F);
        } else {
            if (count != 2) {
                return new AxisAlignedBB(nX ? 0F : jLower, nY ? 0F : jLower, nZ ? 0F : jLower, pX ? 1F : jUpper, pY ? 1F : jUpper, pZ ? 1F : jUpper);
            } else {
                return new AxisAlignedBB(nX ? 0F : lower, nY ? 0F : lower, nZ ? 0F : lower, pX ? 1F : upper, pY ? 1F : upper, pZ ? 1F : upper);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        for (int meta = 0; meta < 15; meta++) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), meta, new ModelResourceLocation(this.getRegistryName(), "meta=" + meta));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        for (int i = 0; i < 3; i++) {
            String mat = materials[i];
            iconStraight[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_straight"));
            iconEnd[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_end"));
            iconCurveTL[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_curve_tl"));
            iconCurveTR[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_curve_tr"));
            iconCurveBL[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_curve_bl"));
            iconCurveBR[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_curve_br"));
            for (int j = 0; j < 5; j++) {
                iconJunction[i][j] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_" + mat + "_junction_" + j));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        for (int meta = 0; meta < 15; meta++) {
            IBakedModel bakedModel = new DuctBakedModel(meta, false);
            ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
            event.getModelRegistry().putObject(modelLocation, bakedModel);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "meta=" + state.getValue(META));
            }
        };
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxForPlacement(World source, BlockPos pos, ItemStack stack) {
        FluidType te = Fluids.NONE;
        boolean nX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST, te);
        boolean pX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST, te);
        boolean nY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN, te);
        boolean pY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP, te);
        boolean nZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH, te);
        boolean pZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH, te);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);
        int count = (pX ? 1 : 0) + (nX ? 1 : 0) + (pY ? 1 : 0) + (nY ? 1 : 0) + (pZ ? 1 : 0) + (nZ ? 1 : 0);

        int meta = stack.getMetadata();
        float lower = 0.125F;
        float upper = 0.875F;
        float jLower = 0.0625F;
        float jUpper = 0.9375F;
        for (int i = 2; i < 13; i += 3) {
            if (meta > i) {
                lower += 0.0625F;
                upper -= 0.0625F;
                jLower += 0.0625F;
                jUpper -= 0.0625F;
            }
        }

        switch (mask) {
            case 0:
                return new AxisAlignedBB(jLower, jLower, jLower, jUpper, jUpper, jUpper);
            case 0b100000:
            case 0b010000:
            case 0b110000:
                return new AxisAlignedBB(0F, lower, lower, 1F, upper, upper);
            case 0b001000:
            case 0b000100:
            case 0b001100:
                return new AxisAlignedBB(lower, 0F, lower, upper, 1F, upper);
            case 0b000010:
            case 0b000001:
            case 0b000011:
                return new AxisAlignedBB(lower, lower, 0F, upper, upper, 1F);
            default:
                if (count != 2) {
                    return new AxisAlignedBB(nX ? 0F : jLower, nY ? 0F : jLower, nZ ? 0F : jLower, pX ? 1F : jUpper, pY ? 1F : jUpper, pZ ? 1F : jUpper);
                } else {
                    return new AxisAlignedBB(nX ? 0F : lower, nY ? 0F : lower, nZ ? 0F : lower, pX ? 1F : upper, pY ? 1F : upper, pZ ? 1F : upper);
                }
        }
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlockSpecialAABB<>(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    public static class ConnectionProperty implements IUnlistedProperty<Boolean> {
        private final String name;

        public ConnectionProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(Boolean value) {
            return true;
        }

        @Override
        public Class<Boolean> getType() {
            return Boolean.class;
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandler(ColorHandlerEvent.Block evt) {
        IBlockColor ductColorHandler = (state, worldIn, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return 0xFFFFFF;
            }

            if (worldIn == null || pos == null) {
                return 0xFFFFFF;
            }

            TileEntity te = worldIn.getTileEntity(pos);
            if (!(te instanceof TileEntityPipeBaseNT pipe)) {
                return 0xFFFFFF;
            }

            FluidType type = pipe.getType();

            int meta = state.getBlock().getMetaFromState(state);
            int color = type.getColor();

            if (meta % 3 == 2) {
                color = ColorUtil.lightenColor(color, 0.25D);
            } else return 0xFFFFFF;

            return color;
        };

        BlockColors blockColors = evt.getBlockColors();
        blockColors.registerBlockColorHandler(ductColorHandler, ModBlocks.fluid_duct_box);
    }
}
