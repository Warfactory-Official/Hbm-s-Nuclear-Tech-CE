package com.hbm.tileentity.machine.oil;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.List;

@AutoRegister
public class TileEntityMachineOilWell extends TileEntityOilDrillBase {


    protected static int maxPower = 100_000;
    protected static int consumption = 100;
    protected static int delay = 50;
    protected static int oilPerDeposit = 500;
    protected static int gasPerDepositMin = 100;
    protected static int gasPerDepositMax = 500;
    protected static double drainChance = 0.05D;
    AxisAlignedBB bb = null;

    @Override
    public String getDefaultName() {
        return "container.oilWell";
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
    public void onDrill(int y) {
        Block b = world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getBlock();
        ItemStack stack = new ItemStack(b);
        if (stack.isEmpty()) return;
        int[] ids = OreDictionary.getOreIDs(stack);
        for (Integer i : ids) {
            String name = OreDictionary.getOreName(i);

            if ("oreUranium".equals(name)) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        if (world.getBlockState(pos.add(j, 10, j)).getBlock().isReplaceable(world, pos.add(j, 7, k))) {
                            world.setBlockState(pos.add(k, 10, k), ModBlocks.gas_radon_dense.getDefaultState());
                        }
                    }
                }
            }

            if ("oreAsbestos".equals(name)) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        if (world.getBlockState(pos.add(j, 10, j)).getBlock().isReplaceable(world, pos.add(j, 7, k))) {
                            world.setBlockState(pos.add(k, 10, k), ModBlocks.gas_asbestos.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSuck(BlockPos pos) {
        world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.BLOCKS, 2.0F, 0.5F);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == ModBlocks.ore_oil) {
            tanks[0].setTankType(Fluids.OIL);
            tanks[1].setTankType(Fluids.GAS);

            this.tanks[0].setFill(this.tanks[0].getFill() + oilPerDeposit);
            if (this.tanks[0].getFill() > this.tanks[0].getMaxFill()) this.tanks[0].setFill(tanks[0].getMaxFill());
            this.tanks[1].setFill(this.tanks[1].getFill() + (gasPerDepositMin + world.rand.nextInt((gasPerDepositMax - gasPerDepositMin + 1))));
            if (this.tanks[1].getFill() > this.tanks[1].getMaxFill()) this.tanks[1].setFill(tanks[1].getMaxFill());

            if (world.rand.nextDouble() < drainChance) {
                world.setBlockState(pos, ModBlocks.ore_oil_empty.getDefaultState(), 3);
            }
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 10, pos.getZ() + 2);
        }

        return bb;
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[]{new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X), new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X), new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z), new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)};
    }

    @Override
    public String getConfigName() {
        return "derrick";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "I:powerCap", maxPower);
        consumption = IConfigurableMachine.grab(obj, "I:consumption", consumption);
        delay = IConfigurableMachine.grab(obj, "I:delay", delay);
        oilPerDeposit = IConfigurableMachine.grab(obj, "I:oilPerDeposit", oilPerDeposit);
        gasPerDepositMin = IConfigurableMachine.grab(obj, "I:gasPerDepositMin", gasPerDepositMin);
        gasPerDepositMax = IConfigurableMachine.grab(obj, "I:gasPerDepositMax", gasPerDepositMax);
        drainChance = IConfigurableMachine.grab(obj, "D:drainChance", drainChance);
    }


    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:powerCap").value(maxPower);
        writer.name("I:consumption").value(consumption);
        writer.name("I:delay").value(delay);
        writer.name("I:oilPerDeposit").value(oilPerDeposit);
        writer.name("I:gasPerDepositMin").value(gasPerDepositMin);
        writer.name("I:gasPerDepositMax").value(gasPerDepositMax);
        writer.name("D:drainChance").value(drainChance);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
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
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_well));
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