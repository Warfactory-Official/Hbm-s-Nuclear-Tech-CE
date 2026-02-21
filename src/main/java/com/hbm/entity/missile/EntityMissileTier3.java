package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.explosion.ExplosionThermo;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.util.MutableVec3d;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityMissileTier3 extends EntityMissileBaseNT {

	public EntityMissileTier3(World world) { super(world); }
	public EntityMissileTier3(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<ItemStack>();

		list.add(new ItemStack(ModItems.plate_steel, 16));
		list.add(new ItemStack(ModItems.plate_titanium, 10));
		list.add(new ItemStack(ModItems.thruster_large, 1));
		
		return list;
	}

	@Override
	public String getTranslationKey() {
		return "radar.target.tier3";
	}

	@Override
	public int getBlipLevel() {
		return IRadarDetectableNT.TIER3;
	}

	@Override
	protected void spawnContrail() {
		
		MutableVec3d thrust = new MutableVec3d(0, 0, 0.5);
		thrust.rotateYawSelf((this.rotationYaw + 90) * (float) Math.PI / 180F);
		thrust.rotatePitchSelf(this.rotationPitch * (float) Math.PI / 180F);
		thrust.rotateYawSelf(-(this.rotationYaw + 90) * (float) Math.PI / 180F);

		this.spawnControlWithOffset(thrust.x, thrust.y, thrust.z);
		this.spawnControlWithOffset(-thrust.z, thrust.y, thrust.x);
		this.spawnControlWithOffset(-thrust.x, -thrust.z, -thrust.z);
		this.spawnControlWithOffset(thrust.z, -thrust.z, -thrust.x);
	}
	@AutoRegister(name = "entity_missile_burst", trackingRange = 1000)
	public static class EntityMissileBurst extends EntityMissileTier3 {
		public EntityMissileBurst(World world) { super(world); }
		public EntityMissileBurst(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop)  {
			this.explodeStandard(50F, 48, false);
			ExplosionCreator.composeEffectLarge(world, posX, posY, posZ);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_generic_large); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_burst); }
	}
	@AutoRegister(name = "entity_missile_inferno", trackingRange = 1000)
	public static class EntityMissileInferno extends EntityMissileTier3 {
		public EntityMissileInferno(World world) { super(world); }
		public EntityMissileInferno(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.explodeStandard(50F, 48, true);
			ExplosionCreator.composeEffectLarge(world, posX, posY, posZ);
			ExplosionChaos.burn(this.world, thrower, getPosition(), 10);
			ExplosionChaos.flameDeath(this.world, thrower, getPosition(), 25);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_incendiary_large); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_inferno); }
	}
	@AutoRegister(name = "entity_missile_rain", trackingRange = 1000)
	public static class EntityMissileRain extends EntityMissileTier3 {
		public EntityMissileRain(World world) { super(world); }
		public EntityMissileRain(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); this.isCluster = true; }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.world.createExplosion(this, this.posX, this.posY, this.posZ, 25F, true);
			ExplosionChaos.cluster(this.world, (int)this.posX, (int)this.posY, (int)this.posZ, 100, 100);
		}
		@Override public void cluster() { this.onMissileImpact(null); }
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_cluster_large); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_rain); }
	}
	@AutoRegister(name = "entity_missile_drill", trackingRange = 1000)
	public static class EntityMissileDrill extends EntityMissileTier3 {
		public EntityMissileDrill(World world) { super(world); }
		public EntityMissileDrill(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			for(int i = 0; i < 30; i++) {
				ExplosionNT explosion = new ExplosionNT(world, this, this.posX, this.posY - i, this.posZ, 10F);
				explosion.addAttrib(ExAttrib.ERRODE);
				explosion.explode(); //an explosion exploded!
			}
			ExplosionLarge.spawnParticles(world, this.posX, this.posY, this.posZ, 25);
			ExplosionLarge.spawnShrapnels(world, this.posX, this.posY, this.posZ, 12);
			ExplosionLarge.jolt(world, thrower, this.posX, this.posY, this.posZ, 10, 50, 1);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.warhead_buster_large); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_drill); }
	}

	//mlbv: missiles below this comment does not exist in upstream.

	@AutoRegister(name = "entity_missile_endo", trackingRange = 1000)
	public static class EntityMissileEndo extends EntityMissileTier3{
		public EntityMissileEndo(World world) { super(world); }
		public EntityMissileEndo(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override
		public void onMissileImpact(RayTraceResult mop) {
			ExplosionThermo.freeze(this.world, null, (int)this.posX, (int)this.posY, (int)this.posZ, 30);
			ExplosionThermo.freezer(this.world, (int)this.posX, (int)this.posY, (int)this.posZ, 40);
		}
		@Override
		public ItemStack getDebrisRareDrop() {
			return new ItemStack(ModItems.warhead_thermo_exo);
		}

		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_endo); }
	}
	@AutoRegister(name = "entity_missile_exo", trackingRange = 1000)
	public static class EntityMissileExo extends EntityMissileTier3{
		public EntityMissileExo(World world) { super(world); }
		public EntityMissileExo(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override
		public void onMissileImpact(RayTraceResult mop) {
			ExplosionThermo.scorch(this.world, null, (int)this.posX, (int)this.posY, (int)this.posZ, 30);
			ExplosionThermo.setEntitiesOnFire(this.world, (int)this.posX, (int)this.posY, (int)this.posZ, 40);
		}
		@Override
		public ItemStack getDebrisRareDrop() {
			return new ItemStack(ModItems.warhead_thermo_exo);
		}

		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_exo); }
	}
}
