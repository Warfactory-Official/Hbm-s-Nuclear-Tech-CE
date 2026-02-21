package com.hbm.blocks.network.energy;

import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBlockSpecialPlacementAABB;
import com.hbm.items.IDynamicModels;
import com.hbm.items.block.ItemBlockSpecialAABB;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.render.model.CableBoxBakedModel;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PowerCableBox extends Block implements ITileEntityProvider, ICustomBlockItem, IDynamicModels, IBlockSpecialPlacementAABB {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 4);
    public static final IUnlistedProperty<Boolean> CONN_NORTH = new PowerCableBox.ConnectionProperty("north");
    public static final IUnlistedProperty<Boolean> CONN_SOUTH = new PowerCableBox.ConnectionProperty("south");
    public static final IUnlistedProperty<Boolean> CONN_WEST = new PowerCableBox.ConnectionProperty("west");
    public static final IUnlistedProperty<Boolean> CONN_EAST = new PowerCableBox.ConnectionProperty("east");
    public static final IUnlistedProperty<Boolean> CONN_UP = new PowerCableBox.ConnectionProperty("up");
    public static final IUnlistedProperty<Boolean> CONN_DOWN = new PowerCableBox.ConnectionProperty("down");

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconStraight;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconEnd;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconCurveTL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconCurveTR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconCurveBL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconCurveBR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite iconJunction;

    static {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            iconEnd = new TextureAtlasSprite[5];
        }
    }

    public PowerCableBox(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.useNeighborBrightness = true;

        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCableBaseNT();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{META}, new IUnlistedProperty[]{CONN_NORTH, CONN_SOUTH, CONN_WEST, CONN_EAST, CONN_UP, CONN_DOWN});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) state;
        ext = ext.withProperty(CONN_NORTH, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH));
        ext = ext.withProperty(CONN_SOUTH, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH));
        ext = ext.withProperty(CONN_WEST, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST));
        ext = ext.withProperty(CONN_EAST, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST));
        ext = ext.withProperty(CONN_UP, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP));
        ext = ext.withProperty(CONN_DOWN, canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN));
        return ext;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta % 5);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 0; i < 5; ++i) {
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
        int meta = stack.getMetadata() % 5;
        worldIn.setBlockState(pos, this.getStateFromMeta(meta), 3);
    }

    protected boolean canConnectTo(IBlockAccess world, int x, int y, int z, EnumFacing dir) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos offset = pos.offset(dir);
        return Library.canConnect(world, offset, ForgeDirection.getOrientation(dir.getOpposite()));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityCableBaseNT();
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item itemBlock = Item.getItemFromBlock(this);
        for (int meta = 0; meta < 5; meta++) {
            ModelResourceLocation modelLocation = new ModelResourceLocation(this.getRegistryName(), "meta=" + meta);
            ModelLoader.setCustomModelResourceLocation(itemBlock, meta, modelLocation);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        iconStraight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_straight"));
        iconCurveTL = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_curve_tl"));
        iconCurveTR = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_curve_tr"));
        iconCurveBL = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_curve_bl"));
        iconCurveBR = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_curve_br"));
        iconJunction = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_junction"));

        for (int i = 0; i < iconEnd.length; i++) {
            iconEnd[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_cable_end_" + i));
        }
    }

    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        for (int meta = 0; meta < 5; meta++) {
            IBakedModel bakedModel = new CableBoxBakedModel(meta);
            ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
            event.getModelRegistry().putObject(modelLocation, bakedModel);
        }
    }

    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "meta=" + state.getValue(META));
            }
        };
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        boolean nX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST);
        boolean pX = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST);
        boolean nY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN);
        boolean pY = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP);
        boolean nZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH);
        boolean pZ = canConnectTo(world, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        int meta = getMetaFromState(state);
        double lower = 0.125D;
        double upper = 0.875D;

        for (int i = 0; i < 5; i++) {
            if (meta > i) {
                lower += 0.0625D;
                upper -= 0.0625D;
            }
        }

        if (lower > 0.5) lower = 0.5;
        if (upper < 0.5) upper = 0.5;

        double jLower = lower;
        double jUpper = upper;

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
            // Center
            bbs.add(new AxisAlignedBB(pos.getX() + jLower, pos.getY() + jLower, pos.getZ() + jLower, pos.getX() + jUpper, pos.getY() + jUpper, pos.getZ() + jUpper));

            // Arms
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
        boolean nX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST);
        boolean pX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST);
        boolean nY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN);
        boolean pY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP);
        boolean nZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH);
        boolean pZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        int meta = getMetaFromState(state);
        float lower = 0.125F;
        float upper = 0.875F;
        for (int i = 0; i < 5; i++) {
            if (meta > i) {
                lower += 0.0625F;
                upper -= 0.0625F;
            }
        }

        float jLower = lower;
        float jUpper = upper;

        return switch (mask) {
            case 0 -> new AxisAlignedBB(jLower, jLower, jLower, jUpper, jUpper, jUpper);
            case 0b100000, 0b010000, 0b110000 -> new AxisAlignedBB(0F, lower, lower, 1F, upper, upper);
            case 0b001000, 0b000100, 0b001100 -> new AxisAlignedBB(lower, 0F, lower, upper, 1F, upper);
            case 0b000010, 0b000001, 0b000011 -> new AxisAlignedBB(lower, lower, 0F, upper, upper, 1F);
            default -> new AxisAlignedBB(nX ? 0F : lower, nY ? 0F : lower, nZ ? 0F : lower, pX ? 1F : upper, pY ? 1F : upper, pZ ? 1F : upper);
        };
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    private boolean canConnectToNeighbor(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
        if (Library.canConnect(world, pos, dir)) {
            return true;
        }

        TileEntity neighbor = world.getTileEntity(pos);
        if (neighbor != null && !neighbor.isInvalid()) {
            EnumFacing facing = dir.getOpposite().toEnumFacing();
            if (neighbor.hasCapability(CapabilityEnergy.ENERGY, facing)) {
                IEnergyStorage storage = neighbor.getCapability(CapabilityEnergy.ENERGY, facing);
                return storage != null && (storage.canReceive() || storage.canExtract());
            }
        }

        return false;
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new ItemBlockSpecialAABB<>(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxForPlacement(World source, BlockPos pos, ItemStack stack) {
        boolean nX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.WEST);
        boolean pX = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.EAST);
        boolean nY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.DOWN);
        boolean pY = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.UP);
        boolean nZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.NORTH);
        boolean pZ = canConnectTo(source, pos.getX(), pos.getY(), pos.getZ(), EnumFacing.SOUTH);

        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        int meta = stack.getMetadata();
        float lower = 0.125F;
        float upper = 0.875F;
        for (int i = 0; i < 5; i++) {
            if (meta > i) {
                lower += 0.0625F;
                upper -= 0.0625F;
            }
        }

        float jLower = lower;
        float jUpper = upper;

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
                return new AxisAlignedBB(nX ? 0F : lower, nY ? 0F : lower, nZ ? 0F : lower, pX ? 1F : upper, pY ? 1F : upper, pZ ? 1F : upper);
        }
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

    private boolean canConnect(IBlockAccess world, BlockPos pos, EnumFacing dir) {
        return switch (dir) {
            case EAST -> canConnectToNeighbor(world, pos, Library.POS_X);
            case WEST -> canConnectToNeighbor(world, pos, Library.NEG_X);
            case UP -> canConnectToNeighbor(world, pos, Library.POS_Y);
            case DOWN -> canConnectToNeighbor(world, pos, Library.NEG_Y);
            case SOUTH -> canConnectToNeighbor(world, pos, Library.POS_Z);
            case NORTH -> canConnectToNeighbor(world, pos, Library.NEG_Z);
        };
    }
}
