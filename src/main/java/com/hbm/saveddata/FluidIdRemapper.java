package com.hbm.saveddata;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.world.phased.PhasedStructureRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

// wrapper around PhasedStructureRegistry to handle fluid ID remapping
@NotNullByDefault
public final class FluidIdRemapper {
    private static @Nullable Int2ObjectOpenHashMap<@UnknownNullability FluidType> stringIdToFluid;
    private static @Nullable Reference2IntOpenHashMap<@UnknownNullability FluidType> fluidToStringId;

    private FluidIdRemapper() {
    }

    public static void onServerStarting() {
        FluidType[] allFluids = Fluids.getAll();
        fluidToStringId = new Reference2IntOpenHashMap<>(allFluids.length);
        fluidToStringId.defaultReturnValue(-1);
        stringIdToFluid = new Int2ObjectOpenHashMap<>(allFluids.length);
        for (FluidType fluid : allFluids) {
            int stringId = PhasedStructureRegistry.getStringId(fluid.getName());
            if (stringId < 0) throw new IllegalStateException("FluidIdRemapper#onServerStarting called before PhasedStructureRegistry#onServerStarting");
            fluidToStringId.put(fluid, stringId);
            stringIdToFluid.put(stringId, fluid);
        }
    }

    public static void onServerStopped() {
        stringIdToFluid = null;
        fluidToStringId = null;
    }

    public static int getStringId(FluidType fluid) {
        if (fluidToStringId == null) throw new IllegalStateException("FluidIdRemapper#getStringId called before initialization");
        return fluidToStringId.getInt(fluid);
    }

    public static FluidType getFluid(int stringId) {
        if (stringIdToFluid == null) throw new IllegalStateException("FluidIdRemapper#getStringId called before initialization");
        FluidType fluid = stringIdToFluid.get(stringId);
        return fluid == null ? Fluids.NONE : fluid;
    }

    // pre-65f3ecab3ee722b56fbc382f4b7506644d5cbbb9 order
    private static final String[] LEGACY_ORDER = {
            "NONE",                     // 0
            "WATER",                    // 1
            "STEAM",                    // 2
            "HOTSTEAM",                 // 3
            "SUPERHOTSTEAM",            // 4
            "ULTRAHOTSTEAM",            // 5
            "COOLANT",                  // 6
            "LAVA",                     // 7
            "DEUTERIUM",                // 8
            "TRITIUM",                  // 9
            "OIL",                      // 10
            "HOTOIL",                   // 11
            "HEAVYOIL",                 // 12
            "BITUMEN",                  // 13
            "SMEAR",                    // 14
            "HEATINGOIL",               // 15
            "RECLAIMED",                // 16
            "PETROIL",                  // 17
            "LUBRICANT",                // 18
            "NAPHTHA",                  // 19
            "DIESEL",                   // 20
            "LIGHTOIL",                 // 21
            "KEROSENE",                 // 22
            "GAS",                      // 23
            "PETROLEUM",                // 24
            "LPG",                      // 25
            "BIOGAS",                   // 26
            "BIOFUEL",                  // 27
            "NITAN",                    // 28
            "UF6",                      // 29
            "PUF6",                     // 30
            "SAS3",                     // 31
            "SCHRABIDIC",               // 32
            "AMAT",                     // 33
            "ASCHRAB",                  // 34
            "PEROXIDE",                 // 35
            "WATZ",                     // 36
            "CRYOGEL",                  // 37
            "HYDROGEN",                 // 38
            "OXYGEN",                   // 39
            "XENON",                    // 40
            "BALEFIRE",                 // 41
            "MERCURY",                  // 42
            "PAIN",                     // 43
            "WASTEFLUID",               // 44
            "WASTEGAS",                 // 45
            "GASOLINE",                 // 46
            "COALGAS",                  // 47
            "SPENTSTEAM",               // 48
            "FRACKSOL",                 // 49
            "PLASMA_DT",                // 50
            "PLASMA_HD",                // 51
            "PLASMA_HT",                // 52
            "PLASMA_XM",                // 53
            "PLASMA_BF",                // 54
            "CARBONDIOXIDE",            // 55
            "PLASMA_DH3",               // 56
            "HELIUM3",                  // 57
            "DEATH",                    // 58
            "ETHANOL",                  // 59
            "HEAVYWATER",               // 60
            "CRACKOIL",                 // 61
            "COALOIL",                  // 62
            "HOTCRACKOIL",              // 63
            "NAPHTHA_CRACK",            // 64
            "LIGHTOIL_CRACK",           // 65
            "DIESEL_CRACK",             // 66
            "AROMATICS",                // 67
            "UNSATURATEDS",             // 68
            "SALIENT",                  // 69
            "XPJUICE",                  // 70
            "ENDERJUICE",               // 71
            "PETROIL_LEADED",           // 72
            "GASOLINE_LEADED",          // 73
            "COALGAS_LEADED",           // 74
            "SULFURIC_ACID",            // 75
            "COOLANT_HOT",              // 76
            "MUG",                      // 77
            "MUG_HOT",                  // 78
            "WOODOIL",                  // 79
            "COALCREOSOTE",             // 80
            "SEEDSLURRY",               // 81
            "NITROGEN",                 // 82
            "BLOOD",                    // 83
            "NITRIC_ACID",              // 84
            "AMMONIA",                  // 85
            "HYDRAZINE",                // 86
            "BLOODGAS",                 // 87
            "SODIUM_ALUMINATE",         // 88
            "AIR",                      // 89
            "BLOOD_HOT",                // 90
            "SOLVENT",                  // 91
            "HCL",                      // 92
            "MINSOL",                   // 93
            "SYNGAS",                   // 94
            "OXYHYDROGEN",              // 95
            "RADIOSOLVENT",             // 96
            "CHLORINE",                 // 97
            "HEAVYOIL_VACUUM",          // 98
            "REFORMATE",                // 99
            "LIGHTOIL_VACUUM",          // 100
            "SOURGAS",                  // 101
            "XYLENE",                   // 102
            "NEON",                     // 103
            "ARGON",                    // 104
            "KRYPTON",                  // 105
            "COFFEE",                   // 106
            "TEA",                      // 107
            "HONEY",                    // 108
            "HEATINGOIL_VACUUM",        // 109
            "DIESEL_REFORM",            // 110
            "DIESEL_CRACK_REFORM",      // 111
            "KEROSENE_REFORM",          // 112
            "REFORMGAS",                // 113
            "MILK",                     // 114
            "SMILK",                    // 115
            "OLIVEOIL",                 // 116
            "COLLOID",                  // 117
            "EVEAIR",                   // 118
            "KMnO4",                    // 119
            "CHLOROMETHANE",            // 120
            "METHANOL",                 // 121
            "BROMINE",                  // 122
            "METHYLENE",                // 123
            "POLYTHYLENE",              // 124
            "FLUORINE",                 // 125
            "TEKTOAIR",                 // 126
            "PHOSGENE",                 // 127
            "MUSTARDGAS",               // 128
            "IONGEL",                   // 129
            "ELBOWGREASE",              // 130
            "NMASSTETRANOL",            // 131
            "NMASS",                    // 132
            "SCUTTERBLOOD",             // 133
            "HTCO4",                    // 134
            "OIL_COKER",                // 135
            "NAPHTHA_COKER",            // 136
            "GAS_COKER",                // 137
            "EGG",                      // 138
            "CHOLESTEROL",              // 139
            "ESTRADIOL",                // 140
            "FISHOIL",                  // 141
            "SUNFLOWEROIL",             // 142
            "NITROGLYCERIN",            // 143
            "REDMUD",                   // 144
            "CHLOROCALCITE_SOLUTION",   // 145
            "CHLOROCALCITE_MIX",        // 146
            "CHLOROCALCITE_CLEANED",    // 147
            "POTASSIUM_CHLORIDE",       // 148
            "CALCIUM_CHLORIDE",         // 149
            "CALCIUM_SOLUTION",         // 150
            "SMOKE",                    // 151
            "SMOKE_LEADED",             // 152
            "SMOKE_POISON",             // 153
            "JOOLGAS",                  // 154
            "SARNUSGAS",                // 155
            "UGAS",                     // 156
            "NGAS",                     // 157
            "EMILK",                    // 158
            "CMILK",                    // 159
            "CREAM",                    // 160
            "MORKITE",                  // 161
            "DICYANOACETYLENE",         // 162
            "MORKINE",                  // 163
            "MSLURRY",                  // 164
            "HELIUM4",                  // 165
            "HEAVYWATER_HOT",           // 166
            "SODIUM",                   // 167
            "SODIUM_HOT",               // 168
            "THORIUM_SALT",             // 169
            "THORIUM_SALT_HOT",         // 170
            "THORIUM_SALT_DEPLETED",    // 171
            "FULLERENE",                // 172
            "PHEROMONE",                // 173
            "PHEROMONE_M",              // 174
            "OIL_DS",                   // 175
            "HOTOIL_DS",                // 176
            "CRACKOIL_DS",              // 177
            "HOTCRACKOIL_DS",           // 178
            "NAPHTHA_DS",               // 179
            "LIGHTOIL_DS",              // 180
            "STELLAR_FLUX",             // 181
            "DUNAAIR",                  // 182
            "VITRIOL",                  // 183
            "SLOP",                     // 184
            "SUPERHEATED_HYDROGEN",     // 185
            "LEAD",                     // 186
            "LEAD_HOT",                 // 187
            "GAS_WATZ",                 // 188
            "URANIUM_BROMIDE",          // 189
            "PLUTONIUM_BROMIDE",        // 190
            "SCHRABIDIUM_BROMIDE",      // 191
            "THORIUM_BROMIDE",          // 192
            "GASEOUS_URANIUM_BROMIDE",  // 193
            "GASEOUS_PLUTONIUM_BROMIDE",// 194
            "GASEOUS_SCHRABIDIUM_BROMIDE", // 195
            "GASEOUS_THORIUM_BROMIDE",  // 196
            "GASEOUS_HYDROGEN",         // 197
            "PERFLUOROMETHYL",          // 198
            "PERFLUOROMETHYL_COLD",     // 199
            "PERFLUOROMETHYL_HOT",      // 200
            "LYE",                      // 201
            "BAUXITE_SOLUTION",         // 202
            "ALUMINA",                  // 203
            "CONCRETE",                 // 204
            "DHC"                       // 205
    };

    @NotNull
    public static FluidType remapLegacyId(int oldId) {
        if (oldId < 0) throw new ArrayIndexOutOfBoundsException(oldId);
        if (oldId >= LEGACY_ORDER.length) return Fluids.fromID(oldId); // probably addon fluids, assume they have set the id high enough
        return Fluids.fromName(LEGACY_ORDER[oldId]);
    }
}
