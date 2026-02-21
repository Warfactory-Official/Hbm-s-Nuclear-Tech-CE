package com.hbm.entity.projectile;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_rbmk_debris", trackingRange = 1000)
public class EntityRBMKDebris extends Entity {

	public static final DataParameter<Integer> TYPE_ID = EntityDataManager.createKey(EntityRBMKDebris.class, DataSerializers.VARINT);
	
	public float rot;
	public float lastRot;
	private boolean hasSizeSet = false;

	public EntityRBMKDebris(World world){
		super(world);
	}

	public EntityRBMKDebris(World world, double x, double y, double z, DebrisType type){
		super(world);
		this.setPosition(x, y, z);
		this.setType(type);
	}

	@Override
	protected void entityInit(){
		this.getDataManager().register(TYPE_ID, 0);
		this.rot = this.lastRot = this.rand.nextFloat() * 360;
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand){
		if(!world.isRemote && !isDead) {
			switch(this.getType()){
			case BLANK:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal)))
					this.setDead();
				break;
			case ELEMENT:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal)))
					this.setDead();
				break;
			case FUEL:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_fuel)))
					this.setDead();
				break;
			case GRAPHITE:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_graphite)))
					this.setDead();
				break;
			case LID:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.rbmk_lid)))
					this.setDead();
				break;
			case ROD:
				if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal)))
					this.setDead();
				break;
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		return false;
	}
	
	@Override
	public void onUpdate(){

		if(!hasSizeSet) {

			switch(this.getType()){
			case BLANK:
				this.setSize(0.5F, 0.5F);
				break;
			case ELEMENT:
				this.setSize(1F, 1F);
				break;
			case FUEL:
				this.setSize(0.25F, 0.25F);
				break;
			case GRAPHITE:
				this.setSize(0.25F, 0.25F);
				break;
			case LID:
				this.setSize(1F, 0.5F);
				break;
			case ROD:
				this.setSize(0.75F, 0.5F);
				break;
			}

			hasSizeSet = true;
		}

		if(!world.isRemote) {
			if(motionY > 0) {

				Vec3d pos = new Vec3d(posX, posY, posZ);
				Vec3d next = new Vec3d(posX + motionX * 2, posY + motionY * 2, posZ + motionZ * 2);
				RayTraceResult mop = world.rayTraceBlocks(pos, next, false, false, false);

				if(mop != null && mop.typeOfHit == Type.BLOCK) {

					int x = mop.getBlockPos().getX();
					int y = mop.getBlockPos().getY();
					int z = mop.getBlockPos().getZ();

					for(int i = -1; i <= 1; i++) {
						for(int j = -1; j <= 1; j++) {
							for(int k = -1; k <= 1; k++) {

								int rn = Math.abs(i) + Math.abs(j) + Math.abs(k);

								if(rn <= 1 || rand.nextInt(rn) == 0)
									world.setBlockToAir(new BlockPos(x + i, y + j, z + k));
							}
						}
					}

					this.setDead();
				}
			}

			if(this.getType() == DebrisType.FUEL) {
				ContaminationUtil.radiate(world, this.posX, this.posY, this.posZ, 16, 100);
			}

			if(!RBMKDials.getPermaScrap(world) && this.ticksExisted > getLifetime() + this.getEntityId() % 50){
				this.setDead();
			}
		}
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionY -= 0.04D;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		this.lastRot = this.rot;

		if(this.onGround) {
			this.motionX *= 0.85D;
			this.motionZ *= 0.85D;
			this.motionY *= -0.5D;

		} else {

			this.rot += 10F;

			if(rot >= 360F) {
				this.rot -= 360F;
				this.lastRot -= 360F;
			}
		}
	}

	private int getLifetime(){

		switch(this.getType()){
		case BLANK:
			return 30 * 60 * 20;
		case ELEMENT:
			return 30 * 60 * 20;
		case FUEL:
			return 100 * 60 * 20;
		case GRAPHITE:
			return 150 * 60 * 20;
		case LID:
			return 300 * 20;
		case ROD:
			return 600 * 20;
		default:
			return 0;
		}
	}

	public void setType(DebrisType type){
		this.getDataManager().set(TYPE_ID, type.ordinal());
	}

	public DebrisType getType(){
		return DebrisType.VALUES[Math.abs(this.getDataManager().get(TYPE_ID)) % DebrisType.VALUES.length];
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt){
		this.getDataManager().set(TYPE_ID, nbt.getInteger("debtype"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt){
		nbt.setInteger("debtype", this.getDataManager().get(TYPE_ID));
	}

	@Override
	public void move(MoverType type, double moX, double moY, double moZ){
		this.world.profiler.startSection("move");

		if(this.isInWeb) {
			this.isInWeb = false;
		}

		double initMoX = moX;
		double initMoY = moY;
		double initMoZ = moZ;

		List<AxisAlignedBB> list = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(moX, moY, moZ));

		for(int i = 0; i < list.size(); ++i) {
			moY = list.get(i).calculateYOffset(this.getEntityBoundingBox(), moY);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, moY, 0.0D));

		if(initMoY != moY) {
			moZ = 0.0D;
			moY = 0.0D;
			moX = 0.0D;
		}

		boolean isGoingDown = this.onGround || initMoY != moY && initMoY < 0.0D;
		int j;

		for(j = 0; j < list.size(); ++j) {
			moX = ((AxisAlignedBB)list.get(j)).calculateXOffset(this.getEntityBoundingBox(), moX);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(moX, 0.0D, 0.0D));

		for(j = 0; j < list.size(); ++j) {
			moZ = ((AxisAlignedBB)list.get(j)).calculateZOffset(this.getEntityBoundingBox(), moZ);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, moZ));

		double d10;
		double d11;
		int k;
		double d12;

		if(this.stepHeight > 0.0F && isGoingDown && (initMoX != moX || initMoZ != moZ)) {
			d12 = moX;
			d10 = moY;
			d11 = moZ;
			moX = initMoX;
			moY = (double)this.stepHeight;
			moZ = initMoZ;
			AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
			list = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(initMoX, moY, initMoZ));

			for(k = 0; k < list.size(); ++k) {
				moY = ((AxisAlignedBB)list.get(k)).calculateYOffset(this.getEntityBoundingBox(), moY);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, moY, 0.0D));

			for(k = 0; k < list.size(); ++k) {
				moX = ((AxisAlignedBB)list.get(k)).calculateXOffset(this.getEntityBoundingBox(), moX);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(moX, 0.0D, 0.0D));

			for(k = 0; k < list.size(); ++k) {
				moZ = ((AxisAlignedBB)list.get(k)).calculateZOffset(this.getEntityBoundingBox(), moZ);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, moZ));

			moY = (double)(-this.stepHeight);

			for(k = 0; k < list.size(); ++k) {
				moY = ((AxisAlignedBB)list.get(k)).calculateYOffset(this.getEntityBoundingBox(), moY);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, moY, 0.0D));

			if(d12 * d12 + d11 * d11 >= moX * moX + moZ * moZ) {
				moX = d12;
				moY = d10;
				moZ = d11;
				this.setEntityBoundingBox(axisalignedbb1);
			}
		}

		this.world.profiler.endSection();
		this.world.profiler.startSection("rest");
		this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
		this.posY = this.getEntityBoundingBox().minY + (double)this.getYOffset();
		this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
		this.collidedHorizontally = initMoX != moX || initMoZ != moZ;
		this.collidedVertically = initMoY != moY;
		this.onGround = initMoY != moY && initMoY < 0.0D;
		this.collided = this.collidedHorizontally || this.collidedVertically;
		int j6 = MathHelper.floor(this.posX);
		int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
		int k6 = MathHelper.floor(this.posZ);
		BlockPos blockpos = new BlockPos(j6, i1, k6);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		this.updateFallState(moY, this.onGround, iblockstate, blockpos);

		if(initMoX != moX) {
			//this.motionX = 0.0D;
			this.motionX *= -0.75D;

		}

		if(initMoY != moY) {
			this.motionY = 0.0D;
		}

		if(initMoZ != moZ) {
			//this.motionZ = 0.0D;
			this.motionZ *= -0.75D;
		}

		try {
			this.doBlockCollisions();
		} catch(Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
			this.addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}

		this.world.profiler.endSection();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double dist){

		int range = 128;
		return dist < range * range;
	}

	public static enum DebrisType {
		BLANK, //just a metal beam
		ELEMENT, //the entire casing of a fuel assembly because fuck you
		FUEL, //spicy
		ROD, //solid boron rod
		GRAPHITE, //spicy rock
		LID; //the all destroying harbinger of annihilation

        public static final DebrisType[] VALUES = values();
	}
}