package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class InventoryPlayerTransformer {
    static final String TARGET = "net.minecraft.entity.player.InventoryPlayer";

    private static final ObfSafeName SET_SLOT_CONTENT = new ObfSafeName("setInventorySlotContents", "func_70299_a");

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (SET_SLOT_CONTENT.matches(method.name)) {
                    coreLogger.info("Patching method: {} / {}", SET_SLOT_CONTENT.mcp, method.name);
                    method.instructions.insert(createFastPathHook());
                }
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            fail(TARGET, t);
            return basicClass;
        }
    }

    private static InsnList createFastPathHook() {
        InsnList toInject = new InsnList();
        toInject.add(new VarInsnNode(ALOAD, 0)); // this (InventoryPlayer)
        toInject.add(new VarInsnNode(ILOAD, 1)); // int (slot)
        toInject.add(new VarInsnNode(ALOAD, 2)); // ItemStack
        toInject.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/InventoryHook", "onClientSlotChange", "(Lnet/minecraft/entity/player" +
                "/InventoryPlayer;ILnet/minecraft/item/ItemStack;)V", false));
        return toInject;
    }
}
