/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.util;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Logging bytecode generation, which can make debugging easy
 *
 * @since 2.5.0
 */
public class LoggableTextifier extends Textifier {
    private static final String GROOVY_LOG_CLASSGEN_STACKTRACE_MAX_DEPTH = "groovy.log.classgen.stacktrace.max.depth";
    private static final String GROOVY = ".groovy.";
    private static final String LOGGABLE_TEXTIFIER = ".LoggableTextifier";
    private static final int STACKTRACE_MAX_DEPTH = Integer.getInteger(GROOVY_LOG_CLASSGEN_STACKTRACE_MAX_DEPTH, 0);
    private int loggedLineCnt = 0;

    public LoggableTextifier() {
        super(CompilerConfiguration.ASM_API_VERSION);
    }

    @Override
    protected Textifier createTextifier() {
        return new LoggableTextifier();
    }

    protected void log() {
        int textSize = text.size();

        List<Object> bcList = new LinkedList<>();
        for (int i = loggedLineCnt; i < textSize; i++) {
            Object bc = text.get(i);

            if (bc instanceof List && 0 == ((List) bc).size()) {
                continue;
            }

            bcList.add(bc);
        }

        if (bcList.size() > 0) {
            List<StackTraceElement> invocationPositionInfo = getInvocationPositionInfo();
            if (invocationPositionInfo.size() > 0) {
                System.out.print(formatInvocationPositionInfo(invocationPositionInfo));
            }

            for (Object bc : bcList) {
                System.out.print(bc);
            }
        }

        loggedLineCnt = textSize;
    }

    private List<StackTraceElement> getInvocationPositionInfo() {
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        List<StackTraceElement> stackTraceElementList = new LinkedList<>();

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            String className = stackTraceElement.getClassName();
            if (className.contains(GROOVY) && !className.endsWith(LOGGABLE_TEXTIFIER)) {
                if (stackTraceElementList.size() >= STACKTRACE_MAX_DEPTH) {
                    break;
                }

                stackTraceElementList.add(stackTraceElement);
            }
        }

        return stackTraceElementList;
    }

    private String formatInvocationPositionInfo(List<StackTraceElement> stackTraceElementList) {
        StringBuilder sb = new StringBuilder(128);
        for (StackTraceElement stackTraceElement : stackTraceElementList) {
            sb.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t// ");
            sb.append(String.format("%s#%s:%s%n", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
        }

        return sb.toString();
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        log();
    }

    @Override
    public void visitSource(String file, String debug) {
        super.visitSource(file, debug);
        log();
    }

    @Override
    public Printer visitModule(final String name, final int access, final String version) {
        Printer p = super.visitModule(name, access, version);
        log();
        return p;
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
        log();
    }

    @Override
    public Textifier visitClassAnnotation(String desc, boolean visible) {
        Textifier t = super.visitClassAnnotation(desc, visible);
        log();
        return t;
    }

    @Override
    public Printer visitClassTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Printer t = super.visitClassTypeAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitClassAttribute(Attribute attr) {
        super.visitClassAttribute(attr);
        log();
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
        log();
    }

    @Override
    public Textifier visitField(int access, String name, String desc, String signature, Object value) {
        Textifier t = super.visitField(access, name, desc, signature, value);
        log();
        return t;
    }

    @Override
    public Textifier visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        Textifier t = super.visitMethod(access, name, desc, signature, exceptions);
        log();
        return t;
    }

    @Override
    public void visitClassEnd() {
        super.visitClassEnd();
        log();
    }

    @Override
    public void visitRequire(String require, int access, String version) {
        super.visitRequire(require, access, version);
        log();
    }

    @Override
    public void visitExport(String export, int access, String... modules) {
        super.visitExport(export, access, modules);
        log();
    }

    @Override
    public void visitUse(String use) {
        super.visitUse(use);
        log();
    }

    @Override
    public void visitProvide(String provide, String... providers) {
        super.visitProvide(provide, providers);
        log();
    }

    @Override
    public void visitModuleEnd() {
        super.visitModuleEnd();
        log();
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);
        log();
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        super.visitEnum(name, desc, value);
        log();
    }

    @Override
    public Textifier visitAnnotation(String name, String desc) {
        Textifier t = super.visitAnnotation(name, desc);
        log();
        return t;
    }

    @Override
    public Textifier visitArray(String name) {
        Textifier t = super.visitArray(name);
        log();
        return t;
    }

    @Override
    public void visitAnnotationEnd() {
        super.visitAnnotationEnd();
        log();
    }

    @Override
    public Textifier visitFieldAnnotation(String desc, boolean visible) {
        Textifier t = super.visitFieldAnnotation(desc, visible);
        log();
        return t;
    }

    @Override
    public Printer visitFieldTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Printer t = super.visitFieldTypeAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitFieldAttribute(Attribute attr) {
        super.visitFieldAttribute(attr);
        log();
    }

    @Override
    public void visitFieldEnd() {
        super.visitFieldEnd();
        log();
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
        log();
    }

    @Override
    public Textifier visitAnnotationDefault() {
        Textifier t = super.visitAnnotationDefault();
        log();
        return t;
    }

    @Override
    public Textifier visitMethodAnnotation(String desc, boolean visible) {
        Textifier t = super.visitMethodAnnotation(desc, visible);
        log();
        return t;
    }

    @Override
    public Printer visitMethodTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Printer t = super.visitMethodTypeAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public Textifier visitParameterAnnotation(int parameter, String desc, boolean visible) {
        Textifier t = super.visitParameterAnnotation(parameter, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitMethodAttribute(Attribute attr) {
        super.visitMethodAttribute(attr);
        log();
    }

    @Override
    public void visitCode() {
        super.visitCode();
        log();
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        super.visitFrame(type, nLocal, local, nStack, stack);
        log();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        log();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        log();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        log();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        log();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
        log();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        super.visitMethodInsn(opcode, owner, name, desc);
        log();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        log();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        log();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        log();
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        log();
    }

    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        log();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        log();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        log();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        log();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        super.visitMultiANewArrayInsn(desc, dims);
        log();
    }

    @Override
    public Printer visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Printer t = super.visitInsnAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        log();
    }

    @Override
    public Printer visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Printer t = super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
        log();
    }

    @Override
    public Printer visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        Printer t = super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        log();
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        log();
    }

    @Override
    public void visitMethodEnd() {
        super.visitMethodEnd();
        log();
    }

    @Override
    public Textifier visitAnnotation(String desc, boolean visible) {
        Textifier t = super.visitAnnotation(desc, visible);
        log();
        return t;
    }

    @Override
    public Textifier visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        Textifier t = super.visitTypeAnnotation(typeRef, typePath, desc, visible);
        log();
        return t;
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
        log();
    }

    @Override
    public void visitNestHost(String nestHost) {
        super.visitNestHost(nestHost);
        log();
    }

    @Override
    public void visitNestMember(String nestMember) {
        super.visitNestMember(nestMember);
        log();
    }

    @Override
    public void visitMainClass(String mainClass) {
        super.visitMainClass(mainClass);
        log();
    }

    @Override
    public void visitPackage(String packaze) {
        super.visitPackage(packaze);
        log();
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        super.visitOpen(packaze, access, modules);
        log();
    }

    @Override
    public Textifier visitAnnotableParameterCount(int parameterCount, boolean visible) {
        Textifier t = super.visitAnnotableParameterCount(parameterCount, visible);
        log();
        return t;
    }

    @Override
    public Printer visitRecordComponent(String name, String descriptor, String signature) {
        Printer p = super.visitRecordComponent(name, descriptor, signature);
        log();
        return p;
    }

    @Override
    public Textifier visitRecordComponentAnnotation(String descriptor, boolean visible) {
        Textifier t = super.visitRecordComponentAnnotation(descriptor, visible);
        log();
        return t;
    }

    @Override
    public Printer visitRecordComponentTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Printer p = super.visitRecordComponentTypeAnnotation(typeRef, typePath, descriptor, visible);
        log();
        return p;
    }

    @Override
    public void visitRecordComponentAttribute(Attribute attribute) {
        super.visitRecordComponentAttribute(attribute);
        log();
    }

    @Override
    public void visitRecordComponentEnd() {
        super.visitRecordComponentEnd();
        log();
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        super.visitPermittedSubclass(permittedSubclass);
        log();
    }
}
