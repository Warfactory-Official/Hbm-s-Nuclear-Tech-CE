package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;

final class BlockDummyAirTransformer {
	static final String TARGET = "net.minecraftforge.registries.GameData$BlockCallbacks$BlockDummyAir";

	static byte[] transform(String name, String transformedName, byte[] basicClass) {
		coreLogger.info("Patching class {} / {}", transformedName, name);

		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);

			classNode.superName = "com/hbm/core/BlockMetaAir";

			for (MethodNode method : classNode.methods) {
				for (AbstractInsnNode node : method.instructions.toArray()) {
					if (node instanceof MethodInsnNode mthd) {
						if (mthd.owner.equals("net/minecraft/block/BlockAir"))
							mthd.owner = "com/hbm/core/BlockMetaAir";
					}
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
}
