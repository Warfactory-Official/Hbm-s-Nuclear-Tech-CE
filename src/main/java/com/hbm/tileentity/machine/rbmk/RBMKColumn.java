package com.hbm.tileentity.machine.rbmk;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlManual.RBMKColor;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Polymorphic column data classes for RBMK Console.
 * These replace the old NBT-based RBMKColumn system with strictly typed structs.
 */
public abstract class RBMKColumn {
    public double heat;
    public double maxHeat;
    public boolean moderated;
    public int reasimWater;
    public int reasimSteam;

    public final @NotNull ColumnType type;

    protected RBMKColumn(ColumnType type) {
        this.type = type;
    }

    protected void serialize(ByteBuf buf) {
        buf.writeDouble(heat);
        buf.writeDouble(maxHeat);
        buf.writeBoolean(moderated);
        buf.writeInt(reasimWater);
        buf.writeInt(reasimSteam);
    }

    protected void deserialize(ByteBuf buf) {
        heat = buf.readDouble();
        maxHeat = buf.readDouble();
        moderated = buf.readBoolean();
        reasimWater = buf.readInt();
        reasimSteam = buf.readInt();
    }

    @SideOnly(Side.CLIENT)
    public List<String> getFancyStats() {
        List<String> stats = new ArrayList<>();
        stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.heat", ((int) (heat * 10D)) / 10D + "°C"));

        if (moderated) {
            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.moderated"));
        }

        return stats;
    }

    public static RBMKColumn readFromBuf(ByteBuf buf) {
        byte ordinal = buf.readByte();
        if (ordinal == -1) {
            return null;
        }
        ColumnType type = ColumnType.VALUES[ordinal];
        RBMKColumn column = createForType(type);
        column.deserialize(buf);
        return column;
    }

    public static void writeToBuf(ByteBuf buf, RBMKColumn column) {
        if (column == null) {
            buf.writeByte(-1);
        } else {
            buf.writeByte((byte) column.type.ordinal());
            column.serialize(buf);
        }
    }

    public static RBMKColumn createForType(ColumnType type) {
        return switch (type) {
            case FUEL, FUEL_SIM, BREEDER -> new FuelColumn(type);
            case BOILER -> new BoilerColumn();
            case CONTROL, CONTROL_AUTO -> new ControlColumn(type);
            case COOLER -> new CoolerColumn();
            case OUTGASSER -> new OutgasserColumn();
            case HEATEX -> new HeaterColumn();
            default -> new StandardColumn(type);
        };
    }

    public enum ColumnType {
        BLANK(0), FUEL(10), FUEL_SIM(90), CONTROL(20), CONTROL_AUTO(30), BOILER(40),
        MODERATOR(50), ABSORBER(60), REFLECTOR(70), OUTGASSER(80), BREEDER(100),
        STORAGE(110), COOLER(120), HEATEX(130);

        public static final ColumnType[] VALUES = values();

        public final int offset;

        ColumnType(int offset) {
            this.offset = offset;
        }
    }

    public static class StandardColumn extends RBMKColumn {
        public StandardColumn(ColumnType type) {
            super(type);
        }
    }

    public static class FuelColumn extends RBMKColumn {
        public double enrichment;
        public double xenon;
        public double c_coreHeat;
        public double c_heat;
        public double c_maxHeat;

        public FuelColumn(ColumnType type) {
            super(type);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeDouble(enrichment);
            buf.writeDouble(xenon);
            buf.writeDouble(c_coreHeat);
            buf.writeDouble(c_heat);
            buf.writeDouble(c_maxHeat);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            enrichment = buf.readDouble();
            xenon = buf.readDouble();
            c_coreHeat = buf.readDouble();
            c_heat = buf.readDouble();
            c_maxHeat = buf.readDouble();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            stats.add(TextFormatting.GREEN + I18nUtil.resolveKey("rbmk.rod.depletion", ((int) (((1D - enrichment) * 100000)) / 1000D) + "%"));
            stats.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("rbmk.rod.xenon", ((int) ((xenon * 1000D)) / 1000D) + "%"));
            stats.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("rbmk.rod.coreTemp", ((int) (c_coreHeat * 10D)) / 10D + "°C"));
            stats.add(TextFormatting.RED + I18nUtil.resolveKey("rbmk.rod.skinTemp", ((int) (c_heat * 10D)) / 10D + "°C", ((int) (c_maxHeat * 10D)) / 10D + "°C"));
            return stats;
        }
    }

    public static class BoilerColumn extends RBMKColumn {
        public int water;
        public int maxWater;
        public int steam;
        public int maxSteam;
        public short steamType;

        public BoilerColumn() {
            super(ColumnType.BOILER);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeInt(water);
            buf.writeInt(maxWater);
            buf.writeInt(steam);
            buf.writeInt(maxSteam);
            buf.writeShort(steamType);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            water = buf.readInt();
            maxWater = buf.readInt();
            steam = buf.readInt();
            maxSteam = buf.readInt();
            steamType = buf.readShort();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            stats.add(TextFormatting.BLUE + I18nUtil.resolveKey("rbmk.boiler.water", water, maxWater));
            stats.add(TextFormatting.WHITE + I18nUtil.resolveKey("rbmk.boiler.steam", steam, maxSteam));
            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.boiler.type", Fluids.fromID(steamType).getLocalizedName()));
            return stats;
        }
    }

    public static class ControlColumn extends RBMKColumn {
        public double level;
        public short color = -1; // -1 means no color

        public ControlColumn(ColumnType type) {
            super(type);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeDouble(level);
            buf.writeShort(color);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            level = buf.readDouble();
            color = buf.readShort();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            if (color >= 0 && color < RBMKColor.VALUES.length) {
                stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.control." + RBMKColor.VALUES[color].name().toLowerCase(Locale.US)));
            }
            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.control.level", ((int) (level * 100D)) + "%"));
            return stats;
        }
    }

    // ==================== Cooler Column ====================

    public static class CoolerColumn extends RBMKColumn {
        public int cooled;
        public int cryo;

        public CoolerColumn() {
            super(ColumnType.COOLER);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeInt(cooled);
            buf.writeInt(cryo);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            cooled = buf.readInt();
            cryo = buf.readInt();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            stats.add(TextFormatting.AQUA + I18nUtil.resolveKey("rbmk.cooler.cooling", cooled * 20));
            stats.add(TextFormatting.DARK_AQUA + I18nUtil.resolveKey("rbmk.cooler.cryo", cryo));
            return stats;
        }
    }

    public static class OutgasserColumn extends RBMKColumn {
        public int gas;
        public int maxGas;
        public double progress;
        public double maxProgress;
        public double usedFlux;

        public OutgasserColumn() {
            super(ColumnType.OUTGASSER);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeInt(gas);
            buf.writeInt(maxGas);
            buf.writeDouble(progress);
            buf.writeDouble(maxProgress);
            buf.writeDouble(usedFlux);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            gas = buf.readInt();
            maxGas = buf.readInt();
            progress = buf.readDouble();
            maxProgress = buf.readDouble();
            usedFlux = buf.readDouble();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            int eta = 0;
            if (usedFlux > 0) eta = (int) ((maxProgress - progress) / usedFlux);
            stats.add(TextFormatting.GOLD + I18nUtil.resolveKey("rbmk.outgasser.eta", com.hbm.util.BobMathUtil.toDate(com.hbm.util.BobMathUtil.ticksToDate(eta, 72000))));
            stats.add(TextFormatting.AQUA + I18nUtil.resolveKey("rbmk.outgasser.flux", com.hbm.lib.Library.getShortNumber((long) usedFlux)));
            stats.add(TextFormatting.DARK_AQUA + I18nUtil.resolveKey("rbmk.outgasser.progress", com.hbm.lib.Library.getShortNumber((long) progress), com.hbm.lib.Library.getShortNumber((long) maxProgress), com.hbm.lib.Library.getPercentage(progress / maxProgress)));
            stats.add(TextFormatting.YELLOW + I18nUtil.resolveKey("rbmk.outgasser.gas", gas, maxGas));
            return stats;
        }
    }

    public static class HeaterColumn extends RBMKColumn {
        public int water;
        public int maxWater;
        public int steam;
        public int maxSteam;
        public short coldType;
        public short hotType;

        public HeaterColumn() {
            super(ColumnType.HEATEX);
        }

        @Override
        public void serialize(ByteBuf buf) {
            super.serialize(buf);
            buf.writeInt(water);
            buf.writeInt(maxWater);
            buf.writeInt(steam);
            buf.writeInt(maxSteam);
            buf.writeShort(coldType);
            buf.writeShort(hotType);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            super.deserialize(buf);
            water = buf.readInt();
            maxWater = buf.readInt();
            steam = buf.readInt();
            maxSteam = buf.readInt();
            coldType = buf.readShort();
            hotType = buf.readShort();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public List<String> getFancyStats() {
            List<String> stats = super.getFancyStats();
            stats.add(TextFormatting.BLUE + Fluids.fromID(coldType).getLocalizedName() + " " + water + "/" + maxWater + "mB");
            stats.add(TextFormatting.RED + Fluids.fromID(hotType).getLocalizedName() + " " + steam + "/" + maxSteam + "mB");
            return stats;
        }
    }
}
