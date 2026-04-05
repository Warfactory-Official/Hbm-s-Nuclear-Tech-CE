package com.hbm.tileentity.deco;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PacketSpecialDeath;
import com.hbm.particle.gluon.ParticleGluonFlare;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityObjTester extends TileEntity implements ITickable {
	private AxisAlignedBB bb;

	public int fireAge = -1;

	@Override
	public void update() {
		RayTraceResult r = Library.rayTraceIncludeEntities(world, new Vec3d(this.pos).add(0, 2, 0.5), new Vec3d(this.pos).add(12, 2, 0.5), null);
		if(world.isRemote) {
			if(world.getTotalWorldTime() %1 == 0){
				if(r != null && r.hitVec != null){
					ParticleGluonFlare flare = new ParticleGluonFlare(world, r.hitVec.x-0.1, r.hitVec.y, r.hitVec.z);
					Minecraft.getMinecraft().effectRenderer.addEffect(flare);
				} else {
					ParticleGluonFlare flare = new ParticleGluonFlare(world, pos.getX() + 10.9, pos.getY() + 2, pos.getZ() + 0.5);
					Minecraft.getMinecraft().effectRenderer.addEffect(flare);
				}
				
			}
			if(fireAge >= 0) {
				fireAge++;
			}
			MainRegistry.proxy.spawnParticle(pos.getX(), pos.getY(), pos.getZ(), "bfg_fire", new float[]{fireAge});
		} else {
			if(r != null && r.typeOfHit == Type.ENTITY && r.entityHit instanceof EntityLivingBase){
				EntityLivingBase ent = ((EntityLivingBase)r.entityHit);
				ent.setHealth(ent.getHealth()-2);
				PacketDispatcher.wrapper.sendToAllTracking(new PacketSpecialDeath(ent, 1), ent);
				//Why doesn't the player count as tracking itself? I don't know.
				if(ent instanceof EntityPlayerMP){
					PacketDispatcher.wrapper.sendTo(new PacketSpecialDeath(ent, 1), (EntityPlayerMP) ent);
				}
				if(ent.getHealth() <= 0){
					PacketDispatcher.wrapper.sendToAllTracking(new PacketSpecialDeath(ent, 0), ent);
					ent.setDead();
					if(ent instanceof EntityPlayerMP){
						PacketDispatcher.wrapper.sendTo(new PacketSpecialDeath(ent, 0), (EntityPlayerMP) ent);
					}
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (bb == null) bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 13, pos.getY() + 4, pos.getZ() + 2);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
