package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.transformers.MixinClassWriter;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

/**
 * CodeChickenLib compatibility patch, fixes Draconic Evolution / Brandon's Core and every other CCL consumer.
 * CCL feeds a single FMLProxyPacket to the vanilla broadcast helpers, which write that same instance to
 * every connection while only FMLOutboundHandler retains per recipient.
 * The multi target send methods are rerouted through the channel's FMLOutboundHandler instead.
 */
final class CodeChickenLibNetworkTransformer {
    static final String TARGET = "codechicken.lib.packet.PacketCustom";

    private static final String HELPER = "com/hbm/core/CCLNetworkHelper";
    private static final String PACKET = "Lnet/minecraft/network/Packet;";
    private static final String WORLD = "Lnet/minecraft/world/World;";

    private static final String[][] REDIRECTS = {
            {"sendToClients", "(" + PACKET + ")V"},
            {"sendToAllAround", "(" + PACKET + "DDDDI)V"},
            {"sendToDimension", "(" + PACKET + "I)V"},
            {"sendToChunk", "(" + PACKET + WORLD + "II)V"},
            {"sendToOps", "(" + PACKET + ")V"}
    };

    // let's hope nothing else breaks and mov won't kill me for using claude at this point..
    private static boolean redirect(ClassNode cn, String name, String desc) {
        for (MethodNode mn : cn.methods) {
            if (name.equals(mn.name) && desc.equals(mn.desc)) {
                if ((mn.access & ACC_STATIC) == 0)
                    throw new IllegalStateException("PacketCustom." + name + desc + " is not static");

                coreLogger.info("Patching CodeChickenLib method {}{}", name, desc);

                InsnList body = new InsnList();
                int slot = 0;
                for (Type arg : Type.getArgumentTypes(desc)) {
                    body.add(new VarInsnNode(arg.getOpcode(ILOAD), slot));
                    slot += arg.getSize();
                }
                body.add(new MethodInsnNode(INVOKESTATIC, HELPER, name, desc, false));
                body.add(new InsnNode(RETURN));

                AsmHelper.clearAndSetInstructions(mn, body);
                return true;
            }
        }
        return false;
    }

    static byte[] transform(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);
        try {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            for (String[] target : REDIRECTS) {
                if (!redirect(cn, target[0], target[1])) {
                    throw new IllegalStateException("Failed to find PacketCustom." + target[0] + target[1]);
                }
            }

            ClassWriter cw = new MixinClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            fail(TARGET, t);
            return basicClass;
        }
    }
}