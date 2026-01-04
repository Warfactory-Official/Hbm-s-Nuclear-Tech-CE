package com.hbm.tileentity.machine.oil;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.tile.IHeatSource;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineCoker;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineCoker;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineCoker extends TileEntityMachineBase implements IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, ITickable {

    public boolean wasOn;
    public int progress;
    public static int processTime = 20_000;

    public int heat;
    public static int maxHeat = 100_000;
    public static double diffusion = 0.25D;

    public FluidTankNTM[] tanks;

    public TileEntityMachineCoker() {
        super(2, true, false);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.HEAVYOIL, 16_000);
        tanks[1] = new FluidTankNTM(Fluids.OIL_COKER, 8_000);
    }

    @Override
    public String getDefaultName() {
        return "container.machineCoker";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.tryPullHeat();
            this.tanks[0].setType(0, inventory);

            if(world.getTotalWorldTime() % 20 == 0) {
                for(DirPos pos : getConPos()) {
                    this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            }

            this.wasOn = false;

            if(canProcess()) {
                int burn = heat / 100;

                if(burn > 0) {
                    this.wasOn = true;
                    this.progress += burn;
                    this.heat -= burn;

                    if(progress >= processTime) {
                        this.markDirty();
                        progress -= this.processTime;

                        Tuple.Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());
                        int fillReq = recipe.getX();
                        ItemStack output = recipe.getY();
                        FluidStack byproduct = recipe.getZ();

                        if(output != null) {
                            if(inventory.getStackInSlot(1).isEmpty()) {
                                inventory.setStackInSlot(1, output.copy());
                            } else {
                                inventory.getStackInSlot(1).grow(output.getCount());
                            }
                        }

                        if(byproduct != null) {
                            tanks[1].setFill(tanks[1].getFill() + byproduct.fill);
                        }

                        tanks[0].setFill(tanks[0].getFill() - fillReq);
                    }
                }

                if(wasOn && world.getTotalWorldTime() % 5 == 0) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
            }

            for(DirPos pos : getConPos()) {
                if(this.tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            networkPackNT(25);
        } else {

            if(this.wasOn) {

                if(world.getTotalWorldTime() % 2 == 0) {
                    NBTTagCompound fx = new NBTTagCompound();
                    fx.setString("type", "tower");
                    fx.setFloat("lift", 10F);
                    fx.setFloat("base", 0.75F);
                    fx.setFloat("max", 3F);
                    fx.setInteger("life", 200 + world.rand.nextInt(50));
                    fx.setInteger("color",0x404040);
                    fx.setDouble("posX", pos.getX() + 0.5);
                    fx.setDouble("posY", pos.getY() + 22);
                    fx.setDouble("posZ", pos.getZ() + 0.5);
                    MainRegistry.proxy.effectNT(fx);
                }
            }
        }
    }

    public DirPos[] getConPos() {

        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    public boolean canProcess() {
        Tuple.Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());

        if(recipe == null) return false;

        int fillReq = recipe.getX();
        ItemStack output = recipe.getY();
        FluidStack byproduct = recipe.getZ();

        if(byproduct != null) tanks[1].setTankType(byproduct.type);

        if(tanks[0].getFill() < fillReq) return false;
        if(byproduct != null && byproduct.fill + tanks[1].getFill() > tanks[1].getMaxFill()) return false;

        if(output != null && !inventory.getStackInSlot(1).isEmpty()) {
            if(output.getItem() != inventory.getStackInSlot(1).getItem()) return false;
            if(output.getItemDamage() != inventory.getStackInSlot(1).getItemDamage()) return false;
            return output.getCount() + inventory.getStackInSlot(1).getCount() <= output.getMaxStackSize();
        }

        return true;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.wasOn);
        buf.writeInt(this.heat);
        buf.writeInt(this.progress);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.wasOn = buf.readBoolean();
        this.heat = buf.readInt();
        this.progress = buf.readInt();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    protected void tryPullHeat() {

        if(this.heat >= this.maxHeat) return;

        TileEntity con = world.getTileEntity(pos.add(0, -1, 0));

        if(con instanceof IHeatSource) {
            IHeatSource source = (IHeatSource) con;
            int diff = source.getHeatStored() - this.heat;

            if(diff == 0) {
                return;
            }

            if(diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                source.useUpHeat(diff);
                this.heat += diff;
                if(this.heat > this.maxHeat)
                    this.heat = this.maxHeat;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] { 1 };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
        this.progress = nbt.getInteger("prog");
        this.heat = nbt.getInteger("heat");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        nbt.setInteger("prog", progress);
        nbt.setInteger("heat", heat);
        return super.writeToNBT(nbt);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] { tanks[1] };
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] { tanks[0] };
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 23,
                    pos.getZ() + 3
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineCoker(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineCoker(player.inventory, this);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 1024.0D;
    }
}
