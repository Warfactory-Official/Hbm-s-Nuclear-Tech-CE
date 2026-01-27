package com.hbm.core;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

public final class AsmHelper {
    private AsmHelper() {
    }

    static AbstractInsnNode firstRealInsn(MethodNode method) {
        return firstRealInsn(method.instructions);
    }

    static AbstractInsnNode firstRealInsn(InsnList list) {
        AbstractInsnNode cur = list.getFirst();
        while (isSyntheticNode(cur)) {
            cur = cur.getNext();
        }
        return cur;
    }

    static AbstractInsnNode firstRealInsnOrHead(InsnList list) {
        AbstractInsnNode cur = firstRealInsn(list);
        return cur == null ? list.getFirst() : cur;
    }

    static AbstractInsnNode nextRealInsn(AbstractInsnNode insn) {
        AbstractInsnNode n = insn;
        do {
            n = n.getNext();
        } while (isSyntheticNode(n));
        return n;
    }

    private static boolean isSyntheticNode(AbstractInsnNode node) {
        return node instanceof LabelNode || node instanceof LineNumberNode || node instanceof FrameNode;
    }
}
