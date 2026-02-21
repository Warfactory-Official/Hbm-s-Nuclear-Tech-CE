package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@AutoRegister
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityRBMKCooler extends TileEntityRBMKBase implements IFluidStandardReceiver, SimpleComponent, CompatHandler.OCComponent {

	public FluidTankNTM tank;
	int lastCooled;

	public TileEntityRBMKCooler() {
		super();
		this.tank = new FluidTankNTM(Fluids.CRYOGEL, 16000);
	}

	public void getDiagData(NBTTagCompound nbt) {
		this.writeToNBT(nbt);
		nbt.removeTag("jumpheight");
	}

	@Override
	public void update() {
        int rbmkHeight = RBMKDials.getColumnHeight(world);
		if(!world.isRemote) {

            if (this.world.getTotalWorldTime() % 20 == 0)
                this.trySubscribe(tank.getTankType(), world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);

			if((int) (this.heat) > 750) {

				int heatProvided = (int) (this.heat - 750D);
				int cooling = Math.min(heatProvided, tank.getFluidAmount());

				this.heat -= cooling;
				this.tank.drain(cooling, true);

				this.lastCooled = cooling;

				if(lastCooled > 0) {
					List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX(), pos.getY()+rbmkHeight, pos.getZ(), pos.getX()+1, pos.getY()+rbmkHeight+6, pos.getZ()+1));

					for(Entity e : entities) {
						e.setFire(5);
						e.attackEntityFrom(DamageSource.IN_FIRE, 10);
					}
				}
			} else {
				this.lastCooled = 0;
			}

			if(this.lastCooled > 100) {
				world.playSound(null, pos.getX() + 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.5, HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, 1.0F, 1.25F + world.rand.nextFloat());
				world.playSound(null, pos.getX() + 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1F + world.rand.nextFloat() * 0.5F);
			} else if(this.lastCooled > 50){
				world.playSound(null, pos.getX() + 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.75F + world.rand.nextFloat() * 0.5F);
			} else if(this.lastCooled > 5){
				if(world.rand.nextInt(20) == 0) {
					world.playSound(null, pos.getX() + 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.25F + world.rand.nextFloat() * 0.5F);
				}
			}

		} else {

			if(this.lastCooled > 100) {

				for(int i = 0; i < 2; i++) {
					world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, 0, 1, 0);
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, 0, 1, 0);
				}

				if(world.rand.nextInt(20) == 0){
					world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0);
				}

			} else if(this.lastCooled > 75) {

				for(int i = 0; i < 2; i++) {
					world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, world.rand.nextGaussian() * 0.05, 0.5, world.rand.nextGaussian() * 0.05);
				}

				if(world.rand.nextInt(20) == 0){
					world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.0, 0);
				}

			} else if(this.lastCooled > 50) {

				for(int i = 0; i < 2; i++) {
					world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, world.rand.nextGaussian() * 0.05, 0.3, world.rand.nextGaussian() * 0.05);
				}

			} else if(this.lastCooled > 5) {

				if(world.getTotalWorldTime() % 2 == 0){
					world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + rbmkHeight + 0.5, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.2, 0);
				}
			}
		}

		super.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		tank.readFromNBT(nbt, "cryo");
		this.lastCooled = nbt.getInteger("cooled");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tank.writeToNBT(nbt, "cryo");
		nbt.setInteger("cooled", this.lastCooled);
		return nbt;
	}

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        this.tank.serialize(buf);
        buf.writeInt(this.lastCooled);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.tank.deserialize(buf);
        this.lastCooled = buf.readInt();
    }

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.COOLER;
	}

	@Override
	public RBMKColumn getConsoleData() {
		RBMKColumn.CoolerColumn data = (RBMKColumn.CoolerColumn) super.getConsoleData();
		data.cryo = this.tank.getFill();
		data.cooled = this.lastCooled;
		return data;
	}

	// control panel
	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = super.getQueryData();

		data.put("coolant", new DataValueFloat(tank.getFill()));

		return data;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "rbmk_cooler";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[]{heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCryo(Context context, Arguments args) {
		return new Object[]{tank.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCryoMax(Context context, Arguments args) {
		return new Object[]{tank.getMaxFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[]{heat, tank.getFill(), tank.getMaxFill(), pos.getX(), pos.getY(), pos.getZ()};
	}
}
