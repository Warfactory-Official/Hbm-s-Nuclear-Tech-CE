package com.hbm.core;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {

    private static final String PACKAGE_PREFIX = "com.hbm.mixin.mod.";
    private static final boolean NEONIUM_PRESENT = Launch.classLoader.getResource("io/neox/neonium/Neonium.class") != null;
    private static final boolean NOTHIRIUM_PRESENT = Launch.classLoader.getResource("meldexun/nothirium/mc/Nothirium.class") != null;
    private static final boolean OPTIFINE_PRESENT = Launch.classLoader.getResource("optifine/OptiFineForgeTweaker.class") != null;
    private static final boolean CELERITAS_PRESENT = Launch.classLoader.getResource("org/taumc/celeritas/CeleritasVintage.class") != null;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(PACKAGE_PREFIX)) {
            return true;
        }

        String suffix = mixinClassName.substring(PACKAGE_PREFIX.length());
        int separator = suffix.indexOf('.');
        if (separator < 0) {
            return true;
        }

        String group = suffix.substring(0, separator);
        return switch (group) {
            case "neonium" -> NEONIUM_PRESENT;
            case "nothirium" -> NOTHIRIUM_PRESENT;
            case "optifine" -> OPTIFINE_PRESENT;
            case "celeritas" -> CELERITAS_PRESENT;
            default -> true;
        };
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
