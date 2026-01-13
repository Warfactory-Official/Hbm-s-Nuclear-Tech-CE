package com.hbm.entity.effect;

import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister(name = "entity_black_hole", trackingRange = 1000)
public class EntityBlackHole extends Entity implements IConstantRenderer {
	protected static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.FLOAT);
	protected boolean breaksBlocks = true;

	public EntityBlackHole(World worldIn) {
		super(worldIn);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.noClip = true;
	}

	public EntityBlackHole(World worldIn, float size) {
		this(worldIn);
		this.dataManager.set(SIZE, size);
	}

	public EntityBlackHole noBreak() {
		this.breaksBlocks = false;
		return this;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!CompatibilityConfig.isWarDim(world)) {
			this.setDead();
			return;
		}

		final float size = this.dataManager.get(SIZE);

		if (!world.isRemote && breaksBlocks) {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int k = 0; k < size * 2; k++) {
				double phi = rand.nextDouble() * (Math.PI * 2);
				double costheta = rand.nextDouble() * 2 - 1;
				double theta = Math.acos(costheta);
				double x = Math.sin(theta) * Math.cos(phi);
				double y = Math.sin(theta) * Math.sin(phi);
				double z = Math.cos(theta);

				int length = (int) Math.ceil(size * 15);

				for (int i = 0; i < length; i++) {
					int x0 = (int) (this.posX + (x * i));
					int y0 = (int) (this.posY + (y * i));
					int z0 = (int) (this.posZ + (z * i));

					pos.setPos(x0, y0, z0);
					IBlockState state = world.getBlockState(pos);

					if (state.getMaterial().isLiquid()) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						state = world.getBlockState(pos);
					}

					if (state.getBlock() != Blocks.AIR) {
						EntityRubble rubble = new EntityRubble(world);
						rubble.posX = x0 + 0.5F;
						rubble.posY = y0;
						rubble.posZ = z0 + 0.5F;
						Block b = state.getBlock();
						rubble.setMetaBasedOnBlock(b, b.getMetaFromState(state));
						world.spawnEntity(rubble);
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						break;
					}
				}
			}
		}

		final double range = size * 15;

		List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(
						posX - range, posY - range, posZ - range,
						posX + range, posY + range, posZ + range
				)
		);

		for (Entity e : entities) {

			if (e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)
				continue;

			if (e instanceof EntityFallingBlock && !world.isRemote && e.ticksExisted > 1) {
				double x = e.posX;
				double y = e.posY;
				double z = e.posZ;

				IBlockState st = ((EntityFallingBlock) e).getBlock();
				Block b = st.getBlock();
				int meta = b.getMetaFromState(st);

				e.setDead();

				EntityRubble rubble = new EntityRubble(world);
				rubble.setMetaBasedOnBlock(b, meta);
				rubble.setPositionAndRotation(x, y, z, 0, 0);
				rubble.motionX = e.motionX;
				rubble.motionY = e.motionY;
				rubble.motionZ = e.motionZ;
				world.spawnEntity(rubble);
			}

			Vec3 vec = Vec3.createVectorHelper(posX - e.posX, posY - e.posY, posZ - e.posZ);

			double dist = vec.length();
			if (dist > range)
				continue;

			vec = vec.normalize();

			if (!(e instanceof EntityItem))
				vec.rotateAroundY((float) Math.toRadians(15));

			double speed = 0.1D;
			e.motionX += vec.xCoord * speed;
			e.motionY += vec.yCoord * speed * 2;
			e.motionZ += vec.zCoord * speed;

			if (e instanceof EntityBlackHole)
				continue;

			if (dist < size * 1.5F) {
				e.attackEntityFrom(ModDamageSource.blackhole, 1000.0F);

				if (!(e instanceof EntityLivingBase))
					e.setDead();

				if (!world.isRemote && e instanceof EntityItem) {
					ItemStack stack = ((EntityItem) e).getItem();
					if (stack.getItem() == ModItems.pellet_antimatter || stack.getItem() == ModItems.flame_pony) {
						this.setDead();
						world.createExplosion(null, this.posX, this.posY, this.posZ, 5.0F, true);
						return;
					}
				}
			}
		}

		this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(SIZE, 0.5F);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.dataManager.set(SIZE, compound.getFloat("size"));
		if (compound.hasKey("breaksBlocks")) {
			this.breaksBlocks = compound.getBoolean("breaksBlocks");
		} else {
			this.breaksBlocks = true;
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setFloat("size", this.dataManager.get(SIZE));
		compound.setBoolean("breaksBlocks", this.breaksBlocks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 25000;
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
