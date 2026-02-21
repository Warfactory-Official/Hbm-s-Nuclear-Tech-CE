package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineITER;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerITER;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIITER;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import com.hbm.inventory.recipes.FusionRecipesLegacy;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemFusionShield;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityITER extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, CompatHandler.OCComponent {

	public long power;
	public static final long maxPower = 10000000;
	public static final int powerReq = 100000;
    public FluidTankNTM[] tanks;
	public FluidTankNTM plasma;

	public int progress;
	public static final int duration = 100;
	public long totalRuntime;

	@SideOnly(Side.CLIENT)
	public int blanket;

	public float rotor;
	public float lastRotor;
	public boolean isOn;

	private float rotorSpeed = 0F;

	private AudioWrapper audio;

    public TileEntityITER() {
		super(5, true, true);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.WATER, 1280000);
		tanks[1] = new FluidTankNTM(Fluids.ULTRAHOTSTEAM, 128000);
		plasma = new FluidTankNTM(Fluids.PLASMA_DT, 16000);
	}

	@Override
	public String getDefaultName() {
		return "container.machineITER";
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			this.updateConnections();
			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

			/// START Processing part ///

			if(!isOn) {
				plasma.setFill(0);	//jettison plasma if the thing is turned off
			}

			//explode either if there's plasma that is too hot or if the reactor is turned on but the magnets have no power
			if(plasma.getFill() > 0 && (this.plasma.getTankType().temperature >= this.getShield() || (this.isOn && this.power < this.powerReq))) {
				this.explode();
            }

			if(isOn && power >= powerReq) {
				power -= powerReq;

				if(plasma.getFill() > 0) {
					this.totalRuntime++;
					int delay = FusionRecipesLegacy.getByproductDelay(plasma.getTankType());
					if(delay > 0 && totalRuntime % delay == 0) produceByproduct();
				}

				if(plasma.getFill() > 0 && this.getShield() != 0) {

					ItemFusionShield.setShieldDamage(inventory.getStackInSlot(3), ItemFusionShield.getShieldDamage(inventory.getStackInSlot(3)) + 1);

					if(ItemFusionShield.getShieldDamage(inventory.getStackInSlot(3)) > ((ItemFusionShield)inventory.getStackInSlot(3).getItem()).maxDamage){
						inventory.setStackInSlot(3, ItemStack.EMPTY);
						world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.shutdown, SoundCategory.BLOCKS, 5F, 1F);
						this.isOn = false;
						this.markDirty();
					}
				}

				int prod = FusionRecipesLegacy.getSteamProduction(plasma.getTankType());

				for(int i = 0; i < 20; i++) {

					if(plasma.getFill() > 0) {

						if(tanks[0].getFill() >= prod * 10) {
							tanks[0].setFill(tanks[0].getFill() - prod * 10);
							tanks[1].setFill(tanks[1].getFill() + prod);

							if(tanks[1].getFill() > tanks[1].getMaxFill())
								tanks[1].setFill(tanks[1].getMaxFill());
						}

						plasma.setFill(plasma.getFill() - 1);
					}
				}
			}
			doBreederStuff();
			/// END Processing part ///

			/// START Notif packets ///
			for(DirPos pos : getConPos()) {
				if(tanks[1].getFill() > 0) {
					this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}

			networkPackNT(250);
			/// END Notif packets ///
		} else {

			this.lastRotor = this.rotor;
			this.rotor += this.rotorSpeed;

			if(this.rotor >= 360) {
				this.rotor -= 360;
				this.lastRotor -= 360;
			}

			if(this.isOn && this.power >= powerReq) {
				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed + 0.05F));

				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound(new SoundEvent(new ResourceLocation("hbm:block.fusionReactorRunning")), SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 30F, 1.0F);
					audio.startSound();
				}

				float rotorSpeed = this.rotorSpeed / 15F;
				audio.updateVolume(getVolume(0.5f * rotorSpeed));
				audio.updatePitch(0.25F + 0.75F * rotorSpeed);
			} else {
				this.rotorSpeed = Math.max(0F, Math.min(15F, this.rotorSpeed - 0.1F));

				if(audio != null) {
					if(this.rotorSpeed > 0) {
						float rotorSpeed = this.rotorSpeed / 15F;
						audio.updateVolume(getVolume(0.5f * rotorSpeed));
						audio.updatePitch(0.25F + 0.75F * rotorSpeed);
					} else {
						audio.stopSound();
						audio = null;
					}
				}
			}
		}
	}

	protected List<DirPos> connections;

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	protected List<DirPos> getConPos() {
		if(connections != null && !connections.isEmpty())
			return connections;

		connections = new ArrayList();

		connections.add(new DirPos(pos.getX(), pos.getY() + 3, pos.getZ(), ForgeDirection.UP));
		connections.add(new DirPos(pos.getX(), pos.getY() - 3, pos.getZ(), ForgeDirection.DOWN));

		Vec3 vec = Vec3.createVectorHelper(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {
			vec.rotateAroundY((float) (Math.PI / 8));
			connections.add(new DirPos(pos.getX() + (int)vec.xCoord, pos.getY() + 3, pos.getZ() + (int)vec.zCoord, ForgeDirection.UP));
			connections.add(new DirPos(pos.getX() + (int)vec.xCoord, pos.getY() - 3, pos.getZ() + (int)vec.zCoord, ForgeDirection.DOWN));
		}

		return connections;
	}

    private void explode() {
        this.disassemble();
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        if(this.plasma.getTankType() == Fluids.PLASMA_BF) {

            world.playSound(null, xCoord, yCoord, zCoord, HBMSoundHandler.mukeExplosion, SoundCategory.HOSTILE, 15.0F, 1.0F);
            ExplosionLarge.spawnShrapnels(world, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 50);

            ExplosionNT exp = new ExplosionNT(world, null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 20F)
                    .addAttrib(ExplosionNT.ExAttrib.BALEFIRE)
                    .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE)
                    .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                    .addAttrib(ExplosionNT.ExAttrib.NODROP)
                    .overrideResolution(64);
            exp.doExplosionA();
            exp.doExplosionB(false);

            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "muke");
            data.setBoolean("balefire", true);
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), xCoord, yCoord, zCoord, 250));

        } else {
            Vec3 vec = Vec3.createVectorHelper(5.5, 0, 0);
            vec.rotateAroundY(world.rand.nextFloat() * (float)Math.PI * 2F);
            world.newExplosion(null, xCoord + 0.5 + vec.xCoord, yCoord + 0.5 + world.rand.nextGaussian() * 1.5D, zCoord + 0.5 + vec.zCoord, 2.5F, true, true);
        }
    }
	
	private void doBreederStuff() {

		if(plasma.getFill() == 0) {
			this.progress = 0;
			return;
		}

        ItemStack stackIn = inventory.getStackInSlot(1);
        if (stackIn.isEmpty()) return;
        BreederRecipe out = BreederRecipes.getOutput(stackIn);
		
		if(stackIn.getItem() == ModItems.meteorite_sword_irradiated)
			out = new BreederRecipe(ModItems.meteorite_sword_fused, 1000);

		if(stackIn.getItem() == ModItems.meteorite_sword_fused)
			out = new BreederRecipe(ModItems.meteorite_sword_baleful, 4000);

		if(out == null) {
			this.progress = 0;
			return;
		}

		if(!inventory.insertItem(2, out.output, true).isEmpty()) {
			this.progress = 0;
			return;
		}

		int level = FusionRecipesLegacy.getBreedingLevel(plasma.getTankType());

		if(out.flux > level) {
			this.progress = 0;
			return;
		}

		progress++;

		if(progress > duration) {
			this.progress = 0;
            inventory.insertItem(2, out.output, false);
			inventory.extractItem(1, 1, false);
			this.markDirty();
		}
	}

	private void produceByproduct() {

		ItemStack by = FusionRecipesLegacy.getByproduct(plasma.getTankType());

		if(by == null)
			return;

		if(inventory.getStackInSlot(4).isEmpty()) {
			inventory.setStackInSlot(4, by);
			return;
		}

		if(inventory.getStackInSlot(4).getItem() == by.getItem() && inventory.getStackInSlot(4).getItemDamage() == by.getItemDamage() && inventory.getStackInSlot(4).getCount() < inventory.getStackInSlot(4).getMaxStackSize()) {
			inventory.getStackInSlot(4).grow(1);
		}
	}

	public int getShield() {

		if(inventory.getStackInSlot(3).isEmpty() || !(inventory.getStackInSlot(3).getItem() instanceof ItemFusionShield))
			return 0;

		return ((ItemFusionShield) inventory.getStackInSlot(3).getItem()).maxTemp + 273;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isOn);
		buf.writeLong(power);

		ItemStack itemStack = inventory.getStackInSlot(3);

		if(itemStack.isEmpty()) {
			buf.writeByte(0);
		} else if(itemStack.getItem() == ModItems.fusion_shield_tungsten) {
			buf.writeByte(1);
		} else if(itemStack.getItem() == ModItems.fusion_shield_desh) {
			buf.writeByte(2);
		} else if(itemStack.getItem() == ModItems.fusion_shield_chlorophyte) {
			buf.writeByte(3);
		} else if(itemStack.getItem() == ModItems.fusion_shield_vaporwave) {
			buf.writeByte(4);
		}
		buf.writeInt(progress);
		for (FluidTankNTM tank : tanks)
			tank.serialize(buf);
		plasma.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
		this.power = buf.readLong();
		this.blanket = buf.readByte();
		this.progress = buf.readInt();
		for (FluidTankNTM tank : tanks)
			tank.deserialize(buf);
		plasma.deserialize(buf);
	}

	@Override
	public void handleButtonPacket(int value, int meta) {

		if(meta == 0) {
			this.isOn = !this.isOn;
		}
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public long getProgressScaled(long i) {
		return (progress * i) / duration;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		this.totalRuntime = nbt.getLong("totalRuntime");
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		plasma.readFromNBT(nbt, "plasma");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", this.power);
		nbt.setBoolean("isOn", isOn);
		nbt.setLong("totalRuntime", this.totalRuntime);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		plasma.writeToNBT(nbt, "plasma");
		return nbt;
	}

	public void disassemble() {
		
		MachineITER.drop = false;

		int[][][] layout = TileEntityITERStruct.layout;

		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < layout[0].length; x++) {
				for(int z = 0; z < layout[0][0].length; z++) {

					int ly = y > 2 ? 4 - y : y;

					int width = 7;

					if(x == width && y == 0 && z == width)
						continue;

					int b = layout[ly][x][z];

					switch(b) {
                        /*
					case 1: world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_conductor.getDefaultState());break;
					case 2: world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_center.getDefaultState());break;
					case 3: world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.fusion_motor.getDefaultState());break;
					case 4: world.setBlockState(new BlockPos(pos.getX() - width + x, pos.getY() + y - 2, pos.getZ() - width + z), ModBlocks.reinforced_glass.getDefaultState());break;

                         */
					}
				}
			}
		}

		MachineITER.drop = true;
		
		List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class,
				new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).grow(50, 10, 50));

		for(EntityPlayerMP player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.achMeltdown);
		}
	}

	@Override
	public void setPower(long i) {
		this.power = i;
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
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanks[1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanks[0]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return true;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 2, 4 };
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = new AxisAlignedBB(pos.getX() + 0.5 - 8, pos.getY() + 0.5 - 3, pos.getZ() + 0.5 - 8, pos.getX() + 0.5 + 8, pos.getY() + 0.5 + 3, pos.getZ() + 0.5 + 8);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerITER(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIITER(player.inventory, this);
	}

    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_fusion";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[] {getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isActive(Context context, Arguments args) {
        return new Object[] {isOn};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setActive(Context context, Arguments args) {
        isOn = args.checkBoolean(0);
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFluid(Context context, Arguments args) {
        return new Object[] {
                tanks[0].getFill(), tanks[0].getMaxFill(),
                tanks[1].getFill(), tanks[1].getMaxFill(),
                plasma.getFill(), plasma.getMaxFill(), plasma.getTankType().getTranslationKey()
        };
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPlasmaTemp(Context context, Arguments args) {
        return new Object[] {plasma.getTankType().temperature};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getMaxTemp(Context context, Arguments args) {
        if (!inventory.getStackInSlot(3).isEmpty() && (inventory.getStackInSlot(3).getItem() instanceof ItemFusionShield))
            return new Object[] {((ItemFusionShield) inventory.getStackInSlot(3).getItem()).maxTemp};
        return new Object[] {"N/A"};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getBlanketDamage(Context context, Arguments args) {
        if (!inventory.getStackInSlot(3).isEmpty() && (inventory.getStackInSlot(3).getItem() instanceof ItemFusionShield))
            return new Object[]{ItemFusionShield.getShieldDamage(inventory.getStackInSlot(3)), ((ItemFusionShield)inventory.getStackInSlot(3).getItem()).maxDamage};
        return new Object[] {"N/A", "N/A"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
                "getEnergyInfo",
                "isActive",
                "setActive",
                "getFluid",
                "getPlasmaTemp",
                "getMaxTemp",
                "getBlanketDamage"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case ("getEnergyInfo"):
                return getEnergyInfo(context, args);
            case ("isActive"):
                return isActive(context, args);
            case ("setActive"):
                return setActive(context, args);
            case ("getFluid"):
                return getFluid(context, args);
            case ("getPlasmaTemp"):
                return getPlasmaTemp(context, args);
            case ("getMaxTemp"):
                return getMaxTemp(context, args);
            case ("getBlanketDamage"):
                return getBlanketDamage(context, args);
        }
        throw new NoSuchMethodException();
    }

	@Override
	public FluidTankNTM getTankToPaste() {
		return null;
	}
}


