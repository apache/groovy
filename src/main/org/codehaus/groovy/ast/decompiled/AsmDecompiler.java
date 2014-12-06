package org.codehaus.groovy.ast.decompiled;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Peter Gromov
 */
public abstract class AsmDecompiler {

    public static ClassStub parseClass(File file) throws IOException {
        DecompilingVisitor visitor = new DecompilingVisitor();
        new ClassReader(new BufferedInputStream(new FileInputStream(file))).accept(visitor, ClassReader.SKIP_FRAMES);
        return visitor.result;
    }

    private static class DecompilingVisitor extends ClassVisitor {
        private static final String[] EMPTY_STRING_ARRAY = new String[0];
        private ClassStub result;

        public DecompilingVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaceNames) {
            result = new ClassStub(fromInternalName(name), access, superName, interfaceNames);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!"<clinit>".equals(name)) {
                result.methods.add(new MethodStub(name, access, desc, signature, exceptions != null ? exceptions : EMPTY_STRING_ARRAY));
            }
            return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            result.fields.add(new FieldStub(name, access, desc, signature));
            return null;
        }
    }

    static String fromInternalName(String name) {
        return name.replace('/', '.');
    }
}
