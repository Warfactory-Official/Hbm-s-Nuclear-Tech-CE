package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerAssemfac;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIAssemfac;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.Random;

@AutoRegister
public class TileEntityMachineAssemfac extends TileEntityMachineAssemblerBase implements IFluidStandardTransceiver, IGUIProvider, IFluidCopiable {

    private static final int invSize = 117;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public AssemblerArm[] arms;
    public FluidTankNTM water;
    public FluidTankNTM steam;
    AxisAlignedBB bb = null;
    ImmutablePair<BlockPos, ForgeDirection>[] inpos;
    ImmutablePair<BlockPos, ForgeDirection>[] outpos;

    public TileEntityMachineAssemfac() {
        super(invSize, true, true); //8 assembler groups with 14 slots, 4 upgrade slots, 1 battery slot

        arms = new AssemblerArm[6];
        for (int i = 0; i < arms.length; i++) {
            arms[i] = new AssemblerArm(i % 3 == 1 ? 1 : 0); //the second of every group of three becomes a welder
        }

        water = new FluidTankNTM(Fluids.WATER, 64_000);
        steam = new FluidTankNTM(Fluids.SPENTSTEAM, 64_000);

        inventory = new ItemStackHandler(invSize) {
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
    public String getDefaultName() {
        return "container.assemfac";
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {

            if (world.getTotalWorldTime() % 20 == 0) {
                this.updateConnections();
            }

            this.speed = 100;
            this.consumption = 100;

            upgradeManager.checkSlots(inventory, 1, 4);

            int speedLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 6);
            int powerLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);
            int overLevel = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);

            this.speed -= speedLevel * 15;
            this.consumption += speedLevel * 300;
            this.speed += powerLevel * 5;
            this.consumption -= powerLevel * 30;
            this.speed /= (overLevel + 1);
            this.consumption *= (overLevel + 1);

            for (DirPos pos : getConPos()) {
                this.sendFluid(steam, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            this.networkPackNT(150);

        } else {

            for (AssemblerArm arm : arms) {
                arm.updateInterp();
                if (isProgressing) {
                    arm.updateArm();
                }
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
        water.serialize(buf);
        steam.serialize(buf);
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
        water.deserialize(buf);
        steam.deserialize(buf);
    }

    private int getWaterRequired() {
        return 1000 / this.speed;
    }

    @Override
    protected boolean canProcess(int index) {
        return super.canProcess(index) && this.water.getFill() >= getWaterRequired() && this.steam.getFill() + getWaterRequired() <= this.steam.getMaxFill();
    }

    @Override
    protected void process(int index) {
        super.process(index);
        this.water.setFill(this.water.getFill() - getWaterRequired());
        this.steam.setFill(this.steam.getFill() + getWaterRequired());
    }

    private void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(water.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    public DirPos[] getConPos() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[]{
                new DirPos(pos.getX() - dir.offsetX * 3 + rot.offsetX * 5, pos.getY(), pos.getZ() - dir.offsetZ * 3 + rot.offsetZ * 5, rot),
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 5, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 5, rot),
                new DirPos(pos.getX() - dir.offsetX * 3 - rot.offsetX * 4, pos.getY(), pos.getZ() - dir.offsetZ * 3 - rot.offsetZ * 4, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 4, rot.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 5 + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ * 5 + rot.offsetZ * 3, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 5 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 5 - rot.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 4 + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 3, dir),
                new DirPos(pos.getX() + dir.offsetX * 4 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 2, dir)
        };
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
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public long getMaxPower() {
        return 10_000_000;
    }

    @Override
    public int getRecipeCount() {
        return 8;
    }

    @Override
    public int getTemplateIndex(int index) {
        return 17 + index * 14;
    }

    @Override
    public int[] getSlotIndicesFromIndex(int index) {
        return new int[]{5 + index * 14, 16 + index * 14, 18 + index * 14};
    }

    @Override
    public ImmutablePair<BlockPos, ForgeDirection>[] getInputPositions() {

        if (inpos != null)
            return inpos;

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        inpos = new ImmutablePair[]{
                ImmutablePair.of(new BlockPos(getPos().getX() + dir.offsetX * 4 - rot.offsetX, getPos().getY(), getPos().getZ() + dir.offsetZ * 4 - rot.offsetZ), dir),
                ImmutablePair.of(new BlockPos(getPos().getX() - dir.offsetX * 5 + rot.offsetX * 2, getPos().getY(), getPos().getZ() - dir.offsetZ * 5 + rot.offsetZ * 2), dir.getOpposite()),
                ImmutablePair.of(new BlockPos(getPos().getX() - dir.offsetX * 2 - rot.offsetX * 4, getPos().getY(), getPos().getZ() - dir.offsetZ * 2 - rot.offsetZ * 4), rot.getOpposite()),
                ImmutablePair.of(new BlockPos(getPos().getX() + dir.offsetX + rot.offsetX * 5, getPos().getY(), getPos().getZ() + dir.offsetZ + rot.offsetZ * 5), rot)
        };

        return inpos;
    }

    @Override
    public ImmutablePair<BlockPos, ForgeDirection>[] getOutputPositions() {

        if (outpos != null)
            return outpos;

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        outpos = new ImmutablePair[]{
                ImmutablePair.of(new BlockPos(getPos().getX() + dir.offsetX * 4 + rot.offsetX * 2, getPos().getY(), getPos().getZ() + dir.offsetZ * 4 + rot.offsetZ * 2), dir),
                ImmutablePair.of(new BlockPos(getPos().getX() - dir.offsetX * 5 - rot.offsetX * 1, getPos().getY(), getPos().getZ() - dir.offsetZ * 5 - rot.offsetZ * 1), dir.getOpposite()),
                ImmutablePair.of(new BlockPos(getPos().getX() + dir.offsetX * 1 - rot.offsetX * 4, getPos().getY(), getPos().getZ() + dir.offsetZ * 1 - rot.offsetZ * 4), rot.getOpposite()),
                ImmutablePair.of(new BlockPos(getPos().getX() - dir.offsetX * 2 + rot.offsetX * 5, getPos().getY(), getPos().getZ() - dir.offsetZ * 2 + rot.offsetZ * 5), rot)
        };

        return outpos;
    }

    @Override
    public int getPowerSlot() {
        return 0;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{steam};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{water};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{water, steam};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerAssemfac(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIAssemfac(player.inventory, this);
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return null;
    }

    public static class AssemblerArm {
        public double[] angles = new double[4];
        public double[] prevAngles = new double[4];
        public double[] targetAngles = new double[4];
        public double[] speed = new double[4];

        Random rand = new Random();

        int actionMode;
        ArmActionState state;
        int actionDelay = 0;

        public AssemblerArm(int actionMode) {
            this.actionMode = actionMode;

            if (this.actionMode == 0) {
                speed[0] = 15;    //Pivot
                speed[1] = 15;    //Arm
                speed[2] = 15;    //Piston
                speed[3] = 0.5;    //Striker
            } else if (this.actionMode == 1) {
                speed[0] = 3;        //Pivot
                speed[1] = 3;        //Arm
                speed[2] = 1;        //Piston
                speed[3] = 0.125;    //Striker
            }

            state = ArmActionState.ASSUME_POSITION;
            chooseNewArmPoistion();
            actionDelay = rand.nextInt(20);
        }

        public void updateArm() {

            if (actionDelay > 0) {
                actionDelay--;
                return;
            }

            switch (state) {
                //Move. If done moving, set a delay and progress to EXTEND
                case ASSUME_POSITION:
                    if (move()) {
                        if (this.actionMode == 0) {
                            actionDelay = 2;
                        } else if (this.actionMode == 1) {
                            actionDelay = 10;
                        }
                        state = ArmActionState.EXTEND_STRIKER;
                        targetAngles[3] = 1D;
                    }
                    break;
                case EXTEND_STRIKER:
                    if (move()) {
                        if (this.actionMode == 0) {
                            state = ArmActionState.RETRACT_STRIKER;
                            targetAngles[3] = 0D;
                        } else if (this.actionMode == 1) {
                            state = ArmActionState.WELD;
                            targetAngles[2] -= 20;
                            actionDelay = 5 + rand.nextInt(5);
                        }
                    }
                    break;
                case WELD:
                    if (move()) {
                        state = ArmActionState.RETRACT_STRIKER;
                        targetAngles[3] = 0D;
                        actionDelay = 10 + rand.nextInt(5);
                    }
                    break;
                case RETRACT_STRIKER:
                    if (move()) {
                        if (this.actionMode == 0) {
                            actionDelay = 2 + rand.nextInt(5);
                        } else if (this.actionMode == 1) {
                            actionDelay = 5 + rand.nextInt(3);
                        }
                        chooseNewArmPoistion();
                        state = ArmActionState.ASSUME_POSITION;
                    }
                    break;

            }
        }

        public void chooseNewArmPoistion() {

            if (this.actionMode == 0) {
                targetAngles[0] = -rand.nextInt(50);        //Pivot
                targetAngles[1] = -targetAngles[0];            //Arm
                targetAngles[2] = rand.nextInt(30) - 15;    //Piston
            } else if (this.actionMode == 1) {
                targetAngles[0] = -rand.nextInt(30) + 10;    //Pivot
                targetAngles[1] = -targetAngles[0];            //Arm
                targetAngles[2] = rand.nextInt(10) + 10;    //Piston
            }
        }

        private void updateInterp() {
            for (int i = 0; i < angles.length; i++) {
                prevAngles[i] = angles[i];
            }
        }

        /**
         * @return True when it has finished moving
         */
        private boolean move() {
            boolean didMove = false;

            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i])
                    continue;

                didMove = true;

                double angle = angles[i];
                double target = targetAngles[i];
                double turn = speed[i];
                double delta = Math.abs(angle - target);

                if (delta <= turn) {
                    angles[i] = targetAngles[i];
                    continue;
                }

                if (angle < target) {
                    angles[i] += turn;
                } else {
                    angles[i] -= turn;
                }
            }

            return !didMove;
        }

        public static enum ArmActionState {
            ASSUME_POSITION,
            EXTEND_STRIKER,
            WELD,
            RETRACT_STRIKER
        }
    }
}
