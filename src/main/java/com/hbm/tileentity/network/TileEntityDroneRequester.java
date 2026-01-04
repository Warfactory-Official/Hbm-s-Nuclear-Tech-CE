package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerDroneRequester;
import com.hbm.inventory.gui.GUIDroneRequester;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityDroneRequester extends TileEntityRequestNetworkContainer implements IGUIProvider, IControlReceiverFilter {

    public ModulePatternMatcher matcher;

    public TileEntityDroneRequester() {
        super(18);
        this.matcher = new ModulePatternMatcher(9);
    }

    @Override
    public String getName() {
        return "container.droneRequester";
    }

    @Override
    public void update() {
        super.update();

        if(!world.isRemote) {
            networkPackNT(15);
        }
    }

    @Override public void serialize(ByteBuf buf) {
        this.matcher.serialize(buf);
    }

    @Override public void deserialize(ByteBuf buf) {
        this.matcher.deserialize(buf);
    }

    @Override
    public void nextMode(int i) {
        this.matcher.nextMode(world, inventory.getStackInSlot(i), i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 };
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, EnumFacing j) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.matcher.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.matcher.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDroneRequester(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDroneRequester(player.inventory, this);
    }

    @Override
    public RequestNetwork.PathNode createNode(BlockPos pos) {
        List<RecipesCommon.AStack> request = new ArrayList<>();
        for(int i = 0; i < 9; i++) {
            ItemStack filter = inventory.getStackInSlot(i);
            ItemStack stock = inventory.getStackInSlot(i + 9);
            if(filter.isEmpty()) continue;
            String mode = this.matcher.modes[i];
            RecipesCommon.AStack aStack = null;

            if(ModulePatternMatcher.MODE_EXACT.equals(mode)) {
                aStack = new RecipesCommon.ComparableStack(filter).makeSingular();
            } else if(ModulePatternMatcher.MODE_WILDCARD.equals(mode)) {
                aStack = new RecipesCommon.ComparableStack(filter.getItem(), 1, OreDictionary.WILDCARD_VALUE);
            } else if(mode != null) {
                aStack = new RecipesCommon.OreDictStack(mode);
            }

            if(aStack == null) continue;

            if(stock.isEmpty() || !this.matcher.isValidForFilter(filter, i, stock)) request.add(aStack);
        }
        return new RequestNetwork.RequestNode(pos, this.reachableNodes, request);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{0,9};
    }
}
