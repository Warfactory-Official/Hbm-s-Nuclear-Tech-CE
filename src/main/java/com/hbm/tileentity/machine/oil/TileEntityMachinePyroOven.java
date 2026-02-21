package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerPyroOven;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIPyroOven;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes.PyroOvenRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.machine.TileEntityMachinePolluting;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachinePyroOven extends TileEntityMachinePolluting implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, IUpgradeInfoProvider, IFluidCopiable {

    public long power;
    public static final long maxPower = 10_000_000;
    public boolean isVenting;
    public boolean isProgressing;
    public float progress;
    public static int consumption = 10_000;

    public int prevAnim;
    public int anim = 0;

    public FluidTankNTM[] tanks;

    private AudioWrapper audio;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public TileEntityMachinePyroOven() {
        super(6, 50, true, true);
        inventory = this.getNewInventory(6);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.NONE, 24_000);
        tanks[1] = new FluidTankNTM(Fluids.NONE, 24_000);
    }
    // is that the best solution?... ugh
    public ItemStackHandler getNewInventory(int scount) {
        return new ItemStackHandler(scount) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);

                if (!stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade && slot >= 4 && slot <= 5) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            HBMSoundHandler.upgradePlug,
                            net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

        };
    }

    @Override
    public String getDefaultName() {
        return "container.machinePyroOven";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            tanks[0].setType(3, inventory);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
            ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
            if(smoke.getFill() > 0) this.sendFluid(smoke, world, pos.getX() - rot.offsetX, pos.getY() + 3, pos.getZ() - rot.offsetZ, Library.POS_Y);

            ItemStack[] allSlots = new ItemStack[inventory.getSlots()];
            for(int i = 0; i < inventory.getSlots(); i++) {
                allSlots[i] = inventory.getStackInSlot(i);
            }

            upgradeManager.checkSlots(allSlots, 4, 5);
            int speed = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED);
            int powerSaving = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER);
            int overdrive = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);

            this.isProgressing = false;
            this.isVenting = false;

            if(this.canProcess()) {
                PyroOvenRecipe recipe = getMatchingRecipe();
                this.progress += 1F / Math.max((recipe.duration - speed * (recipe.duration / 4)) / (overdrive * 2 + 1), 1);
                this.isProgressing = true;
                this.power -= this.getConsumption(speed + overdrive * 2, powerSaving);

                if(progress >= 1F) {
                    this.progress = 0F;
                    this.finishRecipe(recipe);
                    this.markDirty();
                }

                this.pollute(PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

            } else {
                this.progress = 0F;
            }

            this.networkPackNT(50);
        } else {

            this.prevAnim = this.anim;
            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
            ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

            if(isProgressing) {
                this.anim++;

                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.keepAlive();
                audio.updateVolume(this.getVolume(1F));

                if(MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5) < 50) {
                    if(world.rand.nextInt(20) == 0) world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 - rot.offsetX - dir.offsetX * 0.875, pos.getY() + 3, pos.getZ() + 0.5 - rot.offsetZ - dir.offsetZ * 0.875, 0.0, 0.05, 0.0);
                    if(world.rand.nextInt(20) == 0) world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 - rot.offsetX - dir.offsetX * 2.375, pos.getY() + 3, pos.getZ() + 0.5 - rot.offsetZ - dir.offsetZ * 2.375, 0.0, 0.05, 0.0);
                    if(world.rand.nextInt(20) == 0) world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 - rot.offsetX + dir.offsetX * 0.875, pos.getY() + 3, pos.getZ() + 0.5 - rot.offsetZ + dir.offsetZ * 0.875, 0.0, 0.05, 0.0);
                    if(world.rand.nextInt(20) == 0) world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + 0.5 - rot.offsetX + dir.offsetX * 2.375, pos.getY() + 3, pos.getZ() + 0.5 - rot.offsetZ + dir.offsetZ * 2.375, 0.0, 0.05, 0.0);
                }

            } else {

                if(audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }

            if(this.isVenting) {

                if(world.getTotalWorldTime() % 2 == 0) {
                    NBTTagCompound fx = new NBTTagCompound();
                    fx.setString("type", "tower");
                    fx.setFloat("lift", 10F);
                    fx.setFloat("base", 0.25F);
                    fx.setFloat("max", 2.5F);
                    fx.setInteger("life", 100 + world.rand.nextInt(20));
                    fx.setInteger("color",0x202020);
                    fx.setDouble("posX", pos.getX() + 0.5 - rot.offsetX);
                    fx.setDouble("posY", pos.getY() + 3);
                    fx.setDouble("posZ", pos.getZ() + 0.5 - rot.offsetZ);
                    MainRegistry.proxy.effectNT(fx);
                }
            }
        }
    }

    public static int getConsumption(int speed, int powerSaving) {
        return (int) (consumption * Math.pow(speed + 1, 2)) / (powerSaving + 1);
    }

    protected PyroOvenRecipe lastValidRecipe;

    public PyroOvenRecipe getMatchingRecipe() {

        if(lastValidRecipe != null && doesRecipeMatch(lastValidRecipe)) return lastValidRecipe;

        for(PyroOvenRecipe rec : PyroOvenRecipes.recipes) {
            if(doesRecipeMatch(rec)) {
                lastValidRecipe = rec;
                return rec;
            }
        }

        return null;
    }

    public boolean doesRecipeMatch(PyroOvenRecipe recipe) {

        if(recipe.inputFluid != null) {
            if(tanks[0].getTankType() != recipe.inputFluid.type) return false; // recipe needs fluid, fluid doesn't match
        }
        if(recipe.inputItem != null) {
            if(inventory.getStackInSlot(1).isEmpty()) return false; // recipe needs item, no item present
            return recipe.inputItem.matchesRecipe(inventory.getStackInSlot(1), true); // recipe needs item, item doesn't match
        } else {
            return inventory.getStackInSlot(1).isEmpty(); // recipe does not need item, but item is present
        }
    }

    public boolean canProcess() {
        int speed = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int powerSaving = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER);
        if(power < this.getConsumption(speed, powerSaving)) return false; // not enough power

        PyroOvenRecipe recipe = this.getMatchingRecipe();
        if(recipe == null) return false; // no matching recipe
        if(recipe.inputFluid != null && tanks[0].getFill() < recipe.inputFluid.fill) return false; // not enough input fluid
        if(recipe.inputItem != null && inventory.getStackInSlot(1).getCount() < recipe.inputItem.stacksize) return false; // not enough input item
        if(recipe.outputFluid != null && recipe.outputFluid.fill + tanks[1].getFill() > tanks[1].getMaxFill() && recipe.outputFluid.type == tanks[1].getTankType()) return false; // too much output fluid
        if(recipe.outputItem != null && !inventory.getStackInSlot(2).isEmpty() && recipe.outputItem.getCount() + inventory.getStackInSlot(2).getCount() > inventory.getStackInSlot(2).getMaxStackSize()) return false; // too much output item
        if(recipe.outputItem != null && !inventory.getStackInSlot(2).isEmpty() && recipe.outputItem.getItem() != inventory.getStackInSlot(2).getItem()) return false; // output item doesn't match
        return recipe.outputItem == null || inventory.getStackInSlot(2).isEmpty() || recipe.outputItem.getItemDamage() == inventory.getStackInSlot(2).getItemDamage(); // output meta doesn't match
    }

    public void finishRecipe(PyroOvenRecipe recipe) {
        if(recipe.outputItem != null) {
            if(inventory.getStackInSlot(2).isEmpty()) {
                inventory.setStackInSlot(2, recipe.outputItem.copy());
            } else {
                inventory.getStackInSlot(2).grow(recipe.outputItem.getCount());
            }
        }
        if(recipe.outputFluid != null) {
            tanks[1].setTankType(recipe.outputFluid.type);
            tanks[1].setFill(tanks[1].getFill() + recipe.outputFluid.fill);
        }
        if(recipe.inputItem != null) {
            this.inventory.getStackInSlot(1).shrink(recipe.inputItem.stacksize);
        }
        if(recipe.inputFluid != null) {
            tanks[0].setFill(tanks[0].getFill() - recipe.inputFluid.fill);
        }
    }

    protected DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() + dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() + rot.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - dir.offsetX + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 3, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 3, rot),
        };
    }

    @Override public void serialize(ByteBuf buf) {
        super.serialize(buf);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
        buf.writeLong(power);
        buf.writeBoolean(isVenting);
        buf.writeBoolean(isProgressing);
        buf.writeFloat(progress);
    }

    @Override public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
        power = buf.readLong();
        isVenting = buf.readBoolean();
        isProgressing = buf.readBoolean();
        progress = buf.readFloat();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
        this.progress = nbt.getFloat("prog");
        this.power = nbt.getLong("power");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        nbt.setFloat("prog", progress);
        nbt.setLong("power", power);
        return super.writeToNBT(nbt);
    }

    @Override public int[] getAccessibleSlotsFromSide(EnumFacing e) { return new int[] { 1, 2 }; }
    @Override public boolean isItemValidForSlot(int i, ItemStack itemStack) { return i == 1; }
    @Override public boolean canExtractItem(int i, ItemStack itemStack, int j) { return i == 2; }

    @Override
    public void pollute(PollutionHandler.PollutionType type, float amount) {
        FluidTankNTM tank = type == PollutionHandler.PollutionType.SOOT ? smoke : type == PollutionHandler.PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

        int fluidAmount = (int) Math.ceil(amount * 100);
        tank.setFill(tank.getFill() + fluidAmount);

        if(tank.getFill() > tank.getMaxFill()) {
            int overflow = tank.getFill() - tank.getMaxFill();
            tank.setFill(tank.getMaxFill());
            PollutionHandler.incrementPollution(world, pos, type, overflow / 100F);
            this.isVenting = true;
        }
    }

    @Override public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.pyroOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 15F, 1.0F, 20);
    }

    @Override public void onChunkUnload() {
        super.onChunkUnload();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    @Override public void invalidate() {
        super.invalidate();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) bb = new AxisAlignedBB(pos.getX() - 3, pos.getY(), pos.getZ() - 3, pos.getX() + 4, pos.getY() + 3.5, pos.getZ() + 4);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] { tanks[0], tanks[1], smoke }; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] { tanks[1], smoke }; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] { tanks[0] }; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerPyroOven(player.inventory, this); }
    @Override
    @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIPyroOven(player.inventory, this); }

    @Override
    public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED || type == ItemMachineUpgrade.UpgradeType.POWER;
    }

    @Override
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_pyrooven));
        if(type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (Math.pow(level + 1, 2) * 100 - 100) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
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
}
