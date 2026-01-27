package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class GlStateManagerTransformer {
    static final String TARGET = "net.minecraft.client.renderer.GlStateManager";
    private static final String OWNER = "net/minecraft/client/renderer/GlStateManager";

    private static final String ROTATE = "rotate";
    private static final String ROTATE_DESC = "(DFFF)V";

    private static final ObfSafeName BLEND_FUNC = new ObfSafeName("blendFunc", "func_179112_b");
    private static final String BLEND_FUNC_DESC = "(II)V";

    private static final ObfSafeName BLEND_STATE = new ObfSafeName("blendState", "field_179157_e");
    private static final String BLEND_STATE_OWNER = "net/minecraft/client/renderer/GlStateManager$BlendState";
    private static final String BLEND_STATE_DESC = "Lnet/minecraft/client/renderer/GlStateManager$BlendState;";

    private static final ObfSafeName SRC_FACTOR = new ObfSafeName("srcFactor", "field_179211_b");
    private static final ObfSafeName DST_FACTOR = new ObfSafeName("dstFactor", "field_179212_c");
    private static final ObfSafeName SRC_FACTOR_ALPHA = new ObfSafeName("srcFactorAlpha", "field_179209_d");
    private static final ObfSafeName DST_FACTOR_ALPHA = new ObfSafeName("dstFactorAlpha", "field_179210_e");

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            patchRotate(classNode);
            patchBlendFunc(classNode);

            ClassWriter writer = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            fail("net.minecraft.client.renderer.GlStateManager", t);
            return basicClass;
        }
    }

    private static void patchRotate(ClassNode classNode) {
        MethodNode targetMethod = null;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(ROTATE) && method.desc.equals(ROTATE_DESC)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod != null) {
            targetMethod.instructions.clear();
            targetMethod.tryCatchBlocks.clear();
        } else {
            targetMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, ROTATE, ROTATE_DESC, null, null);
            classNode.methods.add(targetMethod);
        }

        populateRotate(targetMethod);
    }

    private static void populateRotate(MethodNode methodNode) {
        InsnList instructions = methodNode.instructions;
        instructions.add(new VarInsnNode(DLOAD, 0));
        instructions.add(new VarInsnNode(FLOAD, 2));
        instructions.add(new InsnNode(F2D));
        instructions.add(new VarInsnNode(FLOAD, 3));
        instructions.add(new InsnNode(F2D));
        instructions.add(new VarInsnNode(FLOAD, 4));
        instructions.add(new InsnNode(F2D));

        instructions.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glRotated", "(DDDD)V", false));
        instructions.add(new InsnNode(RETURN));
    }

    //vanilla bugfix: GlStateManager.blendFunc should update srcFactorAlpha and dstFactorAlpha
    //this makes RenderUtil getBlendSrcAlphaFactor and getBlendDstAlphaFactor work correctly
    private static void patchBlendFunc(ClassNode classNode) {
        MethodNode targetMethod = null;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(BLEND_FUNC.getName()) && method.desc.equals(BLEND_FUNC_DESC)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod != null) {
            targetMethod.instructions.clear();
            targetMethod.tryCatchBlocks.clear();
        } else {
            targetMethod = new MethodNode(ACC_PUBLIC | ACC_STATIC, BLEND_FUNC.getName(), BLEND_FUNC_DESC, null, null);
            classNode.methods.add(targetMethod);
        }

        populateBlendFunc(targetMethod);
    }

    private static void populateBlendFunc(MethodNode methodNode) {
        InsnList insn = methodNode.instructions;

        LabelNode labelDo = new LabelNode();
        LabelNode labelRet = new LabelNode();

        // if (src != blendState.srcFactor) goto DO;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new FieldInsnNode(GETFIELD, BLEND_STATE_OWNER, SRC_FACTOR.getName(), "I"));
        insn.add(new VarInsnNode(ILOAD, 0)); // src
        insn.add(new JumpInsnNode(IF_ICMPNE, labelDo));

        // if (dst != blendState.dstFactor) goto DO;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new FieldInsnNode(GETFIELD, BLEND_STATE_OWNER, DST_FACTOR.getName(), "I"));
        insn.add(new VarInsnNode(ILOAD, 1)); // dst
        insn.add(new JumpInsnNode(IF_ICMPNE, labelDo));

        // if (src != blendState.srcFactorAlpha) goto DO;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new FieldInsnNode(GETFIELD, BLEND_STATE_OWNER, SRC_FACTOR_ALPHA.getName(), "I"));
        insn.add(new VarInsnNode(ILOAD, 0)); // src
        insn.add(new JumpInsnNode(IF_ICMPNE, labelDo));

        // if (dst != blendState.dstFactorAlpha) goto DO;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new FieldInsnNode(GETFIELD, BLEND_STATE_OWNER, DST_FACTOR_ALPHA.getName(), "I"));
        insn.add(new VarInsnNode(ILOAD, 1)); // dst
        insn.add(new JumpInsnNode(IF_ICMPNE, labelDo));

        // All equal -> return;
        insn.add(new JumpInsnNode(GOTO, labelRet));

        // DO:
        insn.add(labelDo);

        // blendState.srcFactor = src;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new VarInsnNode(ILOAD, 0));
        insn.add(new FieldInsnNode(PUTFIELD, BLEND_STATE_OWNER, SRC_FACTOR.getName(), "I"));

        // blendState.dstFactor = dst;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new VarInsnNode(ILOAD, 1));
        insn.add(new FieldInsnNode(PUTFIELD, BLEND_STATE_OWNER, DST_FACTOR.getName(), "I"));

        // blendState.srcFactorAlpha = src;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new VarInsnNode(ILOAD, 0));
        insn.add(new FieldInsnNode(PUTFIELD, BLEND_STATE_OWNER, SRC_FACTOR_ALPHA.getName(), "I"));

        // blendState.dstFactorAlpha = dst;
        insn.add(new FieldInsnNode(GETSTATIC, OWNER, BLEND_STATE.getName(), BLEND_STATE_DESC));
        insn.add(new VarInsnNode(ILOAD, 1));
        insn.add(new FieldInsnNode(PUTFIELD, BLEND_STATE_OWNER, DST_FACTOR_ALPHA.getName(), "I"));

        // GL11.glBlendFunc(src, dst);
        insn.add(new VarInsnNode(ILOAD, 0));
        insn.add(new VarInsnNode(ILOAD, 1));
        insn.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glBlendFunc", "(II)V", false));

        // return;
        insn.add(labelRet);
        insn.add(new InsnNode(RETURN));
    }
}
