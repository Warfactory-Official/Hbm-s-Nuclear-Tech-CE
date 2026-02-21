package com.hbm.blocks.network;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.items.IDynamicModels;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPipeAnchor;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FluidPipeAnchor extends FluidDuctBase implements ITooltipProvider, ILookOverlay, IDynamicModels {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public FluidPipeAnchor(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setSoundType(ModSoundTypes.pipe);
        this.useNeighborBrightness = true;
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new TileEntityPipeAnchor();
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return getAabb(state.getValue(FACING));
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return getAabb(state.getValue(FACING));
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand) {
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    private AxisAlignedBB getAabb(EnumFacing facing) {
        double pixel = 0.0625D;
        double min = pixel * 4D;
        double max = pixel * 12D;

        EnumFacing dir = facing.getOpposite();

        double minX = dir == EnumFacing.WEST ? 0D : min;
        double maxX = dir == EnumFacing.EAST ? 1D : max;
        double minY = dir == EnumFacing.DOWN ? 0D : min;
        double maxY = dir == EnumFacing.UP ? 1D : max;
        double minZ = dir == EnumFacing.NORTH ? 0D : min;
        double maxZ = dir == EnumFacing.SOUTH ? 1D : max;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, List<String> tooltip, @NotNull ITooltipFlag advanced) {
        tooltip.add(TextFormatting.GOLD + "Connection Type: " + TextFormatting.YELLOW + "Single");
        tooltip.add(TextFormatting.GOLD + "Connection Range: " + TextFormatting.YELLOW + "10m");
    }

    /* @Override GO FUCK YOURSELF
    didn't think this was overridable, that makes everything so much easier. good job martin
    public void changeTypeRecursively(World world, int x, int y, int z, FluidType prevType, FluidType type, int loopsRemaining) {

        TileEntity te = world.getTileEntity(x, y, z);

        if(te instanceof TileEntityPipeAnchor pipe) {

            if(pipe.getType() == prevType && pipe.getType() != type) {
                pipe.setType(type);

                if(loopsRemaining > 0) {
                    ForgeDirection dir = ForgeDirection.getOrientation(pipe.getBlockMetadata()).getOpposite();
                    Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

                    if(b instanceof IBlockFluidDuct) ((IBlockFluidDuct) b).changeTypeRecursively(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, prevType, type, loopsRemaining - 1);

                    for(int[] pos : pipe.getConnected()) {
                        Block c = world.getBlock(pos[0], pos[1], pos[2]);
                        if(c instanceof IBlockFluidDuct) ((IBlockFluidDuct) c).changeTypeRecursively(world, pos[0], pos[1], pos[2], prevType, type, loopsRemaining - 1);
                    }
                }
            }
        }
    } */

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

        TileEntity te = world.getTileEntity(pos);

        if(!(te instanceof TileEntityPipeBaseNT duct))
            return;

        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
            ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
            IModel blockRetextured = blockBaseModel.retexture(blockTextures);
            IBakedModel blockBaked = blockRetextured.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.BLOCK,
                    ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, blockBaked);
            IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
            ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
            IModel itemRetextured = itemBaseModel.retexture(itemTextures);
            IBakedModel itemBaked = itemRetextured.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.ITEM,
                    ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(inventoryLocation, itemBaked);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }
}
