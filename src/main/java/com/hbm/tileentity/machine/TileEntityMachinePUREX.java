package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachinePUREX;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachinePUREX;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.modules.machine.ModuleMachinePUREX;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachinePUREX extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IGUIProvider {

    public FluidTankNTM[] inputTanks;
    public FluidTankNTM[] outputTanks;

    public long power;
    public long maxPower = 1_000_000;
    public boolean didProcess = false;

    public boolean frame = false;
    public int anim;
    public int prevAnim;

    public ModuleMachinePUREX purexModule;
    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public TileEntityMachinePUREX() {
        super(0, true, true);

        inventory = new ItemStackHandler(13) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot >= 2 && slot <= 3)
                    SoundUtil.playUpgradePlugSound(world, pos);
            }
        };

        this.inputTanks = new FluidTankNTM[3];
        this.outputTanks = new FluidTankNTM[1];
        for(int i = 0; i < 3; i++) {
            this.inputTanks[i] = new FluidTankNTM(Fluids.NONE, 24_000);
        }
        this.outputTanks[0] = new FluidTankNTM(Fluids.NONE, 24_000);

        this.purexModule = new ModuleMachinePUREX(0, this, inventory)
                .itemInput(4).itemOutput(7)
                .fluidInput(inputTanks[0], inputTanks[1], inputTanks[2]).fluidOutput(outputTanks[0]);
    }

    @Override
    public String getDefaultName() {
        return "container.machinePUREX";
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!world.isRemote) {

            GenericRecipe recipe = PUREXRecipes.INSTANCE.recipeNameMap.get(purexModule.recipe);
            if(recipe != null) {
                this.maxPower = recipe.power * 100;
            }
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            upgradeManager.checkSlots(inventory, 2, 3);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                for(FluidTankNTM tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), world, pos);
                for(FluidTankNTM tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, world, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.purexModule.update(speed, pow, true, inventory.getStackInSlot(1));
            this.didProcess = this.purexModule.didProcess;
            if(this.purexModule.markDirty) this.markDirty();

            this.networkPackNT(100);

        } else {

            this.prevAnim = this.anim;
            if(this.didProcess) this.anim++;

            if(world.getTotalWorldTime() % 20 == 0) {
                BlockPos upFive = pos.up(5);
                IBlockState state = world.getBlockState(upFive);
                frame = !state.getBlock().isAir(state, world, upFive);
            }
        }
    }

    public DirPos[] getConPos() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        return new DirPos[] {
                new DirPos(xCoord + 3, yCoord, zCoord - 2, Library.POS_X),
                new DirPos(xCoord + 3, yCoord, zCoord - 1, Library.POS_X),
                new DirPos(xCoord + 3, yCoord, zCoord + 0, Library.POS_X),
                new DirPos(xCoord + 3, yCoord, zCoord + 1, Library.POS_X),
                new DirPos(xCoord + 3, yCoord, zCoord + 2, Library.POS_X),
                new DirPos(xCoord - 3, yCoord, zCoord - 1, Library.NEG_X),
                new DirPos(xCoord - 3, yCoord, zCoord - 2, Library.NEG_X),
                new DirPos(xCoord - 3, yCoord, zCoord + 0, Library.NEG_X),
                new DirPos(xCoord - 3, yCoord, zCoord + 1, Library.NEG_X),
                new DirPos(xCoord - 3, yCoord, zCoord + 2, Library.NEG_X),
                new DirPos(xCoord - 2, yCoord, zCoord + 3, Library.POS_Z),
                new DirPos(xCoord - 1, yCoord, zCoord + 3, Library.POS_Z),
                new DirPos(xCoord + 0, yCoord, zCoord + 3, Library.POS_Z),
                new DirPos(xCoord + 1, yCoord, zCoord + 3, Library.POS_Z),
                new DirPos(xCoord + 2, yCoord, zCoord + 3, Library.POS_Z),
                new DirPos(xCoord - 2, yCoord, zCoord - 3, Library.NEG_Z),
                new DirPos(xCoord - 1, yCoord, zCoord - 3, Library.NEG_Z),
                new DirPos(xCoord + 0, yCoord, zCoord - 3, Library.NEG_Z),
                new DirPos(xCoord + 1, yCoord, zCoord - 3, Library.NEG_Z),
                new DirPos(xCoord + 2, yCoord, zCoord - 3, Library.NEG_Z),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        for(FluidTankNTM tank : inputTanks) tank.serialize(buf);
        for(FluidTankNTM tank : outputTanks) tank.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeBoolean(didProcess);
        this.purexModule.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        for(FluidTankNTM tank : inputTanks) tank.deserialize(buf);
        for(FluidTankNTM tank : outputTanks) tank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.purexModule.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        for(int i = 0; i < 3; i++) {
            this.inputTanks[i].readFromNBT(nbt, "i" + i);
        }
        this.outputTanks[0].readFromNBT(nbt, "o" + 0);

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.purexModule.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        for(int i = 0; i < 3; i++) {
            this.inputTanks[i].writeToNBT(nbt, "i" + i);
        }
        this.outputTanks[0].writeToNBT(nbt, "o" + 0);

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        this.purexModule.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        if(slot == 1 && stack.getItem() == ModItems.blueprints) return true;
        if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
        if(this.purexModule.isItemValid(slot, stack)) return true; // recipe input crap
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return (i >= 7 && i <= 12) || this.purexModule.isSlotClogged(i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12};
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return inputTanks; }
    @Override public FluidTankNTM[] getSendingTanks() { return outputTanks; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTanks[0], inputTanks[1], inputTanks[2], outputTanks[0]}; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachinePUREX(player.inventory, this.inventory); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachinePUREX(player.inventory, this); }

    @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index == 0) {
                this.purexModule.recipe = selection;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null){
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(xCoord - 2, yCoord, zCoord - 2, xCoord + 3, yCoord + 5, zCoord + 3);
        }
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_purex));
        if(type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_SPEED, "+" + (level * 100 / 3) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 50) + "%"));
        }
        if(type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
        }
        if(type == UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }
}
