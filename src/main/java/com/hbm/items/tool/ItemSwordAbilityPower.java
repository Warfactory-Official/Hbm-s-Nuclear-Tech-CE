package com.hbm.items.tool;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.lib.Library;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemSwordAbilityPower extends ItemSwordAbility implements IBatteryItem {

	public long maxPower = 1;
	public long chargeRate;
	public long consumption;
	
	public ItemSwordAbilityPower(float damage, double attackSpeed, double movement, ToolMaterial material, long maxPower, long chargeRate, long consumption, String s) {
		super(damage, attackSpeed, movement, material, s);
		this.maxPower = maxPower;
		this.chargeRate = chargeRate;
		this.consumption = consumption;
		this.setMaxDamage(1);
	}

    public ItemSwordAbilityPower(float damage, double movement, ToolMaterial material, long maxPower, long chargeRate, long consumption, String s) {
        this(damage, -2.4, movement, material, maxPower, chargeRate, consumption, s);
    }
	
	@Override
	public void chargeBattery(ItemStack stack, long i) {
    	if(stack.getItem() instanceof ItemSwordAbilityPower) {
    		if(stack.hasTagCompound()) {
    			stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") + i);
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", i);
    		}
    	}
    }

	@Override
    public void setCharge(ItemStack stack, long i) {
    	if(stack.getItem() instanceof ItemSwordAbilityPower) {
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
    	if(stack.getItem() instanceof ItemSwordAbilityPower) {
    		if(stack.hasTagCompound()) {
    			stack.getTagCompound().setLong("charge", stack.getTagCompound().getLong("charge") - i);
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", this.maxPower - i);
    		}

    		if(stack.getTagCompound().getLong("charge") < 0)
    			stack.getTagCompound().setLong("charge", 0);
    	}
    }

	@Override
    public long getCharge(ItemStack stack) {
    	if(stack.getItem() instanceof ItemSwordAbilityPower) {
    		if(stack.hasTagCompound()) {
    			return stack.getTagCompound().getLong("charge");
    		} else {
    			stack.setTagCompound(new NBTTagCompound());
    			stack.getTagCompound().setLong("charge", ((ItemSwordAbilityPower)stack.getItem()).maxPower);
    			return stack.getTagCompound().getLong("charge");
    		}
    	}

    	return 0;
    }

	@Override
	public long getDischargeRate(ItemStack stack) {
		return 0;
	}

    public static String getColor(long a, long b){
        float fraction = 100F * a/b;
        if(fraction > 75)
            return "§a";
        if(fraction > 25)
            return "§e";
        return "§c";
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        long power = getCharge(stack);
        list.add("Charge: " + getColor(power, maxPower) + Library.getShortNumber(power) + " §2/ " + Library.getShortNumber(maxPower));
        super.addInformation(stack, worldIn, list, flagIn);
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
    	return getCharge(stack) < maxPower;
    }
    
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
    	return 1 - (double)getCharge(stack) / (double)maxPower;
    }
    
    @Override
    protected boolean canOperate(ItemStack stack) {
    	return getCharge(stack) >= this.consumption;
    }
    
    @Override
    public long getMaxCharge(ItemStack stack) {
    	return maxPower;
    }

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

    @Override
    public void setDamage(ItemStack stack, int damage) {
    	this.dischargeBattery(stack, damage * consumption);
    }
    
    @Override
    public boolean isDamageable() {
    	return true;
    }
}
