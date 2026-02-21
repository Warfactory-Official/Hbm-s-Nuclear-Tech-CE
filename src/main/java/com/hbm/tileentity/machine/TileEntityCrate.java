package com.hbm.tileentity.machine;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.config.MachineConfig;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

// mlbv: I tried overriding markDirty to calculate the changes but somehow it always delays by one operation.
// also, implementing ITickable is a bad idea, remove it if you can find a better way.
public abstract class TileEntityCrate extends TileEntityCrateBase implements IGUIProvider, ITickable, IPersistentNBT {


    private final AtomicBoolean isCheckScheduled = new AtomicBoolean(false);
    public float fillPercentage = 0.0F;
    protected String name;
    boolean needsUpdate = false;
    private boolean needsSync = false;
    private boolean destroyedByCreativePlayer = false;

    private record CrateDropData(NBTTagCompound persistentData, double radiation) {
    }

    public TileEntityCrate(int scount, String name) {
        super(scount);
        this.name = name;
    }

    @Override
    protected ItemStackHandler getNewInventory(int scount, int slotlimit){
        return new ItemStackHandler(scount){
            @Override
            public ItemStack getStackInSlot(int slot) {
                ensureFilled();
                return super.getStackInSlot(slot);
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                ensureFilled();
                super.setStackInSlot(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
                needsUpdate = true;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotlimit;
            }
        };
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (needsUpdate && world.getTotalWorldTime() % 5 == 4) {
            scheduleCheck();
            needsUpdate = false;
        }
        if (needsSync) {
            networkPackNT(10);
            needsSync = false;
        }
    }

    void scheduleCheck() {
        if (this.isCheckScheduled.compareAndSet(false, true)) {
            CompletableFuture.supplyAsync(this::getSize).whenComplete((currentSize, error) -> {
                try {
                    if (error != null) {
                        MainRegistry.logger.error("Error checking crate size at {}", pos, error);
                        return;
                    }
                    if (currentSize > MachineConfig.crateByteSize * 2L) {
                        ((WorldServer) world).addScheduledTask(this::ejectAndClearInventory);
                    } else {
                        this.fillPercentage = (float) currentSize / MachineConfig.crateByteSize * 100F;
                    }
                } finally {
                    this.isCheckScheduled.set(false);
                    needsSync = true;
                }
            });
        }
    }

    private void ejectAndClearInventory() {
        InventoryHelper.dropInventoryItems(world, pos, this);
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        this.fillPercentage = 0.0F;
        super.markDirty();
        MainRegistry.logger.debug("Crate at {} was oversized and has been emptied to prevent data corruption.", pos);
    }

    public long getSize() {
        return Library.getCompressedNbtSize(assembleDropTag(buildDropData()));
    }

    private CrateDropData buildDropData() {
        NBTTagCompound persistentData = new NBTTagCompound();
        double radiation = 0D;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            radiation += HazardSystem.getTotalRadsFromStack(stack) * stack.getCount();
            NBTTagCompound slot = new NBTTagCompound();
            stack.writeToNBT(slot);
            persistentData.setTag("slot" + i, slot);
        }
        if (this.isLocked()) {
            persistentData.setInteger("lock", this.getPins());
            persistentData.setDouble("lockMod", this.getMod());
        }
        return new CrateDropData(persistentData, radiation);
    }

    private static NBTTagCompound assembleDropTag(CrateDropData data) {
        NBTTagCompound root = new NBTTagCompound();
        if (!data.persistentData.isEmpty()) {
            root.setTag(NBT_PERSISTENT_KEY, data.persistentData.copy());
        }
        if (data.radiation > 0D) {
            root.setDouble(BlockStorageCrate.CRATE_RAD_KEY, data.radiation);
        }
        return root;
    }

    @Override
    public boolean canAccess(EntityPlayer player) {

        if (!this.isLocked() || player == null) {
            return true;
        } else {
            ItemStack stack = player.getHeldItemMainhand();

            if (stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            if (stack.getItem() == ModItems.key_red) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            return this.tryPick(player);
        }
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : name;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        fillPercentage = compound.getFloat("fill");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("fill", fillPercentage);
        return compound;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        CrateDropData data = buildDropData();
        NBTTagCompound dropTag = assembleDropTag(data);
        if (world != null && !world.isRemote && Library.getCompressedNbtSize(dropTag) > MachineConfig.crateByteSize) {
            InventoryHelper.dropInventoryItems(world, pos, this);
            return;
        }
        if (!data.persistentData.isEmpty()) {
            nbt.setTag(NBT_PERSISTENT_KEY, data.persistentData);
        }
        if (data.radiation > 0D) {
            nbt.setDouble(BlockStorageCrate.CRATE_RAD_KEY, data.radiation);
        }
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.hasKey(NBT_PERSISTENT_KEY) ? nbt.getCompoundTag(NBT_PERSISTENT_KEY) : nbt;
        for (int i = 0; i < inventory.getSlots(); i++) {
            String key = "slot" + i;
            if (data.hasKey(key)) {
                inventory.setStackInSlot(i, new ItemStack(data.getCompoundTag(key)));
            } else {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        if (data.hasKey("lock")) {
            this.setPins(data.getInteger("lock"));
            this.setMod(data.getDouble("lockMod"));
            this.lock();
        }
    }

    @Override
    public void setDestroyedByCreativePlayer() {
        destroyedByCreativePlayer = true;
    }

    @Override
    public boolean isDestroyedByCreativePlayer() {
        return destroyedByCreativePlayer;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeFloat(this.fillPercentage);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.fillPercentage = buf.readFloat();
    }

    @Override
    protected boolean checkLock(EnumFacing facing){
        return facing == null || !isLocked();
    }
}
