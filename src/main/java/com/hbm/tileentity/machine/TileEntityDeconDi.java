package com.hbm.tileentity.machine;

import com.hbm.capability.HbmLivingCapability.EntityHbmPropsProvider;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityDeconDi extends TileEntity implements ITickable {

	private static float digammaRemove;
	public TileEntityDeconDi(float dig) {
		super();
		digammaRemove = dig;
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5));

			if(!entities.isEmpty()) {
				for(Entity e : entities) {
					if(e.hasCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null)){
						if(digammaRemove > 0.0F){
							e.getCapability(EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).decreaseDigamma(digammaRemove);
						}
					}
				}
			}
		} else {

			Random rand = world.rand;

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("mX", 0.0);
			nbt.setDouble("mY", 0.04);
			nbt.setDouble("mZ", 0.0);
			MainRegistry.proxy.effectNT(HbmEffectNT.VanillaExt_TownAura, pos.getX() + 0.125 + rand.nextDouble() * 0.75, pos.getY() + 1.1, pos.getZ() + 0.125 + rand.nextDouble() * 0.75, nbt);
		}
	}
}
