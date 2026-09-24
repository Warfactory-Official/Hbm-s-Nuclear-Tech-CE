package com.hbm.tileentity.turret;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretRichard;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Vec3NT;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretRichard extends TileEntityTurretBaseNT implements IGUIProvider {

	static List<Integer> configs = new ArrayList<>();

	static {
		for(BulletConfig cfg : XFactoryRocket.rocket_ml) configs.add(cfg.id);
	}

	@Override
	protected List<Integer> getAmmoList(){
		return configs;
	}

	@Override
	public String getDefaultName(){
		return "container.turretRichard";
	}

	@Override
	public double getTurretDepression(){
		return 25D;
	}

	@Override
	public double getTurretElevation(){
		return 25D;
	}

	@Override
	public double getBarrelLength(){
		return 1.25D;
	}

	@Override
	public long getMaxPower(){
		return 10000;
	}

	@Override
	public double getDecetorGrace(){
		return 8D;
	}

	@Override
	public double getDecetorRange(){
		return 64D;
	}

	int timer;
	public int loaded;
	int reload;

	@Override
	public void update(){
		super.update();
		
		if(!world.isRemote) {
			
			if(reload > 0) {
				reload--;
				
				if(reload == 0)
					this.loaded = 17;
			}
			
			if(loaded <= 0 && reload <= 0 && this.getFirstConfigLoaded() != null) {
				reload = 100;
			}
			
			if(this.getFirstConfigLoaded() == null) {
				this.loaded = 0;
			}

			networkPackNT(250);
		}
	}

	@Override
	public void updateFiringTick(){
		if(reload > 0)
			return;
		
		timer++;
		
		if(timer > 0 && timer % 10 == 0) {

			BulletConfig conf = this.getFirstConfigLoaded();
			
			if(conf != null) {
				this.spawnBullet(conf, 30F);
				this.consumeAmmo(conf.ammo);
				this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.richard_fire, SoundCategory.BLOCKS, 2.0F, 1.0F);
				this.loaded--;
				
			} else {
				this.loaded = 0;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.loaded);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.loaded = buf.readInt();
	}

	@Override
	public void spawnBullet(BulletConfig bullet, float baseDamage){
		Vec3d pos = this.getTurretPos();
		Vec3NT vec = new Vec3NT(this.getBarrelLength(), 0, 0);
		vec.rotatePitchSelf((float) -this.rotationPitch);
		vec.rotateYawSelf((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(world, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) rotationPitch);
		proj.setPositionAndRotation(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z, proj.rotationYaw, proj.rotationPitch);

		proj.lockonTarget = this.target;
		world.spawnEntity(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		this.loaded = nbt.getInteger("loaded");
		super.readFromNBT(nbt);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt){
		nbt.setInteger("loaded", this.loaded);
		return super.writeToNBT(nbt);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretRichard(player.inventory, this);
	}

}
