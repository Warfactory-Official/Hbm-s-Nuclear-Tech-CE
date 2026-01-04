package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.PneumoTubeBakedModel;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.util.Compat;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PneumoTube extends BlockContainer implements IToolable, ITooltipProvider, IDynamicModels {
    public static final IUnlistedProperty<Boolean> CONN_NORTH = new ConnectionProperty("north");
    public static final IUnlistedProperty<Boolean> CONN_SOUTH = new ConnectionProperty("south");
    public static final IUnlistedProperty<Boolean> CONN_WEST = new ConnectionProperty("west");
    public static final IUnlistedProperty<Boolean> CONN_EAST = new ConnectionProperty("east");
    public static final IUnlistedProperty<Boolean> CONN_UP = new ConnectionProperty("up");
    public static final IUnlistedProperty<Boolean> CONN_DOWN = new ConnectionProperty("down");
    public static final IUnlistedProperty<ForgeDirection> OUT_DIR = new DirectionProperty("out_dir");
    public static final IUnlistedProperty<ForgeDirection> IN_DIR = new DirectionProperty("in_dir");
    public static final IUnlistedProperty<ForgeDirection> CONNECTOR_DIR = new DirectionProperty("connector_dir");

    @SideOnly(Side.CLIENT) public static TextureAtlasSprite iconBase, iconStraight, iconIn, iconOut, iconConnector;

    public PneumoTube(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(MainRegistry.controlTab);
        this.useNeighborBrightness = true;
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPneumoTube();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.CENTER;
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
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{CONN_NORTH, CONN_SOUTH, CONN_WEST, CONN_EAST, CONN_UP, CONN_DOWN, OUT_DIR, IN_DIR, CONNECTOR_DIR});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) state;
        TileEntity te = world.getTileEntity(pos);
        ext = ext.withProperty(CONN_NORTH, canConnectTo(world, pos, ForgeDirection.NORTH));
        ext = ext.withProperty(CONN_SOUTH, canConnectTo(world, pos, ForgeDirection.SOUTH));
        ext = ext.withProperty(CONN_WEST, canConnectTo(world, pos, ForgeDirection.WEST));
        ext = ext.withProperty(CONN_EAST, canConnectTo(world, pos, ForgeDirection.EAST));
        ext = ext.withProperty(CONN_UP, canConnectTo(world, pos, ForgeDirection.UP));
        ext = ext.withProperty(CONN_DOWN, canConnectTo(world, pos, ForgeDirection.DOWN));
        ext = ext.withProperty(OUT_DIR, te instanceof TileEntityPneumoTube tube ? tube.ejectionDir : ForgeDirection.UNKNOWN);
        ext = ext.withProperty(IN_DIR, te instanceof TileEntityPneumoTube tube ? tube.insertionDir : ForgeDirection.UNKNOWN);
        ForgeDirection connectorDir = ForgeDirection.UNKNOWN;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (canConnectToAir(world, pos, dir)) { connectorDir = dir; break; }
        }
        ext = ext.withProperty(CONNECTOR_DIR, connectorDir);
        return ext;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (ToolType.getType(player.getHeldItemMainhand()) == ToolType.SCREWDRIVER) return false;
        if (!player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityPneumoTube tube) {
                if(tube.isCompressor()) {
                    if (!world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                    return true;
                } else if(tube.isEndpoint()) {
                    if (!world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        return onScrew(world, player, new BlockPos(x, y, z), side, fX, fY, fZ, hand, tool);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, BlockPos pos, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if(tool != ToolType.SCREWDRIVER) return false;
        if(world.isRemote) return true;

        TileEntityPneumoTube tube = (TileEntityPneumoTube) world.getTileEntity(pos);

        ForgeDirection rot = player.isSneaking() ? tube.ejectionDir : tube.insertionDir;
        ForgeDirection oth = player.isSneaking() ? tube.insertionDir : tube.ejectionDir;

        for(int i = 0; i < 7; i++) {
            rot = ForgeDirection.getOrientation((rot.ordinal() + 1) % 7);
            if(rot == ForgeDirection.UNKNOWN) break; //unknown is always valid, simply disables this part
            if(rot == oth) continue; //skip if both positions collide
            TileEntity tile = Compat.getTileStandard(world, pos.getX() + rot.offsetX, pos.getY() + rot.offsetY, pos.getZ() + rot.offsetZ);
            if(tile == null || tile instanceof TileEntityPneumoTube) continue;
            if(hasItemHandler(tile, rot)) break;
            if(tile instanceof IInventory) break; //fallback for legacy inventories
        }

        if(player.isSneaking()) tube.ejectionDir = rot; else tube.insertionDir = rot;

        tube.markDirty();
        if(world instanceof WorldServer) ((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(pos);
        world.markBlockRangeForRenderUpdate(pos, pos);

        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        double lower = 0.3125D;
        double upper = 0.6875D;

        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(lower, lower, lower, upper, upper, upper));

        if(canConnectTo(world, pos, Library.POS_X) || canConnectToAir(world, pos, Library.POS_X))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(upper, lower, lower, 1.0D, upper, upper));

        if(canConnectTo(world, pos, Library.NEG_X) || canConnectToAir(world, pos, Library.NEG_X))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, lower, lower, lower, upper, upper));

        if(canConnectTo(world, pos, Library.POS_Y) || canConnectToAir(world, pos, Library.POS_Y))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(lower, upper, lower, upper, 1.0D, upper));

        if(canConnectTo(world, pos, Library.NEG_Y) || canConnectToAir(world, pos, Library.NEG_Y))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(lower, 0.0D, lower, upper, lower, upper));

        if(canConnectTo(world, pos, Library.POS_Z) || canConnectToAir(world, pos, Library.POS_Z))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(lower, lower, upper, upper, upper, 1.0D));

        if(canConnectTo(world, pos, Library.NEG_Z) || canConnectToAir(world, pos, Library.NEG_Z))
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(lower, lower, 0.0D, upper, upper, lower));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        boolean nX = canConnectTo(source, pos, Library.NEG_X);
        boolean pX = canConnectTo(source, pos, Library.POS_X);
        boolean nY = canConnectTo(source, pos, Library.NEG_Y);
        boolean pY = canConnectTo(source, pos, Library.POS_Y);
        boolean nZ = canConnectTo(source, pos, Library.NEG_Z);
        boolean pZ = canConnectTo(source, pos, Library.POS_Z);
        int mask = (pX ? 32 : 0) + (nX ? 16 : 0) + (pY ? 8 : 0) + (nY ? 4 : 0) + (pZ ? 2 : 0) + (nZ ? 1 : 0);

        if (mask == 0) {
            return new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F);
        } else if (mask == 0b110000) {
            return new AxisAlignedBB(0F, 0.3125F, 0.3125F, 1F, 0.6875F, 0.6875F);
        } else if (mask == 0b001100) {
            return new AxisAlignedBB(0.3125F, 0F, 0.3125F, 0.6875F, 1F, 0.6875F);
        } else if (mask == 0b000011) {
            return new AxisAlignedBB(0.3125F, 0.3125F, 0F, 0.6875F, 0.6875F, 1F);
        } else {
            return new AxisAlignedBB(
                    nX ? 0F : 0.3125F,
                    nY ? 0F : 0.3125F,
                    nZ ? 0F : 0.3125F,
                    pX ? 1F : 0.6875F,
                    pY ? 1F : 0.6875F,
                    pZ ? 1F : 0.6875F);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    public boolean canConnectTo(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
        TileEntity tile = world instanceof World ?
                Compat.getTileStandard((World) world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ)
                : world.getTileEntity(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ));
        return tile instanceof TileEntityPneumoTube;
    }

    public boolean canConnectToAir(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
        TileEntity te = world.getTileEntity(pos);
        TileEntityPneumoTube tube = te instanceof TileEntityPneumoTube ? (TileEntityPneumoTube) te : null;
        if(tube != null) {
            if(!tube.isCompressor()) return false;
            if(tube.insertionDir == dir || tube.ejectionDir == dir) return false;
        }
        TileEntity tile = world.getTileEntity(new BlockPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ));
        if(tile instanceof TileEntityPneumoTube) return false;
        return Library.canConnectFluid(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir, Fluids.AIR);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName().toString()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        iconBase = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube"));
        iconStraight = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_straight"));
        iconIn = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_in"));
        iconOut = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_out"));
        iconConnector = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_connector"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        IBakedModel bakedModel = new PneumoTubeBakedModel();
        ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName().toString());
        event.getModelRegistry().putObject(modelLocation, bakedModel);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc.toString());
            }
        };
    }

    public static class ConnectionProperty implements IUnlistedProperty<Boolean> {
        private final String name;

        public ConnectionProperty(String name) {
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public boolean isValid(Boolean value) { return true; }
        @Override public Class<Boolean> getType() { return Boolean.class; }
        @Override public String valueToString(Boolean value) { return value.toString(); }
    }

    public static boolean hasItemHandler(@NotNull TileEntity tile, @NotNull ForgeDirection tubeDir) {
        ForgeDirection dir = tubeDir != ForgeDirection.UNKNOWN ? tubeDir.getOpposite() : ForgeDirection.UNKNOWN;
        EnumFacing facing = dir != ForgeDirection.UNKNOWN ? dir.toEnumFacing() : null;
        if(facing != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            if(handler != null && handler.getSlots() > 0) return true;
        }
        return tile instanceof IInventory;
    }

    public static class DirectionProperty implements IUnlistedProperty<ForgeDirection> {
        private final String name;

        public DirectionProperty(String name) {
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public boolean isValid(ForgeDirection value) { return true; }
        @Override public Class<ForgeDirection> getType() { return ForgeDirection.class; }
        @Override public String valueToString(ForgeDirection value) { return value.name().toLowerCase(); }
    }
}
