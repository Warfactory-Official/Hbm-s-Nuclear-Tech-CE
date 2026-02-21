package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ICopiable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@AutoRegister
public class TileEntityHeaterElectric extends TileEntityLoadedBase implements IHeatSource, IEnergyReceiverMK2, ITickable, IBufPacketReceiver, ICopiable {
	
	public long power;
	public int heatEnergy;
	public boolean isOn;
	protected int setting = 1;

	private AudioWrapper audio;

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			if(world.getTotalWorldTime() % 20 == 0) { //doesn't have to happen constantly
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				this.trySubscribe(world, pos.getX() + dir.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 3, dir);
			}
			
			this.heatEnergy *= 0.999;
			
			this.tryPullHeat();

			this.isOn = false;
			if(setting > 0 && this.power >= this.getConsumption()) {
				this.power -= this.getConsumption();
				this.heatEnergy += getHeatGen();
				this.isOn = true;
			}

			networkPackNT(25);
		} else {

			if(isOn) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.electricHum, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.25F, 7.5F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.muffled);
		buf.writeByte(this.setting);
		buf.writeInt(this.heatEnergy);
		buf.writeBoolean(isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.muffled = buf.readBoolean();
		this.setting = buf.readByte();
		this.heatEnergy = buf.readInt();
		this.isOn = buf.readBoolean();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.setting = nbt.getInteger("setting");
		this.heatEnergy = nbt.getInteger("heatEnergy");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setInteger("setting", setting);
		nbt.setInteger("heatEnergy", heatEnergy);
		return nbt;
	}
	
	protected void tryPullHeat() {
		TileEntity con = world.getTileEntity(pos.add(0, -1, 0));
		
		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			this.heatEnergy += source.getHeatStored() * 0.85;
			source.useUpHeat(source.getHeatStored());
		}
	}
	
	public void toggleSettingUp() {
		setting++;
		
		if(setting > 10)
			setting = 0;
	}

	public void toggleSettingDown() {
		setting--;
		
		if(setting < 0)
			setting = 10;
	}


	@Override
	public long getPower() {
		return power;
	}
	
	public long getConsumption() {
		return (long) (Math.pow(setting, 1.4D) * 200D);
	}

	@Override
	public long getMaxPower() {
		return getConsumption() * 20;
	}
	
	public int getHeatGen() {
		return this.setting * 100;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public int getHeatStored() {
		return heatEnergy;
	}

	@Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}
	
	AxisAlignedBB bb = null;
	@Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
        }

        return bb;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("setting", setting);
		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		this.setting = nbt.getInteger("setting");
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}
