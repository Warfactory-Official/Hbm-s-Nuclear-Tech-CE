package com.hbm.entity.item;

import com.google.common.collect.ImmutableSet;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.items.ItemStackHandler;

import java.util.LinkedHashSet;
import java.util.Set;

@AutoRegister(name = "entity_delivery_drone", sendVelocityUpdates = false)
public class EntityDeliveryDrone extends EntityDroneBase implements IInventory, IChunkLoader {
    protected ItemStackHandler inventory = new ItemStackHandler(this.getSizeInventory());
    public FluidStack fluid;

    protected boolean chunkLoading = false;
    private Ticket loaderTicket;

    public EntityDeliveryDrone(World world) {
        super(world);
    }

    @Override
    public boolean hitByEntity(Entity attacker) {

        if(this.isDead) return false;

        if(attacker instanceof EntityPlayer && !world.isRemote) {
            this.setDead();
            for (int i = 0; i < inventory.getSlots(); i++) {
                if(!inventory.getStackInSlot(i).isEmpty())
                    this.entityDropItem(inventory.getStackInSlot(i), 1F);
            }
            int meta = 0;

            //whether it is an express drone
            if(this.dataManager.get(IS_EXPRESS))
                meta = 2;

            if(chunkLoading)
                meta += 1;

            this.entityDropItem(new ItemStack(ModItems.drone, 1, meta), 1F);
        }

        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(IS_EXPRESS, false);
    }

    public void setChunkLoading() {
        this.chunkLoading = true;
        if(!world.isRemote && this.loaderTicket == null) {
            init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
        }
    }

    @Override
    public double getSpeed() {
        return this.dataManager.get(IS_EXPRESS) ? 0.375 * 3 : 0.375;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        NBTTagList list = new NBTTagList();

        for(int i = 0; i < this.inventory.getSlots(); ++i) {
            if(!this.inventory.getStackInSlot(i).isEmpty()) {
                NBTTagCompound stackCompund = new NBTTagCompound();
                stackCompund.setByte("Slot", (byte) i);
                this.inventory.getStackInSlot(i).writeToNBT(stackCompund);
                list.appendTag(stackCompund);
            }
        }

        compound.setTag("Items", list);

        if(fluid != null) {
            compound.setInteger("fluidType", fluid.type.getID());
            compound.setInteger("fluidAmount", fluid.fill);
        }

        compound.setBoolean("load", this.dataManager.get(IS_EXPRESS));
        compound.setBoolean("chunkLoading", chunkLoading);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        NBTTagList list = nbt.getTagList("Items", 10);
        this.inventory = new ItemStackHandler(this.getSizeInventory());

        for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound stackCompound = list.getCompoundTagAt(i);
            int j = stackCompound.getByte("Slot") & 255;

            if(j >= 0 && j < this.inventory.getSlots()) {
                this.inventory.setStackInSlot(i, new ItemStack(stackCompound));
            }
        }

        if(nbt.hasKey("fluidType")) {
            FluidType type = Fluids.fromNameCompat(nbt.getString("fluidType"));
            if(type != Fluids.NONE) {
                nbt.removeTag(nbt.getString("fluidType"));
            } else
                type = Fluids.fromID(nbt.getInteger("fluidType"));

            this.fluid = new FluidStack(type, nbt.getInteger("fluidAmount"));
        }

        this.dataManager.set(IS_EXPRESS, nbt.getBoolean("load"));
        this.chunkLoading = nbt.getBoolean("chunkLoading");
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if(!this.inventory.getStackInSlot(slot).isEmpty()) {
            ItemStack itemstack;

            if (this.inventory.getStackInSlot(slot).getCount() <= amount) {
                itemstack = this.inventory.getStackInSlot(slot);
                this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                itemstack = this.inventory.getStackInSlot(slot).splitStack(amount);

                if(this.inventory.getStackInSlot(slot).getCount() == 0) {
                    this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                }

            }
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = this.inventory.getStackInSlot(index);

        if (itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            this.inventory.setStackInSlot(index, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.inventory.setStackInSlot(slot, stack);
    }

    @Override public int getSizeInventory() { return 18; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return false; }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }

    @Override public void markDirty() { }
    @Override public void openInventory(EntityPlayer player) { }
    @Override public void closeInventory(EntityPlayer player) { }

    @Override public boolean isEmpty() { return false; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) { }
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { }

    @Override
    protected void loadNeighboringChunks() {
        if(!world.isRemote && loaderTicket != null) {

            for(ChunkPos chunk : ImmutableSet.copyOf(loaderTicket.getChunkList())) {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }

            Set<ChunkPos> chunksToLoad = new LinkedHashSet<>();
            int chunkX = MathHelper.floor(this.posX / 16.0D);
            int chunkZ = MathHelper.floor(this.posZ / 16.0D);
            this.addChunkArea(chunksToLoad, chunkX, chunkZ);

            if(this.motionX * this.motionX + this.motionZ * this.motionZ > 0.0001D) {
                int lookAheadChunkX = MathHelper.floor((this.posX + this.motionX * 16.0D) / 16.0D);
                int lookAheadChunkZ = MathHelper.floor((this.posZ + this.motionZ * 16.0D) / 16.0D);
                this.addChunkArea(chunksToLoad, lookAheadChunkX, lookAheadChunkZ);
            }

            // Keep waypoints and crates ticking before the drone reaches the chunk border.
            for(ChunkPos chunk : chunksToLoad) {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
    }

    private void addChunkArea(Set<ChunkPos> chunksToLoad, int centerChunkX, int centerChunkZ) {
        for(int chunkOffsetX = -1; chunkOffsetX <= 1; chunkOffsetX++) {
            for(int chunkOffsetZ = -1; chunkOffsetZ <= 1; chunkOffsetZ++) {
                chunksToLoad.add(new ChunkPos(centerChunkX + chunkOffsetX, centerChunkZ + chunkOffsetZ));
            }
        }
    }

    @Override
    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        this.loadNeighboringChunks();
    }

    @Override
    public void setDead() {
        super.setDead();
        this.clearChunkLoader();
    }

    public void clearChunkLoader() {
        if(!world.isRemote && loaderTicket != null) {
            ForgeChunkManager.releaseTicket(loaderTicket);
            this.loaderTicket = null;
        }
    }

    @Override
    public void init(Ticket ticket) {
        if(!world.isRemote && ticket != null) {
            if(loaderTicket == null) {
                loaderTicket = ticket;
                loaderTicket.bindEntity(this);
                loaderTicket.getModData();
            } else if(loaderTicket != ticket) {
                ForgeChunkManager.releaseTicket(ticket);
            }
            this.loadNeighboringChunks();
        }
    }
}
