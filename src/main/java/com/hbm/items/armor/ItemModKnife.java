package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;
import java.util.UUID;

public class ItemModKnife extends ItemArmorMod {
	
	public static final UUID trigamma_UUID = UUID.fromString("86d44ca9-44f1-4ca6-bdbb-d9d33bead251");

	public ItemModKnife(String s) {
		super(ArmorModHandler.extra, false, true, false, false, s);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.RED + I18nUtil.resolveKey("desc.knifeinjector1"));
		list.add("");
		list.add(TextFormatting.RED + I18nUtil.resolveKey("desc.knifeinjector2"));
		
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.RED + "  " + stack.getDisplayName());
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!entity.world.isRemote) {
			
			if(entity.ticksExisted % 50 == 0 && entity.getMaxHealth() > 2F) {

				entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.slicer, SoundCategory.PLAYERS, 1.0F, 1.0F);
				
				NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("mode", "blood");
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Vomit, nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
				
				IAttributeInstance attributeinstance = entity.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
				
				float health = entity.getMaxHealth();
				
				try {
					attributeinstance.removeModifier(attributeinstance.getModifier(trigamma_UUID));
				} catch(Exception ex) { }
				
				attributeinstance.applyModifier(new AttributeModifier(trigamma_UUID, "digamma", -(entity.getMaxHealth() - health + 2), 0));
				
				if(entity instanceof EntityPlayerMP) {
					NBTTagCompound data = new NBTTagCompound();
					
					if(entity.getMaxHealth() > 2F) {
						data.setInteger("time", 10000 + entity.getRNG().nextInt(10000));
						data.setInteger("maxTime", 10000);
					} else {
						data.setInteger("time", 0);
						data.setInteger("maxTime", 0);
						
						AdvancementManager.grantAchievement((EntityPlayer)entity, AdvancementManager.achSomeWounds);
					}
					PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.ProperJolt, data, 0, 0, 0), (EntityPlayerMP)entity);
				}
			}
		}
	}
}