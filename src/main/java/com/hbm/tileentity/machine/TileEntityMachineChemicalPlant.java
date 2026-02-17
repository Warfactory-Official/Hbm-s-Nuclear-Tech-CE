package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineChemicalPlant;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineChemicalPlant;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineChemplant;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;
@AutoRegister
public class TileEntityMachineChemicalPlant extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IGUIProvider {

    public FluidTankNTM[] inputTanks;
    public FluidTankNTM[] outputTanks;

    public long power;
    public long maxPower = 100_000;
    public boolean didProcess = false;

    public boolean frame = false;
    public int anim;
    public int prevAnim;
    private AudioWrapper audio;

    public ModuleMachineChemplant chemplantModule;
    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public TileEntityMachineChemicalPlant() {
        super(0, true, true);

        inventory = new ItemStackHandler(22) {
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
        this.outputTanks = new FluidTankNTM[3];
        for(int i = 0; i < 3; i++) {
            this.inputTanks[i] = new FluidTankNTM(Fluids.NONE, 24_000);
            this.outputTanks[i] = new FluidTankNTM(Fluids.NONE, 24_000);
        }

        this.chemplantModule = new ModuleMachineChemplant(0, this, inventory)
                .itemInput(4, 5, 6)
                .itemOutput(7, 8, 9)
                .fluidInput(inputTanks[0], inputTanks[1], inputTanks[2])
                .fluidOutput(outputTanks[0], outputTanks[1], outputTanks[2]);
    }

    @Override
    public String getDefaultName() {
        return "container.machineChemicalPlant";
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!world.isRemote) {

            GenericRecipe recipe = ChemicalPlantRecipes.INSTANCE.recipeNameMap.get(chemplantModule.recipe);
            if(recipe != null) {
                this.maxPower = recipe.power * 100;
            }
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            upgradeManager.checkSlots(inventory, 2, 3);

            if(recipe != null && recipe.inputFluid != null) {
                for(int i = 0; i < Math.min(3, recipe.inputFluid.length); i++) {
                    inputTanks[i].loadTank(10 + i, 13 + i, inventory);
                }
            }

            outputTanks[0].unloadTank(16, 19, inventory);
            outputTanks[1].unloadTank(17, 20, inventory);
            outputTanks[2].unloadTank(18, 21, inventory);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                for(FluidTankNTM tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), world, pos);
                for(FluidTankNTM tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, world, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.chemplantModule.update(speed, pow, true, inventory.getStackInSlot(1));
            this.didProcess = this.chemplantModule.didProcess;
            if(this.chemplantModule.markDirty) this.markDirty();

            if(didProcess) {
                if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.meteorite_sword_machined)
                    inventory.setStackInSlot(0, new ItemStack(ModItems.meteorite_sword_treated));
            }

            this.networkPackNT(100);

        } else {

            this.prevAnim = this.anim;
            if(this.didProcess) this.anim++;

            if(world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getBlock() != Blocks.AIR;
            }

            if(this.didProcess && MainRegistry.proxy.me().getDistance(pos.getX(), pos.getY(), pos.getZ()) < 50) {
                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }
                audio.keepAlive();
                audio.updateVolume(this.getVolume(1F));

            } else {
                if(audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }
    // FIXME this shit doesn't work
    @Override public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.chemicalPlant, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1F, 15F, 1.0F, 20);
    }

    @Override public void onChunkUnload() {
        super.onChunkUnload();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    @Override public void invalidate() {
        super.invalidate();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 0, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 0, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 0, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() + 0, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
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
        this.chemplantModule.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        for(FluidTankNTM tank : inputTanks) tank.deserialize(buf);
        for(FluidTankNTM tank : outputTanks) tank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.chemplantModule.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        for(int i = 0; i < 3; i++) {
            this.inputTanks[i].readFromNBT(nbt, "i" + i);
            this.outputTanks[i].readFromNBT(nbt, "o" + i);
        }

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.chemplantModule.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for(int i = 0; i < 3; i++) {
            this.inputTanks[i].writeToNBT(nbt, "i" + i);
            this.outputTanks[i].writeToNBT(nbt, "o" + i);
        }

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        this.chemplantModule.writeToNBT(nbt);

        return super.writeToNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        if(slot == 1 && stack.getItem() == ModItems.blueprints) return true;
        if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
        if(slot >= 10 && slot <= 12) return true; // input fluid
        if(slot >= 16 && slot <= 18) return true; // output fluid
        return this.chemplantModule.isItemValid(slot, stack); // recipe input crap
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i >= 7 && i <= 9;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {4, 5, 6, 7, 8, 9};
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return inputTanks; }
    @Override public FluidTankNTM[] getSendingTanks() { return outputTanks; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTanks[0], inputTanks[1], inputTanks[2], outputTanks[0], outputTanks[1], outputTanks[2]}; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineChemicalPlant(player.inventory, getCheckedInventory(), this); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineChemicalPlant(player.inventory, this); }

    @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index == 0) {
                this.chemplantModule.recipe = selection;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED || type == ItemMachineUpgrade.UpgradeType.POWER || type == ItemMachineUpgrade.UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_chemical_plant));
        if(type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_SPEED, "+" + (level * 100 / 3) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 50) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
        }
    }

    @Override
    public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
        HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.POWER, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }
}
