package com.hbm.config;

import com.hbm.interfaces.IDoor;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.Locale;

public class MachineConfig {

    protected static boolean scaleRTGPower = false;
    protected static boolean doRTGsDecay = true;
    protected static boolean disableMachines = false;
    public static boolean holdDoorRedstone = false;
    public static int crateByteSize = 8192;
    public static int rbmkJumpTemp = 1250;
    public static HashMap<String, IDoor.Mode> doorConf = new HashMap<>();

    public static void loadFromConfig(Configuration config) {

        final String CATEGORY_MACHINE = CommonConfig.CATEGORY_MACHINES;

        scaleRTGPower = CommonConfig.createConfigBool(config, CATEGORY_MACHINE, "9.01_scaleRTGPower", "Should RTG/Betavoltaic fuel power scale down as it decays?", false);
        doRTGsDecay = CommonConfig.createConfigBool(config, CATEGORY_MACHINE, "9.02_doRTGsDecay", "Should RTG/Betavoltaic fuel decay at all?", true);
        disableMachines = CommonConfig.createConfigBool(config, CATEGORY_MACHINE, "9.00_disableMachines", "Prevent mod from registering any Machines? (WARNING: THIS WILL BREAK PREEXISTING WORLDS)", false);
        holdDoorRedstone = CommonConfig.createConfigBool(config, CATEGORY_MACHINE, "9.99_CE_03_holdDoorRedstone", "Whether the door requires a continuous redstone signal to stay open, or toggles on each activation.", false);
        crateByteSize = CommonConfig.createConfigInt(config, CATEGORY_MACHINE, "9.99_CE_04_crateByteSize", "Maximum compressed NBT size, in bytes, that a crate or lead box may carry as an item. Contents exceeding this are dropped instead of stored, which keeps oversized crates from being sent over the network. 1.7 hardcodes this to 6144.", 8192);

        doorConf.clear();
        String[] doorConfStr = config.get(CATEGORY_MACHINE, "9.99_CE_02_doorConf", new String[]{},
                                             "Configuration for door modes. Format: 'modid:door_name:MODE' (e.g. 'hbm:vault_door:REDSTONE') or 'ALL:MODE'. Modes: DEFAULT, TOOLABLE, REDSTONE")
                                     .getStringList();
        for (String entry : doorConfStr) {
            if (entry == null) continue;
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) continue;
            String[] split = trimmed.split(":");
            try {
                if (split.length == 2 && "ALL".equalsIgnoreCase(split[0])) {
                    IDoor.Mode mode = IDoor.Mode.valueOf(split[1].toUpperCase(Locale.ROOT));
                    doorConf.put("ALL", mode);
                } else if (split.length == 3) {
                    IDoor.Mode mode = IDoor.Mode.valueOf(split[2].toUpperCase(Locale.ROOT));
                    doorConf.put(split[0] + ":" + split[1], mode);
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid modes
            }
        }
    }
}
