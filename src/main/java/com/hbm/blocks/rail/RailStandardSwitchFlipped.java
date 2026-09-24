package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.rail.TileEntityRailSwitch;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RailStandardSwitchFlipped extends BlockRailWaypointSystem {

    public RailStandardSwitchFlipped(String s) {
        super(Material.IRON, s);

        RailDef main = new RailDef("main");
        RailDef side = new RailDef("side");
        railDefs.add(main);
        railDefs.add(side);

        main.nodes.add(new Vec3d(-8.5, 0.1875, 0.5));
        main.nodes.add(new Vec3d(-7.5, 0.1875, 0.5));
        main.nodes.add(new Vec3d(6.5, 0.1875, 0.5));
        main.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
        main.nodes.add(new Vec3d(8.5, 0.1875, 0.5));

        side.nodes.add(new Vec3d(-8.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-7.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-6.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-5.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-4.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-3.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-2.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-1.5, 0.1875, -3.5));
        side.nodes.add(new Vec3d(-0.5, 0.1875, -3.25));
        side.nodes.add(new Vec3d(0.5, 0.1875, -2.9375));
        side.nodes.add(new Vec3d(1.5, 0.1875, -2.375));
        side.nodes.add(new Vec3d(2.5, 0.1875, -1.4625));
        side.nodes.add(new Vec3d(3.5, 0.1875, -0.75));
        side.nodes.add(new Vec3d(4.5, 0.1875, -0.1875));
        side.nodes.add(new Vec3d(5.5, 0.1875, 0.175));
        side.nodes.add(new Vec3d(6.5, 0.1875, 0.375));
        side.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
        side.nodes.add(new Vec3d(8.5, 0.1875, 0.5));
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityRailSwitch();
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 7, 7, 1, 0};
    }

    @Override
    public int getOffset() {
        return 7;
    }

    @Override
    public TrackGauge getGauge(World world, int x, int y, int z) {
        return TrackGauge.STANDARD;
    }

    @Override
    public boolean canCross(World world, int x, int y, int z, Vec3d from, Vec3d to, RailDef def) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if(!(te instanceof TileEntityRailSwitch)) return true;
        TileEntityRailSwitch tile = (TileEntityRailSwitch) te;

        ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockState(new BlockPos(x, y, z)).getValue(META) - 10);

        if(dir == Library.POS_X) if(from.x < to.x) return true;
        if(dir == Library.NEG_X) if(from.x > to.x) return true;
        if(dir == Library.POS_Z) if(from.z < to.z) return true;
        if(dir == Library.NEG_Z) if(from.z > to.z) return true;

        if(dir == Library.POS_X) if(to.x < x + 0.5 + 7) return true;
        if(dir == Library.NEG_X) if(to.x > x + 0.5 - 7) return true;
        if(dir == Library.POS_Z) if(to.z < z + 0.5 + 7) return true;
        if(dir == Library.NEG_Z) if(to.z > z + 0.5 - 7) return true;

        if(tile.isSwitched) {
            return "side".equals(def.name);
        } else {
            return "main".equals(def.name);
        }
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(world.isRemote) return true;
        if(player.isSneaking()) return false;
        if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.train) return false;

        int[] core = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

        if(core != null) {
            BlockPos corePos = new BlockPos(core[0], core[1], core[2]);
            TileEntity tile = world.getTileEntity(corePos);

            if(tile instanceof TileEntityRailSwitch) {
                TileEntityRailSwitch sw = (TileEntityRailSwitch) tile;
                sw.isSwitched = !sw.isSwitched;
                sw.markDirty();
                IBlockState coreState = world.getBlockState(corePos);
                world.notifyBlockUpdate(corePos, coreState, coreState, 3);
            }
        }

        return true;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        if(!super.checkRequirement(world, x, y, z, dir, o)) return false;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        for(int i = 0; i < 4; i++) if(!replaceable(world, x + dX * (2 + i) + rX * 2, y, z + dZ * (2 + i) + rZ * 2)) return false;
        for(int i = 0; i < 2; i++) if(!replaceable(world, x + dX * (4 + i) + rX * 3, y, z + dZ * (4 + i) + rZ * 3)) return false;
        if(!replaceable(world, x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4)) return false;
        for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) if(!replaceable(world, x + dX * (6 + j) + rX * (3 + i), y, z + dZ * (6 + j) + rZ * (3 + i))) return false;
        if(!replaceable(world, x + dX * 7 + rX * 5, y, z + dZ * 7 + rZ * 5)) return false;
        for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) if(!replaceable(world, x + dX * (8 + j) + rX * (4 + i), y, z + dZ * (8 + j) + rZ * (4 + i))) return false;

        return true;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        BlockDummyable.safeRem = true;

        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
        dir = dir.getOpposite();

        int dX = dir.offsetX;
        int dZ = dir.offsetZ;
        int rX = rot.offsetX;
        int rZ = rot.offsetZ;

        int d = dir.ordinal();
        int r = rot.ordinal();

        for(int i = 0; i < 4; i++) place(world, x + dX * (2 + i) + rX, y, z + dZ * (2 + i) + rZ, r);
        for(int i = 0; i < 2; i++) place(world, x + dX * (4 + i) + rX * 2, y, z + dZ * (4 + i) + rZ * 2, r);
        place(world, x + dX * 5 + rX * 3, y, z + dZ * 5 + rZ * 3, r);
        for(int j = 0; j < 2; j++) for(int i = 0; i < 2; i++) place(world, x + dX * (6 + j) + rX * (2 + i), y, z + dZ * (6 + j) + rZ * (2 + i), d);
        place(world, x + dX * 7 + rX * 4, y, z + dZ * 7 + rZ * 4, r);
        for(int j = 0; j < 7; j++) for(int i = 0; i < 2; i++) place(world, x + dX * (8 + j) + rX * (3 + i), y, z + dZ * (8 + j) + rZ * (3 + i), d);

        BlockDummyable.safeRem = false;
    }
}
