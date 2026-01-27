package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineAssemblyMachine;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineAssemblyMachine;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineAssembler;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
@AutoRegister
public class TileEntityMachineAssemblyMachine extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IGUIProvider {

    public FluidTankNTM inputTank;
    public FluidTankNTM outputTank;

    public long power;
    public long maxPower = 100_000;
    public boolean didProcess = false;

    public boolean frame = false;
    private AudioWrapper audio;

    public ModuleMachineAssembler assemblerModule;

    public AssemblerArm[] arms = new AssemblerArm[2];
    public double prevRing;
    public double ring;
    public double ringSpeed;
    public double ringTarget;
    public int ringDelay;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public TileEntityMachineAssemblyMachine() {
        super(17, true, true);
        this.inputTank = new FluidTankNTM(Fluids.NONE, 4_000);
        this.outputTank = new FluidTankNTM(Fluids.NONE, 4_000);

        for(int i = 0; i < this.arms.length; i++) this.arms[i] = new AssemblerArm();

        this.assemblerModule = new ModuleMachineAssembler(0, this, inventory)
                .itemInput(4).itemOutput(16)
                .fluidInput(inputTank).fluidOutput(outputTank);
    }

    @Override
    public String getDefaultName() {
        return "container.machineAssemblyMachine";
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!world.isRemote) {

            GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assemblerModule.recipe);
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

            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.assemblerModule.update(speed, pow, true, inventory.getStackInSlot(1));
            this.didProcess = this.assemblerModule.didProcess;
            if(this.assemblerModule.markDirty) this.markDirty();

            if(didProcess) {
                if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.meteorite_sword_alloyed)
                    inventory.setStackInSlot(0, new ItemStack(ModItems.meteorite_sword_machined));
            }

            this.networkPackNT(100);

        } else {

            if(world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getBlock() != Blocks.AIR;
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

            for(AssemblerArm arm : arms) {
                arm.updateInterp();
                if(didProcess) {
                    arm.updateArm();
                } else{
                    arm.returnToNullPos();
                }

                if(!this.muffled && arm.prevAngles[3] != arm.angles[3] && arm.angles[3] == -0.75) {
                    MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStrike, SoundCategory.BLOCKS, this.getVolume(0.5F), 1F);
                }
            }

            this.prevRing = this.ring;

            if(didProcess) {
                if(this.ring != this.ringTarget) {
                    double ringDelta = Math.abs(this.ringTarget - this.ring);
                    if(ringDelta <= this.ringSpeed) this.ring = this.ringTarget;
                    if(this.ringTarget > this.ring) this.ring += this.ringSpeed;
                    if(this.ringTarget < this.ring) this.ring -= this.ringSpeed;
                    if(this.ringTarget == this.ring) {
                        if(ringTarget >= 360) {
                            this.ringTarget -= 360D;
                            this.ring -= 360D;
                            this.prevRing -= 360D;
                        }
                        if(ringTarget <= -360) {
                            this.ringTarget += 360D;
                            this.ring += 360D;
                            this.prevRing += 360D;
                        }
                        this.ringDelay = 20 + world.rand.nextInt(21);
                        //MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), "hbm:block.assemblerStop", this.getVolume(0.25F), 1.5F);
                    }
                } else {
                    if(this.ringDelay > 0) this.ringDelay--;
                    if(this.ringDelay <= 0) {
                        this.ringTarget += (world.rand.nextDouble() * 2 - 1) * 135;
                        this.ringSpeed = 10D + world.rand.nextDouble() * 5D;
                        if(!this.muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStart, SoundCategory.BLOCKS, this.getVolume(0.25F), 1.25F + world.rand.nextFloat() * 0.25F);
                    }
                }
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
        boolean wasProcessing = this.didProcess;
        this.inputTank.deserialize(buf);
        this.outputTank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.assemblerModule.deserialize(buf);

        if(wasProcessing && !didProcess) {
            MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.assemblerStop, SoundCategory.BLOCKS, this.getVolume(0.25F), 1.5F);
        }
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
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
        if(this.assemblerModule.isItemValid(slot, stack)) return true; // recipe input crap
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 16 || this.assemblerModule.isSlotClogged(i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {inputTank}; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {outputTank}; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTank, outputTank}; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineAssemblyMachine(player.inventory, getCheckedInventory()); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineAssemblyMachine(player.inventory, this); }

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
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_assembly_machine));
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

    public static class AssemblerArm {

        public double[] angles = new double[4];
        public double[] prevAngles = new double[4];
        public double[] targetAngles = new double[4];
        public double[] speed = new double[4];

        Random rand = new Random();
        ArmActionState state = ArmActionState.ASSUME_POSITION;
        int actionDelay = 0;

        public static enum ArmActionState {
            ASSUME_POSITION,
            EXTEND_STRIKER,
            RETRACT_STRIKER
        }

        public AssemblerArm() {
            this.resetSpeed();
        }

        private void updateInterp() {
            for(int i = 0; i < angles.length; i++) {
                prevAngles[i] = angles[i];
            }
        }

        private void returnToNullPos() {
            for(int i = 0; i < 4; i++) this.targetAngles[i] = 0;
            for(int i = 0; i < 3; i++) this.speed[i] = 3;
            this.speed[3] = 0.25;
            this.state = ArmActionState.RETRACT_STRIKER;

            this.move();
        }

        private void resetSpeed() {
            speed[0] = 15;	//Pivot
            speed[1] = 15;	//Arm
            speed[2] = 15;	//Piston
            speed[3] = 0.5;	//Striker
        }

        public void updateArm() {
            resetSpeed();

            if(actionDelay > 0) {
                actionDelay--;
                return;
            }

            switch(state) {
                // Move. If done moving, set a delay and progress to EXTEND
                case ASSUME_POSITION:
                    if(move()) {
                        actionDelay = 2;
                        state = ArmActionState.EXTEND_STRIKER;
                        targetAngles[3] = -0.75D;
                    }
                    break;
                case EXTEND_STRIKER:
                    if(move()) {
                        state = ArmActionState.RETRACT_STRIKER;
                        targetAngles[3] = 0D;
                    }
                    break;
                case RETRACT_STRIKER:
                    if(move()) {
                        actionDelay = 2 + rand.nextInt(5);
                        chooseNewArmPoistion();
                        state = ArmActionState.ASSUME_POSITION;
                    }
                    break;

            }
        }

        private double[][] pos = new double[][] { // possible positions for the arms
                {45, -15, -5},
                {15, 15, -15},
                {25, 10, -15},
                {30, 0, -10},
                {70, -10, -25},
        }; // sure it's not truly random like with the old assemfac, but at least now the striker always hits the center and doesn't clip through the board

        public void chooseNewArmPoistion() {
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
                    BobMathUtil.interp(this.prevAngles[3], this.angles[3], interp)
            };
        }
    }
}
