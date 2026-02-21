package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

// uncomment the line in HbmCoreTransformer to log usage
// For comparisons use tools/unused-assets.ps1 or tools/unused-assets.sh
final class ResourceAccessTransformer {
    static final String TARGET = "net.minecraft.client.resources.FallbackResourceManager";
    private static final String DESC_GET_INPUT_STREAM = "(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/resources/IResourcePack;)Ljava/io/InputStream;";

    private static boolean injectLogger(MethodNode method) {
        AbstractInsnNode anchor = AsmHelper.firstRealInsn(method);
        if (anchor == null) {
            return false;
        }

        InsnList patch = new InsnList();
        patch.add(new VarInsnNode(ALOAD, 1));
        patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/ResourceAccessLogger", "log", "(Lnet/minecraft/util/ResourceLocation;)V", false));
        method.instructions.insertBefore(anchor, patch);
        return true;
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            boolean patchedGetInputStream = false;

            for (MethodNode mn : cn.methods) {
                if (DESC_GET_INPUT_STREAM.equals(mn.desc)) {
                    coreLogger.info("Patching method: {}{}", mn.name, mn.desc);
                    patchedGetInputStream = injectLogger(mn);
                }
            }

            if (!patchedGetInputStream) {
                throw new IllegalStateException("Failed to patch getInputStream in FallbackResourceManager");
            }

            ClassWriter cw = new MinecraftClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            fail(TARGET, t);
            return basicClass;
        }
    }
}
