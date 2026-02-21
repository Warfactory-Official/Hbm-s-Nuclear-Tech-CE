package com.hbm.potion;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HbmPotion extends Potion {
	
	public static HbmPotion taint;
	public static HbmPotion radiation;
	public static HbmPotion bang;
	public static HbmPotion mutation;
	public static HbmPotion radx;
	public static HbmPotion lead;
	public static HbmPotion radaway;
	public static HbmPotion telekinesis;
	public static HbmPotion phosphorus;
	public static HbmPotion stability;
	public static HbmPotion potionsickness;
	public static HbmPotion death;
	
	public HbmPotion(boolean isBad, int color, String name, int x, int y){
		super(isBad, color);
		this.setPotionName(name);
		this.setRegistryName(Tags.MODID, name);
		this.setIconIndex(x, y);
	}

	public static void init() {
		taint = registerPotion(true, 8388736, "potion.hbm_taint", 0, 0);
		radiation = registerPotion(true, 8700200, "potion.hbm_radiation", 1, 0);
		bang = registerPotion(true, 1118481, "potion.hbm_bang", 3, 0);
		mutation = registerPotion(false, 0xFF8132, "potion.hbm_mutation", 2, 0);
		radx = registerPotion(false, 0x225900, "potion.hbm_radx", 5, 0);
		lead = registerPotion(true, 0x767682, "potion.hbm_lead", 6, 0);
		radaway = registerPotion(false, 0xFFE400, "potion.hbm_radaway", 7, 0);
		telekinesis = registerPotion(true, 0x00F3FF, "potion.hbm_telekinesis", 0, 1);
		phosphorus = registerPotion(true, 0xFF3A00, "potion.hbm_phosphorus", 1, 1);
		stability = registerPotion(false, 0xD0D0D0, "potion.hbm_stability", 2, 1);
		potionsickness = registerPotion(false, 0xFF8080, "potion.hbm_potionsickness", 3, 1);
		death = registerPotion(false, 0x111111, "potion.hbm_death", 4, 1);
	}

	public static HbmPotion registerPotion(boolean isBad, int color, String name, int x, int y) {
		
		HbmPotion effect = new HbmPotion(isBad, color, name, x, y);
		ForgeRegistries.POTIONS.register(effect);
		
		return effect;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		ResourceLocation loc = new ResourceLocation(Tags.MODID, "textures/gui/potions.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(loc);
		return super.getStatusIconIndex();
	}

    @Override
	public void performEffect(EntityLivingBase entity, int level) {
        if (entity.world.isRemote) return;
		if(this == taint) {
			if(!(entity instanceof EntityCreeperTainted) && entity.world.rand.nextInt(80) == 0)
				entity.attackEntityFrom(ModDamageSource.taint, (level + 1));
			
			if(GeneralConfig.enableHardcoreTaint && !entity.world.isRemote && CompatibilityConfig.isWarDim(entity.world)) {
				
				int x = (int)Math.floor(entity.posX);
				int y = (int)Math.floor(entity.posY);
				int z = (int)Math.floor(entity.posZ);
				BlockPos pos = new BlockPos(x, y, z);
                IBlockState state = entity.world.getBlockState(pos);
				if(y > 1 && state.isNormalCube() && !state.getBlock().isAir(state, entity.world, pos)) {
					entity.world.setBlockState(pos, ModBlocks.taint.getBlockState().getBaseState().withProperty(BlockTaint.TAINTAGE, 14), 2);
				}
			} 
		}
		else if(this == radiation) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float)(level + 1F) * 0.05F);
		}
		else if(this == radaway) {
			if(entity.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null))
				entity.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).decreaseRads((level+1)*0.05F);
		}
        else if(this == bang) {
			if(CompatibilityConfig.isWarDim(entity.world)){
				entity.attackEntityFrom(ModDamageSource.bang, 10000*(level+1));

				if (!(entity instanceof EntityPlayer)){
					entity.onDeath(ModDamageSource.bang);
					entity.setHealth(0);
				}
			}
			entity.world.playSound(null, new BlockPos(entity), HBMSoundHandler.laserBang, SoundCategory.AMBIENT, 100.0F, 1.0F);
			ExplosionLarge.spawnParticles(entity.world, entity.posX, entity.posY, entity.posZ, 10);
		}
        else if(this == lead) {
			
			entity.attackEntityFrom(ModDamageSource.lead, (level + 1));
		}
        else if(this == telekinesis) {
			
			int remaining = entity.getActivePotionEffect(this).getDuration();
			
			if(remaining > 1) {
				entity.motionX = entity.motionX+(entity.getRNG().nextFloat()-0.5)*(level+1)*0.5;
				entity.motionY = entity.motionY+(entity.getRNG().nextFloat()-0.5)*(level+1)*0.5;
				entity.motionZ = entity.motionZ+(entity.getRNG().nextFloat()-0.5)*(level+1)*0.5;
			}
		}
        else if(this == phosphorus && !entity.world.isRemote && CompatibilityConfig.isWarDim(entity.world)) {
			
			entity.setFire(level+1);
		}

        else if(this == potionsickness && !entity.world.isRemote) {
			
			if(entity.world.rand.nextInt(128) == 0){
				entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 8*20, 0));
			}
		}
	}

    @Override
	public boolean isReady(int par1, int par2) {

		if(this == taint || this == potionsickness) {

	        return par1 % 2 == 0;
		}
        else if(this == radiation || this == radaway || this == telekinesis || this == phosphorus) {
			
			return true;
		}
        else if(this == bang) {

			return par1 <= 10;
		}
        else if(this == lead) {

			int k = 60;
	        return k > 0 ? par1 % k == 0 : true;
		}
		
		return false;
	}
}
