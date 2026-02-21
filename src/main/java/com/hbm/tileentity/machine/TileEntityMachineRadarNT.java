package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.entity.IRadarDetectable;
import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.api.entity.RadarEntry;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmLivingProps;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineRadarNT;
import com.hbm.inventory.gui.GUIMachineRadarNT;
import com.hbm.inventory.gui.GUIMachineRadarNTSlots;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemCoordinateBase;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.satellites.*;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IRadarCommandReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple;
import com.hbm.world.WorldUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Now with SmЯt™ lag-free entity detection! (patent pending)
 *
 * @author hbm
 */
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityMachineRadarNT extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, IConfigurableMachine, IControlReceiver, SimpleComponent, CompatHandler.OCComponent {

    protected final static int maxTimer = 80;
    public static int maxPower = 100_000;
    public static int consumption = 500;
    public static int radarRange = 1_000;
    public static int radarBuffer = 30;
    public static int radarAltitude = 55;
    public static int chunkLoadCap = 10;
    public static boolean generateChunks = false;
    /**
     * List of lambdas that are supplied a Pair with the entity and radar in question to generate a RadarEntry
     * The converters coming first have the highest priority
     */
    public static List<Function<Tuple.Triplet<Entity, Object, IRadarDetectableNT.RadarScanParams>, RadarEntry>> converters = new ArrayList<>();
    public static ReferenceOpenHashSet<Class<?>> classes = new ReferenceOpenHashSet<>();
    private static final Reference2BooleanOpenHashMap<Class<?>> isMatchCache = new Reference2BooleanOpenHashMap<>();
    public static List<Entity> matchingEntities = new ArrayList<>();
    public boolean scanMissiles = true;
    public boolean scanShells = true;
    public boolean scanPlayers = true;
    public boolean smartMode = true;
    public boolean redMode = true;
    public boolean showMap = false;
    public boolean jammed = false;
    public float prevRotation;
    public float rotation;
    public long power = 0;
    public byte[] map = new byte[40_000];
    public boolean clearFlag = false;
    public volatile List<RadarEntry> entries = new ArrayList<>();
    protected int pingTimer = 0;
    protected int lastPower;
    AxisAlignedBB bb = null;

    public TileEntityMachineRadarNT() {
        super(10);
    }

    /**
     * Iterates over every entity in the world and add them to the matchingEntities list if the class is in the detectable list
     * From this compiled list, radars can easily grab the required entities since we can assume that the total amount of detectable entities is comparatively low
     */
    public static void updateSystem() {
        matchingEntities.clear();

        for (WorldServer world : DimensionManager.getWorlds()) {
            List<Entity> list = world.loadedEntityList;
            for (int i = 0; i < list.size(); i++) {
                if (i >= list.size()) break;

                Entity entity = list.get(i);
                if (entity == null) continue;

                if (isMatchCache.computeIfAbsent(entity.getClass(), TileEntityMachineRadarNT::checkHierarchy)) {
                    matchingEntities.add(entity);
                }
            }
        }
    }

    private static boolean checkHierarchy(Class<?> entityClass) {
        for (Class<?> target : classes) {
            if (target.isAssignableFrom(entityClass)) return true;
        }
        return false;
    }

    /**
     * Registers a class that if an entity inherits that class, it is picked up by the system
     */
    public static void registerEntityClasses() {
        classes.add(IRadarDetectableNT.class);
        classes.add(IRadarDetectable.class);
        classes.add(EntityPlayer.class);
    }

    /**
     * Registers converters. Converters are used to go over the list of detected entities and turn them into a RadarEntry using the entity instance and the radar's instance.
     */
    public static void registerConverters() {
        //IRadarDetectableNT
        converters.add(x -> {
            Entity e = x.getX();
            if (e instanceof IRadarDetectableNT) {
                IRadarDetectableNT detectable = (IRadarDetectableNT) e;
                if (detectable.canBeSeenBy(x.getY()) && detectable.paramsApplicable(x.getZ()))
                    return new RadarEntry(detectable, e, detectable.suppliesRedstone(x.getZ()));
            }
            return null;
        });
        //IRadarDetectable, Legacy
        converters.add(x -> {
            Entity e = x.getX();
            IRadarDetectableNT.RadarScanParams params = x.getZ();
            if (e instanceof IRadarDetectable && params.scanMissiles) {
                return new RadarEntry((IRadarDetectable) e, e);
            }
            return null;
        });
        //Players
        converters.add(x -> {
            if (x.getX() instanceof EntityPlayer && x.getZ().scanPlayers)
                return new RadarEntry((EntityPlayer) x.getX());
            return null;
        });
    }

    @Override
    public String getConfigName() {
        return "radar";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "L:powerCap", maxPower);
        consumption = IConfigurableMachine.grab(obj, "L:consumption", consumption);
        radarRange = IConfigurableMachine.grab(obj, "I:radarRange", radarRange);
        radarBuffer = IConfigurableMachine.grab(obj, "I:radarBuffer", radarBuffer);
        radarAltitude = IConfigurableMachine.grab(obj, "I:radarAltitude", radarAltitude);
        chunkLoadCap = IConfigurableMachine.grab(obj, "I:chunkLoadCap", chunkLoadCap);
        generateChunks = IConfigurableMachine.grab(obj, "B:generateChunks", generateChunks);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:powerCap").value(maxPower);
        writer.name("L:consumption").value(consumption);
        writer.name("I:radarRange").value(radarRange);
        writer.name("I:radarBuffer").value(radarBuffer);
        writer.name("I:radarAltitude").value(radarAltitude);
        writer.name("B:generateChunks").value(generateChunks);
    }

    @Override
    public String getDefaultName() {
        return "container.radar";
    }

    public int getRange() {
        return radarRange;
    }

    @Override
    public void update() {

        if (this.map == null || this.map.length != 40_000) this.map = new byte[40_000];

        if (!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 9, power, maxPower);

            if (world.getTotalWorldTime() % 20 == 0) {
                for (DirPos pos : getConPos()) {
                    this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            }

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            this.jammed = false;
            allocateTargets();

            if (this.lastPower != getRedPower()) {
                this.markChanged();
                if (this.getBlockType() == ModBlocks.machine_radar_large)
                    for (DirPos dirPos : getConPos())
                        this.world.notifyNeighborsOfStateChange(pos.offset(dirPos.getDir().toEnumFacing()), getBlockType(), true);
                else this.world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
            }
            lastPower = getRedPower();

            if (!this.muffled) {

                pingTimer++;

                if (power > 0 && pingTimer >= maxTimer) {
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.sonarPing, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    pingTimer = 0;
                }
            }

            if (this.showMap) {
                int chunkLoads = 0;
                for (int i = 0; i < 100; i++) {
                    int index = (int) (world.getTotalWorldTime() % 400) * 100 + i;
                    int iX = (index % 200) * getRange() * 2 / 200;
                    int iZ = index / 200 * getRange() * 2 / 200;

                    int x = pos.getX() - getRange() + iX;
                    int z = pos.getZ() - getRange() + iZ;

                    if (world.getChunkProvider().isChunkGeneratedAt(x >> 4, z >> 4)) {
                        this.map[index] = (byte) MathHelper.clamp(world.getHeight(x, z), 50, 128);
                    } else {
                        if (this.map[index] == 0 && chunkLoads < chunkLoadCap) {
                            if (this.generateChunks) {
                                world.getChunk(x >> 4, z >> 4);
                                this.map[index] = (byte) MathHelper.clamp(world.getHeight(x, z), 50, 128);
                                chunkLoads++;
                            } else {
                                WorldUtil.provideChunk((WorldServer) world, x >> 4, z >> 4);
                                this.map[index] = (byte) MathHelper.clamp(world.getHeight(x, z), 50, 128);
                                if (world.getChunkProvider().isChunkGeneratedAt(x >> 4, z >> 4)) chunkLoads++;
                            }
                        }
                    }
                }
            }

            if (!inventory.getStackInSlot(8).isEmpty() && inventory.getStackInSlot(8).getItem() == ModItems.radar_linker) {
                BlockPos pos = ItemCoordinateBase.getPosition(inventory.getStackInSlot(8));
                if (pos != null) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileEntityMachineRadarScreen screen) {
                        screen.entries.clear();
                        screen.entries.addAll(this.entries);
                        screen.refX = this.pos.getX();
                        screen.refY = this.pos.getY();
                        screen.refZ = this.pos.getZ();
                        screen.range = this.getRange();
                        screen.linked = true;
                        screen.networkPackNT(25);
                    }
                }
            }

            this.networkPackNT(50);
            if (this.clearFlag) {
                this.map = new byte[40_000];
                this.clearFlag = false;
            }
        } else {
            prevRotation = rotation;
            if (power > 0) rotation += 5F;

            if (rotation >= 360) {
                rotation -= 360F;
                prevRotation -= 360F;
            }
        }
    }

    public DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.scanMissiles);
        buf.writeBoolean(this.scanShells);
        buf.writeBoolean(this.scanPlayers);
        buf.writeBoolean(this.smartMode);
        buf.writeBoolean(this.redMode);
        buf.writeBoolean(this.showMap);
        buf.writeBoolean(this.jammed);
        buf.writeInt(entries.size());
        for (RadarEntry entry : entries) entry.toBytes(buf);
        if (this.clearFlag) {
            buf.writeBoolean(true);
        } else {
            buf.writeBoolean(false);
            if (this.showMap) {
                buf.writeBoolean(true);
                short index = (short) (world.getTotalWorldTime() % 400);
                buf.writeShort(index);
                for (int i = index * 100; i < (index + 1) * 100; i++) {
                    buf.writeByte(this.map[i]);
                }
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.scanMissiles = buf.readBoolean();
        this.scanShells = buf.readBoolean();
        this.scanPlayers = buf.readBoolean();
        this.smartMode = buf.readBoolean();
        this.redMode = buf.readBoolean();
        this.showMap = buf.readBoolean();
        this.jammed = buf.readBoolean();
        int count = buf.readInt();
        List<RadarEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RadarEntry entry = new RadarEntry();
            entry.fromBytes(buf);
            entries.add(entry);
        }
        this.entries = entries;
        if (buf.readBoolean()) { // clear flag
            this.map = new byte[40_000];
        } else {
            if (buf.readBoolean()) { // map enabled
                int index = buf.readShort();
                for (int i = index * 100; i < (index + 1) * 100; i++) {
                    this.map[i] = buf.readByte();
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");
        this.scanMissiles = nbt.getBoolean("scanMissiles");
        this.scanShells = nbt.getBoolean("scanShells");
        this.scanPlayers = nbt.getBoolean("scanPlayers");
        this.smartMode = nbt.getBoolean("smartMode");
        this.redMode = nbt.getBoolean("redMode");
        this.showMap = nbt.getBoolean("showMap");
        if (nbt.hasKey("map")) this.map = nbt.getByteArray("map");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setBoolean("scanMissiles", scanMissiles);
        nbt.setBoolean("scanShells", scanShells);
        nbt.setBoolean("scanPlayers", scanPlayers);
        nbt.setBoolean("smartMode", smartMode);
        nbt.setBoolean("redMode", redMode);
        nbt.setBoolean("showMap", showMap);
        nbt.setByteArray("map", map);
        return super.writeToNBT(nbt);
    }

    protected void allocateTargets() {
        this.entries.clear();

        if (this.pos.getY() < radarAltitude) return;
        if (this.power < consumption) return;
        this.power -= consumption;

        int scan = this.getRange();

        IRadarDetectableNT.RadarScanParams params = new IRadarDetectableNT.RadarScanParams(this.scanMissiles, this.scanShells, this.scanPlayers, this.smartMode);

        for (Entity e : matchingEntities) {

            if (e.dimension == world.provider.getDimension() && Math.abs(e.posX - (pos.getX() + 0.5)) <= scan && Math.abs(e.posZ - (pos.getZ() + 0.5)) <= scan && e.posY - pos.getY() > radarBuffer) {

                if (e instanceof EntityLivingBase && HbmLivingProps.getDigamma((EntityLivingBase) e) > 0.001) {
                    this.jammed = true;
                    entries.clear();
                    return;
                }

                for (Function<Tuple.Triplet<Entity, Object, IRadarDetectableNT.RadarScanParams>, RadarEntry> converter : converters) {

                    RadarEntry entry = converter.apply(new Tuple.Triplet(e, this, params));
                    if (entry != null) {
                        this.entries.add(entry);
                        break;
                    }
                }
            }
        }
    }

    public int getRedPower() {

        if (!entries.isEmpty()) {

            /// PROXIMITY ///
            if (redMode) {

                double maxRange = this.getRange() * Math.sqrt(2D);
                int power = 0;

                for (int i = 0; i < entries.size(); i++) {
                    RadarEntry e = entries.get(i);
                    if (!e.redstone) continue;
                    double dist = Math.sqrt(Math.pow(e.posX - pos.getX(), 2) + Math.pow(e.posZ - pos.getZ(), 2));
                    int p = 15 - (int) Math.floor(dist / maxRange * 15);

                    if (p > power) power = p;
                }

                return power;

                /// TIER ///
            } else {

                int power = 0;

                for (int i = 0; i < entries.size(); i++) {
                    RadarEntry e = entries.get(i);
                    if (!e.redstone) continue;
                    if (e.blipLevel + 1 > power) {
                        power = e.blipLevel + 1;
                    }
                }

                return power;
            }
        }

        return 0;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {

        if (data.hasKey("missiles")) this.scanMissiles = !this.scanMissiles;
        if (data.hasKey("shells")) this.scanShells = !this.scanShells;
        if (data.hasKey("players")) this.scanPlayers = !this.scanPlayers;
        if (data.hasKey("smart")) this.smartMode = !this.smartMode;
        if (data.hasKey("red")) this.redMode = !this.redMode;
        if (data.hasKey("map")) this.showMap = !this.showMap;
        if (data.hasKey("clear")) this.clearFlag = true;

        if (data.hasKey("gui1"))
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());

        if (data.hasKey("link")) {
            int id = data.getInteger("link");
            ItemStack link = inventory.getStackInSlot(id);

            if (!link.isEmpty() && link.getItem() == ModItems.sat_relay) {
                World world = player.getEntityWorld();
                Satellite sat = SatelliteSavedData.getData(world).getSatFromFreq(ISatChip.getFreqS(link));
                if (sat instanceof SatelliteLaser) {
                    if (data.hasKey("launchPosX")) {
                        int x = data.getInteger("launchPosX");
                        int z = data.getInteger("launchPosZ");
                        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
                        sat.onClick(world, player, x, z);
                    }
                }
                if (sat instanceof SatelliteHorizons) {
                    if (data.hasKey("launchPosX")) {
                        int x = data.getInteger("launchPosX");
                        int z = data.getInteger("launchPosZ");
                        int y = 60; //one day I will make radars transmit Y coordinate as well and you will be butchered alhamdulila
                        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
                        sat.onCoordAction(world, player, x, y, z);
                    }
                }
                if (sat instanceof SatelliteResonator) {
                    if (data.hasKey("launchPosX")) {
                        int x = data.getInteger("launchPosX");
                        int z = data.getInteger("launchPosZ");
                        int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, pos.getY() + 1, z)).getY(); //the top fucking block because I will never make radars transmit Y coordinates as well!
                        world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.AMBIENT, 1.0F, 1.0F);
                        sat.onCoordAction(world, player, x, y, z);
                    }
                }
            }
            if (!link.isEmpty() && link.getItem() == ModItems.radar_linker) {
                BlockPos pos = ItemCoordinateBase.getPosition(link);

                if (pos != null) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof IRadarCommandReceiver) {
                        IRadarCommandReceiver rec = (IRadarCommandReceiver) tile;

                        if (data.hasKey("launchEntity")) {
                            Entity entity = world.getEntityByID(data.getInteger("launchEntity"));
                            if (entity != null) {
                                if (rec.sendCommandEntity(entity)) {
                                    world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
                                }
                            }
                        } else if (data.hasKey("launchPosX")) {
                            int x = data.getInteger("launchPosX");
                            int z = data.getInteger("launchPosZ");
                            if (rec.sendCommandPosition(x, pos.getY(), z)) {
                                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 3,
                    pos.getZ() + 2
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 1) return new ContainerMachineRadarNT(player.inventory, this);
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) return new GUIMachineRadarNT(this);
        if (ID == 1) return new GUIMachineRadarNTSlots(player.inventory, this);
        return null;
    }


    //OC compat!

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_radar";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSettings(Context context, Arguments args) {
        return new Object[]{scanMissiles, scanShells, scanPlayers, smartMode};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getRange(Context context, Arguments args) {
        return new Object[]{this.getRange()};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setSettings(Context context, Arguments args) {
        this.scanMissiles = args.checkBoolean(0);
        this.scanShells = args.checkBoolean(1);
        this.scanPlayers = args.checkBoolean(2);
        this.smartMode = args.checkBoolean(3);
        return new Object[]{};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isJammed(Context context, Arguments args) {
        return new Object[]{this.jammed};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getAmount(Context context, Arguments args) {
        return new Object[]{entries.size()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isIndexPlayer(Context context, Arguments args) {
        int index = args.checkInteger(0) - 1;
        if (index > entries.size() || index < 0) {
            return new Object[]{null, "No entity exists at that index."};
        }
        RadarEntry e = entries.get(index);
        return new Object[]{e.blipLevel == IRadarDetectableNT.PLAYER};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getIndexType(Context context, Arguments args) {
        int index = args.checkInteger(0) - 1;
        if (index > entries.size() || index < 0) {
            return new Object[]{null, "No entity exists at that index."};
        }
        RadarEntry e = entries.get(index);
        return new Object[]{e.blipLevel};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEntityAtIndex(Context context, Arguments args) {
        int index = args.checkInteger(0) - 1;
        if (index > entries.size() || index < 0) {
            return new Object[]{null, "No entity exists at that index."};
        }
        RadarEntry e = entries.get(index);
        int type = e.blipLevel;
        if (e.blipLevel == IRadarDetectableNT.PLAYER) {
            return new Object[]{true, e.posX, e.posY, e.posZ, type, e.unlocalizedName};
        }
        return new Object[]{false, e.posX, e.posY, e.posZ, type};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{
                "getSettings",
                "getRange",
                "setSettings",
                "getEnergyInfo",
                "isJammed",
                "getAmount",
                "isIndexPlayer",
                "getIndexType",
                "getEntityAtIndex"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case ("getSettings"):
                return getSettings(context, args);
            case ("getRange"):
                return getRange(context, args);
            case ("setSettings"):
                return setSettings(context, args);
            case ("getEnergyInfo"):
                return getEnergyInfo(context, args);
            case ("isJammed"):
                return isJammed(context, args);
            case ("getAmount"):
                return getAmount(context, args);
            case ("isIndexPlayer"):
                return isIndexPlayer(context, args);
            case ("getIndexType"):
                return getIndexType(context, args);
            case ("getEntityAtIndex"):
                return getEntityAtIndex(context, args);
        }
        throw new NoSuchMethodException();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
