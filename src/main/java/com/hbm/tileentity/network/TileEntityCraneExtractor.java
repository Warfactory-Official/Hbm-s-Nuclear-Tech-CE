package com.hbm.tileentity.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IEnterableBlock;
import cofh.core.util.core.SideConfig;
import cofh.core.util.core.SlotConfig;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneExtractor;
import com.hbm.inventory.gui.GUICraneExtractor;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ItemStackHandlerWrapper;
import com.hbm.lib.Library;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityCraneExtractor extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {
    public boolean isIndirectlyPowered;
    public boolean isWhitelist = false;
    public boolean maxEject = false;
    protected SideConfig sideConfig;
    protected SlotConfig slotConfig;
    public byte[] sideCache;

    private boolean isCofhCoreLoaded() {
        return Loader.isModLoaded("cofhcore");
    }
    private int tickCounter = 0;
    public ModulePatternMatcher matcher;

    public static int[] allowed_slots = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private IItemHandler nullInsertHandler;

    public TileEntityCraneExtractor() {
        super(0);

        inventory = new ItemStackHandler(20) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                super.setStackInSlot(slot, stack);
                if (Library.isMachineUpgrade(stack) && slot >= 18 && slot <= 19)
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        };

        this.matcher = new ModulePatternMatcher(9);
    }

    @Override
    public String getDefaultName() {
        return "container.craneExtractor";
    }

    public boolean canExtractItemExtended(int slot, EnumFacing side) {
        if (side == null) {
            return true;
        } else {
            if (slot < 9) {
                return SideConfig.allowExtraction(this.sideConfig.sideTypes[this.sideCache[side.ordinal()]])
                        && (this.sideConfig.sideTypes[this.sideCache[side.ordinal()]] == 7
                        || this.slotConfig.allowExtractionSlot[slot]);
            }
            else {
                return true;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!world.isRemote) isIndirectlyPowered = world.isBlockPowered(pos);
    }

    public boolean canExtractItemCE(int slot, ItemStack stack, EnumFacing side, ISidedInventory sided) {
        if(isCofhCoreLoaded()) {
            return canExtractItemExtended(slot, side);
        } else {
            return sided.canExtractItem(slot, stack, side);
        }
    }

    @Override
    public void update() {
        super.update();
        if(!world.isRemote) {

            tickCounter++;

            int delay = 20;

            ItemStack ejector = inventory.getStackInSlot(19);
            if(!ejector.isEmpty()){
                if(ejector.getItem() == ModItems.upgrade_ejector_1) {
                    delay = 10;
                } else if(ejector.getItem() == ModItems.upgrade_ejector_2){
                    delay = 5;
                } else if(ejector.getItem() == ModItems.upgrade_ejector_3){
                    delay = 2;
                }
            }

            if(tickCounter >= delay && !isIndirectlyPowered) {
                tickCounter = 0;
                int amount = 1;

                ItemStack stackUpgrade = inventory.getStackInSlot(18);
                if(!stackUpgrade.isEmpty()){
                    if(stackUpgrade.getItem() == ModItems.upgrade_stack_1) {
                        amount = 4;
                    } else if(stackUpgrade.getItem() == ModItems.upgrade_stack_2){
                        amount = 16;
                    } else if(stackUpgrade.getItem() == ModItems.upgrade_stack_3){
                        amount = 64;
                    }
                }

                EnumFacing inputSide = getOutputSide(); // note the switcheroo!
                EnumFacing outputSide = getInputSide();
                EnumFacing inputAccessSide = inputSide.getOpposite();
                TileEntity te = world.getTileEntity(pos.offset(inputSide));
                Block b = world.getBlockState(pos.offset(outputSide)).getBlock();

                int[] access = null;
                ISidedInventory sided = null;

                if(te instanceof ISidedInventory && !(te instanceof TileEntityCraneExtractor)) {
                    sided = (ISidedInventory) te;
                    //access = sided.getAccessibleSlotsFromSide(dir.ordinal());
                    access = masquerade(sided, inputAccessSide);
                }

                boolean hasSent = false;

                IConveyorBelt belt = b instanceof IConveyorBelt ? (IConveyorBelt) b : null;

                /* try to send items from a connected inv, if present */
                if(te != null && !(te instanceof TileEntityCraneExtractor) && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputAccessSide)) {

                    IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputAccessSide);
                    if(inv != null) {
                        int size = access == null ? inv.getSlots() : access.length;

                        for(int i = 0; i < size; i++) {
                            int index = access == null ? i : access[i];
                            ItemStack stack = inv.getStackInSlot(index);

                            if(!stack.isEmpty() && (sided == null || canExtractItemCE(index, stack, inputAccessSide, sided))) {

                                int maxTarget = Math.min(amount, stack.getMaxStackSize());
                                if(this.maxEject && stack.getCount() < maxTarget) continue;
                                boolean match = this.matchesFilter(stack);

                                if((isWhitelist && match) || (!isWhitelist && !match)) {
                                    int toSend = Math.min(amount, stack.getCount());

                                    if (belt != null) {
                                        ItemStack extracted = inv.extractItem(index, toSend, false);
                                        if(!extracted.isEmpty()) {
                                            sendItem(extracted, belt, outputSide);
                                            hasSent = true;
                                            break;
                                        }
                                    } else {
                                        ItemStack simExtracted = inv.extractItem(index, toSend, true);
                                        if(!simExtracted.isEmpty()) {
                                            int fill = tryInsertItemCap(inventory, simExtracted.copy(), allowed_slots);
                                            if(fill > 0 && fill <= toSend) {
                                                inv.extractItem(index, fill, false);
                                                hasSent = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /* if no item has been sent, send buffered items while ignoring the filter */
                if(!hasSent && belt != null) {

                    for(int index : allowed_slots) {
                        ItemStack stack = inventory.getStackInSlot(index);

                        if(!stack.isEmpty()){
                            int maxTarget = Math.min(amount, stack.getMaxStackSize());
                            if(this.maxEject && stack.getCount() < maxTarget) continue;

                            int toSend = Math.min(amount, stack.getCount());
                            ItemStack cStack = stack.copy();
                            cStack.setCount(toSend);
                            stack.shrink(toSend);
                            if(stack.getCount() == 0) inventory.setStackInSlot(index, ItemStack.EMPTY);

                            sendItem(cStack, belt, outputSide);

                            break;
                        }
                    }
                }
            }

            networkPackNT(15);
        }
    }

    private void sendItem(ItemStack stack, IConveyorBelt belt, EnumFacing outputSide) {
        BlockPos targetPos = pos.offset(outputSide);
        EntityMovingItem moving = new EntityMovingItem(world);
        Vec3d itemPos = new Vec3d(
                pos.getX() + 0.5 + outputSide.getXOffset() * 0.55,
                pos.getY() + 0.5 + outputSide.getYOffset() * 0.55,
                pos.getZ() + 0.5 + outputSide.getZOffset() * 0.55);
        Vec3d snap = belt.getClosestSnappingPosition(world, targetPos, itemPos);
        moving.setPosition(snap.x, snap.y, snap.z);
        moving.setItemStack(stack);
        world.spawnEntity(moving);

        if (belt instanceof IEnterableBlock enterable) {
            if (enterable.canItemEnter(world, targetPos.getX(), targetPos.getY(), targetPos.getZ(), outputSide.getOpposite(), moving)) {
                enterable.onItemEnter(world, targetPos.getX(), targetPos.getY(), targetPos.getZ(), outputSide.getOpposite(), moving);
                moving.setDead();
            }
        }
    }

    //Unloads output into chests. Capability version.
    public static int tryInsertItemCap(IItemHandler chest, ItemStack stack, int[] allowed_slots) {
        if(stack.isEmpty()) return 0;

        int filledAmount = 0;

        for(int i : allowed_slots) {
            if(stack.isEmpty()) break;

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
                    filledAmount += actuallyInserted;
                }
            }
        }

        return filledAmount;
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

    public static int[] masquerade(ISidedInventory sided, EnumFacing side) {

        if(sided instanceof TileEntityFurnace) {
            return new int[] {2};
        }

        return sided.getSlotsForFace(side);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(isWhitelist);
        buf.writeBoolean(maxEject);
        this.matcher.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.isWhitelist = buf.readBoolean();
        this.maxEject = buf.readBoolean();
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

    public void initPattern(ItemStack stack, int index) {
        this.matcher.initPatternStandard(world, stack, index);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i > 8 && i < 18;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {

        return new ContainerCraneExtractor(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneExtractor(player.inventory, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isWhitelist = nbt.getBoolean("isWhitelist");
        this.maxEject = nbt.getBoolean("maxEject");
        this.matcher.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isWhitelist", this.isWhitelist);
        nbt.setBoolean("maxEject", this.maxEject);
        this.matcher.writeToNBT(nbt);
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
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("whitelist")) {
            this.isWhitelist = !this.isWhitelist;
        }
        if(data.hasKey("maxEject")) {
            this.maxEject = !this.maxEject;
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return allowed_slots;
    }
    
    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack) {
        return this.isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return false;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            if (facing == null) {
                if (nullInsertHandler == null) {
                    nullInsertHandler = new ItemStackHandlerWrapper(inventory, allowed_slots) {
                        @Override
                        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                            return isItemValidForSlot(slot, stack);
                        }
                    };
                }
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(nullInsertHandler);
            }
        }
        return super.getCapability(capability, facing);
    }
}
