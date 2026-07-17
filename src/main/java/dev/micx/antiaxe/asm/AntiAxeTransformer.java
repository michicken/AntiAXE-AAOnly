package dev.micx.antiaxe.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Inserts one local early return before every vanilla right-click interaction path. */
public final class AntiAxeTransformer implements IClassTransformer {

    static final String GUARD_OWNER = "dev/micx/antiaxe/AntiAxeGuard";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !"net.minecraft.client.Minecraft".equals(transformedName)) {
            return basicClass;
        }
        ClassNode node = new ClassNode(Opcodes.ASM5);
        new ClassReader(basicClass).accept(node, 0);
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (!"()V".equals(method.desc) || !isRightClickMethod(node.name, method)) continue;
            if (alreadyInjected(method)) return basicClass;
            LabelNode proceed = new LabelNode();
            InsnList guard = new InsnList();
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD_OWNER,
                "shouldBlockRightClick", "()Z", false));
            guard.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(proceed);
            guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            method.instructions.insert(guard);
            changed = true;
            break;
        }
        if (!changed) throw new IllegalStateException("AntiAXE could not find Minecraft.rightClickMouse");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static boolean isRightClickMethod(String owner, MethodNode method) {
        if ("rightClickMouse".equals(method.name) || "func_147121_ag".equals(method.name)
                || "ax".equals(method.name)) return true;
        String mapped = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, method.name, method.desc);
        return "func_147121_ag".equals(mapped) || "rightClickMouse".equals(mapped);
    }

    private static boolean alreadyInjected(MethodNode method) {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (GUARD_OWNER.equals(call.owner) && "shouldBlockRightClick".equals(call.name)) return true;
        }
        return false;
    }
}
