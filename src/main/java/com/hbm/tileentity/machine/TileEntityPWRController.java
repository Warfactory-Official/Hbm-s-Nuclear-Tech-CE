package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerPWR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.fluid.trait.FT_PWRModerator;
import com.hbm.inventory.gui.GUIPWR;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityPWRController extends TileEntityMachineBase implements ITickable, IGUIProvider, IControlReceiver, SimpleComponent, IFluidStandardTransceiver, CompatHandler.OCComponent {

    public static final long coreHeatCapacityBase = 10_000_000;
    public static final long hullHeatCapacityBase = 10_000_000;
    public FluidTankNTM[] tanks;
    public volatile long coreHeat;
    public volatile long coreHeatCapacity = 10_000_000;
    public volatile long hullHeat;
    public volatile double flux;

    public volatile double rodLevel = 100;
    public volatile double rodTarget = 100;

    public volatile int typeLoaded;
    public volatile int amountLoaded;
    public volatile double progress;
    public volatile double processTime;

    public int rodCount;
    public int connections;
    public int connectionsControlled;
    public int heatexCount;
    public int heatsinkCount;
    public int channelCount;
    public int sourceCount;

    public int unloadDelay = 0;
    public boolean assembled;
    protected List<BlockPos> ports = new ArrayList<>();
    protected List<BlockPos> rods = new ArrayList<>();
    private AudioWrapper audio;

    public TileEntityPWRController() {
        super(3, true, false);

        this.tanks = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.COOLANT, 128_000);
        this.tanks[1] = new FluidTankNTM(Fluids.COOLANT_HOT, 128_000);
    }

    /**
     * The initial creation of the reactor, does all the pre-calculation and whatnot
     */
    public void setup(HashMap<BlockPos, IBlockState> partMap, HashMap<BlockPos, IBlockState> rodMap) {
        rodCount = 0;
        connections = 0;
        connectionsControlled = 0;
        heatexCount = 0;
        channelCount = 0;
        heatsinkCount = 0;
        sourceCount = 0;
        ports.clear();
        rods.clear();

        int connectionsDouble = 0;
        int connectionsControlledDouble = 0;

        for (Entry<BlockPos, IBlockState> entry : partMap.entrySet()) {
            Block block = entry.getValue().getBlock();

            if (block == ModBlocks.pwr_fuelrod) rodCount++;
            if (block == ModBlocks.pwr_heatex) heatexCount++;
            if (block == ModBlocks.pwr_channel) channelCount++;
            if (block == ModBlocks.pwr_heatsink) heatsinkCount++;
            if (block == ModBlocks.pwr_neutron_source) sourceCount++;
            if (block == ModBlocks.pwr_port) ports.add(entry.getKey());
        }

        for (Entry<BlockPos, IBlockState> entry : rodMap.entrySet()) {
            BlockPos fuelPos = entry.getKey();
            rods.add(fuelPos);

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                boolean controlled = false;

                for (int i = 1; i < 16; i++) {
                    BlockPos checkPos = fuelPos.offset(dir.toEnumFacing(), i);
                    IBlockState stateAtPos = partMap.get(checkPos);
                    Block atPos = stateAtPos != null ? stateAtPos.getBlock() : null;

                    if (atPos == null || atPos == ModBlocks.pwr_casing) break;
                    if (atPos == ModBlocks.pwr_control) controlled = true;
                    if (atPos == ModBlocks.pwr_fuelrod) {
                        if (controlled) {
                            connectionsControlledDouble++;
                        } else {
                            connectionsDouble++;
                        }
                        break;
                    }
                    if (atPos == ModBlocks.pwr_reflector) {
                        if (controlled) {
                            connectionsControlledDouble += 2;
                        } else {
                            connectionsDouble += 2;
                        }
                        break;
                    }
                }
            }
        }

        connections = connectionsDouble / 2;
        connectionsControlled = connectionsControlledDouble / 2;
        heatsinkCount = Math.min(heatsinkCount, 80);

        this.coreHeatCapacity = coreHeatCapacityBase + this.heatsinkCount * (coreHeatCapacityBase / 20);
    }

    @Override
    public String getDefaultName() {
        return "container.pwrController";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.tanks[0].setType(2, inventory);
            setupTanks();

            if (unloadDelay > 0) unloadDelay--;

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            if (world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) == null ||
                    world.getChunkProvider().getLoadedChunk(chunkX + 2, chunkZ + 2) == null ||
                    world.getChunkProvider().getLoadedChunk(chunkX + 2, chunkZ - 2) == null ||
                    world.getChunkProvider().getLoadedChunk(chunkX - 2, chunkZ + 2) == null ||
                    world.getChunkProvider().getLoadedChunk(chunkX - 2, chunkZ - 2) == null) {
                this.unloadDelay = 60;
            }

            if (this.assembled) {
                for (BlockPos portPos : ports) {
                    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        BlockPos targetPos = portPos.offset(dir.toEnumFacing());

                        if (tanks[1].getFill() > 0)
                            this.sendFluid(tanks[1], world, targetPos.getX(), targetPos.getY(), targetPos.getZ(), dir);
                        if (world.getTotalWorldTime() % 20 == 0)
                            this.trySubscribe(tanks[0].getTankType(), world, targetPos.getX(), targetPos.getY(), targetPos.getZ(), dir);
                    }
                }

                if (this.unloadDelay <= 0) {
                    ItemStack rodStack = inventory.getStackInSlot(0);
                    ItemStack rodHotStack = inventory.getStackInSlot(1);

                    if ((typeLoaded == -1 || amountLoaded <= 0) && !rodStack.isEmpty() && rodStack.getItem() == ModItems.pwr_fuel) {
                        typeLoaded = rodStack.getItemDamage();
                        amountLoaded++;
                        rodStack.shrink(1);
                        this.markDirty();
                    } else if (!rodStack.isEmpty() && rodStack.getItem() == ModItems.pwr_fuel && rodStack.getItemDamage() == typeLoaded && amountLoaded < rodCount) {
                        amountLoaded++;
                        rodStack.shrink(1);
                        this.markDirty();
                    }
                    double diff = this.rodLevel - this.rodTarget;
                    if (diff < 1 && diff > -1) this.rodLevel = this.rodTarget;
                    if (this.rodTarget > this.rodLevel) this.rodLevel++;
                    if (this.rodTarget < this.rodLevel) this.rodLevel--;

                    int newFlux = this.sourceCount * 20;

                    if (typeLoaded != -1 && amountLoaded > 0) {
                        EnumPWRFuel fuel = EnumUtil.grabEnumSafely(EnumPWRFuel.VALUES, typeLoaded);
                        double usedRods = getTotalProcessMultiplier();
                        double fluxPerRod = (this.rodCount > 0) ? this.flux / this.rodCount : 0;
                        double outputPerRod = fuel.function.effonix(fluxPerRod);
                        double totalOutput = outputPerRod * amountLoaded * usedRods;
                        double totalHeatOutput = totalOutput * fuel.heatEmission;

                        this.coreHeat += totalHeatOutput;
                        newFlux += totalOutput;

                        this.processTime = (int) fuel.yield;
                        this.progress += totalOutput;

                        if (this.progress >= this.processTime) {
                            this.progress -= this.processTime;

                            if (rodHotStack.isEmpty()) {
                                inventory.setStackInSlot(1, new ItemStack(ModItems.pwr_fuel_hot, 1, typeLoaded));
                            } else if (rodHotStack.getItem() == ModItems.pwr_fuel_hot && rodHotStack.getItemDamage() == typeLoaded && rodHotStack.getCount() < rodHotStack.getMaxStackSize()) {
                                rodHotStack.grow(1);
                            }

                            this.amountLoaded--;
                            this.markDirty();
                        }
                    }

                    if (this.amountLoaded <= 0) {
                        this.typeLoaded = -1;
                    }

                    if (amountLoaded > rodCount) amountLoaded = rodCount;

                    /* CORE COOLING */
                    double coreCoolingApproachNum = getXOverE((double) this.heatexCount * 5 / (double) getRodCountForCoolant(), 2) / 2D;
                    long averageCoreHeat = (this.coreHeat + this.hullHeat) / 2;
                    this.coreHeat -= (coreHeat - averageCoreHeat) * coreCoolingApproachNum;
                    this.hullHeat -= (hullHeat - averageCoreHeat) * coreCoolingApproachNum;

                    updateCoolant();

                    this.coreHeat *= 0.999D;
                    this.hullHeat *= 0.999D;

                    this.flux = newFlux;

                    if (tanks[0].getTankType().hasTrait(FT_PWRModerator.class) && tanks[0].getFill() > 0) {
                        this.flux *= tanks[0].getTankType().getTrait(FT_PWRModerator.class).getMultiplier();
                    }

                    if (this.coreHeat > this.coreHeatCapacity) {
                        meltDown();
                    }
                } else {
                    this.hullHeat = 0;
                    this.coreHeat = 0;
                }
            }
            this.networkPackNT(150);
        } else {
            if (amountLoaded > 0) {
                if (audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }
                audio.updateVolume(getVolume(1F));
                audio.keepAlive();
            } else {
                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    protected void meltDown() {
        world.setBlockToAir(this.getPos());

        double x = 0;
        double y = 0;
        double z = 0;

        if (rods.isEmpty()) return;

        for (BlockPos pos : this.rods) {
            IBlockState state = world.getBlockState(pos);
            state.getBlock().breakBlock(world, pos, state);
            world.setBlockState(pos, ModBlocks.corium_block.getDefaultState(), 3);

            x += pos.getX() + 0.5;
            y += pos.getY() + 0.5;
            z += pos.getZ() + 0.5;
        }

        x /= rods.size();
        y /= rods.size();
        z /= rods.size();

        world.newExplosion(null, x, y, z, 15F, true, true);
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.reactorLoop, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1F, 10F, 1.0F, 20);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    private void updateCoolant() {
        FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
        if (trait == null || trait.getEfficiency(HeatingType.PWR) <= 0) return;

        double coolingEff = (double) this.channelCount / (double) getRodCountForCoolant() * 0.1D; //10% cooling if numbers match
        if (coolingEff > 1D) coolingEff = 1D;

        int heatToUse = (int) Math.min(Math.min(this.hullHeat, (long) (this.hullHeat * coolingEff * trait.getEfficiency(HeatingType.PWR))), 2_000_000_000);
        HeatingStep step = trait.getFirstStep();
        if (step.amountReq <= 0 || step.heatReq <= 0) return; // Avoid division by zero
        int coolCycles = tanks[0].getFill() / step.amountReq;
        int hotCycles = (tanks[1].getMaxFill() - tanks[1].getFill()) / step.amountProduced;
        int heatCycles = heatToUse / step.heatReq;
        int cycles = Math.min(coolCycles, Math.min(hotCycles, heatCycles));

        this.hullHeat -= (long) step.heatReq * cycles;
        this.tanks[0].setFill(tanks[0].getFill() - step.amountReq * cycles);
        this.tanks[1].setFill(tanks[1].getFill() + step.amountProduced * cycles);
    }

    protected int getRodCountForCoolant() {
        return this.rodCount + (int) Math.ceil(this.heatsinkCount / 4D);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.assembled);
        buf.writeInt(this.rodCount);
        buf.writeLong(this.coreHeat);
        buf.writeLong(this.hullHeat);
        buf.writeDouble(this.flux);
        buf.writeDouble(this.processTime);
        buf.writeDouble(this.progress);
        buf.writeInt(this.typeLoaded);
        buf.writeInt(this.amountLoaded);
        buf.writeDouble(this.rodLevel);
        buf.writeDouble(this.rodTarget);
        buf.writeLong(this.coreHeatCapacity);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.assembled = buf.readBoolean();
        this.rodCount = buf.readInt();
        this.coreHeat = buf.readLong();
        this.hullHeat = buf.readLong();
        this.flux = buf.readDouble();
        this.processTime = buf.readDouble();
        this.progress = buf.readDouble();
        this.typeLoaded = buf.readInt();
        this.amountLoaded = buf.readInt();
        this.rodLevel = buf.readDouble();
        this.rodTarget = buf.readDouble();
        this.coreHeatCapacity = buf.readLong();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    private void setupTanks() {
        FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
        if (trait == null || trait.getEfficiency(HeatingType.PWR) <= 0) {
            tanks[0].setTankType(Fluids.NONE);
            tanks[1].setTankType(Fluids.NONE);
            return;
        }
        tanks[1].setTankType(trait.getFirstStep().typeProduced);
    }

    public double getTotalProcessMultiplier() {
        double totalConnections = this.connections + this.connectionsControlled * (1D - (this.rodLevel / 100D));
        return connectinFunc(totalConnections);
    }

    public double connectinFunc(double connections) {
        return connections / 10D * (1D - getXOverE(connections, 300D)) + connections / 150D * getXOverE(connections, 300D);
    }

    public double getXOverE(double x, double d) {
        return 1 - Math.pow(Math.E, -x / d);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) return stack.getItem() == ModItems.pwr_fuel;
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
        return slot == 1;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir){
        return type == tanks[0].getTankType() || type == tanks[1].getTankType();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{0, 1};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tanks[0].readFromNBT(nbt, "t0");
        tanks[1].readFromNBT(nbt, "t1");
        this.assembled = nbt.getBoolean("assembled");
        this.coreHeat = nbt.getLong("coreHeatL");
        this.hullHeat = nbt.getLong("hullHeatL");
        this.flux = nbt.getDouble("flux");
        this.rodLevel = nbt.getDouble("rodLevel");
        this.rodTarget = nbt.getDouble("rodTarget");
        this.typeLoaded = nbt.getInteger("typeLoaded");
        this.amountLoaded = nbt.getInteger("amountLoaded");
        this.progress = nbt.getDouble("progress");
        this.processTime = nbt.getDouble("processTime");
        this.coreHeatCapacity = nbt.getLong("coreHeatCapacityL");
        if (this.coreHeatCapacity < coreHeatCapacityBase) this.coreHeatCapacity = coreHeatCapacityBase;

        this.rodCount = nbt.getInteger("rodCount");
        this.connections = nbt.getInteger("connections");
        this.connectionsControlled = nbt.getInteger("connectionsControlled");
        this.heatexCount = nbt.getInteger("heatexCount");
        this.channelCount = nbt.getInteger("channelCount");
        this.sourceCount = nbt.getInteger("sourceCount");
        this.heatsinkCount = nbt.getInteger("heatsinkCount");

        ports.clear();
        int portCount = nbt.getInteger("portCount");
        for (int i = 0; i < portCount; i++) {
            int[] port = nbt.getIntArray("p" + i);
            ports.add(new BlockPos(port[0], port[1], port[2]));
        }

        rods.clear();
        int rodListSize = nbt.getInteger("rods_list_size");
        for (int i = 0; i < rodListSize; i++) {
            if (nbt.hasKey("r" + i)) {
                int[] port = nbt.getIntArray("r" + i);
                rods.add(new BlockPos(port[0], port[1], port[2]));
            }
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tanks[0].writeToNBT(nbt, "t0");
        tanks[1].writeToNBT(nbt, "t1");
        nbt.setBoolean("assembled", assembled);
        nbt.setLong("coreHeatL", coreHeat);
        nbt.setLong("hullHeatL", hullHeat);
        nbt.setDouble("flux", flux);
        nbt.setDouble("rodLevel", rodLevel);
        nbt.setDouble("rodTarget", rodTarget);
        nbt.setInteger("typeLoaded", typeLoaded);
        nbt.setInteger("amountLoaded", amountLoaded);
        nbt.setDouble("progress", progress);
        nbt.setDouble("processTime", processTime);
        nbt.setLong("coreHeatCapacityL", coreHeatCapacity);

        nbt.setInteger("rodCount", rodCount);
        nbt.setInteger("connections", connections);
        nbt.setInteger("connectionsControlled", connectionsControlled);
        nbt.setInteger("heatexCount", heatexCount);
        nbt.setInteger("channelCount", channelCount);
        nbt.setInteger("sourceCount", sourceCount);
        nbt.setInteger("heatsinkCount", heatsinkCount);

        nbt.setInteger("portCount", ports.size());
        for (int i = 0; i < ports.size(); i++) {
            BlockPos pos = ports.get(i);
            nbt.setIntArray("p" + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }

        nbt.setInteger("rods_list_size", rods.size());
        for (int i = 0; i < rods.size(); i++) {
            BlockPos pos = rods.get(i);
            nbt.setIntArray("r" + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUsableByPlayer(player);
    }

    // Standard IInventory method to check if a player is close enough to use the GUI
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("control")) {
            this.rodTarget = MathHelper.clamp(data.getInteger("control"), 0, 100);
            this.markDirty();
        }
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_pwr_control";
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getHeat(Context context, Arguments args) {
        return new Object[]{coreHeat, hullHeat};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFlux(Context context, Arguments args) {
        return new Object[]{flux};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getLevel(Context context, Arguments args) {
        return new Object[]{rodTarget, rodLevel};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCoolantInfo(Context context, Arguments args) {
        return new Object[]{tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill()};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFuelInfo(Context context, Arguments args) {
        return new Object[]{amountLoaded, progress, processTime};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{coreHeat, hullHeat, flux, rodTarget, rodLevel, amountLoaded, progress, processTime, tanks[0].getFill(), tanks[0].getMaxFill(), tanks[1].getFill(), tanks[1].getMaxFill()};
    }

    @SuppressWarnings("unused")
    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setLevel(Context context, Arguments args) {
        rodTarget = MathHelper.clamp(args.checkDouble(0), 0, 100);
        this.markDirty();
        return new Object[]{true};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPWR(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPWR(player.inventory, this);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }
}
