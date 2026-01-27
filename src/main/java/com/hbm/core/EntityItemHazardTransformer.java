package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class EntityItemHazardTransformer {
    static final String TARGET = "net.minecraft.entity.item.EntityItem";
    private static final ObfSafeName ON_UPDATE = new ObfSafeName("onUpdate", "func_70071_h_");
    private static final String HAZARD_OWNER = "com/hbm/hazard/HazardSystem";
    private static final String HAZARD_NAME  = "updateDroppedItem";
    private static final String HAZARD_DESC  = "(Lnet/minecraft/entity/item/EntityItem;)V";
    private static void injectHook(MethodNode method) {
        boolean injected = false;
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn.getOpcode() != RETURN) continue;
            InsnList patch = new InsnList();
            patch.add(new VarInsnNode(ALOAD, 0));
            patch.add(new MethodInsnNode(INVOKESTATIC, HAZARD_OWNER, HAZARD_NAME, HAZARD_DESC, false));
            method.instructions.insertBefore(insn, patch);
            injected = true;
        }
        if (!injected) {
            throw new RuntimeException("Failed to find RETURN in EntityItem#onUpdate");
        }
        coreLogger.info("Injected HazardSystem hook into EntityItem#onUpdate ({} return site(s))", "all");
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            boolean patched = false;
            for (MethodNode method : classNode.methods) {
                if (ON_UPDATE.matches(method.name) && "()V".equals(method.desc)) {
                    coreLogger.info("Patching method: {} / {}", ON_UPDATE.mcp, method.name);
                    injectHook(method);
                    patched = true;
                    break;
                }
            }
            if (!patched) {
                throw new RuntimeException("Failed to find EntityItem#onUpdate()V");
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            fail(TARGET, t);
            return basicClass;
        }
    }
}
