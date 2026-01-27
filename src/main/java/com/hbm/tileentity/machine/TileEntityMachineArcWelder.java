package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineArcWelder;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineArcWelder;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes.ArcWelderRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineArcWelder extends TileEntityMachineBase
        implements IEnergyReceiverMK2,
        IFluidStandardReceiver,
        IGUIProvider,
        IUpgradeInfoProvider,
        IFluidCopiable,
        ITickable {
    private static final int invSize = 8;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public long maxPower = 2_000;
    public long consumption;
    public int progress;
    public int processTime = 1;
    public FluidTankNTM tank;
    public ItemStack display = ItemStack.EMPTY;
    AxisAlignedBB bb = null;

    public TileEntityMachineArcWelder() {
        super(invSize, true, true);

        this.tank = new FluidTankNTM(Fluids.NONE, 24_000);

        inventory =
                new ItemStackHandler(invSize) {
                    @Override
                    protected void onContentsChanged(int slot) {
                        super.onContentsChanged(slot);
                        markDirty();
                    }

                    @Override
                    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                        super.setStackInSlot(slot, stack);

                        if (!stack.isEmpty()
                                && slot >= 6
                                && slot <= 7
                                && stack.getItem() instanceof ItemMachineUpgrade) {
                            world.playSound(
                                    null,
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5,
                                    HBMSoundHandler.upgradePlug,
                                    SoundCategory.BLOCKS,
                                    1.0F,
                                    1.0F);
                        }
                    }
                };

    }

    @Override
    public @NotNull String getDefaultName() {
        return "container.machineArcWelder";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 4, this.getPower(), this.getMaxPower());
            this.tank.setType(5, inventory);

            if (world.getTotalWorldTime() % 20 == 0) {
                for (DirPos dirPos : getConPos()) {
                    this.trySubscribe(
                            world,
                            dirPos.getPos().getX(),
                            dirPos.getPos().getY(),
                            dirPos.getPos().getZ(),
                            dirPos.getDir());
                    if (tank.getTankType() != Fluids.NONE)
                        this.trySubscribe(
                                tank.getTankType(),
                                world,
                                dirPos.getPos().getX(),
                                dirPos.getPos().getY(),
                                dirPos.getPos().getZ(),
                                dirPos.getDir());
                }
            }

            ArcWelderRecipe recipe =
                    ArcWelderRecipes.getRecipe(
                            inventory.getStackInSlot(0),
                            inventory.getStackInSlot(1),
                            inventory.getStackInSlot(2));
            long intendedMaxPower;

            upgradeManager.checkSlots(inventory, 6, 7);
            int redLevel = upgradeManager.getLevel(UpgradeType.SPEED);
            int blueLevel = upgradeManager.getLevel(UpgradeType.POWER);
            int blackLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            if (recipe != null) {
                this.processTime =
                        recipe.duration - (recipe.duration * redLevel / 6) + (recipe.duration * blueLevel / 3);
                this.consumption =
                        recipe.consumption
                                + (recipe.consumption * redLevel)
                                - (recipe.consumption * blueLevel / 6);
                this.consumption *= (long) Math.pow(2, blackLevel);
                intendedMaxPower = consumption * 20;

                if (canProcess(recipe)) {
                    this.progress += (1 + blackLevel);
                    this.power -= this.consumption;

                    if (progress >= processTime) {
                        this.progress = 0;
                        this.consumeItems(recipe);

                        if (inventory.getStackInSlot(3).isEmpty()) {
                            inventory.setStackInSlot(3, recipe.output.copy());
                        } else {
                            inventory.getStackInSlot(3).grow(recipe.output.getCount());
                        }

                        this.markDirty();
                    }

                    if (world.getTotalWorldTime() % 2 == 0) {
                        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                        NBTTagCompound dPart = new NBTTagCompound();
                        dPart.setString("type", world.getTotalWorldTime() % 20 == 0 ? "tau" : "hadron");
                        dPart.setByte("count", (byte) 5);
                        PacketThreading.createAllAroundThreadedPacket(
                                new AuxParticlePacketNT(
                                        dPart,
                                        pos.getX() + 0.5 - dir.offsetX * 0.5,
                                        pos.getY() + 1.25,
                                        pos.getZ() + 0.5 - dir.offsetZ * 0.5),
                                new TargetPoint(
                                        world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 25));
                    }

                } else {
                    this.progress = 0;
                }

            } else {
                this.progress = 0;
                this.consumption = 100;
                intendedMaxPower = 2000;
            }

            this.maxPower = Math.max(intendedMaxPower, power);

            this.networkPackNT(25);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeLong(consumption);
        buf.writeInt(progress);
        buf.writeInt(processTime);

        tank.serialize(buf);

        ArcWelderRecipe recipe =
                ArcWelderRecipes.getRecipe(
                        inventory.getStackInSlot(0), inventory.getStackInSlot(1), inventory.getStackInSlot(2));

        if (recipe != null) {
            buf.writeBoolean(true);
            buf.writeInt(Item.getIdFromItem(recipe.output.getItem()));
            buf.writeInt(recipe.output.getItemDamage());
        } else buf.writeBoolean(false);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        maxPower = buf.readLong();
        consumption = buf.readLong();
        progress = buf.readInt();
        processTime = buf.readInt();

        tank.deserialize(buf);

        if (buf.readBoolean()) {
            this.display = new ItemStack(Item.getItemById(buf.readInt()), 1, buf.readInt());
        } else this.display = ItemStack.EMPTY;
    }

    public boolean canProcess(ArcWelderRecipe recipe) {

        if (this.power < this.consumption) return false;

        if (recipe.fluid != null) {
            if (this.tank.getTankType() != recipe.fluid.type) return false;
            if (this.tank.getFill() < recipe.fluid.fill) return false;
        }

        ItemStack stack = inventory.getStackInSlot(3);

        if (!stack.isEmpty()) {
            if (stack.getItem() != recipe.output.getItem()) return false;
            if (stack.getItemDamage() != recipe.output.getItemDamage()) return false;
            if (stack.getCount() + recipe.output.getCount() > stack.getMaxStackSize()) return false;
        }

        return true;
    }

    public void consumeItems(ArcWelderRecipe recipe) {

        for (AStack aStack : recipe.ingredients) {

            for (int i = 0; i < 3; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
                    inventory.getStackInSlot(i).shrink(aStack.stacksize);
                    break;
                }
            }
        }

        if (recipe.fluid != null) {
            this.tank.setFill(tank.getFill() - recipe.fluid.fill);
        }
    }

    protected DirPos[] getConPos() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return new DirPos[]{
                new DirPos(x + dir.offsetX, y, z + dir.offsetZ, dir),
                new DirPos(x + dir.offsetX + rot.offsetX, y, z + dir.offsetZ + rot.offsetZ, dir),
                new DirPos(x + dir.offsetX - rot.offsetX, y, z + dir.offsetZ - rot.offsetZ, dir),
                new DirPos(x - dir.offsetX * 2, y, z - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(
                        x - dir.offsetX * 2 + rot.offsetX,
                        y,
                        z - dir.offsetZ * 2 + rot.offsetZ,
                        dir.getOpposite()),
                new DirPos(
                        x - dir.offsetX * 2 - rot.offsetX,
                        y,
                        z - dir.offsetZ * 2 - rot.offsetZ,
                        dir.getOpposite()),
                new DirPos(x + rot.offsetX * 2, y, z + rot.offsetZ * 2, rot),
                new DirPos(x - dir.offsetX + rot.offsetX * 2, y, z - dir.offsetZ + rot.offsetZ * 2, rot),
                new DirPos(x - rot.offsetX * 2, y, z - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(
                        x - dir.offsetX - rot.offsetX * 2,
                        y,
                        z - dir.offsetZ - rot.offsetZ * 2,
                        rot.getOpposite())
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.progress = nbt.getInteger("progress");
        this.processTime = nbt.getInteger("processTime");
        tank.readFromNBT(nbt, "t");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        nbt.setInteger("progress", progress);
        nbt.setInteger("processTime", processTime);
        tank.writeToNBT(nbt, "t");
        return nbt;
    }

    @Override
    public long getPower() {
        return Math.max(Math.min(power, maxPower), 0);
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public boolean isItemValidForSlot(int slot, @NotNull ItemStack stack) {
        return slot < 3;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int amount) {
        return slot == 3;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack) {
        return slot < 3;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos accessorPos) {
        EnumFacing dir = EnumFacing.byIndex(this.getBlockMetadata() - 10);
        EnumFacing rot = dir.rotateY();
        BlockPos core = this.pos;

        // Red
        BlockPos red1 = core.offset(rot);
        BlockPos red2 = core.offset(rot.getOpposite()).offset(dir.getOpposite());
        if (accessorPos.equals(red1) || accessorPos.equals(red2)) {
            return new int[]{0, 3};
        }

        // Yellow
        BlockPos yellow = core.offset(dir.getOpposite());
        if (accessorPos.equals(yellow)) {
            return new int[]{1, 3};
        }

        // Green
        BlockPos green1 = core.offset(rot.getOpposite());
        BlockPos green2 = core.offset(rot).offset(dir.getOpposite());
        if (accessorPos.equals(green1) || accessorPos.equals(green2)) {
            return new int[]{2, 3};
        }

        return new int[]{};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineArcWelder(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineArcWelder(player.inventory, this);
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb =
                    new AxisAlignedBB(
                            pos.getX() - 1,
                            pos.getY(),
                            pos.getZ() - 1,
                            pos.getX() + 2,
                            pos.getY() + 3,
                            pos.getZ() + 2);
        }

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
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_arc_welder));
        if (type == UpgradeType.SPEED) {
            info.add(
                    ChatFormatting.GREEN
                            + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 100 / 6) + "%"));
            info.add(
                    ChatFormatting.RED
                            + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if (type == UpgradeType.POWER) {
            info.add(
                    ChatFormatting.GREEN
                            + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 100 / 6) + "%"));
            info.add(
                    ChatFormatting.RED + I18nUtil.resolveKey(this.KEY_DELAY, "+" + (level * 100 / 3) + "%"));
        }
        if (type == UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
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

    @Override
    public FluidTankNTM getTankToPaste() {
        return tank;
    }
}
