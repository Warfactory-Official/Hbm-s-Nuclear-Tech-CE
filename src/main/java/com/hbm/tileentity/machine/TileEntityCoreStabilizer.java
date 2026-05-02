package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCoreStabilizer;
import com.hbm.inventory.gui.GUICoreStabilizer;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemLens;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityCoreStabilizer extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, SimpleComponent, IGUIProvider, CompatHandler.OCComponent, IRORValueProvider, IRORInteractive {

    public static final long maxPower = 2500000000L;
    public static final int range = 15;
    public long power;
    public int watts;
    public int beam;
    private int prevBeam;
    public boolean isOn;

    public TileEntityCoreStabilizer() {
        super(1);

    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.updateConnections();

            watts = MathHelper.clamp(watts, 1, 100);
            int demand = (int) Math.pow(watts, 4);

            beam = 0;

            if (power >= demand && !getLensSlot().isEmpty() && getLensSlot().getItem() instanceof ItemLens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) getLensSlot().getItem()).maxDamage) {

                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
                for (int i = 1; i <= range; i++) {

                    int x = pos.getX() + dir.offsetX * i;
                    int y = pos.getY() + dir.offsetY * i;
                    int z = pos.getZ() + dir.offsetZ * i;
                    BlockPos pos = new BlockPos(x, y, z);

                    TileEntity te = world.getTileEntity(pos);

                    if (te instanceof TileEntityCore) {

                        TileEntityCore core = (TileEntityCore) te;
                        core.field = Math.max(core.field, watts);
                        this.power -= demand;
                        beam = i;

                        long dmg = ItemLens.getLensDamage(getLensSlot());
                        dmg += watts;

                        if (dmg >= ((ItemLens) ModItems.ams_lens).maxDamage)
                            inventory.setStackInSlot(0, ItemStack.EMPTY);
                        else ItemLens.setLensDamage(getLensSlot(), dmg);

                        break;
                    }

                    if (!world.isAirBlock(pos)) break;
                }
            }

            this.networkPackNT(250);
        } else {
            if (prevBeam != beam) {
                prevBeam = beam;
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    private @NotNull ItemStack getLensSlot() {
        return inventory.getStackInSlot(0);
    }

    private void updateConnections() {

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeLong(power);
        buf.writeInt(watts);
        buf.writeInt(beam);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        this.power = buf.readLong();
        this.watts = buf.readInt();
        this.beam = buf.readInt();
    }


    @Override
    public String getDefaultName() {
        return "container.dfcStabilizer";
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public int getWattsScaled(int i) {
        return (watts * i) / 100;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long i) {
        this.power = i;
    }

    @Override
    public long getMaxPower() {
        return this.maxPower;
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
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN;
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        power = compound.getLong("power");
        watts = compound.getInteger("watts");
        isOn = compound.getBoolean("isOn");
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setInteger("watts", watts);
        compound.setBoolean("isOn", isOn);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(new NTMEnergyCapabilityWrapper(this));
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[]{PREFIX_VALUE + "durability", PREFIX_VALUE + "durabilitypercent", PREFIX_FUNCTION + "setpower" + NAME_SEPARATOR + "percent",};
    }

    @Override
    public String provideRORValue(String name) {
        if ((PREFIX_VALUE + "durability").equals(name)) {
            ItemStack stack = inventory.getStackInSlot(0);
            return (!stack.isEmpty() && stack.getItem() == ModItems.ams_lens) ? "" + (((ItemLens) stack.getItem()).maxDamage - ItemLens.getLensDamage(stack)) : "0";
        }
        if ((PREFIX_VALUE + "durabilitypercent").equals(name)) {
            ItemStack stack = inventory.getStackInSlot(0);
            return (!stack.isEmpty() && stack.getItem() == ModItems.ams_lens) ? "" + ((((ItemLens) stack.getItem()).maxDamage - ItemLens.getLensDamage(stack)) * 100 / ((ItemLens) stack.getItem()).maxDamage) : "0";
        }

        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((PREFIX_FUNCTION + "setpower").equals(name) && params.length > 0) {
            this.watts = IRORInteractive.parseInt(params[0], 0, 100);
            this.markChanged();
            return null;
        }

        return null;
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "dfc_stabilizer";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInput(Context context, Arguments args) {
        return new Object[]{watts};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getDurability(Context context, Arguments args) {
        if (getLensSlot().getItem() == ModItems.ams_lens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) ModItems.ams_lens).maxDamage) {
            return new Object[]{ItemLens.getLensDamage(getLensSlot())};
        }
        return new Object[]{"N/A"};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        Object lens_damage_buf;
        if (!getLensSlot().isEmpty() && getLensSlot().getItem() == ModItems.ams_lens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) ModItems.ams_lens).maxDamage) {
            lens_damage_buf = ItemLens.getLensDamage(getLensSlot());
        } else {
            lens_damage_buf = "N/A";
        }
        return new Object[]{power, maxPower, watts, lens_damage_buf};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setInput(Context context, Arguments args) {
        int newOutput = args.checkInteger(0);
        watts = MathHelper.clamp(newOutput, 0, 100);
        return new Object[]{};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCoreStabilizer(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICoreStabilizer(player, this);
    }
}
