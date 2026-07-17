package dev.micx.antiaxe;

import dev.micx.antiaxe.asm.AntiAxeTransformer;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AntiAxeTransformerTest {

    @Test
    public void guardIsInjectedBeforeTheOriginalRightClickBody() {
        ClassNode fixture = new ClassNode(Opcodes.ASM5);
        fixture.version = Opcodes.V1_8;
        fixture.access = Opcodes.ACC_PUBLIC;
        fixture.name = "net/minecraft/client/Minecraft";
        fixture.superName = "java/lang/Object";
        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, "rightClickMouse", "()V", null, null);
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System",
            "nanoTime", "()J", false));
        method.instructions.add(new InsnNode(Opcodes.POP2));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        fixture.methods.add(method);
        ClassWriter source = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        fixture.accept(source);

        byte[] transformed = new AntiAxeTransformer().transform(
            "ave", "net.minecraft.client.Minecraft", source.toByteArray());
        ClassNode result = new ClassNode(Opcodes.ASM5);
        new ClassReader(transformed).accept(result, 0);
        int guardCalls = 0;
        int returns = 0;
        int guardIndex = -1;
        int vanillaBodyIndex = -1;
        int index = 0;
        for (AbstractInsnNode instruction : result.methods.get(0).instructions.toArray()) {
            if (instruction instanceof MethodInsnNode
                    && "shouldBlockRightClick".equals(((MethodInsnNode) instruction).name)) {
                guardCalls++;
            }
            if (instruction instanceof MethodInsnNode
                    && "nanoTime".equals(((MethodInsnNode) instruction).name)) vanillaBodyIndex = index;
            if (instruction instanceof MethodInsnNode
                    && "shouldBlockRightClick".equals(((MethodInsnNode) instruction).name)) guardIndex = index;
            if (instruction.getOpcode() == Opcodes.RETURN) returns++;
            index++;
        }
        assertEquals(1, guardCalls);
        assertEquals(2, returns);
        assertTrue("guard must run before vanilla right-click code", guardIndex >= 0 && guardIndex < vanillaBodyIndex);
    }
}
