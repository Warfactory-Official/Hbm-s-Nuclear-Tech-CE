package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ReactorResearch;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineReactorBreeding;
import com.hbm.inventory.gui.GUIMachineReactorBreeding;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineReactorBreeding extends TileEntityMachineBase implements IBufPacketReceiver, IGUIProvider, ITickable {

    public int flux;
    public float progress;

    private static final int[] slots_io = new int[] { 0, 1 };

    public TileEntityMachineReactorBreeding() {
        super(2);
    }

    @Override
    public String getDefaultName() {
        return "container.reactorBreeding";
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            this.flux = 0;
            getInteractions();

            if(canProcess()) {

                progress += 0.0025F * ((float) this.flux / BreederRecipes.getOutput(inventory.getStackInSlot(0)).flux);

                if(this.progress >= 1.0F) {
                    this.progress = 0F;
                    this.processItem();
                    this.markDirty();
                }
            } else {
                progress = 0.0F;
            }

            networkPackNT(20);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(flux);
        buf.writeFloat(progress);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        flux = buf.readInt();
        progress = buf.readFloat();
    }

    public void getInteractions() {

        for(byte d = 2; d < 6; d++) {
            ForgeDirection dir = ForgeDirection.getOrientation(d);
            Block b = world.getBlockState(pos.add(dir.offsetX, 0, dir.offsetZ)).getBlock();

            if(b == ModBlocks.reactor_research) {

                int[] posC = ((ReactorResearch) ModBlocks.reactor_research).findCore(world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ);

                if(posC != null) {
                    TileEntity tile = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

                    if(tile instanceof TileEntityReactorResearch) {
                        TileEntityReactorResearch reactor = (TileEntityReactorResearch) tile;
                        this.flux += reactor.totalFlux;
                    }
                }
            }
        }
    }

    public boolean canProcess() {

        if(inventory.getStackInSlot(0).isEmpty())
            return false;

        BreederRecipes.BreederRecipe recipe = BreederRecipes.getOutput(inventory.getStackInSlot(0));

        if(recipe == null)
            return false;

        if(this.flux < recipe.flux)
            return false;

        if(inventory.getStackInSlot(1).isEmpty())
            return true;

        if(!inventory.getStackInSlot(1).isItemEqual(recipe.output))
            return false;

        if(inventory.getStackInSlot(1).getCount() < inventory.getStackInSlot(1).getMaxStackSize())
            return true;
        else
            return false;
    }

    private void processItem() {

        if(canProcess()) {

            BreederRecipes.BreederRecipe rec = BreederRecipes.getOutput(inventory.getStackInSlot(0));

            if(rec == null)
                return;

            ItemStack itemStack = rec.output;

            if(inventory.getStackInSlot(1).isEmpty()) {
                inventory.setStackInSlot(1, itemStack.copy());
            } else if(inventory.getStackInSlot(1).isItemEqual(itemStack)) {
                inventory.getStackInSlot(1).grow(itemStack.getCount());
            }

            inventory.getStackInSlot(0).shrink(1);

            if(inventory.getStackInSlot(0).getCount() <= 0) {
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }



    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slots_io;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 0;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 1;
    }

    public int getProgressScaled(int i) {
        return (int) (this.progress * i);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        flux = nbt.getInteger("flux");
        progress = nbt.getFloat("progress");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("flux", flux);
        nbt.setFloat("progress", progress);
        return super.writeToNBT(nbt);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    pos.getX() + 1,
                    pos.getY() + 3,
                    pos.getZ() + 1
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
        return new ContainerMachineReactorBreeding(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineReactorBreeding(player.inventory, this);
    }
}
