package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineVacuumDistill;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineVacuumDistill;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineVacuumDistill extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IPersistentNBT, IGUIProvider, IFluidCopiable {

    public long power;
    public static final long maxPower = 1_000_000;

    public FluidTankNTM[] tanks;

    private AudioWrapper audio;
    private int audioTime;
    public boolean isOn;

    public TileEntityMachineVacuumDistill() {
        super(10, true, true);

        this.tanks = new FluidTankNTM[5];
        this.tanks[0] = new FluidTankNTM(Fluids.OIL, 64_000).withPressure(2);
        this.tanks[1] = new FluidTankNTM(Fluids.HEAVYOIL_VACUUM, 24_000);
        this.tanks[2] = new FluidTankNTM(Fluids.REFORMATE, 24_000);
        this.tanks[3] = new FluidTankNTM(Fluids.LIGHTOIL_VACUUM, 24_000);
        this.tanks[4] = new FluidTankNTM(Fluids.SOURGAS, 24_000);
    }

    @Override
    public String getDefaultName() {
        return "container.vacuumDistill";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.isOn = false;

            this.updateConnections();
            power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            tanks[0].setType(9, inventory);

            refine();

            tanks[1].unloadTank(1, 2, inventory);
            tanks[2].unloadTank(3, 4, inventory);
            tanks[3].unloadTank(5, 6, inventory);
            tanks[4].unloadTank(7, 8, inventory);

            for(DirPos pos : getConPos()) {
                for(int i = 1; i < 5; i++) {
                    if(tanks[i].getFill() > 0) {
                        this.sendFluid(tanks[i], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    }
                }
            }

            networkPackNT(150);
        } else {

            if(this.isOn) audioTime = 20;

            if(audioTime > 0) {

                audioTime--;

                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.updateVolume(getVolume(1F));
                audio.keepAlive();

            } else {

                if(audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.boiler, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.25F, 15F, 1.0F, 20);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {

        super.invalidate();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        for (FluidTankNTM tank : tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        for (FluidTankNTM tank : tanks) tank.deserialize(buf);
    }

    private void refine() {
        Tuple.Quartet<FluidStack, FluidStack, FluidStack, FluidStack> refinery = RefineryRecipes.getVacuum(tanks[0].getTankType());
        if(refinery == null) {
            for(int i = 1; i < 5; i++) tanks[i].setTankType(Fluids.NONE);
            return;
        }

        FluidStack[] stacks = new FluidStack[] {refinery.getW(), refinery.getX(), refinery.getY(), refinery.getZ()};
        for(int i = 0; i < stacks.length; i++) tanks[i + 1].setTankType(stacks[i].type);

        if(power < 10_000) return;
        if(tanks[0].getFill() < 100) return;
        for(int i = 0; i < stacks.length; i++) if(tanks[i + 1].getFill() + stacks[i].fill > tanks[i + 1].getMaxFill()) return;

        this.isOn = true;
        power -= 10_000;
        tanks[0].setFill(tanks[0].getFill() - 100);

        for(int i = 0; i < stacks.length; i++) tanks[i + 1].setFill(tanks[i + 1].getFill() + stacks[i].fill);
    }

    private void updateConnections() {
        for(DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        power = nbt.getLong("power");
        tanks[0].readFromNBT(nbt, "input");
        tanks[1].readFromNBT(nbt, "heavy");
        tanks[2].readFromNBT(nbt, "reformate");
        tanks[3].readFromNBT(nbt, "light");
        tanks[4].readFromNBT(nbt, "gas");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        tanks[0].writeToNBT(nbt, "input");
        tanks[1].writeToNBT(nbt, "heavy");
        tanks[2].writeToNBT(nbt, "reformate");
        tanks[3].writeToNBT(nbt, "light");
        tanks[4].writeToNBT(nbt, "gas");

        return super.writeToNBT(nbt);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 9,
                    pos.getZ() + 2
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanks[1], tanks[2], tanks[3], tanks[4]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanks[0]};
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if(tanks[0].getFill() == 0 && tanks[1].getFill() == 0 && tanks[2].getFill() == 0 && tanks[3].getFill() == 0 && tanks[4].getFill() == 0) return;
        NBTTagCompound data = new NBTTagCompound();
        for(int i = 0; i < 5; i++) this.tanks[i].writeToNBT(data, "" + i);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        for(int i = 0; i < 5; i++) this.tanks[i].readFromNBT(data, "" + i);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineVacuumDistill(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineVacuumDistill(player.inventory, this);
    }
    // Th3_Sl1ze: maybe I should just change the fucking value in TileEntityMachineBase..
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 1024.0D;
    }
}
