package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

final class FMLNetworkTransformer {
    static final String TARGET_DISPATCHER = "net.minecraftforge.fml.common.network.handshake.NetworkDispatcher";
    static final String TARGET_PACKET = "net.minecraftforge.fml.common.network.internal.FMLProxyPacket";
    private static final ObfSafeName write = new ObfSafeName("write", "write");
    private static final ObfSafeName toS3FPackets = new ObfSafeName("toS3FPackets", "toS3FPackets");

    private static void nuke(MethodNode mn, InsnList body) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        if (mn.localVariables != null) mn.localVariables.clear();
        mn.instructions.add(body);
        mn.maxStack = 0;
        mn.maxLocals = 0;
    }

    private static boolean patchNetworkDispatcher(ClassNode cn) {
        boolean patched = false;

        for (MethodNode mn : cn.methods) {
            if (write.matches(mn.name) && "(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Object;Lio/netty/channel/ChannelPromise;)V".equals(mn.desc)) {
                coreLogger.info("Patching NetworkDispatcher.write{}", mn.desc);
                InsnList body = new InsnList();
                body.add(new VarInsnNode(ALOAD, 0));
                body.add(new VarInsnNode(ALOAD, 1));
                body.add(new VarInsnNode(ALOAD, 2));
                body.add(new VarInsnNode(ALOAD, 3));
                body.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/FMLNetworkHook", "networkDispatcherWrite",
                        "(Lnet/minecraftforge/fml/common/network/handshake/NetworkDispatcher;Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Object;Lio/netty/channel/ChannelPromise;)V",
                        false));
                body.add(new InsnNode(RETURN));

                nuke(mn, body);
                patched = true;
                break;
            }
        }

        return patched;
    }

    private static boolean patchFMLProxyPacket(ClassNode cn) {
        boolean patched = false;
        for (MethodNode mn : cn.methods) {
            if (toS3FPackets.matches(mn.name) && "()Ljava/util/List;".equals(mn.desc)) {
                coreLogger.info("Patching FMLProxyPacket.toS3FPackets{}", mn.desc);
                InsnList body = new InsnList();
                body.add(new VarInsnNode(ALOAD, 0));
                body.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/FMLNetworkHook", "fmlProxyPacketToS3FPackets", "(Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;)Ljava/util/List;", false));
                body.add(new InsnNode(ARETURN));
                nuke(mn, body);
                patched = true;
                break;
            }
        }
        return patched;
    }

    static byte[] transformNetworkDispatcher(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);
        try {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            boolean ok = patchNetworkDispatcher(cn);
            if (!ok) throw new IllegalStateException("Failed to patch NetworkDispatcher.write");
            ClassWriter cw = new MinecraftClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            fail(transformedName, t);
            return basicClass;
        }
    }

    static byte[] transformFMLProxyPacket(String name, String transformedName, byte[] basicClass) {
        coreLogger.info("Patching class {} / {}", transformedName, name);
        try {
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            boolean ok = patchFMLProxyPacket(cn);
            if (!ok) throw new IllegalStateException("Failed to patch FMLProxyPacket.toS3FPackets");
            ClassWriter cw = new MinecraftClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            fail(transformedName, t);
            return basicClass;
        }
    }
}
