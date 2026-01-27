package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineBrickFurnace;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.container.ContainerFurnaceBrick;
import com.hbm.inventory.gui.GUIFurnaceBrick;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
@AutoRegister
public class TileEntityFurnaceBrick extends TileEntityMachineBase implements IGUIProvider, ITickable {

    private static final int[] slotsTop = new int[] { 0 };
    private static final int[] slotsBottom = new int[] { 2, 3 }; // intentionally modified from 1.7 to avoid fuels being sucked out
    private static final int[] slotsSides = new int[] {1};

    public static HashMap<Item, Integer> burnSpeed = new HashMap();

    static {
        burnSpeed.put(Items.CLAY_BALL,								4);
        burnSpeed.put(ModItems.ball_fireclay,						4);
        burnSpeed.put(Item.getItemFromBlock(Blocks.NETHERRACK),		4);
        burnSpeed.put(Item.getItemFromBlock(Blocks.COBBLESTONE),	2);
        burnSpeed.put(Item.getItemFromBlock(Blocks.SAND),			2);
        burnSpeed.put(Item.getItemFromBlock(Blocks.LOG),			2);
        burnSpeed.put(Item.getItemFromBlock(Blocks.LOG2),			2);
    }

    public int burnTime;
    public int maxBurnTime;
    public int progress;

    public int ashLevelWood;
    public int ashLevelCoal;
    public int ashLevelMisc;

    public TileEntityFurnaceBrick() {
        super(4);
    }

    @Override
    public String getDefaultName() {
        return "container.furnaceBrick";
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            boolean wasBurning = this.burnTime > 0;
            boolean markDirty = false;

            if(this.burnTime > 0) {
                this.burnTime--;
            }

            if(this.burnTime != 0 || !this.inventory.getStackInSlot(1).isEmpty() && !this.inventory.getStackInSlot(0).isEmpty()) {
                if(this.burnTime == 0 && this.canSmelt()) {
                    this.maxBurnTime = this.burnTime = TileEntityFurnace.getItemBurnTime(this.inventory.getStackInSlot(1));

                    if(this.burnTime > 0) {
                        markDirty = true;

                        if(!this.inventory.getStackInSlot(1).isEmpty()) {
                            this.inventory.getStackInSlot(1).shrink(1);

                            ItemEnums.EnumAshType type = TileEntityFireboxBase.getAshFromFuel(inventory.getStackInSlot(1));
                            if(type == ItemEnums.EnumAshType.WOOD) ashLevelWood += burnTime;
                            if(type == ItemEnums.EnumAshType.COAL) ashLevelCoal += burnTime;
                            if(type == ItemEnums.EnumAshType.MISC) ashLevelMisc += burnTime;
                            int threshold = 2000;
                            if(processAsh(ashLevelWood, ItemEnums.EnumAshType.WOOD, threshold)) ashLevelWood -= threshold;
                            if(processAsh(ashLevelCoal, ItemEnums.EnumAshType.COAL, threshold)) ashLevelCoal -= threshold;
                            if(processAsh(ashLevelMisc, ItemEnums.EnumAshType.MISC, threshold)) ashLevelMisc -= threshold;

                            if(this.inventory.getStackInSlot(1).getCount() == 0) {
                                this.inventory.setStackInSlot(1, inventory.getStackInSlot(1).getItem().getContainerItem(inventory.getStackInSlot(1)));
                            }
                        }
                    }
                }

                if(this.burnTime > 0 && this.canSmelt()) {
                    this.progress += this.getBurnSpeed();

                    if(this.progress >= 200) {
                        this.progress = 0;
                        this.smeltItem();
                        markDirty = true;
                    }
                } else {
                    this.progress = 0;
                }
            }

            if(wasBurning != this.burnTime > 0) {
                markDirty = true;
                MachineBrickFurnace.updateBlockState(this.burnTime > 0, this.world, this.getPos());
            }

            if(markDirty) {
                this.markDirty();
            }

            this.networkPackNT(15);
        }
    }

    public int getBurnSpeed() {
        Integer speed = burnSpeed.get(inventory.getStackInSlot(0).getItem());
        if(speed != null) return speed;
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot < 2 && (slot != 1 || TileEntityFurnace.getItemBurnTime(stack) > 0);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return side.getIndex() == 0 ? slotsBottom : (side.getIndex() == 1 ? slotsTop : slotsSides);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(burnTime);
        buf.writeInt(maxBurnTime);
        buf.writeInt(progress);
    }

    @Override public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.burnTime = buf.readInt();
        this.maxBurnTime = buf.readInt();
        this.progress = buf.readInt();
    }

    protected boolean processAsh(int level, ItemEnums.EnumAshType type, int threshold) {

        if(level >= threshold) {
            if(inventory.getStackInSlot(3).isEmpty()) {
                inventory.setStackInSlot(3, OreDictManager.DictFrame.fromOne(ModItems.powder_ash, type));
                return true;
            } else if(inventory.getStackInSlot(3).getCount() < inventory.getStackInSlot(3).getMaxStackSize() && inventory.getStackInSlot(3).getItem() == ModItems.powder_ash && inventory.getStackInSlot(3).getItemDamage() == type.ordinal()) {
                inventory.getStackInSlot(3).grow(1);
                return true;
            }
        }

        return false;
    }

    private boolean canSmelt() {
                if(inventory.getStackInSlot(0).isEmpty()) {
            return false;
        } else {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(0));
            if(itemstack.isEmpty())
                return false;
            if(inventory.getStackInSlot(2).isEmpty())
                return true;
            if(!inventory.getStackInSlot(2).isItemEqual(itemstack))
                return false;
            int result = inventory.getStackInSlot(2).getCount() + itemstack.getCount();
            return result <= 64 && result <= inventory.getStackInSlot(2).getMaxStackSize();
        }
    }

    public void smeltItem() {
        if(this.canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.inventory.getStackInSlot(0));

            if(this.inventory.getStackInSlot(2).isEmpty()) {
                this.inventory.setStackInSlot(2, itemstack.copy());
            } else if(this.inventory.getStackInSlot(2).getItem() == itemstack.getItem()) {
                this.inventory.getStackInSlot(2).grow(itemstack.getCount());
            }

            this.inventory.getStackInSlot(0).shrink(1);

            if(this.inventory.getStackInSlot(0).getCount() <= 0) {
                this.inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.burnTime = nbt.getInteger("burnTime");
        this.maxBurnTime = nbt.getInteger("maxBurn");
        this.progress = nbt.getInteger("progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("burnTime", this.burnTime);
        nbt.setInteger("maxBurn", this.maxBurnTime);
        nbt.setInteger("progress", this.progress);
        return super.writeToNBT(nbt);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFurnaceBrick(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFurnaceBrick(player.inventory, this);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (Library.isSwappingBetweenVariants(oldState, newState, ModBlocks.machine_furnace_brick_on, ModBlocks.machine_furnace_brick_off)) return false;
        return super.shouldRefresh(world, pos, oldState, newState);
    }
}
