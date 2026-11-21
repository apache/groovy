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
package org.codehaus.groovy.classgen.asm.sc;

import groovy.transform.Internal;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.isGenerated;
import static org.apache.groovy.ast.tools.ClassNodeUtils.samePackageName;

/**
 * Reports what statically compiled code accesses in another run-time package,
 * which is the package name and the class loader, when
 * {@link CompilerConfiguration#RUNTIME_PACKAGE_CHECK} applies. The language
 * allows such access, as the package name is the same, but the JVM refuses it
 * at run time with {@code IllegalAccessError}.
 *
 * @see AsmClassGenerator#isInSameRuntimePackage(ClassNode, ClassNode)
 * @since 6.0.1
 */
@Internal
public final class RuntimePackageCheck {

    private static final String USE_COMPILE_DYNAMIC = "Use @CompileDynamic to reach it through the meta-class";

    private RuntimePackageCheck() {
    }

    /**
     * Checks a call of a constructor. There is no subclass rule for {@code new}:
     * only {@code super(...)} can call a protected constructor from a subclass.
     * <p>
     * A {@code super(...)} call without a source position in a generated
     * constructor, as {@code @InheritConstructors} adds for each constructor of
     * the superclass, is not reported: it fails only if that constructor is called.
     * Dynamic code calls a super constructor without the meta-class too, so the
     * report of such a call does not advise {@code @CompileDynamic}.
     *
     * @param controller the writer of the current class
     * @param ctor       the constructor that is called
     * @param superCall  whether the call is {@code super(...)}
     * @param call       where to report it
     */
    public static void checkConstructor(final WriterController controller, final ConstructorNode ctor, final boolean superCall, final ASTNode call) {
        if (isRefused(controller, ctor.getModifiers(), ctor.getDeclaringClass()) && !(superCall && (ctor.isProtected() || isGeneratedCall(controller, call)))) {
            report(controller, "call", ctor.isProtected(), "constructor", ctor.getDeclaringClass(), call,
                    superCall ? "A super constructor call fails at run time in dynamic code too" : USE_COMPILE_DYNAMIC);
        }
    }

    private static boolean isGeneratedCall(final WriterController controller, final ASTNode call) {
        ConstructorNode current = controller.getConstructorNode();
        return call.getLineNumber() < 1 && current != null && isGenerated(current);
    }

    /**
     * Checks the constructor of the superclass that the generated constructor of
     * an anonymous class calls. That call is selected at run time, so the arguments
     * of the anonymous class creation tell which constructor it is, if they tell.
     *
     * @param controller the writer of the current class
     * @param call       the creation of the anonymous class
     */
    public static void checkAnonymousClass(final WriterController controller, final ConstructorCallExpression call) {
        if (!isEnabled(controller)) return;
        ClassNode type = call.getType(), superClass = type.getSuperClass();
        List<Expression> arguments = ((TupleExpression) call.getArguments()).getExpressions();
        // "this" and the shared variables come before the arguments of the creation, see InnerClassVisitor
        int leading = (type.getDeclaredField("this$0") != null ? 1 : 0) + ((InnerClassNode) type.redirect()).getVariableScope().getReferencedLocalVariablesCount();
        ClassNode[] argumentTypes = arguments.subList(leading, arguments.size()).stream()
                .map(argument -> controller.getTypeChooser().resolveType(argument, controller.getClassNode())).toArray(ClassNode[]::new);
        List<MethodNode> ctors = StaticTypeCheckingSupport.chooseBestMethod(superClass, new ArrayList<>(superClass.getDeclaredConstructors()), argumentTypes);
        if (ctors.size() == 1 && ctors.get(0) instanceof ConstructorNode ctor) {
            checkConstructor(controller, ctor, true, call);
        }
    }

    /**
     * Checks a call of a method.
     *
     * @param controller   the writer of the current class
     * @param method       the method that is called
     * @param receiverType the type of the receiver, or {@code null} for {@code this} or {@code super}
     * @param call         where to report it
     */
    public static void checkMethod(final WriterController controller, final MethodNode method, final ClassNode receiverType, final ASTNode call) {
        if (isRefused(controller, method.getModifiers(), method.getDeclaringClass())
                && !isProtectedAccessOfSubclass(controller, method.isProtected(), method.isStatic(), method.getDeclaringClass(), receiverType)) {
            report(controller, "call", method.isProtected(), "method " + method.getName(), method.getDeclaringClass(), call, USE_COMPILE_DYNAMIC);
        }
    }

    /**
     * Checks a read or write of a field.
     *
     * @param controller   the writer of the current class
     * @param field        the field that is accessed
     * @param receiverType the type of the receiver, or {@code null} for {@code this} or {@code super}
     * @param expression   where to report it
     */
    public static void checkField(final WriterController controller, final FieldNode field, final ClassNode receiverType, final ASTNode expression) {
        if (isRefused(controller, field.getModifiers(), field.getDeclaringClass())
                && !isProtectedAccessOfSubclass(controller, field.isProtected(), field.isStatic(), field.getDeclaringClass(), receiverType)) {
            report(controller, "access", field.isProtected(), "field " + field.getName(), field.getDeclaringClass(), expression, USE_COMPILE_DYNAMIC);
        }
    }

    /**
     * Checks the superclass and interfaces of a statically compiled class. The JVM
     * refuses to load a class whose superclass or interface it cannot access, in
     * dynamic code too, so the report does not advise {@code @CompileDynamic}.
     *
     * @param controller the writer of the class
     */
    public static void checkSuperTypes(final WriterController controller) {
        ClassNode classNode = controller.getClassNode();
        if (!StaticCompilationVisitor.isStaticallyCompiled(classNode) || !isOptionOn(controller.getSourceUnit())) return;
        List<ClassNode> superTypes = new ArrayList<>(List.of(classNode.getInterfaces()));
        if (classNode.getSuperClass() != null) superTypes.add(classNode.getSuperClass());
        for (ClassNode superType : superTypes) {
            if ((superType.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0 && samePackageName(superType, classNode)
                    && !AsmClassGenerator.isInSameRuntimePackage(superType, classNode)) {
                reportClass(controller, superType.getName(), classNode, "A class that extends or implements it fails to load in dynamic code too");
            }
        }
    }

    /**
     * Returns a visitor that reports the instructions of a statically compiled
     * method that name a class the JVM refuses: a class that is not public, with
     * the same package name as the current class, of another run-time package.
     * The JVM checks that wherever an instruction names the class, so a value of
     * such a class can still be stored and passed on as long as none of them does.
     *
     * @param controller the writer of the current class
     * @param delegate   the visitor of the method
     * @return the visitor to use for the instructions of the method
     */
    public static MethodVisitor checkNamedClasses(final WriterController controller, final MethodVisitor delegate) {
        return isOptionOn(controller.getSourceUnit()) ? new NamedClassChecker(controller, delegate) : delegate;
    }

    private static boolean isRefused(final WriterController controller, final int modifiers, final ClassNode declaringClass) {
        if (Modifier.isPublic(modifiers) || Modifier.isPrivate(modifiers)) return false;
        ClassNode accessingClass = controller.getClassNode();
        return samePackageName(declaringClass, accessingClass)
            && !AsmClassGenerator.isInSameRuntimePackage(declaringClass, accessingClass)
            && isEnabled(controller);
    }

    /**
     * JVMS 5.4.4, 4.10.1.8: a subclass can use a protected member of another run-time
     * package, an instance member only through a receiver of the subclass.
     */
    private static boolean isProtectedAccessOfSubclass(final WriterController controller, final boolean isProtected, final boolean isStatic, final ClassNode declaringClass, final ClassNode receiverType) {
        ClassNode accessingClass = controller.getClassNode();
        return isProtected && accessingClass.isDerivedFrom(declaringClass)
            && (isStatic || receiverType == null || receiverType.isDerivedFrom(accessingClass));
    }

    private static boolean isEnabled(final WriterController controller) {
        return controller.getInvocationWriter() instanceof StaticInvocationWriter // dynamic code uses the meta-class
            && isOptionOn(controller.getSourceUnit());
    }

    private static boolean isOptionOn(final SourceUnit sourceUnit) {
        if (sourceUnit == null) return false; // a class generated without one is not defined by a GroovyClassLoader compilation
        Object option = sourceUnit.getConfiguration().getOptimizationOptions().get(CompilerConfiguration.RUNTIME_PACKAGE_CHECK);
        return option != null ? Boolean.TRUE.equals(option) // see GroovyClassLoader for the default of a GroovyShell
            : Boolean.TRUE.equals(sourceUnit.getAST().getUnit().getNodeMetaData(CompilerConfiguration.RUNTIME_PACKAGE_CHECK));
    }

    private static void report(final WriterController controller, final String action, final boolean isProtected, final String member, final ClassNode declaringClass, final ASTNode node, final String advice) {
        report(controller, "Cannot " + action + " " + (isProtected ? "protected" : "package-private") + " " + member + " of " + declaringClass.toString(false),
                declaringClass.getNameWithoutPackage(), node, advice);
    }

    private static void reportClass(final WriterController controller, final String className, final ASTNode node, final String advice) {
        report(controller, "Cannot use package-private class " + className, className.substring(className.lastIndexOf('.') + 1), node, advice);
    }

    private static void report(final WriterController controller, final String problem, final String simpleName, final ASTNode node, final String advice) {
        String message = problem + " from class " + controller.getClassNode().toString(false) + ": " + simpleName + " is defined by another class loader,"
                + " so the JVM refuses this access. " + advice;
        int line = node != null ? node.getLineNumber() : -1, column = node != null ? node.getColumnNumber() : -1;
        if (line < 1) { // a synthetic node: fall back to the current statement
            line = controller.getLineNumber();
            column = 1;
        }
        controller.getSourceUnit().addError(new SyntaxException(message, line, column));
    }

    private static final class NamedClassChecker extends MethodVisitor {

        private final WriterController controller;
        private final Set<String> checked = new HashSet<>();

        NamedClassChecker(final WriterController controller, final MethodVisitor delegate) {
            super(CompilerConfiguration.ASM_API_VERSION, delegate);
            this.controller = controller;
        }

        private void check(final Type type) {
            switch (type.getSort()) {
                case Type.ARRAY -> check(type.getElementType());
                case Type.OBJECT -> checkName(type.getInternalName());
                case Type.METHOD -> {
                    check(type.getReturnType());
                    for (Type argumentType : type.getArgumentTypes()) check(argumentType);
                }
                default -> { }
            }
        }

        private void check(final Object constant) {
            if (constant instanceof Type type) {
                check(type);
            } else if (constant instanceof Handle handle) {
                checkName(handle.getOwner());
                check(Type.getType(handle.getDesc()));
            }
        }

        private void checkOwner(final String owner) {
            check(Type.getObjectType(owner));
        }

        private void checkName(final String internalName) {
            ClassNode classNode = controller.getClassNode();
            int end = internalName.lastIndexOf('/');
            String packageName = end < 0 ? null : internalName.substring(0, end).replace('/', '.');
            if (!Objects.equals(packageName, classNode.getPackageName()) || isGeneratedCode() || !checked.add(internalName)) return;
            if (controller.getInvocationWriter() instanceof StaticInvocationWriter) {
                String className = internalName.replace('/', '.');
                if (!isCheckedSuperType(className) && isClassOfAnotherRuntimePackage(className)) reportClass(controller, className, null, USE_COMPILE_DYNAMIC);
            }
        }

        /**
         * Determines if the instruction is code without a source position in a
         * generated constructor, like the one {@code @InheritConstructors} adds for
         * each constructor of the superclass. Such code fails only if that
         * constructor is called.
         */
        private boolean isGeneratedCode() {
            ConstructorNode ctor = controller.getConstructorNode();
            return controller.getLineNumber() < 1 && ctor != null && isGenerated(ctor);
        }

        /**
         * Determines if {@link #checkSuperTypes} checks the class, as a superclass
         * or interface of the current class, so it is not reported again.
         */
        private boolean isCheckedSuperType(final String className) {
            ClassNode classNode = controller.getClassNode(), superClass = classNode.getSuperClass();
            return StaticCompilationVisitor.isStaticallyCompiled(classNode)
                && ((superClass != null && superClass.getName().equals(className))
                    || Arrays.stream(classNode.getInterfaces()).anyMatch(type -> type.getName().equals(className)));
        }

        private boolean isClassOfAnotherRuntimePackage(final String className) {
            SourceUnit sourceUnit = controller.getSourceUnit();
            if (sourceUnit.getAST().getUnit().getClass(className) != null) return false; // this compilation
            try {
                Class<?> type = sourceUnit.getClassLoader().loadClass(className, false, true);
                return (type.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0 // a protected nested class is public in its class file
                    && !AsmClassGenerator.isInSameRuntimePackage(ClassHelper.make(type), controller.getClassNode());
            } catch (ClassNotFoundException | LinkageError e) {
                return false; // generated with this class, such as a closure
            }
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            checkOwner(type);
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
            checkOwner(owner);
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
            checkOwner(owner);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
            check(Type.getMethodType(descriptor));
            for (Object argument : bootstrapMethodArguments) check(argument);
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitLdcInsn(final Object value) {
            check(value);
            super.visitLdcInsn(value);
        }

        @Override
        public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
            check(Type.getType(descriptor));
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
            if (type != null) checkOwner(type);
            super.visitTryCatchBlock(start, end, handler, type);
        }
    }
}
