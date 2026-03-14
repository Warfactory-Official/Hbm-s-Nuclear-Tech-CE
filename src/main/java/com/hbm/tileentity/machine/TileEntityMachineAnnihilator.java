package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerMachineAnnihilator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.gui.GUIMachineAnnihilator;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.saveddata.AnnihilatorSavedData;
import com.hbm.saveddata.AnnihilatorSavedData.AnnihilatorPool;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ParticleUtil;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@AutoRegister
public class TileEntityMachineAnnihilator extends TileEntityMachineBase implements ITickable, IFluidStandardReceiverMK2, IControlReceiver, IGUIProvider {

    public String pool = "Recycling";
    public int timer;

    public FluidTankNTM tank;
    public BigInteger monitorBigInt = BigInteger.ZERO;

    public TileEntityMachineAnnihilator() {
        super(11, true, false);

        this.tank = new FluidTankNTM(Fluids.NONE, 2_500_000);
    }

    @Override
    public String getDefaultName() {
        return "container.annihilator";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.tank.setType(1, inventory);

            if(this.pool != null && !this.pool.isEmpty()) {

                for(DirPos pos : getConPos()) {
                    if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), world, pos);
                }

                AnnihilatorSavedData data = AnnihilatorSavedData.getData(world);
                boolean didSomething = false;

                ItemStack stack0 = inventory.getStackInSlot(0);
                if(!stack0.isEmpty()) {
                    tryAddPayout(data.pushToPool(pool, stack0, false));
                    this.inventory.setStackInSlot(0, ItemStack.EMPTY);
                    this.markChanged();
                    didSomething = true;
                }
                if(tank.getFill() > 0) {
                    FT_Polluting.pollute(world, getPos().getX(), getPos().getY(), getPos().getZ(), tank.getTankType(), FluidTrait.FluidReleaseType.BURN, tank.getFill() * 2);
                    tryAddPayout(data.pushToPool(pool, tank.getTankType(), tank.getFill(), false));
                    tank.setFill(0);
                    this.markChanged();
                    didSomething = true;
                }

                if(didSomething) {
                    ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                    ParticleUtil.spawnGasFlame(world, this.pos.getX() + 0.5 - dir.offsetX * 3, this.pos.getY() + 8.75, this.pos.getZ() + 0.5 - dir.offsetZ * 3, world.rand.nextGaussian() * 0.05, 0.1, world.rand.nextGaussian() * 0.05);

                    if(world.getTotalWorldTime() % 3 == 0)
                        this.world.playSound(null, this.pos.getX() + 0.5 - dir.offsetX * 3, this.pos.getY() + 8.75, this.pos.getZ() + 0.5 - dir.offsetZ * 3, HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, getVolume(1F), 0.5F + world.rand.nextFloat() * 0.25F);
                }

                ItemStack stack8 = inventory.getStackInSlot(8);
                if(!stack8.isEmpty()) {
                    if(stack8.getItem() instanceof IItemFluidIdentifier id) {
                        FluidType type = id.getType(world, pos.getX(), pos.getY(), pos.getZ(), stack8);
                        monitor(data, type);
                    } else {
                        monitor(data, new ComparableStack(stack8).makeSingular());
                    }
                }

                ItemStack stack9 = inventory.getStackInSlot(9);
                if(!stack9.isEmpty()) {
                    ItemStack single = stack9.copy();
                    single.setCount(1);
                    ItemStack payout = data.pushToPool(pool, single, true);
                    inventory.extractItem(9, 1, false);
                    if(payout != null && !payout.isEmpty()) {
                        ItemStack stack10 = inventory.getStackInSlot(10);
                        if(stack10.isEmpty()) {
                            inventory.setStackInSlot(10, payout);
                        } else if(stack10.getItem() == payout.getItem() && stack10.getItemDamage() == payout.getItemDamage() &&
                                ItemStack.areItemStackTagsEqual(stack10, payout) && stack10.getMaxStackSize() >= stack10.getCount() + payout.getCount()) {
                            stack10.grow(payout.getCount());
                            inventory.setStackInSlot(10, stack10);
                        }
                    }
                }
            }

            this.networkPackNT(25);
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 5, pos.getY(), pos.getZ() + dir.offsetZ * 5, dir),
                new DirPos(pos.getX() + dir.offsetX * 3 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 3 + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + dir.offsetX * 3 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 3 - rot.offsetZ * 2, rot.getOpposite())
        };
    }

    public void monitor(AnnihilatorSavedData data, Object type) {
        AnnihilatorPool pool = data.pools.get(this.pool);
        if(pool != null) {
            this.monitorBigInt = pool.items.get(type);
            if(this.monitorBigInt == null) this.monitorBigInt = BigInteger.ZERO;
        } else {
            this.monitorBigInt = BigInteger.ZERO;
        }
    }

    public void tryAddPayout(ItemStack payout) {
        if(payout == null || payout.isEmpty()) return;

        for(int i = 2; i <= 7; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(!stack.isEmpty() && stack.getItem() == payout.getItem() && stack.getItemDamage() == payout.getItemDamage() &&
                    ItemStack.areItemStackTagsEqual(stack, payout) && stack.getMaxStackSize() >= stack.getCount() + payout.getCount()) {
                stack.grow(payout.getCount());
                inventory.setStackInSlot(i, stack);
                this.markDirty();
                return;
            }
        }

        for(int i = 2; i <= 7; i++) {
            if(inventory.getStackInSlot(i).isEmpty()) {
                inventory.setStackInSlot(i, payout);
                this.markDirty();
                return;
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // trash
        if(slot == 1 && stack.getItem() instanceof IItemFluidIdentifier) return true;
        if(slot == 8) return true; // monitor
        return slot == 9; // payout request
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
        return slot >= 2 && slot <= 7;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] {0, 2, 3, 4, 5, 6, 7};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.readFromNBT(nbt, "t");
        this.pool = nbt.getString("pool");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tank.writeToNBT(nbt, "t");
        nbt.setString("pool", pool);
        return super.writeToNBT(nbt);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        ByteBufUtils.writeUTF8String(buf, this.pool == null ? "" : this.pool);
        byte[] array = this.monitorBigInt.toByteArray();
        buf.writeInt(array.length);
        for(byte b : array) buf.writeByte(b);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.pool = ByteBufUtils.readUTF8String(buf);
        byte[] array = new byte[buf.readInt()];
        for(int i = 0 ; i < array.length; i++) array[i] = buf.readByte();
        this.monitorBigInt = new BigInteger(array);
    }

    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {tank}; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tank}; }
    @Override public IEnergyReceiverMK2.ConnectionPriority getFluidPriority() { return IEnergyReceiverMK2.ConnectionPriority.LOW; }

    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineAnnihilator(player.inventory, this.inventory); }
    @Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineAnnihilator(player.inventory, this); }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("pool")) {
            String pool = data.getString("pool");
            if(pool != null && !pool.isEmpty()) {
                this.pool = pool;
                this.markChanged();
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 5,
                    pos.getY(),
                    pos.getZ() - 5,
                    pos.getX() + 6,
                    pos.getY() + 8,
                    pos.getZ() + 6
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
