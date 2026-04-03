package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ILaserable;
import com.hbm.inventory.container.ContainerCoreEmitter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICoreEmitter;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityCoreEmitter extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2,  ILaserable, IFluidStandardReceiver, IGUIProvider, SimpleComponent, CompatHandler.OCComponent {

	public long power;
	public static final long maxPower = 1000000000L;
	public int watts;
	public int beam;
	public long joules;
	public boolean isOn;
	public FluidTankNTM tank;
	public long prev;
    private int prevBeam;

	public static final int range = 50;
	
	public TileEntityCoreEmitter() {
		super(0, true, true);
		tank = new FluidTankNTM(Fluids.CRYOGEL,64000);
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			this.subscribeToAllAround(tank.getTankType(), this);

			watts = MathHelper.clamp(watts, 1, 100);
			long demand = maxPower * watts / 2000;


			beam = 0;

			if(joules > 0 || prev > 0) {

				if(tank.getFill() >= 20) {
					tank.setFill(tank.getFill() - 20);
				} else {
					world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
					return;
				}
			}

			if(isOn) {

				//i.e. 50,000,000 HE = 10,000 SPK
				//1 SPK = 5,000HE

				if(power >= demand) {
					power -= demand;
					long add = watts * 100L;
					joules += add;
				}
				prev = joules;

				if(joules > 0) {

					long out = joules * 95 / 100;

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
					for(int i = 1; i <= range; i++) {

						beam = i;

						int x = pos.getX() + dir.offsetX * i;
						int y = pos.getY() + dir.offsetY * i;
						int z = pos.getZ() + dir.offsetZ * i;
						BlockPos affectedPos = new BlockPos(x, y, z);

						IBlockState state = world.getBlockState( affectedPos);
						Block block = state.getBlock();
						TileEntity te = world.getTileEntity(affectedPos);

						if(block instanceof ILaserable) { ((ILaserable)block).addEnergy(world, affectedPos, out, dir.toEnumFacing()); break; }
						if(te instanceof ILaserable) { ((ILaserable)te).addEnergy(world, affectedPos, out, dir.toEnumFacing()); break; }
						if(te instanceof TileEntityCore) { out = ((TileEntityCore)te).burn(out); continue; }


						if(!world.isAirBlock(affectedPos)) {

							if(state.getMaterial().isLiquid()) {
								world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
								world.setBlockToAir(affectedPos);
								break;
							}

							@SuppressWarnings("deprecation")
							float hardness = block.getExplosionResistance(null);
							if(hardness < 6000 && world.rand.nextInt(20) == 0)  {
								world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
								world.destroyBlock(affectedPos, false);
							}
							break;
						}
					}


					joules = 0;

					double blx = Math.min(pos.getX(), pos.getX() + dir.offsetX * beam) + 0.2;
					double bux = Math.max(pos.getX(), pos.getX() + dir.offsetX * beam) + 0.8;
					double bly = Math.min(pos.getY(), pos.getY() + dir.offsetY * beam) + 0.2;
					double buy = Math.max(pos.getY(), pos.getY() + dir.offsetY * beam) + 0.8;
					double blz = Math.min(pos.getZ(), pos.getZ() + dir.offsetZ * beam) + 0.2;
					double buz = Math.max(pos.getZ(), pos.getZ() + dir.offsetZ * beam) + 0.8;

					List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blx, bly, blz, bux, buy, buz));

					for(Entity e : list) {
						e.attackEntityFrom(ModDamageSource.amsCore, 50);
						e.setFire(10);
					}
				}
			} else {
				joules = 0;
				prev = 0;
			}

			this.markDirty();

			this.networkPackNT(250);
        } else {
            if (prevBeam != beam) {
                prevBeam = beam;
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
		}

	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}

	@Override
	public String getDefaultName() {
		return "container.dfcEmitter";
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public int getWattsScaled(int i) {
		return (watts * i) / 100;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(power);
		buf.writeInt(watts);
		buf.writeLong(prev);
		buf.writeInt(beam);
		buf.writeBoolean(isOn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.power = buf.readLong();
		this.watts = buf.readInt();
		this.prev = buf.readLong();
		this.beam = buf.readInt();
		this.isOn = buf.readBoolean();
		tank.deserialize(buf);
	}

	@Override
	public void addEnergy(World world, BlockPos pos, long energy, EnumFacing dir) {
		//do not accept lasers from the front
		if(dir.getOpposite().ordinal() != this.getBlockMetadata())
			joules += energy;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
        if (beam <= 0) {
            return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1,
                    pos.getZ() + 1);
        }
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
        int endX = pos.getX() + dir.offsetX * beam;
        int endY = pos.getY() + dir.offsetY * beam;
        int endZ = pos.getZ() + dir.offsetZ * beam;
        return new AxisAlignedBB(
                Math.min(pos.getX(), endX), Math.min(pos.getY(), endY), Math.min(pos.getZ(), endZ),
                Math.max(pos.getX(), endX) + 1, Math.max(pos.getY(), endY) + 1, Math.max(pos.getZ(), endZ) + 1
        );
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		power = compound.getLong("power");
		watts = compound.getInteger("watts");
		joules = compound.getLong("joules");
		prev = compound.getLong("prev");
		isOn = compound.getBoolean("isOn");
		tank.readFromNBT(compound, "tank");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setInteger("watts", watts);
		compound.setLong("joules", joules);
		compound.setLong("prev", prev);
		compound.setBoolean("isOn", isOn);
		tank.writeToNBT(compound, "tank");
		return super.writeToNBT(compound);
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tank };
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tank};
	}

	// do some opencomputer stuff
	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "dfc_emitter";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCryogel(Context context, Arguments args) {
		return new Object[] {tank.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInput(Context context, Arguments args) {
		return new Object[] {watts};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower(), tank.getFill(), watts, isOn};
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

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "opencomputers")
	public Object[] setInput(Context context, Arguments args) {
		int newOutput = args.checkInteger(0);
		watts = MathHelper.clamp(newOutput, 0, 100);
		return new Object[] {};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCoreEmitter(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICoreEmitter(player, this);
	}

}
