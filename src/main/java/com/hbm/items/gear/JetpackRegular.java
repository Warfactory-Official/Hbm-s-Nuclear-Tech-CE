package com.hbm.items.gear;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.armor.JetpackFueledBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;

public class JetpackRegular extends JetpackFueledBase {

	public JetpackRegular(FluidType fuel, int maxFuel, String s) {
		super(fuel, maxFuel, s);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "hbm:textures/armor/JetPackRed.png";
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		tooltip.add("Regular jetpack for simple upwards momentum.");
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		IHBMData props = HbmCapability.getData(player);
		if(!world.isRemote) {

			if(getFuel(stack) > 0 && props.isJetpackActive()) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "jetpack");
				data.setInteger("player", player.getEntityId());
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
			}
		}
		if(getFuel(stack) > 0 && props.isJetpackActive()) {
			player.fallDistance = 0;

			if(player.motionY < 0.4D)
				player.motionY += 0.1D;

			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.flamethrowerShoot, SoundCategory.PLAYERS, 0.25F, 1.5F);
			this.useUpFuel(player, stack, 5);
			ArmorUtil.resetFlightTime(player);
		}
	}
}
