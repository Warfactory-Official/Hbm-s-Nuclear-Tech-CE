package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineElectricFurnace;
import com.hbm.inventory.gui.GUIMachineElectricFurnace;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineElectricFurnace extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider,
        IUpgradeInfoProvider {

    public static final long maxPower = 100000;
    private static final int[] slots_io = new int[]{0, 1, 2};
    public final UpgradeManagerNT upgradeManager;
    public int progress;
    public long power;
    public int maxProgress = 100;
    public int consumption = 50;
    private int cooldown = 0;

    public TileEntityMachineElectricFurnace() {
        super(0, false, true);

        inventory = new ItemStackHandler(4) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot == 3)
                    SoundUtil.playUpgradePlugSound(world, pos);
            }
        };

        upgradeManager = new UpgradeManagerNT(this);
    }

    @Override
    public String getDefaultName() {
        return "container.electricFurnace";
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        if (i == 0) {
            return Library.isBattery(itemStack);
        }

        if (i == 1) {
            return !FurnaceRecipes.instance().getSmeltingResult(itemStack).isEmpty();
        }

        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.progress = nbt.getInteger("progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("power", power);
        nbt.setInteger("progress", progress);
        return nbt;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slots_io;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        if (i == 0) if (Library.isEmptyBattery(itemStack)) return true;
        return i == 2;
    }

    public int getProgressScaled(int i) {
        return (progress * i) / maxProgress;
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public boolean hasPower() {
        return power >= consumption;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    public boolean canProcess() {
        if (inventory.getStackInSlot(1).isEmpty() || cooldown > 0) return false;
        ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(1)).copy();
        if (itemStack.isEmpty()) return false;
        return inventory.insertItem(2, itemStack, true).isEmpty();
    }

    private void processItem() {
        if (canProcess()) {
            ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(1)).copy();
            inventory.insertItem(2, itemStack, false);
            inventory.extractItem(1, 1, false);
        }
    }

    @Override
    public void update() {
        boolean markDirty = false;

        if (!world.isRemote) {

            if (cooldown > 0) {
                cooldown--;
            }

            power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            if (world.getTotalWorldTime() % 40 == 0) this.updateConnections();

            this.consumption = 50;
            this.maxProgress = 100;

            upgradeManager.checkSlots(3, 3);

            int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

            maxProgress -= speedLevel * 25;
            consumption += speedLevel * 50;
            maxProgress += powerLevel * 10;
            consumption -= powerLevel * 15;

            if (!hasPower()) {
                cooldown = 20;
            }

            if (hasPower() && canProcess()) {
                progress++;

                power -= consumption;

                if (world.getTotalWorldTime() % 20 == 0)
                    PollutionHandler.incrementPollution(world, this.pos, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

                if (this.progress >= maxProgress) {
                    this.progress = 0;
                    this.processItem();
                    markDirty = true;
                }
            } else {
                progress = 0;
            }

            boolean trigger = !hasPower() || !canProcess() || this.progress != 0;

            if (trigger) {
                markDirty = true;
                MachineElectricFurnace.updateBlockState(this.progress > 0, this.world, this.pos);
            }

            this.networkPackNT(50);


            if (markDirty) {
                this.markDirty();
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(maxProgress);
        buf.writeInt(progress);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        maxProgress = buf.readInt();
        progress = buf.readInt();
    }

    private void updateConnections() {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineElectricFurnace(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineElectricFurnace(player.inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_electric_furnace_off));
        if (type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if (type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (level * 30) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "+" + (level * 10) + "%"));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        return upgrades;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (Library.isSwappingBetweenVariants(oldState, newState, ModBlocks.machine_electric_furnace_off, ModBlocks.machine_electric_furnace_on)) return false;
        return super.shouldRefresh(world, pos, oldState, newState);
    }
}
