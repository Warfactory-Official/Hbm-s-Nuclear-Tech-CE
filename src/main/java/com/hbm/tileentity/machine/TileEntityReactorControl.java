package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ReactorResearch;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerReactorControl;
import com.hbm.inventory.gui.GUIReactorControl;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityReactorControl extends TileEntityMachineBase implements IControlReceiver, IGUIProvider, ITickable, SimpleComponent, CompatHandler.OCComponent {

    public TileEntityReactorControl() {
        super(1);
    }

    @Override
    public String getDefaultName() {
        return "container.reactorControl";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        isLinked = nbt.getBoolean("isLinked");
        levelLower = nbt.getDouble("levelLower");
        levelUpper = nbt.getDouble("levelUpper");
        heatLower = nbt.getDouble("heatLower");
        heatUpper = nbt.getDouble("heatUpper");
        function = RodFunction.VALUES[nbt.getInteger("function")];

        if (nbt.hasKey("inventory", 10)) {
            inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
        } else {
            inventory.deserializeNBT(new NBTTagCompound());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("isLinked", isLinked);
        nbt.setDouble("levelLower", levelLower);
        nbt.setDouble("levelUpper", levelUpper);
        nbt.setDouble("heatLower", heatLower);
        nbt.setDouble("heatUpper", heatUpper);
        nbt.setInteger("function", function.ordinal());

        nbt.setTag("inventory", inventory.serializeNBT());

        return super.writeToNBT(nbt);
    }

    public TileEntityReactorResearch reactor;

    public boolean isLinked;

    public int flux;
    public double level;
    public int heat;

    public double levelLower;
    public double levelUpper;
    public double heatLower;
    public double heatUpper;
    public RodFunction function = RodFunction.LINEAR;

    @Override
    public void update() {

        if(!world.isRemote) {

            isLinked = establishLink();

            if(isLinked) {

                double fauxLevel = 0;

                double lowerBound = Math.min(this.heatLower, this.heatUpper);
                double upperBound = Math.max(this.heatLower, this.heatUpper);

                if(this.heat < lowerBound) {
                    fauxLevel = this.levelLower;

                } else if(this.heat > upperBound) {
                    fauxLevel = this.levelUpper;

                } else {
                    fauxLevel = getTargetLevel(this.function, this.heat);
                }

                double level = MathHelper.clamp((fauxLevel * 0.01D), 0D, 1D);

                if(level != this.level) {
                    reactor.setTarget(level);
                }
            }

            this.networkPackNT(150);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(heat);
        buf.writeDouble(level);
        buf.writeInt(flux);
        buf.writeBoolean(isLinked);
        buf.writeDouble(levelLower);
        buf.writeDouble(levelUpper);
        buf.writeDouble(heatLower);
        buf.writeDouble(heatUpper);
        buf.writeByte(function.ordinal());
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.level = buf.readDouble();
        this.flux = buf.readInt();
        isLinked = buf.readBoolean();
        levelLower = buf.readDouble();
        levelUpper = buf.readDouble();
        heatLower = buf.readDouble();
        heatUpper = buf.readDouble();
        function = RodFunction.VALUES[buf.readByte()];
    }

    private boolean establishLink() {
        ItemStack stack = inventory.getStackInSlot(0);
        if(!stack.isEmpty() && stack.getItem() == ModItems.reactor_sensor && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            int xCoord = tag.getInteger("x");
            int yCoord = tag.getInteger("y");
            int zCoord = tag.getInteger("z");

            BlockPos sensorPos = new BlockPos(xCoord, yCoord, zCoord);
            Block b = world.getBlockState(sensorPos).getBlock();

            if(b == ModBlocks.reactor_research) {

                int[] pos = ((ReactorResearch) ModBlocks.reactor_research).findCore(world, xCoord, yCoord, zCoord);

                if(pos != null) {

                    TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

                    if(tile instanceof TileEntityReactorResearch) {
                        reactor = (TileEntityReactorResearch) tile;

                        this.flux = reactor.totalFlux;
                        this.level = reactor.level;
                        this.heat = reactor.heat;

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public double getTargetLevel(RodFunction function, int heat) {
        double fauxLevel = 0;

        switch(function) {
            case LINEAR:
                fauxLevel = (heat - this.heatLower) * ((this.levelUpper - this.levelLower) / (this.heatUpper - this.heatLower)) + this.levelLower;
                return fauxLevel;
            case LOG:
                fauxLevel = Math.pow((heat - this.heatUpper) / (this.heatLower - this.heatUpper), 2) * (this.levelLower - this.levelUpper) + this.levelUpper;
                return fauxLevel;
            case QUAD:
                fauxLevel = Math.pow((heat - this.heatLower) / (this.heatUpper - this.heatLower), 2) * (this.levelUpper - this.levelLower) + this.levelLower;
                return fauxLevel;
            default:
                return 0.0D;
        }
    }

    public int[] getDisplayData() {
        if(this.isLinked) {
            int[] data = new int[3];
            data[0] = (int) (this.level * 100);
            data[1] = this.flux;
            data[2] = (int) Math.round((this.heat) * 0.00002 * 980 + 20);
            return data;
        } else {
            return new int[] { 0, 0, 0 };
        }
    }

    @Override
    public void receiveControl(NBTTagCompound data) {

        if(data.hasKey("function")) {
            this.function = RodFunction.VALUES[data.getInteger("function")];
        } else {
            this.levelLower = data.getDouble("levelLower");
            this.levelUpper = data.getDouble("levelUpper");
            this.heatLower = data.getDouble("heatLower");
            this.heatUpper = data.getDouble("heatUpper");
        }

        this.markDirty();
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 400.0D;
    }

    public enum RodFunction {
        LINEAR,
        QUAD,
        LOG;

        public static final RodFunction[] VALUES = values();
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "reactor_control";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isLinked(Context context, Arguments args) {
        return new Object[] {isLinked};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getReactor(Context context, Arguments args) {
        return new Object[] {getDisplayData()};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setParams(Context context, Arguments args) { //i hate my life
        int newFunction = args.checkInteger(0);
        double newMaxHeat = args.checkDouble(1);
        double newMinHeat = args.checkDouble(2);
        double newMaxLevel = args.checkDouble(3)/100.0;
        double newMinLevel = args.checkDouble(4)/100.0;
        function = RodFunction.VALUES[MathHelper.clamp(newFunction, 0, 2)];
        heatUpper = MathHelper.clamp(newMaxHeat, 0, 9999);
        heatLower = MathHelper.clamp(newMinHeat, 0, 9999);
        levelUpper = MathHelper.clamp(newMaxLevel, 0, 1);
        levelLower = MathHelper.clamp(newMinLevel, 0, 1);
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getParams(Context context, Arguments args) {
        return new Object[] {function.ordinal(), heatUpper, heatLower, levelUpper, levelLower};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerReactorControl(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIReactorControl(player.inventory, this);
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @javax.annotation.Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing) {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }
}
