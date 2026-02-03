package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineArcFurnaceLarge;
import com.hbm.inventory.gui.GUIMachineArcFurnaceLarge;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.MutableVec3d;
import com.mojang.realmsclient.gui.ChatFormatting;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityMachineArcFurnaceLarge extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IControlReceiver, IGUIProvider, IUpgradeInfoProvider {

    public long power;
    public static final long maxPower = 2_500_000;
    public boolean liquidMode = false;
    public float progress;
    public boolean isProgressing;
    public boolean hasMaterial;
    public int delay;
    public int upgrade;

    public float lid;
    public float prevLid;
    public int approachNum;
    public float syncLid;

    private AudioWrapper audioLid;
    private AudioWrapper audioProgress;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public byte[] electrodes = new byte[3];
    public static final byte ELECTRODE_NONE = 0;
    public static final byte ELECTRODE_FRESH = 1;
    public static final byte ELECTRODE_USED = 2;
    public static final byte ELECTRODE_DEPLETED = 3;

    public int getMaxInputSize() {
        return upgrade == 0 ? 1 : upgrade == 1 ? 4 : upgrade == 2 ? 8 : 16;
    }

    public static final int maxLiquid = MaterialShapes.BLOCK.q(128);
    public volatile List<Mats.MaterialStack> liquids = new ArrayList<>();

    public TileEntityMachineArcFurnaceLarge() {
        super(25);
        inventory = getNewInventory(25);
    }

    @Override
    public String getDefaultName() {
        return "container.machineArcFurnaceLarge";
    }
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

                if (!stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade && slot == 4) {
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
    public void update() {
        ItemStack[] allSlots = new ItemStack[inventory.getSlots()];
        for(int i = 0; i < inventory.getSlots(); i++) {
            allSlots[i] = inventory.getStackInSlot(i);
        }
        upgradeManager.checkSlots(allSlots, 4, 4);
        this.upgrade = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED);

        if(!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 3, power, maxPower);
            this.isProgressing = false;

            for(DirPos pos : getConPos()) this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());

            if(power > 0) {

                boolean ingredients = this.hasIngredients();
                boolean electrodes = this.hasElectrodes();

                int consumption = (int) (1_000 * Math.pow(5, upgrade));

                if(ingredients && electrodes && delay <= 0 && this.liquids.isEmpty()) {
                    if(lid > 0) {
                        lid -= 1F / (60F / (upgrade * 0.5 + 1));
                        if(lid < 0) lid = 0;
                        this.progress = 0;
                    } else {

                        if(power >= consumption) {
                            int duration = 400 / (upgrade * 2 + 1);
                            this.progress += 1F / duration;
                            this.isProgressing = true;
                            this.power -= consumption;
                            if(this.progress >= 1F) {
                                this.process();
                                this.progress = 0;
                                this.markDirty();
                                this.delay = (int) (120 / (upgrade * 0.5 + 1));
                                PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, 10F);
                            }
                        }
                    }
                } else {
                    if(this.delay > 0) delay--;
                    this.progress = 0;
                    if(lid < 1 && this.electrodes[0] != 0 && this.electrodes[1] != 0 && this.electrodes[2] != 0) {
                        lid += 1F / (60F / (upgrade * 0.5 + 1));
                        if(lid > 1) lid = 1;
                    }
                }

                hasMaterial = ingredients;
            }

            this.decideElectrodeState();

            if(!hasMaterial) hasMaterial = this.hasIngredients();

            if(!this.liquids.isEmpty() && this.lid > 0F) {

                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

                MutableVec3d impact = new MutableVec3d();
                Mats.MaterialStack didPour = CrucibleUtil.pourFullStack(world, pos.getX() + 0.5D + dir.offsetX * 2.875D, pos.getY() + 1.25D, pos.getZ() + 0.5D + dir.offsetZ * 2.875D, 6, true, this.liquids, MaterialShapes.INGOT.q(1), impact);

                if(didPour != null) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "foundry");
                    data.setInteger("color", didPour.material.moltenColor);
                    data.setByte("dir", (byte) dir.ordinal());
                    data.setFloat("off", 0.625F);
                    data.setFloat("base", 0.625F);
                    data.setFloat("len", Math.max(1F, pos.getY() + 1 - (float) (Math.ceil(impact.y) - 0.875)));
                    ThreadedPacket message = new AuxParticlePacketNT(data, pos.getX() + 0.5D + dir.offsetX * 2.875D, pos.getY() + 1, pos.getZ() + 0.5D + dir.offsetZ * 2.875D);
                    PacketThreading.createAllAroundThreadedPacket(message,
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));
                }
            }

            this.liquids.removeIf(o -> o.amount <= 0);

            this.networkPackNT(150);
        } else {

            this.prevLid = this.lid;

            if(this.approachNum > 0) {
                this.lid = this.lid + ((this.syncLid - this.lid) / (float) this.approachNum);
                --this.approachNum;
            } else {
                this.lid = this.syncLid;
            }

            if(this.lid != this.prevLid) {
                if(this.audioLid == null || !this.audioLid.isPlaying()) {
                    this.audioLid = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.wgh_start, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), this.getVolume(0.75F), 15F);
                    this.audioLid.startSound();
                }
                this.audioLid.keepAlive();
            } else {
                if(this.audioLid != null) {
                    this.audioLid.stopSound();
                    this.audioLid = null;
                }
            }

            if((lid == 1 || lid == 0) && lid != prevLid && !(this.prevLid == 0)) {
                MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.wgh_stop, SoundCategory.BLOCKS, this.getVolume(1F), 1F);
            }

            if(this.isProgressing) {
                if(this.audioProgress == null || !this.audioProgress.isPlaying()) {
                    this.audioProgress = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.electricHum, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), this.getVolume(1.5F), 15F);
                    this.audioProgress.startSound();
                }
                this.audioProgress.updatePitch(0.75F);
                this.audioProgress.keepAlive();
            } else {
                if(this.audioProgress != null) {
                    this.audioProgress.stopSound();
                    this.audioProgress = null;
                }
            }

            if(this.lid != this.prevLid && this.lid > this.prevLid && !(this.prevLid == 0 && this.lid == 1) && MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 4, pos.getZ() + 0.5) < 50) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "tower");
                data.setFloat("lift", 0.01F);
                data.setFloat("base", 0.5F);
                data.setFloat("max", 2F);
                data.setInteger("life", 70 + world.rand.nextInt(30));
                data.setDouble("posX", pos.getX() + 0.5 + world.rand.nextGaussian() * 0.5);
                data.setDouble("posZ", pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.5);
                data.setDouble("posY", pos.getY() + 4);
                data.setBoolean("noWind", true);
                data.setFloat("alphaMod", prevLid / lid);
                data.setInteger("color", 0x000000);
                data.setFloat("strafe", 0.05F);
                for(int i = 0; i < 3; i++) MainRegistry.proxy.effectNT(data);
            }

            if(this.lid != this.prevLid && this.lid < this.prevLid && this.lid > 0.5F && this.hasMaterial && MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 4, pos.getZ() + 0.5) < 50) {

                if(world.rand.nextInt(5) == 0) {
                    NBTTagCompound flame = new NBTTagCompound();
                    flame.setString("type", "rbmkflame");
                    flame.setDouble("posX", pos.getX() + 0.5 + world.rand.nextGaussian() * 0.5);
                    flame.setDouble("posZ", pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.5);
                    flame.setDouble("posY", pos.getY() + 2.75);
                    flame.setInteger("maxAge", 50);
                    for(int i = 0; i < 2; i++) MainRegistry.proxy.effectNT(flame);
                }
            }
        }
    }

    public void decideElectrodeState() {
        for(int i = 0; i < 3; i++) {

            if(!inventory.getStackInSlot(i).isEmpty()) {
                if(inventory.getStackInSlot(i).getItem() == ModItems.arc_electrode_burnt) { this.electrodes[i] = this.ELECTRODE_DEPLETED; continue; }
                if(inventory.getStackInSlot(i).getItem() == ModItems.arc_electrode) {
                    if(this.isProgressing || ItemArcElectrode.getDurability(inventory.getStackInSlot(i)) > 0) this.electrodes[i] = this.ELECTRODE_USED;
                    else this.electrodes[i] = this.ELECTRODE_FRESH;
                    continue;
                }
            }
            this.electrodes[i] = this.ELECTRODE_NONE;
        }
    }

    public void process() {

        for(int i = 5; i < 25; i++) {
            if(inventory.getStackInSlot(i).isEmpty()) continue;
            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(inventory.getStackInSlot(i), this.liquidMode);
            if(recipe == null) continue;

            if(!liquidMode && recipe.solidOutput != null) {
                int amount = inventory.getStackInSlot(i).getCount();
                inventory.setStackInSlot(i, recipe.solidOutput.copy());
                inventory.getStackInSlot(i).setCount(inventory.getStackInSlot(i).getCount() * amount);
            }

            if(liquidMode && recipe.fluidOutput != null) {

                while(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getCount() > 0) {
                    int liquid = this.getStackAmount(liquids);
                    int toAdd = this.getStackAmount(recipe.fluidOutput);

                    if(liquid + toAdd <= this.maxLiquid) {
                        this.inventory.getStackInSlot(i).shrink(1);
                        for(Mats.MaterialStack stack : recipe.fluidOutput) {
                            this.addToStack(stack);
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        for(int i = 0; i < 3; i++) {
            if(ItemArcElectrode.damage(inventory.getStackInSlot(i))) {
                inventory.setStackInSlot(i, new ItemStack(ModItems.arc_electrode_burnt, 1, inventory.getStackInSlot(i).getItemDamage()));
            }
        }
    }

    public boolean hasIngredients() {

        for(int i = 5; i < 25; i++) {
            if(inventory.getStackInSlot(i).isEmpty()) continue;
            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(inventory.getStackInSlot(i), this.liquidMode);
            if(recipe == null) continue;
            if(liquidMode && recipe.fluidOutput != null) return true;
            if(!liquidMode && recipe.solidOutput != null) return true;
        }

        return false;
    }

    public boolean hasElectrodes() {
        for(int i = 0; i < 3; i++) {
            if(inventory.getStackInSlot(i).isEmpty() || inventory.getStackInSlot(i).getItem() != ModItems.arc_electrode) return false;
        }
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] { 0, 1, 2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    }
    // Th3_Sl1ze: original 1.7 behaviour implied that the lid should be actually open
    // but bruh I can insert items by hand even with closed lid, why can't hoppers do the same thing?
    @Override
    public boolean canInsertItem(int slot, ItemStack stack) {
        if(slot < 3) return stack.getItem() == ModItems.arc_electrode;
        if(slot > 4) {
            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, this.liquidMode);
            if(recipe == null) return false;
            if(liquidMode) {
                if(recipe.fluidOutput == null) return false;
                int sta = !inventory.getStackInSlot(slot).isEmpty() ? inventory.getStackInSlot(slot).getCount() : 0;
                sta += stack.getCount();
                return sta <= getMaxInputSize();
            } else {
                if(recipe.solidOutput == null) return false;
                int sta = !inventory.getStackInSlot(slot).isEmpty() ? inventory.getStackInSlot(slot).getCount() : 0;
                sta += stack.getCount();
                return sta * recipe.solidOutput.getCount() <= recipe.solidOutput.getMaxStackSize() && sta <= getMaxInputSize();
            }
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot < 3) return stack.getItem() == ModItems.arc_electrode;
        if(slot > 4) {
            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(stack, this.liquidMode);
            if(recipe == null) return false;
            if(liquidMode) {
                return recipe.fluidOutput != null;
            } else {
                return recipe.solidOutput != null;
            }
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int amount) {
        if(slot < 3) return lid >= 1 && stack.getItem() != ModItems.arc_electrode;
        if(slot > 4) return lid > 0 && ArcFurnaceRecipes.getOutput(stack, this.liquidMode) == null;
        return false;
    }

    public void addToStack(Mats.MaterialStack matStack) {

        for(Mats.MaterialStack mat : liquids) {
            if(mat.material == matStack.material) {
                mat.amount += matStack.amount;
                return;
            }
        }

        liquids.add(matStack.copy());
    }

    public static int getStackAmount(List<Mats.MaterialStack> stack) {
        int amount = 0;
        for(Mats.MaterialStack mat : stack) amount += mat.amount;
        return amount;
    }

    public static int getStackAmount(Mats.MaterialStack[] stack) {
        int amount = 0;
        for(Mats.MaterialStack mat : stack) amount += mat.amount;
        return amount;
    }

    protected DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 3 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 3 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 3 - rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 3 - rot.offsetZ, dir),
                new DirPos(pos.getX() + rot.offsetX * 3 + dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 3 + dir.offsetZ, rot),
                new DirPos(pos.getX() + rot.offsetX * 3 - dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 3 - dir.offsetZ, rot),
                new DirPos(pos.getX() - rot.offsetX * 3 + dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ * 3 + dir.offsetZ, rot.getOpposite()),
                new DirPos(pos.getX() - rot.offsetX * 3 - dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ * 3 - dir.offsetZ, rot.getOpposite())
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeFloat(progress);
        buf.writeFloat(lid);
        buf.writeBoolean(isProgressing);
        buf.writeBoolean(liquidMode);
        buf.writeBoolean(hasMaterial);

        for(int i = 0; i < 3; i++) buf.writeByte(electrodes[i]);

        buf.writeShort(liquids.size());

        for(Mats.MaterialStack mat : liquids) {
            buf.writeInt(mat.material.id);
            buf.writeInt(mat.amount);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readFloat();
        this.syncLid = buf.readFloat();
        this.isProgressing = buf.readBoolean();
        this.liquidMode = buf.readBoolean();
        this.hasMaterial = buf.readBoolean();

        for(int i = 0; i < 3; i++) electrodes[i] = buf.readByte();

        int mats = buf.readShort();

        List<Mats.MaterialStack> newLiquids = new ArrayList<>();
        for(int i = 0; i < mats; i++) {
            newLiquids.add(new Mats.MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()));
        }
        this.liquids = newLiquids;

        if(syncLid != 0 && syncLid != 1) this.approachNum = 2;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.liquidMode = nbt.getBoolean("liquidMode");
        this.progress = nbt.getFloat("progress");
        this.lid = nbt.getFloat("lid");
        this.delay = nbt.getInteger("delay");

        int count = nbt.getShort("count");
        liquids.clear();

        for(int i = 0; i < count; i++) {
            liquids.add(new Mats.MaterialStack(Mats.matById.get(nbt.getInteger("m" + i)), nbt.getInteger("a" + i)));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setBoolean("liquidMode", liquidMode);
        nbt.setFloat("progress", progress);
        nbt.setFloat("lid", lid);
        nbt.setInteger("delay", delay);

        int count = liquids.size();
        nbt.setShort("count", (short) count);
        for(int i = 0; i < count; i++) {
            Mats.MaterialStack mat = liquids.get(i);
            nbt.setInteger("m" + i, mat.material.id);
            nbt.setInteger("a" + i, mat.amount);
        }
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

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 3,
                    pos.getY(),
                    pos.getZ() - 3,
                    pos.getX() + 4,
                    pos.getY() + 6,
                    pos.getZ() + 4
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
        return new ContainerMachineArcFurnaceLarge(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineArcFurnaceLarge(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.getBoolean("liquid")) {
            this.liquidMode = !this.liquidMode;
            this.markDirty();
        }
    }

    @Override
    public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED;
    }

    @Override
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_arc_furnace));
        if(type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (100 - 100 / (level * 2 + 1)) + "%"));
            info.add(ChatFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + ((int) Math.pow(5, level) * 100 - 100) + "%"));
        }
    }

    @Override
    public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
        HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 3);
        return upgrades;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

}
