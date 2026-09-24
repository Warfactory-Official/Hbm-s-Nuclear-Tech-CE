package com.hbm.tileentity.machine.albion;

import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPARFC;
import com.hbm.inventory.gui.GUIPARFC;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.albion.TileEntityPASource.PAState;
import com.hbm.tileentity.machine.albion.TileEntityPASource.Particle;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.api.redstoneoverradio.IRORValueProvider;

@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityPARFC extends TileEntityCooledBase implements IGUIProvider, IParticleUser, IRORValueProvider, SimpleComponent, CompatHandler.OCComponent {

    public static final long usage = 250_000;
    public static final int momentumGain = 100;
    public static final int defocusGain = 100;
    AxisAlignedBB bb = null;

    public TileEntityPARFC() {
        super(1);
    }

    @Override
    public long getMaxPower() {
        return 1_000_000;
    }

    @Override
    public String getDefaultName() {
        return "container.paRFC";
    }

    @Override
    public boolean canParticleEnter(Particle particle, ForgeDirection dir, BlockPos pos) {
        ForgeDirection rfcDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
        BlockPos input = getPos().offset(rfcDir.toEnumFacing(), -4);
        return input.equals(pos) && rfcDir == dir;
    }

    @Override
    public void onEnter(Particle particle, ForgeDirection dir) {

        if (!isCool()) particle.crash(PAState.CRASH_NOCOOL);
        if (this.power < usage) particle.crash(PAState.CRASH_NOPOWER);

        if (particle.invalid) return;

        particle.addDistance(9);
        particle.momentum += momentumGain;
        particle.defocus(defocusGain);
        this.power -= usage;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        ForgeDirection beamlineDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
        return getPos().offset(beamlineDir.toEnumFacing(), 5);
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());
        }

        super.update();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(getPos().add(-4, -1, -4), getPos().add(5, 2, 5));
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
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.UP);
        BlockPos pos = getPos();
        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 3, pos.getY() + 2, pos.getZ() + dir.offsetZ * 3, Library.POS_Y),
                new DirPos(pos.getX() - dir.offsetX * 3, pos.getY() + 2, pos.getZ() - dir.offsetZ * 3, Library.POS_Y),
                new DirPos(pos.getX(), pos.getY() + 2, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX() + dir.offsetX * 3, pos.getY() - 2, pos.getZ() + dir.offsetZ * 3, Library.NEG_Y),
                new DirPos(pos.getX() - dir.offsetX * 3, pos.getY() - 2, pos.getZ() - dir.offsetZ * 3, Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY() - 2, pos.getZ(), Library.NEG_Y)
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPARFC(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPARFC(player.inventory, this);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "temperature",
                PREFIX_VALUE + "pfmcold",
                PREFIX_VALUE + "pfm"
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
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_pa_rfc";
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
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[] {
            getPower(), getMaxPower(),
            coolantTanks[0].getFill(), coolantTanks[0].getMaxFill(),
            coolantTanks[1].getFill(), coolantTanks[1].getMaxFill(),
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
            "getEnergyInfo",
            "getCoolant",
            "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "getEnergyInfo": return getEnergyInfo(context, args);
            case "getCoolant": return getCoolant(context, args);
            case "getInfo": return getInfo(context, args);
        }
        throw new NoSuchMethodException();
    }
}
