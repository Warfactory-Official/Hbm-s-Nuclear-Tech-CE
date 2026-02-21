package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockMetalFence extends BlockPane implements ICustomBlockItem {

    public static final PropertyBool FORCE_POST = PropertyBool.create("force_post");
    public static final PropertyBool PILLAR = PropertyBool.create("pillar");

    public static final AxisAlignedBB PILLAR_SHORT_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);
    public static final AxisAlignedBB SOUTH_SHORT_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.625D, 0.625D, 1.0D, 1.0D);
    public static final AxisAlignedBB WEST_SHORT_AABB  = new AxisAlignedBB(0.0D,   0.0D, 0.375D, 0.375D, 1.0D, 0.625D);
    public static final AxisAlignedBB NORTH_SHORT_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D,   0.625D, 1.0D, 0.375D);
    public static final AxisAlignedBB EAST_SHORT_AABB  = new AxisAlignedBB(0.625D, 0.0D, 0.375D, 1.0D,   1.0D, 0.625D);

    public BlockMetalFence(Material materialIn, String id) {
        super(materialIn, true);
        this.setSoundType(SoundType.METAL);
        this.setTranslationKey(id);
        this.setRegistryName(id);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(NORTH, Boolean.FALSE)
                .withProperty(EAST,  Boolean.FALSE)
                .withProperty(SOUTH, Boolean.FALSE)
                .withProperty(WEST,  Boolean.FALSE)
                .withProperty(FORCE_POST, Boolean.FALSE)
                .withProperty(PILLAR, Boolean.TRUE)
        );
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, FORCE_POST, PILLAR);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FORCE_POST, (meta & 1) == 1)
                .withProperty(PILLAR, Boolean.TRUE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(FORCE_POST)) meta |= 1;
        return meta;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        return state.withProperty(PILLAR, shouldShowPillar(state));
    }

    private static boolean shouldShowPillar(IBlockState state) {
        boolean xNeg = state.getValue(WEST);
        boolean xPos = state.getValue(EAST);
        boolean zNeg = state.getValue(NORTH);
        boolean zPos = state.getValue(SOUTH);

        boolean hasX = xNeg || xPos;
        boolean hasZ = zNeg || zPos;

        boolean straightX = !hasZ && xNeg && xPos;
        boolean straightZ = !hasX && zNeg && zPos;

        return state.getValue(FORCE_POST) || (!straightX && !straightZ);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, java.util.List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        if (!isActualState) {
            state = this.getActualState(state, worldIn, pos);
        }

        if (state.getValue(PILLAR)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, PILLAR_SHORT_AABB);
        }
        if (state.getValue(NORTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_SHORT_AABB);
        }
        if (state.getValue(EAST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_SHORT_AABB);
        }
        if (state.getValue(SOUTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_SHORT_AABB);
        }
        if (state.getValue(WEST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_SHORT_AABB);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);

        AxisAlignedBB box = null;

        if (state.getValue(PILLAR)) {
            box = PILLAR_SHORT_AABB;
        }
        if (state.getValue(NORTH)) {
            box = union(box, NORTH_SHORT_AABB);
        }
        if (state.getValue(EAST)) {
            box = union(box, EAST_SHORT_AABB);
        }
        if (state.getValue(SOUTH)) {
            box = union(box, SOUTH_SHORT_AABB);
        }
        if (state.getValue(WEST)) {
            box = union(box, WEST_SHORT_AABB);
        }

        return box == null ? PILLAR_SHORT_AABB : box;
    }

    private static AxisAlignedBB union(AxisAlignedBB a, AxisAlignedBB b) {
        if (a == null) return b;
        if (b == null) return a;
        return new AxisAlignedBB(
                Math.min(a.minX, b.minX),
                Math.min(a.minY, b.minY),
                Math.min(a.minZ, b.minZ),
                Math.max(a.maxX, b.maxX),
                Math.max(a.maxY, b.maxY),
                Math.max(a.maxZ, b.maxZ)
        );
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FORCE_POST, (meta & 1) == 1);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    @Override
    public void registerItem() {
        ForgeRegistries.ITEMS.register(new ItemBlockMetalFence(this));
    }

    public static class ItemBlockMetalFence extends ItemBlock {

        public ItemBlockMetalFence(Block block) {
            super(block);
            this.setRegistryName(block.getRegistryName());
            this.setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage) {
            return damage & 1;
        }

        @Override
        public String getTranslationKey(ItemStack stack) {
            int meta = stack.getMetadata() & 1;
            String base = super.getTranslationKey();
            return meta == 1 ? base + "_post" : base;
        }
    }
}
