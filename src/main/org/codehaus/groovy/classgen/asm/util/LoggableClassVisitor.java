package org.codehaus.groovy.classgen.asm.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * A ClassVisitor proxy, which can log bytecode generation
 */
public class LoggableClassVisitor extends ClassVisitor {
    public LoggableClassVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM6, new TraceClassVisitor(cv, new LoggableTextifier(), null));
    }
}
