package org.codehaus.groovy.classgen.asm.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * A ClassVisitor proxy, which can provide logging bytecode generation
 */
public class LoggableClassVisitor extends ClassVisitor {
    public LoggableClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM6, new TraceClassVisitor(cv, new LoggableTextifier(), null));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        return new TraceMethodVisitor(mv, ((TraceClassVisitor) super.cv).p);
    }
}
