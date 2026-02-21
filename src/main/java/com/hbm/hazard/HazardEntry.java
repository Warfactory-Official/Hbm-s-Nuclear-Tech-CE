package com.hbm.hazard;

import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.hazard.type.IHazardType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HazardEntry implements Cloneable {

    //mlbv: fields made public for the convenience of modders
	public final IHazardType type;
    public final double baseLevel;
	
	/**
	 * Modifiers are evaluated in the order they're being applied to the entry.
     * Shared across all cloned instances, be careful.
	 */
	public List<IHazardModifier> mods = new ArrayList<>();
	
	public HazardEntry(final IHazardType type) {
        this(type, 1D);
	}

    public HazardEntry(final IHazardType type, final double level) {
		this.type = type;
		this.baseLevel = level;
	}
	
	public HazardEntry addMod(final IHazardModifier mod) {
		this.mods.add(mod);
		return this;
	}
	
	public void applyHazard(final ItemStack stack, final EntityLivingBase entity) {
		type.onUpdate(entity, IHazardModifier.evalAllModifiers(stack, entity, baseLevel, mods), stack);
	}

    @Override
    public HazardEntry clone() {
        try {
            return (HazardEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public HazardEntry clone(final double mult) {
		final HazardEntry clone = new HazardEntry(type, baseLevel * mult);
		clone.mods = this.mods;
		return clone;
	}
}
