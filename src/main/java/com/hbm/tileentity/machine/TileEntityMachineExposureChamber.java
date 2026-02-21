package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineExposureChamber;
import com.hbm.inventory.gui.GUIMachineExposureChamber;
import com.hbm.inventory.recipes.ExposureChamberRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes.ExposureChamberRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineExposureChamber extends TileEntityMachineBase implements IGUIProvider, IEnergyReceiverMK2, IUpgradeInfoProvider,
        ITickable {

    public static final long maxPower = 1_000_000;
    public static final int processTimeBase = 200;
    public static final int consumptionBase = 10_000;
    public static final int maxParticles = 8;
    public long power;
    public int progress;
    public int processTime = processTimeBase;
    public int consumption = consumptionBase;
    public int savedParticles;
    public boolean isOn = false;
    public float rotation;
    public float prevRotation;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    AxisAlignedBB bb = null;

    public TileEntityMachineExposureChamber() {
        /*
         * 0: Particle
         * 1: Particle internal
         * 2: Particle container
         * 3: Ingredient
         * 4: Output
         * 5: Battery
         * 6-7: Upgrades
         */
        super(0, false, true);

        inventory = new ItemStackHandler(8) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot >= 6 && slot <= 7)
                    SoundUtil.playUpgradePlugSound(world, pos);
            }
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getInteger("progress");
        this.power = nbt.getLong("power");
        this.savedParticles = nbt.getInteger("savedParticles");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", progress);
        nbt.setLong("power", power);
        nbt.setInteger("savedParticles", savedParticles);
        return nbt;
    }

    @Override
    public String getDefaultName() {
        return "container.exposureChamber";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.isOn = false;
            this.power = Library.chargeTEFromItems(inventory, 5, power, maxPower);

            if (world.getTotalWorldTime() % 20 == 0) {
                for (DirPos pos : getConPos()) this.trySubscribe(world, pos.getPos(), pos.getDir());
            }

            upgradeManager.checkSlots(6, 7);
            int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);
            int overdriveLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.consumption = consumptionBase;

            this.processTime = processTimeBase - processTimeBase / 4 * speedLevel;
            this.consumption *= (speedLevel / 2 + 1);
            this.processTime *= (powerLevel / 2 + 1);
            this.consumption /= (powerLevel + 1);
            this.processTime /= (overdriveLevel + 1);
            this.consumption *= (overdriveLevel * 2 + 1);

            if (inventory.getStackInSlot(1).isEmpty() && !inventory.getStackInSlot(0).isEmpty() && !inventory.getStackInSlot(3).isEmpty() && this.savedParticles <= 0) {
                ExposureChamberRecipe recipe = this.getRecipe(inventory.getStackInSlot(0), inventory.getStackInSlot(3));

                if (recipe != null) {

                    ItemStack container = inventory.getStackInSlot(0).getItem().getContainerItem(inventory.getStackInSlot(0));

                    boolean canStore = false;

                    if (container.isEmpty()) {
                        canStore = true;
                    } else if (inventory.getStackInSlot(2).isEmpty()) {
                        inventory.setStackInSlot(2, container.copy());
                        canStore = true;
                    } else if (inventory.insertItem(2, container, true).isEmpty()) {
                        inventory.insertItem(2, container, false);
                        canStore = true;
                    }

                    if (canStore) {
                        var stack0cpy = inventory.getStackInSlot(0).copy();
                        stack0cpy.setCount(1);
                        inventory.setStackInSlot(1, stack0cpy);
                        this.inventory.extractItem(0, 1, false);
                        this.savedParticles = maxParticles;
                    }
                }
            }

            if (!inventory.getStackInSlot(1).isEmpty() && this.savedParticles > 0 && this.power >= this.consumption) {
                ExposureChamberRecipe recipe = this.getRecipe(inventory.getStackInSlot(1), inventory.getStackInSlot(3));

                if (recipe != null && inventory.insertItem(4, recipe.output, true).isEmpty()) {
                    this.progress++;
                    this.power -= this.consumption;
                    this.isOn = true;

                    if (this.progress >= this.processTime) {
                        this.progress = 0;
                        this.savedParticles--;
                        this.inventory.extractItem(3, 1, false);
                        this.inventory.insertItem(4, recipe.output.copy(), false);
                    }

                } else {
                    this.progress = 0;
                }
            } else {
                this.progress = 0;
            }

            if (this.savedParticles <= 0) {
                this.inventory.setStackInSlot(1, ItemStack.EMPTY);
            }

            this.networkPackNT(50);
        } else {

            this.prevRotation = this.rotation;

            if (this.isOn) {

                this.rotation += 10D;

                if (this.rotation >= 720D) {
                    this.rotation -= 720D;
                    this.prevRotation -= 720D;
                }
            }
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP).getOpposite();
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        return new DirPos[]{
                new DirPos(xCoord + rot.offsetX * 7 + dir.offsetX * 2, yCoord, zCoord + rot.offsetZ * 7 + dir.offsetZ * 2, dir),
                new DirPos(xCoord + rot.offsetX * 7 - dir.offsetX * 2, yCoord, zCoord + rot.offsetZ * 7 - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(xCoord + rot.offsetX * 8 + dir.offsetX * 2, yCoord, zCoord + rot.offsetZ * 8 + dir.offsetZ * 2, dir),
                new DirPos(xCoord + rot.offsetX * 8 - dir.offsetX * 2, yCoord, zCoord + rot.offsetZ * 8 - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(xCoord + rot.offsetX * 9, yCoord, zCoord + rot.offsetZ * 9, rot)
        };
    }

    public ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack ingredient) {
        return ExposureChamberRecipes.getRecipe(particle, ingredient);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {

        //will only load new capsules if there's no cached particles, this should prevent clogging

        //accept items when the slots are already partially filled, i.e. applicable
        if (i == 1 || i == 3 && !this.inventory.getStackInSlot(i).isEmpty())
            return this.inventory.insertItem(i, stack, true).isEmpty();

        //if there's no particle stored, use the un-consumed capsule for reference
        ItemStack particle = inventory.getStackInSlot(1).isEmpty() ? inventory.getStackInSlot(0) : inventory.getStackInSlot(1);

        //if no particle is loaded and an ingot is present
        if (i == 0 && particle.isEmpty() && !inventory.getStackInSlot(3).isEmpty()) {
            ExposureChamberRecipe recipe = getRecipe(stack, inventory.getStackInSlot(3));
            return recipe != null;
        }

        //if a particle is loaded but no ingot present
        if (i == 3 && !particle.isEmpty() && inventory.getStackInSlot(3).isEmpty()) {
            ExposureChamberRecipe recipe = getRecipe(inventory.getStackInSlot(0), stack);
            return recipe != null;
        }

        //if there's nothing at all, find a reference recipe and see if the item matches anything
        if (particle.isEmpty() && inventory.getStackInSlot(3).isEmpty()) {
            for (ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes) {
                if (i == 0 && recipe.particle.matchesRecipe(stack, true)) return true;
                if (i == 3 && recipe.ingredient.matchesRecipe(stack, true)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 2 || i == 4;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0, 2, 3, 4};
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.isOn);
        buf.writeInt(this.progress);
        buf.writeInt(this.processTime);
        buf.writeInt(this.consumption);
        buf.writeLong(this.power);
        buf.writeByte((byte) this.savedParticles);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.progress = buf.readInt();
        this.processTime = buf.readInt();
        this.consumption = buf.readInt();
        this.power = buf.readLong();
        this.savedParticles = buf.readByte();
    }

    @Override
    public long getPower() {
        return power;
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
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(
                    xCoord - 8,
                    yCoord,
                    zCoord - 8,
                    xCoord + 9,
                    yCoord + 5,
                    zCoord + 9
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineExposureChamber(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineExposureChamber(player.inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_exposure_chamber));
        if (type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 50) + "%"));
        }
        if (type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "+" + (level * 50) + "%"));
        }
        if (type == UpgradeType.OVERDRIVE) {
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
