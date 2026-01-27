package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.FFUtils;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.*;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

public abstract class TileEntityOilDrillBase extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IConfigurableMachine, IPersistentNBT, IGUIProvider, IFluidCopiable, IFFtoNTMF {
    private static boolean converted = false;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public int indicator = 0;
    public FluidTank[] tanksOld;
    public Fluid[] tankTypes;
    public FluidTankNTM[] tanks;
    public int speedLevel;
    public int energyLevel;
    public int overLevel;
    HashSet<BlockPos> processed = new HashSet<>();

    public TileEntityOilDrillBase() {
        super(8, true, true);
        tanksOld = new FluidTank[3];
        tankTypes = new Fluid[3];

        tanksOld[0] = new FluidTank(128000);
        tankTypes[0] = Fluids.OIL.getFF();
        ;
        tanksOld[1] = new FluidTank(128000);
        tankTypes[1] = Fluids.GAS.getFF();
        ;

        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.OIL, 64_000);
        tanks[1] = new FluidTankNTM(Fluids.GAS, 64_000);


        converted = true;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.power = compound.getLong("powerTime");
        if (!converted) {
            tankTypes[0] = Fluids.OIL.getFF();
            tankTypes[1] = Fluids.GAS.getFF();
            if (compound.hasKey("tanks"))
                FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanksOld);
        } else {
            for (int i = 0; i < this.tanks.length; i++)
                this.tanks[i].readFromNBT(compound, "t" + i);
            if (compound.hasKey("tanks"))
                compound.removeTag("tanks");
            if (compound.hasKey("age"))
                compound.removeTag("age");
        }
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("powerTime", power);
        if (!converted) {
            compound.setTag("tanks", FFUtils.serializeTankArray(tanksOld));
        } else {
            for (int i = 0; i < this.tanks.length; i++)
                this.tanks[i].writeToNBT(compound, "t" + i);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {

        boolean empty = power == 0;
        for (FluidTankNTM tank : tanks) if (tank.getFill() > 0) empty = false;

        if (!empty) {
            nbt.setLong("power", power);
            for (int i = 0; i < this.tanks.length; i++) {
                this.tanks[i].writeToNBT(nbt, "t" + i);
            }
        }
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.power = nbt.getLong("power");
        for (int i = 0; i < this.tanks.length; i++)
            this.tanks[i].readFromNBT(nbt, "t" + i);
    }

    @Override
    public void update() {
        if (!converted) {
            convertAndSetFluids(tankTypes, tanksOld, tanks);
            converted = true;
        }
        if (!world.isRemote) {

            this.updateConnections();

            this.tanks[0].unloadTank(1, 2, inventory);
            this.tanks[1].unloadTank(3, 4, inventory);

            upgradeManager.checkSlots(inventory, 5, 7);
            this.speedLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            this.energyLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);
            this.overLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE), 3) + 1;
            int abLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.AFTERBURN), 3);

            int toBurn = Math.min(tanks[1].getFill(), abLevel * 10);

            if (toBurn > 0) {
                tanks[1].setFill(tanks[1].getFill() - toBurn);
                this.power += toBurn * 5;

                if (this.power > this.getMaxPower())
                    this.power = this.getMaxPower();
            }

            power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());

            for (DirPos pos : getConPos()) {
                if (tanks[0].getFill() > 0)
                    this.sendFluid(tanks[0], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (tanks[1].getFill() > 0)
                    this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            if (this.power >= this.getPowerReqEff() && this.tanks[0].getFill() < this.tanks[0].getMaxFill() && this.tanks[1].getFill() < this.tanks[1].getMaxFill()) {

                this.power -= this.getPowerReqEff();

                if (world.getTotalWorldTime() % getDelayEff() == 0) {
                    this.indicator = 0;

                    for (int y = pos.getY() - 1; y >= getDrillDepth(); y--) {

                        if (world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getBlock() != ModBlocks.oil_pipe) {

                            if (trySuck(y)) {
                                break;
                            } else {
                                tryDrill(y);
                                break;
                            }
                        }

                        if (y == getDrillDepth())
                            this.indicator = 1;
                    }
                }

            } else {
                this.indicator = 2;
            }

            this.sendUpdate();
        }
    }

    public void sendUpdate() {
        networkPackNT(25);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(indicator);

        for (FluidTankNTM tank : tanks)
            tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.indicator = buf.readInt();

        for (FluidTankNTM tank : tanks)
            tank.deserialize(buf);
    }

    public boolean canPump() {
        return true;
    }

    public int getPowerReqEff() {
        int req = this.getPowerReq();
        return (req + (req / 4 * this.speedLevel) - (req / 4 * this.energyLevel)) * this.overLevel;
    }

    public int getDelayEff() {
        int delay = getDelay();
        return Math.max((delay - (delay / 4 * this.speedLevel) + (delay / 10 * this.energyLevel)) / this.overLevel, 1);
    }

    public abstract int getPowerReq();

    public abstract int getDelay();

    public void tryDrill(int y) {
        BlockPos posD = new BlockPos(pos.getX(), y, pos.getZ());
        Block b = world.getBlockState(posD).getBlock();

        if (b.getExplosionResistance(null) < 1000) {
            onDrill(y);
            world.setBlockState(posD, ModBlocks.oil_pipe.getDefaultState());
        } else {
            this.indicator = 2;
        }
    }

    public void onDrill(int y) {
    }

    public int getDrillDepth() {
        return 5;
    }

    public boolean trySuck(int y) {
        BlockPos startPos = new BlockPos(pos.getX(), y, pos.getZ());
        Block startBlock = world.getBlockState(startPos).getBlock();
        if (!canSuckBlock(startBlock)) return false;
        if (!this.canPump()) return true;
        Queue<BlockPos> queue = new ArrayDeque<>();
        processed.clear();
        queue.offer(startPos);
        processed.add(startPos);

        int nodesVisited = 0;
        while (!queue.isEmpty() && nodesVisited < 256) {
            BlockPos currentPos = queue.poll();
            nodesVisited++;
            Block currentBlock = world.getBlockState(currentPos).getBlock();
            if (currentBlock == ModBlocks.ore_oil || currentBlock == ModBlocks.ore_bedrock_oil) {
                doSuck(currentPos);
                return true;
            }
            if (currentBlock != ModBlocks.ore_oil_empty) continue;
            for (ForgeDirection dir : BobMathUtil.getShuffledDirs()) {
                BlockPos neighborPos = currentPos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
                if (!processed.contains(neighborPos) && canSuckBlock(world.getBlockState(neighborPos).getBlock())) {
                    processed.add(neighborPos);
                    queue.offer(neighborPos);
                }
            }
        }
        return false;
    }

    public boolean canSuckBlock(Block b) {
        return b == ModBlocks.ore_oil || b == ModBlocks.ore_oil_empty;
    }

    public void doSuck(BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();

        if (b == ModBlocks.ore_oil) {
            onSuck(pos);
        }
    }

    public abstract void onSuck(BlockPos pos);

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;

    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    public abstract DirPos[] getConPos();

    protected void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return null;
    }
}