package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineSolderingStation;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineSolderingStation;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.SolderingRecipes.SolderingRecipe;
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

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineSolderingStation extends TileEntityMachineBase
        implements IEnergyReceiverMK2,
        IFluidStandardReceiver,
        IControlReceiver,
        IGUIProvider,
        IUpgradeInfoProvider,
        IFluidCopiable,
        ITickable {
    private static final int invSize = 11;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public long maxPower = 2_000;
    public long consumption;
    public boolean collisionPrevention = false;
    public int progress;
    public int processTime = 1;
    public FluidTankNTM tank;
    public ItemStack display = ItemStack.EMPTY;
    AxisAlignedBB bb = null;
    private SolderingRecipe recipe;

    public TileEntityMachineSolderingStation() {
        super(invSize, true, true);
        inventory =
                new ItemStackHandler(invSize) {
                    @Override
                    protected void onContentsChanged(int slot) {
                        super.onContentsChanged(slot);
                        markDirty();
                    }

                    @Override
                    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                        super.setStackInSlot(slot, stack);

                        if (!stack.isEmpty()
                                && slot >= 9
                                && slot <= 10
                                && stack.getItem() instanceof ItemMachineUpgrade) {
                            final BlockPos pos = getPos();

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
        this.tank = new FluidTankNTM(Fluids.NONE, 8_000);
    }

    @Override
    public @NotNull String getDefaultName() {
        return "container.machineSolderingStation";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 7, this.getPower(), this.getMaxPower());
            this.tank.setType(8, inventory);

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

            recipe =
                    SolderingRecipes.getRecipe(
                            new ItemStack[]{
                                    inventory.getStackInSlot(0),
                                    inventory.getStackInSlot(1),
                                    inventory.getStackInSlot(2),
                                    inventory.getStackInSlot(3),
                                    inventory.getStackInSlot(4),
                                    inventory.getStackInSlot(5)
                            });
            long intendedMaxPower;

            upgradeManager.checkSlots(inventory, 9, 10);
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

                        if (inventory.getStackInSlot(6).isEmpty()) {
                            inventory.setStackInSlot(6, recipe.output.copy());
                        } else {
                            inventory.getStackInSlot(6).grow(recipe.output.getCount());
                        }

                        this.markDirty();
                    }

                    if (world.getTotalWorldTime() % 20 == 0) {
                        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
                        BlockPos pos = getPos();
                        NBTTagCompound dPart = new NBTTagCompound();
                        dPart.setString("type", "tau");
                        dPart.setByte("count", (byte) 3);
                        PacketThreading.createAllAroundThreadedPacket(
                                new AuxParticlePacketNT(
                                        dPart,
                                        pos.getX() + 0.5 - dir.offsetX * 0.5 + rot.offsetX * 0.5,
                                        pos.getY() + 1.125,
                                        pos.getZ() + 0.5 - dir.offsetZ * 0.5 + rot.offsetZ * 0.5),
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

    public boolean canProcess(SolderingRecipe recipe) {

        if (this.power < this.consumption) return false;

        if (recipe.fluid != null) {
            if (this.tank.getTankType() != recipe.fluid.type) return false;
            if (this.tank.getFill() < recipe.fluid.fill) return false;
        }

        if (collisionPrevention && recipe.fluid == null && this.tank.getFill() > 0) return false;

        if (!inventory.getStackInSlot(6).isEmpty()) {
            if (inventory.getStackInSlot(6).getItem() != recipe.output.getItem()) return false;
            if (inventory.getStackInSlot(6).getItemDamage() != recipe.output.getItemDamage())
                return false;

            if (inventory.getStackInSlot(6).getCount() + recipe.output.getCount()
                    > inventory.getStackInSlot(6).getMaxStackSize()) return false;
        }

        return true;
    }

    public void consumeItems(SolderingRecipe recipe) {

        for (AStack aStack : recipe.toppings) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
                    inventory.getStackInSlot(i).shrink(aStack.stacksize);
                    break;
                }
            }
        }

        for (AStack aStack : recipe.pcb) {
            for (int i = 3; i < 5; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
                    inventory.getStackInSlot(i).shrink(aStack.stacksize);
                    break;
                }
            }
        }

        for (AStack aStack : recipe.solder) {
            for (int i = 5; i < 6; i++) {
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

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot < 3) {
            for (int i = 0; i < 3; i++)
                if (i != slot
                        && !inventory.getStackInSlot(i).isEmpty()
                        && inventory.getStackInSlot(i).isItemEqual(stack)) return false;
            for (AStack t : SolderingRecipes.toppings) if (t.matchesRecipe(stack, true)) return true;
        } else if (slot < 5) {
            for (int i = 3; i < 5; i++)
                if (i != slot
                        && !inventory.getStackInSlot(i).isEmpty()
                        && inventory.getStackInSlot(i).isItemEqual(stack)) return false;
            for (AStack t : SolderingRecipes.pcb) if (t.matchesRecipe(stack, true)) return true;
        } else if (slot < 6) {
            for (int i = 5; i < 6; i++)
                if (i != slot
                        && !inventory.getStackInSlot(i).isEmpty()
                        && inventory.getStackInSlot(i).isItemEqual(stack)) return false;
            for (AStack t : SolderingRecipes.solder) if (t.matchesRecipe(stack, true)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 6;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0, 1, 2, 3, 4, 5, 6};
    }

    protected DirPos[] getConPos() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        BlockPos pos = getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new DirPos[]{
                new DirPos(x + dir.offsetX, y, z + dir.offsetZ, dir),
                new DirPos(x + dir.offsetX + rot.offsetX, y, z + dir.offsetZ + rot.offsetZ, dir),
                new DirPos(x - dir.offsetX * 2, y, z - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(
                        x - dir.offsetX * 2 + rot.offsetX,
                        y,
                        z - dir.offsetZ * 2 + rot.offsetZ,
                        dir.getOpposite()),
                new DirPos(x - rot.offsetX, y, z - rot.offsetZ, rot.getOpposite()),
                new DirPos(
                        x - dir.offsetX - rot.offsetX, y, z - dir.offsetZ - rot.offsetZ, rot.getOpposite()),
                new DirPos(x + rot.offsetX * 2, y, z + rot.offsetZ * 2, rot),
                new DirPos(x - dir.offsetX + rot.offsetX * 2, y, z - dir.offsetZ + rot.offsetZ * 2, rot),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeLong(this.consumption);
        buf.writeInt(this.progress);
        buf.writeInt(this.processTime);
        buf.writeBoolean(this.collisionPrevention);
        buf.writeBoolean(recipe != null);
        if (recipe != null) {
            buf.writeInt(Item.getIdFromItem(recipe.output.getItem()));
            buf.writeInt(recipe.output.getItemDamage());
        }
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.consumption = buf.readLong();
        this.progress = buf.readInt();
        this.processTime = buf.readInt();
        this.collisionPrevention = buf.readBoolean();

        if (buf.readBoolean()) {
            int id = buf.readInt();
            this.display = new ItemStack(Item.getItemById(id), 1, buf.readInt());
        } else {
            this.display = ItemStack.EMPTY;
        }

        this.tank.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.progress = nbt.getInteger("progress");
        this.processTime = nbt.getInteger("processTime");
        this.collisionPrevention = nbt.getBoolean("collisionPrevention");
        tank.readFromNBT(nbt, "t");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        nbt.setInteger("progress", progress);
        nbt.setInteger("processTime", processTime);
        nbt.setBoolean("collisionPrevention", collisionPrevention);
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineSolderingStation(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineSolderingStation(player.inventory, this);
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
    public boolean canProvideInfo(
            ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED
                || type == ItemMachineUpgrade.UpgradeType.POWER
                || type == ItemMachineUpgrade.UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(
            ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_soldering_station));
        if (type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(
                    ChatFormatting.GREEN
                            + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 100 / 6) + "%"));
            info.add(
                    ChatFormatting.RED
                            + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(
                    ChatFormatting.GREEN
                            + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 100 / 6) + "%"));
            info.add(
                    ChatFormatting.RED + I18nUtil.resolveKey(this.KEY_DELAY, "+" + (level * 100 / 3) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
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

    @Override
    public FluidTankNTM getTankToPaste() {
        return tank;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        this.collisionPrevention = !this.collisionPrevention;
        this.markDirty();
    }
}
