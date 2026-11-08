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

import org.codehaus.groovy.ast.ASTNode;
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
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
 * @since 7.0.0
 */
public final class RuntimePackageCheck {

    private RuntimePackageCheck() {
    }

    /**
     * Checks a call of a constructor. There is no subclass rule for {@code new}:
     * only {@code super(...)} can call a protected constructor from a subclass.
     * <p>
     * A {@code super(...)} call without a source position in a generated
     * constructor, as {@code @InheritConstructors} adds for each constructor of
     * the superclass, is not reported: it fails only if that constructor is called.
     *
     * @param controller the writer of the current class
     * @param ctor       the constructor that is called
     * @param superCall  whether the call is {@code super(...)}
     * @param call       where to report it
     */
    public static void checkConstructor(final WriterController controller, final ConstructorNode ctor, final boolean superCall, final ASTNode call) {
        if (isRefused(controller, ctor.getModifiers(), ctor.getDeclaringClass()) && !(superCall && (ctor.isProtected() || isGeneratedCall(controller, call)))) {
            report(controller, "call", ctor.isProtected(), "constructor", ctor.getDeclaringClass(), call);
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
            report(controller, "call", method.isProtected(), "method " + method.getName(), method.getDeclaringClass(), call);
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
            report(controller, "access", field.isProtected(), "field " + field.getName(), field.getDeclaringClass(), expression);
        }
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
        if (!(controller.getInvocationWriter() instanceof StaticInvocationWriter)) return false; // dynamic code uses the meta-class
        SourceUnit sourceUnit = controller.getSourceUnit();
        Object option = sourceUnit.getConfiguration().getOptimizationOptions().get(CompilerConfiguration.RUNTIME_PACKAGE_CHECK);
        return option != null ? Boolean.TRUE.equals(option) // see GroovyClassLoader for the default of a GroovyShell
            : Boolean.TRUE.equals(sourceUnit.getAST().getUnit().getNodeMetaData(CompilerConfiguration.RUNTIME_PACKAGE_CHECK));
    }

    private static void report(final WriterController controller, final String action, final boolean isProtected, final String member, final ClassNode declaringClass, final ASTNode node) {
        String message = "Cannot " + action + " " + (isProtected ? "protected" : "package-private") + " " + member + " of " + declaringClass.toString(false)
                + " from class " + controller.getClassNode().toString(false) + ": " + declaringClass.getNameWithoutPackage() + " is defined by another class loader,"
                + " so the JVM refuses this access. Use @CompileDynamic to reach it through the meta-class";
        int line = node.getLineNumber(), column = node.getColumnNumber();
        if (line < 1) { // a synthetic node: fall back to the current statement
            line = controller.getLineNumber();
            column = 1;
        }
        controller.getSourceUnit().addError(new SyntaxException(message, line, column));
    }
}
