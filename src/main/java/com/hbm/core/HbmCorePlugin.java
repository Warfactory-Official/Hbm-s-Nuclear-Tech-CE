package com.hbm.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({"com.hbm.core", "com.hbm.mixin"})
@IFMLLoadingPlugin.SortingIndex(2077) // mlbv: this shit must be greater than 1000, after the srg transformer
public class HbmCorePlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    static final Logger coreLogger = LogManager.getLogger("HBM CoreMod");
    static final boolean NEONIUM_PRESENT = Launch.classLoader.getResource("io/neox/neonium/Neonium.class") != null;
    static final boolean NOTHIRIUM_PRESENT = Launch.classLoader.getResource("meldexun/nothirium/mc/Nothirium.class") != null;
    static final boolean OPTIFINE_PRESENT = Launch.classLoader.getResource("optifine/OptiFineForgeTweaker.class") != null;
    static final boolean CELERITAS_PRESENT = Launch.classLoader.getResource("org/taumc/celeritas/CeleritasVintage.class") != null;

    private static final Brand brand;
    private static boolean runtimeDeobfEnabled = false;
    private static boolean hardCrash = true;

    static {
        if (Launch.classLoader.getResource("catserver/server/CatServer.class") != null) {
            brand = Brand.CAT_SERVER;
        } else if (Launch.classLoader.getResource("com/mohistmc/MohistMC.class") != null) {
            brand = Brand.MOHIST;
        } else if (Launch.classLoader.getResource("org/magmafoundation/magma/Magma.class") != null) {
            brand = Brand.MAGMA;
        } else if (Launch.classLoader.getResource("com/cleanroommc/boot/Main.class") != null) {
            brand = Brand.CLEANROOM;
        } else {
            brand = Brand.FORGE;
        }
    }

    static void fail(String className, Throwable t) {
        coreLogger.fatal("Error transforming class {}. This is a coremod clash! Please report this on our issue tracker", className, t);
        if (hardCrash) {
            coreLogger.info("Crashing! To suppress the crash, launch Minecraft with -Dhbm.core.disablecrash");
            throw new IllegalStateException("HBM CoreMod transformation failure: " + className, t);
        }
    }

    public static boolean runtimeDeobfEnabled() {
        return runtimeDeobfEnabled;
    }

    public static String chooseName(String mcp, String srg) {
        return runtimeDeobfEnabled ? srg : mcp;
    }

    public static Brand getBrand() {
        return brand;
    }

    public static boolean isOptifinePresent() {
        return OPTIFINE_PRESENT;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{HbmCoreTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return HbmCoreModContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        runtimeDeobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
        String prop = System.getProperty("hbm.core.disablecrash");
        if (prop != null) {
            hardCrash = false;
            coreLogger.info("Crash suppressed with -Dhbm.core.disablecrash");
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList(
                "hbm.common.mixin.json",
                "hbm.mod.mixin.json",
                "hbm.vanilla.mixin.json"
        );
    }

    public enum Brand {
        FORGE, CAT_SERVER, MOHIST, MAGMA, CLEANROOM;

        public boolean isHybrid() {
            return this == CAT_SERVER || this == MOHIST || this == MAGMA;
        }
    }
}
