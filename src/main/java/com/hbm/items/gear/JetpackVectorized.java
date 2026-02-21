package com.hbm.items.gear;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.armor.JetpackFueledBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;

public class JetpackVectorized extends JetpackFueledBase {

	public JetpackVectorized(FluidType fuel, int maxFuel, String s) {
		super(fuel, maxFuel, s);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "hbm:textures/armor/JetPackGreen.png";
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		tooltip.add("High-mobility jetpack.");
		tooltip.add("Higher fuel consumption.");
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
	
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		IHBMData props = HbmCapability.getData(player);

		if(!world.isRemote) {

			if(getFuel(stack) > 0 && props.isJetpackActive()) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "jetpack");
				data.setInteger("player", player.getEntityId());
				data.setInteger("mode", 1);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
			}
		}
		if(getFuel(stack) > 0 && props.getKeyPressed(EnumKeybind.JETPACK) && props.isJetpackActive()) {

			if(player.motionY < 0.4D)
				player.motionY += 0.1D;

			Vec3d look = player.getLookVec();

			if(Vec3.createVectorHelper(player.motionX, player.motionY, player.motionZ).length() < 2) {
				player.motionX += look.x * 0.1;
				player.motionY += look.y * 0.1;
				player.motionZ += look.z * 0.1;

				if(look.y > 0)
					player.fallDistance = 0;
			}

			world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.flamethrowerShoot, SoundCategory.PLAYERS, 0.25F, 1.5F);
			this.useUpFuel(player, stack, 3);
			ArmorUtil.resetFlightTime(player);

		}
	}

}
