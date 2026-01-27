package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class PlayerInteractionManagerTransformer {
    static final String TARGET = "net.minecraft.server.management.PlayerInteractionManager";

    private static final ObfSafeName processRightClickBlock = new ObfSafeName("processRightClickBlock", "func_187251_a");
    private static final ObfSafeName getTileEntity = new ObfSafeName("getTileEntity", "func_175625_s");

    private static boolean patchSpectatorRightClickBlock(MethodNode method) {
        Integer tileEntityVar = null;
        AbstractInsnNode spectatorReturnGetStatic = null;

        AbstractInsnNode[] insns = method.instructions.toArray();

        for (AbstractInsnNode n : insns) {
            if (n.getOpcode() == INVOKEVIRTUAL && n instanceof MethodInsnNode mi) {
                if ("net/minecraft/world/World".equals(mi.owner) && getTileEntity.matches(mi.name) && "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;".equals(mi.desc)) {

                    AbstractInsnNode next = AsmHelper.nextRealInsn(mi);

                    if (next instanceof VarInsnNode varInsnNode && next.getOpcode() == ASTORE) {
                        tileEntityVar = varInsnNode.var;
                    }
                }
            }
            if (tileEntityVar != null && n.getOpcode() == GETSTATIC && n instanceof FieldInsnNode f) {
                if ("net/minecraft/util/EnumActionResult".equals(f.owner) && "PASS".equals(f.name) && "Lnet/minecraft/util/EnumActionResult;".equals(f.desc)) {

                    AbstractInsnNode next = AsmHelper.nextRealInsn(f);

                    if (next != null && next.getOpcode() == ARETURN) {
                        spectatorReturnGetStatic = f;
                        break;
                    }
                }
            }
        }

        if (tileEntityVar == null || spectatorReturnGetStatic == null) {
            coreLogger.error("Failed to locate tileentity local or spectator PASS return in processRightClickBlock");
            return false;
        }

        InsnList patch = new InsnList();
        patch.add(new VarInsnNode(ALOAD, 1));//player
        patch.add(new VarInsnNode(ALOAD, 2));//worldIn
        patch.add(new VarInsnNode(ALOAD, 3));//stack
        patch.add(new VarInsnNode(ALOAD, 4));//hand
        patch.add(new VarInsnNode(ALOAD, 5));//pos
        patch.add(new VarInsnNode(ALOAD, 6));//facing
        patch.add(new VarInsnNode(FLOAD, 7));//hitX
        patch.add(new VarInsnNode(FLOAD, 8));//hitY
        patch.add(new VarInsnNode(FLOAD, 9));//hitZ
        patch.add(new VarInsnNode(ALOAD, tileEntityVar));//tileentity

        patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/PlayerInteractionManagerHook", "onSpectatorRightClickBlock", "(Lnet/minecraft/entity/player/EntityPlayer;" + "Lnet/minecraft/world/World;" + "Lnet/minecraft/item/ItemStack;" + "Lnet/minecraft/util/EnumHand;" + "Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/util/EnumFacing;FFF" + "Lnet/minecraft/tileentity/TileEntity;)" + "Lnet/minecraft/util/EnumActionResult;", false));

        method.instructions.insertBefore(spectatorReturnGetStatic, patch);
        method.instructions.remove(spectatorReturnGetStatic);

        coreLogger.info("Injected PlayerInteractionManagerHook.onSpectatorRightClickBlock before spectator PASS return");
        return true;
    }


    private static boolean injectPostHookHybrid(MethodNode method) {
        int resultLocal = method.maxLocals;
        method.maxLocals += 1; // EnumActionResult

        boolean foundAnyReturn = false;

        AbstractInsnNode n = method.instructions.getFirst();
        while (n != null) {
            if (n.getOpcode() == ARETURN) {
                foundAnyReturn = true;
                AbstractInsnNode returnNode = n;
                n = n.getNext();
                InsnList patch = new InsnList();
                patch.add(new VarInsnNode(ASTORE, resultLocal));
                patch.add(new VarInsnNode(ALOAD, 0)); // this
                patch.add(new VarInsnNode(ALOAD, 1)); // player
                patch.add(new VarInsnNode(ALOAD, 2)); // world
                patch.add(new VarInsnNode(ALOAD, 3)); // stack
                patch.add(new VarInsnNode(ALOAD, 4)); // hand
                patch.add(new VarInsnNode(ALOAD, 5)); // pos
                patch.add(new VarInsnNode(ALOAD, 6)); // facing
                patch.add(new VarInsnNode(FLOAD, 7)); // hitX
                patch.add(new VarInsnNode(FLOAD, 8)); // hitY
                patch.add(new VarInsnNode(FLOAD, 9)); // hitZ
                patch.add(new VarInsnNode(ALOAD, resultLocal));
                patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/PlayerInteractionManagerHook", "onRightClickBlockPost", "(Lnet/minecraft/server/management/PlayerInteractionManager;" + "Lnet/minecraft/entity/player/EntityPlayer;" + "Lnet/minecraft/world/World;" + "Lnet/minecraft/item/ItemStack;" + "Lnet/minecraft/util/EnumHand;" + "Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/util/EnumFacing;FFF" + "Lnet/minecraft/util/EnumActionResult;)" + "Lnet/minecraft/util/EnumActionResult;", false));
                patch.add(new InsnNode(ARETURN));
                method.instructions.insertBefore(returnNode, patch);
                method.instructions.remove(returnNode);
            } else {
                n = n.getNext();
            }
        }

        if (!foundAnyReturn) {
            coreLogger.error("No ARETURNs found in processRightClickBlock (hybrid path)");
        } else {
            coreLogger.info("Wrapped {} ARETURNs in processRightClickBlock with hybrid post-hook", true);
        }

        return foundAnyReturn;
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            boolean patched = false;
            boolean hybrid = HbmCorePlugin.getBrand().isHybrid();

            for (MethodNode mn : cn.methods) {
                if (processRightClickBlock.matches(mn.name) && "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;".equals(mn.desc)) {

                    coreLogger.info("Patching method: {}{}", processRightClickBlock.getName(), mn.desc);
                    boolean ok = hybrid ? injectPostHookHybrid(mn) : patchSpectatorRightClickBlock(mn);
                    if (!ok) throw new IllegalStateException("Failed to inject PlayerInteractionManager hook (hybrid=" + hybrid + ")");
                    patched = true;
                    break;
                }
            }

            if (!patched) {
                throw new IllegalStateException("Did not find processRightClickBlock to patch");
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
