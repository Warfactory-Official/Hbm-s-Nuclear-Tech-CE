package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.forgefluid.FFUtils;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerHeaterHeatex;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.gui.GUIHeaterHeatex;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@AutoRegister
public class TileEntityHeaterHeatex extends TileEntityMachineBase implements IHeatSource, IControlReceiver, IGUIProvider, IFluidStandardTransceiver, ITickable, IFFtoNTMF, IFluidCopiable, IConnectionAnchors {

    public FluidTankNTM[] tanksNew;
    public FluidTank[] tanks;
    public Fluid[] tankTypes;

    public int amountToCool = 24_000;
    public int tickDelay = 1;
    public int heatEnergy;
    private boolean converted;

    public TileEntityHeaterHeatex() {
        super(1, true, false);

        this.tanksNew = new FluidTankNTM[2];
        this.tanksNew[0] = new FluidTankNTM(Fluids.COOLANT_HOT, 24_000, 0).withOwner(this);
        this.tanksNew[1] = new FluidTankNTM(Fluids.COOLANT, 24_000, 1).withOwner(this);

        this.tanks = new FluidTank[2];
        this.tankTypes = new Fluid[2];

        this.tanks[0] = new FluidTank(Fluids.COOLANT_HOT.getFF(), 0, 24_000);
        this.tankTypes[0] = Fluids.COOLANT_HOT.getFF();
        this.tanks[1] = new FluidTank(Fluids.COOLANT.getFF(), 0, 24_000);
        this.tankTypes[1] = Fluids.COOLANT.getFF();

        converted = true;
    }

    @Override
    public String getDefaultName() {
        return "container.heaterHeatex";
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            if(!converted){
                convertAndSetFluids(tankTypes, tanks, tanksNew);
                converted = true;
            }
            // first, update current tank settings
            this.tanksNew[0].setType(0, inventory);
            this.setupTanks();
            this.updateConnections();

            this.heatEnergy *= 0.999;

            this.tryConvert();

            networkPackNT(25);

            for(DirPos pos : getConPos()) {
                if(this.tanksNew[1].getFill() > 0) this.sendFluid(tanksNew[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        tanksNew[0].serialize(buf);
        tanksNew[1].serialize(buf);
        buf.writeInt(heatEnergy);
        buf.writeInt(amountToCool);
        buf.writeInt(tickDelay);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        tanksNew[0].deserialize(buf);
        tanksNew[1].deserialize(buf);
        this.heatEnergy = buf.readInt();
        this.amountToCool = buf.readInt();
        this.tickDelay = buf.readInt();
    }

    protected void setupTanks() {

        if(tanksNew[0].getTankType().hasTrait(FT_Coolable.class)) {
            FT_Coolable trait = tanksNew[0].getTankType().getTrait(FT_Coolable.class);
            if(trait.getEfficiency(FT_Coolable.CoolingType.HEATEXCHANGER) > 0) {
                tanksNew[1].setTankType(trait.coolsTo);
                return;
            }
        }

        tanksNew[0].setTankType(Fluids.NONE);
        tanksNew[1].setTankType(Fluids.NONE);
    }

    protected void updateConnections() {

        for(DirPos pos : getConPos()) {
            this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    protected void tryConvert() {

        if(!tanksNew[0].getTankType().hasTrait(FT_Coolable.class)) return;
        if(tickDelay < 1) tickDelay = 1;
        if(world.getTotalWorldTime() % tickDelay != 0) return;

        FT_Coolable trait = tanksNew[0].getTankType().getTrait(FT_Coolable.class);

        int inputOps = tanksNew[0].getFill() / trait.amountReq;
        int outputOps = (tanksNew[1].getMaxFill() - tanksNew[1].getFill()) / trait.amountProduced;
        int opCap = this.amountToCool;

        int ops = Math.min(inputOps, Math.min(outputOps, opCap));
        tanksNew[0].setFill(tanksNew[0].getFill() - trait.amountReq * ops);
        tanksNew[1].setFill(tanksNew[1].getFill() + trait.amountProduced * ops);
        this.heatEnergy += (int) (trait.heatEnergy * ops * trait.getEfficiency(FT_Coolable.CoolingType.HEATEXCHANGER));
        this.markDirty();
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ, dir),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite())
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(!converted){
            if (nbt.hasKey("tanks")) {
                FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
            }
            if(nbt.hasKey("tankTypes0")) tankTypes[0] = FluidRegistry.getFluid(nbt.getString("tankTypes0"));
            if(nbt.hasKey("tankTypes1")) tankTypes[1] = FluidRegistry.getFluid(nbt.getString("tankTypes1"));
        } else {
            this.tanksNew[0].readFromNBT(nbt, "0");
            this.tanksNew[1].readFromNBT(nbt, "1");
            if (nbt.hasKey("tanks")) {
                nbt.removeTag("tanks");
                nbt.removeTag("tankTypes0");
                nbt.removeTag("tankTypes1");
            }
        }
        this.heatEnergy = nbt.getInteger("heatEnergy");
        this.amountToCool = nbt.getInteger("toCool");
        this.tickDelay = nbt.getInteger("delay");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if(!converted) {
            nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
            nbt.setString("tankTypes0", tankTypes[0].getName());
            nbt.setString("tankTypes1", tankTypes[1].getName());
        } else {
            this.tanksNew[0].writeToNBT(nbt, "0");
            this.tanksNew[1].writeToNBT(nbt, "1");
        }
        nbt.setInteger("heatEnergy", heatEnergy);
        nbt.setInteger("toCool", amountToCool);
        nbt.setInteger("delay", tickDelay);
        return nbt;
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        this.heatEnergy = Math.max(0, this.heatEnergy - heat);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanksNew;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanksNew[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanksNew[0]};
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        return dir == facing || dir == facing.getOpposite();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerHeaterHeatex(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIHeaterHeatex(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 16;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("toCool")) this.amountToCool = MathHelper.clamp(data.getInteger("toCool"), 1, tanksNew[0].getMaxFill());
        if(data.hasKey("delay")) this.tickDelay = Math.max(data.getInteger("delay"), 1);

        this.markDirty();
    }
}
