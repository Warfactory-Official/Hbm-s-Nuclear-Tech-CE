package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerICF;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.gui.GUIICF;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemICFPellet;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IFluidCopiable;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityICF extends TileEntityMachineBase implements ITickable, IGUIProvider, IFluidStandardTransceiver, SimpleComponent,
        CompatHandler.OCComponent, IFluidCopiable {

    public static final long maxHeat = 1_000_000_000_000L;
    public static final int[] io = new int[]{0, 1, 2, 3, 4, 6, 7, 8, 9, 10};
    public long laser;
    public long maxLaser;
    public long laserSync;
    public long maxLaserSync;
    public long heat;
    public long heatup;
    public int consumption;
    public int output;
    public FluidTankNTM[] tanks;
    private AxisAlignedBB bb = null;

    public TileEntityICF() {
        super(12, true, false);
        this.tanks = new FluidTankNTM[3];
        this.tanks[0] = new FluidTankNTM(Fluids.SODIUM, 512_000);
        this.tanks[1] = new FluidTankNTM(Fluids.SODIUM_HOT, 512_000);
        this.tanks[2] = new FluidTankNTM(Fluids.STELLAR_FLUX, 24_000);
    }

    @Override
    public String getDefaultName() {
        return "container.machineICF";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            tanks[0].setType(11, inventory);

            for (DirPos pos : getConPos()) {
                this.trySubscribe(tanks[0].getTankType(), world, pos.getPos(), pos.getDir());
            }

            boolean markDirty = false;

            if (!inventory.getStackInSlot(5).isEmpty() && inventory.getStackInSlot(5).getItem() == ModItems.icf_pellet_depleted) {
                for (int i = 6; i < 11; i++) {
                    if (inventory.getStackInSlot(i).isEmpty()) {
                        inventory.setStackInSlot(i, inventory.getStackInSlot(5).copy());
                        inventory.setStackInSlot(5, ItemStack.EMPTY);
                        markDirty = true;
                        break;
                    }
                }
            }

            //insert fresh pellet
            if (inventory.getStackInSlot(5).isEmpty()) {
                for (int i = 0; i < 5; i++) {
                    if (!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() == ModItems.icf_pellet) {
                        inventory.setStackInSlot(5, inventory.getStackInSlot(i).copy());
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                        markDirty = true;
                        break;
                    }
                }
            }

            this.heatup = 0;

            if (!inventory.getStackInSlot(5).isEmpty() && inventory.getStackInSlot(5).getItem() == ModItems.icf_pellet) {
                if (ItemICFPellet.getFusingDifficulty(inventory.getStackInSlot(5)) <= this.laser) {
                    ItemStack slot5 = inventory.getStackInSlot(5).copy();
                    this.heatup = ItemICFPellet.react(slot5, this.laser);
                    inventory.setStackInSlot(5, slot5);
                    this.heat += heatup;
                    if (ItemICFPellet.getDepletion(inventory.getStackInSlot(5)) >= ItemICFPellet.getMaxDepletion(inventory.getStackInSlot(5))) {
                        inventory.setStackInSlot(5, new ItemStack(ModItems.icf_pellet_depleted));
                        markDirty = true;
                    }

                    tanks[2].setFill(tanks[2].getFill() + (int) Math.ceil(this.heat * 10.0D / maxHeat));
                    if (tanks[2].getFill() > tanks[2].getMaxFill()) tanks[2].setFill(tanks[2].getMaxFill());

                    NBTTagCompound dPart = new NBTTagCompound();
                    dPart.setString("type", "hadron");
                    PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, getPos()),
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 25));
                }
            }

            if (heatup == 0) {
                this.heat += this.laser * 0.25D;
            }

            this.consumption = 0;
            this.output = 0;

            if (tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
                FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
                HeatingStep step = trait.getFirstStep();
                tanks[1].setTankType(step.typeProduced);

                int coolingCycles = tanks[0].getFill() / step.amountReq;
                int heatingCycles = (tanks[1].getMaxFill() - tanks[1].getFill()) / step.amountProduced;
                int heatCycles = (int) Math.min(this.heat / 4D / step.heatReq * trait.getEfficiency(HeatingType.ICF),
                        (double) this.heat / step.heatReq); //25% cooling per tick
                int cycles = Math.min(coolingCycles, Math.min(heatingCycles, heatCycles));

                tanks[0].setFill(tanks[0].getFill() - step.amountReq * cycles);
                tanks[1].setFill(tanks[1].getFill() + step.amountProduced * cycles);
                this.heat -= (long) step.heatReq * cycles;

                this.consumption = step.amountReq * cycles;
                this.output = step.amountProduced * cycles;
            }

            for (DirPos pos : getConPos()) {
                this.sendFluid(tanks[1], world, pos.getPos(), pos.getDir());
                this.sendFluid(tanks[2], world, pos.getPos(), pos.getDir());
            }

            this.heat *= 0.999D;
            if (this.heat > maxHeat) this.heat = maxHeat;
            if (markDirty) this.markDirty();
            laserSync = laser;
            maxLaserSync = maxLaser;
            this.networkPackNT(150);
            this.laser = 0;
            this.maxLaser = 0;
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        int xCoord = getPos().getX(), yCoord = getPos().getY(), zCoord = getPos().getZ();
        return new DirPos[]{new DirPos(xCoord, yCoord + 6, zCoord, Library.POS_Y), new DirPos(xCoord, yCoord - 1, zCoord, Library.NEG_Y),
                new DirPos(xCoord + dir.offsetX * 3 + rot.offsetX * 6, yCoord + 3, zCoord + dir.offsetZ * 3 + rot.offsetZ * 6, dir),
                new DirPos(xCoord + dir.offsetX * 3 - rot.offsetX * 6, yCoord + 3, zCoord + dir.offsetZ * 3 - rot.offsetZ * 6, dir),
                new DirPos(xCoord - dir.offsetX * 3 + rot.offsetX * 6, yCoord + 3, zCoord - dir.offsetZ * 3 + rot.offsetZ * 6, dir.getOpposite()),
                new DirPos(xCoord - dir.offsetX * 3 - rot.offsetX * 6, yCoord + 3, zCoord - dir.offsetZ * 3 - rot.offsetZ * 6, dir.getOpposite())};
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(laserSync);
        buf.writeLong(maxLaserSync);
        buf.writeLong(heat);
        for (int i = 0; i < 3; i++) tanks[i].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.laser = buf.readLong();
        this.maxLaser = buf.readLong();
        this.heat = buf.readLong();
        for (int i = 0; i < 3; i++) tanks[i].deserialize(buf);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot < 5 && stack.getItem() == ModItems.icf_pellet;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot > 5;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return io;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        for (int i = 0; i < 3; i++) tanks[i].readFromNBT(nbt, "t" + i);
        this.heat = nbt.getLong("heat");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for (int i = 0; i < 3; i++) tanks[i].writeToNBT(nbt, "t" + i);
        nbt.setLong("heat", heat);
        return nbt;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(getPos()) <= 256;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            int xCoord = getPos().getX(), yCoord = getPos().getY(), zCoord = getPos().getZ();
            bb = new AxisAlignedBB(xCoord + 0.5 - 8, yCoord, zCoord + 0.5 - 8, xCoord + 0.5 + 9, yCoord + 0.5 + 5, zCoord + 0.5 + 9);
        }
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1], tanks[2]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerICF(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIICF(player.inventory, this);
    }

    //OC stuff

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_icf_reactor";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getHeat(Context context, Arguments args) {
        return new Object[]{this.heat};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getHeatingRate(Context context, Arguments args) {
        return new Object[]{this.heatup};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getMaxHeat(Context context, Arguments args) {
        return new Object[]{maxHeat};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPower(Context context, Arguments args) {
        return new Object[]{this.laser};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFluid(Context context, Arguments args) {
        return new Object[]{tanks[0].getFill(), tanks[0].getMaxFill(), tanks[0].getTankType().getTranslationKey(), tanks[1].getFill(),
                tanks[1].getMaxFill(), tanks[1].getTankType().getTranslationKey(), tanks[2].getFill(), tanks[2].getMaxFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPelletStats(Context context, Arguments args) {
        return new Object[]{ItemICFPellet.getDepletion(inventory.getStackInSlot(5)), ItemICFPellet.getMaxDepletion(inventory.getStackInSlot(5)),
                ItemICFPellet.getFusingDifficulty(inventory.getStackInSlot(5)), ItemICFPellet.getType(inventory.getStackInSlot(5), true).name(),
                ItemICFPellet.getType(inventory.getStackInSlot(5), false).name()};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{"getHeat", "getHeatingRate", "getMaxHeat", "getPower", "getFluid", "getPelletStats"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getHeat" -> getHeat(context, args);
            case "getHeatingRate" -> getHeatingRate(context, args);
            case "getMaxHeat" -> getMaxHeat(context, args);
            case "getPower" -> getPower(context, args);
            case "getFluid" -> getFluid(context, args);
            case "getPelletStats" -> getPelletStats(context, args);
            default -> throw new NoSuchMethodException();
        };
    }
}
