package com.hbm.entity.item;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_item_waste", trackingRange = 64)
public class EntityItemWaste extends EntityItem {

	public EntityItemWaste(World world) {
		super(world);
	}

	public EntityItemWaste(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityItemWaste(World world, double x, double y, double z, ItemStack stack) {
		super(world, x, y, z, stack);
	}

	@Override
	public boolean isEntityInvulnerable(@NotNull DamageSource source) {
		return true;
	}

	@Override
	public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
		return false;
	}
}
