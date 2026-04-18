package com.hbm.core;

import net.minecraft.launchwrapper.Launch;

// for lazy evaluation
public final class ModPresence {

    public static final boolean OPTIFINE = Launch.classLoader.getResource("optifine/OptiFineForgeTweaker.class") != null;
    public static final boolean NEONIUM = Launch.classLoader.getResource("io/neox/neonium/Neonium.class") != null;
    public static final boolean NOTHIRIUM = Launch.classLoader.getResource("meldexun/nothirium/mc/Nothirium.class") != null;
    public static final boolean CELERITAS = Launch.classLoader.getResource("org/taumc/celeritas/CeleritasVintage.class") != null;

    private ModPresence() {
    }
}
