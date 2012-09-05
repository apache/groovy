package org.codehaus.groovy;

import org.objectweb.asm.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class ExceptionUtilsGenerator implements Opcodes {
    private final static Logger LOGGER = Logger.getLogger(ExceptionUtilsGenerator.class.getName());

	public static void main(String... args) {
        if (args==null || args.length==0) {
            throw new IllegalArgumentException("You must specify at least one file");
        }

		ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "org/codehaus/groovy/runtime/ExceptionUtils", null, "java/lang/Object", null);

        cw.visitSource("ExceptionUtils.java", null);

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(18, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "Lorg/codehaus/groovy/runtime/ExceptionUtils;", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "sneakyThrow", "(Ljava/lang/Throwable;)V", null, null);
        mv.visitCode();
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(20, l2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ATHROW);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l2, l3, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        LOGGER.info("Generating ExceptionUtils");
        byte[] bytes = cw.toByteArray();
        for (String classFilePath : args) {
            File classFile = new File(classFilePath);
            if (classFile.getParentFile().exists() || classFile.getParentFile().mkdirs()) {
                try {
                    if (classFile.exists()) {
                        classFile.delete();
                    }
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(classFile));
                    bos.write(bytes);
                    bos.close();
                } catch (IOException e) {
                    LOGGER.warning("Unable to write file "+classFile);
                }
            } else {
                LOGGER.warning("Unable to create directory "+classFile.getParentFile());
            }
        }
	}
}
