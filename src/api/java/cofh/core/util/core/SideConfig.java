package cofh.core.util.core;

public class SideConfig {

    /* Number of Side Configs */
    public int numConfig;

    /* Side Types - Determines Texture & Behavior */
    public int[] sideTypes;

    /* Slot Groups accessible per Config */
    public int[][] slotGroups;

    /* Default Side configuration for freshly placed block */
    public byte[] defaultSides;

    public static boolean allowInsertion(int type) {

        return SIDE_INSERTION[type];
    }

    public static boolean allowExtraction(int type) {

        return SIDE_EXTRACTION[type];
    }

    public static boolean isPrimaryInput(int type) {

        return SIDE_INPUT_PRIMARY[type];
    }

    public static boolean isSecondaryInput(int type) {

        return SIDE_INPUT_SECONDARY[type];
    }

    public static boolean isPrimaryOutput(int type) {

        return SIDE_OUTPUT_PRIMARY[type];
    }

    public static boolean isSecondaryOutput(int type) {

        return SIDE_OUTPUT_SECONDARY[type];
    }

    public static final int NONE = 0;
    public static final int INPUT_ALL = 1;
    public static final int OUTPUT_PRIMARY = 2;
    public static final int OUTPUT_SECONDARY = 3;
    public static final int OUTPUT_ALL = 4;
    public static final int INPUT_PRIMARY = 5;
    public static final int INPUT_SECONDARY = 6;
    public static final int OPEN = 7;
    public static final int OMNI = 8;

    public static boolean[] SIDE_INSERTION = { false, true, false, false, false, true, true, true, true };
    public static boolean[] SIDE_EXTRACTION = { false, true, true, true, true, true, true, true, true };

    public static boolean[] SIDE_INPUT_PRIMARY = { false, true, false, false, false, true, false, false, true };
    public static boolean[] SIDE_INPUT_SECONDARY = { false, true, false, false, false, false, true, false, true };
    public static boolean[] SIDE_OUTPUT_PRIMARY = { false, false, true, false, true, false, false, false, true };
    public static boolean[] SIDE_OUTPUT_SECONDARY = { false, false, false, true, true, false, false, false, true };

}
