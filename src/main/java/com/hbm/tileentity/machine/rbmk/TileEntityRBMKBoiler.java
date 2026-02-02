package com.hbm.tileentity.machine.rbmk;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerRBMKBoiler;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.DataValueString;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIRBMKBoiler;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.uninos.UniNodespace;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityRBMKBoiler extends TileEntityRBMKSlottedBase implements IControlReceiver, IFluidStandardTransceiver, SimpleComponent, IGUIProvider {

    public FluidTankNTM feed;
    public FluidTankNTM steam;
    protected int consumption;
    protected int output;

    public TileEntityRBMKBoiler() {
        super(0);

        feed = new FluidTankNTM(Fluids.WATER, 10000);
        steam = new FluidTankNTM(Fluids.STEAM, 1000000);
    }

    public void getDiagData(NBTTagCompound nbt) {
        this.writeToNBT(nbt);
        nbt.removeTag("jumpheight");
        nbt.setInteger("water", feed.getFill());
        nbt.setInteger("steam", steam.getFill());
    }

    @Override
    public String getName() {
        return "container.rbmkBoiler";
    }

    @Override
    public void update() {
        if (!world.isRemote) {

            this.consumption = 0;
            this.output = 0;

            double heatCap = getHeatFromSteam(steam.getTankType());
            double heatProvided = this.heat - heatCap;

            if (heatProvided > 0) {
                double HEAT_PER_MB_WATER = RBMKDials.getBoilerHeatConsumption(world);
                double steamFactor = getFactorFromSteam(steam.getTankType());
                int waterUsed;
                int steamProduced;

                if (steam.getTankType() == Fluids.ULTRAHOTSTEAM) {
                    steamProduced = (int) Math.floor((heatProvided / HEAT_PER_MB_WATER) * 100D / steamFactor);
                    waterUsed = (int) Math.floor(steamProduced / 100D * steamFactor);

                    if (feed.getFill() < waterUsed) {
                        steamProduced = (int) Math.floor(feed.getFill() * 100D / steamFactor);
                        waterUsed = (int) Math.floor(steamProduced / 100D * steamFactor);
                    }
                } else {
                    waterUsed = (int) Math.floor(heatProvided / HEAT_PER_MB_WATER);
                    waterUsed = Math.min(waterUsed, feed.getFill());
                    steamProduced = (int) Math.floor((waterUsed * 100D) / steamFactor);
                }

                this.consumption = waterUsed;
                this.output = steamProduced;

                feed.setFill(feed.getFill() - waterUsed);
                steam.setFill(steam.getFill() + steamProduced);

                if (steam.getFill() > steam.getMaxFill())
                    steam.setFill(steam.getMaxFill());

                this.heat -= waterUsed * HEAT_PER_MB_WATER;
            }

            this.trySubscribe(feed.getTankType(), world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);
            for (DirPos pos : getOutputPos()) {
                if (this.steam.getFill() > 0)
                    this.sendFluid(steam, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }
        }

        super.update();
    }

    public static double getHeatFromSteam(FluidType type) {
        if (type == Fluids.STEAM) return 100D;
        if (type == Fluids.HOTSTEAM) return 300D;
        if (type == Fluids.SUPERHOTSTEAM) return 450D;
        if (type == Fluids.ULTRAHOTSTEAM) return 600D;
        return 0D;
    }

    public static double getFactorFromSteam(FluidType type) {
        if (type == Fluids.STEAM) return 1D;
        if (type == Fluids.HOTSTEAM) return 10D;
        if (type == Fluids.SUPERHOTSTEAM) return 100D;
        if (type == Fluids.ULTRAHOTSTEAM) return 1000D;
        return 0D;
    }

    protected DirPos[] getOutputPos() {

        if (world.getBlockState(pos.down(1)).getBlock() == ModBlocks.rbmk_loader) {
            return new DirPos[]{
                    new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
                    new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
                    new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
                    new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
                    new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
                    new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
            };
        } else if (world.getBlockState(pos.down(2)).getBlock() == ModBlocks.rbmk_loader) {
            return new DirPos[]{
                    new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y),
                    new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
                    new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
                    new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
                    new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
                    new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
            };
        } else {
            return new DirPos[]{
                    new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), Library.POS_Y)
            };
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        feed.readFromNBT(nbt, "feed");
        steam.readFromNBT(nbt, "steam");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        feed.writeToNBT(nbt, "feed");
        steam.writeToNBT(nbt, "steam");
        return nbt;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        steam.serialize(buf);
        feed.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.steam.deserialize(buf);
        this.feed.deserialize(buf);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("compression")) {
            this.cyceCompressor();
            this.markDirty();
        }
    }

    // mlbv: why is it named cyceCompressor?
    public void cyceCompressor() {

        FluidType type = steam.getTankType();
        if(type == Fluids.STEAM) {			steam.setTankType(Fluids.HOTSTEAM);			steam.setFill(steam.getFill() / 10); }
        if(type == Fluids.HOTSTEAM) {		steam.setTankType(Fluids.SUPERHOTSTEAM);	steam.setFill(steam.getFill() / 10); }
        if(type == Fluids.SUPERHOTSTEAM) {	steam.setTankType(Fluids.ULTRAHOTSTEAM);	steam.setFill(steam.getFill() / 10); }
        if(type == Fluids.ULTRAHOTSTEAM) {	steam.setTankType(Fluids.STEAM);			steam.setFill(Math.min(steam.getFill() * 1000, steam.getMaxFill())); }

        this.markDirty();
    }

    @Override
    public void onMelt(int reduce) {

        int count = 1 + world.rand.nextInt(2);

        for (int i = 0; i < count; i++) {
            spawnDebris(DebrisType.BLANK);
        }

        if (RBMKDials.getOverpressure(world)) {
            for (DirPos pos : getOutputPos()) {
                FluidNode node = (FluidNode) UniNodespace.getNode(world, pos.getPos(), steam.getTankType().getNetworkProvider());
                if (node.net != null && node.hasValidNet()) {
                    this.pipes.add(node.net);
                }
            }
        }

        super.onMelt(reduce);
    }

    @Override
    public ColumnType getConsoleType() {
        return ColumnType.BOILER;
    }

    @Override
    public RBMKColumn getConsoleData() {
        RBMKColumn.BoilerColumn data = (RBMKColumn.BoilerColumn) super.getConsoleData();
        data.water = this.feed.getFill();
        data.maxWater = this.feed.getMaxFill();
        data.steam = this.steam.getFill();
        data.maxSteam = this.steam.getMaxFill();
        data.steamType = (short) this.steam.getTankType().getID();
        return data;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{feed, steam};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{steam};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{feed};
    }

    // control panel
    @Override
    public Map<String, DataValue> getQueryData() {
        Map<String, DataValue> data = super.getQueryData();

        data.put("feed", new DataValueFloat((float) feed.getFill()));
        data.put("steam", new DataValueFloat((float) steam.getFill()));
        data.put("steamType", new DataValueString(steam.getTankType().getName()));

        return data;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "rbmk_boiler";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getHeat(Context context, Arguments args) {
        return new Object[] {heat};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSteam(Context context, Arguments args) {
        return new Object[] {steam.getFill()};
    }
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSteamMax(Context context, Arguments args) {
        return new Object[] {steam.getMaxFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getWater(Context context, Arguments args) {
        return new Object[] {feed.getFill()};
    }
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getWaterMax(Context context, Arguments args) {
        return new Object[] {feed.getMaxFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCoordinates(Context context, Arguments args) {
        return new Object[] {pos.getX(), pos.getY(), pos.getZ()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        int type_1 = (int) CompatHandler.steamTypeToInt(steam.getTankType())[0];
        return new Object[] {heat, steam.getFill(), steam.getMaxFill(), feed.getFill(), feed.getMaxFill(), type_1, pos.getX(), pos.getY(), pos.getZ()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSteamType(Context context, Arguments args) {
        return CompatHandler.steamTypeToInt(steam.getTankType());
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setSteamType(Context context, Arguments args) {
        int type = args.checkInteger(0);
        steam.setTankType(CompatHandler.intToSteamType(type));
        return new Object[] {true};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRBMKBoiler(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRBMKBoiler(player.inventory, this);
    }
}
