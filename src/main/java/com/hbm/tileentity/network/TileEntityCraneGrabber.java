package com.hbm.tileentity.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneGrabber;
import com.hbm.inventory.gui.GUICraneGrabber;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityCraneGrabber extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {
    public boolean isIndirectlyPowered;
    public boolean isWhitelist = false;
    public ModulePatternMatcher matcher;
    public long lastGrabbedTick = 0;

    public TileEntityCraneGrabber() {
        super(0);

        inventory = new ItemStackHandler(11) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot >= 9 && slot <= 10)
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        };

        this.matcher = new ModulePatternMatcher(9);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!world.isRemote) isIndirectlyPowered = world.isBlockPowered(pos);
    }

    @Override
    public String getDefaultName() {
        return "container.craneGrabber";
    }

    @Override
    public void update() {
        super.update();
        if(!world.isRemote) {

            int delay = 20;
            Item ejector = inventory.getStackInSlot(10).getItem();
            if(ejector == ModItems.upgrade_ejector_1) delay = 10;
            else if(ejector == ModItems.upgrade_ejector_2) delay = 5;
            else if(ejector == ModItems.upgrade_ejector_3) delay = 2;

            if(world.getTotalWorldTime() >= lastGrabbedTick + delay && !isIndirectlyPowered) {
                int amount = 1;
                Item stackUpgrade = inventory.getStackInSlot(9).getItem();
                if(stackUpgrade == ModItems.upgrade_stack_1) amount = 4;
                else if(stackUpgrade == ModItems.upgrade_stack_2) amount = 16;
                else if(stackUpgrade == ModItems.upgrade_stack_3) amount = 64;

                EnumFacing inputSide = getInputSide();
                EnumFacing outputSide = getOutputSide();
                BlockPos outputPos = pos.offset(outputSide);
                Block beltBlock = world.getBlockState(outputPos).getBlock();

                double reach = 1D;
                if(inputSide.getAxis() != EnumFacing.Axis.Y) {
                    Block b = world.getBlockState(pos.offset(inputSide)).getBlock();
                    if(b == ModBlocks.conveyor_double) reach = 0.5D;
                    if(b == ModBlocks.conveyor_triple) reach = 0.33D;
                }

                double x = pos.getX() + inputSide.getXOffset() * reach;
                double y = pos.getY() + inputSide.getYOffset() * reach;
                double z = pos.getZ() + inputSide.getZOffset() * reach;
                List<EntityMovingItem> items = world.getEntitiesWithinAABB(EntityMovingItem.class, new AxisAlignedBB(x + 0.1875D, y + 0.1875D, z + 0.1875D, x + 0.8125D, y + 0.8125D, z + 0.8125D));

                if(beltBlock instanceof IConveyorBelt belt) {
                    for(EntityMovingItem item : items) {
                        if(item.isDead) continue;
                        ItemStack stack = item.getItemStack();
                        boolean match = this.matchesFilter(stack);
                        if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

                        lastGrabbedTick = world.getTotalWorldTime();

                        Vec3d itemPos = new Vec3d(pos.getX() + 0.5 + outputSide.getXOffset() * 0.55, pos.getY() + 0.5 + outputSide.getYOffset() * 0.55, pos.getZ() + 0.5 + outputSide.getZOffset() * 0.55);
                        Vec3d snap = belt.getClosestSnappingPosition(world, outputPos, itemPos);
                        EntityMovingItem newItem = new EntityMovingItem(world);
                        newItem.setItemStack(stack.copy());
                        newItem.setPosition(snap.x, snap.y, snap.z);
                        item.setDead();
                        world.spawnEntity(newItem);
                        break;
                    }
                } else {
                    TileEntity te = world.getTileEntity(outputPos);
                    if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide.getOpposite())) {
                        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide.getOpposite());

                        for(EntityMovingItem item : items) {
                            if(item.isDead) continue;
                            ItemStack stack = item.getItemStack();
                            boolean match = this.matchesFilter(stack);
                            if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

                            lastGrabbedTick = world.getTotalWorldTime();

                            int toAdd = Math.min(stack.getCount(), amount);
                            ItemStack copy = stack.copy();
                            copy.setCount(toAdd);
                            tryInsertItemCap(cap, copy);
                            int didAdd = toAdd - copy.getCount();

                            if(stack.getCount() - didAdd <= 0) {
                                item.setDead();
                            } else if(didAdd > 0) {
                                ItemStack remaining = stack.copy();
                                remaining.shrink(didAdd);
                                item.setItemStack(remaining);
                            }

                            amount -= didAdd;
                            if(amount <= 0) break;
                        }
                    }
                }
            }

            networkPackNT(15);
        }
    }

    //Unloads output into chests. Capability version.
    public static boolean tryInsertItemCap(IItemHandler chest, ItemStack stack) {
        if(stack.isEmpty()) return false;

        boolean movedAny = false;

        for(int i = 0; i < chest.getSlots() && !stack.isEmpty(); i++) {
            ItemStack probe = stack.copy();
            probe.setCount(1);
            ItemStack simOne = chest.insertItem(i, probe, true);
            if(!simOne.isEmpty()) continue;

            int maxTry = Math.min(stack.getCount(), chest.getSlotLimit(i));
            int accepted = findMaxInsertable(chest, i, stack, maxTry);

            if(accepted > 0) {
                ItemStack toInsert = stack.copy();
                toInsert.setCount(accepted);
                ItemStack rest = chest.insertItem(i, toInsert, false);

                int actuallyInserted = accepted - (!rest.isEmpty() ? rest.getCount() : 0);
                if(actuallyInserted > 0) {
                    stack.shrink(actuallyInserted);
                    movedAny = true;
                }
            }
        }

        return movedAny;
    }

    private static int findMaxInsertable(IItemHandler target, int slot, ItemStack stack, int upperBound) {
        int lo = 0;
        int hi = upperBound;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            ItemStack test = stack.copy();
            test.setCount(mid);
            ItemStack res = target.insertItem(slot, test, true);
            if (res.isEmpty()) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(isWhitelist);
        this.matcher.serialize(buf);
    }

    public void deserialize(ByteBuf buf) {
        this.isWhitelist = buf.readBoolean();
        this.matcher.modes = new String[this.matcher.modes.length];
        this.matcher.deserialize(buf);
    }

    public boolean matchesFilter(ItemStack stack) {

        for(int i = 0; i < 9; i++) {
            ItemStack filter = inventory.getStackInSlot(i);

            if(!filter.isEmpty() && this.matcher.isValidForFilter(filter, i, stack)) {
                return true;
            }
        }
        return false;
    }

    public void nextMode(int i) {
        this.matcher.nextMode(world, inventory.getStackInSlot(i), i);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCraneGrabber(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneGrabber(player.inventory, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isWhitelist = nbt.getBoolean("isWhitelist");
        this.matcher.readFromNBT(nbt);
        this.lastGrabbedTick = nbt.getLong("lastGrabbedTick");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isWhitelist", this.isWhitelist);
        this.matcher.writeToNBT(nbt);
        nbt.setLong("lastGrabbedTick", lastGrabbedTick);
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        return new Vec3d(xCoord - player.posX, yCoord - player.posY, zCoord - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
        if(data.hasKey("whitelist")) {
            this.isWhitelist = !this.isWhitelist;
        }
    }
}
