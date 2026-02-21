package com.hbm.blocks.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CraneSplitter extends BlockDummyable implements IConveyorBelt, IEnterableBlock {

    public CraneSplitter(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 0, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        if(meta >= 12) return new TileEntityCraneSplitter();
        if(meta >= 6) return new TileEntityProxyCombo(false, true, true);

        return null;

    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.crane_splitter);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ModBlocks.crane_splitter);
    }

    private EnumFacing getCustomMap(int meta){
        switch(meta){
            case 4: return EnumFacing.NORTH;
            case 2: return EnumFacing.EAST;
            case 5: return EnumFacing.SOUTH;
            case 3: return EnumFacing.WEST;

            case 13: return EnumFacing.NORTH;
            case 14: return EnumFacing.EAST;
            case 12: return EnumFacing.SOUTH;
            case 15: return EnumFacing.WEST;
            default: return EnumFacing.NORTH;
        }
    }

    @Override 
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) { 
        return getTravelDirection(world, new BlockPos(x, y, z), null) == dir; 
    }

    public EnumFacing getTravelDirection(World world, BlockPos pos, Vec3d itemPos) {
        Block block = world.getBlockState(pos).getBlock();
        int meta = block.getMetaFromState(world.getBlockState(pos));
        return getCustomMap(meta).getOpposite();
    }

    @Override
    public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
        return true;
    }

    @Override 
    public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { return true; }
    
    @Override 
    public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        if (entity == null) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return;
        pos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityCraneSplitter)) return;
        TileEntityCraneSplitter splitter = (TileEntityCraneSplitter) tile;

        boolean pos1 = splitter.getPosition();
        EnumFacing rot = getCustomMap(splitter.getBlockMetadata()).rotateY().rotateY().rotateY();

        if(pos1){
            this.spawnMovingBox(world, pos.getX(), pos.getY(), pos.getZ(), entity.getItemStacks());
        } else {
            this.spawnMovingBox(world, pos.getX() + rot.getXOffset(), pos.getY(), pos.getZ() + rot.getZOffset(), entity.getItemStacks());
        }
        splitter.setPosition(!pos1);
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
        BlockPos pos = new BlockPos(x, y, z);
        EnumFacing dir = this.getTravelDirection(world, pos, itemPos);
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
        Vec3d ret = new Vec3d(
                itemPos.x + motion.x / len * speed,
                itemPos.y + motion.y / len * speed,
                itemPos.z + motion.z / len * speed);
        return ret;
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, Vec3d itemPos) {
        EnumFacing dir = this.getTravelDirection(world, pos, itemPos);

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
        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if (core == null) return;
        pos = new BlockPos(core[0], core[1], core[2]);
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityCraneSplitter)) return;
        TileEntityCraneSplitter splitter = (TileEntityCraneSplitter) tile;

        boolean pos1 = splitter.getPosition();
        ItemStack stack = entity.getItemStack();
        EnumFacing rot = getCustomMap(splitter.getBlockMetadata()).rotateY().rotateY().rotateY();

        if (stack.getCount() % 2 == 0) {
            stack.setCount(stack.getCount()>>1);
            this.spawnMovingItem(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
            this.spawnMovingItem(world, pos.getX() + rot.getXOffset(), pos.getY(), pos.getZ() + rot.getZOffset(), stack.copy());
        } else {
            int baseSize = stack.getCount()>>1;
            if(baseSize == 0){
                if(pos1){
                    this.spawnMovingItem(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                } else {
                    this.spawnMovingItem(world, pos.getX() + rot.getXOffset(), pos.getY(), pos.getZ() + rot.getZOffset(), stack.copy());
                }
            } else {
                stack.setCount(baseSize + (pos1 ? 0 : 1));
                this.spawnMovingItem(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                stack.setCount(baseSize + (pos1 ? 1 : 0));
                this.spawnMovingItem(world, pos.getX() + rot.getXOffset(), pos.getY(), pos.getZ() + rot.getZOffset(), stack.copy());
            }
            splitter.setPosition(!pos1);
        }
    }

    private void spawnMovingItem(World worldIn, int x, int y, int z, ItemStack stack) {
        BlockPos pos1 = new BlockPos(x, y, z);
        int xCoord = pos1.getX();
        int yCoord = pos1.getY();
        int zCoord = pos1.getZ();
        if (stack.getCount() <= 0) return;
        EntityMovingItem moving = new EntityMovingItem(worldIn);
        Vec3d itemPos = new Vec3d(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
        Vec3d snap = this.getClosestSnappingPosition(worldIn, pos1, itemPos);
        moving.setPosition(snap.x, snap.y, snap.z);
        moving.setItemStack(stack);
        worldIn.spawnEntity(moving);
    }

    private void spawnMovingBox(World worldIn, int x, int y, int z, ItemStack[] stacks) {
        BlockPos pos1 = new BlockPos(x, y, z);
        int xCoord = pos1.getX();
        int yCoord = pos1.getY();
        int zCoord = pos1.getZ();
        EntityMovingPackage moving = new EntityMovingPackage(worldIn);
        Vec3d itemPos = new Vec3d(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
        Vec3d snap = this.getClosestSnappingPosition(worldIn, pos1, itemPos);
        moving.setPosition(snap.x, snap.y, snap.z);
        moving.setItemStacks(stacks);
        worldIn.spawnEntity(moving);
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
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        return super.checkRequirement(world, x, y, z, dir, o);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        MultiblockHandlerXR.fillSpace(world, x, y, z, getDimensions(), this, dir);
    }
}
