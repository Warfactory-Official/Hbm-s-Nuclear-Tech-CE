package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.tile.IHeatSource;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerOilburner;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.gui.GUIOilburner;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityHeaterOilburner extends TileEntityMachinePolluting implements ITickable, IGUIProvider, IHeatSource, IControlReceiver, IFluidStandardTransceiver, IFFtoNTMF, IFluidCopiable {

    public static final int maxHeatEnergy = 100_000;
    public boolean isOn = false;
    public FluidTankNTM tank;
    public int setting = 1;
    public int heatEnergy;
    AxisAlignedBB bb = null;

    public TileEntityHeaterOilburner() {
        super(3, 1000, true, false);
        tank = new FluidTankNTM(Fluids.HEATINGOIL, 16000);

    }

    public DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            tank.loadTank(0, 1, inventory);
            tank.setType(2, inventory);

            for (DirPos pos : this.getConPos()) {
                this.trySubscribe(tank.getTankType(), world, pos.getPos() , pos.getDir());
                this.sendSmoke(pos.getPos(), pos.getDir());
            }

            boolean shouldCool = true;

            if (this.isOn && this.heatEnergy < maxHeatEnergy) {

                if (tank.getTankType().hasTrait(FT_Flammable.class)) {
                    FT_Flammable type = tank.getTankType().getTrait(FT_Flammable.class);

                    int burnRate = setting;
                    int toBurn = Math.min(burnRate, tank.getFill());

                    tank.setFill(tank.getFill() - toBurn);

                    int heat = (int) (type.getHeatEnergy() / 1000);

                    this.heatEnergy += heat * toBurn;

                    if (world.getTotalWorldTime() % 5 == 0 && toBurn > 0) {
                        super.pollute(tank.getTankType(), FluidTrait.FluidReleaseType.BURN, toBurn * 5);
                    }

                    shouldCool = false;
                }
            }

            if (this.heatEnergy >= maxHeatEnergy)
                shouldCool = false;

            if (shouldCool)
                this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);

            this.networkPackNT(25);
        }

    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        tank.serialize(buf);

        buf.writeBoolean(isOn);
        buf.writeInt(heatEnergy);
        buf.writeByte((byte) this.setting);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        tank.deserialize(buf);

        isOn = buf.readBoolean();
        heatEnergy = buf.readInt();
        setting = buf.readByte();
    }

    @Override
    public String getDefaultName() {
        return "container.heaterOilburner";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt, "tank");
        isOn = nbt.getBoolean("isOn");
        heatEnergy = nbt.getInteger("heatEnergy");
        setting = nbt.getByte("setting");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        tank.writeToNBT(nbt, "tank");
        nbt.setBoolean("isOn", isOn);
        nbt.setInteger("heatEnergy", heatEnergy);
        nbt.setByte("setting", (byte) this.setting);
        return super.writeToNBT(nbt);
    }

    public void toggleSettingUp() {
        setting++;

        if (setting > 100) {
            setting = 1;
        }
    }

    public void toggleSettingDown() {
        setting--;

        if (setting < 1) {
            setting = 100;
        }
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerOilburner(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIOilburner(player.inventory, this);
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
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos) <= 256;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("toggle")) {
            this.isOn = !this.isOn;
        }

        this.markDirty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
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
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{smoke, smoke_leaded, smoke_poison};
    }
    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("fluidID", new int[]{tank.getTankType().getID()});
        tag.setInteger("burnRate", setting);
        tag.setBoolean("isOn", isOn);
        return tag;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        int id = nbt.getIntArray("fluidID")[index];
        tank.setTankType(Fluids.fromID(id));
        if(nbt.hasKey("isOn")) isOn = nbt.getBoolean("isOn");
        if(nbt.hasKey("burnRate")) setting = nbt.getInteger("burnRate");
    }
}
