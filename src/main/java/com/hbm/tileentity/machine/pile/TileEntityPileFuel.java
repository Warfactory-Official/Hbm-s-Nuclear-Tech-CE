package com.hbm.tileentity.machine.pile;

import com.hbm.api.block.IPileNeutronReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@AutoRegister
public class TileEntityPileFuel extends TileEntityPileBase implements IPileNeutronReceiver {

	public int heat;
	public static final int maxHeat = 1000;
	public int neutrons;
	public int lastNeutrons;
	public int progress;
	public static final int maxProgress = GeneralConfig.enable528 ? 75000 : 50000;

	@Override
	public void update() {
		
		if(!world.isRemote) {
			dissipateHeat();
			checkRedstone(react());
			transmute();
			
			if(this.heat >= maxHeat) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, true, true);
			}

			if(world.rand.nextFloat() * 2F <= this.heat / (float)this.maxHeat) {
				NBTTagCompound data = new NBTTagCompound();
				data.setDouble("mY", 0.05);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_Smoke, data, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + 1, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5),
						new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 20));
			}
			
			if(this.progress >= maxProgress) {
				world.setBlockState(pos, ModBlocks.block_graphite_plutonium.getDefaultState().withProperty(BlockMeta.META, this.getBlockMetadata() & 7), 3);
			}
		}
	}

	private void dissipateHeat() {
		this.heat -= (this.getBlockMetadata() & 4) == 4 ? heat * 0.065 : heat * 0.05; //remove 5% of the stored heat per tick; 6.5% for windscale
	}

	private int react() {

		int reaction = (int) (this.neutrons * (1D - ((double)this.heat / (double)this.maxHeat) * 0.5D)); //max heat reduces reaction by 50% due to thermal expansion

		this.lastNeutrons = this.neutrons;
		this.neutrons = 0;

		int lastProgress = this.progress;

		this.progress += reaction;

		if(reaction <= 0)
			return lastProgress;

		this.heat += reaction;

		for(int i = 0; i < 12; i++)
			this.castRay((int) Math.max(reaction * 0.25, 1));

		return lastProgress;
	}

	private void checkRedstone(int lastProgress) {
		int lastLevel = MathHelper.clamp((lastProgress * 16) / maxProgress, 0, 15);
		int newLevel = MathHelper.clamp((progress * 16) / maxProgress, 0, 15);
		if(lastLevel != newLevel)
			world.notifyNeighborsOfStateChange(pos, this.getBlockType(), true);
	}

	private void transmute() {

		if((this.getBlockMetadata() & 8) == 8) {
			if(this.progress < this.maxProgress - 1000) //Might be subject to change, but 1000 seems like a good number.
				this.progress = maxProgress - 1000;

		} else if(this.progress >= maxProgress - 1000) {
			world.setBlockState(this.pos, world.getBlockState(pos).withProperty(BlockMeta.META, this.getBlockMetadata() | 8), 3);
		}
	}

	@Override
	public void receiveNeutrons(int n) {
		this.neutrons += n;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.heat = nbt.getInteger("heat");
		this.progress = nbt.getInteger("progress");
		this.neutrons = nbt.getInteger("neutrons");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("heat", this.heat);
		nbt.setInteger("progress", this.progress);
		nbt.setInteger("neutrons", this.neutrons);
		return nbt;
	}
}