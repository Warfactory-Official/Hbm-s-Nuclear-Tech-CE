package com.hbm.entity.projectile;

import com.hbm.util.Vec3NT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBeamBase extends Entity {

	public static final DataParameter<String> PLAYER_NAME = EntityDataManager.createKey(EntityBeamBase.class, DataSerializers.STRING);
	
	public EntityBeamBase(World worldIn) {
		super(worldIn);
		this.ignoreFrustumCheck = true;
	}

	public EntityBeamBase(World world, EntityPlayer player) {
		super(world);

		this.ignoreFrustumCheck = true;
		this.getDataManager().set(PLAYER_NAME, player.getDisplayName().getUnformattedText());

		Vec3NT vec = new Vec3NT(player.getLookVec());
		vec.rotateYawSelf(-90F);
		float l = 0.075F;
		vec.setX(vec.x * (l));
		vec.setY(vec.y * (l));
		vec.setZ(vec.z * (l));

		Vec3NT vec0 = new Vec3NT(player.getLookVec());
		float d = 0.1F;
		vec0.setX(vec0.x * (d));
		vec0.setY(vec0.y * (d));
		vec0.setZ(vec0.z * (d));

		this.setPosition(player.posX + vec.x + vec0.x, player.posY + player.getEyeHeight() + vec0.y, player.posZ + vec.z + vec0.z);
	}
	
	@Override
	protected void entityInit() {
		this.getDataManager().register(PLAYER_NAME, "");
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
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

}
