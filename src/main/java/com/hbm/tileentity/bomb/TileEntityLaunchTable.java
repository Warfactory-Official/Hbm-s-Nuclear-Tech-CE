package com.hbm.tileentity.bomb;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.MissileStruct;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerLaunchTable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineLaunchTable;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.ModContext;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEMissileMultipartPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityLaunchTable extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IBufPacketReceiver, IFluidStandardReceiver, SimpleComponent, IGUIProvider, IConnectionAnchors {

    public static final long maxPower = 100000;
    public static final int maxSolid = 100000;
    public static final int clearingDuration = 100;

    private AxisAlignedBB bb;
    public long power;
    public int solid;
    public FluidTankNTM[] tanks;
    public PartSize padSize;
    public int clearingTimer = 0;
    public MissileStruct load;
    public int height;

    public int redstonePower = 0;
    private int prevRedstonePower = 0;

    private ItemStack lastMissileStack = ItemStack.EMPTY;

    public TileEntityLaunchTable() {
        super(8, true, true);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.NONE, 100000).withOwner(this);
        tanks[1] = new FluidTankNTM(Fluids.NONE, 100000).withOwner(this);
        padSize = PartSize.SIZE_10;
    }

    public static MissileStruct getStruct(ItemStack stack) {
        return ItemCustomMissile.getStruct(stack);
    }

    @Override
    public String getDefaultName() {
        return "container.launchTable";
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public int getSolidScaled(int i) {
        return (solid * i) / maxSolid;
    }

    @Override
    public void update() {
        ItemStack currentMissile = this.inventory.getStackInSlot(0);
        if (!ItemStack.areItemStacksEqual(currentMissile, this.lastMissileStack)) {
            this.lastMissileStack = currentMissile.copy();
            this.updateTypes();
        }

        if (!world.isRemote) {
            if (clearingTimer > 0) clearingTimer--;

            if (world.getTotalWorldTime() % 20 == 0) this.updateConnections();

            tanks[0].loadTank(2, 6, inventory);
            tanks[1].loadTank(3, 7, inventory);

            power = Library.chargeTEFromItems(inventory, 5, power, maxPower);

            if (!inventory.getStackInSlot(4).isEmpty() && inventory.getStackInSlot(4).getItem() == ModItems.rocket_fuel && solid + 250 <= maxSolid) {
                inventory.getStackInSlot(4).shrink(1);
                if (inventory.getStackInSlot(4).isEmpty()) inventory.setStackInSlot(4, ItemStack.EMPTY);
                solid += 250;
            }

            MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
            if (multipart != null) {
                PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, multipart), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 200));
            } else
                PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, new MissileStruct()), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 200));

            networkPackNT(20);

            this.prevRedstonePower = this.redstonePower;
            this.redstonePower = 0;
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (world.isBlockPowered(pos.add(x, 0, z))) {
                        this.redstonePower++;
                    }
                }
            }

            // Launch only when signal goes from off to on
            if (this.redstonePower > 0 && this.prevRedstonePower <= 0 && canLaunch()) {
                launch();
            }

        } else {
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));
            for (Entity e : entities) {
                if (e instanceof EntityMissileCustom) {
                    for (int i = 0; i < 15; i++)
                        MainRegistry.proxy.spawnParticle(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, "launchsmoke", null);
                    break;
                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(solid);
        buf.writeInt(padSize.ordinal());
        buf.writeInt(clearingTimer);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        solid = buf.readInt();
        padSize = PartSize.values()[buf.readInt()];
        clearingTimer = buf.readInt();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    @Override
    public DirPos[] getConPos() {
        DirPos[] result = new DirPos[9 * 4];
        int idx = 0;
        for (int i = -4; i <= 4; i++) {
            result[idx++] = new DirPos(pos.getX() + 5, pos.getY(), pos.getZ() + i, ForgeDirection.EAST);
            result[idx++] = new DirPos(pos.getX() - 5, pos.getY(), pos.getZ() + i, ForgeDirection.WEST);
            result[idx++] = new DirPos(pos.getX() + i, pos.getY(), pos.getZ() + 5, ForgeDirection.SOUTH);
            result[idx++] = new DirPos(pos.getX() + i, pos.getY(), pos.getZ() - 5, ForgeDirection.NORTH);
        }
        return result;
    }

    private void updateConnections() {
        for (int i = -4; i <= 4; i++) {
            this.trySubscribe(world, pos.getX() + 5, pos.getY(), pos.getZ() + i, ForgeDirection.EAST);
            this.trySubscribe(tanks[0].getTankType(), world, pos.getX() + 5, pos.getY(), pos.getZ() + i, ForgeDirection.EAST);
            this.trySubscribe(tanks[1].getTankType(), world, pos.getX() + 5, pos.getY(), pos.getZ() + i, ForgeDirection.EAST);

            this.trySubscribe(world, pos.getX() - 5, pos.getY(), pos.getZ() + i, ForgeDirection.WEST);
            this.trySubscribe(tanks[0].getTankType(), world, pos.getX() - 5, pos.getY(), pos.getZ() + i, ForgeDirection.WEST);
            this.trySubscribe(tanks[1].getTankType(), world, pos.getX() - 5, pos.getY(), pos.getZ() + i, ForgeDirection.WEST);

            this.trySubscribe(world, pos.getX() + i, pos.getY(), pos.getZ() + 5, ForgeDirection.SOUTH);
            this.trySubscribe(tanks[0].getTankType(), world, pos.getX() + i, pos.getY(), pos.getZ() + 5, ForgeDirection.SOUTH);
            this.trySubscribe(tanks[1].getTankType(), world, pos.getX() + i, pos.getY(), pos.getZ() + 5, ForgeDirection.SOUTH);

            this.trySubscribe(world, pos.getX() + i, pos.getY(), pos.getZ() - 5, ForgeDirection.NORTH);
            this.trySubscribe(tanks[0].getTankType(), world, pos.getX() + i, pos.getY(), pos.getZ() - 5, ForgeDirection.NORTH);
            this.trySubscribe(tanks[1].getTankType(), world, pos.getX() + i, pos.getY(), pos.getZ() - 5, ForgeDirection.NORTH);
        }
    }

    public boolean canLaunch() {
        return power >= maxPower * 0.75 && isMissileValid() && hasDesignator() && hasFuel() && clearingTimer == 0;
    }

    public void launch() {
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.missileTakeoff, SoundCategory.BLOCKS, 10.0F, 1.0F);

        int tX = inventory.getStackInSlot(1).getTagCompound().getInteger("xCoord");
        int tZ = inventory.getStackInSlot(1).getTagCompound().getInteger("zCoord");

        EntityMissileCustom missile = new EntityMissileCustom(world, pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F, tX, tZ, getStruct(inventory.getStackInSlot(0)));
        if (ModContext.DETONATOR_CONTEXT.get() instanceof EntityLivingBase entityLivingBase)
            missile.setThrower(entityLivingBase);
        world.spawnEntity(missile);

        subtractFuel();
        clearingTimer = clearingDuration;
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    private boolean hasFuel() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return false;

        ItemMissile fuselage = multipart.fuselage;
        float requiredFuel = (Float) fuselage.attributes[1];
        FuelType fuelType = (FuelType) fuselage.attributes[0];

        return switch (fuelType) {
            case SOLID -> this.solid >= requiredFuel;
            case KEROSENE, HYDROGEN, BALEFIRE ->
                    tanks[0].getFill() >= requiredFuel && tanks[1].getFill() >= requiredFuel;
            case XENON -> tanks[0].getFill() >= requiredFuel;
            default -> false;
        };
    }

    private void subtractFuel() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return;

        ItemMissile fuselage = multipart.fuselage;
        int fuel = (int) (float) fuselage.attributes[1];

        switch ((FuelType) fuselage.attributes[0]) {
            case KEROSENE:
            case HYDROGEN:
            case BALEFIRE:
                tanks[0].setFill(tanks[0].getFill() - fuel);
                tanks[1].setFill(tanks[1].getFill() - fuel);
                break;
            case XENON:
                tanks[0].setFill(tanks[0].getFill() - fuel);
                break;
            case SOLID:
                this.solid -= fuel;
                break;
            default:
                break;
        }
        this.power -= (long) (maxPower * 0.75);
    }

    public boolean isMissileValid() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return false;
        return multipart.fuselage.top == padSize;
    }

    public boolean hasDesignator() {
        ItemStack designator = inventory.getStackInSlot(1);
        if (!designator.isEmpty()) {
            Item item = designator.getItem();
            return (item == ModItems.designator || item == ModItems.designator_range || item == ModItems.designator_manual) && designator.hasTagCompound();
        }
        return false;
    }

    public void updateTypes() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));

        if (multipart == null || multipart.fuselage == null) {
            tanks[0].setTankType(Fluids.NONE);
            tanks[1].setTankType(Fluids.NONE);
            return;
        }

        ItemMissile fuselage = multipart.fuselage;
        switch ((FuelType) fuselage.attributes[0]) {
            case KEROSENE:
                tanks[0].setTankType(Fluids.KEROSENE);
                tanks[1].setTankType(Fluids.PEROXIDE);
                break;
            case HYDROGEN:
                tanks[0].setTankType(Fluids.HYDROGEN);
                tanks[1].setTankType(Fluids.OXYGEN);
                break;
            case XENON:
                tanks[0].setTankType(Fluids.XENON);
                tanks[1].setTankType(Fluids.NONE);
                break;
            case BALEFIRE:
                tanks[0].setTankType(Fluids.BALEFIRE);
                tanks[1].setTankType(Fluids.PEROXIDE);
                break;
            default:
                tanks[0].setTankType(Fluids.NONE);
                tanks[1].setTankType(Fluids.NONE);
                break;
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tanks[0].writeToNBT(nbt, "tank0");
        tanks[1].writeToNBT(nbt, "tank1");
        nbt.setInteger("solidfuel", solid);
        nbt.setLong("power", power);
        nbt.setInteger("padSize", padSize.ordinal());
        nbt.setInteger("redstonePower", this.redstonePower);
        nbt.setInteger("prevRedstonePower", this.prevRedstonePower);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tanks[0].readFromNBT(nbt, "tank0");
        this.tanks[1].readFromNBT(nbt, "tank1");
        this.solid = nbt.getInteger("solidfuel");
        this.power = nbt.getLong("power");
        this.padSize = PartSize.values()[nbt.getInteger("padSize")];
        this.redstonePower = nbt.getInteger("redstonePower");
        this.prevRedstonePower = nbt.getInteger("prevRedstonePower");
    }

    @NotNull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX() - 5, pos.getY(), pos.getZ() - 5, pos.getX() + 6, pos.getY() + 20, pos.getZ() + 6);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
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
        return TileEntityLaunchTable.maxPower;
    }


    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return this.tanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return this.tanks;
    }

    public boolean setCoords(int x, int z) {
        ItemStack designator = inventory.getStackInSlot(1);
        if (!designator.isEmpty() && (designator.getItem() == ModItems.designator || designator.getItem() == ModItems.designator_range || designator.getItem() == ModItems.designator_manual)) {
            NBTTagCompound nbt = designator.hasTagCompound() ? designator.getTagCompound() : new NBTTagCompound();
            nbt.setInteger("xCoord", x);
            nbt.setInteger("zCoord", z);
            designator.setTagCompound(nbt);
            return true;
        }
        return false;
    }

    public int liquidState() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return -1;

        ItemMissile fuselage = multipart.fuselage;
        float requiredFuel = (Float) fuselage.attributes[1];
        FuelType fuelType = (FuelType) fuselage.attributes[0];

        return switch (fuelType) {
            case KEROSENE, HYDROGEN, XENON, BALEFIRE -> tanks[0].getFill() >= requiredFuel ? 1 : 0;
            default -> -1;
        };
    }

    public int oxidizerState() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return -1;

        ItemMissile fuselage = multipart.fuselage;
        float requiredFuel = (Float) fuselage.attributes[1];
        FuelType fuelType = (FuelType) fuselage.attributes[0];

        return switch (fuelType) {
            case KEROSENE, HYDROGEN, BALEFIRE -> tanks[1].getFill() >= requiredFuel ? 1 : 0;
            default -> -1;
        };
    }

    public int solidState() {
        MissileStruct multipart = getStruct(inventory.getStackInSlot(0));
        if (multipart == null || multipart.fuselage == null) return -1;

        ItemMissile fuselage = multipart.fuselage;
        float requiredFuel = (Float) fuselage.attributes[1];
        FuelType fuelType = (FuelType) fuselage.attributes[0];

        if (fuelType == FuelType.SOLID) {
            return this.solid >= requiredFuel ? 1 : 0;
        }

        return -1;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerLaunchTable(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineLaunchTable(player.inventory, this);
    }

    // --- OpenComputers Interface ---

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "launchtable";
    }

    @Callback(doc = "setTarget(x:integer, z:integer) -- Sets the target coordinates in the designator. Returns true on success.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setTarget(Context context, Arguments args) {
        int x = args.checkInteger(0);
        int z = args.checkInteger(1);
        return new Object[]{setCoords(x, z)};
    }

    @Callback(doc = "launch() -- Tries to launch the missile. Returns true on success, or false and a reason on failure.")
    @Optional.Method(modid = "opencomputers")
    public Object[] launch(Context context, Arguments args) {
        if (!isMissileValid()) return new Object[]{false, "Invalid or no missile."};
        if (!hasDesignator()) return new Object[]{false, "Missing or unlinked designator."};
        if (power < maxPower * 0.75) return new Object[]{false, "Insufficient power."};
        if (!hasFuel()) return new Object[]{false, "Insufficient fuel."};
        if (clearingTimer > 0) return new Object[]{false, "Launch pad is cooling down."};
        launch();
        return new Object[]{true};
    }

    @Callback(doc = "getPower() -- Returns current and maximum stored power.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPower(Context context, Arguments args) {
        return new Object[]{this.power, maxPower};
    }

    @Callback(doc = "getSolidFuel() -- Returns current and maximum solid fuel.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSolidFuel(Context context, Arguments args) {
        return new Object[]{this.solid, maxSolid};
    }

    @Callback(doc = "getLiquidFuel() -- Returns fuel tank info: current amount, max capacity, and fluid name.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getLiquidFuel(Context context, Arguments args) {
        return new Object[]{tanks[0].getFill(), tanks[0].getMaxFill(), tanks[0].getTankType().getTranslationKey()};
    }

    @Callback(doc = "getOxidizer() -- Returns oxidizer tank info: current amount, max capacity, and fluid name.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getOxidizer(Context context, Arguments args) {
        return new Object[]{tanks[1].getFill(), tanks[1].getMaxFill(), tanks[1].getTankType().getTranslationKey()};
    }

    @Callback(doc = "canLaunch() -- Returns true if all conditions for launch are met.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] canLaunch(Context context, Arguments args) {
        return new Object[]{canLaunch()};
    }

    @Callback(doc = "getMissileInfo() -- Returns the display name of the loaded missile.", direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getMissileInfo(Context context, Arguments args) {
        ItemStack missile = inventory.getStackInSlot(0);
        if (missile.isEmpty()) {
            return new Object[]{null, "No missile loaded."};
        }
        return new Object[]{missile.getDisplayName()};
    }
}