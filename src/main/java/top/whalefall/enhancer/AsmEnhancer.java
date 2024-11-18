package top.whalefall.enhancer;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Liu Yu
 * @date 2024-11-15 15:20:00
 */
public class AsmEnhancer {

    public static byte[] enhanceClass(byte[] bytes) {
        ClassWriter classWriter = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM7, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MyMethodVisitor(this.api, methodVisitor);
            }
        };
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classVisitor, 0);

        return classWriter.toByteArray();
    }
}

class MyMethodVisitor extends MethodVisitor {

    public MyMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv);
    }

    // 方法执行一开始，调用System.out.println方法
    @Override
    public void visitCode() {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("开始执行");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        super.visitCode();
    }

    // 返回时，执行System.out.println
    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN || opcode == RETURN) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("结束执行");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
        super.visitInsn(opcode);
    }

    // 指定最大栈深度20，最大局部变量表大小50
    @Override
    public void visitEnd() {
        mv.visitMaxs(20, 50);
        super.visitEnd();
    }
}

