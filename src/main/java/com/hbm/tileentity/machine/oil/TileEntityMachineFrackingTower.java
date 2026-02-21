package com.hbm.tileentity.machine.oil;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.world.feature.OilSpot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@AutoRegister
public class TileEntityMachineFrackingTower extends TileEntityOilDrillBase {

    protected static int maxPower = 5_000_000;
    protected static int consumption = 5000;
    protected static int solutionRequired = 10;
    protected static int delay = 20;
    protected static int oilPerDeposit = 1000;
    protected static int gasPerDepositMin = 100;
    protected static int gasPerDepositMax = 500;
    protected static double drainChance = 0.02D;
    protected static int oilPerBedrockDepsoit = 100;
    protected static int gasPerBedrockDepositMin = 10;
    protected static int gasPerBedrockDepositMax = 50;
    protected static int destructionRange = 75;

    public TileEntityMachineFrackingTower() {
        super();
        tanksOld[2] = new FluidTank(64000);
        tankTypes[2] = Fluids.FRACKSOL.getFF();

        tanks = new FluidTankNTM[3];
        tanks[0] = new FluidTankNTM(Fluids.OIL, 64_000);
        tanks[1] = new FluidTankNTM(Fluids.GAS, 64_000);
        tanks[2] = new FluidTankNTM(Fluids.FRACKSOL, 64_000);
    }

    @Override
    public String getDefaultName() {
        return "container.frackingTower";
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public int getPowerReq() {
        return consumption;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public int getDrillDepth() {
        return 0;
    }

    @Override
    public boolean canPump() {
        boolean b = this.tanks[2].getFill() >= solutionRequired;

        if (!b) {
            this.indicator = 3;
        }

        return b;
    }

    @Override
    public boolean canSuckBlock(Block b) {
        return super.canSuckBlock(b) || b == ModBlocks.ore_bedrock_oil;
    }

    @Override
    public void doSuck(BlockPos pos) {
        super.doSuck(pos);

        if (world.getBlockState(pos).getBlock() == ModBlocks.ore_bedrock_oil) {
            onSuck(pos);
        }
    }

    @Override
    public void onSuck(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        int meta = b.getMetaFromState(state);

        int oil = 0;
        int gas = 0;

        if (b == ModBlocks.ore_oil) {
            tanks[0].setTankType(Fluids.OIL);

            oil = oilPerDeposit;
            gas = gasPerDepositMin + world.rand.nextInt(gasPerDepositMax - gasPerDepositMin + 1);

            if (world.rand.nextDouble() < drainChance) {
                world.setBlockState(pos, ModBlocks.ore_oil_empty.getDefaultState(), 3);
            }
        }

        if (b == ModBlocks.ore_bedrock_oil) {
            oil = oilPerBedrockDepsoit;
            gas = gasPerBedrockDepositMin + world.rand.nextInt(gasPerBedrockDepositMax - gasPerBedrockDepositMin + 1);
        }

        this.tanks[0].setFill(this.tanks[0].getFill() + oil);
        if (this.tanks[0].getFill() > this.tanks[0].getMaxFill()) this.tanks[0].setFill(tanks[0].getMaxFill());
        this.tanks[1].setFill(this.tanks[1].getFill() + gas);
        if (this.tanks[1].getFill() > this.tanks[1].getMaxFill()) this.tanks[1].setFill(tanks[1].getMaxFill());

        this.tanks[2].setFill(tanks[2].getFill() - solutionRequired);

        OilSpot.generateOilSpot(world, this.pos.getX(), this.pos.getZ(), destructionRange, 10, false);
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[0], tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[2]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
        };
    }

    @Override
    protected void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanks[2].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    @Override
    public String getConfigName() {
        return "frackingtower";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "I:powerCap", maxPower);
        consumption = IConfigurableMachine.grab(obj, "I:consumption", consumption);
        solutionRequired = IConfigurableMachine.grab(obj, "I:solutionRequired", solutionRequired);
        delay = IConfigurableMachine.grab(obj, "I:delay", delay);
        oilPerDeposit = IConfigurableMachine.grab(obj, "I:oilPerDeposit", oilPerDeposit);
        gasPerDepositMin = IConfigurableMachine.grab(obj, "I:gasPerDepositMin", gasPerDepositMin);
        gasPerDepositMax = IConfigurableMachine.grab(obj, "I:gasPerDepositMax", gasPerDepositMax);
        drainChance = IConfigurableMachine.grab(obj, "D:drainChance", drainChance);
        oilPerBedrockDepsoit = IConfigurableMachine.grab(obj, "I:oilPerBedrockDeposit", oilPerBedrockDepsoit);
        gasPerBedrockDepositMin = IConfigurableMachine.grab(obj, "I:gasPerBedrockDepositMin", gasPerBedrockDepositMin);
        gasPerBedrockDepositMax = IConfigurableMachine.grab(obj, "I:gasPerBedrockDepositMax", gasPerBedrockDepositMax);
        destructionRange = IConfigurableMachine.grab(obj, "I:destructionRange", destructionRange);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:powerCap").value(maxPower);
        writer.name("I:consumption").value(consumption);
        writer.name("I:solutionRequired").value(solutionRequired);
        writer.name("I:delay").value(delay);
        writer.name("I:oilPerDeposit").value(oilPerDeposit);
        writer.name("I:gasPerDepositMin").value(gasPerDepositMin);
        writer.name("I:gasPerDepositMax").value(gasPerDepositMax);
        writer.name("D:drainChance").value(drainChance);
        writer.name("I:oilPerBedrockDeposit").value(oilPerBedrockDepsoit);
        writer.name("I:gasPerBedrockDepositMin").value(gasPerBedrockDepositMin);
        writer.name("I:gasPerBedrockDepositMax").value(gasPerBedrockDepositMax);
        writer.name("I:destructionRange").value(destructionRange);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineOilWell(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineOilWell(player.inventory, this);
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 1024.0D;
    }

    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_fracking_tower));
        if (type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 25) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "+" + (level * 10) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.AFTERBURN) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_BURN, level * 10, level * 50));
        }
        if (type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
        }
    }
}