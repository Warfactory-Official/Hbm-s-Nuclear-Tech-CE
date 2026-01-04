package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineRefinery;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.*;
import com.hbm.util.ParticleUtil;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityMachineRefinery extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IOverpressurable, IPersistentNBT, IRepairable, IFluidStandardTransceiver, IGUIProvider, IFluidCopiable {

    public static final int maxSulfur = 100;
    public static final long maxPower = 1000;
    public long power = 0;
    public int sulfur = 0;
    public int itemOutputTimer = 0;
    public boolean isOn;
    public FluidTankNTM[] tanks;
    public boolean hasExploded = false;
    public boolean onFire = false;
    public Explosion lastExplosion = null;
    List<RecipesCommon.AStack> repair = new ArrayList<>();
    private AudioWrapper audio;
    private int audioTime;

    public TileEntityMachineRefinery() {
        super(13, true, true);
        tanks = new FluidTankNTM[5];
        tanks[0] = new FluidTankNTM(Fluids.HOTOIL, 64_000);
        tanks[1] = new FluidTankNTM(Fluids.HEAVYOIL, 24_000);
        tanks[2] = new FluidTankNTM(Fluids.NAPHTHA, 24_000);
        tanks[3] = new FluidTankNTM(Fluids.LIGHTOIL, 24_000);
        tanks[4] = new FluidTankNTM(Fluids.PETROLEUM, 24_000);
    }

    public String getDefaultName() {
        return "container.machineRefinery";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        power = nbt.getLong("power");
        tanks[0].readFromNBT(nbt, "input");
        tanks[1].readFromNBT(nbt, "heavy");
        tanks[2].readFromNBT(nbt, "naphtha");
        tanks[3].readFromNBT(nbt, "light");
        tanks[4].readFromNBT(nbt, "petroleum");
        if (nbt.hasKey("f"))
            nbt.removeTag("f");
        if (nbt.hasKey("tanks"))
            nbt.removeTag("tanks");
        sulfur = nbt.getInteger("sulfur");
        itemOutputTimer = nbt.getInteger("itemOutputTimer");
        super.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setInteger("itemOutputTimer", itemOutputTimer);
        tanks[0].writeToNBT(nbt, "input");
        tanks[1].writeToNBT(nbt, "heavy");
        tanks[2].writeToNBT(nbt, "naphtha");
        tanks[3].writeToNBT(nbt, "light");
        tanks[4].writeToNBT(nbt, "petroleum");
        nbt.setInteger("sulfur", sulfur);
        return super.writeToNBT(nbt);
    }

    @Override
    public void update() {
        if (!world.isRemote) {

            this.isOn = false;

            if (this.getBlockMetadata() < 12) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getRotation(ForgeDirection.DOWN);
                world.removeTileEntity(pos);
                world.setBlockState(pos, ModBlocks.machine_refinery.getStateFromMeta(dir.ordinal() + 10), 3);
                MultiblockHandlerXR.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ((BlockDummyable) ModBlocks.machine_refinery).getDimensions(), ModBlocks.machine_refinery, dir);
                NBTTagCompound data = new NBTTagCompound();
                this.writeToNBT(data);
                world.getTileEntity(pos).readFromNBT(data);
                return;
            }

            if (!this.hasExploded) {

                this.updateConnections();

                power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
                tanks[0].setType(12, inventory);
                tanks[0].loadTank(1, 2, inventory);

                refine();

                tanks[1].unloadTank(3, 4, inventory);
                tanks[2].unloadTank(5, 6, inventory);
                tanks[3].unloadTank(7, 8, inventory);
                tanks[4].unloadTank(9, 10, inventory);

                for (DirPos pos : getConPos()) {
                    for (int i = 1; i < 5; i++) {
                        if (tanks[i].getFill() > 0) {
                            this.sendFluid(tanks[i], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                        }
                    }
                }
            } else if (onFire) {

                boolean hasFuel = false;
                for (int i = 0; i < 5; i++) {
                    if (tanks[i].getFill() > 0) {
                        tanks[i].setFill(Math.max(tanks[i].getFill() - 10, 0));
                        hasFuel = true;
                    }
                }

                if (hasFuel) {
                    List<Entity> affected = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-1.5, 0, -1.5), pos.add(2.5, 8, 2.5)));
                    for (Entity e : affected) e.setFire(5);
                    Random rand = world.rand;
                    ParticleUtil.spawnGasFlame(world, pos.getX() + rand.nextDouble(), pos.getY() + 1.5 + rand.nextDouble() * 3, pos.getZ() + rand.nextDouble(), rand.nextGaussian() * 0.05, 0.1, rand.nextGaussian() * 0.05);

                    if (world.getTotalWorldTime() % 20 == 0) {
                        PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 70);
                    }
                }
            }

            networkPackNT(150);
        } else {

            if (this.isOn) audioTime = 20;

            if (audioTime > 0) {

                audioTime--;

                if (audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.updateVolume(getVolume(1F));
                audio.keepAlive();

            } else {

                if (audio != null) {
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

    private void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    @Override
    public void onChunkUnload() {

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

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        for (FluidTankNTM tank : tanks) tank.serialize(buf);
        buf.writeBoolean(hasExploded);
        buf.writeBoolean(onFire);
        buf.writeBoolean(isOn);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        for (FluidTankNTM tank : tanks) tank.deserialize(buf);
        this.hasExploded = buf.readBoolean();
        this.onFire = buf.readBoolean();
        this.isOn = buf.readBoolean();
    }

    public DirPos[] getConPos() {
        return new DirPos[]{
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

    private void refine() {
        Tuple.Quintet<FluidStack, FluidStack, FluidStack, FluidStack, ItemStack> refinery = RefineryRecipes.getRefinery(tanks[0].getTankType());
        if (refinery == null) {
            for (int i = 1; i < 5; i++) tanks[i].setTankType(Fluids.NONE);
            return;
        }

        FluidStack[] stacks = new FluidStack[]{refinery.getV(), refinery.getW(), refinery.getX(), refinery.getY()};

        for (int i = 0; i < stacks.length; i++) tanks[i + 1].setTankType(stacks[i].type);

        if (power < 5 || tanks[0].getFill() < 100) return;

        for (int i = 0; i < stacks.length; i++) {
            if (tanks[i + 1].getFill() + stacks[i].fill > tanks[i + 1].getMaxFill()) {
                return;
            }
        }

        this.isOn = true;
        tanks[0].setFill(tanks[0].getFill() - 100);

        for (int i = 0; i < stacks.length; i++) tanks[i + 1].setFill(tanks[i + 1].getFill() + stacks[i].fill);

        this.sulfur++;

        if (this.sulfur >= maxSulfur) {
            this.sulfur -= maxSulfur;

            ItemStack out = refinery.getZ();

            if (out != null) {

                if (inventory.getStackInSlot(11).isEmpty()) {
                    inventory.setStackInSlot(11, out.copy());
                } else {
                    if (out.getItem() == inventory.getStackInSlot(11).getItem() && out.getItemDamage() == inventory.getStackInSlot(11).getItemDamage() && inventory.getStackInSlot(11).getCount() + out.getCount() <= inventory.getStackInSlot(11).getMaxStackSize()) {
                        ItemStack stack = inventory.getStackInSlot(11).copy();
                        stack.grow(out.getCount());
                        inventory.setStackInSlot(11, stack);
                    }

                }
            }

            this.markDirty();
        }

        if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
        this.power -= 5;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int amount) {
        return i == 2 || i == 4 || i == 6 || i == 8 || i == 10 || i == 11;
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    @Override
    public long getPower() {
        return power;

    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1], tanks[2], tanks[3], tanks[4]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    @Override
    public void explode(World world, int x, int y, int z) {

        if (this.hasExploded) return;

        this.hasExploded = true;
        this.onFire = true;
        this.markDirty();
    }

    @Override
    public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) {
        if (!this.hasExploded || !this.onFire) return;

        if (type == EnumExtinguishType.FOAM || type == EnumExtinguishType.CO2) {
            this.onFire = false;
            this.markDirty();
            return;
        }

        if (type == EnumExtinguishType.WATER) {
            for (FluidTankNTM tank : tanks) {
                if (tank.getFill() > 0) {
                    world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 5F, true, true);
                    return;
                }
            }
        }
    }

    @Override
    public boolean isDamaged() {
        return this.hasExploded;
    }

    @Override
    public List<RecipesCommon.AStack> getRepairMaterials() {

        if (!repair.isEmpty())
            return repair;

        repair.add(new RecipesCommon.OreDictStack(OreDictManager.STEEL.plate(), 8));
        repair.add(new RecipesCommon.ComparableStack(ModItems.ducttape, 4));
        return repair;
    }

    @Override
    public void repair() {
        this.hasExploded = false;
        this.markDirty();
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (tanks[0].getFill() == 0 && tanks[1].getFill() == 0 && tanks[2].getFill() == 0 && tanks[3].getFill() == 0 && tanks[4].getFill() == 0 && !this.hasExploded)
            return;
        NBTTagCompound data = new NBTTagCompound();
        for (int i = 0; i < 5; i++) this.tanks[i].writeToNBT(data, "" + i);
        data.setBoolean("hasExploded", hasExploded);
        data.setBoolean("onFire", onFire);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        for (int i = 0; i < 5; i++) this.tanks[i].readFromNBT(data, "" + i);
        this.hasExploded = data.getBoolean("hasExploded");
        this.onFire = data.getBoolean("onFire");
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineRefinery(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineRefinery(player.inventory, this);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 1024.0D;
    }
}
