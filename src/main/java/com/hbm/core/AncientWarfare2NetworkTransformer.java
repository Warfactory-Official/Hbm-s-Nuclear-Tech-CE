package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

/**
 * Ancient Warfare 2 compatibility patch, supports both the original version and tweaked fork.
 * It rewrites NetworkHandler#sendToAllTracking to route through FMLEventChannel,
 * instead of using vanilla EntityTracker, it reuses packets without retaining.
 */
final class AncientWarfare2NetworkTransformer {
    static final String TARGET = "net.shadowmage.ancientwarfare.core.network.NetworkHandler";

    private static final String OWNER = "net/shadowmage/ancientwarfare/core/network/NetworkHandler";
    private static final String PACKET_BASE = "net/shadowmage/ancientwarfare/core/network/PacketBase";
    private static final String METHOD = "sendToAllTracking";
    private static final String METHOD_DESC = "(Lnet/minecraft/entity/Entity;Lnet/shadowmage/ancientwarfare/core/network/PacketBase;)V";

    private static boolean patchSendToAllTracking(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (METHOD.equals(mn.name) && METHOD_DESC.equals(mn.desc)) {
                coreLogger.info("Patching Ancient Warfare method {}{}", METHOD, METHOD_DESC);

                InsnList body = new InsnList();
                // INSTANCE.channel.sendToAllTracking(pkt.getFMLPacket(), entity)
                body.add(new FieldInsnNode(GETSTATIC, OWNER, "INSTANCE", "L" + OWNER + ";"));
                body.add(new FieldInsnNode(GETFIELD, OWNER, "channel", "Lnet/minecraftforge/fml/common/network/FMLEventChannel;"));
                body.add(new VarInsnNode(ALOAD, 1));
                body.add(new MethodInsnNode(INVOKEVIRTUAL, PACKET_BASE, "getFMLPacket", "()Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;", false));
                body.add(new VarInsnNode(ALOAD, 0));
                body.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/FMLEventChannel", "sendToAllTracking", "(Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;Lnet/minecraft/entity/Entity;)V", false));
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

            boolean patched = patchSendToAllTracking(cn);
            if (!patched) {
                throw new IllegalStateException("Failed to find NetworkHandler.sendToAllTracking");
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
