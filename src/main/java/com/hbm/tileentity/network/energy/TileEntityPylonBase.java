package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.ColorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityPylonBase extends TileEntityCableBaseNT {
	
	public List<int[]> connected = new ArrayList<>();
	public int color;
	private AxisAlignedBB bb;

	public static int canConnect(TileEntityPylonBase first, TileEntityPylonBase second) {

		if(first.getConnectionType() != second.getConnectionType())
			return 1;

		if(first == second)
			return 2;

		double len = Math.min(first.getMaxWireLength(), second.getMaxWireLength());

		Vec3d firstPos = first.getConnectionPoint();
		Vec3d secondPos = second.getConnectionPoint();

		Vec3d delta = new Vec3d(
				(secondPos.x) - (firstPos.x),
				(secondPos.y) - (firstPos.y),
				(secondPos.z) - (firstPos.z)
		);

		return len >= delta.length() ? 0 : 3;
	}

	public boolean setColor(ItemStack stack) {
		if(stack == ItemStack.EMPTY) return false;
		int color = ColorUtil.getColorFromDye(stack);
		if(color == 0 || color == this.color) return false;
		stack.shrink(1);
		this.color = color;

		this.markDirty();
		if (world instanceof WorldServer server) {
			server.notifyBlockUpdate(pos, server.getBlockState(pos), world.getBlockState(pos), 3);
		}

		return true;
	}

	@Override
	public Nodespace.PowerNode createNode() {
		TileEntity tile = this;
		Nodespace.PowerNode node = new Nodespace.PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN));
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}

	public void addConnection(int x, int y, int z) {

		connected.add(new int[] {x, y, z});
		this.bb = null;

		Nodespace.PowerNode node = Nodespace.getNode(world, pos);
		if (node == null) return;
		node.recentlyChanged = true;
		node.addConnection(new DirPos(x, y, z, ForgeDirection.UNKNOWN));

		this.markDirty();

		if(world instanceof WorldServer server) {
			server.notifyBlockUpdate(pos, server.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

	public void disconnectAll() {

		for(int[] pos : connected) {

			TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

			if(te == this)
				continue;

			if(te instanceof TileEntityPylonBase pylon) {
				Nodespace.destroyNode(world, new BlockPos(pos[0], pos[1], pos[2]));

				for(int i = 0; i < pylon.connected.size(); i++) {
					int[] conPos = pylon.connected.get(i);

					if(conPos[0] == this.pos.getX() && conPos[1] == this.pos.getY() && conPos[2] == this.pos.getZ()) {
						pylon.connected.remove(i);
						i--;
					}
				}

				pylon.bb = null;
				pylon.markDirty();

				if(world instanceof WorldServer worldS) {
					worldS.notifyBlockUpdate(pylon.pos, worldS.getBlockState(pylon.pos), world.getBlockState(pylon.pos), 3);
				}
			}
		}

		Nodespace.destroyNode(world, pos);
	}

	public abstract ConnectionType getConnectionType();
	public abstract Vec3d[] getMountPos();
	public abstract double getMaxWireLength();

	public Vec3d getConnectionPoint() {
		Vec3d[] mounts = this.getMountPos();

		if(mounts == null || mounts.length == 0)
			return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

		return mounts[0].add(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("conCount", connected.size());
		nbt.setInteger("color", color);

		for(int i = 0; i < connected.size(); i++) {
			nbt.setIntArray("con" + i, connected.get(i));
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int count = nbt.getInteger("conCount");
		this.color = nbt.getInteger("color");

		this.connected.clear();
		this.bb = null;

		for(int i = 0; i < count; i++) {
			connected.add(nbt.getIntArray("con" + i));
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(this.pos, 0, nbt);
	}

	@Override
	public @NotNull NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		return this.writeToNBT(nbt);
	}

	@Override
	public void handleUpdateTag(@NotNull NBTTagCompound tag) {
		this.readFromNBT(tag);
	}
	
	@Override
	public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		if (world != null) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	public enum ConnectionType {
		SINGLE,
		TRIPLE,
		QUAD
		//more to follow
	}
	
	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		if (bb != null) return bb;
		double minX = pos.getX(), minY = pos.getY(), minZ = pos.getZ();
		double maxX = pos.getX() + 1, maxY = pos.getY() + 1, maxZ = pos.getZ() + 1;

		for (Vec3d m : getMountPos()) {
			double mx = pos.getX() + m.x;
			double my = pos.getY() + m.y;
			double mz = pos.getZ() + m.z;
			if (mx < minX) minX = mx;
			if (my < minY) minY = my;
			if (my > maxY) maxY = my;
			if (mz < minZ) minZ = mz;
			if (mx > maxX) maxX = mx;
			if (mz > maxZ) maxZ = mz;
		}

		for (int[] c : connected) {
			TileEntity te = world != null ? world.getTileEntity(new BlockPos(c[0], c[1], c[2])) : null;
			if (te instanceof TileEntityPylonBase other) {
				for (Vec3d m : other.getMountPos()) {
					double mx = c[0] + m.x;
					double my = c[1] + m.y;
					double mz = c[2] + m.z;
					if (mx < minX) minX = mx;
					if (my < minY) minY = my;
					if (my > maxY) maxY = my;
					if (mz < minZ) minZ = mz;
					if (mx > maxX) maxX = mx;
					if (mz > maxZ) maxZ = mz;
				}
			} else {
				if (c[0] < minX) minX = c[0];
				if (c[1] < minY) minY = c[1];
				if (c[2] < minZ) minZ = c[2];
				if (c[0] + 1 > maxX) maxX = c[0] + 1;
				if (c[1] + 1 > maxY) maxY = c[1] + 1;
				if (c[2] + 1 > maxZ) maxZ = c[2] + 1;
			}
		}

		// cable sag: up to min(length/15, 2.5) blocks below the straight line
		minY -= 2.5;

		return bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
