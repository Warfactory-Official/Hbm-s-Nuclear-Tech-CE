package com.hbm.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.world.WorldUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityMissileTier0 extends EntityMissileBaseNT {

	public EntityMissileTier0(World world) { super(world); }
	public EntityMissileTier0(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.wire_fine, 4, Mats.MAT_ALUMINIUM.id));
		list.add(new ItemStack(ModItems.plate_titanium, 4));
		list.add(new ItemStack(ModItems.shell, 2, Mats.MAT_ALUMINIUM.id));
		list.add(new ItemStack(ModItems.ducttape, 1));
		return list;
	}

	@Override
	protected float getContrailScale() {
		return 0.5F;
	}

	//mlbv: EntityMissileTest isn't here but since it's not craftable in survival ig there isn't a necessity to have it ported

	@AutoRegister(name = "entity_missile_micro", trackingRange = 1000)
	public static class EntityMissileMicro extends EntityMissileTier0 {
		public EntityMissileMicro(World world) { super(world); }
		public EntityMissileMicro(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			if (!this.world.isRemote) {
				ExplosionNukeSmall.explode(world, posX, posY + 0.5, posZ, ExplosionNukeSmall.PARAMS_HIGH);
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return OreDictManager.DictFrame.fromOne(ModItems.ammo_standard, GunFactory.EnumAmmo.NUKE_HIGH); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_micro); }
	}
	@AutoRegister(name = "entity_missile_schrab", trackingRange = 1000)
	public static class EntityMissileSchrabidium extends EntityMissileTier0 {
		public EntityMissileSchrabidium(World world) { super(world); }
		public EntityMissileSchrabidium(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			if (!this.world.isRemote) {
				EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(world, posX, posY, posZ, BombConfig.aSchrabRadius);
				if (!ex.isDead) {
					WorldUtil.loadAndSpawnEntityInWorld(ex);
					EntityCloudFleija cloud = new EntityCloudFleija(this.world, BombConfig.aSchrabRadius);
					cloud.posX = this.posX;
					cloud.posY = this.posY;
					cloud.posZ = this.posZ;
					this.world.spawnEntity(cloud);
				}
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return null; }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_schrabidium); }
	}
	@AutoRegister(name = "entity_missile_bhole", trackingRange = 1000)
	public static class EntityMissileBHole extends EntityMissileTier0 {
		public EntityMissileBHole(World world) { super(world); }
		public EntityMissileBHole(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.5F, true);
			EntityBlackHole bl = new EntityBlackHole(this.world, 1.5F);
			bl.posX = this.posX;
			bl.posY = this.posY;
			bl.posZ = this.posZ;
			this.world.spawnEntity(bl);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.grenade_black_hole, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_bhole); }
	}
	@AutoRegister(name = "entity_missile_taint", trackingRange = 1000)
	public static class EntityMissileTaint extends EntityMissileTier0 {
		public EntityMissileTaint(World world) { super(world); }
		public EntityMissileTaint(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			this.world.createExplosion(this, this.posX, this.posY, this.posZ, 10.0F, true);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for(int i = 0; i < 100; i++) {
				int a = rand.nextInt(11) + (int) this.posX - 5;
				int b = rand.nextInt(11) + (int) this.posY - 5;
				int c = rand.nextInt(11) + (int) this.posZ - 5;
				pos.setPos(a, b, c);
                IBlockState state = world.getBlockState(pos);
				if(state.isNormalCube() && !state.getBlock().isAir(state, world, pos))
                    world.setBlockState(pos, ModBlocks.taint.getDefaultState(), 2);
			}
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModItems.powder_spark_mix, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_taint); }
	}
	@AutoRegister(name = "entity_missile_emp", trackingRange = 1000)
	public static class EntityMissileEMP extends EntityMissileTier0 {
		public EntityMissileEMP(World world) { super(world); }
		public EntityMissileEMP(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }
		@Override public void onMissileImpact(RayTraceResult mop) {
			ExplosionNukeGeneric.empBlast(world, thrower, (int)posX, (int)posY, (int)posZ, 50);
			EntityEMPBlast wave = new EntityEMPBlast(world, 50);
			wave.posX = posX;
			wave.posY = posY;
			wave.posZ = posZ;
			world.spawnEntity(wave);
		}
		@Override public ItemStack getDebrisRareDrop() { return new ItemStack(ModBlocks.emp_bomb, 1); }
		@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_emp); }
	}
}
