package com.hbm.util;

import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingCapability.EntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.grenade.EntityGrenadeASchrab;
import com.hbm.entity.grenade.EntityGrenadeNuclear;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.missile.EntityMIRV;
import com.hbm.entity.mob.EntityCreeperNuclear;
import com.hbm.entity.mob.EntityQuackos;
import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.entity.projectile.EntityExplosiveBeam;
import com.hbm.entity.projectile.EntityMiniMIRV;
import com.hbm.entity.projectile.EntityMiniNuke;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.HazmatRegistry;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.type.HazardTypeRadiation;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ContaminationUtil {

	public static final String NTM_NEUTRON_NBT_KEY = "ntmNeutron";
    public static final String RAD_MULT_KEY = "hbmradmultiplier";
    public static Class<?>[] immuneEntities  = new Class<?>[]{
            EntityCreeperNuclear.class,
            EntityMooshroom.class,
            EntityZombie.class,
            EntitySkeleton.class,
            EntityQuackos.class,
            EntityOcelot.class,
            IRadiationImmune.class,
            // 1.12.2 Addition
            EntityZombieHorse.class,
            EntitySkeletonHorse.class,
            EntityArmorStand.class
    };

    /**
	 * Calculates how much radiation can be applied to this entity by calculating resistance
	 * @param entity
	 * @return
	 */
    public static double calculateRadiationMod(EntityLivingBase entity) {

        if (entity.isPotionActive(HbmPotion.mutation))
            return 0D;
        double mult = 1D;
        if (entity.getEntityData().hasKey(RAD_MULT_KEY, 99))
            mult = entity.getEntityData().getFloat(RAD_MULT_KEY);

        double koeff = 10.0D;
        return Math.pow(koeff, -(getConfigEntityRadResistance(entity) + HazmatRegistry.getResistance(entity))) * mult;
    }

    private static void applyRadData(Entity e, double f) {

		if(e instanceof IRadiationImmune)
			return;
		
		if(!(e instanceof EntityLivingBase entity))
			return;

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;
		
		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

        f *= calculateRadiationMod(entity);

		if(entity.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
            HbmLivingCapability.IEntityHbmProps ent = entity.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);
            ent.increaseRads(f);
        }
    }

    private static void applyRadDirect(Entity entity, double f) {

		if(entity instanceof IRadiationImmune)
			return;

        if (entity.getEntityData().hasKey(RAD_MULT_KEY, 99))
            f *= entity.getEntityData().getFloat(RAD_MULT_KEY);

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;
		
		if(!(entity instanceof EntityLivingBase))
			return;

		if(((EntityLivingBase) entity).isPotionActive(HbmPotion.mutation))
			return;

		if(entity.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)) {
            HbmLivingCapability.IEntityHbmProps ent = entity.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null);
            ent.increaseRads(f);
        }
    }

    public static void printGeigerData(EntityPlayer player) {
        double rawRadMod = ContaminationUtil.calculateRadiationMod(player);
        double eRad = HbmLivingProps.getRadiation(player);
        double rads = ChunkRadiationManager.proxy.getRadiation(player.world, player.getPosition());
        double env = getPlayerRads(player);
        double res = (1.0 - rawRadMod) * 100.0;
        double resKoeff = HazmatRegistry.getResistance(player) * 100.0;
        double rec = env * rawRadMod;
        double ar;
        String eRadS, radsS, envS, recS, resS, resKoeffS;
        ar = Math.abs(eRad);
        eRadS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-3)) ? String.format("%.3e", eRad) : String.format("%.3f", eRad);
        ar = Math.abs(rads);
        radsS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-3)) ? String.format("%.3e", rads) : String.format("%.3f", rads);
        ar = Math.abs(env);
        envS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-3)) ? String.format("%.3e", env) : String.format("%.3f", env);
        ar = Math.abs(rec);
        recS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-3)) ? String.format("%.3e", rec) : String.format("%.3f", rec);
        ar = Math.abs(res);
        resS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-6)) ? String.format("%.6e", res) : String.format("%.6f", res);
        ar = Math.abs(resKoeff);
        resKoeffS = (ar >= 1.0e6 || (ar > 0.0 && ar < 1.0e-2)) ? String.format("%.2e", resKoeff) : String.format("%.2f", resKoeff);

        String chunkPrefix = getPreffixFromRad(rads);
        String envPrefix = getPreffixFromRad(env);
        String recPrefix = getPreffixFromRad(rec);
        String radPrefix = "";
        String resPrefix = "" + TextFormatting.WHITE;

        if (eRad < 200) radPrefix += TextFormatting.GREEN;
        else if (eRad < 400) radPrefix += TextFormatting.YELLOW;
        else if (eRad < 600) radPrefix += TextFormatting.GOLD;
        else if (eRad < 800) radPrefix += TextFormatting.RED;
        else if (eRad < 1000) radPrefix += TextFormatting.DARK_RED;
        else radPrefix += TextFormatting.DARK_GRAY;
        if (resKoeff > 0) resPrefix += TextFormatting.GREEN;

        //localization and server-side restrictions have turned this into a painful mess
        //a *functioning* painful mess, nonetheless
        //@formatter:off
        player.sendMessage(new TextComponentString("===== ☢ ")
                .appendSibling(new TextComponentTranslation("geiger.title"))
                .appendSibling(new TextComponentString(" ☢ ====="))
                .setStyle(new Style().setColor(TextFormatting.GOLD)));
        player.sendMessage(new TextComponentTranslation("geiger.chunkRad")
                .appendSibling(new TextComponentString(" " + chunkPrefix + radsS + " RAD/s"))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        player.sendMessage(new TextComponentTranslation("geiger.envRad")
                .appendSibling(new TextComponentString(" " + envPrefix + envS + " RAD/s"))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        player.sendMessage(new TextComponentTranslation("geiger.recievedRad")
                .appendSibling(new TextComponentString(" " + recPrefix + recS + " RAD/s"))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        player.sendMessage(new TextComponentTranslation("geiger.playerRad")
                .appendSibling(new TextComponentString(" " + radPrefix + eRadS + " RAD"))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        player.sendMessage(new TextComponentTranslation("geiger.playerRes")
                .appendSibling(new TextComponentString(" " + resPrefix + resS + "% (" + resKoeffS + ")"))
                .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        //@formatter:on
    }

	public static void printDosimeterData(EntityPlayer player) {

		double rads = ContaminationUtil.getActualPlayerRads(player);
		boolean limit = false;
		
		if(rads > 3.6D) {
			rads = 3.6D;
			limit = true;
		}
		rads = ((int)(1000D * rads))/ 1000D;
		String radsPrefix = getPreffixFromRad(rads);
		
		player.sendMessage(new TextComponentString("===== ☢ ").appendSibling(new TextComponentTranslation("dosimeter.title")).appendSibling(new TextComponentString(" ☢ =====")).setStyle(new Style().setColor(TextFormatting.GOLD)));
		player.sendMessage(new TextComponentTranslation("geiger.recievedRad").appendSibling(new TextComponentString(" " + radsPrefix + (limit ? ">" : "") + rads + " RAD/s")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
	}

	public static String getTextColorFromPercent(double percent){
		if(percent < 0.5)
			return ""+TextFormatting.GREEN;
		else if(percent < 0.6)
			return ""+TextFormatting.YELLOW;
		else if(percent < 0.7)
			return ""+TextFormatting.GOLD;
		else if(percent < 0.8)
			return ""+TextFormatting.RED;
		else if(percent < 0.9)
			return ""+TextFormatting.DARK_RED;
		else
			return ""+TextFormatting.DARK_GRAY;
	}

	public static String getTextColorLung(double percent){
		if(percent > 0.9)
			return ""+TextFormatting.GREEN;
		else if(percent > 0.75)
			return ""+TextFormatting.YELLOW;
		else if(percent > 0.5)
			return ""+TextFormatting.GOLD;
		else if(percent > 0.25)
			return ""+TextFormatting.RED;
		else if(percent > 0.1)
			return ""+TextFormatting.DARK_RED;
		else
			return ""+TextFormatting.DARK_GRAY;
	}

	public static void printDiagnosticData(EntityPlayer player) {

		double digamma = ((int)(HbmLivingProps.getDigamma(player) * 1000)) / 1000D;
		double halflife = ((int)((1D - Math.pow(0.5, digamma)) * 10000)) / 100D;
		
		player.sendMessage(new TextComponentString("===== Ϝ ").appendSibling(new TextComponentTranslation("digamma.title")).appendSibling(new TextComponentString(" Ϝ =====")).setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)));
		player.sendMessage(new TextComponentTranslation("digamma.playerDigamma").appendSibling(new TextComponentString(TextFormatting.RED + " " + digamma + " DRX")).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
		player.sendMessage(new TextComponentTranslation("digamma.playerHealth").appendSibling(new TextComponentString(getTextColorFromPercent(halflife/100D) + String.format(" %6.2f", halflife) + "%")).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
	}

	public static void printLungDiagnosticData(EntityPlayer player) {

		float playerAsbestos = 100F-((int)(10000F * HbmLivingProps.getAsbestos(player) / EntityHbmProps.maxAsbestos))/100F;
		float playerBlacklung = 100F-((int)(10000F * HbmLivingProps.getBlackLung(player) / EntityHbmProps.maxBlacklung))/100F;
		float playerTotal = (playerAsbestos * playerBlacklung/100F);
		int contagion = HbmLivingProps.getContagion(player);

		player.sendMessage(new TextComponentString("===== L ").appendSibling(new TextComponentTranslation("lung_scanner.title")).appendSibling(new TextComponentString(" L =====")).setStyle(new Style().setColor(TextFormatting.WHITE)));
		player.sendMessage(new TextComponentTranslation("lung_scanner.player_asbestos_health").setStyle(new Style().setColor(TextFormatting.WHITE)).appendSibling(new TextComponentString(String.format(getTextColorLung(playerAsbestos/100D)+" %6.2f", playerAsbestos)+" %")));
		player.sendMessage(new TextComponentTranslation("lung_scanner.player_coal_health").setStyle(new Style().setColor(TextFormatting.DARK_GRAY)).appendSibling(new TextComponentString(String.format(getTextColorLung(playerBlacklung/100D)+" %6.2f", playerBlacklung)+" %")));
		player.sendMessage(new TextComponentTranslation("lung_scanner.player_total_health").setStyle(new Style().setColor(TextFormatting.GRAY)).appendSibling(new TextComponentString(String.format(getTextColorLung(playerTotal/100D)+" %6.2f", playerTotal)+" %")));
		player.sendMessage(new TextComponentTranslation("lung_scanner.player_mku").setStyle(new Style().setColor(TextFormatting.GRAY)).appendSibling(new TextComponentTranslation(contagion > 0 ? "lung_scanner.pos" : "lung_scanner.neg" )));
		if(contagion > 0){
			player.sendMessage(new TextComponentTranslation("lung_scanner.player_mku_duration").setStyle(new Style().setColor(TextFormatting.GRAY)).appendSibling(new TextComponentString(" §c"+BobMathUtil.ticksToDateString(contagion, 72000))));
		}
	}

	public static double getActualPlayerRads(EntityLivingBase entity) {
		return getPlayerRads(entity) * ContaminationUtil.calculateRadiationMod(entity);
	}

	public static double getPlayerRads(EntityLivingBase entity) {
		double rads = HbmLivingProps.getRadBuf(entity);
		if(entity instanceof EntityPlayer)
			 rads = rads + HbmLivingProps.getNeutron(entity)*20;
		return rads;
	}

    public static double getNoNeutronPlayerRads(EntityLivingBase entity) {
        return HbmLivingProps.getRadBuf(entity) * ContaminationUtil.calculateRadiationMod(entity);
    }

	public static boolean isRadItem(ItemStack stack){
		if(stack == null)
			return false;

        return HazardSystem.getRawRadsFromStack(stack) > 0;
    }

	public static float getNeutronRads(ItemStack stack){
		if(stack != null && !stack.isEmpty() && !isRadItem(stack)){
			if(stack.hasTagCompound()){
				NBTTagCompound nbt = stack.getTagCompound();
				if(nbt.hasKey(NTM_NEUTRON_NBT_KEY)){
					return nbt.getFloat(NTM_NEUTRON_NBT_KEY) * stack.getCount();
				}
			}
		}
		return 0F;
	}

	public static void addNeutronRadInfo(ItemStack stack, EntityPlayer player, List<String> list, ITooltipFlag flagIn){
		if (HazardSystem.getRawRadsFromStack(stack) > 0) return;

        float activationRads = getNeutronRads(stack);
        if (activationRads > 0) {
            list.add("§a[" + I18nUtil.resolveKey("trait.radioactive") + "]");
            float stackRad = activationRads / stack.getCount();
            list.add(" §e" + Library.roundFloat(HazardTypeRadiation.getNewValue(stackRad), 3) + HazardTypeRadiation.getSuffix(stackRad) + " RAD/s");

            if (stack.getCount() > 1) {
                list.add(" §eStack: " + Library.roundFloat(HazardTypeRadiation.getNewValue(activationRads), 3) + HazardTypeRadiation.getSuffix(activationRads) + " RAD/s");
            }
        }
	}

	public static boolean neutronActivateInventory(EntityPlayer player, float rad, float decay) {
		boolean changed = false;
		for (int slotI = 0; slotI < player.inventory.mainInventory.size(); slotI++) {
			if (slotI != player.inventory.currentItem) {
				if (neutronActivateItem(player.inventory.getStackInSlot(slotI), rad, decay)) {
					changed = true;
				}
			}
		}
		for(ItemStack slotA : player.inventory.armorInventory){
			if (neutronActivateItem(slotA, rad, decay)) {
				changed = true;
			}
		}
		return changed;
	}

	public static boolean neutronActivateItem(ItemStack stack, float rad, float decay) {
		if (stack == null || stack.isEmpty() || stack.getCount() != 1 || isRadItem(stack)) return false;
		float prevActivation = 0;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NTM_NEUTRON_NBT_KEY)) {
			prevActivation = stack.getTagCompound().getFloat(NTM_NEUTRON_NBT_KEY);
		}

		float newActivation = prevActivation * decay + (rad / stack.getCount());

		if (newActivation < 0.0001F) {
			if (prevActivation > 0) {
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.removeTag(NTM_NEUTRON_NBT_KEY);
				if (nbt.isEmpty()) {
					stack.setTagCompound(null);
				}
				return true;
			}
		} else {
			if (Math.abs(newActivation - prevActivation) > 1e-6) {
				NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
				nbt.setFloat(NTM_NEUTRON_NBT_KEY, newActivation);
				stack.setTagCompound(nbt);
				return true;
			}
		}
		return false;
	}

	public static boolean isContaminated(ItemStack stack){
		if(!stack.hasTagCompound())
			return false;
        return stack.getTagCompound().hasKey(NTM_NEUTRON_NBT_KEY);
    }

    public static String getPreffixFromRad(double rads) {

		String chunkPrefix = "";
		
		if(rads == 0)
			chunkPrefix += TextFormatting.GREEN;
		else if(rads < 1)
			chunkPrefix += TextFormatting.YELLOW;
		else if(rads < 10)
			chunkPrefix += TextFormatting.GOLD;
		else if(rads < 100)
			chunkPrefix += TextFormatting.RED;
		else if(rads < 1000)
			chunkPrefix += TextFormatting.DARK_RED;
		else
			chunkPrefix += TextFormatting.DARK_GRAY;
		
		return chunkPrefix;
	}

    public static double getRads(Entity e) {
        if (e instanceof IRadiationImmune)
            return 0.0D;
        if (e instanceof EntityLivingBase)
            return HbmLivingProps.getRadiation((EntityLivingBase) e);
        return 0.0D;
    }

	public static float getConfigEntityRadResistance(Entity e){
		float totalResistanceValue = 0.0F;
		if(!(e instanceof EntityPlayer)){
			ResourceLocation entity_path = EntityList.getKey(e);
			Object resistanceMod = CompatibilityConfig.mobModRadresistance.get(entity_path.getNamespace());
			Object resistanceMob = CompatibilityConfig.mobRadresistance.get(entity_path.toString());
			if(resistanceMod != null){
				totalResistanceValue = totalResistanceValue + (float)resistanceMod;
			}
			if(resistanceMob != null){
				totalResistanceValue = totalResistanceValue + (float)resistanceMob;
			}	
		}
		return totalResistanceValue;
	}

	public static boolean checkConfigEntityImmunity(Entity e){
		if(!(e instanceof EntityPlayer)){
			ResourceLocation entity_path = EntityList.getKey(e);
			if(entity_path != null){
				if(CompatibilityConfig.mobModRadimmune.contains(entity_path.getNamespace())){
					return true;
				}else{
					return CompatibilityConfig.mobRadimmune.contains(entity_path.toString());
				}
			}
		}
		return false;
	}
	
	public static boolean isRadImmune(Entity e) {
		if(e instanceof EntityLivingBase livingBase && livingBase.isPotionActive(HbmPotion.mutation))
			return true;
		Class<? extends Entity> entityClass = e.getClass();
        for (Class<?> radImmuneClass : immuneEntities) {
            if (radImmuneClass.isAssignableFrom(entityClass)) return true;
        }
		return checkConfigEntityImmunity(e);
	}
	
	/// ASBESTOS ///

	public static void applyAsbestos(Entity e, int i, int dmg) {
		applyAsbestos(e, i, dmg, 1);
	}

	public static void applyAsbestos(Entity e, int i, int dmg, int chance) {

		if(!GeneralConfig.enableAsbestosDust)
			return;

		if(!(e instanceof EntityLivingBase entity))
			return;

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;
		
		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

        if(ArmorRegistry.hasProtection(entity, EntityEquipmentSlot.HEAD, HazardClass.PARTICLE_FINE)){
			if(chance > 1){
				if(entity.world.rand.nextInt(chance) == 0){
					ArmorUtil.damageGasMaskFilter(entity, 1);
				}
			}
			else{
				ArmorUtil.damageGasMaskFilter(entity, dmg);
			}
		}
		else{
			HbmLivingProps.incrementAsbestos(entity, i);
		}
	}

	public static void applyCoal(Entity e, int i, int dmg) {
		applyCoal(e, i, dmg, 1);
	}

	/// COAL ///
	public static void applyCoal(Entity e, int i, int dmg, int chance) {

		if(!GeneralConfig.enableCoalGas)
			return;

		if(!(e instanceof EntityLivingBase entity))
			return;

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;
		
		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

        if(ArmorRegistry.hasProtection(entity, EntityEquipmentSlot.HEAD, HazardClass.PARTICLE_COARSE)){
			if(chance > 1){
				if(entity.world.rand.nextInt(chance) == 0){
					ArmorUtil.damageGasMaskFilter(entity, 1);
				}
			}
			else{
				ArmorUtil.damageGasMaskFilter(entity, dmg);
			}
		}
		else{
			HbmLivingProps.incrementBlackLung(entity, i);
		}
	}
		
	/// DIGAMMA ///
	public static void applyDigammaData(Entity e, double f) {

		if(!(e instanceof EntityLivingBase entity))
			return;

		if(e instanceof EntityQuackos || e instanceof EntityOcelot)
			return;

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;
		
		if(e instanceof EntityPlayer && e.ticksExisted < 200)
			return;

        if(entity.isPotionActive(HbmPotion.stability))
            return;
		
		if(!(entity instanceof EntityPlayer && ArmorUtil.checkForDigamma((EntityPlayer) entity)))
			HbmLivingProps.incrementDigamma(entity, f);
	}
		
	public static void applyDigammaDirect(Entity e, double f) {

		if(!(e instanceof EntityLivingBase entity))
			return;

		if(e instanceof IRadiationImmune)
			return;

		if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
			return;

        HbmLivingProps.incrementDigamma(entity, f);
	}

    public static double getDigamma(Entity e) {
        if (!(e instanceof EntityLivingBase entity))
            return 0.0D;
        return HbmLivingProps.getDigamma(entity);
    }

	public static void radiate(World world, double x, double y, double z, double range, float rad3d) {
		radiate(world, x, y, z, range, rad3d, 0, 0, 0, 0);
	}

	public static void radiate(World world, double x, double y, double z, double range, float rad3d, float dig3d, float fire3d) {
		radiate(world, x, y, z, range, rad3d, dig3d, fire3d, 0, 0);
	}

	public static void radiate(World world, double x, double y, double z, double range, float rad3d, float dig3d, float fire3d, float blast3d) {
		radiate(world, x, y, z, range, rad3d, dig3d, fire3d, blast3d, range);
	}

	public static void radiate(World world, double x, double y, double z, double range, float rad3d, float dig3d, float fire3d, float blast3d, double blastRange) {
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x-range, y-range, z-range, x+range, y+range, z+range));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for(Entity e : entities) {
			if(isExplosionExempt(e)) continue;

			Vec3 vec = Vec3.createVectorHelper(e.posX - x, (e.posY + e.getEyeHeight()) - y, e.posZ - z);
			double len = vec.length();

			if(len > range) continue;
			vec = vec.normalize();
			double dmgLen = Math.max(len, range * 0.05D);
			
			float res = 0;
			
			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + vec.xCoord * i);
				int iy = (int)Math.floor(y + vec.yCoord * i);
				int iz = (int)Math.floor(z + vec.zCoord * i);
				res += world.getBlockState(pos.setPos(ix, iy, iz)).getBlock().getExplosionResistance(null);
			}
			boolean isLiving = e instanceof EntityLivingBase;
			
			if(res < 1)
				res = 1;
			if(isLiving && rad3d > 0){
				float eRads = rad3d;
				eRads /= (float)(dmgLen * dmgLen * Math.sqrt(res));
				
				contaminate((EntityLivingBase)e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
			}
			if(isLiving && dig3d > 0){
				float eDig = dig3d;
				eDig /= (float)(dmgLen * dmgLen * dmgLen);
				
				contaminate((EntityLivingBase)e, HazardType.DIGAMMA, ContaminationType.DIGAMMA, eDig);
			}
			
			if(fire3d > 0.025) {
				float fireDmg = fire3d;
				fireDmg /= (float)(dmgLen * dmgLen * res * res);
				if(fireDmg > 0.025){
					if(fireDmg > 0.1 && e instanceof EntityPlayer p) {

                        if(p.getHeldItemMainhand().getItem() == ModItems.marshmallow && p.getRNG().nextInt((int)len) == 0) {
							p.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(ModItems.marshmallow_roasted));
						}

						if(p.getHeldItemOffhand().getItem() == ModItems.marshmallow && p.getRNG().nextInt((int)len) == 0) {
							p.setHeldItem(EnumHand.OFF_HAND, new ItemStack(ModItems.marshmallow_roasted));
						}
					}
					e.attackEntityFrom(DamageSource.IN_FIRE, fireDmg);
					e.setFire(5);
				}
			}

			if(len < blastRange && blast3d > 0.025) {
				float blastDmg = blast3d;
				blastDmg /= (float)(dmgLen * dmgLen * res);
				if(blastDmg > 0.025){
					if(rad3d > 0)
						e.attackEntityFrom(ModDamageSource.nuclearBlast, blastDmg);
					else
						e.attackEntityFrom(ModDamageSource.blast, blastDmg);
				}
				e.motionX += vec.xCoord * 0.005D * blastDmg;
				e.motionY += vec.yCoord * 0.005D * blastDmg;
				e.motionZ += vec.zCoord * 0.005D * blastDmg;
			}
		}
	}

	private static boolean isExplosionExempt(Entity e) {

		if (e instanceof EntityOcelot ||
				e instanceof EntityNukeTorex ||
				e instanceof EntityNukeExplosionMK5 ||
				e instanceof EntityMIRV ||
				e instanceof EntityMiniNuke ||
				e instanceof EntityMiniMIRV ||
				e instanceof EntityGrenadeASchrab ||
				e instanceof EntityGrenadeNuclear ||
				e instanceof EntityExplosiveBeam ||
				e instanceof EntityBulletBase ||
				(e instanceof EntityPlayer &&
				ArmorUtil.checkArmor((EntityPlayer) e, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots))) {
			return true;
		}

        return e instanceof EntityPlayer && (((EntityPlayer) e).isCreative() || ((EntityPlayer) e).isSpectator());
    }

	
	public enum HazardType {
		MONOXIDE,
		RADIATION,
		NEUTRON,
		DIGAMMA
	}
	
	public enum ContaminationType {
		GAS,				//filterable by gas mask
		GAS_NON_REACTIVE,	//not filterable by gas mask
		GOGGLES,			//preventable by goggles
		FARADAY,			//preventable by metal armor
		HAZMAT,				//preventable by hazmat
		HAZMAT2,			//preventable by heavy hazmat
		DIGAMMA,			//preventable by fau armor or stability
		DIGAMMA2,			//preventable by robes
		CREATIVE,			//preventable by creative mode, for rad calculation armor piece bonuses still apply
		RAD_BYPASS,			//same as creaative but fill not apply radiation resistance calculation
		NONE				//not preventable
	}
	
	/*
	 * This system is nice but the cont types are a bit confusing. Cont types should have much better names and multiple cont types should be applicable.
	 */
	@SuppressWarnings("incomplete-switch") //just shut up
    public static boolean contaminate(EntityLivingBase entity, HazardType hazard, ContaminationType cont, double amount) {

        if (hazard == HazardType.RADIATION) {
            double radEnv = HbmLivingProps.getRadEnv(entity);
            HbmLivingProps.setRadEnv(entity, radEnv + amount);
        }
		
		if(entity instanceof EntityPlayer player) {
			if (player.isSpectator()) return false;
            switch(cont) {
			case GOGGLES:			if(ArmorUtil.checkForGoggles(player))	return false; break;
			case FARADAY:			if(ArmorUtil.checkForFaraday(player))	return false; break;
			case HAZMAT:			if(ArmorUtil.checkForHazmat(player))	return false; break;
			case HAZMAT2:			if(ArmorUtil.checkForHaz2(player))		return false; break;
			case DIGAMMA:			if(ArmorUtil.checkForDigamma(player))	return false; break;
			case DIGAMMA2: break;
			}
			
			if(player.capabilities.isCreativeMode && cont != ContaminationType.NONE){
				if(hazard == HazardType.NEUTRON)
					HbmLivingProps.setNeutron(entity, amount);
				return false;
			}
			
			if(player.ticksExisted < 200)
				return false;
		}
		
		if((hazard == HazardType.RADIATION || hazard == HazardType.NEUTRON) && isRadImmune(entity)){
			return false;
		}

        switch (hazard) {
            case MONOXIDE -> entity.attackEntityFrom(ModDamageSource.monoxide, (float) amount);
            case RADIATION ->
                    HbmLivingProps.incrementRadiation(entity, amount * (cont == ContaminationType.RAD_BYPASS ? 1D : calculateRadiationMod(entity)));
            case NEUTRON -> {
                HbmLivingProps.incrementRadiation(entity, amount * (cont == ContaminationType.RAD_BYPASS ? 1D : calculateRadiationMod(entity)));
                HbmLivingProps.setNeutron(entity, amount);
            }
            case DIGAMMA -> applyDigammaData(entity, amount);
        }
		
		return true;
	}
}
