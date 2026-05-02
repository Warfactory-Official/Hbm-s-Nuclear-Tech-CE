package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ILaserable;
import com.hbm.inventory.container.ContainerCoreReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICoreReceiver;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityCoreReceiver extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IGUIProvider, IFluidStandardReceiver, ILaserable, CompatHandler.OCComponent {

    public long power;
    public long joules;
    public long prevJoules;
    public FluidTankNTM tank;
    private AxisAlignedBB bb;

    public TileEntityCoreReceiver() {
        super(0, true, true);
        tank = new FluidTankNTM(Fluids.CRYOGEL, 64000).withOwner(this);
    }

    @Override
    public void update() {
        if (!world.isRemote) {

            this.subscribeToAllAround(tank.getTankType(), this);

            power = joules * 5000;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

            if (joules > 0) {

                if (tank.getFill() >= 20) {
                    tank.setFill(tank.getFill() - 20);
                } else {
                    world.setBlockState(pos, Blocks.LAVA.getDefaultState());
                    return;
                }
            }

            prevJoules = joules;
            this.networkPackNT(50);


            joules = 0;
        }
    }

    @Override
    public String getDefaultName() {
        return "container.dfcReceiver";
    }


    @Override
    public void addEnergy(World world, BlockPos pos, long energy, EnumFacing dir) {
        // only accept lasers from the front
        if (dir.getOpposite().ordinal() == this.getBlockMetadata()) {
            joules += energy;
        } else {
            world.destroyBlock(pos, false);
            world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.5F, true);
        }
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        power = compound.getLong("power");
        joules = compound.getLong("joules");
        tank.readFromNBT(compound, "tank");
    }


    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setLong("joules", joules);
        tank.writeToNBT(compound, "tank");
        return super.writeToNBT(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeLong(prevJoules);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        joules = buf.readLong();
        tank.deserialize(buf);
    }


    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return this.power;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "dfc_receiver";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{joules, getPower()}; //literally only doing this for the consistency between components
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCryogel(Context context, Arguments args) {
        return new Object[]{tank.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{joules, getPower(), tank.getFill()};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCoreReceiver(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICoreReceiver(player, this);
    }

    //TODO: figure out what this is
//	@Override
//	public void provideExtraInfo(NBTTagCompound data) {
//		data.setDouble(CompatEnergyControl.D_CONSUMPTION_MB, joules > 0 ? 20 : 0);
//		data.setDouble(CompatEnergyControl.D_OUTPUT_HE, joules * 5000);
//	}
}
