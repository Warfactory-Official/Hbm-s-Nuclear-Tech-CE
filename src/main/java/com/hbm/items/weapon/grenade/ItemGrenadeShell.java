package com.hbm.items.weapon.grenade;

import com.hbm.items.ItemEnumMulti;

public class ItemGrenadeShell extends ItemEnumMulti<ItemGrenadeShell.EnumGrenadeShell> {

    /*
     *  __________
     * |          | ________ SHELL COLOR - Changes based on filling
     * |   _____  |
     * |  |    _____________ LABEL COLOR - Changes based on filling
     * |  |_____| |
     *  \________/__________ FUZE INDICATOR RING - Changes based on installed fuze
     *   \______/
     *    |    |
     *    |    |
     *    |   ______________ MISC - Remains static for this shell
     *    |    |
     *    |    |
     *    |    |
     *    |    |
     *    |    |
     *    /    \
     *   |______|
     *     {__}
     */

    public ItemGrenadeShell(String registryName) {
        super(registryName, EnumGrenadeShell.VALUES, true, true);
    }

    public enum EnumGrenadeShell {
        FRAG(4, 30, 0.5D, 1D),      // bonus fragmentation
        STICK(4, 43, 0.25D, 1.5D),  // thrown farther
        TECH(2, 30, 0.5D, 1D),      // casing with electronics for EMP/plasma
        NUKE(1, 43, 0.25D, 1.5D);   // nuka grenade casing for high yield grenades

        public static final EnumGrenadeShell[] VALUES = values();

        private final int stackLimit;
        private final int drawDuration;
        private final double bounceModifier;
        private final double yeetForce;

        EnumGrenadeShell(int stackLimit, int drawDuration, double bounceModifier, double yeetForce) {
            this.stackLimit = stackLimit;
            this.drawDuration = drawDuration;
            this.bounceModifier = bounceModifier;
            this.yeetForce = yeetForce;
        }

        public int getStackLimit() { return stackLimit; }
        public int getDrawDuration() { return drawDuration; }
        public double getBounce() { return bounceModifier; }
        public double getYeetForce() { return yeetForce; }
    }
}
