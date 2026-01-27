package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class ForgeHooksTransformer {
    static final String TARGET = "net.minecraftforge.common.ForgeHooks";
    private static final ObfSafeName isSpectator = new ObfSafeName("isSpectator", "func_175149_v");
    private static final ObfSafeName getTileEntity = new ObfSafeName("getTileEntity", "func_175625_s");
    private static final ObfSafeName storeTEInStack = new ObfSafeName("storeTEInStack", "func_184119_a");
    private static final ObfSafeName getMinecraft = new ObfSafeName("getMinecraft", "func_71410_x");

    private static AbstractInsnNode findAnchor(MethodNode method) {
        for (AbstractInsnNode n : method.instructions.toArray()) {
            if (n.getOpcode() == GETSTATIC) {
                FieldInsnNode f = (FieldInsnNode) n;
                if ("net/minecraftforge/common/ForgeModContainer".equals(f.owner) && "fullBoundingBoxLadders".equals(f.name) && "Z".equals(f.desc)) {
                    return n;
                }
            }
        }
        return null;
    }

    private static void injectHook(MethodNode method, AbstractInsnNode anchor, boolean headFallback) {
        // 0: IBlockState state
        // 1: World       world
        // 2: BlockPos    pos
        // 3: EntityLivingBase entity

        LabelNode L_returnTrue = new LabelNode();
        LabelNode L_returnFalse = new LabelNode();
        LabelNode L_continue = new LabelNode();

        InsnList patch = new InsnList();

        // Fallback path for LittleTiles because they literally nuked the method
        // so we have to inject our own spectator guard
        if (headFallback) {
            LabelNode L_notSpectator = new LabelNode();
            patch.add(new VarInsnNode(ALOAD, 3));
            patch.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/entity/player/EntityPlayer"));
            patch.add(new JumpInsnNode(IFEQ, L_notSpectator));

            patch.add(new VarInsnNode(ALOAD, 3));
            patch.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/player/EntityPlayer"));
            patch.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", isSpectator.getName(), "()Z", false));
            patch.add(new JumpInsnNode(IFEQ, L_notSpectator));
            patch.add(new InsnNode(ICONST_0));
            patch.add(new InsnNode(IRETURN));
            patch.add(L_notSpectator);
        }

        // Call LadderHook.onCheckLadder(state, world, pos, entity)
        patch.add(new VarInsnNode(ALOAD, 0));
        patch.add(new VarInsnNode(ALOAD, 1));
        patch.add(new VarInsnNode(ALOAD, 2));
        patch.add(new VarInsnNode(ALOAD, 3));
        patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/LadderHook", "onCheckLadder",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraftforge/fml/common/eventhandler/Event$Result;", false));

        // Duplicate result and compare against Event.Result.ALLOW
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/eventhandler/Event$Result", "ALLOW",
                "Lnet/minecraftforge/fml/common/eventhandler/Event$Result;"));
        patch.add(new JumpInsnNode(IF_ACMPEQ, L_returnTrue));

        // Duplicate again and compare against Event.Result.DENY
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/eventhandler/Event$Result", "DENY",
                "Lnet/minecraftforge/fml/common/eventhandler/Event$Result;"));
        patch.add(new JumpInsnNode(IF_ACMPEQ, L_returnFalse));

        // DEFAULT -> pop the result and continue
        patch.add(new InsnNode(POP));
        patch.add(new JumpInsnNode(GOTO, L_continue));

        // ALLOW -> pop result, return true
        patch.add(L_returnTrue);
        patch.add(new InsnNode(POP));
        patch.add(new InsnNode(ICONST_1));
        patch.add(new InsnNode(IRETURN));

        // DENY -> pop result, return false
        patch.add(L_returnFalse);
        patch.add(new InsnNode(POP));
        patch.add(new InsnNode(ICONST_0));
        patch.add(new InsnNode(IRETURN));
        patch.add(L_continue);

        method.instructions.insertBefore(anchor, patch);
        if (!headFallback) coreLogger.info("Injected CheckLadderEvent hook before fullBoundingBoxLadders read");
        else coreLogger.warn("A mod nuked isLivingOnLadder! Injected spectator check and CheckLadderEvent hook on isLivingOnLadder HEAD");
    }

    private static boolean patchPickBlock(MethodNode method) {
        boolean replacedGetTileEntity = false;
        boolean injectedHook = false;

        AbstractInsnNode[] insns = method.instructions.toArray();
        for (AbstractInsnNode insn : insns) {
            if (insn instanceof MethodInsnNode methodInsn) {
                if (!replacedGetTileEntity && methodInsn.getOpcode() == INVOKEVIRTUAL && "net/minecraft/world/World".equals(methodInsn.owner) && getTileEntity.matches(methodInsn.name) && "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;".equals(methodInsn.desc)) {
                    MethodInsnNode replacement = new MethodInsnNode(INVOKESTATIC, "com/hbm/util/CompatExternal", "getCoreFromPos", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", false);
                    method.instructions.set(methodInsn, replacement);
                    replacedGetTileEntity = true;
                    continue;
                }

                if (!injectedHook && "net/minecraft/client/Minecraft".equals(methodInsn.owner) && storeTEInStack.matches(methodInsn.name) && "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/item/ItemStack;".equals(methodInsn.desc)) {

                    AbstractInsnNode teLoadNode = methodInsn.getPrevious();
                    if (!(teLoadNode instanceof VarInsnNode teVar) || teVar.getOpcode() != ALOAD) continue;

                    AbstractInsnNode resultLoadNode = teLoadNode.getPrevious();
                    if (!(resultLoadNode instanceof VarInsnNode resultVar) || resultVar.getOpcode() != ALOAD) continue;

                    AbstractInsnNode getMinecraftCallCandidate = resultLoadNode.getPrevious();
                    if (!(getMinecraftCallCandidate instanceof MethodInsnNode minecraftCall)) continue;
                    if (minecraftCall.getOpcode() != INVOKESTATIC || !"net/minecraft/client/Minecraft".equals(minecraftCall.owner) || !getMinecraft.matches(minecraftCall.name) || !"()Lnet/minecraft/client/Minecraft;".equals(minecraftCall.desc)) {
                        continue;
                    }

                    AbstractInsnNode popNode = methodInsn.getNext();
                    if (!(popNode instanceof InsnNode) || popNode.getOpcode() != POP) {
                        continue;
                    }

                    AbstractInsnNode afterPop = popNode.getNext();
                    LabelNode afterStoreLabel;
                    if (afterPop instanceof LabelNode labelNode) {
                        afterStoreLabel = labelNode;
                    } else {
                        afterStoreLabel = new LabelNode();
                        method.instructions.insert(popNode, afterStoreLabel);
                    }

                    LabelNode labelCallStore = new LabelNode();
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(ALOAD, resultVar.var)); // result
                    hook.add(new VarInsnNode(ALOAD, 0)); // target
                    hook.add(new VarInsnNode(ALOAD, 2)); // world
                    hook.add(new VarInsnNode(ALOAD, teVar.var)); // tile entity
                    hook.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/ForgeHooksPickBlockHook", "handlePickBlock", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/world/World;Lnet/minecraft/tileentity/TileEntity;)Z", false));
                    hook.add(new JumpInsnNode(IFEQ, labelCallStore));
                    hook.add(new JumpInsnNode(GOTO, afterStoreLabel));
                    hook.add(labelCallStore);

                    method.instructions.insertBefore(minecraftCall, hook);
                    injectedHook = true;
                }
            }
        }
        return replacedGetTileEntity && injectedHook;
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            cr.accept(cn, 0);

            boolean patchedLadder = false;
            boolean patchedPickBlock = false;
            for (MethodNode mn : cn.methods) {
                if ("isLivingOnLadder".equals(mn.name) && ("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/entity/EntityLivingBase;)Z").equals(mn.desc)) {
                    coreLogger.info("Patching method: {}{}", "isLivingOnLadder", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" + "Lnet/minecraft/entity/EntityLivingBase;)Z");

                    AbstractInsnNode anchor = findAnchor(mn);
                    boolean headFallback = false;
                    if (anchor == null) {
                        anchor = AsmHelper.firstRealInsnOrHead(mn.instructions);
                        headFallback = true;
                    }
                    injectHook(mn, anchor, headFallback);
                    patchedLadder = true;
                } else if ("onPickBlock".equals(mn.name) && "(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;)Z".equals(mn.desc)) {
                    coreLogger.info("Patching method: {}{}", "onPickBlock", "(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;)Z");
                    if (patchPickBlock(mn)) {
                        patchedPickBlock = true;
                    }
                }
            }

            if (!patchedLadder) {
                throw new IllegalStateException("Failed to patch isLivingOnLadder");
            }

            if (!patchedPickBlock) {
                throw new IllegalStateException("Failed to patch onPickBlock");
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
