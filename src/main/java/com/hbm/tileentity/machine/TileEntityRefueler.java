package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Random;
@AutoRegister
public class TileEntityRefueler extends TileEntityLoadedBase implements IFluidStandardReceiver, ITickable {

    public double fillLevel;
    public double prevFillLevel;

    private boolean isOperating = false;
    private int operatingTime;
    private AxisAlignedBB bb;

    public FluidTankNTM tank;

    public TileEntityRefueler() {
        super();
        tank = new FluidTankNTM(Fluids.KEROSENE, 100);
    }

    @Override
    public void update() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        if(!world.isRemote) {
            trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir);

            isOperating = false;

            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).expand(0.5, 0.0, 0.5));

            for(EntityPlayer player : players) {
                InventoryPlayer inv = player.inventory;
                for(int i = 0; i < inv.getSizeInventory(); i ++){

                    ItemStack stack = inv.getStackInSlot(i);
                    if(stack.isEmpty()) continue;

                    if(fillFillable(stack)) {
                        isOperating = true;
                    }

                    if(stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
                        for(ItemStack mod : ArmorModHandler.pryMods(stack)) {
                            if(mod == null) continue;

                            if(fillFillable(mod)) {
                                ArmorModHandler.applyMod(stack, mod);
                                isOperating = true;
                            }
                        }
                    }
                }
            }

            if(isOperating) {
                if(operatingTime % 20 == 0)
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.2F, 0.5F);

                operatingTime++;
            } else {
                operatingTime = 0;
            }

            networkPackNT(150);
        } else {
            if(isOperating) {
                Random rand = world.rand;

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "fluidfill");
                data.setInteger("color", tank.getTankType().getColor());
                data.setDouble("posX", pos.getX() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetX * 0.5 + rot.offsetX * 0.25);
                data.setDouble("posZ", pos.getZ() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetZ * 0.5 + rot.offsetZ * 0.25);
                data.setDouble("posY", pos.getY() + 0.375);
                data.setDouble("mX", -dir.offsetX + rand.nextGaussian() * 0.1);
                data.setDouble("mZ", -dir.offsetZ + rand.nextGaussian() * 0.1);
                data.setDouble("mY", 0D);

                MainRegistry.proxy.effectNT(data);
            }

            prevFillLevel = fillLevel;

            double targetFill = (double)tank.getFill() / (double)tank.getMaxFill();
            fillLevel = BobMathUtil.interp(fillLevel, targetFill, targetFill > fillLevel || !isOperating ? 0.1F : 0.01F);
        }


    }

    private boolean fillFillable(ItemStack stack) {
        if(stack.getItem() instanceof IFillableItem fillable) {
            if(fillable.acceptsFluid(tank.getTankType(), stack)) {
                int prevFill = tank.getFill();
                tank.setFill(fillable.tryFill(tank.getTankType(), tank.getFill(), stack));
                return tank.getFill() < prevFill;
            }
        }

        return false;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(isOperating);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        isOperating = buf.readBoolean();
        tank.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt, "t");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        tank.writeToNBT(nbt, "t");
        return super.writeToNBT(nbt);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] { tank };
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] { tank };
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return bb;
    }
}
