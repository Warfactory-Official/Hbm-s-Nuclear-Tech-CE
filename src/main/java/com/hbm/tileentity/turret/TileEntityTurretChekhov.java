package com.hbm.tileentity.turret;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretChekhov;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory50;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretChekhov extends TileEntityTurretBaseNT implements IGUIProvider {

	static List<Integer> configs = new ArrayList<>();

	//because cramming it into the ArrayList's constructor with nested curly brackets and all that turned out to be not as pretty
	//also having a floaty `static` like this looks fun
	//idk if it's just me though
	static {
		configs.add(XFactory50.bmg50_sp.id);
		configs.add(XFactory50.bmg50_fmj.id);
		configs.add(XFactory50.bmg50_jhp.id);
		configs.add(XFactory50.bmg50_ap.id);
		configs.add(XFactory50.bmg50_du.id);
	}

	@Override
	public long getMaxPower(){
		return 10000;
	}

	@Override
	protected List<Integer> getAmmoList(){
		return configs;
	}

	@Override
	public String getDefaultName(){
		return "container.turretChekhov";
	}
	
	@Override
	public double getBarrelLength() {
		return 3.5D;
	}
	
	@Override
	public double getTurretElevation(){
		return 45D;
	}
	
	@Override
	public double getAcceptableInaccuracy() {
		return 15;
	}
	
	int timer;
	
	@Override
	public void updateFiringTick(){
		timer++;
		
		if(timer > 20 && timer % getDelay() == 0) {

			BulletConfig conf = this.getFirstConfigLoaded();

			if(conf != null) {
				this.cachedCasingConfig = conf.casing;
				this.spawnBullet(conf, 10F);
				this.consumeAmmo(conf.ammo);
				this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.chekhov_fire, SoundCategory.BLOCKS, 2.0F, 1.0F);
				
				Vec3 pos = new Vec3(this.getTurretPos());
				Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
				vec.rotateAroundZ((float) -this.rotationPitch);
				vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
				
				NBTTagCompound data = new NBTTagCompound();
				data.setFloat("size", 1.5F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_LargeExplode, data, pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord), new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
			}
		}
	}
	
	public int getDelay() {
		return 2;
	}
	
	public float spin;
	public float lastSpin;
	private float accel;
	private boolean manual;
	
	@Override
	public void update(){
		super.update();
		if(world.isRemote) {
			
			if(this.tPos != null || manual) {
				this.accel = Math.min(45F, this.accel += 2);
			} else {
				this.accel = Math.max(0F, this.accel -= 2);
			}
			
			manual = false;
			
			this.lastSpin = this.spin;
			this.spin += this.accel;
			
			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		} else {
			
			if(this.tPos == null && !manual) {
				
				this.timer--;
				
				if(timer > 20)
					timer = 20;
				
				if(timer < 0)
					timer = 0;
			}
		}
	}
	
	@Override
	public void manualSetup(){
		manual = true;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretChekhov(player.inventory, this);
	}

}
