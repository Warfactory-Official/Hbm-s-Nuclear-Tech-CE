package com.hbm.tileentity;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidConnectorMK2;
import com.hbm.api.fluidmk2.IFluidReceiverMK2;
import com.hbm.api.redstoneoverradio.IRORInfo;
import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.api.tile.IHeatSource;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.CapabilityContextProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@Optional.InterfaceList({
        @Optional.Interface(iface = "com.hbm.handler.CompatHandler.OCComponent", modid = "opencomputers"),
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
})
@AutoRegister
public class TileEntityProxyCombo extends TileEntityProxyBase implements IEnergyReceiverMK2, IHeatSource, IFluidReceiverMK2, ICrucibleAcceptor, SimpleComponent, CompatHandler.OCComponent, IRORValueProvider, IRORInteractive {

    TileEntity tile;
    boolean inventory;
    boolean power;
    boolean conductor;
    boolean fluid;
    public boolean moltenMetal;

    // due to some issues with OC deciding that it's gonna call the component name function before the worldObj is loaded
    // the component name must be cached to prevent it from shitting itself
    String componentName = CompatHandler.nullComponent;
    boolean supportsOC;

    boolean heat;

    public TileEntityProxyCombo() {
    }

    public TileEntityProxyCombo(boolean inventory, boolean power, boolean fluid) {
        this.inventory = inventory;
        this.power = power;
        this.fluid = fluid;
        this.heat = false;
    }

    public TileEntityProxyCombo(boolean inventory, boolean power, boolean fluid, boolean heat) {
        this.inventory = inventory;
        this.power = power;
        this.fluid = fluid;
        this.heat = heat;
    }


    public TileEntityProxyCombo inventory() {
        this.inventory = true;
        return this;
    }

    public TileEntityProxyCombo power() {
        this.power = true;
        return this;
    }

    public TileEntityProxyCombo conductor() {
        this.conductor = true;
        return this;
    }

    public TileEntityProxyCombo moltenMetal() {
        this.moltenMetal = true;
        return this;
    }

    public TileEntityProxyCombo fluid() {
        this.fluid = true;
        return this;
    }

    public TileEntityProxyCombo heatSource() {
        this.heat = true;
        return this;
    }

    /**
     * Returns the actual tile entity that represents the core. Only for internal use, and EnergyControl.
     */
    public TileEntity getTile() {
        if (tile == null || tile.isInvalid() || (tile instanceof TileEntityLoadedBase && !((TileEntityLoadedBase) tile).isLoaded)) {
            tile = this.getTE();
            supportsOC = tile instanceof CompatHandler.OCComponent;
        }
        return tile;
    }

    /**
     * Returns the core tile entity, or a delegate object.
     */
    protected Object getCoreObject() {
        return getTile();
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntity te = getTile();
        if (te == null) return super.getCapability(capability, facing);
        if (inventory && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || power && capability == CapabilityEnergy.ENERGY || fluid && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return te.getCapability(capability, facing);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntity te = getTile();
        if (te == null) return super.hasCapability(capability, facing);

        if (inventory && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || power && capability == CapabilityEnergy.ENERGY || fluid && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return te.hasCapability(capability, facing);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return super.hasCapability(capability, facing);
    }

    private @Nullable IEnergyReceiverMK2 coreEnergy() {
        Object te = getCoreObject();
        return te instanceof IEnergyReceiverMK2 ? (IEnergyReceiverMK2) te : null;
    }

    private @Nullable IEnergyConductorMK2 coreEnergyConductor() {
        Object te = getCoreObject();
        return te instanceof IEnergyConductorMK2 ? (IEnergyConductorMK2) te : null;
    }

    @Override
    public void setPower(long i) {
        if (!power) return;
        IEnergyReceiverMK2 core = coreEnergy();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                core.setPower(i);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
    }

    @Override
    public long getPower() {
        if (!power) return 0;
        IEnergyReceiverMK2 core = coreEnergy();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.getPower();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return 0;
    }

    @Override
    public long getMaxPower() {
        if (!power) return 0;
        IEnergyReceiverMK2 core = coreEnergy();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.getMaxPower();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return 0;
    }

    @Override
    public long transferPower(long amount, boolean simulate) {
        if (!this.power) return amount;
        IEnergyReceiverMK2 core = coreEnergy();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.transferPower(amount, simulate);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return amount;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        if (!power) return false;
        IEnergyReceiverMK2 core = coreEnergy();
        IEnergyConductorMK2 conductor = coreEnergyConductor();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.canConnect(dir);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        } else if (conductor != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return conductor.canConnect(dir);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory = compound.getBoolean("inv");
        fluid = compound.getBoolean("flu");
        this.moltenMetal = compound.getBoolean("metal");
        power = compound.getBoolean("pow");
        conductor = compound.getBoolean("conductor");
        heat = compound.getBoolean("hea");
        if(Compat.isOpenComputersLoaded())
            this.componentName = compound.getString("ocname");

        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("inv", inventory);
        compound.setBoolean("flu", fluid);
        compound.setBoolean("metal", moltenMetal);
        compound.setBoolean("pow", power);
        compound.setBoolean("conductor", conductor);
        compound.setBoolean("hea", heat);
        if(Compat.isOpenComputersLoaded())
            compound.setString("ocname", componentName);
        return super.writeToNBT(compound);
    }

    @Override
    public void serializeInitial(ByteBuf buf) {
        byte flags = 0;
        if (inventory)   flags |= 1;
        if (power)       flags |= 2;
        if (fluid)       flags |= 4;
        if (conductor)   flags |= 8;
        if (heat)        flags |= 16;
        if (moltenMetal) flags |= 32;
        buf.writeByte(flags);
    }

    @Override
    public void deserializeInitial(ByteBuf buf) {
        byte flags = buf.readByte();
        inventory   = (flags & 1)  != 0;
        power       = (flags & 2)  != 0;
        fluid       = (flags & 4)  != 0;
        conductor   = (flags & 8)  != 0;
        heat        = (flags & 16) != 0;
        moltenMetal = (flags & 32) != 0;
    }

    @Override
    public boolean allowDirectProvision() {
        if (!power) return false;
        if (getCoreObject() instanceof IEnergyReceiverMK2)
            return ((IEnergyReceiverMK2) getCoreObject()).allowDirectProvision();
        return true;
    }


    public static final FluidTankNTM[] EMPTY_TANKS = new FluidTankNTM[0];

    private @Nullable IFluidReceiverMK2 coreFluidRecv() {
        Object te = getCoreObject();
        return te instanceof IFluidReceiverMK2 ? (IFluidReceiverMK2) te : null;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        if (!fluid) return EMPTY_TANKS;
        IFluidReceiverMK2 core = coreFluidRecv();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.getAllTanks();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return EMPTY_TANKS;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!fluid) return amount;
        IFluidReceiverMK2 core = coreFluidRecv();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.transferFluid(type, pressure, amount);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return amount;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if (!fluid) return 0;
        IFluidReceiverMK2 core = coreFluidRecv();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.getDemand(type, pressure);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return 0;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        if (!this.fluid) return false;
        Object te = getCoreObject();
        if (te instanceof IFluidConnectorMK2 connectorMK2) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return connectorMK2.canConnect(type, dir);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return true;
    }

    private @Nullable IHeatSource coreHeat() {
        Object te = getCoreObject();
        return te instanceof IHeatSource ? (IHeatSource) te : null;
    }

    @Override
    public int getHeatStored() {
        if (!this.heat) return 0;
        IHeatSource core = coreHeat();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                return core.getHeatStored();
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
        return 0;
    }

    @Override
    public void useUpHeat(int heat) {
        if (!this.heat) return;
        IHeatSource core = coreHeat();
        if (core != null) {
            BlockPos prev = CapabilityContextProvider.pushPos(this.pos);
            try {
                core.useUpHeat(heat);
            } finally {
                CapabilityContextProvider.popPos(prev);
            }
        }
    }

    @Override
    public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
        if (this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor) {
            return ((ICrucibleAcceptor) getCoreObject()).canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
        }
        return false;
    }

    @Override
    public Mats.MaterialStack pour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
        if (this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor) {
            return ((ICrucibleAcceptor) getCoreObject()).pour(world, pos, dX, dY, dZ, side, stack);
        }
        return null;
    }

    @Override
    public boolean canAcceptPartialFlow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
        if (this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor) {
            return ((ICrucibleAcceptor) getCoreObject()).canAcceptPartialFlow(world, pos, side, stack);
        }
        return false;
    }

    @Override
    public Mats.MaterialStack flow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
        if (this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor) {
            return ((ICrucibleAcceptor) getCoreObject()).flow(world, pos, side, stack);
        }
        return null;
    }

    @Override // please work
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        if(this.world == null) // OC is going too fast, grab from NBT!
            return componentName;
        getTile(); // ensure supportsOC is initialized
        if(supportsOC) {
            if (componentName == null || componentName.equals(CompatHandler.OCComponent.super.getComponentName())) {
                componentName = ((CompatHandler.OCComponent) this.getCoreObject()).getComponentName();
            }
            return componentName;
        }
        return CompatHandler.OCComponent.super.getComponentName();
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public boolean canConnectNode(EnumFacing side) {
        getTile();
        if(supportsOC) {
            boolean isComponent = false;
            if (this.world != null) {
                Object nodeTE = Compat.getTileStandard(this.world, this.pos.getX() + side.getXOffset(), this.pos.getY() + side.getYOffset(), this.pos.getZ() + side.getZOffset());
                if (nodeTE instanceof TileEntityProxyCombo proxy) {
                    if (proxy.getCoreObject() == this.getCoreObject()) isComponent = true;
                } else if (nodeTE == this.getCoreObject()) {
                    isComponent = true;
                }
            }
            return (this.getBlockMetadata() >= 6 && this.getBlockMetadata() <= 11)
                    && (power || fluid) &&
                    ((CompatHandler.OCComponent) this.getCoreObject()).canConnectNode(side) &&
                    !isComponent;
        }
        return CompatHandler.OCComponent.super.canConnectNode(null);
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        getTile();
        if(supportsOC)
            return ((CompatHandler.OCComponent) this.getCoreObject()).methods();
        return CompatHandler.OCComponent.super.methods();
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        getTile();
        if(supportsOC)
            return ((CompatHandler.OCComponent) this.getCoreObject()).invoke(method, context, args);
        return CompatHandler.OCComponent.super.invoke(null, null, null);
    }

    @Override
    public String[] getFunctionInfo() {
        if (getCoreObject() instanceof IRORInfo) return ((IRORInfo) getCoreObject()).getFunctionInfo();
        return new String[0];
    }

    @Override
    public String provideRORValue(String name) {
        if (getCoreObject() instanceof IRORValueProvider)
            return ((IRORValueProvider) getCoreObject()).provideRORValue(name);
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if (getCoreObject() instanceof IRORInteractive)
            return ((IRORInteractive) getCoreObject()).runRORFunction(name, params);
        return null;
    }
}
