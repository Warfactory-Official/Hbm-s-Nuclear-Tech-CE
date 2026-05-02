package com.hbm.tileentity.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneBoxer;
import com.hbm.inventory.gui.GUICraneBoxer;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityCraneBoxer extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {

    public byte mode = 0;
    public static final byte MODE_4 = 0;
    public static final byte MODE_8 = 1;
    public static final byte MODE_16 = 2;
    public static final byte MODE_REDSTONE = 3;
    private static final int MODE_VERSION = 1;

    public static int[] allowed_slots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

    private boolean lastRedstone = false;


    public TileEntityCraneBoxer() {
        super(21);
    }

    @Override
    public String getDefaultName() {
        return "container.craneBoxer";
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            int xCoord = pos.getX();
            int yCoord = pos.getY();
            int zCoord = pos.getZ();
            boolean redstone = world.isBlockPowered(pos);

            if (mode == MODE_REDSTONE && redstone && !lastRedstone) {
                EnumFacing outputSide = getOutputSide();
                BlockPos outputPos = pos.offset(outputSide);
                Block outputBlock = world.getBlockState(outputPos).getBlock();
                IConveyorBelt belt = null;

                if (outputBlock instanceof IConveyorBelt) {
                    belt = (IConveyorBelt) outputBlock;
                }

                int pack = 0;

                for (int index : allowed_slots) {
                    ItemStack stack = inventory.getStackInSlot(index);

                    if(!stack.isEmpty()){
                        pack++;
                    }
                }

                if (belt != null && pack > 0) {
                    ItemStack[] box = new ItemStack[pack];

                    for (int index : allowed_slots) {
                        if (pack > 0) {
                            ItemStack stack = inventory.getStackInSlot(index);
                            if (!stack.isEmpty()) {
                                pack--;
                                box[pack] = stack.copy();
                                inventory.setStackInSlot(index, ItemStack.EMPTY);
                            }
                        }
                    }

                    EntityMovingPackage moving = new EntityMovingPackage(world);
                    Vec3d pos = new Vec3d(xCoord + 0.5 + outputSide.getDirectionVec().getX() * 0.55, yCoord + 0.5 + outputSide.getDirectionVec().getY() * 0.55, zCoord + 0.5 + outputSide.getDirectionVec().getZ() * 0.55);
                    Vec3d snap = belt.getClosestSnappingPosition(world, outputPos, pos);
                    moving.setPosition(snap.x, snap.y, snap.z);
                    moving.setItemStacks(box);
                    world.spawnEntity(moving);
                }
            }

            this.lastRedstone = redstone;

            if(mode != MODE_REDSTONE && world.getTotalWorldTime() % 2 == 0) {
                int pack = switch (mode) {
                    case MODE_4 -> 4;
                    case MODE_8 -> 8;
                    case MODE_16 -> 16;
                    default -> 1;
                };

                int fullStacks = 0;

                for(int index : allowed_slots) {
                    ItemStack stack = inventory.getStackInSlot(index);

                    if(!stack.isEmpty() && stack.getCount() == stack.getMaxStackSize()) {
                        fullStacks++;
                    }
                }

                EnumFacing outputSide = getOutputSide();
                Block b = world.getBlockState(pos.offset(outputSide)).getBlock();
                IConveyorBelt belt = null;

                if(b instanceof IConveyorBelt) {
                    belt = (IConveyorBelt) b;
                }

                if(belt != null && fullStacks >= pack) {

                    ItemStack[] box = new ItemStack[pack];

                    for(int index : allowed_slots) {
                        ItemStack stack = inventory.getStackInSlot(index);

                        if(!stack.isEmpty() && stack.getCount() == stack.getMaxStackSize()) {
                            pack--;
                            if(pack >= 0){
                                box[pack] = stack.copy();
                                inventory.setStackInSlot(index, ItemStack.EMPTY);
                            }
                        }
                    }

                    EntityMovingPackage moving = new EntityMovingPackage(world);
                    Vec3d posV = new Vec3d(xCoord + 0.5 + outputSide.getDirectionVec().getX() * 0.55, yCoord + 0.5 + outputSide.getDirectionVec().getY() * 0.55, zCoord + 0.5 + outputSide.getDirectionVec().getZ() * 0.55);
                    Vec3d snap = belt.getClosestSnappingPosition(world, pos.offset(outputSide), posV);
                    moving.setPosition(snap.x, snap.y, snap.z);
                    moving.setItemStacks(box);
                    world.spawnEntity(moving);
                }
            }

            networkPackNT(15);
        }
    }

    public boolean tryFillTeDirect(ItemStack stack){
        return tryInsertItemCap(inventory, stack);
    }

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
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return allowed_slots;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCraneBoxer(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        return new Vec3d(xCoord - player.posX, yCoord - player.posY, zCoord - player.posZ).length() < 20;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneBoxer(player.inventory, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        byte storedMode = nbt.getByte("mode");
        this.mode = nbt.hasKey("boxerModeVersion") ? normalizeCurrentMode(storedMode) : convertLegacyMode(storedMode);
        this.lastRedstone = nbt.getBoolean("lastRedstone");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("mode", mode);
        nbt.setInteger("boxerModeVersion", MODE_VERSION);
        nbt.setBoolean("lastRedstone", lastRedstone);
        return nbt;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("toggle")) {
            mode = (byte) ((mode + 1) % 4);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeByte(mode);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.mode = normalizeCurrentMode(buf.readByte());
    }

    private static byte normalizeCurrentMode(byte rawMode) {
        if(rawMode < MODE_4 || rawMode > MODE_REDSTONE) {
            return MODE_4;
        }
        return rawMode;
    }

    private static byte convertLegacyMode(byte legacyMode) {
        return switch (legacyMode) {
            case 3 -> MODE_8;
            case 4 -> MODE_16;
            case 5 -> MODE_REDSTONE;
            default -> MODE_4;
        };
    }
}
