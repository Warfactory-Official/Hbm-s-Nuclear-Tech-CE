package com.hbm.tileentity.machine.fusion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerFusionTorus;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIFusionTorus;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.machine.ModuleMachineFusion;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.albion.TileEntityCooledBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.util.BobMathUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AutoRegister
public class TileEntityFusionTorus extends TileEntityCooledBase implements ITickable, IGUIProvider, IControlReceiver {

    public boolean didProcess = false;

    public FluidTankNTM[] tanks;
    public ModuleMachineFusion fusionModule;

    protected KlystronNetwork.KlystronNode[] klystronNodes;
    protected PlasmaNetwork.PlasmaNode[] plasmaNodes;
    public boolean[] connections;

    public long klystronEnergy;
    private long klystronEnergySync;
    public long plasmaEnergy;
    public double fuelConsumption;

    public float magnet;
    public float prevMagnet;
    public float magnetSpeed;
    public static final float MAGNET_ACCELERATION = 0.25F;

    private AudioWrapper audio;
    public int timeOffset = -1;

    public TileEntityFusionTorus() {
        super(3);

        klystronNodes = new KlystronNetwork.KlystronNode[4];
        plasmaNodes = new PlasmaNetwork.PlasmaNode[4];
        connections = new boolean[4];

        this.tanks = new FluidTankNTM[4];

        this.tanks[0] = new FluidTankNTM(Fluids.NONE, 4_000);
        this.tanks[1] = new FluidTankNTM(Fluids.NONE, 4_000);
        this.tanks[2] = new FluidTankNTM(Fluids.NONE, 4_000);
        this.tanks[3] = new FluidTankNTM(Fluids.NONE, 4_000);

        this.fusionModule = new ModuleMachineFusion(0, this, inventory)
                .fluidInput(tanks[0], tanks[1], tanks[2])
                .fluidOutput(tanks[3])
                .itemOutput(2);
    }

    @Override
    public String getDefaultName() {
        return "container.fusionTorus";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            for(int i = 0; i < 4; i++) {
                if(klystronNodes[i] == null || klystronNodes[i].expired) klystronNodes[i] = createKlystronNode(KlystronNetwork.THE_PROVIDER, ForgeDirection.getOrientation(i + 2));
                if(plasmaNodes[i] == null || plasmaNodes[i].expired) plasmaNodes[i] = createPlasmaNode(PlasmaNetwork.THE_PROVIDER, ForgeDirection.getOrientation(i + 2));

                if(klystronNodes[i].net != null) klystronNodes[i].net.addReceiver(this);
                if(plasmaNodes[i].net != null) plasmaNodes[i].net.addProvider(this);
            }

            this.temperature += temp_passive_heating;
            if(this.temperature > KELVIN + 20) this.temperature = KELVIN + 20;

            if(this.temperature > temperature_target) {
                int cyclesTemp = (int) Math.ceil((Math.min(this.temperature - temperature_target, temp_change_max)) / temp_change_per_mb);
                int cyclesCool = coolantTanks[0].getFill();
                int cyclesHot = coolantTanks[1].getMaxFill() - coolantTanks[1].getFill();
                int cycles = BobMathUtil.min(cyclesTemp, cyclesCool, cyclesHot);

                coolantTanks[0].setFill(coolantTanks[0].getFill() - cycles);
                coolantTanks[1].setFill(coolantTanks[1].getFill() + cycles);
                this.temperature -= temp_change_per_mb * cycles;
            }

            for(DirPos pos : getConPos()) {

                if(world.getTotalWorldTime() % 20 == 0) {
                    this.trySubscribe(world, pos);
                    this.trySubscribe(coolantTanks[0].getTankType(), world, pos);
                    if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos);
                    if(tanks[1].getTankType() != Fluids.NONE) this.trySubscribe(tanks[1].getTankType(), world, pos);
                    if(tanks[2].getTankType() != Fluids.NONE) this.trySubscribe(tanks[2].getTankType(), world, pos);
                }

                if(coolantTanks[1].getFill() > 0) this.tryProvide(coolantTanks[1], world, pos);
                if(tanks[3].getFill() > 0) this.tryProvide(tanks[3], world, pos);
            }

            this.power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());

            // keeping track of PLASMA receivers because those need to share the combined output
            int receiverCount = 0;
            // collectors for determining the speed of the bonus bar
            int collectors = 0;

            for(int i = 0; i < 4; i++) {
                connections[i] = klystronNodes[i] != null && klystronNodes[i].hasValidNet() && !klystronNodes[i].net.providerEntries.isEmpty();
                if(!connections[i] && plasmaNodes[i] != null && plasmaNodes[i].hasValidNet() && !plasmaNodes[i].net.receiverEntries.isEmpty()) connections[i] = true;

                if(plasmaNodes[i] != null && plasmaNodes[i].hasValidNet() && !plasmaNodes[i].net.receiverEntries.isEmpty()) {

                    for(Map.Entry<TileEntity, Long> o : plasmaNodes[i].net.receiverEntries.entrySet()) {
                        TileEntity thing = o.getKey();
                        if(thing instanceof TileEntityLoadedBase && !((TileEntityLoadedBase) thing).isLoaded()) continue;
                        if(thing instanceof IFusionPowerReceiver && ((IFusionPowerReceiver) thing).receivesFusionPower()) receiverCount++;
                        if(thing instanceof TileEntityFusionCollector) collectors++;
                        break;
                    }
                }
            }

            FusionRecipe recipe = (FusionRecipe) this.fusionModule.getRecipe();

            double powerFactor = TileEntityFusionTorus.getSpeedScaled(this.getMaxPower(), power);
            double fuel0Factor = recipe != null && recipe.inputFluid.length > 0 ?  getSpeedScaled(tanks[0].getMaxFill(), tanks[0].getFill()) : 1D;
            double fuel1Factor = recipe != null && recipe.inputFluid.length > 1 ?  getSpeedScaled(tanks[1].getMaxFill(), tanks[1].getFill()) : 1D;
            double fuel2Factor = recipe != null && recipe.inputFluid.length > 2 ?  getSpeedScaled(tanks[2].getMaxFill(), tanks[2].getFill()) : 1D;

            double factor = BobMathUtil.min(powerFactor, fuel0Factor, fuel1Factor, fuel2Factor);

            boolean ignition = recipe == null || recipe.ignitionTemp <= this.klystronEnergy;

            this.plasmaEnergy = 0;
            this.fuelConsumption = 0;
            this.fusionModule.preUpdate(factor, collectors * 0.5D);
            this.fusionModule.update(1D, 1D, this.isCool() && ignition, inventory.getStackInSlot(1));
            this.didProcess = this.fusionModule.didProcess;
            if(this.fusionModule.markDirty) this.markDirty();
            if(didProcess && recipe != null) {
                this.plasmaEnergy = (long) Math.ceil(recipe.outputTemp * factor);
                this.fuelConsumption = factor;
            }

            double outputIntensity = getOuputIntensity(receiverCount);
            double outputFlux = recipe != null ? recipe.neutronFlux * factor : 0D;

            if(this.plasmaEnergy > 0) for(int i = 0; i < 4; i++) {

                if(plasmaNodes[i] != null && plasmaNodes[i].hasValidNet() && !plasmaNodes[i].net.receiverEntries.isEmpty()) {

                    for(Map.Entry<TileEntity, Long> o : plasmaNodes[i].net.receiverEntries.entrySet()) {
                        if(o.getKey() instanceof IFusionPowerReceiver receiver) {
                            long powerReceived = (long) Math.ceil(this.plasmaEnergy * outputIntensity);
                            receiver.receiveFusionPower(powerReceived, outputFlux);
                        }
                    }
                }
            }
            this.klystronEnergySync = this.klystronEnergy;
            this.networkPackNT(150);

            this.klystronEnergy = 0;

        } else {

            if(timeOffset == -1) this.timeOffset = world.rand.nextInt(30_000);

            double powerFactor = TileEntityFusionTorus.getSpeedScaled(this.getMaxPower(), power);
            if(this.didProcess) this.magnetSpeed += MAGNET_ACCELERATION;
            else this.magnetSpeed -= MAGNET_ACCELERATION;

            this.magnetSpeed = MathHelper.clamp(this.magnetSpeed, 0F, 30F * (float) powerFactor);

            this.prevMagnet = this.magnet;
            this.magnet += this.magnetSpeed;

            if(this.magnet >= 360F) {
                this.magnet -= 360F;
                this.prevMagnet -= 360F;
            }

            if(this.magnetSpeed > 0 && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5) < 50 * 50) {

                float speed = this.magnetSpeed / 30F;

                if(audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.fusionReactorRunning, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 2.5F, pos.getZ() + 0.5F, getVolume(speed), 30F, speed, 20);
                    audio.startSound();
                } else {
                    audio.updateVolume(getVolume(speed));
                    audio.updatePitch(speed);
                    audio.keepAlive();
                }

            } else {

                if(audio != null) {
                    if(audio.isPlaying()) audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    public static double getOuputIntensity(int receiverCount) {
        if(receiverCount == 1) return 1D; // 100%
        if(receiverCount == 2) return 0.625D; // 125%
        if(receiverCount == 3) return 0.5D; // 150%
        return 0.4375D; // 175%
    }

    public PlasmaNetwork.PlasmaNode createPlasmaNode(INetworkProvider<PlasmaNetwork> provider, ForgeDirection dir) {
        PlasmaNetwork.PlasmaNode node = UniNodespace.getNode(world, pos.add(dir.offsetX * 7, 2, dir.offsetZ * 7), provider);
        if(node != null) return node;

        node = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(provider,
                new BlockPos(pos.getX() + dir.offsetX * 7, pos.getY() + 2, pos.getZ() + dir.offsetZ * 7))
                .setConnections(new DirPos(pos.getX() + dir.offsetX * 8, pos.getY() + 2, pos.getZ() + dir.offsetZ * 8, dir));

        UniNodespace.createNode(world, node);

        return node;
    }
    public KlystronNetwork.KlystronNode createKlystronNode(INetworkProvider<KlystronNetwork> provider, ForgeDirection dir) {
        KlystronNetwork.KlystronNode node = UniNodespace.getNode(world, pos.add(dir.offsetX * 7, 2, dir.offsetZ * 7), provider);
        if(node != null) return node;

        node = (KlystronNetwork.KlystronNode) new KlystronNetwork.KlystronNode(provider,
                new BlockPos(pos.getX() + dir.offsetX * 7, pos.getY() + 2, pos.getZ() + dir.offsetZ * 7))
                .setConnections(new DirPos(pos.getX() + dir.offsetX * 8, pos.getY() + 2, pos.getZ() + dir.offsetZ * 8, dir));

        UniNodespace.createNode(world, node);

        return node;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }

        if(!world.isRemote) {
            for(KlystronNetwork.KlystronNode node : klystronNodes)
                if(node != null)
                    UniNodespace.destroyNode(world, node);
            for(PlasmaNetwork.PlasmaNode node : plasmaNodes)
                if(node != null)
                    UniNodespace.destroyNode(world, node);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.didProcess);
        buf.writeLong(this.klystronEnergySync);
        buf.writeLong(this.plasmaEnergy);
        buf.writeDouble(this.fuelConsumption);

        this.fusionModule.serialize(buf);
        for(int i = 0; i < 4; i++) this.tanks[i].serialize(buf);
        for(int i = 0; i < 4; i++) buf.writeBoolean(connections[i]);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.didProcess = buf.readBoolean();
        this.klystronEnergy = buf.readLong();
        this.plasmaEnergy = buf.readLong();
        this.fuelConsumption = buf.readDouble();

        this.fusionModule.deserialize(buf);
        for(int i = 0; i < 4; i++) this.tanks[i].deserialize(buf);
        for(int i = 0; i < 4; i++) connections[i] = buf.readBoolean();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        for(int i = 0; i < 4; i++) this.tanks[i].readFromNBT(nbt, "ft" + i);

        this.power = nbt.getLong("power");
        this.fusionModule.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for(int i = 0; i < 4; i++) this.tanks[i].writeToNBT(nbt, "ft" + i);

        nbt.setLong("power", power);
        this.fusionModule.writeToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override
    public long getMaxPower() {
        return 10_000_000;
    }

    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {coolantTanks[1], tanks[3]}; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {coolantTanks[0], tanks[0], tanks[1], tanks[2]}; }
    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {coolantTanks[0], coolantTanks[1], tanks[0], tanks[1], tanks[2], tanks[3]}; }

    /** Linearly scales up from 0% to 100% from 0 to 0.5, then stays at 100% */
    public static double getSpeedScaled(double max, double level) {
        if(max == 0) return 0D;
        if(level >= max * 0.5) return 1D;
        return level / max * 2D;
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY() + 5, pos.getZ(), Library.POS_Y),

                new DirPos(pos.getX() + 6, pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX() + 6, pos.getY() + 5, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX() + 6, pos.getY() - 1, pos.getZ() + 2, Library.NEG_Y),
                new DirPos(pos.getX() + 6, pos.getY() + 5, pos.getZ() + 2, Library.POS_Y),
                new DirPos(pos.getX() + 6, pos.getY() - 1, pos.getZ() - 2, Library.NEG_Y),
                new DirPos(pos.getX() + 6, pos.getY() + 5, pos.getZ() - 2, Library.POS_Y),

                new DirPos(pos.getX() - 6, pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX() - 6, pos.getY() + 5, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX() - 6, pos.getY() - 1, pos.getZ() + 2, Library.NEG_Y),
                new DirPos(pos.getX() - 6, pos.getY() + 5, pos.getZ() + 2, Library.POS_Y),
                new DirPos(pos.getX() - 6, pos.getY() - 1, pos.getZ() - 2, Library.NEG_Y),
                new DirPos(pos.getX() - 6, pos.getY() + 5, pos.getZ() - 2, Library.POS_Y),

                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() + 6, Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY() + 5, pos.getZ() + 6, Library.POS_Y),
                new DirPos(pos.getX() + 2, pos.getY() - 1, pos.getZ() + 6, Library.NEG_Y),
                new DirPos(pos.getX() + 2, pos.getY() + 5, pos.getZ() + 6, Library.POS_Y),
                new DirPos(pos.getX() - 2, pos.getY() - 1, pos.getZ() + 6, Library.NEG_Y),
                new DirPos(pos.getX() - 2, pos.getY() + 5, pos.getZ() + 6, Library.POS_Y),

                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() - 6, Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY() + 5, pos.getZ() - 6, Library.POS_Y),
                new DirPos(pos.getX() + 2, pos.getY() - 1, pos.getZ() - 6, Library.NEG_Y),
                new DirPos(pos.getX() + 2, pos.getY() + 5, pos.getZ() - 6, Library.POS_Y),
                new DirPos(pos.getX() - 2, pos.getY() - 1, pos.getZ() - 6, Library.NEG_Y),
                new DirPos(pos.getX() - 2, pos.getY() + 5, pos.getZ() - 6, Library.POS_Y),
        };
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return true; // battery
        return slot == 1 && stack.getItem() == ModItems.blueprints;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i == 2;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] {2};
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 8,
                    pos.getY(),
                    pos.getZ() - 8,
                    pos.getX() + 9,
                    pos.getY() + 5,
                    pos.getZ() + 9
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
        return new ContainerFusionTorus(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFusionTorus(player.inventory, this);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if(world.getTileEntity(pos) != this) return false;
        return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 32 * 32;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if(index == 0) {
                this.fusionModule.recipe = selection;
                this.markChanged();
            }
        }
    }
}
