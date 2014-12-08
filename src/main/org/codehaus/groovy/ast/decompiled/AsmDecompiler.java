package org.codehaus.groovy.ast.decompiled;

import org.objectweb.asm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Gromov
 */
public abstract class AsmDecompiler {

    public static ClassStub parseClass(InputStream stream) throws IOException {
        DecompilingVisitor visitor = new DecompilingVisitor();
        new ClassReader(new BufferedInputStream(stream)).accept(visitor, ClassReader.SKIP_FRAMES);
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
            result = new ClassStub(fromInternalName(name), access, signature, superName, interfaceNames);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            result.innerClassModifiers.put(innerName, access);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!"<clinit>".equals(name)) {
                final MethodStub stub = new MethodStub(name, access, desc, signature, exceptions != null ? exceptions : EMPTY_STRING_ARRAY);
                result.methods.add(stub);
                return new MethodVisitor(api) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        return readAnnotationMembers(stub.addAnnotation(desc));
                    }

                    @Override
                    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                        List<AnnotationStub> list = stub.parameterAnnotations.get(parameter);
                        if (list == null) {
                            stub.parameterAnnotations.put(parameter, list = new ArrayList<AnnotationStub>());
                        }
                        AnnotationStub annotationStub = new AnnotationStub(desc);
                        list.add(annotationStub);
                        return readAnnotationMembers(annotationStub);
                    }

                    @Override
                    public AnnotationVisitor visitAnnotationDefault() {
                        return new AnnotationReader() {
                            @Override
                            void visitAttribute(String name, Object value) {
                                stub.annotationDefault = value;
                            }
                        };
                    }
                };
            }
            return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return readAnnotationMembers(result.addAnnotation(desc));
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            final FieldStub stub = new FieldStub(name, access, desc, signature);
            result.fields.add(stub);
            return new FieldVisitor(api) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return readAnnotationMembers(stub.addAnnotation(desc));
                }
            };
        }
    }

    private static AnnotationReader readAnnotationMembers(final AnnotationStub stub) {
        return new AnnotationReader() {
            @Override
            void visitAttribute(String name, Object value) {
                stub.members.put(name, value);
            }
        };
    }

    static String fromInternalName(String name) {
        return name.replace('/', '.');
    }

    private static abstract class AnnotationReader extends AnnotationVisitor {
        public AnnotationReader() {
            super(Opcodes.ASM5);
        }

        abstract void visitAttribute(String name, Object value);

        @Override
        public void visit(String name, Object value) {
            visitAttribute(name, value instanceof Type ? new TypeWrapper(((Type) value).getDescriptor()) : value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            visitAttribute(name, new EnumConstantWrapper(desc, value));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            AnnotationStub stub = new AnnotationStub(desc);
            visitAttribute(name, stub);
            return readAnnotationMembers(stub);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            final ArrayList<Object> list = new ArrayList<Object>();
            visitAttribute(name, list);
            return new AnnotationReader() {
                @Override
                void visitAttribute(String name, Object value) {
                    list.add(value);
                }
            };
        }

    }
}

