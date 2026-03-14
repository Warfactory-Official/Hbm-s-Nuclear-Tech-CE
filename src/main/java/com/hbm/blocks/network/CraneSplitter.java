package com.hbm.blocks.network;

import com.hbm.api.block.IToolable;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CraneSplitter extends BlockDummyable implements IConveyorBelt, IEnterableBlock, ITooltipProvider, IToolable, ILookOverlay {

    public CraneSplitter(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 0, 0, 0, 1};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {

        if(meta >= 12) return new TileEntityCraneSplitter();
        if(meta >= 6) return new TileEntityProxyCombo(false, false, false);

        return null;

    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.crane_splitter);
    }

    @Override
    public @NotNull ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return new ItemStack(ModBlocks.crane_splitter);
    }

    private EnumFacing getCustomMap(int meta){
        return switch (meta) {
            case 2, 14 -> EnumFacing.EAST;
            case 5, 12 -> EnumFacing.SOUTH;
            case 3, 15 -> EnumFacing.WEST;
            default -> EnumFacing.NORTH;
        };
    }

    @Override 
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) { 
        return getTravelDirection(world, new BlockPos(x, y, z)) == dir;
    }

    public EnumFacing getTravelDirection(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        int meta = state.getBlock().getMetaFromState(state);

        if (meta < 12) {
            BlockPos corePos = findCore(world, pos);
            if (corePos != null) {
                IBlockState coreState = world.getBlockState(corePos);
                meta = coreState.getBlock().getMetaFromState(coreState);
            }
        }

        return getCustomMap(meta).getOpposite();
    }

    @Override
    public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
        return true;
    }

    @Override public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { return false; }
    @Override public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
        BlockPos pos = new BlockPos(x, y, z);
        EnumFacing dir = this.getTravelDirection(world, pos);
        Vec3d snap = this.getClosestSnappingPosition(world, pos, itemPos);
        Vec3d dest = new Vec3d(
                snap.x - dir.getXOffset() * speed,
                snap.y - dir.getYOffset() * speed,
                snap.z - dir.getZOffset() * speed);
        Vec3d motion = new Vec3d(
                dest.x - itemPos.x,
                dest.y - itemPos.y,
                dest.z - itemPos.z);
        double len = motion.length();
        return new Vec3d(
                itemPos.x + motion.x / len * speed,
                itemPos.y + motion.y / len * speed,
                itemPos.z + motion.z / len * speed);
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, Vec3d itemPos) {
        EnumFacing dir = this.getTravelDirection(world, pos);

        double posX = MathHelper.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        double posZ = MathHelper.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        double y = pos.getY() + 0.25;

        if (dir.getAxis() == EnumFacing.Axis.X) {
            x = posX;
        } else if (dir.getAxis() == EnumFacing.Axis.Z) {
            z = posZ;
        }

        return new Vec3d(x, y, z);
    }
    @Override
    public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        if (entity == null || entity.getItemStack().isEmpty() || entity.getItemStack().getCount() <= 0) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = this.findCoreTE(world, pos);
        if (!(tile instanceof TileEntityCraneSplitter splitter)) return;

        ForgeDirection rot = ForgeDirection.getOrientation(splitter.getBlockMetadata() - offset).getRotation(ForgeDirection.DOWN);

        ItemStack[] splits = splitter.splitStack(entity.getItemStack());

        BlockPos corePos = splitter.getPos();
        spawnMovingItem(world, corePos.getX(), corePos.getY(), corePos.getZ(), splits[0]);
        spawnMovingItem(world, corePos.getX() + rot.offsetX, corePos.getY(), corePos.getZ() + rot.offsetZ, splits[1]);
    }

    private void spawnMovingItem(World worldIn, int x, int y, int z, ItemStack stack) {
        BlockPos pos1 = new BlockPos(x, y, z);
        int xCoord = pos1.getX();
        int yCoord = pos1.getY();
        int zCoord = pos1.getZ();
        if (stack.isEmpty() || stack.getCount() <= 0) return;
        EntityMovingItem moving = new EntityMovingItem(worldIn);
        Vec3d itemPos = new Vec3d(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
        Vec3d snap = this.getClosestSnappingPosition(worldIn, pos1, itemPos);
        moving.setPosition(snap.x, snap.y, snap.z);
        moving.setItemStack(stack);
        worldIn.spawnEntity(moving);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if(world.isRemote) return true;
        if(tool != ToolType.SCREWDRIVER) return false;

        if(!(findCoreTE(world, new BlockPos(x, y, z)) instanceof TileEntityCraneSplitter crane)) return false;

        // The core of the dummy is always the left hand block
        boolean isLeft = x == crane.getPos().getX() && y == crane.getPos().getY() && z == crane.getPos().getZ();
        int adjust = player.isSneaking() ? -1 : 1;

        if(isLeft) {
            crane.leftRatio = (byte)MathHelper.clamp(crane.leftRatio + adjust, 1, 16);
        } else {
            crane.rightRatio = (byte)MathHelper.clamp(crane.rightRatio + adjust, 1, 16);
        }

        crane.markDirty();
        crane.networkPackNT(15);

        return true;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        if(!(findCoreTE(world, pos) instanceof TileEntityCraneSplitter crane)) return;

        List<String> text = new ArrayList<>();
        text.add("Splitter ratio: " + crane.leftRatio + ":" + crane.rightRatio);

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        return super.checkRequirement(world, x, y, z, dir, o);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        MultiblockHandlerXR.fillSpace(world, x, y, z, getDimensions(), this, dir);
    }
}
