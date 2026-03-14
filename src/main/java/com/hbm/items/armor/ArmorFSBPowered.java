package com.hbm.items.armor;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.blocks.machine.ItemSelfcharger;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.lib.Library;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ArmorFSBPowered extends ArmorFSB implements IBatteryItem {

	public long maxPower;
	public long chargeRate;
	public long consumption;
	public long drain;

	public ArmorFSBPowered(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, long maxPower, long chargeRate, long consumption, long drain, String s) {
		super(material, layer, slot, texture, s);
		this.maxPower = maxPower;
		this.chargeRate = chargeRate;
		this.consumption = consumption;
		this.drain = drain;
		this.setMaxDamage(1);
	}

	public static String getColor(long a, long b){
        return Library.getColor(a, b);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add("Charge: " + BobMathUtil.getShortNumber(getCharge(stack)) + " / " + BobMathUtil.getShortNumber(getMaxCharge(stack)));
    	super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
	public boolean isArmorEnabled(ItemStack stack) {
		return getCharge(stack) > 0;
	}
    
	@Override
    public void chargeBattery(ItemStack stack, long i) {
    	if(stack.getItem() instanceof ArmorFSBPowered) {
    		if(stack.hasTagCompound()) {
    			stack.getTagCompound().setLong("charge", Math.min(getMaxCharge(stack), Math.max(0, stack.getTagCompound().getLong("charge") + i)));
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", Math.min(getMaxCharge(stack), Math.max(0, i)));
    		}
    	}
    }

	@Override
    public void setCharge(ItemStack stack, long i) {
    	if(stack.getItem() instanceof ArmorFSBPowered) {
    		if(stack.hasTagCompound()) {
    			stack.getTagCompound().setLong("charge", i);
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", i);
    		}
    	}
    }

	@Override
    public void dischargeBattery(ItemStack stack, long i) {
    	if(stack.getItem() instanceof ArmorFSBPowered) {
    		if(stack.hasTagCompound()) {
    			stack.getTagCompound().setLong("charge", Math.min(getMaxCharge(stack), Math.max(0, stack.getTagCompound().getLong("charge") - i)));
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", Math.min(getMaxCharge(stack), Math.max(0, getMaxCharge(stack) - i)));
    		}
    	}
    }

    private ItemSelfcharger getHeldSCBattery(EntityLivingBase entity){
    	if(entity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemSelfcharger){
    		return (ItemSelfcharger) entity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem();
    	}
    	if(entity.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem() instanceof ItemSelfcharger){
    		return (ItemSelfcharger) entity.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem();
    	}
    	return null;
    }

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    	if(!player.capabilities.isCreativeMode && this.drain > 0 && ArmorFSB.hasFSBArmor(player)) {
    		long netto_drain = drain;
    		ItemSelfcharger sc_battery = this.getHeldSCBattery(player);
    		if(sc_battery != null){
    			netto_drain = netto_drain - (sc_battery.getDischargeRate(stack)/4L);
    		}
    		this.dischargeBattery(stack, netto_drain);
    	}
    }
	
	@Override
    public long getCharge(ItemStack stack) {
    	if(stack.getItem() instanceof ArmorFSBPowered) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setLong("charge", ((ArmorFSBPowered) stack.getItem()).getMaxCharge(stack));
            }
            return stack.getTagCompound().getLong("charge");
        }

    	return 0;
    }

	@Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getCharge(stack) < getMaxCharge(stack);
    }

	@Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - (double)getCharge(stack) / (double) getMaxCharge(stack);
    }

	@Override
    public long getMaxCharge(ItemStack stack) {
		if(ArmorModHandler.hasMods(stack)) {
			ItemStack mod = ArmorModHandler.pryMod(stack, ArmorModHandler.battery);
			if(mod != null && mod.getItem() instanceof ItemModBattery) {
				return (long) (maxPower * ((ItemModBattery) mod.getItem()).mod);
			}
		}
		return maxPower;
    }

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return 0;
	}

	@Override
    public void setDamage(ItemStack stack, int damage)
    {
        this.dischargeBattery(stack, damage * consumption);
    }

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}
}