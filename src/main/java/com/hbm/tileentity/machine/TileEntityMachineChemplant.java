package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineChemplant;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineChemplant;
import com.hbm.inventory.recipes.ChemplantRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineChemplant extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IGUIProvider, ITickable, IUpgradeInfoProvider {

    public static final long maxPower = 100000;
    public long power;
    public int progress;
    public int maxProgress = 100;
    public boolean isProgressing;
    public FluidTankNTM[] tanksNew;
    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    //upgraded stats
    int consumption = 100;
    int speed = 100;
    // last successful load
    int lsl0 = 0;
    int lsl1 = 0;
    int lsu0 = 0;
    int lsu1 = 0;
    private AudioWrapper audio;

    public TileEntityMachineChemplant() {
        super(21, true, true);
        /*
         * 0 Battery
         * 1-3 Upgrades
         * 4 Schematic
         * 5-8 Output
         * 9-10 FOut In
         * 11-12 FOut Out
         * 13-16 Input
         * 17-18 FIn In
         * 19-20 FIn Out
         */
        tanksNew = new FluidTankNTM[4];
        for (int i = 0; i < 4; i++) {
            tanksNew[i] = new FluidTankNTM(Fluids.NONE, 24_000);
        }
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSqToCenter(pos) <= 128;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("powerTime");
        isProgressing = nbt.getBoolean("progressing");
        for (int i = 0; i < tanksNew.length; i++) {
            tanksNew[i].readFromNBT(nbt, "t" + i);
        }
        if (nbt.hasKey("input1")) {
            nbt.removeTag("input1");
            nbt.removeTag("input2");
            nbt.removeTag("output1");
            nbt.removeTag("output2");
            nbt.removeTag("tankType0");
            nbt.removeTag("tankType1");
            nbt.removeTag("tankType2");
            nbt.removeTag("tankType3");
        }
        if (nbt.hasKey("inventory"))
            inventory.deserializeNBT((NBTTagCompound) nbt.getTag("inventory"));
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("powerTime", power);
        nbt.setBoolean("progressing", isProgressing);
        for (int i = 0; i < tanksNew.length; i++) {
            tanksNew[i].writeToNBT(nbt, "t" + i);
        }
        NBTTagCompound inv = inventory.serializeNBT();
        nbt.setTag("inventory", inv);
        return nbt;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(progress);
        buf.writeInt(maxProgress);
        buf.writeBoolean(isProgressing);

        for (FluidTankNTM fluidTankNTM : tanksNew) fluidTankNTM.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        progress = buf.readInt();
        maxProgress = buf.readInt();
        isProgressing = buf.readBoolean();

        for (FluidTankNTM fluidTankNTM : tanksNew) fluidTankNTM.deserialize(buf);
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public int getProgressScaled(int i) {
        return (progress * i) / Math.max(10, maxProgress);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.chemplantOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F, 1.0F);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {

        super.invalidate();

        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {

            this.speed = 100;
            this.consumption = 100;

            this.isProgressing = false;
            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            int fluidDelay = 40;

            if (lsu0 >= fluidDelay && tanksNew[0].loadTank(17, 19, inventory)) lsl0 = 0;
            if (lsu1 >= fluidDelay && tanksNew[1].loadTank(18, 20, inventory)) lsl1 = 0;

            if (lsl0 >= fluidDelay && !inventory.getStackInSlot(17).isEmpty() && !FluidTankNTM.noDualUnload.contains(inventory.getStackInSlot(17).getItem()))
                if (tanksNew[0].unloadTank(17, 19, inventory)) lsu0 = 0;
            if (lsl1 >= fluidDelay && !inventory.getStackInSlot(18).isEmpty() && !FluidTankNTM.noDualUnload.contains(inventory.getStackInSlot(18).getItem()))
                if (tanksNew[1].unloadTank(18, 20, inventory)) lsu1 = 0;

            tanksNew[2].unloadTank(9, 11, inventory);
            tanksNew[3].unloadTank(10, 12, inventory);

            if (lsl0 < fluidDelay) lsl0++;
            if (lsl1 < fluidDelay) lsl1++;
            if (lsu0 < fluidDelay) lsu0++;
            if (lsu1 < fluidDelay) lsu1++;

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
            ejectOutput();
            TileEntity input = world.getTileEntity(new BlockPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2));
            if (input != null && input.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY())) {
                IItemHandler cap = input.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.toEnumFacing().rotateY());

                ItemStack template = inventory.getStackInSlot(4);
                if (!template.isEmpty() && template.getItem() == ModItems.chemistry_template) {
                    ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(template.getItemDamage());
                    if (recipe != null && recipe.inputs != null) {
                        List<AStack> ingredients = new ArrayList<>();
                        for (AStack a : recipe.inputs) {
                            if (a != null) ingredients.add(a);
                        }

                        if (!ingredients.isEmpty()) {
                            int[] slots;
                            TileEntityMachineBase sourceTE = (input instanceof TileEntityMachineBase) ? (TileEntityMachineBase) input : null;
                            if (sourceTE != null) {
                                slots = sourceTE.getAccessibleSlotsFromSide(dir.toEnumFacing().rotateY());
                            } else {
                                slots = new int[cap.getSlots()];
                                for (int i = 0; i < slots.length; i++) slots[i] = i;
                            }

                            Library.pullItemsForRecipe(cap, slots, this.inventory, ingredients, sourceTE, 13, 16, this::markDirty);
                        }
                    }
                }
            }

            if (isProgressing && this.world.getTotalWorldTime() % 3 == 0) {
                ForgeDirection rotP = dir.getRotation(ForgeDirection.UP);
                double x = pos.getX() + 0.5 + dir.offsetX * 1.125 + rotP.offsetX * 0.125;
                double y = pos.getY() + 3;
                double z = pos.getZ() + 0.5 + dir.offsetZ * 1.125 + rotP.offsetZ * 0.125;
                world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0.0, 0.1, 0.0);
            }

            if (world.getTotalWorldTime() % 20 == 0) {
                this.updateConnections();
            }

            for (DirPos pos : getConPos()) {
                if (tanksNew[2].getFill() > 0)
                    this.sendFluid(tanksNew[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (tanksNew[3].getFill() > 0)
                    this.sendFluid(tanksNew[3], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            upgradeManager.checkSlots(inventory, 1, 3);

            int speedLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            int powerLevel = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);
            int overLevel = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);

            this.speed -= speedLevel * 25;
            this.consumption += speedLevel * 300;
            this.speed += powerLevel * 5;
            this.consumption -= powerLevel * 20;
            this.speed /= (overLevel + 1);
            this.consumption *= (overLevel + 1);

            if (this.speed <= 0) {
                this.speed = 1;
            }

            if (!canProcess()) {
                this.progress = 0;
            } else {
                isProgressing = true;
                process();
            }

            this.networkPackNT(150);
        } else {

            float volume = this.getVolume(1F);

            if (isProgressing && volume > 0) {

                if (audio == null) {
                    audio = createAudioLoop();
                    audio.updateVolume(volume);
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                    audio.updateVolume(volume);
                }

            } else {

                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }


    private void updateConnections() {

        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanksNew[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    public DirPos[] getConPos() {

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

        return new DirPos[]{
                new DirPos(pos.getX() + rot.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - rot.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() + rot.offsetX * 3 + dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 3 + dir.offsetZ, rot),
                new DirPos(pos.getX() - rot.offsetX * 2 + dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ * 2 + dir.offsetZ, rot.getOpposite())
        };
    }

    private boolean canProcess() {

        if (inventory.getStackInSlot(4).isEmpty() || inventory.getStackInSlot(4).getItem() != ModItems.chemistry_template)
            return false;

        ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(4).getItemDamage());

        if (recipe == null)
            return false;

        setupTanks(recipe);

        if (this.power < this.consumption) return false;
        if (!hasRequiredFluids(recipe)) return false;
        if (!hasSpaceForFluids(recipe)) return false;
        if (!hasRequiredItems(recipe)) return false;
        if (!hasSpaceForItems(recipe)) return false;

        return true;
    }

    private void setupTanks(ChemplantRecipes.ChemRecipe recipe) {
        if (recipe.inputFluids[0] != null)
            tanksNew[0].withPressure(recipe.inputFluids[0].pressure).setTankType(recipe.inputFluids[0].type);
        else tanksNew[0].setTankType(Fluids.NONE);
        if (recipe.inputFluids[1] != null)
            tanksNew[1].withPressure(recipe.inputFluids[1].pressure).setTankType(recipe.inputFluids[1].type);
        else tanksNew[1].setTankType(Fluids.NONE);
        if (recipe.outputFluids[0] != null)
            tanksNew[2].withPressure(recipe.outputFluids[0].pressure).setTankType(recipe.outputFluids[0].type);
        else tanksNew[2].setTankType(Fluids.NONE);
        if (recipe.outputFluids[1] != null)
            tanksNew[3].withPressure(recipe.outputFluids[1].pressure).setTankType(recipe.outputFluids[1].type);
        else tanksNew[3].setTankType(Fluids.NONE);
    }

    private boolean hasRequiredFluids(ChemplantRecipes.ChemRecipe recipe) {
        if (recipe.inputFluids[0] != null && tanksNew[0].getFill() < recipe.inputFluids[0].fill) return false;
        if (recipe.inputFluids[1] != null && tanksNew[1].getFill() < recipe.inputFluids[1].fill) return false;
        return true;
    }

    private boolean hasSpaceForFluids(ChemplantRecipes.ChemRecipe recipe) {
        if (recipe.outputFluids[0] != null && tanksNew[2].getFill() + recipe.outputFluids[0].fill > tanksNew[2].getMaxFill())
            return false;
        if (recipe.outputFluids[1] != null && tanksNew[3].getFill() + recipe.outputFluids[1].fill > tanksNew[3].getMaxFill())
            return false;
        return true;
    }

    private boolean hasRequiredItems(ChemplantRecipes.ChemRecipe recipe) {
        return InventoryUtil.doesArrayHaveIngredients(inventory, 13, 16, recipe.inputs);
    }

    private boolean hasSpaceForItems(ChemplantRecipes.ChemRecipe recipe) {
        return InventoryUtil.doesArrayHaveSpace(inventory, 5, 8, recipe.outputs);
    }

    private void process() {

        this.power -= this.consumption;
        this.progress++;

        if (!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.meteorite_sword_machined)
            inventory.setStackInSlot(0, new ItemStack(ModItems.meteorite_sword_treated)); //fisfndmoivndlmgindgifgjfdnblfm

        ChemplantRecipes.ChemRecipe recipe = ChemplantRecipes.indexMapping.get(inventory.getStackInSlot(4).getItemDamage());

        this.maxProgress = recipe.getDuration() * this.speed / 100;

        if (maxProgress <= 0) maxProgress = 1;

        if (this.progress >= this.maxProgress) {
            consumeFluids(recipe);
            produceFluids(recipe);
            consumeItems(recipe);
            produceItems(recipe);
            this.progress = 0;
            this.markDirty();
        }
    }

    private void consumeFluids(ChemplantRecipes.ChemRecipe recipe) {
        if (recipe.inputFluids[0] != null) tanksNew[0].setFill(tanksNew[0].getFill() - recipe.inputFluids[0].fill);
        if (recipe.inputFluids[1] != null) tanksNew[1].setFill(tanksNew[1].getFill() - recipe.inputFluids[1].fill);
    }

    private void produceFluids(ChemplantRecipes.ChemRecipe recipe) {
        if (recipe.outputFluids[0] != null) tanksNew[2].setFill(tanksNew[2].getFill() + recipe.outputFluids[0].fill);
        if (recipe.outputFluids[1] != null) tanksNew[3].setFill(tanksNew[3].getFill() + recipe.outputFluids[1].fill);
    }

    private void consumeItems(ChemplantRecipes.ChemRecipe recipe) {

        for (AStack in : recipe.inputs) {
            if (in != null)
                InventoryUtil.tryConsumeAStack(inventory, 13, 16, in);
        }
    }

    private void produceItems(ChemplantRecipes.ChemRecipe recipe) {

        for (ItemStack out : recipe.outputs) {
            if (out != null)
                InventoryUtil.tryAddItemToInventory(inventory, 5, 8, out.copy());
        }
    }

    private void ejectOutput() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
        BlockPos outputPos = new BlockPos(pos.getX() + dir.offsetX * 3 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 3 + rot.offsetZ);
        ForgeDirection accessSide = dir.getOpposite();
        List<ItemStack> itemsToEject = new ArrayList<>();
        for (int i = 5; i <= 8; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemsToEject.add(stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        if (itemsToEject.isEmpty()) return;
        List<ItemStack> leftovers = Library.popProducts(world, outputPos, accessSide, itemsToEject);
        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers) {
                InventoryUtil.tryAddItemToInventory(inventory, 5, 8, leftover);
            }
        }
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
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
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);

        return new SPacketUpdateTileEntity(pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

        readFromNBT(pkt.getNbtCompound());
    }

    public ItemStack getStackInSlot(int i) {
        return inventory.getStackInSlot(i);
    }

    @Override
    public String getDefaultName() {
        return "container.chemplant";
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanksNew[2], tanksNew[3]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanksNew[0], tanksNew[1]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanksNew;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineChemplant(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineChemplant(player.inventory, this);
    }

    @Override
    public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED || type == ItemMachineUpgrade.UpgradeType.POWER || type == ItemMachineUpgrade.UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_chemplant));
        if (type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 300) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 30) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 5) + "%"));
        }
        if (type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
        }
    }

    @Override
    public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
        HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.POWER, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.OVERDRIVE, 9);
        return upgrades;
    }
}
