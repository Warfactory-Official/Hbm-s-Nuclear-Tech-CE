package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCraneRouter;
import com.hbm.inventory.gui.GUICraneRouter;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityCraneRouter extends TileEntityMachineBase implements IGUIProvider, IControlReceiverFilter, ITickable {
    public ModulePatternMatcher[] patterns = new ModulePatternMatcher[6]; //why did i make six matchers???
    public int[] modes = new int[6];
    public static final int MODE_NONE = 0;
    public static final int MODE_WHITELIST = 1;
    public static final int MODE_BLACKLIST = 2;
    public static final int MODE_WILDCARD = 3;

    public TileEntityCraneRouter() {
        super(5 * 6);

        for(int i = 0; i < patterns.length; i++) {
            patterns[i] = new ModulePatternMatcher(5);
        }
    }

    @Override
    public String getDefaultName() {
        return "container.craneRouter";
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            networkPackNT(15);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        for (ModulePatternMatcher pattern : patterns) {
            pattern.serialize(buf);
        }

        BufferUtil.writeIntArray(buf, this.modes);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        for (ModulePatternMatcher pattern : patterns) {
            pattern.deserialize(buf);
        }

        this.modes = BufferUtil.readIntArray(buf);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCraneRouter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneRouter(player.inventory, this);
    }

    public void nextMode(int index) {

        int matcher = index / 5;
        int mIndex = index % 5;

        this.patterns[matcher].nextMode(world, inventory.getStackInSlot(index), mIndex);
    }

    public void initPattern(ItemStack stack, int index) {

        int matcher = index / 5;
        int mIndex = index % 5;

        this.patterns[matcher].initPatternSmart(world, stack, mIndex);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        for(int i = 0; i < patterns.length; i++) {
            NBTTagCompound compound = nbt.getCompoundTag("pattern" + i);
            patterns[i].readFromNBT(compound);
        }
        this.modes = nbt.getIntArray("modes");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        for(int i = 0; i < patterns.length; i++) {
            NBTTagCompound compound = new NBTTagCompound();
            patterns[i].writeToNBT(compound);
            nbt.setTag("pattern" + i, compound);
        }
        nbt.setIntArray("modes", this.modes);
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        return new Vec3d(xCoord - player.posX, yCoord - player.posY, zCoord - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("toggle")) {
            int i = data.getInteger("toggle");
            modes[i]++;
            if(modes[i] > 3)
                modes[i] = 0;
        }
        if(data.hasKey("slot")) {
            setFilterContents(data);
        }
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{0, 30};
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound nbt = IControlReceiverFilter.super.getSettings(world, x, y, z);
        nbt.setIntArray("modes", modes);
        return nbt;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        IControlReceiverFilter.super.pasteSettings(nbt, index, world, player, x, y, z);
        if(nbt.hasKey("modes")) {
            modes = nbt.getIntArray("modes");
        }
    }

    @Override
    public String[] infoForDisplay(World world, int x, int y, int z) {
        String[] options = new String[patterns.length];
        for(int i = 0; i < options.length; i++) {
            options[i] = "copytool.pattern" + i;
        }
        return options;
    }
}
