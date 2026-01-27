package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public abstract class BlockPlantEnumMeta<E extends Enum<E>> extends BlockEnumMeta<E> {

    public static Set<Block> PLANTABLE_BLOCKS = new HashSet<>();

    public BlockPlantEnumMeta(String registryName, E[] blockEnum) {
        super(Material.PLANTS, SoundType.PLANT, registryName, blockEnum, true, true);
        this.setTickRandomly(true);
    }

    public static void initPlacables(){
    }

    @Override
    protected BlockBakeFrame[] generateBlockFrames(String registryName) {
        return Arrays.stream(blockEnum)
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(Enum::name)
                .map(name -> registryName + "_" + name.toLowerCase(Locale.US))
                .map(texture -> new BlockBakeFrame(BlockBakeFrame.BlockForm.CROSS, texture))
                .toArray(BlockBakeFrame[]::new);
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return this.canBlockStay(world, pos, world.getBlockState(pos));
    }


     public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.checkAndDropBlock(worldIn, pos, state);
    }
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!this.canBlockStay(worldIn, pos, state))
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        Block block = world.getBlockState(pos.down()).getBlock();
        return PLANTABLE_BLOCKS.contains(block);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.4000000059604645D, 0.8999999761581421D);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return net.minecraftforge.common.EnumPlantType.Plains; //TODO: Make custom one for custom plants
    }

    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return getDefaultState();
        return state;
    }

    protected boolean isOiled(World world, BlockPos pos){
        return (world.getBlockState(pos.down()).getBlock() == ModBlocks.dirt_oily) || (world.getBlockState(pos.down()).getBlock() == ModBlocks.dirt_dead);

    }

    protected boolean isWatered(World world, BlockPos pos){

        Block[] directions = {
                world.getBlockState(pos.north().down()).getBlock(), // North
                world.getBlockState(pos.south().down()).getBlock(), // South
                world.getBlockState(pos.east().down()).getBlock(),  // East
                world.getBlockState(pos.west().down()).getBlock()   // West
        };

        for (Block block : directions) {
            if (block == Blocks.WATER) {
                return true;
            }
        }

        return false;

    }

    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        for (int meta = 0; meta <= META_COUNT - 1; meta++) {
            BlockBakeFrame blockFrame = blockFrames[meta % blockFrames.length];
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

                blockFrame.putTextures(textureMap);
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
                event.getModelRegistry().putObject(modelLocation, bakedModel);

                IModel itemModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
                ImmutableMap.Builder<String, String> itemTextureMap = ImmutableMap.builder();
                ResourceLocation itemTexture = new ResourceLocation(Tags.MODID, BlockBakeFrame.ROOT_PATH + blockFrame.textureArray[0]);
                itemTextureMap.put("layer0", itemTexture.toString());
                IModel retexturedItemModel = itemModel.retexture(itemTextureMap.build());
                IBakedModel bakedItemModel = retexturedItemModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()
                );
                ModelResourceLocation itemModelLocation = new ModelResourceLocation(getRegistryName(), "inventory-" + meta);
                event.getModelRegistry().putObject(itemModelLocation, bakedItemModel);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        for (int meta = 0; meta <= this.META_COUNT; meta++) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(this),
                    meta,
                    new ModelResourceLocation(this.getRegistryName(), "inventory-" + meta)
            );

        }
    }
}
