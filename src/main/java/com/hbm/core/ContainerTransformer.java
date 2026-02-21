package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class ContainerTransformer {
    private static final ObfSafeName DETECT_AND_SEND_CHANGES = new ObfSafeName("detectAndSendChanges", "func_75142_b");
    static final String TARGET = "net.minecraft.inventory.Container";

    /**
     * Made safe against Spigot's patch.
     * @see <a href="https://github.com/Luohuayu/CatServer/blob/1.12.2/patches/net/minecraft/inventory/Container.java.patch">Container.java.patch</a>
     */
    private static void injectHook(MethodNode method) {
        boolean injected = false;

        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode mi)) continue;

            boolean isSet = "set".equals(mi.name) && "(ILjava/lang/Object;)Ljava/lang/Object;".equals(mi.desc) &&
                            (mi.getOpcode() == INVOKEVIRTUAL || mi.getOpcode() == INVOKEINTERFACE) &&
                            ("net/minecraft/util/NonNullList".equals(mi.owner) || "java/util/List".equals(mi.owner));

            if (!isSet) continue;

            AbstractInsnNode maybePop = mi.getNext();
            if (maybePop == null || maybePop.getOpcode() != POP) {
                continue;
            }

            int localList = method.maxLocals;
            int localIndex = localList + 1;
            int localNew = localList + 2;
            int localPrev = localList + 3;
            method.maxLocals += 4;

            InsnList patch = new InsnList();

            patch.add(new VarInsnNode(ASTORE, localNew));   // pop newItem
            patch.add(new VarInsnNode(ISTORE, localIndex)); // pop index
            patch.add(new VarInsnNode(ASTORE, localList));  // pop list

            // Replay the set(list, index, newItem) and capture the previous value
            patch.add(new VarInsnNode(ALOAD, localList));
            patch.add(new VarInsnNode(ILOAD, localIndex));
            patch.add(new VarInsnNode(ALOAD, localNew));
            patch.add(new MethodInsnNode(mi.getOpcode(), mi.owner, mi.name, mi.desc, (mi.getOpcode() == INVOKEINTERFACE)));
            patch.add(new TypeInsnNode(CHECKCAST, "net/minecraft/item/ItemStack"));
            patch.add(new VarInsnNode(ASTORE, localPrev));

            patch.add(new VarInsnNode(ALOAD, 0));            // this (Container)
            patch.add(new VarInsnNode(ILOAD, localIndex));       // index
            patch.add(new VarInsnNode(ALOAD, localPrev));        // previous ItemStack
            patch.add(new VarInsnNode(ALOAD, localNew));         // new ItemStack
            patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/InventoryHook", "onContainerChange",
                    "(Lnet/minecraft/inventory/Container;" + "ILnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", false));

            method.instructions.insertBefore(mi, patch);
            method.instructions.remove(maybePop);
            method.instructions.remove(mi);

            injected = true;
            coreLogger.info("Injected hook at NonNullList#set site");
            break;
        }

        if (!injected) {
            throw new RuntimeException("Failed to find NonNullList#set call in detectAndSendChanges");
        }
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (MethodNode method : classNode.methods) {
                if (DETECT_AND_SEND_CHANGES.matches(method.name) && method.desc.equals("()V")) {
                    coreLogger.info("Patching method: {} / {}", DETECT_AND_SEND_CHANGES.mcp, method.name);
                    injectHook(method);
                }
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            fail("net.minecraft.inventory.Container", t);
            return basicClass;
        }
    }
}
