package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.tileentity.machine.TileEntityCargoElevator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockCargoElevator extends BlockDummyable {

    private static final AxisAlignedBB DETAIL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.999D, 1.0D);

    public BlockCargoElevator(String registryName) {
        super(net.minecraft.block.material.Material.IRON, registryName);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta >= offset ? new TileEntityCargoElevator() : null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return DETAIL_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (player.isSneaking()) return false;

        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return true;

        BlockPos corePos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(corePos);
        if (!(tile instanceof TileEntityCargoElevator elevator)) return true;

        if (!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == Item.getItemFromBlock(this)) {
            boolean replaceable = true;
            int topY = corePos.getY() + elevator.height + 1;
            for (int x = corePos.getX() - 1; x < corePos.getX() + 2 && replaceable; x++) {
                for (int z = corePos.getZ() - 1; z < corePos.getZ() + 2; z++) {
                    BlockPos checkPos = new BlockPos(x, topY, z);
                    if (!world.getBlockState(checkPos).getBlock().isReplaceable(world, checkPos)) {
                        replaceable = false;
                        break;
                    }
                }
            }

            if (replaceable) {
                for (int x = corePos.getX() - 1; x < corePos.getX() + 2; x++) {
                    for (int z = corePos.getZ() - 1; z < corePos.getZ() + 2; z++) {
                        world.setBlockState(new BlockPos(x, topY, z), this.getDefaultState().withProperty(META, 1), 3);
                    }
                }
                elevator.height++;
                elevator.markDirty();
                elevator.markChanged();
                if (!player.capabilities.isCreativeMode) {
                    player.getHeldItem(hand).shrink(1);
                }
            }
        } else {
            elevator.toggleElevator();
        }

        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return;

        BlockPos corePos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(corePos);
        if (!(tile instanceof TileEntityCargoElevator elevator)) return;

        for (AxisAlignedBB aabb : getAABBs(elevator, corePos)) {
            if (entityBox.intersects(aabb)) {
                collidingBoxes.add(aabb);
            }
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return null;

        BlockPos corePos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(corePos);
        if (!(tile instanceof TileEntityCargoElevator elevator)) return null;

        for (AxisAlignedBB aabb : getAABBs(elevator, corePos)) {
            RayTraceResult intercept = aabb.calculateIntercept(start, end);
            if (intercept != null) {
                return new RayTraceResult(intercept.hitVec, intercept.sideHit, pos);
            }
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldDrawHighlight(World world, BlockPos pos) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {
        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return;

        BlockPos corePos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(corePos);
        if (!(tile instanceof TileEntityCargoElevator elevator)) return;

        double dx = event.getPlayer().lastTickPosX + (event.getPlayer().posX - event.getPlayer().lastTickPosX) * event.getPartialTicks();
        double dy = event.getPlayer().lastTickPosY + (event.getPlayer().posY - event.getPlayer().lastTickPosY) * event.getPartialTicks();
        double dz = event.getPlayer().lastTickPosZ + (event.getPlayer().posZ - event.getPlayer().lastTickPosZ) * event.getPartialTicks();
        float exp = 0.002F;

        ICustomBlockHighlight.setup();
        for (AxisAlignedBB aabb : getAABBs(elevator, corePos)) {
            RenderGlobal.drawSelectionBoundingBox(aabb.grow(exp).offset(-dx, -dy, -dz), 0.0F, 0.0F, 0.0F, 1.0F);
        }
        ICustomBlockHighlight.cleanup();
    }

    private AxisAlignedBB[] getAABBs(TileEntityCargoElevator elevator, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int height = elevator.height + 1;
        return new AxisAlignedBB[] {
                new AxisAlignedBB(x - 1, y, z - 1, x - 0.75D, y + height, z - 0.75D),
                new AxisAlignedBB(x - 1, y, z + 1.75D, x - 0.75D, y + height, z + 2D),
                new AxisAlignedBB(x + 1.75D, y, z - 1, x + 2D, y + height, z - 0.75D),
                new AxisAlignedBB(x + 1.75D, y, z + 1.75D, x + 2D, y + height, z + 2D),
                new AxisAlignedBB(x - 1, y + 0.75D + elevator.extension, z - 1, x + 2D, y + 1D + elevator.extension, z + 2D),
        };
    }
}
