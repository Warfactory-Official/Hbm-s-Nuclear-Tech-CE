package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerCentrifuge;
import com.hbm.inventory.gui.GUIMachineCentrifuge;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineCentrifuge extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, IUpgradeInfoProvider
		, IConfigurableMachine {

    /*
     * So why do we do this now? You have a funny mekanism/thermal/whatever pipe and you want to output stuff from a side
     * that isn't the bottom, what do? Answer: make all slots accessible from all sides and regulate in/output in the
     * dedicated methods. Duh.
     */
    private static final int[] slot_io = new int[]{0, 2, 3, 4, 5};
    //configurable values
    public static int maxPower = 100000;
    public static int processingSpeed = 200;
    public static int baseConsumption = 200;
    public final UpgradeManagerNT upgradeManager;
    public int progress;
    public long power;
    public boolean isProgressing;
    AxisAlignedBB bb = null;
    private int audioDuration = 0;
    private AudioWrapper audio;

    public TileEntityMachineCentrifuge() {
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

        upgradeManager = new UpgradeManagerNT(this);
    }

    @Override
    public String getConfigName() {
        return "centrifuge";
    }

    /* reads the JSON object and sets the machine's parameters, use defaults and ignore if a value is not yet present */
    @Override
    public void readIfPresent(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "I:powerCap", maxPower);
        processingSpeed = IConfigurableMachine.grab(obj, "I:timeToProcess", processingSpeed);
        baseConsumption = IConfigurableMachine.grab(obj, "I:consumption", baseConsumption);
    }

    /* writes the entire config for this machine using the relevant values */
    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:powerCap").value(maxPower);
        writer.name("I:timeToProcess").value(processingSpeed);
        writer.name("I:consumption").value(baseConsumption);
    }

    public String getDefaultName() {
        return "container.centrifuge";
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 0;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slot_io;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        power = nbt.getLong("power");
        progress = nbt.getShort("progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("power", power);
        nbt.setShort("progress", (short) progress);
        return nbt;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i > 1;
    }

    public int getCentrifugeProgressScaled(int i) {
        return (progress * i) / processingSpeed;
    }

    public long getPowerRemainingScaled(int i) {
        return (power * i) / maxPower;
    }

    public boolean canProcess() {

        if (inventory.getStackInSlot(0).isEmpty()) {
            return false;
        }
        ItemStack[] out = CentrifugeRecipes.getOutput(inventory.getStackInSlot(0));

        if (out == null) return false;

        for (int i = 0; i < Math.min(4, out.length); i++) {
            if (out[i] == null || inventory.insertItem(i + 2, out[i], true).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private void processItem() {
        ItemStack[] out = CentrifugeRecipes.getOutput(inventory.getStackInSlot(0));

        for (int i = 0; i < Math.min(4, out.length); i++) {
            if (out[i] != null) inventory.insertItem(i + 2, out[i], false);
        }

        inventory.extractItem(0, 1, false);
        this.markDirty();
    }

    public boolean hasPower() {
        return power > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

            power = Library.chargeTEFromItems(inventory, 1, power, maxPower);

            int consumption = baseConsumption;
            int speed = 1;

            upgradeManager.checkSlots(6, 7);
            speed += upgradeManager.getLevel(UpgradeType.SPEED);
            consumption += upgradeManager.getLevel(UpgradeType.SPEED) * baseConsumption;

            speed *= (1 + upgradeManager.getLevel(UpgradeType.OVERDRIVE) * 5);
            consumption += upgradeManager.getLevel(UpgradeType.OVERDRIVE) * baseConsumption * 50;

            consumption /= (1 + upgradeManager.getLevel(UpgradeType.POWER));

            if (hasPower() && isProcessing()) {
                this.power -= consumption;

                if (this.power < 0) {
                    this.power = 0;
                }
            }

            isProgressing = hasPower() && canProcess();

            if (isProgressing) {
                progress += speed;

                if (this.progress >= TileEntityMachineCentrifuge.processingSpeed) {
                    this.progress = 0;
                    this.processItem();
                }
            } else {
                progress = 0;
            }

            this.networkPackNT(50);
        } else {

            if (isProgressing) {
                audioDuration += 2;
            } else {
                audioDuration -= 3;
            }

            audioDuration = MathHelper.clamp(audioDuration, 0, 60);

            if (audioDuration > 10 && MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 25) {
                if (audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.updateVolume(getVolume(1F));
                audio.updatePitch((audioDuration - 10) / 100F + 0.5F);
                audio.keepAlive();

            } else {

                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeInt(progress);
        buf.writeBoolean(isProgressing);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();
        progress = buf.readInt();
        isProgressing = buf.readBoolean();
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.centrifugeOperate, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 0.5F,
                pos.getZ() + 0.5F, 1.0F, 10F, 1.0F, 20);
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
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos, pos.add(1, 4, 1));
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCentrifuge(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineCentrifuge(player.inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_centrifuge));
        if (type == UpgradeType.SPEED) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
            info.add(TextFormatting.RED + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if (type == UpgradeType.POWER) {
            info.add(TextFormatting.GREEN + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
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
