package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.api.tile.IHeatSource;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIFurnaceCombo;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.DirPos;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoRegister
public class TileEntityFurnaceCombination extends TileEntityMachinePolluting implements ITickable, IFluidStandardSender, IGUIProvider,
        IFluidCopiable, IConnectionAnchors {

    public static int processTime = 20_000;
    public static int maxHeat = 100_000;
    public static double diffusion = 0.25D;
    public boolean wasOn;
    public int progress;
    public int heat;
    public FluidTankNTM tank;
    private AxisAlignedBB bb = null;

    public TileEntityFurnaceCombination() {
        super(4, 50, true, false);
        this.tank = new FluidTankNTM(Fluids.NONE, 24_000).withOwner(this);
    }

    @Override
    public String getDefaultName() {
        return "container.furnaceCombination";
    }

    @Override
    public void update() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        if (!world.isRemote) {
            this.tryPullHeat();
            if (this.world.getTotalWorldTime() % 20 == 0) {
                for (int i = 2; i < 6; i++) {
                    ForgeDirection dir = ForgeDirection.getOrientation(i);
                    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

                    for (int y = yCoord; y <= yCoord + 1; y++) {
                        for (int j = -1; j <= 1; j++) {
                            if (tank.getFill() > 0)
                                this.sendFluid(tank, world, xCoord + dir.offsetX * 2 + rot.offsetX * j, y, zCoord + dir.offsetZ * 2 + rot.offsetZ * j,
                                        dir);
                            this.sendSmoke(xCoord + dir.offsetX * 2 + rot.offsetX * j, y, zCoord + dir.offsetZ * 2 + rot.offsetZ * j, dir);
                        }
                    }
                }

                for (int x = xCoord - 1; x <= xCoord + 1; x++) {
                    for (int z = zCoord - 1; z <= zCoord + 1; z++) {
                        if (tank.getFill() > 0) this.sendFluid(tank, world, x, yCoord + 2, z, ForgeDirection.UP);
                        this.sendSmoke(x, yCoord + 2, z, ForgeDirection.UP);
                    }
                }
            }

            this.wasOn = false;

            tank.unloadTank(2, 3, inventory);

            if (canSmelt()) {
                int burn = heat / 100;

                if (burn > 0) {
                    this.wasOn = true;
                    this.progress += burn;
                    this.heat -= burn;

                    if (progress >= processTime) {
                        this.world.markChunkDirty(this.pos, this);
                        progress -= processTime;

                        Tuple.Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(inventory.getStackInSlot(0));
                        ItemStack out = pair.getKey(); // guarded by canSmelt so it's NotNull
                        FluidStack fluid = pair.getValue();
                        inventory.insertItem(1, out.copy(), false);

                        if (fluid != null) {
                            if (tank.getTankType() != fluid.type) {
                                tank.setTankType(fluid.type);
                            }
                            tank.setFill(tank.getFill() + fluid.fill);
                        }

                        inventory.extractItem(0, 1, false);
                    }

                    List<Entity> entities = world.getEntitiesWithinAABB(Entity.class,
                            new AxisAlignedBB(xCoord - 0.5, yCoord + 2, zCoord - 0.5, xCoord + 1.5, yCoord + 4, zCoord + 1.5));

                    for (Entity e : entities) e.setFire(5);

                    if (world.getTotalWorldTime() % 10 == 0)
                        this.world.playSound(null, pos.up(), HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, 0.25F, 0.5F);
                    if (world.getTotalWorldTime() % 20 == 0) this.pollute(PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
                }
            } else {
                this.progress = 0;
            }

            this.networkPackNT(50);
        } else {

            if (this.wasOn && world.rand.nextInt(15) == 0) {
                world.spawnParticle(EnumParticleTypes.LAVA, xCoord + 0.5 + world.rand.nextGaussian() * 0.5, yCoord + 2,
                        zCoord + 0.5 + world.rand.nextGaussian() * 0.5, 0, 0, 0);
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(wasOn);
        buf.writeInt(heat);
        buf.writeInt(progress);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        wasOn = buf.readBoolean();
        heat = buf.readInt();
        progress = buf.readInt();
        tank.deserialize(buf);
    }

    private boolean canSmelt() {
        if (inventory.getStackInSlot(0).isEmpty()) return false;
        Tuple.Pair<@Nullable ItemStack, FluidStack> pair = CombinationRecipes.getOutput(inventory.getStackInSlot(0));

        if (pair == null) return false;

        ItemStack out = pair.getKey();
        FluidStack fluid = pair.getValue();
        if (out == null) return false;
        if (!inventory.insertItem(1, out.copy(), true).isEmpty()) return false;

        if (fluid != null) {
            if (tank.getTankType() != fluid.type && tank.getFill() > 0) return false;
            return tank.getTankType() != fluid.type || tank.getFill() + fluid.fill <= tank.getMaxFill();
        }

        return true;
    }

    protected void tryPullHeat() {

        if (this.heat >= maxHeat) return;

        TileEntity con = world.getTileEntity(pos.down());

        if (con instanceof IHeatSource source) {
            int diff = source.getHeatStored() - this.heat;

            if (diff == 0) {
                return;
            }

            if (diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                source.useUpHeat(diff);
                this.heat += diff;
                if (this.heat > maxHeat) this.heat = maxHeat;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{0, 1};
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 0 && CombinationRecipes.getOutput(itemStack) != null;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 1;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.readFromNBT(nbt, "tank");
        this.progress = nbt.getInteger("prog");
        this.heat = nbt.getInteger("heat");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.tank.writeToNBT(nbt, "tank");
        nbt.setInteger("prog", progress);
        nbt.setInteger("heat", heat);
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFurnaceCombo(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFurnaceCombo(player.inventory, this);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
            bb = new AxisAlignedBB(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 2.125, zCoord + 2);
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tank, smoke, smoke_leaded, smoke_poison};
    }

    @Override
    public DirPos[] getConPos() {
        int xCoord = pos.getX(), yCoord = pos.getY(), zCoord = pos.getZ();
        DirPos[] result = new DirPos[24 + 9];
        int idx = 0;
        for (int i = 2; i < 6; i++) {
            ForgeDirection dir = ForgeDirection.getOrientation(i);
            ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
            for (int y = yCoord; y <= yCoord + 1; y++) {
                for (int j = -1; j <= 1; j++) {
                    result[idx++] = new DirPos(xCoord + dir.offsetX * 2 + rot.offsetX * j, y, zCoord + dir.offsetZ * 2 + rot.offsetZ * j, dir);
                }
            }
        }
        for (int x = xCoord - 1; x <= xCoord + 1; x++) {
            for (int z = zCoord - 1; z <= zCoord + 1; z++) {
                result[idx++] = new DirPos(x, yCoord + 2, z, ForgeDirection.UP);
            }
        }
        return result;
    }
}
