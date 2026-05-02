package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister
public class TileEntityConveyorPress extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IConnectionAnchors {

    public int usage = 100;
    public long power = 0;
    public final static long maxPower = 50000;

    public double speed = 0.125;
    public double press;
    public double renderPress;
    public double lastPress;
    private double syncPress;
    private int turnProgress;
    protected boolean isRetracting = false;
    private int delay;

    public ItemStack syncStack = ItemStack.EMPTY;

    public TileEntityConveyorPress() {
        super(1, false, true);
    }

    @Override
    public String getDefaultName() {
        return "";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.updateConnections();

            if(delay <= 0) {

                if(isRetracting) {

                    if(this.canRetract()) {
                        this.press -= speed;
                        this.power -= this.usage;

                        if(press <= 0) {
                            press = 0;
                            this.isRetracting = false;
                            delay = 0;
                        }
                    }

                } else {

                    if(this.canExtend()) {
                        this.press += speed;
                        this.power -= this.usage;

                        if(press >= 1) {
                            press = 1;
                            this.isRetracting = true;
                            delay = 5;
                            this.process();
                        }
                    }
                }

            } else {
                delay--;
            }

            this.networkPackNT(50);
        } else {

            // approach-based interpolation, GO!
            this.lastPress = this.renderPress;

            if(this.turnProgress > 0) {
                this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (double) this.turnProgress);
                --this.turnProgress;
            } else {
                this.renderPress = this.syncPress;
            }
        }
    }

    protected void updateConnections() {
        for(DirPos pos : getConPos()) this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
    }

    public DirPos[] getConPos() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        return new DirPos[] {
                new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
                new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
                new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z),
        };
    }

    public boolean canExtend() {

        if(this.power < usage) return false;
        if(inventory.getStackInSlot(0).isEmpty()) return false;
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        List<EntityMovingItem> items = world.getEntitiesWithinAABB(EntityMovingItem.class, new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 1.5, zCoord + 1));
        if(items.isEmpty()) return false;

        for(EntityMovingItem item : items) {
            ItemStack stack = item.getItemStack();
            if(!PressRecipes.getOutput(stack, inventory.getStackInSlot(0)).isEmpty() && stack.getCount() == 1) {

                double d0 = 0.35;
                double d1 = 0.65;
                if(item.posX > xCoord + d0 && item.posX < xCoord + d1 && item.posZ > zCoord + d0 && item.posZ < zCoord + d1) {
                    item.setPosition(xCoord + 0.5, item.posY, zCoord + 0.5);
                }

                return true;
            }
        }

        return false;
    }

    public void process() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        List<EntityMovingItem> items = world.getEntitiesWithinAABB(EntityMovingItem.class,new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 1.5, zCoord + 1));

        for(EntityMovingItem item : items) {
            ItemStack stack = item.getItemStack();
            ItemStack output = PressRecipes.getOutput(stack, inventory.getStackInSlot(0));

            if(!output.isEmpty() && stack.getCount() == 1) {
                item.setDead();
                EntityMovingItem out = new EntityMovingItem(world);
                out.setPosition(item.posX, item.posY, item.posZ);
                out.setItemStack(output.copy());
                world.spawnEntity(out);
            }
        }

        this.world.playSound(null, pos, HBMSoundHandler.pressOperate, SoundCategory.BLOCKS, getVolume(1.5F), 1.0F);

        if(inventory.getStackInSlot(0).getMaxDamage() != 0) {
            ItemStack stack = inventory.getStackInSlot(0).copy();
            stack.setItemDamage(stack.getItemDamage() + 1);
            if(stack.getItemDamage() >= stack.getMaxDamage()) {
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            } else inventory.setStackInSlot(0, stack);
        }
    }

    public boolean canRetract() {
        return this.power >= usage;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeLong(this.power);
        buf.writeDouble(this.press);
        BufferUtil.writeItemStack(buf, inventory.getStackInSlot(0));
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        this.power = buf.readLong();
        this.syncPress = buf.readDouble();
        this.syncStack = BufferUtil.readItemStack(buf);

        this.turnProgress = 2;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return stack.getItem() instanceof ItemStamp;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] { 0 };
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.DOWN;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");
        this.press = nbt.getDouble("press");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("power", power);
        nbt.setDouble("press", press);
        return nbt;
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(
                    xCoord - 1,
                    yCoord,
                    zCoord - 1,
                    xCoord + 2,
                    yCoord + 3,
                    zCoord + 2
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
