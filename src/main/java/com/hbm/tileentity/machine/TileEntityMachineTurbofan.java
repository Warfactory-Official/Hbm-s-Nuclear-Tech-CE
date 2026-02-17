package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineTurbofan;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.gui.GUIMachineTurbofan;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.*;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineTurbofan extends TileEntityMachinePolluting implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver, IUpgradeInfoProvider, IGUIProvider, IFluidCopiable, IFFtoNTMF {

	public long power;
	public static final long maxPower = 1_000_000;
	public FluidTankNTM tank;
	public FluidTankNTM blood;

	public int afterburner;
	public boolean wasOn;
	public boolean showBlood = false;
	protected int output;
	protected int consumption;

	public float spin;
	public float lastSpin;
	public int momentum = 0;

	private final UpgradeManagerNT upgradeManager;

	//private static final int[] slots_top = new int[] { 0 };
	//private static final int[] slots_bottom = new int[] { 0, 0 };
	//private static final int[] slots_side = new int[] { 0 };

	public AudioWrapper audio;

	public TileEntityMachineTurbofan() {
		super(0, 150, true, true);

        inventory = new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot == 2)
                    SoundUtil.playUpgradePlugSound(world, pos);
            }
        };

		tank = new FluidTankNTM(Fluids.KEROSENE, 24000);
		blood = new FluidTankNTM(Fluids.BLOOD, 24000);
		upgradeManager = new UpgradeManagerNT(this);
	}

	@Override
	public String getDefaultName() {
		return "container.machineTurbofan";
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.power = compound.getLong("powerTime");
		tank.readFromNBT(compound,"tank");
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("powerTime", power);
		tank.writeToNBT(compound, "tank");
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	protected DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(this.pos.getX() + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() + rot.offsetX * 2 - dir.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot),
				new DirPos(this.pos.getX() - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 2 - dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 2 - dir.offsetZ, rot.getOpposite())
		};
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			this.output = 0;
			this.consumption = 0;

			tank.setType(4, inventory);
			tank.loadTank(0, 1, inventory);
			blood.setTankType(Fluids.BLOOD);

			this.wasOn = false;

			upgradeManager.checkSlots(2, 2);
			this.afterburner = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.AFTERBURN);

			if(!inventory.getStackInSlot(2).isEmpty() && inventory.getStackInSlot(2).getItem() == ModItems.flame_pony)
				this.afterburner = 100;

			long burnValue = 0;
			int amount = 1 + this.afterburner;
			int amountToBurn = Math.min(amount, this.tank.getFill());

			boolean redstone = false;
			for(DirPos pos : getConPos()) {
				if(this.world.isBlockPowered(pos.getPos())) {
					redstone = true;
					break;
				}
			}

			if(!redstone) {

				if (tank.getTankType().hasTrait(FT_Combustible.class) &&
					tank.getTankType().getTrait(FT_Combustible.class).getGrade() == FT_Combustible.FuelGrade.AERO) {
					burnValue = tank.getTankType().getTrait(FT_Combustible.class).getCombustionEnergy() / 1_000;
				}
				if (amountToBurn > 0) {
					this.wasOn = true;
					this.tank.setFill(this.tank.getFill() - amountToBurn);
					this.output = (int) (burnValue * amountToBurn * (1 + Math.min(this.afterburner / 3D, 4)));
					this.power += this.output;
					this.consumption = amountToBurn;

					if (world.getTotalWorldTime() % 20 == 0)
						super.pollute(tank.getTankType(), FluidTrait.FluidReleaseType.BURN, amountToBurn * 5);
				}
			}
			power = Library.chargeItemsFromTE(inventory, 3, power, power);

			for(DirPos pos : getConPos()) {
				this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				this.trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				if(this.blood.getFill() > 0) this.sendFluid(blood, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				this.sendSmoke(pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}

			if(burnValue > 0 && amountToBurn > 0) {

				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

				if(this.afterburner > 0) {

					for(int i = 0; i < 2; i++) {
						double speed = 2 + world.rand.nextDouble() * 3;
						double deviation = world.rand.nextGaussian() * 0.2;
						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "gasfire");
						data.setDouble("mX", -dir.offsetX * speed + deviation);
						data.setDouble("mZ", -dir.offsetZ * speed + deviation);
						data.setFloat("scale", 8F);
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5F - dir.offsetX * (3 - i), this.pos.getY() + 1.5F, this.pos.getZ() + 0.5F - dir.offsetZ * (3 - i)), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
					}

					/*if(this.afterburner > 90 && worldObj.rand.nextInt(60) == 0) {
						worldObj.newExplosion(null, xCoord + 0.5 + dir.offsetX * 3.5, yCoord + 0.5, zCoord + 0.5 + dir.offsetZ * 3.5, 3F, false, false);
					}*/

					if(this.afterburner > 90 && world.rand.nextInt(30) == 0) {
						world.playSound(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, HBMSoundHandler.chopperDamage /*HBMSoundHandler.blockDamage*/, SoundCategory.BLOCKS, 3.0F, 0.95F + world.rand.nextFloat() * 0.2F);
					}

					if(this.afterburner > 90) {
						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "gasfire");
						data.setDouble("mY", 0.1 * world.rand.nextDouble());
						data.setFloat("scale", 4F);
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data,
								this.pos.getX() + 0.5F + dir.offsetX * (world.rand.nextDouble() * 4 - 2) + rot.offsetX * (world.rand.nextDouble() * 2 - 1),
								this.pos.getY() + 1F + world.rand.nextDouble() * 2,
								this.pos.getZ() + 0.5F - dir.offsetZ * (world.rand.nextDouble() * 4 - 2) + rot.offsetZ * (world.rand.nextDouble() * 2 - 1)
						), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
					}
				}

				double minX = this.pos.getX() + 0.5 - dir.offsetX * 3.5 - rot.offsetX * 1.5;
				double maxX = this.pos.getX() + 0.5 - dir.offsetX * 19.5 + rot.offsetX * 1.5;
				double minZ = this.pos.getZ() + 0.5 - dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				double maxZ = this.pos.getZ() + 0.5 - dir.offsetZ * 19.5 + rot.offsetZ * 1.5;

				List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {

					if(this.afterburner > 0) {
						e.setFire(5);
						e.attackEntityFrom(DamageSource.ON_FIRE, 5F);
					}
					e.motionX -= dir.offsetX * 0.2;
					e.motionZ -= dir.offsetZ * 0.2;
				}

				minX = this.pos.getX() + 0.5 + dir.offsetX * 3.5 - rot.offsetX * 1.5;
				maxX = this.pos.getX() + 0.5 + dir.offsetX * 8.5 + rot.offsetX * 1.5;
				minZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				maxZ = this.pos.getZ() + 0.5 + dir.offsetZ * 8.5 + rot.offsetZ * 1.5;

				list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {
					e.motionX -= dir.offsetX * 0.2;
					e.motionZ -= dir.offsetZ * 0.2;
				}

				minX = this.pos.getX() + 0.5 + dir.offsetX * 3.5 - rot.offsetX * 1.5;
				maxX = this.pos.getX() + 0.5 + dir.offsetX * 3.75 + rot.offsetX * 1.5;
				minZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				maxZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.75 + rot.offsetZ * 1.5;

				list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {
					e.attackEntityFrom(ModDamageSource.turbofan, 1000);
					e.setInWeb();

					if(!e.isEntityAlive() && e instanceof EntityLivingBase) {
						NBTTagCompound vdat = new NBTTagCompound();
						vdat.setString("type", "giblets");
						vdat.setInteger("ent", e.getEntityId());
						vdat.setInteger("cDiv", 5);
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY + e.height * 0.5, e.posZ, 150));

						world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.BLOCKS, 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);

						blood.setFill(blood.getFill() + 50);
						if(blood.getFill() > blood.getMaxFill()) {
							blood.setFill(blood.getMaxFill());
						}
						this.showBlood = true;
					}
				}
			}

			if(this.power > maxPower) {
				this.power = maxPower;
			}

			networkPackNT(150);

		} else {

			this.lastSpin = this.spin;

			if(wasOn) {
				if(this.momentum < 100F)
					this.momentum++;
			} else {
				if(this.momentum > 0)
					this.momentum--;
			}

			this.spin += momentum / 2F;

			if(this.spin >= 360) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			if(momentum > 0) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				// mlbv: original value = momentum / 50F but this somehow made the sound range extremely short
				// so I lowered the denominator to 10F
				// Th3_Sl1ze: of course it will be short, because you use old audio system
				audio.updateVolume(getVolume(momentum / 50F));
				audio.updatePitch(momentum / 200F + 0.5F + this.afterburner * 0.16F);

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

			/*
			 * All movement related stuff has to be repeated on the client, but only for the client's player
			 * Otherwise this could lead to desync since the motion is never sent form the server
			 */
			if(tank.getFill() > 0 && !MainRegistry.proxy.me().capabilities.isCreativeMode) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

				double minX = this.pos.getX() + 0.5 - dir.offsetX * 3.5 - rot.offsetX * 1.5;
				double maxX = this.pos.getX() + 0.5 - dir.offsetX * 19.5 + rot.offsetX * 1.5;
				double minZ = this.pos.getZ() + 0.5 - dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				double maxZ = this.pos.getZ() + 0.5 - dir.offsetZ * 19.5 + rot.offsetZ * 1.5;

				List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {
					if(e == MainRegistry.proxy.me()) {
						e.motionX -= dir.offsetX * 0.2;
						e.motionZ -= dir.offsetZ * 0.2;
					}
				}

				minX = this.pos.getX() + 0.5 + dir.offsetX * 3.5 - rot.offsetX * 1.5;
				maxX = this.pos.getX() + 0.5 + dir.offsetX * 8.5 + rot.offsetX * 1.5;
				minZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				maxZ = this.pos.getZ() + 0.5 + dir.offsetZ * 8.5 + rot.offsetZ * 1.5;

				list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {
					if(e == MainRegistry.proxy.me()) {
						e.motionX -= dir.offsetX * 0.2;
						e.motionZ -= dir.offsetZ * 0.2;
					}
				}

				minX = this.pos.getX() + 0.5 + dir.offsetX * 3.5 - rot.offsetX * 1.5;
				maxX = this.pos.getX() + 0.5 + dir.offsetX * 3.75 + rot.offsetX * 1.5;
				minZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.5 - rot.offsetZ * 1.5;
				maxZ = this.pos.getZ() + 0.5 + dir.offsetZ * 3.75 + rot.offsetZ * 1.5;

				list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ)));

				for(Entity e : list) {
					if(e == MainRegistry.proxy.me()) {
						e.setInWeb();
					}
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(afterburner);
		buf.writeBoolean(wasOn);
		buf.writeBoolean(showBlood);
		tank.serialize(buf);
		blood.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.afterburner = buf.readInt();
		this.wasOn = buf.readBoolean();
		this.showBlood = buf.readBoolean();
		tank.deserialize(buf);
		blood.deserialize(buf);
	}

	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.turbofanOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 50F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {
        super.onChunkUnload();
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

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@NotNull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tank };
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] { blood };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] { tank, blood, smoke, smoke_leaded, smoke_poison };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineTurbofan(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineTurbofan(player.inventory, this);
	}

	@Override
	public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
		return type == ItemMachineUpgrade.UpgradeType.AFTERBURN;
	}

	@Override
	public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_turbofan));
		if(type == ItemMachineUpgrade.UpgradeType.AFTERBURN) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_EFFICIENCY, "+" + (int)(level * 100 * (1 + Math.min(level / 3D, 4D))) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
	}

	@Override
	public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
		HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(ItemMachineUpgrade.UpgradeType.AFTERBURN, 3);
		return upgrades;
	}
}
