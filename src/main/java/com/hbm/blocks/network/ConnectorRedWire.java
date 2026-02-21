package com.hbm.blocks.network;


import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.network.energy.PylonBase;
import com.hbm.items.IDynamicModels;
import com.hbm.tileentity.network.TileEntityConnector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

//TODO: throw in dummy baked model into it to override the particles
public class ConnectorRedWire extends PylonBase implements ICustomBlockItem {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    private static final double f = 1d / 16d;
    private static final double min = 5 * f;
    private static final double max = 11 * f;

    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(min, 0.0D, min, max, max, max);
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(min, min, min, max, 1.0D, max);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(min, min, 0.0D, max, max, max);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(min, min, min, max, max, 1.0D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, min, min, max, max, max);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(min, min, min, 1.0D, max, max);


    public ConnectorRedWire(Material mat, String reg) {
        super(mat, reg);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityConnector();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
            case UP -> AABB_UP;
            case DOWN -> AABB_DOWN;
        };
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return this.getBoundingBox(state, worldIn, pos);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        list.add(TextFormatting.GOLD + "Connection Type: " + TextFormatting.YELLOW + "Single");
        list.add(TextFormatting.GOLD + "Connection Range: " + TextFormatting.YELLOW + "10m");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    public void registerItem() {
        ItemBlock itemBlock = new DynModelBlockItem(this, "red_connector");
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    private static class DynModelBlockItem extends ItemBlock implements IDynamicModels {
        String texturePath;

        public DynModelBlockItem(Block block, String texturePath) {
            super(block);
            this.texturePath = texturePath;
            IDynamicModels.INSTANCES.add(this);
        }

        @Override
        public void bakeModel(ModelBakeEvent event) {
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath);
                IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
                IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
                event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void registerModel() {
            ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath), "inventory"));
        }

        @Override
        public void registerSprite(TextureMap map) {
            map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
        }
    }
}

