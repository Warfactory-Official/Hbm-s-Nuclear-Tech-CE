package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerChemfac;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIChemfac;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityMachineChemfac extends TileEntityMachineChemplantBase implements IGUIProvider {
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public float rot;
    public float prevRot;

    public FluidTankNTM waterNew;
    public FluidTankNTM steamNew;
    protected List<DirPos> conPos;
    float rotSpeed;
    DirPos[] inpos;
    DirPos[] outpos;
    AxisAlignedBB bb = null;

    public TileEntityMachineChemfac() {
        super(77);

        waterNew = new FluidTankNTM(Fluids.WATER, 64_000, tanksNew.length);
        steamNew = new FluidTankNTM(Fluids.SPENTSTEAM, 64_000, tanksNew.length + 1);

        inventory = new ItemStackHandler(77) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (!stack.isEmpty() && slot >= 1 && slot <= 4 && stack.getItem() instanceof ItemMachineUpgrade) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
        };
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            if (world.getTotalWorldTime() % 60 == 0) {
                for (DirPos pos : getConPos()) {
                    this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());

                    for (FluidTankNTM tank : inTanks()) {
                        if (tank.getTankType() != Fluids.NONE) {
                            this.trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                        }
                    }
                }
            }

            for (DirPos pos : getConPos())
                for (FluidTankNTM tank : outTanks()) {
                    if (tank.getTankType() != Fluids.NONE && tank.getFill() > 0) {
                        this.tryProvide(tank, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    }
                }

            this.speed = 100;
            this.consumption = 100;

            upgradeManager.checkSlots(inventory, 1, 4);
            int speedLevel = Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 6);
            int powerLevel = Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3);
            int overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.speed -= speedLevel * 15;
            this.consumption += speedLevel * 300;
            this.speed += powerLevel * 5;
            this.consumption -= powerLevel * 30;
            this.speed /= (overLevel + 1);
            this.consumption *= (overLevel + 1);

            if (this.speed <= 0) {
                this.speed = 1;
            }

            this.networkPackNT(150);
        } else {
            float maxSpeed = 30F;

            if (isProgressing) {
                rotSpeed += 0.1;

                if (rotSpeed > maxSpeed) {
                    rotSpeed = maxSpeed;
                }

                if (rotSpeed == maxSpeed && world.getTotalWorldTime() % 5 == 0) {
                    ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
                    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
                    Random rand = world.rand;

                    double x = pos.getX() + 0.5 - rot.offsetX * 0.5;
                    double y = pos.getY() + 3;
                    double z = pos.getZ() + 0.5 - rot.offsetZ * 0.5;

                    world.spawnParticle(EnumParticleTypes.CLOUD, x + dir.offsetX * 1.5 + rand.nextGaussian() * 0.2, y, z + dir.offsetZ * 1.5 + rand.nextGaussian() * 0.2, 0.0D, 0.15D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.CLOUD, x + dir.offsetX * -0.5 + rand.nextGaussian() * 0.2, y, z + dir.offsetZ * -0.5 + rand.nextGaussian() * 0.2, 0.0D, 0.15D, 0.0D);
                }
            } else {
                rotSpeed -= 0.1;

                if (rotSpeed < 0) {
                    rotSpeed = 0;
                }
            }

            prevRot = rot;
            rot += rotSpeed;

            if (rot >= 360) {
                rot -= 360;
                prevRot -= 360;
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        for (int i = 0; i < getRecipeCount(); i++) {
            buf.writeInt(progress[i]);
            buf.writeInt(maxProgress[i]);
        }

        buf.writeBoolean(isProgressing);

        for (int i = 0; i < tanksNew.length; i++) tanksNew[i].serialize(buf);

        waterNew.serialize(buf);
        steamNew.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        for (int i = 0; i < getRecipeCount(); i++) {
            progress[i] = buf.readInt();
            maxProgress[i] = buf.readInt();
        }

        isProgressing = buf.readBoolean();

        for (int i = 0; i < tanksNew.length; i++) tanksNew[i].deserialize(buf);

        waterNew.deserialize(buf);
        steamNew.deserialize(buf);
    }

    private int getWaterRequired() {
        return 1000 / this.speed;
    }

    @Override
    protected boolean canProcess(int index) {
        return super.canProcess(index) && this.waterNew.getFill() >= getWaterRequired() && this.steamNew.getFill() + getWaterRequired() <= this.steamNew.getMaxFill();
    }

    @Override
    protected void process(int index) {
        super.process(index);
        this.waterNew.setFill(this.waterNew.getFill() - getWaterRequired());
        this.steamNew.setFill(this.steamNew.getFill() + getWaterRequired());
    }

    protected List<DirPos> getConPos() {

        if (conPos != null && !conPos.isEmpty())
            return conPos;

        conPos = new ArrayList();

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        for (int i = 0; i < 6; i++) {
            conPos.add(new DirPos(pos.getX() + dir.offsetX * (3 - i) + rot.offsetX * 3, pos.getY() + 4, pos.getZ() + dir.offsetZ * (3 - i) + rot.offsetZ * 3, Library.POS_Y));
            conPos.add(new DirPos(pos.getX() + dir.offsetX * (3 - i) - rot.offsetX * 2, pos.getY() + 4, pos.getZ() + dir.offsetZ * (3 - i) - rot.offsetZ * 2, Library.POS_Y));

            for (int j = 0; j < 2; j++) {
                conPos.add(new DirPos(pos.getX() + dir.offsetX * (3 - i) + rot.offsetX * 5, pos.getY() + 1 + j, pos.getZ() + dir.offsetZ * (3 - i) + rot.offsetZ * 5, rot));
                conPos.add(new DirPos(pos.getX() + dir.offsetX * (3 - i) - rot.offsetX * 4, pos.getY() + 1 + j, pos.getZ() + dir.offsetZ * (3 - i) - rot.offsetZ * 4, rot.getOpposite()));
            }
        }

        return conPos;
    }

    @Override
    public long getMaxPower() {
        return 10_000_000;
    }

    @Override
    public String getDefaultName() {
        return "container.machineChemFac";
    }

    @Override
    public int getRecipeCount() {
        return 8;
    }

    @Override
    public int getTankCapacity() {
        return 32_000;
    }

    @Override
    public int getTemplateIndex(int index) {
        return 13 + index * 9;
    }

    @Override
    public int[] getSlotIndicesFromIndex(int index) {
        return new int[]{5 + index * 9, 8 + index * 9, 9 + index * 9, 12 + index * 9};
    }

    @Override
    public DirPos[] getInputPositions() {

        if (inpos != null)
            return inpos;

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        inpos = new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 4 - rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 1, dir),
                new DirPos(pos.getX() - dir.offsetX * 5 + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 5 + rot.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 4, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 4, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 1 + rot.offsetX * 5, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 5, rot)
        };

        return inpos;
    }

    @Override
    public DirPos[] getOutputPositions() {

        if (outpos != null)
            return outpos;

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        outpos = new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 4 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 2, dir),
                new DirPos(pos.getX() - dir.offsetX * 5 - rot.offsetX * 1, pos.getY(), pos.getZ() - dir.offsetZ * 5 - rot.offsetZ * 1, dir.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 1 - rot.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 1 - rot.offsetZ * 4, rot.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 5, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 5, rot)
        };

        return outpos;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        waterNew.readFromNBT(nbt, "w");
        steamNew.readFromNBT(nbt, "s");
        if (nbt.hasKey("water")) nbt.removeTag("water");
        if (nbt.hasKey("steam")) nbt.removeTag("steam");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        waterNew.writeToNBT(nbt, "w");
        steamNew.writeToNBT(nbt, "s");
        return nbt;
    }

    @Override
    public List<FluidTankNTM> inTanks() {

        List<FluidTankNTM> inTanks = super.inTanks();
        inTanks.add(waterNew);

        return inTanks;
    }

    @Override
    public List<FluidTankNTM> outTanks() {

        List<FluidTankNTM> outTanks = super.outTanks();
        outTanks.add(steamNew);

        return outTanks;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 5,
                    pos.getY(),
                    pos.getZ() - 5,
                    pos.getX() + 5,
                    pos.getY() + 4,
                    pos.getZ() + 5
            );
        }

        return bb;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerChemfac(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIChemfac(player.inventory, this);
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return inTanks().toArray(new FluidTankNTM[0]);
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return outTanks().toArray(new FluidTankNTM[0]);
    }
}