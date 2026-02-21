package com.hbm.tileentity.machine.storage;

import com.hbm.api.energymk2.*;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ICopiable;
import com.hbm.lib.DirPos;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public abstract class TileEntityBatteryBase extends TileEntityMachineBase implements ITickable, IEnergyConductorMK2, IEnergyProviderMK2,
        IEnergyReceiverMK2, IControlReceiver, IGUIProvider, SimpleComponent, CompatHandler.OCComponent, ICopiable {

    public byte lastRedstone = 0;
    public long prevPowerState = 0;

    public static final int mode_input = 0;
    public static final int mode_buffer = 1;
    public static final int mode_output = 2;
    public static final int mode_none = 3;
    public short redLow = 0;
    public short redHigh = 2;
    public ConnectionPriority priority = ConnectionPriority.LOW;

    protected Nodespace.PowerNode node;

    public TileEntityBatteryBase(int slotCount) {
        super(slotCount, false, true);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            if (priority == null || priority.ordinal() == 0 || priority.ordinal() == 4) {
                priority = ConnectionPriority.LOW;
            }

            if (this.node == null || this.node.expired) {
                this.node = UniNodespace.getNode(world, pos, Nodespace.THE_POWER_PROVIDER);

                if (this.node == null || this.node.expired) {
                    this.node = this.createNode();
                    UniNodespace.createNode(world, this.node);
                }
            }

            if (this.node != null && this.node.hasValidNet()) switch (this.getRelevantMode(false)) {
                case mode_input:
                    this.node.net.removeProvider(this);
                    this.node.net.addReceiver(this);
                    break;
                case mode_output:
                    this.node.net.addProvider(this);
                    this.node.net.removeReceiver(this);
                    break;
                case mode_buffer:
                    this.node.net.addProvider(this);
                    this.node.net.addReceiver(this);
                    break;
                case mode_none:
                    this.node.net.removeProvider(this);
                    this.node.net.removeReceiver(this);
                    break;
            }

            byte comp = this.getComparatorPower();
            if(comp != this.lastRedstone) {
                System.out.println(comp);
                for(BlockPos port : this.getPortPos()) {
                    TileEntity tile = Compat.getTileStandard(world, port.getX(), port.getY(), port.getZ());
                    if(tile != null) tile.markDirty();
                }
            }
            this.lastRedstone = comp;

            prevPowerState = this.getPower();

            this.networkPackNT(100);
        }
    }

    public byte getComparatorPower() {
        double frac = (double) this.getPower() / (double) this.getMaxPower() * 15D;
        return (byte) (MathHelper.clamp((int) frac + 1, 0, 15)); //to combat eventual rounding errors with the FEnSU's stupid maxPower
    }

    @Override
    public Nodespace.PowerNode createNode() {
        return new Nodespace.PowerNode(this.getPortPos()).setConnections(this.getConPos());
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (!world.isRemote) {
            if (this.node != null) {
                UniNodespace.destroyNode(world, node);
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return stack.getItem() instanceof IBatteryItem;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeShort(redLow);
        buf.writeShort(redHigh);
        buf.writeByte(priority.ordinal());
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        redLow = buf.readShort();
        redHigh = buf.readShort();
        priority = EnumUtil.grabEnumSafely(ConnectionPriority.VALUES, buf.readByte());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.redLow = nbt.getShort("redLow");
        this.redHigh = nbt.getShort("redHigh");
        this.lastRedstone = nbt.getByte("lastRedstone");
        this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.VALUES, nbt.getByte("priority"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setShort("redLow", redLow);
        nbt.setShort("redHigh", redHigh);
        nbt.setByte("lastRedstone", lastRedstone);
        nbt.setByte("priority", (byte) this.priority.ordinal());
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean allowDirectProvision() {
        return false;
    }

    @Override
    public ConnectionPriority getPriority() {
        return this.priority;
    }

    public abstract BlockPos[] getPortPos();

    public abstract DirPos[] getConPos();

    private short modeCache = 0;

    public short getRelevantMode(boolean useCache) {
        if (useCache) return this.modeCache;
        boolean powered = false;
        for (BlockPos pos : getPortPos())
            if (world.isBlockPowered(pos)) {
                powered = true;
                break;
            }
        this.modeCache = powered ? this.redHigh : this.redLow;
        return this.modeCache;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("low")) {
            this.redLow++;
            if (this.redLow > 3) this.redLow = 0;
        }
        if (data.hasKey("high")) {
            this.redHigh++;
            if (this.redHigh > 3) this.redHigh = 0;
        }
        if (data.hasKey("priority")) {
            int ordinal = this.priority.ordinal();
            ordinal++;
            if (ordinal > ConnectionPriority.HIGH.ordinal()) ordinal = ConnectionPriority.LOW.ordinal();
            this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.VALUES, ordinal);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_energy_storage";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[] {getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getModeInfo(Context context, Arguments args) {
        return new Object[] {redLow, redHigh, getPriority().ordinal()-1};
    }
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setModeLow(Context context, Arguments args) {
        short newMode = (short) args.checkInteger(0);
        if (newMode >= mode_input && newMode <= mode_none) {
            redLow = newMode;
            return new Object[] {};
        } else {
            return new Object[] {"Invalid mode"};
        }
    }
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setModeHigh(Context context, Arguments args) {
        short newMode = (short) args.checkInteger(0);
        if (newMode >= mode_input && newMode <= mode_none) {
            redHigh = newMode;
            return new Object[] {};
        } else {
            return new Object[] {"Invalid mode"};
        }
    }
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setPriority(Context context, Arguments args) {
        int newPriority = args.checkInteger(0);
        if (newPriority >= 0 && newPriority <= 2) {
            priority = EnumUtil.grabEnumSafely(ConnectionPriority.VALUES, newPriority+1);
            return new Object[] {};
        } else {
            return new Object[] {"Invalid mode"};
        }
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[] {getPower(), getMaxPower(), redLow, redHigh, getPriority().ordinal()-1};
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound data = new NBTTagCompound();
        data.setShort("redLow", redLow);
        data.setShort("redHigh", redHigh);
        data.setByte("priority", (byte) this.priority.ordinal());
        return data;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        if(nbt.hasKey("redLow")) this.redLow = nbt.getShort("redLow");
        if(nbt.hasKey("redHigh")) this.redHigh = nbt.getShort("redHigh");
        if(nbt.hasKey("priority")) this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.values(), nbt.getByte("priority"));
    }
}
