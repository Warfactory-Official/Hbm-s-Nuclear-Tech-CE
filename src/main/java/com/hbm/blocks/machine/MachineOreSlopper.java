package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineOreSlopper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MachineOreSlopper extends BlockDummyable {

    public MachineOreSlopper(String s) {
        super(Material.IRON, s);		//Base
        this.bounding.add(new AxisAlignedBB(-3.5, 0, -1.5, 3.5, 1, 1.5));
        //Slop bucket
        this.bounding.add(new AxisAlignedBB(0.5, 1, -1.5, 3.5, 3.25, 1.5));
        //Shredder
        this.bounding.add(new AxisAlignedBB(-2.25, 1, -1.5, 0.25, 3.25, -0.75));
        this.bounding.add(new AxisAlignedBB(-2.25, 1, 0.75, 0.25, 3.25, 1.5));
        this.bounding.add(new AxisAlignedBB(-2.25, 1, -1.5, -2, 3.25, 1.5));
        this.bounding.add(new AxisAlignedBB(0, 1, -1.5, 0.25, 3.25, 1.5));
        this.bounding.add(new AxisAlignedBB(-2, 1, -0.75, 0, 2, 0.75));
        //Outlet
        this.bounding.add(new AxisAlignedBB(-3.25, 1, -1, -2.25, 3, 1));
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityMachineOreSlopper();
        if(meta >= 6) return new TileEntityProxyCombo(true, true, true);
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {3, 0, 3, 3, 1, 1};
    }

    @Override
    public int getOffset() {
        return 3;
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.makeExtra(world, x + dir.offsetX * 3, y, z + dir.offsetZ * 3);
        this.makeExtra(world, x - dir.offsetX * 3, y, z - dir.offsetZ * 3);
        this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ);
        this.makeExtra(world, x - rot.offsetX, y, z - rot.offsetZ);
        this.makeExtra(world, x + dir.offsetX * 2 + rot.offsetX, y, z + dir.offsetZ * 2 + rot.offsetZ);
        this.makeExtra(world, x + dir.offsetX * 2 - rot.offsetX, y, z + dir.offsetZ * 2 - rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX * 2 + rot.offsetX, y, z - dir.offsetZ * 2 + rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX * 2 - rot.offsetX, y, z - dir.offsetZ * 2 - rot.offsetZ);
    }
}
