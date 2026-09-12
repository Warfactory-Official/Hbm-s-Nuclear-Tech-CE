package com.hbm.entity.missile;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.Vec3NT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

@AutoRegister(name = "entity_rocket_lambda", trackingRange = 1000)
public class EntityRocketLambda extends EntityRocketBase {

	public EntityRocketLambda(World world) {
		super(world, 1);
		this.setSize(2.0F, 20.0F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(world.isRemote) {
			spawnExhaust(posX, posY + 1, posZ);
		}
	}

	private void spawnExhaust(double x, double y, double z) {

		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("count", 1);
		data.setDouble("width", 0);

		MainRegistry.proxy.effectNT(HbmEffectNT.Exhaust_Lambda, x, y, z, data);

		if(this.ticksExisted % 10 == 0) {

			Vec3NT vec = new Vec3NT(1, 0, 0);
			vec.rotateAroundYDeg(45 * this.rand.nextInt(8));
			double j = 0.5;

			NBTTagCompound contrail = new NBTTagCompound();
			contrail.setFloat("scale", 1F);
			contrail.setDouble("moX", vec.x);
			contrail.setDouble("moY", this.motionY - 0.5);
			contrail.setDouble("moZ", vec.z);
			contrail.setInteger("maxAge", 60 + rand.nextInt(20));

			MainRegistry.proxy.effectNT(HbmEffectNT.MissileContrail, posX - vec.x * j, posY - vec.y * j, posZ - vec.z * j, contrail);
		}
	}
}
