package com.hbm.entity.logic;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "entity_orbital_laser", trackingRange = 1000)
public class EntityOrbitalLaser extends Entity implements IConstantRenderer {

	public static final int maxAge = 5;

	public EntityOrbitalLaser(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() { }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) { }

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) { }

	@Override
	public void onUpdate() {
		if(this.ticksExisted >= maxAge && !world.isRemote) this.setDead();
	}

	public void explode() {

		ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, 5F);
		vnt.setBlockAllocator(new BlockAllocatorStandard());
		vnt.setBlockProcessor(new BlockProcessorStandard());
		vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 1_000F).setupPiercing(50F, 0.5F).setDamageClass(DamageClass.LASER));
		vnt.setPlayerProcessor(new PlayerProcessorStandard());
		vnt.setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F));
		vnt.explode();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		return 15728880;
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
	}
}
