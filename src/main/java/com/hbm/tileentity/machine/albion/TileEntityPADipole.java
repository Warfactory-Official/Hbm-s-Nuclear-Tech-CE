package com.hbm.tileentity.machine.albion;

import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerPADipole;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.gui.GUIPADipole;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPACoil;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.albion.TileEntityPASource.PAState;
import com.hbm.tileentity.machine.albion.TileEntityPASource.Particle;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.api.redstoneoverradio.IRORInteractive;

@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityPADipole extends TileEntityCooledBase implements IGUIProvider, IControlReceiver, IParticleUser, IRORValueProvider, IRORInteractive, SimpleComponent, CompatHandler.OCComponent {

    public static final long usage = 100_000;
    public int dirLower;
    public int dirUpper;
    public int dirRedstone;
    public int threshold;
    AxisAlignedBB bb = null;

    public TileEntityPADipole() {
        super(2);
    }

    public static ForgeDirection ditToForgeDir(int dir) {
        if (dir == 1) return ForgeDirection.EAST;
        if (dir == 2) return ForgeDirection.SOUTH;
        if (dir == 3) return ForgeDirection.WEST;
        return ForgeDirection.NORTH;
    }

    @Override
    public long getMaxPower() {
        return 2_500_000;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;
    }

    @Override
    public String getDefaultName() {
        return "container.paDipole";
    }

    @Override
    public boolean canParticleEnter(Particle particle, ForgeDirection dir, BlockPos pos) {
        return getPos().getY() == pos.getY() && (getPos().getX() == pos.getX() || getPos().getZ() == pos.getZ());
    }

    @Override
    public void onEnter(Particle particle, ForgeDirection dir) {
        ItemPACoil.EnumCoilType type = null;
        boolean isInline = dir.equals(getExitDir(particle));

        int mult = 1;
        if (inventory.getStackInSlot(1).getItem() == ModItems.pa_coil) {
            type = EnumUtil.grabEnumSafely(ItemPACoil.EnumCoilType.VALUES, inventory.getStackInSlot(1).getItemDamage());

            if (type.diMin > particle.momentum) mult *= 10;
            if (type.diDistMin > particle.distanceTraveled) mult *= 10;
            if (isInline) mult = 1;
        }

        if (!isCool()) particle.crash(PAState.CRASH_NOCOOL);
        if (this.power < usage * mult) particle.crash(PAState.CRASH_NOPOWER);
        if (type == null) particle.crash(PAState.CRASH_NOCOIL);
        if (type != null && type.diMax < particle.momentum && !isInline) particle.crash(PAState.CRASH_OVERSPEED);

        if (particle.invalid) return;

        if (isInline) {
            particle.addDistance(3);
        } else {
            particle.resetDistance();
        }

        this.power -= usage * mult;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        particle.dir = getExitDir(particle);
        return getPos().offset(particle.dir.toEnumFacing(), 2);
    }

    public ForgeDirection getExitDir(Particle particle) {
        int dit = particle.momentum < this.threshold
                ? dirLower : checkRedstone()
                ? dirRedstone : dirUpper;
        return ditToForgeDir(dit);
    }

    public boolean checkRedstone() {
        for (DirPos pos : getConPos()) {
            if (world.isBlockPowered(pos.getPos())) return true;
        }
        return false;
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());
        }

        super.update();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(dirLower);
        buf.writeInt(dirUpper);
        buf.writeInt(dirRedstone);
        buf.writeInt(threshold);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        dirLower = buf.readInt();
        dirUpper = buf.readInt();
        dirRedstone = buf.readInt();
        threshold = buf.readInt();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        dirLower = nbt.getInteger("dirLower");
        dirUpper = nbt.getInteger("dirUpper");
        dirRedstone = nbt.getInteger("dirRedstone");
        threshold = nbt.getInteger("threshold");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("dirLower", dirLower);
        nbt.setInteger("dirUpper", dirUpper);
        nbt.setInteger("dirRedstone", dirRedstone);
        nbt.setInteger("threshold", threshold);
        return nbt;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            int xCoord = this.getPos().getX();
            int yCoord = this.getPos().getY();
            int zCoord = this.getPos().getZ();
            bb = new AxisAlignedBB(
                    xCoord - 1,
                    yCoord - 1,
                    zCoord - 1,
                    xCoord + 2,
                    yCoord + 2,
                    zCoord + 2
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
    public DirPos[] getConPos() {
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        return new DirPos[]{
                new DirPos(xCoord + 1, yCoord + 2, zCoord, Library.POS_Y),
                new DirPos(xCoord - 1, yCoord + 2, zCoord, Library.POS_Y),
                new DirPos(xCoord, yCoord + 2, zCoord + 1, Library.POS_Y),
                new DirPos(xCoord, yCoord + 2, zCoord - 1, Library.POS_Y),
                new DirPos(xCoord + 1, yCoord - 2, zCoord, Library.NEG_Y),
                new DirPos(xCoord - 1, yCoord - 2, zCoord, Library.NEG_Y),
                new DirPos(xCoord, yCoord - 2, zCoord + 1, Library.NEG_Y),
                new DirPos(xCoord, yCoord - 2, zCoord - 1, Library.NEG_Y)
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPADipole(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPADipole(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
        if (data.hasKey("lower")) this.dirLower++;
        if (data.hasKey("upper")) this.dirUpper++;
        if (data.hasKey("redstone")) this.dirRedstone++;
        if (data.hasKey("threshold")) this.threshold = data.getInteger("threshold");

        if (this.dirLower > 3) this.dirLower -= 4;
        if (this.dirUpper > 3) this.dirUpper -= 4;
        if (this.dirRedstone > 3) this.dirRedstone -= 4;

        this.threshold = MathHelper.clamp(threshold, 0, 999_999_999);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "temperature",
                PREFIX_VALUE + "pfmcold",
                PREFIX_VALUE + "pfm",
                PREFIX_FUNCTION + "setthreshold" + NAME_SEPARATOR + "threshold"
        };
    }

    @Override
    public String provideRORValue(String name) {
        if((PREFIX_VALUE + "temperature").equals(name)) return "" + (int) this.temperature;
        if((PREFIX_VALUE + "pfmcold").equals(name)) return "" + coolantTanks[0].getFill();
        if((PREFIX_VALUE + "pfm").equals(name)) return "" + coolantTanks[1].getFill();
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if((PREFIX_FUNCTION + "setthreshold").equals(name) && params.length > 0) {
            this.threshold = IRORInteractive.parseInt(params[0], 0, 999_999_999);
            this.markDirty();
        }
        return null;
    }

    public static String dirToName(int dir) {
        if(dir == 1) return "east";
        if(dir == 2) return "south";
        if(dir == 3) return "west";
        return "north";
    }

    public static int nameToDir(String name) {
        if(name.equals("north")) return 0;
        if(name.equals("east")) return 1;
        if(name.equals("south")) return 2;
        if(name.equals("west")) return 3;
        return -1;
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_pa_dipole";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[] {getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCoolant(Context context, Arguments args) {
        return new Object[] {
            coolantTanks[0].getFill(), coolantTanks[0].getMaxFill(),
            coolantTanks[1].getFill(), coolantTanks[1].getMaxFill(),
        };
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getDirLower(Context context, Arguments args) {
        return new Object[] {dirToName(dirLower)};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getDirUpper(Context context, Arguments args) {
        return new Object[] {dirToName(dirUpper)};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getDirRedstone(Context context, Arguments args) {
        return new Object[] {dirToName(dirRedstone)};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getThreshold(Context context, Arguments args) {
        return new Object[] {threshold};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setDirLower(Context context, Arguments args) {
        int dir = nameToDir(args.checkString(0));
        if(dir >= 0) dirLower = dir;
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setDirUpper(Context context, Arguments args) {
        int dir = nameToDir(args.checkString(0));
        if(dir >= 0) dirUpper = dir;
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setDirRedstone(Context context, Arguments args) {
        int dir = nameToDir(args.checkString(0));
        if(dir >= 0) dirRedstone = dir;
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setThreshold(Context context, Arguments args) {
        threshold = net.minecraft.util.math.MathHelper.clamp(args.checkInteger(0), 0, 999_999_999);
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[] {
            getPower(), getMaxPower(),
            coolantTanks[0].getFill(), coolantTanks[0].getMaxFill(),
            coolantTanks[1].getFill(), coolantTanks[1].getMaxFill(),
            dirToName(dirLower), dirToName(dirUpper), dirToName(dirRedstone), threshold
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
            "getEnergyInfo", "getCoolant", "getDirLower", "getDirUpper", "getDirRedstone",
            "getThreshold", "setDirLower", "setDirUpper", "setDirRedstone", "setThreshold", "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "getEnergyInfo": return getEnergyInfo(context, args);
            case "getCoolant": return getCoolant(context, args);
            case "getDirLower": return getDirLower(context, args);
            case "getDirUpper": return getDirUpper(context, args);
            case "getDirRedstone": return getDirRedstone(context, args);
            case "getThreshold": return getThreshold(context, args);
            case "setDirLower": return setDirLower(context, args);
            case "setDirUpper": return setDirUpper(context, args);
            case "setDirRedstone": return setDirRedstone(context, args);
            case "setThreshold": return setThreshold(context, args);
            case "getInfo": return getInfo(context, args);
        }
        throw new NoSuchMethodException();
    }
}
