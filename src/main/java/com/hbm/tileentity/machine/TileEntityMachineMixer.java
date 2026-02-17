package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMixer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMixer;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineMixer extends TileEntityMachineBase implements IControlReceiver, ITickable, IGUIProvider, IFluidStandardTransceiver, IEnergyReceiverMK2, IUpgradeInfoProvider, IFluidCopiable {

    public static final long maxPower = 10_000;
    private final UpgradeManagerNT upgradeManager;
    public long power;
    public int progress;
    public int processTime;
    public int recipeIndex;
    public float rotation;
    public float prevRotation;
    public boolean wasOn = false;
    public FluidTankNTM[] tanks;
    AxisAlignedBB aabb;
    private int consumption = 50;

    public TileEntityMachineMixer() {
        super(0, true, true);

        inventory = new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot >= 3 && slot <= 4)
                    SoundUtil.playUpgradePlugSound(world, pos);
            }
        };

        this.upgradeManager = new UpgradeManagerNT(this);
        this.tanks = new FluidTankNTM[3];
        this.tanks[0] = new FluidTankNTM(Fluids.NONE, 16_000);
        this.tanks[1] = new FluidTankNTM(Fluids.NONE, 16_000);
        this.tanks[2] = new FluidTankNTM(Fluids.NONE, 24_000);
    }

    @Override
    public String getDefaultName() {
        return "container.machineMixer";
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 0, power, getMaxPower());
            tanks[2].setType(2, inventory);

            upgradeManager.checkSlots(3, 4);
            int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
            int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);
            int overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

            this.consumption = 50;

            this.consumption += speedLevel * 150;
            this.consumption -= this.consumption * powerLevel * 0.25;
            this.consumption *= (overLevel * 3 + 1);

            for (DirPos pos : getConPos()) {
                this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (tanks[0].getTankType() != Fluids.NONE)
                    this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (tanks[1].getTankType() != Fluids.NONE)
                    this.trySubscribe(tanks[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            this.wasOn = this.canProcess();

            if (this.wasOn) {
                this.progress++;
                this.power -= this.getConsumption();

                this.processTime -= this.processTime * speedLevel / 4;
                this.processTime /= (overLevel + 1);

                if (processTime <= 0) this.processTime = 1;

                if (this.progress >= this.processTime) {
                    this.process();
                    this.progress = 0;
                }

            } else {
                this.progress = 0;
            }

            for (DirPos pos : getConPos()) {
                if (tanks[2].getFill() > 0)
                    this.sendFluid(tanks[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            networkPackNT(50);

        } else {

            this.prevRotation = this.rotation;

            if (this.wasOn) {
                this.rotation += 20F;
            }

            if (this.rotation >= 360) {
                this.rotation -= 360;
                this.prevRotation -= 360;
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(processTime);
        buf.writeInt(progress);
        buf.writeInt(recipeIndex);
        buf.writeBoolean(wasOn);

        for (FluidTankNTM fluidTankNTM : tanks)
            fluidTankNTM.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        processTime = buf.readInt();
        progress = buf.readInt();
        recipeIndex = buf.readInt();
        wasOn = buf.readBoolean();

        for (FluidTankNTM fluidTankNTM : tanks)
            fluidTankNTM.deserialize(buf);
    }

    public boolean canProcess() {

        MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());
        if (recipes == null || recipes.length <= 0) {
            this.recipeIndex = 0;
            return false;
        }

        this.recipeIndex = this.recipeIndex % recipes.length;
        MixerRecipe recipe = recipes[this.recipeIndex];
        if (recipe == null) {
            this.recipeIndex = 0;
            return false;
        }

        tanks[0].setTankType(recipe.input1 != null ? recipe.input1.type : Fluids.NONE);
        tanks[1].setTankType(recipe.input2 != null ? recipe.input2.type : Fluids.NONE);

        if (recipe.input1 != null && tanks[0].getFill() < recipe.input1.fill) return false;
        if (recipe.input2 != null && tanks[1].getFill() < recipe.input2.fill) return false;

        /* simplest check would usually go first, but fluid checks also do the setup and we want that to happen even without power */
        if (this.power < getConsumption()) return false;

        if (recipe.output + tanks[2].getFill() > tanks[2].getMaxFill()) return false;

        if (recipe.solidInput != null) {

            if (inventory.getStackInSlot(1).isEmpty()) return false;

            if (!recipe.solidInput.matchesRecipe(inventory.getStackInSlot(1), true) || recipe.solidInput.getStack().getCount() > inventory.getStackInSlot(1).getCount())
                return false;
        }

        this.processTime = recipe.processTime;
        return true;
    }

    protected void process() {

        MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());
        MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];

        if (recipe.input1 != null) tanks[0].setFill(tanks[0].getFill() - recipe.input1.fill);
        if (recipe.input2 != null) tanks[1].setFill(tanks[1].getFill() - recipe.input2.fill);
        if (recipe.solidInput != null)
            inventory.extractItem(1, recipe.solidInput.getStack().getCount(), false);
        tanks[2].setFill(tanks[2].getFill() + recipe.output);
    }

    public int getConsumption() {
        return consumption;
    }

    protected DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.add(0, -1, 0), Library.NEG_Y),
                new DirPos(pos.add(1, 0, 0), Library.POS_X),
                new DirPos(pos.add(-1, 0, 0), Library.NEG_X),
                new DirPos(pos.add(0, 0, 1), Library.POS_Z),
                new DirPos(pos.add(0, 0, -1), Library.NEG_Z),
        };
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{1};
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());
        if (recipes == null || recipes.length <= 0) return false;

        MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];
        if (recipe == null || recipe.solidInput == null) return false;

        return recipe.solidInput.matchesRecipe(itemStack, true);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");
        this.progress = nbt.getInteger("progress");
        this.processTime = nbt.getInteger("processTime");
        this.recipeIndex = nbt.getInteger("recipe");
        for (int i = 0; i < 3; i++) this.tanks[i].readFromNBT(nbt, i + "");
        if (nbt.hasKey("f")) {
            nbt.removeTag("f");
            nbt.removeTag("tanks");
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setInteger("progress", progress);
        nbt.setInteger("processTime", processTime);
        nbt.setInteger("recipe", recipeIndex);
        for (int i = 0; i < 3; i++) this.tanks[i].writeToNBT(nbt, i + "");
        return super.writeToNBT(nbt);
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMixer(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMixer(player.inventory, this);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (aabb != null)
            return aabb;

        aabb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
        return aabb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[2]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0], tanks[1]};
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 16;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("toggle")) this.recipeIndex++;
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_mixer));
        if(type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 300) + "%"));
        }
        if(type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (level * 25) + "%"));
        }
        if(type == UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 6);
        return upgrades;
    }
    @Override
    public FluidTankNTM getTankToPaste() {
        return this.tanks[2];
    }
}
