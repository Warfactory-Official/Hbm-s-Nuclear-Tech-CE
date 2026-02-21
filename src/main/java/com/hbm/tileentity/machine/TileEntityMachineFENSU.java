package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
@Deprecated
@AutoRegister
public class TileEntityMachineFENSU extends TileEntityMachineBattery {

	public EnumDyeColor color = EnumDyeColor.LIGHT_BLUE;

	public static final long maxTransfer = 10_000_000_000_000_000L; //10E
	//									9,223,372,036,854,775,807 is long max

	public float prevRotation = 0F;
	public float rotation = 0F;

	@Override
	public void update() {
		if(!world.isRemote) {
			super.update();
		} else {
			this.prevRotation = this.rotation;
			this.rotation += this.getSpeed();

			if(rotation >= 360) {
				rotation -= 360;
				prevRotation -= 360;
			}
		}
	}

	@Override public long getProviderSpeed() {
		int mode = this.getRelevantMode(true);
		return mode == mode_output || mode == mode_buffer ? maxTransfer : 0;
	}

	@Override public long getReceiverSpeed() {
		int mode = this.getRelevantMode(true);
		return mode == mode_input || mode == mode_buffer ? maxTransfer : 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeByte(this.color.getMetadata());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.color = EnumDyeColor.byMetadata(buf.readByte());
	}

	@Override
	public Nodespace.PowerNode createNode() {
		return new Nodespace.PowerNode(pos).setConnections(new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y));
	}

	@Override
	public long getPowerRemainingScaled(long i) {
		double powerScaled = (double)power / (double)getMaxPower();
		return (long)(i * powerScaled);
	}

	public float getSpeed() {
		return (float) Math.pow(Math.log(power * 0.75 + 1) * 0.05F, 5);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public long transferPower(long power, boolean simulate) {
		long freespace = this.getMaxPower() - this.getPower();
		long powerToAccept = Math.min(power, maxTransfer);
		powerToAccept = Math.min(powerToAccept, freespace);
		if (powerToAccept < 0) {
			powerToAccept = 0;
		}
		if (!simulate) {
			this.setPower(this.getPower() + powerToAccept);
		}
		return power - powerToAccept;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.color = EnumDyeColor.byMetadata(compound.getByte("color"));
		super.readFromNBT(compound);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setByte("color", (byte) this.color.getMetadata());
		return super.writeToNBT(compound);
	}

	@Override
	public long getMaxPower() {
		return Long.MAX_VALUE;
	}
}