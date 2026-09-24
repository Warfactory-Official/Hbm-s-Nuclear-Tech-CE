package com.hbm.entity.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemDrone.EnumDroneType;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.Vec3NT;
import com.hbm.tileentity.network.TileEntityDroneDock;
import com.hbm.tileentity.network.TileEntityDroneProvider;
import com.hbm.tileentity.network.TileEntityDroneRequester;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister(name = "entity_request_drone", sendVelocityUpdates = false)
@Spaghetti("onUpdate needs to be cleaned up")
public class EntityRequestDrone extends EntityDroneBase {
    public @NotNull ItemStack heldItem = ItemStack.EMPTY;
    public List<Object> program = new ArrayList<>();
    int nextActionTimer = 0;

    public enum DroneProgram {
        UNLOAD, DOCK
    }

    @Override
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y + 1;
        this.targetZ = z;
    }

    public EntityRequestDrone(World world) {
        super(world);
    }

    @Override
    public boolean hitByEntity(Entity attacker) {
        if(this.isDead) return false;

        if(attacker instanceof EntityPlayer && !world.isRemote) {
            this.setDead();
            if(!heldItem.isEmpty())
                this.entityDropItem(heldItem, 1F);
            this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.REQUEST.ordinal()), 1F);
        }

        return false;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (Vec3NT.createVectorHelper(motionX, motionY, motionZ).length() < 0.01) {
                if (nextActionTimer > 0) {
                    nextActionTimer--;
                } else {
                    if (program.isEmpty()) {
                        this.setDead(); //self-destruct if no further operations are pending
                        this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.REQUEST.ordinal()), 1F);
                        return;
                    }

                    Object next = program.get(0);
                    program.remove(0);

                    if (next instanceof BlockPos pos) {
                        this.setTarget(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    } else if (next instanceof AStack aStack && heldItem.isEmpty()) {
                        //to make DAMN sure this fuckin idiot doesnt miss the dock
                        Vec3NT pos = Vec3NT.createVectorHelper(this.posX, this.posY, this.posZ);
                        Vec3NT nextPos = Vec3NT.createVectorHelper(this.posX, this.posY - 4, this.posZ);
                        RayTraceResult result = this.world.rayTraceBlocks(pos.toVec3d(), nextPos.toVec3d());

                        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                            TileEntity tile = world.getTileEntity(result.getBlockPos());
                            if (tile instanceof TileEntityDroneProvider provider) {

                                for (int i = 0; i < provider.inventory.getSlots(); i++) {
                                    ItemStack stack = provider.inventory.getStackInSlot(i);

                                    if (!stack.isEmpty() && aStack.matchesRecipe(stack, true)) {
                                        this.heldItem = stack.copy();
                                        this.setAppearance(1);
                                        world.playSound(null, posX, posY, posZ, HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);
                                        provider.inventory.setStackInSlot(i, ItemStack.EMPTY);
                                        provider.markDirty();
                                        break;
                                    }
                                }
                            }
                        }
                        nextActionTimer = 5;
                    } else if (next == DroneProgram.UNLOAD && !this.heldItem.isEmpty()) {
                        Vec3NT pos = Vec3NT.createVectorHelper(this.posX, this.posY, this.posZ);
                        Vec3NT nextPos = Vec3NT.createVectorHelper(this.posX, this.posY - 4, this.posZ);
                        RayTraceResult result = this.world.rayTraceBlocks(pos.toVec3d(), nextPos.toVec3d());

                        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {

                            TileEntity tile = world.getTileEntity(result.getBlockPos());
                            if (tile instanceof TileEntityDroneRequester requester) {

                                for (int i = 9; i < 18; i++) {
                                    ItemStack stack = requester.getStackInSlot(i);
                                    if (!stack.isEmpty() && stack.getItem() == heldItem.getItem() && stack.getItemDamage() == heldItem.getItemDamage()) {
                                        int toTransfer = Math.min(stack.getMaxStackSize() - stack.getCount(), heldItem.getCount());
                                        requester.getStackInSlot(i).grow(toTransfer);
                                        this.heldItem.shrink(toTransfer);
                                    }
                                }

                                if (this.heldItem.getCount() <= 0) this.heldItem = ItemStack.EMPTY;

                                if (!this.heldItem.isEmpty()) for (int i = 9; i < 18; i++) {
                                    if (requester.getStackInSlot(i).isEmpty()) {
                                        requester.inventory.setStackInSlot(i, this.heldItem.copy());
                                        this.heldItem = ItemStack.EMPTY;
                                        break;
                                    }
                                }

                                if (this.heldItem.isEmpty()) {
                                    this.setAppearance(0);
                                    world.playSound(null, posX, posY, posZ, HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);
                                }

                                requester.markDirty();
                            }
                        }
                        nextActionTimer = 5;
                    } else if (next == DroneProgram.DOCK) {
                        Vec3NT pos = Vec3NT.createVectorHelper(this.posX, this.posY, this.posZ);
                        Vec3NT nextPos = Vec3NT.createVectorHelper(this.posX, this.posY - 4, this.posZ);
                        RayTraceResult mop = this.world.rayTraceBlocks(pos.toVec3d(), nextPos.toVec3d());

                        if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {

                            TileEntity tile = world.getTileEntity(mop.getBlockPos());
                            if (tile instanceof TileEntityDroneDock dock) {
                                ItemStack drone = new ItemStack(ModItems.drone, 1, EnumDroneType.REQUEST.ordinal());
                                for (int i = 0; i < dock.inventory.getSlots(); i++) {
                                    if (dock.inventory.getStackInSlot(i).isEmpty()) {
                                        this.setDead();
                                        if (!heldItem.isEmpty()) {
                                            if (i != 9 && dock.inventory.getStackInSlot(i + 1).isEmpty()) {
                                                dock.dockDrone(i + 1, heldItem.copy());
                                            }
                                        }
                                        dock.dockDrone(i, drone.copy());
                                        this.world.playSound(null, dock.getPos().getX() + 0.5, dock.getPos().getY() + 0.5, dock.getPos().getZ() + 0.5, HBMSoundHandler.storageClose, SoundCategory.BLOCKS, 2.0F, 1.0F);
                                        break;
                                    } else if (dock.getStackInSlot(i).isItemEqual(drone) && dock.getStackInSlot(i).getCount() < 64) {
                                        this.setDead();
                                        if (!heldItem.isEmpty()) {
                                            if (i != 9 && dock.getStackInSlot(i + 1).isEmpty()) {
                                                dock.dockDrone(i + 1, this.heldItem.copy());
                                            }
                                        }
                                        dock.getStackInSlot(i).grow(1);
                                        this.world.playSound(null, dock.getPos().getX() + 0.5, dock.getPos().getY() + 0.5, dock.getPos().getZ() + 0.5, HBMSoundHandler.storageClose, SoundCategory.BLOCKS, 2.0F, 1.0F);
                                        break;
                                    }
                                }
                            }
                        }
                        if (!this.isDead) {
                            this.setDead();
                            if (!heldItem.isEmpty())
                                this.entityDropItem(heldItem, 1F);
                            this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.REQUEST.ordinal()), 1F);
                        }
                    }
                }
            }
        }
        super.onUpdate();
    }

    @Override
    public double getSpeed() {
        return 0.625D;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        if(nbt.hasKey("held")) {
            NBTTagCompound stack = nbt.getCompoundTag("held");
            this.heldItem = new ItemStack(stack);
        }

        nextActionTimer = 5;

        this.dataManager.set(APPEARANCE, nbt.getByte("app"));

        int size = nbt.getInteger("programSize");

        for(int i = 0; i < size; i++) {
            NBTTagCompound data = nbt.getCompoundTag("program" + i);
            String pType = data.getString("type");

            switch (pType) {
                case "pos" -> {
                    int[] pos = data.getIntArray("pos");
                    this.program.add(new BlockPos(pos[0], pos[1], pos[2]));
                }
                case "unload" -> this.program.add(DroneProgram.UNLOAD);
                case "dock" -> this.program.add(DroneProgram.DOCK);
                case "comp" -> {
                    ComparableStack comp = new ComparableStack(Item.getItemById(nbt.getInteger("id")), 1, nbt.getInteger("meta"));
                    this.program.add(comp);
                }
                case "dict" -> {
                    OreDictStack dict = new OreDictStack(nbt.getString("dict"));
                    this.program.add(dict);
                }
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        if(!heldItem.isEmpty()) {
            NBTTagCompound stack = new NBTTagCompound();
            this.heldItem.writeToNBT(stack);
            nbt.setTag("held", stack);
        }

        nbt.setByte("app", this.dataManager.get(APPEARANCE));

        int size = this.program.size();
        nbt.setInteger("programSize", size);

        for(int i = 0; i < size; i++) {
            NBTTagCompound data = new NBTTagCompound();
            Object p = this.program.get(i);

            if(p instanceof BlockPos pos) {
                data.setString("type", "pos");
                data.setIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
            } else if(p instanceof AStack) {

                // neither of these wretched fungii works correctly, but so long as the pathing works (which it does), it means that the drone will
                // eventually return to the dock and not got lost, and simply retry the task
                if(p instanceof ComparableStack comp) {
                    data.setString("type", "comp");
                    data.setInteger("id", Item.getIdFromItem(comp.item));
                    data.setInteger("meta", comp.meta);
                } else {
                    OreDictStack dict = (OreDictStack) p;
                    data.setString("type", "dict");
                    data.setString("dict", dict.name);
                }

            } else if(p == DroneProgram.UNLOAD) {
                data.setString("type", "unload");

            } else if(p == DroneProgram.DOCK) {
                data.setString("type", "dock");

            }

            nbt.setTag("program" + i, data);
        }
    }
}
