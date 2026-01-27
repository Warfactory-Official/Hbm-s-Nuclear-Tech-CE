package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineChemicalFactory;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineChemicalFactory;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.*;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineChemplant;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TileEntityProxyDyn;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineChemicalFactory extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, 
        IControlReceiver, IGUIProvider, TileEntityProxyDyn.IProxyDelegateProvider {

    public FluidTankNTM[] allTanks;
    public FluidTankNTM[] inputTanks;
    public FluidTankNTM[] outputTanks;

    public FluidTankNTM water;
    public FluidTankNTM lps;

    public long power;
    public long maxPower = 1_000_000;
    public boolean[] didProcess = new boolean[4];

    public boolean frame = false;
    public int anim;
    public int prevAnim;
    private AudioWrapper audio;

    public ModuleMachineChemplant[] chemplantModule;
    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    protected DelegateChemicalFactoy delegate = new DelegateChemicalFactoy();

    public TileEntityMachineChemicalFactory() {
        super(32, true, true);

        this.inputTanks = new FluidTankNTM[12];
        this.outputTanks = new FluidTankNTM[12];
        for(int i = 0; i < 12; i++) {
            this.inputTanks[i] = new FluidTankNTM(Fluids.NONE, 24_000);
            this.outputTanks[i] = new FluidTankNTM(Fluids.NONE, 24_000);
        }

        this.water = new FluidTankNTM(Fluids.WATER, 4_000);
        this.lps = new FluidTankNTM(Fluids.SPENTSTEAM, 4_000);

        this.allTanks = new FluidTankNTM[this.inputTanks.length + this.outputTanks.length + 2];
        System.arraycopy(this.inputTanks, 0, this.allTanks, 0, inputTanks.length);
        System.arraycopy(this.outputTanks, 0, this.allTanks, this.inputTanks.length, outputTanks.length);

        this.allTanks[this.allTanks.length - 2] = this.water;
        this.allTanks[this.allTanks.length - 1] = this.lps;

        this.chemplantModule = new ModuleMachineChemplant[4];
        for(int i = 0; i < 4; i++) this.chemplantModule[i] = new ModuleMachineChemplant(i, this, inventory)
                .itemInput(5 + i * 7, 6 + i * 7, 7 + i * 7)
                .itemOutput(8 + i * 7, 9 + i * 7, 10 + i * 7)
                .fluidInput(inputTanks[i * 3], inputTanks[1 + i * 3], inputTanks[2 + i * 3])
                .fluidOutput(outputTanks[i * 3], outputTanks[1 + i * 3], outputTanks[2 + i * 3]);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        if(i >= 8 && i <= 10) return true;
        if(i >= 15 && i <= 17) return true;
        if(i >= 22 && i <= 24) return true;
        return i >= 29 && i <= 31;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        for(int i = 0; i < 4; i++) if(slot == 4 + i * 7 && stack.getItem() == ModItems.blueprints) return true;
        if(slot >= 1 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
        for(int i = 0; i < 4; i++) if(this.chemplantModule[i].isItemValid(slot, stack)) return true; // recipe input crap
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {
                5, 6, 7, 8, 9, 10,
                12, 13, 14, 15, 16, 17,
                19, 20, 21, 22, 23, 24,
                26, 27, 28, 29, 30, 31
        };
    }

    @Override
    public String getDefaultName() {
        return "container.machineChemicalFactory";
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 10_000_000;

        if(!world.isRemote) {

            long nextMaxPower = 0;
            for(int i = 0; i < 4; i++) {
                GenericRecipe recipe = ChemicalPlantRecipes.INSTANCE.recipeNameMap.get(chemplantModule[i].recipe);
                if(recipe != null) {
                    nextMaxPower += recipe.power * 100;
                }
            }
            this.maxPower = nextMaxPower;
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            upgradeManager.checkSlots(inventory, 1, 3);

            inputTanks[0].loadTank(10, 13, inventory);
            inputTanks[1].loadTank(11, 14, inventory);
            inputTanks[2].loadTank(12, 15, inventory);

            outputTanks[0].unloadTank(16, 19, inventory);
            outputTanks[1].unloadTank(17, 20, inventory);
            outputTanks[2].unloadTank(18, 21, inventory);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                for(FluidTankNTM tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), world, pos);
                for(FluidTankNTM tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, world, pos);
            }

            for(DirPos pos : getCoolPos()) {
                delegate.trySubscribe(world, pos);
                delegate.trySubscribe(water.getTankType(), world, pos);
                delegate.tryProvide(lps, world, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3) * 10D / 3D;
            boolean markDirty = false;

            for(int i = 0; i < 4; i++) {
                this.chemplantModule[i].update(speed * 2D, pow * 2D, canCool(), inventory.getStackInSlot(4 + i * 7));
                this.didProcess[i] =  this.chemplantModule[i].didProcess;
                markDirty |= this.chemplantModule[i].markDirty;

                if(this.chemplantModule[i].didProcess) {
                    this.water.setFill(this.water.getFill() - 100);
                    this.lps.setFill(this.lps.getFill() + 100);
                }
            }

            for(FluidTankNTM in : inputTanks) if(in.getTankType() != Fluids.NONE) for(FluidTankNTM out : outputTanks) { // up to 144 iterations, but most of them are NOP anyway
                if(out.getTankType() == Fluids.NONE) continue;
                if(out.getTankType() != in.getTankType()) continue;
                int toMove = BobMathUtil.min(in.getMaxFill() - in.getFill(), out.getFill(), 50);
                if(toMove > 0) {
                    in.setFill(in.getFill() + toMove);
                    out.setFill(out.getFill() - toMove);
                }
            }

            if(markDirty) this.markDirty();

            this.networkPackNT(100);

        } else {

            this.prevAnim = this.anim;
            boolean didSomething = didProcess[0] || didProcess[1] || didProcess[2] || didProcess[3];
            if(didSomething) this.anim++;

            if(world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getBlock() != Blocks.AIR;
            }

            if(didSomething && MainRegistry.proxy.me().getDistance(pos.getX() , pos.getY(), pos.getZ()) < 50) {
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

    public boolean canCool() {
        return water.getFill() >= 100 && lps.getFill() <= lps.getMaxFill() - 100;
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[] {
                new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() - 2, Library.POS_X),
                new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() + 0, Library.POS_X),
                new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() + 2, Library.POS_X),
                new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() - 2, Library.NEG_X),
                new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() + 0, Library.NEG_X),
                new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() + 2, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 3, Library.POS_Z),
                new DirPos(pos.getX() + 0, pos.getY(), pos.getZ() + 3, Library.POS_Z),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 3, Library.POS_Z),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
                new DirPos(pos.getX() + 0, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 3, Library.NEG_Z),

                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 1 + rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 0 + rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() - dir.offsetX * 1 + rot.offsetX * 2, pos.getY() + 3, pos.getZ() - dir.offsetZ * 1 + rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 2, pos.getY() + 3, pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 1 - rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 1 - rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 0 - rot.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 0 - rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() - dir.offsetX * 1 - rot.offsetX * 2, pos.getY() + 3, pos.getZ() - dir.offsetZ * 1 - rot.offsetZ * 2, Library.POS_Y),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 2, pos.getY() + 3, pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 2, Library.POS_Y),

                new DirPos(pos.getX() + dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() + dir.offsetX - rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ - rot.offsetZ * 3, rot.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX - rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ - rot.offsetZ * 3, rot.getOpposite()),
        };
    }


    public DirPos[] getCoolPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + rot.offsetX + dir.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ + dir.offsetZ * 3, dir),
                new DirPos(pos.getX() - rot.offsetX + dir.offsetX * 3, pos.getY(), pos.getZ() - rot.offsetZ + dir.offsetZ * 3, dir),
                new DirPos(pos.getX() + rot.offsetX - dir.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ - dir.offsetZ * 3, dir.getOpposite()),
                new DirPos(pos.getX() - rot.offsetX - dir.offsetX * 3, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ * 3, dir.getOpposite()),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        for(FluidTankNTM tank : inputTanks) tank.serialize(buf);
        for(FluidTankNTM tank : outputTanks) tank.serialize(buf);
        water.serialize(buf);
        lps.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        for(boolean b : didProcess) buf.writeBoolean(b);
        for(int i = 0; i < 4; i++) this.chemplantModule[i].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        for(FluidTankNTM tank : inputTanks) tank.deserialize(buf);
        for(FluidTankNTM tank : outputTanks) tank.deserialize(buf);
        water.deserialize(buf);
        lps.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        for(int i = 0; i < 4; i++) this.didProcess[i] = buf.readBoolean();
        for(int i = 0; i < 4; i++) this.chemplantModule[i].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        for(int i = 0; i < inputTanks.length; i++) this.inputTanks[i].readFromNBT(nbt, "i" + i);
        for(int i = 0; i < outputTanks.length; i++) this.outputTanks[i].readFromNBT(nbt, "i" + i);

        this.water.readFromNBT(nbt, "w");
        this.lps.readFromNBT(nbt, "s");

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        for(int i = 0; i < 4; i++) this.chemplantModule[i].readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for(int i = 0; i < inputTanks.length; i++) this.inputTanks[i].writeToNBT(nbt, "i" + i);
        for(int i = 0; i < outputTanks.length; i++) this.outputTanks[i].writeToNBT(nbt, "i" + i);

        this.water.writeToNBT(nbt, "w");
        this.lps.writeToNBT(nbt, "s");

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        for(int i = 0; i < 4; i++) this.chemplantModule[i].writeToNBT(nbt);

        return super.writeToNBT(nbt);
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        BlockPos acc = CapabilityContextProvider.getAccessor(this.pos);
        if (isCoolantAccessor(acc)) return new FluidTankNTM[]{ this.water };
        return inputTanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        BlockPos acc = CapabilityContextProvider.getAccessor(this.pos);
        if (isCoolantAccessor(acc)) return new FluidTankNTM[]{ this.lps };
        return outputTanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        BlockPos acc = CapabilityContextProvider.getAccessor(this.pos);
        if (isCoolantAccessor(acc)) return new FluidTankNTM[]{ this.water, this.lps };
        return allTanks;
    }

    private boolean isCoolantAccessor(BlockPos accessor) {
        if (accessor == null) return false;
        if (coolantLine == null) {
            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
            ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
            coolantLine = new DirPos[]{
                    new DirPos(pos.getX() + rot.offsetX + dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ + dir.offsetZ * 2, dir),
                    new DirPos(pos.getX() - rot.offsetX + dir.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ + dir.offsetZ * 2, dir),
                    new DirPos(pos.getX() + rot.offsetX - dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ - dir.offsetZ * 2, dir.getOpposite()),
                    new DirPos(pos.getX() - rot.offsetX - dir.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ * 2, dir.getOpposite()),
            };
        }
        for (DirPos dPos : coolantLine)
            if (dPos.compare(accessor.getX(), accessor.getY(), accessor.getZ())) return true;
        return false;
    }
//
//    @Override public FluidTankNTM[] getReceivingTanks() { return inputTanks; }
//    @Override public FluidTankNTM[] getSendingTanks() { return outputTanks; }
//    @Override public FluidTankNTM[] getAllTanks() { return allTanks; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineChemicalFactory(player.inventory, this.inventory); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineChemicalFactory(player.inventory, this); }

    @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index >= 0 && index < 4) {
                this.chemplantModule[index].recipe = selection;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) bb = new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3);
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
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_chemical_factory));
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

    public DirPos[] coolantLine; // we could make the same fucking array 50,000 times per tick, or we just make it once

    @Override // all the delegating shit so the proxies on the coolant lines only access coolant (and power and inventory) but not the recipe fluids
    public Object getDelegateForPosition(BlockPos tPos) {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        if(coolantLine == null) coolantLine = new DirPos[] {
                new DirPos(pos.getX() + rot.offsetX + dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ + dir.offsetZ * 2, dir),
                new DirPos(pos.getX() - rot.offsetX + dir.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ + dir.offsetZ * 2, dir),
                new DirPos(pos.getX() + rot.offsetX - dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - rot.offsetX - dir.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ * 2, dir.getOpposite()),
        };

        for(DirPos dPos : coolantLine) if(dPos.compare(tPos.getX(), tPos.getY(), tPos.getZ())) return this.delegate; // this actually fucking works

        return null;
    }

    public class DelegateChemicalFactoy implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {

        @Override public long getPower() { return TileEntityMachineChemicalFactory.this.getPower(); }
        @Override public void setPower(long power) { TileEntityMachineChemicalFactory.this.setPower(power); }
        @Override public long getMaxPower() { return TileEntityMachineChemicalFactory.this.getMaxPower(); }
        @Override public boolean isLoaded() { return TileEntityMachineChemicalFactory.this.isLoaded(); }

        @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {TileEntityMachineChemicalFactory.this.water}; }
        @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {TileEntityMachineChemicalFactory.this.lps}; }

        @Override public FluidTankNTM[] getAllTanks() { return TileEntityMachineChemicalFactory.this.getAllTanks(); }
    }
}
