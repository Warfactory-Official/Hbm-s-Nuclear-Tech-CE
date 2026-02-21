package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineAssemblyFactory;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineAssemblyFactory;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineAssembler;
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
import java.util.Random;

@AutoRegister
public class TileEntityMachineAssemblyFactory extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IGUIProvider, TileEntityProxyDyn.IProxyDelegateProvider {

    public FluidTankNTM[] allTanks;
    public FluidTankNTM[] inputTanks;
    public FluidTankNTM[] outputTanks;

    public FluidTankNTM water;
    public FluidTankNTM lps;

    public long power;
    public long maxPower = 1_000_000;
    public boolean[] didProcess = new boolean[4];

    public boolean frame = false;
    private AudioWrapper audio;
    public AssemfacArm[] animations;

    public ModuleMachineAssembler[] assemblerModule;
    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    protected DelegateAssemblyFactoy delegate = new DelegateAssemblyFactoy();

    public TileEntityMachineAssemblyFactory() {
        super(60, true, true);

        animations = new AssemfacArm[2];
        for(int i = 0; i < animations.length; i++) animations[i] = new AssemfacArm(i);

        this.inputTanks = new FluidTankNTM[4];
        this.outputTanks = new FluidTankNTM[4];
        for(int i = 0; i < 4; i++) {
            this.inputTanks[i] = new FluidTankNTM(Fluids.NONE, 4_000);
            this.outputTanks[i] = new FluidTankNTM(Fluids.NONE, 4_000);
        }

        this.water = new FluidTankNTM(Fluids.WATER, 4_000);
        this.lps = new FluidTankNTM(Fluids.SPENTSTEAM, 4_000);

        this.allTanks = new FluidTankNTM[this.inputTanks.length + this.outputTanks.length + 2];
        System.arraycopy(this.inputTanks, 0, this.allTanks, 0, inputTanks.length);
        System.arraycopy(this.outputTanks, 0, this.allTanks, this.inputTanks.length, outputTanks.length);

        this.allTanks[this.allTanks.length - 2] = this.water;
        this.allTanks[this.allTanks.length - 1] = this.lps;

        this.assemblerModule = new ModuleMachineAssembler[4];
        for(int i = 0; i < 4; i++) this.assemblerModule[i] = new ModuleMachineAssembler(i, this, inventory)
                .itemInput(5 + i * 14).itemOutput(17 + i * 14)
                .fluidInput(inputTanks[i]).fluidOutput(outputTanks[i]);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        for(int k = 0; k < 4; k++) if(i == 17 + k * 14) return true;
        for(int k = 0; k < 4; k++) if(this.assemblerModule[k].isSlotClogged(i)) return true;
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        for(int i = 0; i < 4; i++) if(slot == 4 + i * 14 && stack.getItem() == ModItems.blueprints) return true;
        if(slot >= 1 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
        for(int i = 0; i < 4; i++) if(this.assemblerModule[i].isItemValid(slot, stack)) return true; // recipe input crap
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] {
                5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
                33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
                47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59
        }; // ho boy, a big fucking array of hand-written values, surely this isn't gonna bite me in the ass some day
    }

    @Override
    public String getDefaultName() {
        return "container.machineAssemblyFactory";
    }

    public DirPos[] getIOPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(getPos().getX() + dir.offsetX + rot.offsetX * 3, getPos().getY(), getPos().getZ() + dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(getPos().getX() - dir.offsetX + rot.offsetX * 3, getPos().getY(), getPos().getZ() - dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(getPos().getX() + dir.offsetX - rot.offsetX * 3, getPos().getY(), getPos().getZ() + dir.offsetZ - rot.offsetZ * 3, rot.getOpposite()),
                new DirPos(getPos().getX() - dir.offsetX - rot.offsetX * 3, getPos().getY(), getPos().getZ() - dir.offsetZ - rot.offsetZ * 3, rot.getOpposite()),
        };
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 10_000_000;

        if(!world.isRemote) {

            long nextMaxPower = 0;
            for(int i = 0; i < 4; i++) {
                GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assemblerModule[i].recipe);
                if(recipe != null) {
                    nextMaxPower += recipe.power * 100;
                }
            }
            this.maxPower = nextMaxPower;
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            upgradeManager.checkSlots(inventory, 1, 3);

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
                this.assemblerModule[i].update(speed * 2D, pow * 2D, canCool(), inventory.getStackInSlot(4 + i * 7));
                this.didProcess[i] =  this.assemblerModule[i].didProcess;
                markDirty |= this.assemblerModule[i].markDirty;

                if(this.assemblerModule[i].didProcess) {
                    this.water.setFill(this.water.getFill() - 100);
                    this.lps.setFill(this.lps.getFill() + 100);
                }
            }

            if(markDirty) this.markDirty();

            this.networkPackNT(100);
        } else {

            if((didProcess[0] ||didProcess[1] ||didProcess[2] ||didProcess[3]) && MainRegistry.proxy.me().getDistance(pos.getX(), pos.getY(), pos.getZ()) < 50) {
                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }
                audio.keepAlive();
                audio.updatePitch(0.75F);
                audio.updateVolume(this.getVolume(0.5F));

            } else {
                if(audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }

            for(AssemfacArm animation : animations) animation.update(didProcess[0] ||didProcess[1] ||didProcess[2] ||didProcess[3]);

            if(world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getBlock() != Blocks.AIR;
            }
        }
    }

    @Override public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.motor, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.5F, 15F, 0.75F, 20);
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
        for(int i = 0; i < 4; i++) this.assemblerModule[i].serialize(buf);
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
        for(int i = 0; i < 4; i++) this.assemblerModule[i].deserialize(buf);
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
        for(int i = 0; i < 4; i++) this.assemblerModule[i].readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

        for(int i = 0; i < inputTanks.length; i++) this.inputTanks[i].writeToNBT(nbt, "i" + i);
        for(int i = 0; i < outputTanks.length; i++) this.outputTanks[i].writeToNBT(nbt, "i" + i);

        this.water.writeToNBT(nbt, "w");
        this.lps.writeToNBT(nbt, "s");

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        for(int i = 0; i < 4; i++) this.assemblerModule[i].writeToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return inputTanks; }
    @Override public FluidTankNTM[] getSendingTanks() { return outputTanks; }
    @Override public FluidTankNTM[] getAllTanks() { return allTanks; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineAssemblyFactory(player.inventory, this.inventory); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineAssemblyFactory(player.inventory, this); }

    @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index >= 0 && index < 4) {
                this.assemblerModule[index].recipe = selection;
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

    public DirPos[] coolantLine;

    @Override // carelessly copy pasted from TileEntityMachineChemicalFactory
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

    public class DelegateAssemblyFactoy implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 { // scumware

        @Override public long getPower() { return TileEntityMachineAssemblyFactory.this.getPower(); }
        @Override public void setPower(long power) { TileEntityMachineAssemblyFactory.this.setPower(power); }
        @Override public long getMaxPower() { return TileEntityMachineAssemblyFactory.this.getMaxPower(); }
        @Override public boolean isLoaded() { return TileEntityMachineAssemblyFactory.this.isLoaded(); }

        @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {TileEntityMachineAssemblyFactory.this.water}; }
        @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {TileEntityMachineAssemblyFactory.this.lps}; }

        @Override public FluidTankNTM[] getAllTanks() { return TileEntityMachineAssemblyFactory.this.getAllTanks(); }
    }

    /**
     * Carriage consisting of two arms - a striker and a saw
     * Movement of both arms is inverted, one pedestal can only be serviced by one arm at a time
     *
     * @author hbm
     */
    public class AssemfacArm {

        public AssemblerArm striker;
        public AssemblerArm saw;

        Random rand = new Random();
        AFArmState state = AFArmState.WORKING;
        double slider = 0;
        double prevSlider = 0;
        boolean direction = false;
        int timeUntilReposition;

        public AssemfacArm(int group) {
            striker = new AssemblerArm(	group == 0 ? 0 : 3);
            saw = new AssemblerArm(		group == 0 ? 1 : 2).yepThatsASaw();
            timeUntilReposition = 140 + rand.nextInt(161);
        }

        public void update(boolean working) {
            this.prevSlider = this.slider;

            // one of the arms must do something. doesn't matter which or what position the carriage is in
            if(didProcess[striker.recipeIndex] || didProcess[saw.recipeIndex]) switch(state) {
                case WORKING -> {
                    timeUntilReposition--;
                    if (timeUntilReposition <= 0) {
                        state = AFArmState.RETIRING;
                    }
                }
                case RETIRING -> {
                    if (striker.state == ArmState.WAIT && saw.state == ArmState.WAIT) { // only progress as soon as both arms are done moving
                        state = AFArmState.SLIDING;
                        direction = !direction;
                        if (!muffled)
                            MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStart, SoundCategory.BLOCKS, getVolume(0.25F), 1.25F + world.rand.nextFloat() * 0.25F);
                    }
                }
                case SLIDING -> {
                    double sliderSpeed = 1D / 10D; // 10 ticks for transit
                    if (direction) {
                        slider += sliderSpeed;
                        if (slider >= 1) {
                            slider = 1;
                            state = AFArmState.WORKING;
                        }
                    } else {
                        slider -= sliderSpeed;
                        if (slider <= 0) {
                            slider = 0;
                            state = AFArmState.WORKING;
                        }
                    }
                    if (state == AFArmState.WORKING) timeUntilReposition = 140 + rand.nextInt(161); // 7 to 15 seconds
                }
            }

            striker.updateArm();
            saw.updateArm();
        }

        public double getSlider(float interp) {
            return this.prevSlider + (this.slider - this.prevSlider) * interp;
        }

        // there's a ton of way to make this more optimized/readable/professional/scrungular but i don't care i am happy this crap works at all
        public class AssemblerArm { // more fucking nesting!!!11

            public double[] angles = new double[4];
            public double[] prevAngles = new double[4];
            public double[] targetAngles = new double[4];
            public double[] speed = new double[4];
            public double sawAngle;
            public double prevSawAngle;
            public int recipeIndex; // the index of which pedestal is serviced, assuming the carriage is at default position

            ArmState state = ArmState.REPOSITION;
            int actionDelay = 0;
            boolean saw = false;

            public AssemblerArm(int index) {
                this.recipeIndex = index;
                this.resetSpeed();
                this.chooseNewArmPoistion();
            }

            public AssemblerArm yepThatsASaw() { this.saw = true; this.chooseNewArmPoistion(); return this; }

            private void resetSpeed() {
                speed[0] = 15;	//Pivot
                speed[1] = 15;	//Arm
                speed[2] = 15;	//Piston
                speed[3] = saw ? 0.125 : 0.5;	//Striker
            }

            public void updateArm() {
                resetSpeed();

                System.arraycopy(angles, 0, prevAngles, 0, angles.length);

                prevSawAngle = sawAngle;

                int serviceIndex = recipeIndex;
                if(slider > 0.5) serviceIndex += (serviceIndex % 2 == 0 ? 1 : -1); // if the carriage has moved, swap the indices so they match up with the serviced pedestal
                if(!didProcess[serviceIndex]) state = ArmState.RETIRE;

                if(state == ArmState.CUT || state == ArmState.EXTEND) {
                    this.sawAngle += 45D;
                }

                if(actionDelay > 0) {
                    actionDelay--;
                    return;
                }

                switch (state) {
                    // Move. If done moving, set a delay and progress to EXTEND
                    case REPOSITION -> {
                        if (move()) {
                            actionDelay = 2;
                            state = ArmState.EXTEND;
                            targetAngles[3] = saw ? -0.375D : -0.75D;
                        }
                    }
                    case EXTEND -> {
                        if (move()) {

                            if (saw) {
                                state = ArmState.CUT;
                                targetAngles[2] = -targetAngles[2];
                                if(!muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerCut, SoundCategory.BLOCKS, getVolume(0.5F), 1F + rand.nextFloat() * 0.25F);
                            } else {
                                state = ArmState.RETRACT;
                                targetAngles[3] = 0D;
                                if (!muffled)
                                    MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStrike, SoundCategory.BLOCKS, getVolume(0.5F), 1F);
                            }
                        }
                    }
                    case CUT -> {
                        speed[2] = Math.abs(targetAngles[2] / 20D);
                        if (move()) {
                            state = ArmState.RETRACT;
                            targetAngles[3] = 0D;
                        }
                    }
                    case RETRACT -> {
                        if (move()) {
                            actionDelay = 2 + rand.nextInt(5);
                            chooseNewArmPoistion();
                            state = AssemfacArm.this.state == AFArmState.RETIRING ? ArmState.RETIRE : ArmState.REPOSITION;
                        }
                    }
                    case RETIRE -> {
                        this.targetAngles[0] = 0;
                        this.targetAngles[1] = 0;
                        this.targetAngles[2] = 0;
                        this.targetAngles[3] = 0;

                        if (move()) {
                            actionDelay = 2 + rand.nextInt(5);
                            chooseNewArmPoistion();
                            state = ArmState.WAIT;
                        }
                    }
                    case WAIT -> {
                        if (AssemfacArm.this.state == AFArmState.WORKING) this.state = ArmState.REPOSITION;
                    }
                }
            }

            public void chooseNewArmPoistion() {

                double[][] pos = !saw ? new double[][] {
                        // striker
                        {10, 10, -10},
                        {15, 15, -15},
                        {25, 10, -15},
                        {30, 0, -10},
                        {-10, 10, 0},
                        {-20, 30, -15}
                } : new double[][] {
                        // saw
                        {-15, 15, -10},
                        {-15, 15, -15},
                        {-15, 15, 10},
                        {-15, 15, 15},
                        {-15, 15, 2},
                        {-15, 15, -2}
                };

                int chosen = rand.nextInt(pos.length);
                this.targetAngles[0] = pos[chosen][0];
                this.targetAngles[1] = pos[chosen][1];
                this.targetAngles[2] = pos[chosen][2];
            }

            private boolean move() {
                boolean didMove = false;

                for(int i = 0; i < angles.length; i++) {
                    if(angles[i] == targetAngles[i])
                        continue;

                    didMove = true;

                    double angle = angles[i];
                    double target = targetAngles[i];
                    double turn = speed[i];
                    double delta = Math.abs(angle - target);

                    if(delta <= turn) {
                        angles[i] = targetAngles[i];
                        continue;
                    }

                    if(angle < target) {
                        angles[i] += turn;
                    } else {
                        angles[i] -= turn;
                    }
                }

                return !didMove;
            }

            public double[] getPositions(float interp) {
                return new double[] {
                        BobMathUtil.interp(this.prevAngles[0], this.angles[0], interp),
                        BobMathUtil.interp(this.prevAngles[1], this.angles[1], interp),
                        BobMathUtil.interp(this.prevAngles[2], this.angles[2], interp),
                        BobMathUtil.interp(this.prevAngles[3], this.angles[3], interp),
                        BobMathUtil.interp(this.prevSawAngle, this.sawAngle, interp)
                };
            }
        }
    }

    /*
     * Arms cycle through REPOSITION -> EXTEND -> CUT (if saw) -> RETRACT
     * If transit is planned, the carriage's state will change to RETIRING
     * If the carriage is RETIRING, each arm will enter RETIRE state after RETRACT
     * Once the arm has returned to null position, it changes to WAIT
     * If both arms WAIT, the carriage switches to SLIDING
     * Once transit is done, carriage returns to WORKING
     * If the carriage is WORKING, any arm that is in the WAIT state will return to REPOSITION
     */

    public enum AFArmState {
        WORKING,
        RETIRING, // waiting for arms to enter WAITING state
        SLIDING // transit to next position
    }

    public enum ArmState {
        REPOSITION,
        EXTEND,
        CUT,
        RETRACT,
        RETIRE, // return to null position for carriage transit
        WAIT // either waiting for or in the middle of carriage transit
    }
}
