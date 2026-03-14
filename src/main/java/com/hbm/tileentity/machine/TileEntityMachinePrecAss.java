package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachinePrecAss;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachinePrecAss;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachinePrecAss;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;

import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

// horribly copy-pasted crap device
@AutoRegister
public class TileEntityMachinePrecAss extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IGUIProvider {

    public FluidTankNTM inputTank;
    public FluidTankNTM outputTank;

    public long power;
    public long maxPower = 100_000;
    public boolean didProcess = false;

    public boolean frame = false;
    private AudioWrapper audio;

    public ModuleMachinePrecAss assemblerModule;

    public double prevRing;
    public double ring;
    public double ringSpeed;
    public double ringTarget;
    public int ringDelay;

    public double[] armAngles = new double[] {45, -15, -5};
    public double[] prevArmAngles = new double[] {45, -15, -5};
    public double[] strikers = new double[4];
    public double[] prevStrikers = new double[4];
    public boolean[] strikerDir = new boolean[4];
    protected int strikerIndex;
    protected int strikerDelay;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public TileEntityMachinePrecAss() {
        super(22, true, true);
        this.inputTank = new FluidTankNTM(Fluids.NONE, 4_000);
        this.outputTank = new FluidTankNTM(Fluids.NONE, 4_000);

        this.assemblerModule = new ModuleMachinePrecAss(0, this, inventory)
                .itemInput(4).itemOutput(13)
                .fluidInput(inputTank).fluidOutput(outputTank);
    }

    @Override
    public String getDefaultName() {
        return "container.machinePrecAss";
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!world.isRemote) {

            GenericRecipe recipe = PrecAssRecipes.INSTANCE.recipeNameMap.get(assemblerModule.recipe);
            if(recipe != null) {
                this.maxPower = recipe.power * 100;
            }
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            upgradeManager.checkSlots(inventory, 2, 3);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), world, pos);
                if(outputTank.getFill() > 0) this.tryProvide(outputTank, world, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.assemblerModule.update(speed, pow, true, inventory.getStackInSlot(1));
            this.didProcess = this.assemblerModule.didProcess;
            if(this.assemblerModule.markDirty) this.markDirty();

            this.networkPackNT(100);

        } else {

            if(world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getMaterial() != Material.AIR;
            }

            if(this.didProcess && MainRegistry.proxy.me().getDistance(pos.getX() , pos.getY(), pos.getZ()) < 50) {
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

            System.arraycopy(this.armAngles, 0, this.prevArmAngles, 0, 3);
            System.arraycopy(this.strikers, 0, this.prevStrikers, 0, 4);

            this.prevRing = this.ring;

            for(int i = 0; i < 4; i++) {
                if(this.strikerDir[i]) {
                    this.strikers[i] = -0.75D;
                    this.strikerDir[i] = false;
                    if(!this.muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStrike, SoundCategory.BLOCKS, this.getVolume(0.5F), 1.25F);
                } else {
                    this.strikers[i] = MathHelper.clamp(this.strikers[i] + 0.5D, -0.75D, 0D);
                }
            }

            if(this.ring != this.ringTarget) {
                double ringDelta = Math.abs(this.ringTarget - this.ring);
                if(ringDelta <= this.ringSpeed) this.ring = this.ringTarget;
                if(this.ringTarget > this.ring) this.ring += this.ringSpeed;
                if(this.ringTarget < this.ring) this.ring -= this.ringSpeed;
                if(this.ringTarget == this.ring) {
                    double sub = ringTarget >= 360 ? -360D : 360D;
                    this.ringTarget += sub;
                    this.ring += sub;
                    this.prevRing += sub;
                    this.ringDelay = 100 + world.rand.nextInt(21);
                }
            }

            if(didProcess) {
                if(this.ring == this.ringTarget) {
                    if(this.ringDelay > 0) this.ringDelay--;
                    if(this.ringDelay <= 0) {
                        this.ringTarget += 45 * (world.rand.nextBoolean() ? -1 : 1);
                        this.ringSpeed = 10D + world.rand.nextDouble() * 5D;
                        if(!this.muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStart, SoundCategory.BLOCKS, this.getVolume(0.25F), 1.25F + world.rand.nextFloat() * 0.25F);
                    }
                }

                if(!isInWorkingPosition(this.armAngles) && canArmsMove()) {
                    move(WORKING_POSITION);
                }

                if(isInWorkingPosition(this.armAngles)) {
                    this.strikerDelay--;
                    if(this.strikerDelay <= 0) {
                        this.strikerDir[this.strikerIndex] = true;
                        this.strikerIndex = (this.strikerIndex + 1) % this.strikers.length;
                        this.strikerDelay = this.strikerIndex == 3 ? (10 + world.rand.nextInt(3)) : 2;
                    }
                }

            } else {
                for(int i = 0; i < 4; i++) this.strikerDir[i] = false; // set all strikers to retract
                if(canArmsMove()) move(NULL_POSITION);
            }

            if(this.isInWorkingPosition(prevArmAngles) && !this.isInWorkingPosition(armAngles)) {
                if(!this.muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStop, SoundCategory.BLOCKS, this.getVolume(0.25F), 1.25F + world.rand.nextFloat() * 0.25F);
            }
        }
    }

    public double[] NULL_POSITION = new double[] {45, -30, 45};
    public double[] WORKING_POSITION = new double[] {45, -15, -5};

    private boolean canArmsMove() {
        for(int i = 0; i < 4; i++) if(this.strikers[i] != 0) return false;
        return true;
    }

    private boolean isInWorkingPosition(double[] arms) {
        for(int i = 0; i < 3; i++) if(arms[i] != WORKING_POSITION[i]) return false;
        return true;
    }

    private void move(double[] targetAngles) {
        for(int i = 0; i < armAngles.length; i++) {
            if(armAngles[i] == targetAngles[i]) continue;
            double angle = armAngles[i];
            double target = targetAngles[i];
            double turn = 15D;
            double delta = Math.abs(angle - target);

            if(delta <= turn) { armAngles[i] = targetAngles[i]; continue; }
            if(angle < target) armAngles[i] += turn;
            else armAngles[i] -= turn;
        }
    }

    @Override public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.motor, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.5F, 15F, 0.75F, 20);
    }

    @Override public void onChunkUnload() {
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    @Override public void invalidate() {
        super.invalidate();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        this.inputTank.serialize(buf);
        this.outputTank.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeBoolean(didProcess);
        this.assemblerModule.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.inputTank.deserialize(buf);
        this.outputTank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.assemblerModule.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.inputTank.readFromNBT(nbt, "i");
        this.outputTank.readFromNBT(nbt, "o");
        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.assemblerModule.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.inputTank.writeToNBT(nbt, "i");
        this.outputTank.writeToNBT(nbt, "o");
        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        this.assemblerModule.writeToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        if(slot == 1 && stack.getItem() == ModItems.blueprints) return true;
        if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
        return this.assemblerModule.isItemValid(slot, stack); // recipe input crap
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i >= 13 || this.assemblerModule.isSlotClogged(i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {inputTank}; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {outputTank}; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTank, outputTank}; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachinePrecAss(player.inventory, this.inventory); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachinePrecAss(player.inventory, this); }

    @Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index == 0) {
                this.assemblerModule.recipe = selection;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
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
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_precass));
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
