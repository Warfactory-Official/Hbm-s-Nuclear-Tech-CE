package com.hbm.tileentity.machine.oil;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
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
public class TileEntityMachinePumpjack extends TileEntityOilDrillBase {


    protected static int maxPower = 250_000;
    protected static int consumption = 200;
    protected static int delay = 25;
    protected static int oilPerDeposit = 750;
    protected static int gasPerDepositMin = 50;
    protected static int gasPerDepositMax = 250;
    protected static double drainChance = 0.025D;

    public float rot = 0;
    public float prevRot = 0;
    public float speed = 0;
    AxisAlignedBB bb = null;

    @Override
    public String getDefaultName() {
        return "container.pumpjack";
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
                for (int j = 2; j < 6; j++) {
                    ForgeDirection dir = ForgeDirection.getOrientation(j);
                    if (world.getBlockState(pos.add(dir.offsetX, 0, dir.offsetZ)).getBlock().isReplaceable(world, pos.add(dir.offsetX, 0, dir.offsetZ))) {
                        world.setBlockState(pos.add(dir.offsetX, 0, dir.offsetZ), ModBlocks.gas_radon_dense.getDefaultState());
                    }
                }
            }

            if ("oreAsbestos".equals(name)) {
                for (int j = 2; j < 6; j++) {
                    ForgeDirection dir = ForgeDirection.getOrientation(j);
                    if (world.getBlockState(pos.add(dir.offsetX, 0, dir.offsetZ)).getBlock().isReplaceable(world, pos.add(dir.offsetX, 0, dir.offsetZ))) {
                        world.setBlockState(pos.add(dir.offsetX, 0, dir.offsetZ), ModBlocks.gas_asbestos.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {

            this.prevRot = rot;

            if (this.indicator == 0) {
                this.rot += speed;
            }

            if (this.rot >= 360) {
                this.prevRot -= 360;
                this.rot -= 360;
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeFloat(this.indicator == 0 ? (5F + (2F * this.speedLevel)) + (this.overLevel - 1F) * 10 : 0F);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.speed = buf.readFloat();
    }

    @Override
    public void onSuck(BlockPos pos) {
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
            bb = new AxisAlignedBB(
                    pos.getX() - 7,
                    pos.getY(),
                    pos.getZ() - 7,
                    pos.getX() + 8,
                    pos.getY() + 6,
                    pos.getZ() + 8
            );
        }

        return bb;
    }

    @Override
    public DirPos[] getConPos() {
        this.getBlockMetadata();
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        return new DirPos[]{
                new DirPos(pos.getX() + rot.offsetX * 2 + dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2 + dir.offsetZ * 2, dir),
                new DirPos(pos.getX() + rot.offsetX * 2 + dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 4 - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() + rot.offsetX * 4 - dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 4 + dir.offsetZ * 2, dir),
                new DirPos(pos.getX() + rot.offsetX * 4 - dir.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2 - dir.offsetZ * 2, dir.getOpposite())
        };
    }

    @Override
    public String getConfigName() {
        return "pumpjack";
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
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_pumpjack));
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