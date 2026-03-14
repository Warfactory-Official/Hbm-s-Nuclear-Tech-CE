package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ILaserable;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.gui.GUICrateTungsten;
import com.hbm.inventory.recipes.DFCRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCrucible;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@AutoRegister
public class TileEntityCrateTungsten extends TileEntityCrate implements IBufPacketReceiver, ITickable, ILaserable, IGUIProvider {
    private final Random rand = new Random();

    public int heatTimer = 0;
    public int age = 0;
    public long joules = 0;

    public TileEntityCrateTungsten() {
        super(27, "container.crateTungsten");
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (world.getTotalWorldTime() % 10 == 3 && needsUpdate){
                scheduleCheck();
                needsUpdate = false;
            }
            if (heatTimer > 0)
                heatTimer--;

            if (heatTimer > 0) {
                PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacket(pos.getX(), pos.getY(), pos.getZ(), 4),
						new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
            }
            age++;
            if (age > 20) {
                networkPackNT(150);
                age = 0;
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heatTimer);
        buf.writeLong(this.joules);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heatTimer = buf.readInt();
        this.joules = buf.readLong();
    }

    @Override
    public void addEnergy(World world, BlockPos pos, long energy, EnumFacing dir) {
        heatTimer = 5;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            if (stack.isEmpty())
                continue;

            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);

            long requiredEnergy = DFCRecipes.getRequiredFlux(stack);
            if (requiredEnergy > -1 && energy > requiredEnergy) {
                result = DFCRecipes.getOutput(stack);
            }

            if (stack.getItem() == ModItems.crucible && ItemCrucible.getCharges(stack) < 3 && energy > 10000000) {
                ItemCrucible.charge(stack);
            }

            if (!result.isEmpty()) {
                int size = stack.getCount();

                if (result.getCount() * size <= result.getMaxStackSize()) {
                    ItemStack newStack = result.copy();
                    newStack.setCount(result.getCount() * size);
                    inventory.setStackInSlot(i, newStack);
                }
            }
        }
        joules = energy;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("heatTimer"))
            this.heatTimer = compound.getInteger("heatTimer");
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("heatTimer", this.heatTimer);
        return super.writeToNBT(compound);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCrateTungsten(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICrateTungsten(player.inventory, this);
    }
}
