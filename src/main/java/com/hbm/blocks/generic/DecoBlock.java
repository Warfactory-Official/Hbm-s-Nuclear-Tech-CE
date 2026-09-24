package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.deco.TileEntityDecoBlock;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Consumer;

public class DecoBlock extends BlockContainer implements ICustomBlockHighlight, INBTBlockTransformable {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private static final float f = 0.0625F;
    public static final AxisAlignedBB WALL_WEST_BOX = new AxisAlignedBB(14 * f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_EAST_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 2 * f, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_NORTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 14 * f, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_SOUTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2 * f);
    public static final AxisAlignedBB STEEL_ROOF_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1 * f, 1.0F);
    public static final AxisAlignedBB STEEL_BEAM_BOX = new AxisAlignedBB(7 * f, 0.0F, 7 * f, 9 * f, 1.0F, 9 * f);
    public static final AxisAlignedBB SCAFFOLD_EASTWEST_BOX = new AxisAlignedBB(2 * f, 0.0F, 0.0F, 14 * f, 1.0F, 1.0F);
    public static final AxisAlignedBB SCAFFOLD_NORTHSOUTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 2 * f, 1.0F, 1.0F, 14 * f);

    public DecoBlock(Material materialIn, String s) {
        super(materialIn);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setCreativeTab(MainRegistry.blockTab);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if (this == ModBlocks.steel_scaffold || this == ModBlocks.steel_beam) return null;
        return new TileEntityDecoBlock();
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return this != ModBlocks.steel_beam;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        if (this != ModBlocks.steel_beam) return BlockRenderLayer.CUTOUT;
        return super.getRenderLayer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        if (this != ModBlocks.steel_beam) return layer == BlockRenderLayer.CUTOUT;
        return super.canRenderInLayer(state, layer);
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
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos,
                                        @NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        EnumFacing te = state.getValue(FACING);
        if (this == ModBlocks.steel_wall) {
            return switch (te) {
                case WEST -> WALL_WEST_BOX;
                case NORTH -> WALL_NORTH_BOX;
                case EAST -> WALL_EAST_BOX;
                case SOUTH -> WALL_SOUTH_BOX;
                default -> FULL_BLOCK_AABB;
            };
        } else if (this == ModBlocks.steel_roof) {
            return STEEL_ROOF_BOX;
        } else if (this == ModBlocks.steel_beam) {
            return STEEL_BEAM_BOX;
        } else if (this == ModBlocks.steel_scaffold) {
            return switch (te) {
                case WEST, EAST -> SCAFFOLD_EASTWEST_BOX;
                case NORTH, SOUTH -> SCAFFOLD_NORTHSOUTH_BOX;
                default -> FULL_BLOCK_AABB;
            };
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      java.util.List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        EnumFacing te = state.getValue(FACING);

        Consumer<AxisAlignedBB> add = box -> {
            if (box == null) return;
            addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
        };

        if (this == ModBlocks.steel_wall) {
            AxisAlignedBB box = switch (te) {
                case WEST -> WALL_WEST_BOX;
                case NORTH -> WALL_NORTH_BOX;
                case EAST -> WALL_EAST_BOX;
                case SOUTH -> WALL_SOUTH_BOX;
                default -> null;
            };
            add.accept(box);
            return;
        }

        if (this == ModBlocks.steel_corner) {
            AxisAlignedBB a = null, b = null;
            switch (te) {
                case EAST:
                    a = WALL_EAST_BOX;
                    b = WALL_SOUTH_BOX;
                    break;
                case NORTH:
                    a = WALL_NORTH_BOX;
                    b = WALL_EAST_BOX;
                    break;
                case SOUTH:
                    a = WALL_SOUTH_BOX;
                    b = WALL_WEST_BOX;
                    break;
                case WEST:
                    a = WALL_WEST_BOX;
                    b = WALL_NORTH_BOX;
                    break;
                default:
                    break;
            }
            add.accept(a);
            add.accept(b);
            return;
        }

        if (this == ModBlocks.steel_roof) {
            add.accept(STEEL_ROOF_BOX);
            return;
        }

        if (this == ModBlocks.steel_beam) {
            add.accept(STEEL_BEAM_BOX);
            return;
        }

        if (this == ModBlocks.steel_scaffold) {
            AxisAlignedBB box = switch (te) {
                case WEST, EAST -> SCAFFOLD_EASTWEST_BOX;
                case NORTH, SOUTH -> SCAFFOLD_NORTHSOUTH_BOX;
                default -> null;
            };
            add.accept(box);
            return;
        }

        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldDrawHighlight(World world, BlockPos pos) {
        return this == ModBlocks.steel_wall || this == ModBlocks.steel_corner || this == ModBlocks.steel_roof || this == ModBlocks.steel_beam ||
               this == ModBlocks.steel_scaffold;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return;

        EnumFacing te = state.getValue(FACING);
        final float exp = 0.002F;

        double dx = event.getPlayer().lastTickPosX + (event.getPlayer().posX - event.getPlayer().lastTickPosX) * event.getPartialTicks();
        double dy = event.getPlayer().lastTickPosY + (event.getPlayer().posY - event.getPlayer().lastTickPosY) * event.getPartialTicks();
        double dz = event.getPlayer().lastTickPosZ + (event.getPlayer().posZ - event.getPlayer().lastTickPosZ) * event.getPartialTicks();

        java.util.List<AxisAlignedBB> boxes = new java.util.ArrayList<>(2);
        if (this == ModBlocks.steel_wall) {
            boxes.add(switch (te) {
                case WEST -> WALL_WEST_BOX;
                case NORTH -> WALL_NORTH_BOX;
                case EAST -> WALL_EAST_BOX;
                case SOUTH -> WALL_SOUTH_BOX;
                default -> FULL_BLOCK_AABB;
            });
        } else if (this == ModBlocks.steel_corner) {
            switch (te) {
                case EAST:
                    boxes.add(WALL_EAST_BOX);
                    boxes.add(WALL_SOUTH_BOX);
                    break;
                case NORTH:
                    boxes.add(WALL_NORTH_BOX);
                    boxes.add(WALL_EAST_BOX);
                    break;
                case SOUTH:
                    boxes.add(WALL_SOUTH_BOX);
                    boxes.add(WALL_WEST_BOX);
                    break;
                case WEST:
                    boxes.add(WALL_WEST_BOX);
                    boxes.add(WALL_NORTH_BOX);
                    break;
                default:
                    boxes.add(FULL_BLOCK_AABB);
                    break;
            }
        } else if (this == ModBlocks.steel_roof) {
            boxes.add(STEEL_ROOF_BOX);
        } else if (this == ModBlocks.steel_beam) {
            boxes.add(STEEL_BEAM_BOX);
        } else if (this == ModBlocks.steel_scaffold) {
            boxes.add((te == EnumFacing.WEST || te == EnumFacing.EAST) ? SCAFFOLD_EASTWEST_BOX : SCAFFOLD_NORTHSOUTH_BOX);
        } else {
            return;
        }

        ICustomBlockHighlight.setup();
        for (AxisAlignedBB local : boxes) {
            AxisAlignedBB bb = local.expand(exp, exp, exp).offset(pos).offset(-dx, -dy, -dz);
            RenderGlobal.drawSelectionBoundingBox(bb, 0, 0, 0, 1.0F);
        }
        ICustomBlockHighlight.cleanup();
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);

        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, facing);
    }


    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
    }

}
