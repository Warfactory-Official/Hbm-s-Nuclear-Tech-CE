package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCoreInjector;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICoreInjector;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
public class TileEntityCoreInjector extends TileEntityMachineBase implements ITickable, IFluidStandardReceiver, SimpleComponent, IGUIProvider, CompatHandler.OCComponent {

    public static final int range = 15;
    public FluidTankNTM[] tanks;
    public int beam;

    public TileEntityCoreInjector() {
        super(4, true, false);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.DEUTERIUM, 128000);
        tanks[1] = new FluidTankNTM(Fluids.TRITIUM, 128000);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.subscribeToAllAround(tanks[0].getTankType(), this);
            this.subscribeToAllAround(tanks[1].getTankType(), this);

            tanks[0].setType(0, 1, inventory);
            tanks[1].setType(2, 3, inventory);

            beam = 0;

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
            for (int i = 1; i <= range; i++) {

                int x = pos.getX() + dir.offsetX * i;
                int y = pos.getY() + dir.offsetY * i;
                int z = pos.getZ() + dir.offsetZ * i;
                BlockPos pos = new BlockPos(x, y, z);

                TileEntity te = world.getTileEntity(pos);

                if (te instanceof TileEntityCore) {

                    TileEntityCore core = (TileEntityCore) te;

                    for (int t = 0; t < 2; t++) {

                        if (core.tanks[t].getTankType() == tanks[t].getTankType()) {

                            int f = Math.min(tanks[t].getFill(), core.tanks[t].getMaxFill() - core.tanks[t].getFill());

                            tanks[t].setFill(tanks[t].getFill() - f);
                            core.tanks[t].setFill(core.tanks[t].getFill() + f);
                            core.markDirty();

                        } else if (core.tanks[t].getFill() == 0) {

                            core.tanks[t].setTankType(tanks[t].getTankType());
                            int f = Math.min(tanks[t].getFill(), core.tanks[t].getMaxFill() - core.tanks[t].getFill());

                            tanks[t].setFill(tanks[t].getFill() - f);
                            core.tanks[t].setFill(core.tanks[t].getFill() + f);
                            core.markDirty();
                        }
                    }

                    beam = i;
                    break;
                }

                if (!world.isAirBlock(pos))
                    break;
            }

            this.markDirty();

            this.networkPackNT(250);
        }
    }


    @Override
    public String getDefaultName() {
        return "container.dfcInjector";
    }


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
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeInt(beam);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        this.beam = buf.readInt();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

        tanks[0].readFromNBT(nbt, "fuel1");
        tanks[1].readFromNBT(nbt, "fuel2");
        super.readFromNBT(nbt);
    }


    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        tanks[0].writeToNBT(compound, "fuel1");
        tanks[1].writeToNBT(compound, "fuel2");
        return super.writeToNBT(compound);
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0], tanks[1]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "dfc_injector";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFuel(Context context, Arguments args) {
        return new Object[]{tanks[0].getFill(), tanks[1].getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getTypes(Context context, Arguments args) {
        return new Object[]{tanks[0].getTankType().getName(), tanks[1].getTankType().getName()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{tanks[0].getFill(), tanks[0].getTankType().getName(), tanks[1].getFill(), tanks[1].getTankType().getName()};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCoreInjector(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICoreInjector(player.inventory, this);
    }

}
