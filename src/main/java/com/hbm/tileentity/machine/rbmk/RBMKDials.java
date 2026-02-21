package com.hbm.tileentity.machine.rbmk;

import com.hbm.config.GeneralConfig;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class RBMKDials {

    public static void createDials(World world) {
        GameRules rules = world.getGameRules();

        if (!rules.getBoolean(RBMKKeys.KEY_SAVE_DIALS.keyString)) {
            for (RBMKKeys key : RBMKKeys.VALUES) {
                rules.setOrCreateGameRule(key.keyString, String.valueOf(key.defValue));
            }
        }
    }

    /**
     * Returns the amount of heat per tick removed from components passively
     *
     * @param world
     * @return >0
     */
    static double getPassiveCooling(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_PASSIVE_COOLING.keyString),
                (double) RBMKKeys.KEY_PASSIVE_COOLING.defValue), 0.0D);
    }

    /**
     * Returns the percentual step size how quickly neighboring component heat equalizes. 1 is instant, 0.5 is in 50% steps, et cetera.
     *
     * @param world
     * @return [0;1]
     */
    static double getColumnHeatFlow(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_COLUMN_HEAT_FLOW.keyString),
                (double) RBMKKeys.KEY_COLUMN_HEAT_FLOW.defValue), 0.0D, 1.0D);
    }

    /**
     * Returns a modifier for fuel rod diffusion, i.e. how quickly the core and hull temperatures equalize.
     *
     * @param world
     * @return >0
     */
    public static double getFuelDiffusionMod(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_FUEL_DIFFUSION_MOD.keyString),
                (double) RBMKKeys.KEY_FUEL_DIFFUSION_MOD.defValue), 0.0D);
    }

    /**
     * Returns the percentual step size how quickly the fuel hull heat and the component heat equalizes. 1 is instant, 0.5 is in 50% steps, et cetera.
     *
     * @param world
     * @return [0;1]
     */
    public static double getFuelHeatProvision(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_HEAT_PROVISION.keyString),
                (double) RBMKKeys.KEY_HEAT_PROVISION.defValue), 0.0D, 1.0D);
    }

    /**
     * Simple integer that decides how tall the structure is.
     *
     * @param world
     * @return [0;250]
     */
    public static int getColumnHeight(World world) {
        return MathHelper.clamp(shittyWorkaroundParseInt(world.getGameRules().getString(RBMKKeys.KEY_COLUMN_HEIGHT.keyString),
                (int) RBMKKeys.KEY_COLUMN_HEIGHT.defValue), 2, 16) - 1;
    }

    /**
     * Whether or not scrap entities despawn on their own or remain alive until picked up.
     *
     * @param world
     * @return
     */
    public static boolean getPermaScrap(World world) {
        return world.getGameRules().getBoolean(RBMKKeys.KEY_PERMANENT_SCRAP.keyString);
    }

    /**
     * How many heat units are consumed per steam unit (scaled per type) produced.
     *
     * @param world
     * @return >0
     */
    static double getBoilerHeatConsumption(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION.keyString),
                (double) RBMKKeys.KEY_BOILER_HEAT_CONSUMPTION.defValue), 0D);
    }

    /**
     * A multiplier for how quickly the control rods move.
     *
     * @param world
     * @return >0
     */
    static double getControlSpeed(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_CONTROL_SPEED_MOD.keyString),
                (double) RBMKKeys.KEY_CONTROL_SPEED_MOD.defValue), 0.0D);
    }

    /**
     * A multiplier for how much flux the rods give out.
     *
     * @param world
     * @return >0
     */
    public static double getReactivityMod(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_REACTIVITY_MOD.keyString),
                (double) RBMKKeys.KEY_REACTIVITY_MOD.defValue), 0.0D);
    }

    /**
     * A multiplier for how much flux the rods give out.
     *
     * @param world
     * @return >0
     */
    static double getOutgasserMod(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_OUTGASSER_MOD.keyString),
                (double) RBMKKeys.KEY_OUTGASSER_MOD.defValue), 0.0D);
    }

    /**
     * A multiplier for how high the power surge goes when inserting control rods.
     *
     * @param world
     * @return >0
     */
    static double getSurgeMod(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_SURGE_MOD.keyString),
                (double) RBMKKeys.KEY_SURGE_MOD.defValue), 0.0D);
    }

    /**
     * Simple integer that decides how far the flux of a normal fuel rod reaches.
     *
     * @param world
     * @return [1;100]
     */
    public static int getFluxRange(World world) {
        return MathHelper.clamp(shittyWorkaroundParseInt(world.getGameRules().getString(RBMKKeys.KEY_FLUX_RANGE.keyString),
                (int) RBMKKeys.KEY_FLUX_RANGE.defValue), 1, 100);
    }

    /**
     * Simple integer that decides how far the flux of a ReaSim fuel rod reaches.
     *
     * @param world
     * @return [1;100]
     */
    static int getReaSimRange(World world) {
        return MathHelper.clamp(shittyWorkaroundParseInt(world.getGameRules().getString(RBMKKeys.KEY_REASIM_RANGE.keyString),
                (int) RBMKKeys.KEY_REASIM_RANGE.defValue), 1, 100);
    }

    /**
     * Simple integer that decides how many neutrons are created from ReaSim fuel rods.
     *
     * @param world
     * @return [1;24]
     */
    static int getReaSimCount(World world) {
        return MathHelper.clamp(shittyWorkaroundParseInt(world.getGameRules().getString(RBMKKeys.KEY_REASIM_COUNT.keyString),
                (int) RBMKKeys.KEY_REASIM_COUNT.defValue), 1, 24);
    }

    /**
     * Returns a modifier for the outgoing flux of individual streams from the ReaSim fuel rod to compensate for the potentially increased stream
     * count.
     *
     * @param world
     * @return >0
     */
    static double getReaSimOutputMod(World world) {
        return Math.max(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_REASIM_MOD.keyString),
                (double) RBMKKeys.KEY_REASIM_MOD.defValue), 0.0D);
    }

    /**
     * Whether or not all components should act like boilers with dedicated in/outlet blocks
     *
     * @param world
     * @return
     */
    static boolean getReasimBoilers(World world) {
        return world.getGameRules().getBoolean(RBMKKeys.KEY_REASIM_BOILERS.keyString) || (GeneralConfig.enable528 && GeneralConfig.enable528ReasimBoilers);
    }

    /**
     * How much % of the possible steam ends up being produced per tick
     *
     * @param world
     * @return [0;1]
     */
    static double getReaSimBoilerSpeed(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_REASIM_BOILER_SPEED.keyString),
                (double) RBMKKeys.KEY_REASIM_BOILER_SPEED.defValue), 0.0D, 1.0D);
    }

    //why make the double representation accessible in a game rule when you can just force me to add a second pointless parsing operation?
    private static double shittyWorkaroundParseDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
        }
        return def;
    }

    private static int shittyWorkaroundParseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
        }
        return def;
    }

    /**
     * Whether or not fuel columns should initiate a meltdown when overheating
     * The method is in reverse because the default for older worlds will be 'false'
     *
     * @param world
     * @return
     */
    static boolean getMeltdownsDisabled(World world) {
        return world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_MELTDOWNS.keyString);
    }

    /**
     * Whether or not connected pipes and turbines should explode when the reactor undergoes a meltdown.
     *
     * @param world
     * @return
     */
    static boolean getOverpressure(World world) {
        return world.getGameRules().getBoolean(RBMKKeys.KEY_ENABLE_MELTDOWN_OVERPRESSURE.keyString);
    }

    /**
     * The percentage of neutrons to moderate from fast to slow when they pass through a moderator.
     *
     * @param world
     * @return
     */
    public static double getModeratorEfficiency(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_MODERATOR_EFFICIENCY.keyString),
                (double) RBMKKeys.KEY_MODERATOR_EFFICIENCY.defValue), 0.0D, 1.0D);
    }

    /**
     * The percentage of neutrons to be absorbed when a stream hits an absorber column.
     *
     * @param world
     * @return
     */
    public static double getAbsorberEfficiency(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_ABSORBER_EFFICIENCY.keyString),
                (double) RBMKKeys.KEY_ABSORBER_EFFICIENCY.defValue), 0.0D, 1.0D);
    }

    /**
     * The percentage of neutron to reflect when a stream hits a reflector column.
     *
     * @param world
     * @return
     */
    public static double getReflectorEfficiency(World world) {
        return MathHelper.clamp(shittyWorkaroundParseDouble(world.getGameRules().getString(RBMKKeys.KEY_REFLECTOR_EFFICIENCY.keyString),
                (double) RBMKKeys.KEY_REFLECTOR_EFFICIENCY.defValue), 0.0D, 1.0D);
    }

    /**
     * Whether fuel rods should deplete, disabling this makes rods last forever
     *
     * @param world
     * @return
     */
    public static boolean getDepletion(World world) {
        return !world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_DEPLETION.keyString);
    }

    /**
     * Whether xenon poison should be calculated
     *
     * @param world
     * @return
     */
    public static boolean getXenon(World world) {
        return !world.getGameRules().getBoolean(RBMKKeys.KEY_DISABLE_XENON.keyString);
    }

    public enum RBMKKeys {
        KEY_SAVE_DIALS("dialSaveDials", true),
        KEY_PASSIVE_COOLING("dialPassiveCooling", 1.0),
        KEY_COLUMN_HEAT_FLOW("dialColumnHeatFlow", 0.2),
        KEY_FUEL_DIFFUSION_MOD("dialDiffusionMod", 1.0),
        KEY_HEAT_PROVISION("dialHeatProvision", 0.2),
        KEY_COLUMN_HEIGHT("dialColumnHeight", 4),
        KEY_PERMANENT_SCRAP("dialEnablePermaScrap", true),
        KEY_BOILER_HEAT_CONSUMPTION("dialBoilerHeatConsumption", 0.1),
        KEY_CONTROL_SPEED_MOD("dialControlSpeed", 1.0),
        KEY_REACTIVITY_MOD("dialReactivityMod", 1.0),
        KEY_OUTGASSER_MOD("dialOutgasserSpeedMod", 1.0),
        KEY_SURGE_MOD("dialControlSurgeMod", 1.0),
        KEY_FLUX_RANGE("dialFluxRange", 5),
        KEY_REASIM_RANGE("dialReasimRange", 10),
        KEY_REASIM_COUNT("dialReasimCount", 6),
        KEY_REASIM_MOD("dialReasimOutputMod", 1.0),
        KEY_REASIM_BOILERS("dialReasimBoilers", false),
        KEY_REASIM_BOILER_SPEED("dialReasimBoilerSpeed", 0.05),
        KEY_DISABLE_MELTDOWNS("dialDisableMeltdowns", false),
        KEY_ENABLE_MELTDOWN_OVERPRESSURE("dialEnableMeltdownOverpressure", false),
        KEY_MODERATOR_EFFICIENCY("dialModeratorEfficiency", 1.0),
        KEY_ABSORBER_EFFICIENCY("dialAbsorberEfficiency", 1.0),
        KEY_REFLECTOR_EFFICIENCY("dialReflectorEfficiency", 1.0),
        KEY_DISABLE_DEPLETION("dialDisableDepletion", false),
        KEY_DISABLE_XENON("dialDisableXenon", false);

        public static final RBMKKeys[] VALUES = values();

        public final String keyString;
        public final Object defValue;

        RBMKKeys(String key, Object def) {
            keyString = key;
            defValue = def;
        }
    }
}
