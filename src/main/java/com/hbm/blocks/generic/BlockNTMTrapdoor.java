package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class BlockNTMTrapdoor extends BlockTrapDoor implements IDynamicModels {

    private static final AxisAlignedBB LADDER_OPEN_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
    private static final AxisAlignedBB LADDER_OPEN_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB LADDER_OPEN_WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
    private static final AxisAlignedBB LADDER_OPEN_EAST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final PropertyBool LADDER = PropertyBool.create("ladder");

    private final BlockBakeFrame blockFrame;

    public BlockNTMTrapdoor(Material material, String name, BlockBakeFrame blockFrame) {
        super(material);
        this.blockFrame = blockFrame;
        this.useNeighborBrightness = true;
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false).withProperty(HALF, BlockTrapDoor.DoorHalf.BOTTOM).withProperty(LADDER, false));
        if (material == Material.IRON) {
            this.setSoundType(SoundType.METAL);
        } else {
            this.setSoundType(SoundType.WOOD);
        }
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    public BlockNTMTrapdoor(Material material, String name) {
        this(material, name, new BlockBakeFrame(BlockBakeFrame.BlockForm.ALL, name));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        updateLadderState(world, pos);
    }


    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        updateLadderState(world, pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(LADDER, isLadder(state, world, pos, null));
    }

    private void updateLadderState(World world, BlockPos pos) {
        if (world.isRemote) {
            return;
        }
        IBlockState currentState = world.getBlockState(pos);
        if (currentState.getBlock() != this) {
            return;
        }
        boolean ladder = isLadder(currentState, world, pos, null);
        if (currentState.getValue(LADDER) != ladder) {
            world.setBlockState(pos, currentState.withProperty(LADDER, ladder), 2);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, OPEN, HALF, LADDER);
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        if (!state.getValue(OPEN)) {
            return false;
        }
        BlockPos belowPos = pos.down();
        IBlockState belowState = world.getBlockState(belowPos);
        return belowState.getBlock().isLadder(belowState, world, belowPos, entity);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(OPEN) && isLadder(state, source, pos, null)) {
            return getLadderBoundingBox(state);
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(OPEN) && isLadder(state, world, pos, null)) {
            return getLadderBoundingBox(state);
        }
        return super.getCollisionBoundingBox(state, world, pos);
    }

    private AxisAlignedBB getLadderBoundingBox(IBlockState state) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> LADDER_OPEN_SOUTH_AABB;
            case WEST -> LADDER_OPEN_WEST_AABB;
            case EAST -> LADDER_OPEN_EAST_AABB;
            default -> LADDER_OPEN_NORTH_AABB;
        };
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        IBlockState toggledState = state.cycleProperty(OPEN);
        world.setBlockState(pos, toggledState, 2);
        world.playEvent(null, 1003, pos, 0);
        boolean nowOpen = toggledState.getValue(OPEN);
        SoundEvent sound = (nowOpen ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE);
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        ResourceLocation registryName = this.getRegistryName();
        if (registryName == null) {
            return;
        }
        try {
            String texture = blockFrame.getSpriteLoc(0).toString();

            IModel bottomModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/trapdoor_bottom"));
            IModel topModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/trapdoor_top"));
            IModel openModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/trapdoor_open"));

            ImmutableMap<String, String> textureMap = ImmutableMap.of("texture", texture);
            IModel texturedBottom = bottomModel.retexture(textureMap);
            IModel texturedTop = topModel.retexture(textureMap);
            IModel texturedOpen = openModel.retexture(textureMap);

            ModelResourceLocation inventoryLocation = new ModelResourceLocation(registryName, "inventory");
            IBakedModel inventoryModel = texturedBottom.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
            event.getModelRegistry().putObject(inventoryLocation, inventoryModel);

            for (IBlockState state : this.blockState.getValidStates()) {
                ModelRotation rotation = getRotationForState(state);
                IModel stateModel = getModelForState(state, texturedBottom, texturedTop, texturedOpen);
                IBakedModel bakedModel = stateModel.bake(rotation, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
                ModelResourceLocation stateLocation = new ModelResourceLocation(registryName, getVariantString(state));
                event.getModelRegistry().putObject(stateLocation, bakedModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private ModelRotation getRotationForState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        boolean open = state.getValue(OPEN);
        boolean ladder = state.getValue(LADDER);

        int xRot = 0;
        int yRot = BlockBakeFrame.getYRotationForFacing(facing);

        if (open && !ladder) {
            xRot = 180;
        }

        return ModelRotation.getModelRotation(xRot, yRot);
    }

    @SideOnly(Side.CLIENT)
    private IModel getModelForState(IBlockState state, IModel bottom, IModel top, IModel open) {
        if (state.getValue(OPEN)) {
            return open;
        }
        return state.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP ? top : bottom;
    }

    @SideOnly(Side.CLIENT)
    private String getVariantString(IBlockState state) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            if (builder.length() != 0) {
                builder.append(",");
            }
            IProperty<?> property = entry.getKey();
            builder.append(property.getName()).append("=").append(getPropertyValueName(property, entry.getValue()));
        }
        return builder.toString();
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String getPropertyValueName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        ResourceLocation registryName = this.getRegistryName();
        if (registryName == null) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        blockFrame.registerBlockTextures(map);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, getVariantString(state));
            }
        };
    }
}
