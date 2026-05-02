package com.hbm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

import static com.hbm.core.ModPresence.*;

public class VanillaMixinConfigPlugin implements IMixinConfigPlugin {

    private static final String PACKAGE_PREFIX = "com.hbm.mixin.vanilla.";
    // coexistence assumption:
    // only OptiFine and Nothirium can legally coexist.
    private static final boolean VANILLA_TERRAIN_PATH = !NEONIUM && !NOTHIRIUM && !CELERITAS;
    private static final boolean BASE_BUFFER_BUILDER_PATH = !NEONIUM && !NOTHIRIUM && !OPTIFINE;
    private static final boolean OPTIFINE_BUFFER_BUILDER_PATH = OPTIFINE && !NEONIUM && !NOTHIRIUM;
    private static final boolean NOTHIRIUM_OPTIFINE_BUFFER_BUILDER_PATH = NOTHIRIUM && OPTIFINE;

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
            return switch (suffix) {
                case "MixinRenderGlobal" -> VANILLA_TERRAIN_PATH;
                default -> true;
            };
        }

        String group = suffix.substring(0, separator);
        String simpleName = suffix.substring(separator + 1);
        return switch (group) {
            case "base" -> switch (simpleName) {
                case "MixinBufferBuilder" -> BASE_BUFFER_BUILDER_PATH;
                default -> true;
            };
            case "optifine" -> switch (simpleName) {
                case "MixinBufferBuilder" -> OPTIFINE_BUFFER_BUILDER_PATH;
                case "MixinRenderGlobal" -> OPTIFINE && VANILLA_TERRAIN_PATH;
                default -> true;
            };
            case "neonium" -> NEONIUM;
            case "nothirium" -> switch (simpleName) {
                case "MixinBufferBuilder" -> NOTHIRIUM && !OPTIFINE && !NEONIUM;
                case "MixinBufferBuilderOptifine" -> NOTHIRIUM_OPTIFINE_BUFFER_BUILDER_PATH && !NEONIUM;
                default -> NOTHIRIUM;
            };
            case "celeritas" -> CELERITAS;
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
