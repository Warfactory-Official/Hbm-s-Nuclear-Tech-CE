package com.hbm.tileentity.machine.rbmk;

import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIRBMKConsole;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlManual.RBMKColor;
import com.hbm.util.BobMathUtil;
import com.hbm.util.BufferUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityRBMKConsole extends TileEntityMachineBase implements IControlReceiver, IGUIProvider, ITickable, SimpleComponent, CompatHandler.OCComponent {

    public static final int fluxDisplayBuffer = 60;
    public int[] fluxBuffer = new int[fluxDisplayBuffer];
    // one-dimensional for simpler (de)serialization
    public RBMKColumn[] columns = new RBMKColumn[15 * 15];
    public RBMKScreen[] screens = new RBMKScreen[6];
    private int targetX;
    private int targetY;
    private int targetZ;
    private byte rotation;

    public TileEntityRBMKConsole() {
        super(0);
        for (int i = 0; i < screens.length; i++) {
            screens[i] = new RBMKScreen();
        }
    }

    @Override
    public String getDefaultName() {
        return null;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (this.world.getTotalWorldTime() % 10 == 0) {
                rescan();
                prepareScreenInfo();
            }
            networkPackNT(50);
        }
    }

    private void rescan() {
        double flux = 0;

        for (int index = 0; index < columns.length; index++) {
            final int rx = getXFromIndex(index);
            final int rz = getZFromIndex(index);

            TileEntity te = world.getTileEntity(new BlockPos(targetX + rx, targetY, targetZ + rz));

            if (te instanceof TileEntityRBMKBase rbmk) {

                columns[index] = new RBMKColumn(rbmk.getConsoleType(), rbmk.getNBTForConsole());
                columns[index].data.setDouble("heat", rbmk.heat);
                columns[index].data.setDouble("maxHeat", rbmk.maxHeat());
                if (rbmk.isModerated()) columns[index].data.setBoolean("moderated", true);

                if (te instanceof TileEntityRBMKRod fuel) {
                    flux += fuel.lastFluxQuantity;
                }
            } else {
                columns[index] = null;
            }
        }

        for (int i = 0; i < this.fluxBuffer.length - 1; i++) {
            this.fluxBuffer[i] = this.fluxBuffer[i + 1];
        }
        this.fluxBuffer[this.fluxBuffer.length - 1] = (int) flux;
    }

    @SuppressWarnings("incomplete-switch")
    private void prepareScreenInfo() {
        for (RBMKScreen screen : this.screens) {
            if (screen.type == ScreenType.NONE) {
                screen.display = null;
                continue;
            }

            double value = 0;
            int count = 0;

            for (Integer i : screen.columns) {
                RBMKColumn col = this.columns[i];
                if (col == null) continue;

                switch (screen.type) {
                    case COL_TEMP:
                        count++;
                        value += col.data.getDouble("heat");
                        break;
                    case FUEL_DEPLETION:
                        if (col.data.hasKey("enrichment")) {
                            count++;
                            value += (100D - (col.data.getDouble("enrichment") * 100D));
                        }
                        break;
                    case FUEL_POISON:
                        if (col.data.hasKey("xenon")) {
                            count++;
                            value += col.data.getDouble("xenon");
                        }
                        break;
                    case FUEL_TEMP:
                        if (col.data.hasKey("c_heat")) {
                            count++;
                            value += col.data.getDouble("c_heat");
                        }
                        break;
                    case ROD_EXTRACTION:
                        if (col.data.hasKey("level")) {
                            count++;
                            value += col.data.getDouble("level") * 100;
                        }
                        break;
                }
            }

            double result = value / (double) count;
            String text = ((int) (result * 10)) / 10D + "";

            text = switch (screen.type) {
                case COL_TEMP -> "rbmk.screen.temp=" + text + "°C";
                case FUEL_DEPLETION -> "rbmk.screen.depletion=" + text + "%";
                case FUEL_POISON -> "rbmk.screen.xenon=" + text + "%";
                case FUEL_TEMP -> "rbmk.screen.core=" + text + "°C";
                case ROD_EXTRACTION -> "rbmk.screen.rod=" + text + "%";
                default -> text;
            };

            screen.display = text;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        if (this.world.getTotalWorldTime() % 10 == 0) {
            buf.writeBoolean(true);

            for (RBMKColumn column : this.columns) {
                if (column == null || column.type == null) {
                    buf.writeByte(-1);
                } else {
                    buf.writeByte((byte) column.type.ordinal());
                    BufferUtil.writeNBT(buf, column.data);
                }
            }

            BufferUtil.writeIntArray(buf, fluxBuffer);

            for (RBMKScreen screen : this.screens) {
                BufferUtil.writeString(buf, screen.display);
            }
        } else {
            buf.writeBoolean(false);
            for (RBMKScreen screen : screens) {
                buf.writeByte((byte) screen.type.ordinal());
            }
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        if (buf.readBoolean()) {
            for (int i = 0; i < this.columns.length; i++) {
                byte ordinal = buf.readByte();
                if (ordinal == -1) {
                    this.columns[i] = null;
                } else {
                    this.columns[i] = new RBMKColumn(ColumnType.VALUES[ordinal], BufferUtil.readNBT(buf));
                }
            }

            this.fluxBuffer = BufferUtil.readIntArray(buf);

            for (RBMKScreen screen : this.screens) {
                screen.display = BufferUtil.readString(buf);
            }
        } else {
            for (RBMKScreen screen : this.screens) {
                screen.type = ScreenType.VALUES[buf.readByte()];
            }
        }
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return new Vec3d(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("level")) {
            Set<String> keys = data.getKeySet();
            for (String key : keys) {
                if (key.startsWith("sel_")) {
                    int index = data.getInteger(key);
                    int x = getXFromIndex(index);
                    int z = getZFromIndex(index);

                    TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + z));
                    if (te instanceof TileEntityRBMKControlManual rod) {
                        rod.startingLevel = rod.level;
                        rod.setTarget(MathHelper.clamp(data.getDouble("level"), 0, 1));
                        te.markDirty();
                    }
                }
            }
        }

        if (data.hasKey("toggle")) {
            int slot = data.getByte("toggle");
            int next = this.screens[slot].type.ordinal() + 1;
            ScreenType type = ScreenType.VALUES[next % ScreenType.VALUES.length];
            this.screens[slot].type = type;
        }

        if (data.hasKey("id")) {
            int slot = data.getByte("id");
            List<Integer> list = new ArrayList<>();

            for (int i = 0; i < 15 * 15; i++) {
                if (data.getBoolean("s" + i)) {
                    list.add(i);
                }
            }

            Integer[] cols = list.toArray(new Integer[0]);
            this.screens[slot].columns = cols;
        }

        if (data.hasKey("assignColor")) {
            int color = data.getByte("assignColor");
            int[] cols = data.getIntArray("cols");

            for (int i : cols) {
                int x = getXFromIndex(i);
                int z = getZFromIndex(i);

                TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + z));

                if (te instanceof TileEntityRBMKControlManual rod) {
                    rod.color = EnumUtil.grabEnumSafely(RBMKColor.VALUES, color);
                    te.markDirty();
                }
            }
        }

        if (data.hasKey("compressor")) {
            int[] cols = data.getIntArray("cols");

            for (int i : cols) {
                int x = getXFromIndex(i);
                int z = getZFromIndex(i);

                TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + z));

                if (te instanceof TileEntityRBMKBoiler boiler) {
                    boiler.cyceCompressor();
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 4, pos.getZ() + 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    public void setTarget(int x, int y, int z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.targetX = nbt.getInteger("tX");
        this.targetY = nbt.getInteger("tY");
        this.targetZ = nbt.getInteger("tZ");

        for (int i = 0; i < this.screens.length; i++) {
            this.screens[i].type = ScreenType.VALUES[nbt.getByte("t" + i)];
            this.screens[i].columns = Arrays.stream(nbt.getIntArray("s" + i)).boxed().toArray(Integer[]::new);
        }
        rotation = nbt.getByte("rotation");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("tX", this.targetX);
        nbt.setInteger("tY", this.targetY);
        nbt.setInteger("tZ", this.targetZ);

        for (int i = 0; i < this.screens.length; i++) {
            nbt.setByte("t" + i, (byte) this.screens[i].type.ordinal());
            nbt.setIntArray("s" + i, Arrays.stream(this.screens[i].columns).mapToInt(Integer::intValue).toArray());
        }
        nbt.setByte("rotation", rotation);

        return nbt;
    }

    public void rotate() {
        rotation = (byte) ((rotation + 1) % 4);
    }

    public int getXFromIndex(int col) {
        final int i = col % 15 - 7;
        final int j = col / 15 - 7;
        return switch (rotation) {
            case 0 -> // 0°
                    i;
            case 1 -> // 90°
                    -j;
            case 2 -> // 180°
                    -i;
            case 3 -> // 270°
                    j;
            default -> i;
        };
    }

    public int getZFromIndex(int col) {
        final int i = col % 15 - 7;
        final int j = col / 15 - 7;
        return switch (rotation) {
            case 0 -> // 0°
                    j;
            case 1 -> // 90°
                    i;
            case 2 -> // 180°
                    -j;
            case 3 -> // 270°
                    -i;
            default -> j;
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRBMKConsole(player.inventory, this);
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "rbmk_console";
    }

    @Callback(direct = true, doc = "getColumnData(x:int, y:int)")
    @Optional.Method(modid = "opencomputers")
    public Object[] getColumnData(Context context, Arguments args) {
        int x = args.checkInteger(0) - 7;
        int y = -args.checkInteger(1) + 7;

        int i = x;
        int j = y;
        switch (rotation) {
            case 0:
                break;
            case 1:
                i = y;
                j = -x;
                break;
            case 2:
                i = -x;
                j = -y;
                break;
            case 3:
                i = -y;
                j = x;
                break;
        }
        int index = (j + 7) * 15 + (i + 7);

        TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + y));
        if (te instanceof TileEntityRBMKBase column) {

            NBTTagCompound column_data = columns[index].data;
            LinkedHashMap<String, Object> data_table = new LinkedHashMap<>();
            data_table.put("type", column.getConsoleType().name());
            data_table.put("hullTemp", column_data.getDouble("heat"));
            data_table.put("realSimWater", column_data.getDouble("water"));
            data_table.put("realSimSteam", column_data.getDouble("steam"));
            data_table.put("moderated", column_data.getBoolean("moderated"));
            data_table.put("level", column_data.getDouble("level"));
            data_table.put("color", column_data.getShort("color"));
            data_table.put("enrichment", column_data.getDouble("enrichment"));
            data_table.put("xenon", column_data.getDouble("xenon"));
            data_table.put("coreSkinTemp", column_data.getDouble("c_heat"));
            data_table.put("coreTemp", column_data.getDouble("c_coreHeat"));
            data_table.put("coreMaxTemp", column_data.getDouble("c_maxHeat"));

            if (te instanceof TileEntityRBMKRod fuelChannel) {
                data_table.put("fluxQuantity", fuelChannel.lastFluxQuantity);
                data_table.put("fluxRatio", fuelChannel.fluxFastRatio);
            }

            if (te instanceof TileEntityRBMKBoiler boiler) {
                data_table.put("water", boiler.feed.getFill());
                data_table.put("steam", boiler.steam.getFill());
            }

            if (te instanceof TileEntityRBMKOutgasser irradiationChannel) {
                data_table.put("fluxProgress", irradiationChannel.progress);
                data_table.put("requiredFlux", irradiationChannel.duration);
            }
            if(te instanceof TileEntityRBMKCooler coolingChannel){
                data_table.put("degreesCooledPerTick", coolingChannel.lastCooled);
                data_table.put("cryogel", coolingChannel.tank.getFluidAmount());
            }
            if (te instanceof TileEntityRBMKHeater heaterChannel) {
                data_table.put("coolant", heaterChannel.feed.getFill());
                data_table.put("hotcoolant", heaterChannel.steam.getFill());
            }

            return new Object[]{data_table};
        }
        return new Object[]{null};
    }

    @Callback(direct = true, doc = "getRBMKPos()")
    @Optional.Method(modid = "opencomputers")
    public Object[] getRBMKPos(Context context, Arguments args) {
        if (!(targetX == 0 && targetY == 0 && targetZ == 0)) {
            LinkedHashMap<String, Integer> data_table = new LinkedHashMap<>();
            data_table.put("rbmkCenterX", targetX);
            data_table.put("rbmkCenterY", targetY);
            data_table.put("rbmkCenterZ", targetZ);
            return new Object[]{data_table};
        }
        return new Object[]{null};
    }

    @Callback(direct = true, doc = "setLevel(level:double)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setLevel(Context context, Arguments args) {
        double new_level = args.checkDouble(0);
        boolean foundRods = false;
        for (int i = -7; i <= 7; i++) {
            for (int j = -7; j <= 7; j++) {
                TileEntity te = world.getTileEntity(new BlockPos(targetX + i, targetY, targetZ + j));
                if (te instanceof TileEntityRBMKControlManual rod) {
                    rod.startingLevel = rod.level;
                    new_level = Math.min(1, Math.max(0, new_level));
                    rod.setTarget(new_level);
                    te.markDirty();
                    foundRods = true;
                }
            }
        }
        if (foundRods) return new Object[]{};
        return new Object[]{"No control rods found"};
    }

    // opencomputers interface

    @Callback(direct = true, doc = "setColumnLevel(x:int, y:int, level:double)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setColumnLevel(Context context, Arguments args) {
        int x = args.checkInteger(0) - 7;
        int y = -args.checkInteger(1) + 7;
        double new_level = args.checkDouble(2);

        TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + y));
        if (te instanceof TileEntityRBMKControlManual rod) {
            rod.startingLevel = rod.level;
            new_level = Math.min(1, Math.max(0, new_level));
            rod.setTarget(new_level);
            te.markDirty();
            return new Object[]{};
        }
        return new Object[]{"No control rod found at " + (x + 7) + "," + (7 - y)};
    }

    @Callback(direct = true, doc = "setColorLevel(color:int, level:double)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setColorLevel(Context context, Arguments args) {
        int color = args.checkInteger(0);
        double new_level = args.checkDouble(1);
        boolean foundRods = false;
        if (color >= 0 && color <= 4) {
            for (int i = -7; i <= 7; i++) {
                for (int j = -7; j <= 7; j++) {
                    TileEntity te = world.getTileEntity(new BlockPos(targetX + i, targetY, targetZ + j));
                    if (te instanceof TileEntityRBMKControlManual rod) {
                        if (rod.color == RBMKColor.VALUES[color]) {
                            rod.startingLevel = rod.level;
                            new_level = Math.min(1, Math.max(0, new_level));
                            rod.setTarget(new_level);
                            te.markDirty();
                            foundRods = true;
                        }
                    }
                }
            }
            if (foundRods) return new Object[]{};
            return new Object[]{"No rods for color " + color + " found"};
        }
        return new Object[]{"Color " + color + " does not exist"};
    }

    @Callback(direct = true, doc = "setColor(x:int, y:int, color:int)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setColor(Context context, Arguments args) {
        int x = args.checkInteger(0) - 7;
        int y = -args.checkInteger(1) + 7;
        int new_color = args.checkInteger(2);
        if (new_color >= 0 && new_color <= 4) {
            TileEntity te = world.getTileEntity(new BlockPos(targetX + x, targetY, targetZ + y));
            if (te instanceof TileEntityRBMKControlManual rod) {
                rod.color = RBMKColor.VALUES[new_color];
                te.markDirty();
                return new Object[]{};
            }
            return new Object[]{"No control rod found at " + (x + 7) + "," + (7 - y)};
        }
        return new Object[]{"Color " + new_color + " does not exist"};
    }

    @Callback(direct = true, doc = "pressAZ5()")
    @Optional.Method(modid = "opencomputers")
    public Object[] pressAZ5(Context context, Arguments args) {
        boolean hasRods = false;
        for (int i = -7; i <= 7; i++) {
            for (int j = -7; j <= 7; j++) {
                TileEntity te = world.getTileEntity(new BlockPos(targetX + i, targetY, targetZ + j));
                if (te instanceof TileEntityRBMKControlManual rod) {
                    rod.startingLevel = rod.level;
                    rod.setTarget(0);
                    te.markDirty();
                    hasRods = true;
                }
            }
        }
        if (hasRods) return new Object[]{};
        return new Object[]{"No control rods found"};
    }

    public enum ColumnType {
        BLANK(0), FUEL(10), FUEL_SIM(90), CONTROL(20), CONTROL_AUTO(30), BOILER(40), MODERATOR(50), ABSORBER(60), REFLECTOR(70), OUTGASSER(80), BREEDER(100), STORAGE(110), COOLER(120), HEATEX(130);

        public static final ColumnType[] VALUES = values();

        public final int offset;

        ColumnType(int offset) {
            this.offset = offset;
        }
    }

    public enum ScreenType {
        NONE(0), COL_TEMP(18), ROD_EXTRACTION(2 * 18), FUEL_DEPLETION(3 * 18), FUEL_POISON(4 * 18), FUEL_TEMP(5 * 18);

        public static final ScreenType[] VALUES = values();

        public final int offset;

        ScreenType(int offset) {
            this.offset = offset;
        }
    }

    public static class RBMKColumn {
        public ColumnType type;
        public NBTTagCompound data;

        public RBMKColumn(ColumnType type) {
            this.type = type;
        }

        RBMKColumn(ColumnType type, NBTTagCompound data) {
            this.type = type;
            this.data = (data != null) ? data : new NBTTagCompound();
        }

        @SuppressWarnings("incomplete-switch")
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            if (this.data == null) return null;

            List<String> stats = new ArrayList<>();
            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.heat", ((int) ((this.data.getDouble("heat") * 10D)) / 10D) + "°C"));

            /*
             * Making a big switch with the values converted based on type by hand might seem "UnPrOfEsSiOnAl" and a major pain in the ass
             * but my only other solution that would not have me do things in multiple places where they shouldn't be involved passing
             * classes in the enum and then calling a special method from that class and quite honestly it turned out to be such a crime
             * against humanity that I threw the towel. It's not fancy, I get that, please fuck off.
             */

            switch (this.type) {
                case FUEL:
                case FUEL_SIM:
                    if (this.data.hasKey("rod_name"))
                        stats.add("§3" + I18n.format("rbmk.rod.name") + " " + I18n.format(this.data.getString("rod_name") + ".name"));
                    else stats.add("§3" + I18n.format("rbmk.rod.name"));
                    stats.add(TextFormatting.GREEN + I18nUtil.resolveKey("rbmk.rod.depletion", ((int) (((1D - this.data.getDouble("enrichment")) * 100000)) / 1000D) + "%"));
                    stats.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("rbmk.rod.xenon", ((int) (((this.data.getDouble("xenon")) * 1000D)) / 1000D) + "%"));
                    stats.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("rbmk.rod.coreTemp", ((int) ((this.data.getDouble("c_coreHeat") * 10D)) / 10D) + "°C"));
                    stats.add(TextFormatting.RED + I18nUtil.resolveKey("rbmk.rod.skinTemp", ((int) ((this.data.getDouble("c_heat") * 10D)) / 10D) + "°C", ((int) ((this.data.getDouble("c_maxHeat") * 10D)) / 10D) + "°C"));
                    break;

                case BOILER:
                    stats.add(TextFormatting.BLUE + I18nUtil.resolveKey("rbmk.boiler.water", this.data.getInteger("water"), this.data.getInteger("maxWater")));
                    stats.add(TextFormatting.WHITE + I18nUtil.resolveKey("rbmk.boiler.steam", this.data.getInteger("steam"), this.data.getInteger("maxSteam")));
                    stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.boiler.type", Fluids.fromID(this.data.getShort("type")).getLocalizedName()));
                    break;

                case CONTROL:
                    if (this.data.hasKey("color")) {
                        short col = this.data.getShort("color");
                        if (col >= 0 && col < RBMKColor.VALUES.length) {
                            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.control." + RBMKColor.VALUES[col].name().toLowerCase(Locale.US)));
                        }
                    }
                case CONTROL_AUTO:
                    stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.control.level", ((int) ((this.data.getDouble("level") * 100D))) + "%"));
                    break;

                case HEATEX:
                    stats.add(TextFormatting.BLUE + Fluids.fromID(this.data.getShort("type")).getLocalizedName() + " " + this.data.getInteger("water") + "/" + this.data.getInteger("maxWater") + "mB");
                    stats.add(TextFormatting.RED + Fluids.fromID(this.data.getShort("hottype")).getLocalizedName() + " " + this.data.getInteger("steam") + "/" + this.data.getInteger("maxSteam") + "mB");
                    break;
                case COOLER:
                    stats.add(TextFormatting.AQUA + I18nUtil.resolveKey("rbmk.cooler.cooling", this.data.getInteger("cooled") * 20));
                    stats.add(TextFormatting.DARK_AQUA + I18nUtil.resolveKey("rbmk.cooler.cryo", this.data.getInteger("cryo")));
                    break;
                case OUTGASSER:
                    double flux = this.data.getDouble("usedFlux");
                    double progress = this.data.getDouble("progress");
                    double maxProgress = this.data.getDouble("maxProgress");
                    int eta = 0;
                    if (flux > 0) eta = (int) ((maxProgress - progress) / flux);
                    stats.add(TextFormatting.GOLD + I18nUtil.resolveKey("rbmk.outgasser.eta", BobMathUtil.toDate(BobMathUtil.ticksToDate(eta, 72000))));
                    stats.add(TextFormatting.AQUA + I18nUtil.resolveKey("rbmk.outgasser.flux", Library.getShortNumber((long) flux)));
                    stats.add(TextFormatting.DARK_AQUA + I18nUtil.resolveKey("rbmk.outgasser.progress", Library.getShortNumber((long) progress), Library.getShortNumber((long) maxProgress), Library.getPercentage(progress / maxProgress)));
                    stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.outgasser.gas", this.data.getInteger("gas"), this.data.getInteger("maxGas")));
                    break;
            }

            if (data.getBoolean("moderated")) {
                stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.moderated"));
            }

            return stats;
        }
    }

    public static class RBMKScreen {
        public ScreenType type = ScreenType.NONE;
        public Integer[] columns = new Integer[0];
        public String display = null;

        RBMKScreen() {
        }

        RBMKScreen(ScreenType type, Integer[] columns, String display) {
            this.type = type;
            this.columns = columns;
            this.display = display;
        }
    }
}
