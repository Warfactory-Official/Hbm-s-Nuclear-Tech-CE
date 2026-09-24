package com.hbm.tileentity.machine;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blocks.generic.BlockTallPlant;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister(name = "tileentity_thresher")
public class TileEntityMachineThresher extends TileEntityLoadedBase implements ITickable, IBufPacketReceiver, IFluidStandardReceiverMK2, IFluidCopiable {

	public FluidTankNTM tank;

	public boolean isOn;
	public boolean isSuspended;
	public int delay;

	private int turnProgress;
	public float syncAngle;
	public float angle;
	public float prevAngle;

	// 0: waiting, 1: extending, 2: retracting
	private int state = 0;

	public float spin;
	public float lastSpin;
	private AudioWrapper audio;

	public TileEntityMachineThresher() {
		this.tank = new FluidTankNTM(Fluids.WOODOIL, 100);
	}

	private ForgeDirection getDir() {
		IBlockState state = world.getBlockState(pos);
		return ForgeDirection.getOrientation(state.getBlock().getMetaFromState(state)).getOpposite();
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			ForgeDirection dir = getDir();
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			if(!isSuspended && world.getTotalWorldTime() % 20 == 0) {
				if(tank.getFill() > 0) {
					tank.setFill(tank.getFill() - 1);
					this.isOn = true;
				} else {
					this.isOn = false;
				}

				trySubscribe(tank.getTankType(), world, pos.getX() + rot.offsetX, pos.getY(), pos.getZ() + rot.offsetZ, rot);
				trySubscribe(tank.getTankType(), world, pos.getX() - rot.offsetX, pos.getY(), pos.getZ() - rot.offsetZ, rot.getOpposite());
				trySubscribe(tank.getTankType(), world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);
			}

			if(isOn && !isSuspended) {

				if(this.state == 0) {
					this.delay--;
					if(delay <= 0) this.state = 1;
				}

				if(this.state == 1) {
					this.angle += 82.5F / 60F;

					if(this.angle >= 82.5F) {
						this.angle = 82.5F;
						this.state = 2;
					}
				} else if(this.state == 2) {
					this.angle -= 82.5F / 60F;

					if(this.angle <= 0F) {
						this.angle = 0F;
						this.state = 0;
						this.delay = 200 + world.rand.nextInt(100);
					}
				}

				if(this.angle != 0) {
					double pivotX = pos.getX() + 0.5 - dir.offsetX;
					double pivotZ = pos.getZ() + 0.5 - dir.offsetZ;

					Vec3d upperArm = new Vec3d(-dir.offsetX * 4, 0, -dir.offsetZ * 4);
					Vec3d lowerArm = new Vec3d(-dir.offsetX * 4, 0, -dir.offsetZ * 4);

					if(dir.offsetZ != 0) {
						upperArm = upperArm.rotatePitch((float) Math.toRadians(82.5 - angle));
						lowerArm = lowerArm.rotatePitch((float) -Math.toRadians(82.5 - angle));
					}
					if(dir.offsetX != 0) {
						upperArm = rotateAroundZ(upperArm, (float) Math.toRadians(82.5 - angle));
						lowerArm = rotateAroundZ(lowerArm, (float) -Math.toRadians(82.5 - angle));
					}

					double endX = pivotX + upperArm.x + lowerArm.x + (-dir.offsetX * 2);
					double endZ = pivotZ + upperArm.z + lowerArm.z + (-dir.offsetZ * 2);

					for(int i = -3; i <= 3; i++) {
						BlockPos hit = new BlockPos(MathHelper.floor(endX + rot.offsetX * i), pos.getY(), MathHelper.floor(endZ + rot.offsetZ * i));

						IBlockState state = world.getBlockState(hit);
						Block b = state.getBlock();
						int meta = b.getMetaFromState(state);

						if(state.isNormalCube() && !canCut(b)) {
							this.state = 2;
							break;
						}

						if(b == Blocks.DOUBLE_PLANT) {
							// sunflower
							if((meta & 7) == 0 && world.rand.nextInt(250) == 0) {
								world.playEvent(2001, hit, Block.getStateId(state));
								this.dropItem(new ItemStack(Blocks.DOUBLE_PLANT, 1, 0));
							}
							// tall grass
							if((meta & 7) == 2 && world.rand.nextInt(100) == 0) {
								world.playEvent(2001, hit, Block.getStateId(state));
								this.dropItem(new ItemStack(Items.WHEAT_SEEDS, 1, 0));
							}
							continue;
						}

						// NTM tall plants like hemp
						if(b instanceof BlockTallPlant) {
							this.cutTallPlant(b, meta, hit);
							continue;
						}

						if(b == Blocks.REEDS || b == Blocks.CACTUS) {
							this.cutCane(b, hit);
							continue;
						}

						// IGrowable also covers anything that accepts bone meal,
						// so we have to handle actual crops last
						if(canCut(b) && !shouldIgnore(world, hit, state, b, meta)) this.cutCrop(b, state, hit);
					}

					List<EntityLivingBase> affected = world.getEntitiesWithinAABB(EntityLivingBase.class,
							new AxisAlignedBB(endX, pos.getY() + 0.5, endZ, endX, pos.getY() + 0.5, endZ)
									.grow(Math.abs(dir.offsetX * 0.5) + Math.abs(rot.offsetX * 4.5), 0.5, Math.abs(dir.offsetZ * 0.5) + Math.abs(rot.offsetZ * 4.5)));

					for(EntityLivingBase e : affected) {
						if(e.isEntityAlive() && e.attackEntityFrom(ModDamageSource.turbofan, 100)) {
							if(e instanceof IMob && !e.isEntityAlive()) this.dropItem(new ItemStack(ModItems.nitra_small));
							world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.BLOCKS, 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);
							int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
							NBTTagCompound data = new NBTTagCompound();
							data.setString("type", "vanillaburst");
							data.setInteger("count", count * 4);
							data.setDouble("motion", 0.1D);
							data.setString("mode", "blockdust");
							data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
							PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ),
									new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));
						}
					}
				}
			}

			networkPackNT(100);

		} else {

			this.lastSpin = this.spin;

			if(isOn && !isSuspended) {
				if(this.angle > 0) this.spin += 15F;

				ForgeDirection dir = getDir();
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
						pos.getX() + 0.5 + dir.offsetX * 0.8125 + rot.offsetX * 0.375,
						pos.getY() + 1.5625,
						pos.getZ() + 0.5 + dir.offsetZ * 0.8125 + rot.offsetZ * 0.375, 0, 0, 0);
			}

			if(isOn && !isSuspended && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 15 * 15) {
				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			this.prevAngle = this.angle;

			if(this.turnProgress > 0) {
				double d0 = MathHelper.wrapDegrees(this.syncAngle - (double) this.angle);
				this.angle = (float) ((double) this.angle + d0 / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.angle = this.syncAngle;
			}
		}
	}

	private static Vec3d rotateAroundZ(Vec3d vec, float angle) {
		float cos = MathHelper.cos(angle);
		float sin = MathHelper.sin(angle);
		return new Vec3d(vec.x * cos + vec.y * sin, vec.y * cos - vec.x * sin, vec.z);
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.engine, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F, 1.0F + world.rand.nextFloat() * 0.1F, 10);
	}

	@Override
	public void onChunkUnload() {
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	public static boolean canCut(Block b) {
		if(b instanceof IGrowable) return true;
		if(b == Blocks.NETHER_WART) return true;
		if(b == Blocks.MELON_BLOCK || b == Blocks.PUMPKIN) return true;
		return false;
	}

	public static boolean shouldIgnore(net.minecraft.world.World world, BlockPos pos, IBlockState state, Block b, int meta) {

		if(b instanceof BlockStem) return true;
		if(b == Blocks.NETHER_WART) return meta < 3;

		if(b instanceof IGrowable) {
			return ((IGrowable) b).canGrow(world, pos, state, world.isRemote);
		}

		return false;
	}

	protected void cutTallPlant(Block b, int meta, BlockPos pos) {

		// if we hit the lower block, shift focus one block up
		if(meta <= 7) {
			pos = pos.up();
			IBlockState above = world.getBlockState(pos);
			// if it's a lower block and the block above isn't the same, cancel
			if(above.getBlock() != b) return;
			meta = b.getMetaFromState(above);
		}

		// ignore immature willow
		if(meta == EnumTallFlower.CD2.ordinal() + 8 || meta == EnumTallFlower.CD3.ordinal() + 8) return;

		IBlockState state = world.getBlockState(pos);
		world.playEvent(2001, pos, Block.getStateId(state));

		NonNullList<ItemStack> drops = NonNullList.create();
		b.getDrops(drops, world, pos, state, 0);
		for(ItemStack drop : drops) dropItem(drop);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	/** Removes the top two blocks from a three block crop like cacti and sugar cane */
	protected void cutCane(Block target, BlockPos pos) {

		// people may be inclined to incorrectly place this thing one block above
		// the intended operating level, so we compensate for that
		int offset = world.getBlockState(pos.down()).getBlock() == target ? -1 : 0;

		// top to bottom
		for(int i = 2 + offset; i > offset; i--) {
			BlockPos target2 = pos.up(i);
			IBlockState state = world.getBlockState(target2);
			Block b = state.getBlock();
			world.playEvent(2001, target2, Block.getStateId(state));

			NonNullList<ItemStack> drops = NonNullList.create();
			b.getDrops(drops, world, target2, state, 0);
			for(ItemStack drop : drops) dropItem(drop);
			world.setBlockState(target2, Blocks.AIR.getDefaultState());
		}
	}

	/** Harvests and re-plants crops like wheat */
	protected void cutCrop(Block b, IBlockState state, BlockPos pos) {

		IBlockState soil = world.getBlockState(pos.down());

		world.playEvent(2001, pos, Block.getStateId(state));

		IBlockState replacement = Blocks.AIR.getDefaultState();

		if(!world.isRemote && !world.restoringBlockSnapshots) {
			NonNullList<ItemStack> drops = NonNullList.create();
			b.getDrops(drops, world, pos, state, 0);
			boolean replanted = false;

			for(ItemStack drop : drops) {
				if(!replanted && drop.getItem() instanceof IPlantable) {
					IPlantable seed = (IPlantable) drop.getItem();

					if(soil.getBlock().canSustainPlant(soil, world, pos.down(), net.minecraft.util.EnumFacing.UP, seed)) {
						replacement = seed.getPlant(world, pos);
						replanted = true;
						drop.shrink(1);
					}
				}

				if(!drop.isEmpty()) dropItem(drop);
			}

			// until 1.14 full-grown wheat could sometimes drop no seeds at all,
			// this is a quick and dirty workaround for that
			if(b == Blocks.WHEAT && !replanted) {
				replacement = b.getDefaultState();
			}
		}

		world.setBlockState(pos, replacement, 3);
	}

	protected void dropItem(ItemStack drop) {

		ForgeDirection dir = getDir().getOpposite();
		double spawnX = pos.getX() + 0.5 - dir.offsetX * 0.75;
		double spawnZ = pos.getZ() + 0.5 - dir.offsetZ * 0.75;

		EntityItem entityItem = new EntityItem(world, spawnX, pos.getY(), spawnZ, drop);
		entityItem.setPickupDelay(10);
		entityItem.motionX = dir.offsetX * -0.2 + 0.2;
		entityItem.motionZ = dir.offsetZ * -0.2;
		entityItem.velocityChanged = true;
		world.spawnEntity(entityItem);
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.isSuspended);
		buf.writeFloat(this.angle);
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.isOn = buf.readBoolean();
		this.isSuspended = buf.readBoolean();
		this.syncAngle = buf.readFloat();
		this.turnProgress = 3; // use 3-ply for extra smoothness
		this.tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.isOn = nbt.getBoolean("isOn");
		this.isSuspended = nbt.getBoolean("isSuspended");
		this.angle = nbt.getFloat("angle");
		this.state = nbt.getInteger("state");
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("isOn", this.isOn);
		nbt.setBoolean("isSuspended", this.isSuspended);
		nbt.setFloat("angle", this.angle);
		nbt.setInteger("state", this.state);
		tank.writeToNBT(nbt, "t");
		return super.writeToNBT(nbt);
	}

	@Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {tank}; }
	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tank}; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(pos.getX() - 10, pos.getY(), pos.getZ() - 10, pos.getX() + 11, pos.getY() + 7, pos.getZ() + 11);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override public FluidTankNTM getTankToPaste() { return tank; }
}
