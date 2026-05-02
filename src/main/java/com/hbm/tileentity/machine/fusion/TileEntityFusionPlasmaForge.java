package com.hbm.tileentity.machine.fusion;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.container.ContainerMachinePlasmaForge;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachinePlasmaForge;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachinePlasma;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

import static com.hbm.inventory.OreDictManager.*;

@AutoRegister
public class TileEntityFusionPlasmaForge extends TileEntityMachineBase implements ITickable, IFusionPowerReceiver, IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver, IGUIProvider, IConnectionAnchors {

    public FluidTankNTM inputTank;

    public long power;
    public long maxPower = 10_000_000;
    public boolean didProcess;

    public float plasmaRed;
    public float plasmaGreen;
    public float plasmaBlue;
    public long plasmaEnergy;
    public long plasmaEnergySync;
    public double neutronEnergy;
    protected PlasmaNetwork.PlasmaNode receiverNode;
    protected PlasmaNetwork.PlasmaNode providerNode;
    public boolean connected;

    public int booster;
    public int maxBooster;

    public int timeOffset = -1;

    public ModuleMachinePlasma plasmaModule;

    public double prevRing;
    public double ring;
    public double ringSpeed;
    public double ringTarget;
    public int ringDelay;
    public ForgeArm armStriker;
    public ForgeArm armJet;

    public static final List<Tuple.Pair<AStack, Integer>> boosters = new ArrayList<>();

    static {
        boosters.add(new Tuple.Pair<>(new OreDictStack(CO60.nugget()), 20));
        boosters.add(new Tuple.Pair<>(new OreDictStack(CO60.billet()), 120));
        boosters.add(new Tuple.Pair<>(new OreDictStack(CO60.ingot()), 200));
        boosters.add(new Tuple.Pair<>(new OreDictStack(CO60.dust()), 200));
        boosters.add(new Tuple.Pair<>(new OreDictStack(SR90.nugget()), 40));
        boosters.add(new Tuple.Pair<>(new OreDictStack(SR90.dustTiny()), 40));
        boosters.add(new Tuple.Pair<>(new OreDictStack(SR90.billet()), 240));
        boosters.add(new Tuple.Pair<>(new OreDictStack(SR90.ingot()), 400));
        boosters.add(new Tuple.Pair<>(new OreDictStack(SR90.dust()), 400));
        boosters.add(new Tuple.Pair<>(new OreDictStack(AU198.nugget()), 60));
        boosters.add(new Tuple.Pair<>(new OreDictStack(AU198.billet()), 360));
        boosters.add(new Tuple.Pair<>(new OreDictStack(AU198.ingot()), 600));
        boosters.add(new Tuple.Pair<>(new OreDictStack(AU198.dust()), 600));
        boosters.add(new Tuple.Pair<>(new OreDictStack(I131.dustTiny()), 60));
        boosters.add(new Tuple.Pair<>(new OreDictStack(I131.dust()), 600));
        boosters.add(new Tuple.Pair<>(new OreDictStack(XE135.dustTiny()), 60));
        boosters.add(new Tuple.Pair<>(new OreDictStack(XE135.dust()), 600));
        boosters.add(new Tuple.Pair<>(new OreDictStack(CS137.dustTiny()), 50));
        boosters.add(new Tuple.Pair<>(new OreDictStack(CS137.dust()), 500));
        boosters.add(new Tuple.Pair<>(new OreDictStack(AT209.dust()), 1_200));
    }

    public TileEntityFusionPlasmaForge() {
        super(16, true, true);
        this.inputTank = new FluidTankNTM(Fluids.NONE, 16_000).withOwner(this);
        this.plasmaModule = new ModuleMachinePlasma(0, this, inventory).itemInput(3).itemOutput(15).fluidInput(inputTank);
        this.armStriker = new ForgeArm(ForgeArmType.STRIKER);
        this.armJet = new ForgeArm(ForgeArmType.JET);
    }

    @Override
    public String getDefaultName() {
        return "container.machinePlasmaForge";
    }

    @Override public boolean receivesFusionPower() { return true; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        this.plasmaEnergy = fusionPower;
        this.neutronEnergy = neutronPower;
        this.plasmaRed = r;
        this.plasmaGreen = g;
        this.plasmaBlue = b;
    }

    @Override
    public void update() {

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!world.isRemote) {
            this.plasmaEnergySync = this.plasmaEnergy;
            this.plasmaEnergy = 0;

            if(booster <= 0 && !inventory.getStackInSlot(2).isEmpty()) {
                for(Tuple.Pair<AStack, Integer> booster : boosters) {
                    if(booster.getKey().matchesRecipe(inventory.getStackInSlot(2), true)) {
                        this.maxBooster = this.booster = booster.getValue();
                        ItemStack stack = inventory.getStackInSlot(2).copy();
                        stack.shrink(1);
                        inventory.setStackInSlot(2, stack.getCount() <= 0 ? ItemStack.EMPTY : stack);
                        break;
                    }
                }
            }

            ForgeDirection rot = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
            if(receiverNode == null || receiverNode.expired) receiverNode = this.createNode(rot);
            if(providerNode == null || providerNode.expired) providerNode = this.createNode(rot.getOpposite());

            if(receiverNode != null && receiverNode.hasValidNet()) receiverNode.net.addReceiver(this);
            if(providerNode != null && providerNode.hasValidNet()) providerNode.net.addProvider(this);

            PlasmaForgeRecipe recipe = PlasmaForgeRecipes.INSTANCE.recipeNameMap.get(plasmaModule.recipe);
            if(recipe != null) this.maxPower = recipe.power * 100;

            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);
            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), world, pos);
            }

            double speed = booster > 0 ? 4D : 1D;
            boolean ignition = recipe == null || recipe.ignitionTemp <= this.plasmaEnergySync;

            this.plasmaModule.update(speed, 1D, ignition, inventory.getStackInSlot(1));
            this.didProcess = this.plasmaModule.didProcess;
            if(this.plasmaModule.markDirty) this.markDirty();

            long powerReceived = (long) Math.ceil(this.plasmaEnergySync * 0.75D);
            this.connected = providerNode != null && providerNode.hasValidNet() && !providerNode.net.receiverEntries.isEmpty();

            if(providerNode != null && providerNode.hasValidNet()) {
                for(Map.Entry<TileEntity, Long> entry : providerNode.net.receiverEntries.entrySet()) {
                    if(entry.getKey() instanceof IFusionPowerReceiver receiver && powerReceived > 0) {
                        receiver.receiveFusionPower(powerReceived, this.neutronEnergy, plasmaRed, plasmaGreen, plasmaBlue);
                    }
                }
            }

            if(this.didProcess && this.booster > 0) this.booster--;
            this.neutronEnergy = 0D;

            this.networkPackNT(100);
        } else {
            if(timeOffset == -1) this.timeOffset = world.rand.nextInt(30_000);

            this.armStriker.updateArm();
            this.armJet.updateArm();
            this.prevRing = this.ring;

            if(didProcess) {
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
                        this.ringDelay = 100 + world.rand.nextInt(41);
                    }
                } else {
                    if(this.ringDelay > 0) this.ringDelay--;
                    if(this.ringDelay <= 0) {
                        this.ringTarget += (world.rand.nextDouble() + 1D) * 60D * (world.rand.nextBoolean() ? -1D : 1D);
                        this.ringSpeed = 2.5D;
                    }
                }
            }
        }
    }

    public PlasmaNetwork.PlasmaNode createNode(ForgeDirection dir) {
        PlasmaNetwork.PlasmaNode node = UniNodespace.getNode(world, pos.add(dir.offsetX * 5, 2, dir.offsetZ * 5), PlasmaNetwork.THE_PROVIDER);
        if(node != null) return node;

        node = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(PlasmaNetwork.THE_PROVIDER, new BlockPos(pos.getX() + dir.offsetX * 5, pos.getY() + 2, pos.getZ() + dir.offsetZ * 5))
                .setConnections(new DirPos(pos.getX() + dir.offsetX * 6, pos.getY() + 2, pos.getZ() + dir.offsetZ * 6, dir));
        UniNodespace.createNode(world, node);
        return node;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(!world.isRemote) {
            if(receiverNode != null) UniNodespace.destroyNode(world, receiverNode);
            if(providerNode != null) UniNodespace.destroyNode(world, providerNode);
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 6 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 6 - rot.offsetZ * 2, dir),
                new DirPos(pos.getX() + dir.offsetX * 6 - rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 6 - rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 6, pos.getY(), pos.getZ() + dir.offsetZ * 6, dir),
                new DirPos(pos.getX() + dir.offsetX * 6 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 6 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 6 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 6 + rot.offsetZ * 2, dir),
                new DirPos(pos.getX() - dir.offsetX * 6 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 6 - rot.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6 - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 6 - rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6, pos.getY(), pos.getZ() - dir.offsetZ * 6, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 6 + rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6 + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 6 + rot.offsetZ * 2, dir.getOpposite())
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        this.inputTank.serialize(buf);
        buf.writeFloat(plasmaRed);
        buf.writeFloat(plasmaGreen);
        buf.writeFloat(plasmaBlue);
        buf.writeLong(plasmaEnergySync);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeBoolean(didProcess);
        buf.writeBoolean(connected);
        buf.writeInt(booster);
        buf.writeInt(maxBooster);
        this.plasmaModule.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.inputTank.deserialize(buf);
        this.plasmaRed = buf.readFloat();
        this.plasmaGreen = buf.readFloat();
        this.plasmaBlue = buf.readFloat();
        this.plasmaEnergySync = buf.readLong();
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.connected = buf.readBoolean();
        this.booster = buf.readInt();
        this.maxBooster = buf.readInt();
        this.plasmaModule.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.inputTank.readFromNBT(nbt, "i");
        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.booster = nbt.getInteger("booster");
        this.maxBooster = nbt.getInteger("maxBooster");
        this.plasmaModule.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.inputTank.writeToNBT(nbt, "i");
        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        nbt.setInteger("booster", booster);
        nbt.setInteger("maxBooster", maxBooster);
        this.plasmaModule.writeToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true;
        if(slot == 1 && stack.getItem() == ModItems.blueprints) return true;
        if(this.plasmaModule.isItemValid(slot, stack)) return true;
        if(slot == 2) {
            for(Tuple.Pair<AStack, Integer> booster : boosters) {
                if(booster.getKey().matchesRecipe(stack, true)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 15 || this.plasmaModule.isSlotClogged(i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {inputTank}; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {inputTank}; }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index == 0) {
                this.plasmaModule.recipe = selection;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) bb = new AxisAlignedBB(pos.getX() - 5, pos.getY(), pos.getZ() - 5, pos.getX() + 5, pos.getY() + 6, pos.getZ() + 6);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachinePlasmaForge(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachinePlasmaForge(player.inventory, this);
    }

    public class ForgeArm {

        public ForgeArmType type;
        public ForgeArmState state = ForgeArmState.RETIRE;
        public double[] angles;
        public double[] prevAngles;
        public double[] targetAngles;
        public double[] speed;
        public int actionDelay;

        public ForgeArm(ForgeArmType type) {
            this.type = type;
            this.angles = new double[type.angleCount];
            this.prevAngles = new double[type.angleCount];
            this.targetAngles = new double[type.angleCount];
            this.speed = new double[type.angleCount];

            for(int i = 0; i < speed.length; i++) {
                if(i < 3 || i == 4) speed[i] = 15;
                if(i > 4) speed[i] = 0.5;
            }
        }

        public void updateArm() {
            for(int i = 0; i < angles.length; i++) prevAngles[i] = angles[i];

            if(!didProcess()) this.state = ForgeArmState.RETIRE;
            if(this.state == ForgeArmState.RETIRE) this.actionDelay = 0;

            if(this.actionDelay > 0) {
                this.actionDelay--;
                return;
            }

            this.type.stateMachine.accept(this);
        }

        public boolean didProcess() {
            return didProcess;
        }

        public boolean move() {
            boolean didMove = false;

            for(int i = 0; i < angles.length; i++) {
                if(angles[i] == targetAngles[i]) continue;
                didMove = true;

                double angle = angles[i];
                double target = targetAngles[i];
                double turn = speed[i];
                double delta = Math.abs(angle - target);

                if(delta <= turn) {
                    angles[i] = targetAngles[i];
                    continue;
                }
                if(angle < target) angles[i] += turn;
                else angles[i] -= turn;
            }

            return !didMove;
        }

        public void playStrikerSound() {
            if(!muffled) MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.boltgun, SoundCategory.BLOCKS, getVolume(0.25F), 1.25F);
        }

        public double[] getPositions(float interp) {
            double[] positions = new double[this.angles.length];
            for(int i = 0; i < positions.length; i++) positions[i] = BobMathUtil.interp(this.prevAngles[i], this.angles[i], interp);
            return positions;
        }
    }

    public enum ForgeArmType {
        STRIKER(STRIKER_STATE_MACHINE, 6),
        JET(JET_STATE_MACHINE, 4);

        protected final int angleCount;
        protected final Consumer<ForgeArm> stateMachine;

        ForgeArmType(Consumer<ForgeArm> stateMachine, int angleCount) {
            this.stateMachine = stateMachine;
            this.angleCount = angleCount;
        }
    }

    public enum ForgeArmState {
        REPOSITION,
        EXTEND1,
        EXTEND2,
        RETRACT1,
        RETRACT2,
        RETIRE
    }

    public static final Random rand = new Random();

    public static final double[][] strikerPositions = new double[][] {
            {20, -30, -20, 30},
            {45, -80, 15, 30},
            {30, -45, -10, 30},
            {15, -20, -30, 30},
            {0, 10, -55, 30}
    };

    public static final double[][] jetPositions = new double[][] {
            {10, 45, -120},
            {20, 45, -140},
            {0, 30, -80},
            {0, 40, -100},
            {30, 50, -160}
    };

    public static final Consumer<ForgeArm> STRIKER_STATE_MACHINE = arm -> {
        switch(arm.state) {
            case REPOSITION:
                if(arm.move()) {
                    arm.actionDelay = 5;
                    arm.state = ForgeArmState.EXTEND1;
                    arm.targetAngles[4] = 0.5D;
                }
                break;
            case EXTEND1:
                if(arm.move()) {
                    arm.state = ForgeArmState.RETRACT1;
                    arm.targetAngles[4] = 0D;
                    arm.playStrikerSound();
                }
                break;
            case RETRACT1:
                if(arm.move()) {
                    arm.state = ForgeArmState.EXTEND2;
                    arm.targetAngles[5] = 0.5D;
                }
                break;
            case EXTEND2:
                if(arm.move()) {
                    arm.state = ForgeArmState.RETRACT2;
                    arm.targetAngles[5] = 0D;
                    arm.playStrikerSound();
                }
                break;
            case RETRACT2:
                if(arm.move()) {
                    if(rand.nextInt(3) == 0) {
                        arm.actionDelay = 10;
                        arm.state = ForgeArmState.REPOSITION;
                        choosePosition(arm, strikerPositions);
                    } else {
                        arm.actionDelay = 5;
                        arm.state = ForgeArmState.EXTEND1;
                        arm.targetAngles[4] = 0.5D;
                    }
                }
                break;
            case RETIRE:
                for(int i = 0; i < arm.targetAngles.length; i++) arm.targetAngles[i] = 0;
                if(arm.move()) {
                    arm.actionDelay = 10;
                    arm.state = ForgeArmState.REPOSITION;
                    choosePosition(arm, strikerPositions);
                }
                break;
            default:
                break;
        }
    };

    public static final Consumer<ForgeArm> JET_STATE_MACHINE = arm -> {
        switch(arm.state) {
            case REPOSITION:
                if(arm.move()) {
                    arm.actionDelay = 20 + rand.nextInt(3) * 10;
                    arm.state = ForgeArmState.REPOSITION;
                    choosePosition(arm, jetPositions);
                }
                break;
            case RETIRE:
                Arrays.fill(arm.targetAngles, 0);
                if(arm.move()) {
                    arm.actionDelay = 10;
                    arm.state = ForgeArmState.REPOSITION;
                    choosePosition(arm, jetPositions);
                }
                break;
            default:
                break;
        }
    };

    public static void choosePosition(ForgeArm arm, double[][] positions) {
        double[] newPos = positions[rand.nextInt(positions.length)];
        System.arraycopy(newPos, 0, arm.targetAngles, 0, newPos.length);
    }
}
