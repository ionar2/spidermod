package com.tterrag.blur;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class BlurTransformer implements IClassTransformer {

    private static final String GUI_SCREEN_CLASS_NAME = "net.minecraft.client.gui.GuiScreen";
    
    private static final String DRAW_WORLD_BAGKGROUND_METHOD = "drawWorldBackground";
    private static final String DRAW_WORLD_BAGKGROUND_METHOD_OBF = "func_146270_b";
    
    private static final String BLUR_MAIN_CLASS = "com/tterrag/blur/Blur";
    private static final String COLOR_HOOK_METHOD_NAME = "getBackgroundColor";
    private static final String COLOR_HOOK_METHOD_DESC = "(Z)I";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(GUI_SCREEN_CLASS_NAME)) {
            System.out.println("Transforming Class [" + transformedName + "], Method [" + DRAW_WORLD_BAGKGROUND_METHOD + "]");

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            Iterator<MethodNode> methods = classNode.methods.iterator();
            
            while (methods.hasNext()) {
                MethodNode m = methods.next();
                if (m.name.equals(DRAW_WORLD_BAGKGROUND_METHOD) || m.name.equals(DRAW_WORLD_BAGKGROUND_METHOD_OBF)) {
                    for (int i = 0; i < m.instructions.size(); i++) {
                        AbstractInsnNode next = m.instructions.get(i);
                        
//                        if (next.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)next).name.equals(DRAW_GRADIENT_RECT_METHOD_NAME)) {
//                            while (!(next instanceof LabelNode)) {
//                                m.instructions.remove(next);
//                                next = m.instructions.get(--i);
//                            }
//                            break;
//                        }
                        if (next.getOpcode() == Opcodes.LDC) {
                            System.out.println("Modifying GUI background darkness... ");
                            AbstractInsnNode colorHook = new MethodInsnNode(Opcodes.INVOKESTATIC, BLUR_MAIN_CLASS, COLOR_HOOK_METHOD_NAME, COLOR_HOOK_METHOD_DESC, false);
                            AbstractInsnNode colorHook2 = colorHook.clone(null);
                            
                            // Replace LDC with hooks
                            m.instructions.set(next, colorHook);
                            m.instructions.set(colorHook.getNext(), colorHook2);

                            // Load boolean constants for method param
                            m.instructions.insertBefore(colorHook, new InsnNode(Opcodes.ICONST_1));
                            m.instructions.insertBefore(colorHook2, new InsnNode(Opcodes.ICONST_0));
                            break;
                        }
                    }
                    break;
                }
            }
            
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            System.out.println("Transforming " + transformedName + " Finished.");
            return cw.toByteArray();
        }
    
        return basicClass;
    }

}
