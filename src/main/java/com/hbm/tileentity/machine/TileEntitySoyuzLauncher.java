package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.entity.missile.EntitySoyuz;
import com.hbm.handler.MissileStruct;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerSoyuzLauncher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUISoyuzLauncher;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemSoyuz;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntitySoyuzLauncher extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider {

    public static final long maxPower = 1000000;
    public static final int maxCount = 600;
    public long power;
    public FluidTankNTM[] tanks;
    //0: sat, 1: cargo
    public byte mode;
    public boolean starting;
    public int countdown;
    public byte rocketType = -1;
    public MissileStruct load;
    protected List<DirPos> conPos;
    private AudioWrapper audio;

    public TileEntitySoyuzLauncher() {
        super(27, true, true);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.KEROSENE, 128000);
        tanks[1] = new FluidTankNTM(Fluids.OXYGEN, 128000);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.soyuzReady, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 1.0F);
    }

    @Override
    public String getDefaultName() {
        return "container.soyuzLauncher";
    }

    @Override
    public void update() {


        if (!world.isRemote) {

            if (world.getTotalWorldTime() % 20 == 0) {
                for (DirPos pos : getConPos()) {
                    this.trySubscribe(world, pos.getPos(), pos.getDir());
                    this.trySubscribe(tanks[0].getTankType(), world, pos.getPos(), pos.getDir());
                    this.trySubscribe(tanks[1].getTankType(), world, pos.getPos(), pos.getDir());
                }
            }

            tanks[0].loadTank(4, 5, inventory);
            tanks[1].loadTank(6, 7, inventory);

            power = Library.chargeTEFromItems(inventory, 8, power, maxPower);

            if (!starting || !canLaunch()) {
                countdown = maxCount;
                starting = false;
            } else if (countdown > 0) {
                countdown--;

                if (countdown % 100 == 0 && countdown > 0)
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.alarmHatch, SoundCategory.BLOCKS, 100F, 1.1F);

            } else {
                liftOff();
            }

            networkPackNT(250);
        }

        if (world.isRemote) {
            if (!starting || !canLaunch()) {

                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }

                countdown = maxCount;

            } else if (countdown > 0) {

                if (audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.soyuzReady, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 2.0F, 100F, 1.0F);
                    audio.updateVolume(100);
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                countdown--;
            }

            List<EntitySoyuz> entities = world.getEntitiesWithinAABB(EntitySoyuz.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));

            if (!entities.isEmpty()) {

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "smoke");
                data.setString("mode", "shockRand");
                data.setInteger("count", 50);
                data.setDouble("strength", world.rand.nextGaussian() * 3 + 6);
                data.setDouble("posX", pos.getX() + 0.5);
                data.setDouble("posY", pos.getY() - 3);
                data.setDouble("posZ", pos.getZ() + 0.5);

                MainRegistry.proxy.effectNT(data);
            }
        }

    }

    protected List<DirPos> getConPos() {

        if (conPos != null)
            return conPos;

        conPos = new ArrayList<>();

        for (ForgeDirection dir : new ForgeDirection[]{Library.POS_X, Library.POS_Z, Library.NEG_X, Library.NEG_Z}) {
            ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

            for (int i = -6; i <= 6; i++) {
                conPos.add(new DirPos(pos.getX() + dir.offsetX * 7 + rot.offsetX * i, pos.getY() + 0, pos.getZ() + dir.offsetZ * 7 + rot.offsetZ * i, dir));
                conPos.add(new DirPos(pos.getX() + dir.offsetX * 7 + rot.offsetX * i, pos.getY() - 1, pos.getZ() + dir.offsetZ * 7 + rot.offsetZ * i, dir));
            }
        }

        return conPos;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeByte(mode);
        buf.writeBoolean(starting);
        buf.writeByte(this.getType());
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        mode = buf.readByte();
        starting = buf.readBoolean();
        rocketType = buf.readByte();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    public void startCountdown() {

        if (canLaunch())
            starting = true;
    }

    public void liftOff() {

        this.starting = false;

        int req = this.getFuelRequired();
        int pow = this.getPowerRequired();

        EntitySoyuz soyuz = new EntitySoyuz(world);
        soyuz.setSkin(this.getType());
        soyuz.mode = this.mode;
        soyuz.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
        world.spawnEntity(soyuz);

        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.soyuzTakeOff, SoundCategory.BLOCKS, 100F, 1.1F);

        tanks[0].drain(req, true);
        tanks[1].drain(req, true);
        power -= pow;

        if (mode == 0) {
            soyuz.setSat(inventory.getStackInSlot(2));

            if (this.orbital() == 2)
                inventory.setStackInSlot(3, ItemStack.EMPTY);

            inventory.setStackInSlot(2, ItemStack.EMPTY);
        }

        if (mode == 1) {
            List<ItemStack> payload = new ArrayList<ItemStack>();

            for (int i = 9; i < 27; i++) {
                payload.add(inventory.getStackInSlot(i));
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }

            soyuz.targetX = inventory.getStackInSlot(1).getTagCompound().getInteger("pos.getX()");
            soyuz.targetZ = inventory.getStackInSlot(1).getTagCompound().getInteger("pos.getZ()");
            soyuz.setPayload(payload);
        }

        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public boolean canLaunch() {

        return hasRocket() && hasFuel() && hasRocket() && hasPower() && designator() != 1 && orbital() != 1 && satellite() != 1;
    }

    public boolean hasFuel() {

        return tanks[0].getFluidAmount() >= getFuelRequired();
    }

    public boolean hasOxy() {

        return tanks[1].getFluidAmount() >= getFuelRequired();
    }

    public int getFuelRequired() {

        if (mode == 1)
            return 20000 + getDist();

        return 128000;
    }

    public int getDist() {

        if (designator() == 2) {
            int x = inventory.getStackInSlot(1).getTagCompound().getInteger("pos.getX()");
            int z = inventory.getStackInSlot(1).getTagCompound().getInteger("pos.getZ()");

            return (int) Vec3.createVectorHelper(pos.getX() - x, 0, pos.getZ() - z).length();
        }

        return 0;
    }

    public boolean hasPower() {

        return power >= getPowerRequired();
    }

    public int getPowerRequired() {

        return (int) (maxPower * 0.75);
    }

    private byte getType() {
        if(!hasRocket())
            return -1;

        return (byte) inventory.getStackInSlot(0).getItemDamage();
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public boolean hasRocket() {
        return inventory.getStackInSlot(0).getItem() instanceof ItemSoyuz;
    }

    //0: designator not required
    //1: designator required but not present
    //2: designator present
    public int designator() {

        if (mode == 0)
            return 0;
        if ((inventory.getStackInSlot(1).getItem() == ModItems.designator || inventory.getStackInSlot(1).getItem() == ModItems.designator_range || inventory.getStackInSlot(1).getItem() == ModItems.designator_manual) && inventory.getStackInSlot(1).hasTagCompound())
            return 2;
        return 1;
    }

    //0: sat not required
    //1: sat required but not present
    //2: sat present
    public int satellite() {

        if (mode == 1)
            return 0;

        if (!inventory.getStackInSlot(2).isEmpty()) {
            return 2;
        }
        return 1;
    }

    //0: module not required
    //1: module required but not present
    //2: module present
    public int orbital() {

        if (mode == 1)
            return 0;

        if (inventory.getStackInSlot(2).getItem() == ModItems.sat_gerald) {
            if (inventory.getStackInSlot(3).getItem() == ModItems.missile_soyuz_lander)
                return 2;
            return 1;
        }
        return 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        power = compound.getLong("power");
        mode = compound.getByte("mode");
        tanks[0].readFromNBT(compound, "tank0");
        tanks[1].readFromNBT(compound, "tank1");
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setByte("mode", mode);
        tanks[0].writeToNBT(compound, "tank0");
        tanks[1].writeToNBT(compound, "tank1");
        return super.writeToNBT(compound);
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
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long i) {
        this.power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return null;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerSoyuzLauncher(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUISoyuzLauncher(player.inventory, this);
    }
}
