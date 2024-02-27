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
package org.codehaus.groovy.classgen;

import groovy.transform.Sealed;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isStrict;
import static java.lang.reflect.Modifier.isSynchronized;
import static java.lang.reflect.Modifier.isTransient;
import static java.lang.reflect.Modifier.isVolatile;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_STRICT;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

/**
 * Checks that a class satisfies various conditions including:
 * <ul>
 *     <li>Incorrect class or method access modifiers</li>
 *     <li>No abstract methods appear in a non-abstract class</li>
 *     <li>Existence and correct visibility for inherited members</li>
 *     <li>Invalid attempts to override final members</li>
 * </ul>
 */
public class ClassCompletionVerifier extends ClassCodeVisitorSupport {

    private static final String[] INVALID_NAME_CHARS = {".", ":", "/", ";", "[", "<", ">"};
    // the groovy.compiler.strictNames system property is experimental and may change default value or be removed in a future version of Groovy
    private final boolean strictNames = Boolean.getBoolean("groovy.compiler.strictNames");
    private boolean inConstructor, inStaticConstructor;
    private final SourceUnit source;
    private ClassNode currentClass;

    public ClassCompletionVerifier(final SourceUnit source) {
        this.source = source;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    public ClassNode getClassNode() {
        return currentClass;
    }

    @Override
    public void visitClass(final ClassNode node) {
        ClassNode previousClass = currentClass;
        currentClass = node;
        try {
            checkImplementsAndExtends(node);
            if (source != null && !source.getErrorCollector().hasErrors()) {
                checkClassForIncorrectModifiers(node);
                checkInterfaceMethodVisibility(node);
                checkAbstractMethodVisibility(node);
                checkClassForExtendingFinalOrSealed(node);
                checkMethodsForIncorrectModifiers(node);
                checkMethodsForIncorrectName(node);
                checkMethodsForWeakerAccess(node);
                checkMethodsForOverridingFinal(node);
                checkNoAbstractMethodsNonAbstractClass(node);
                checkClassExtendsOrImplementsSelfTypes(node);
                checkNoStaticMethodWithSameSignatureAsNonStatic(node);
                checkGenericsUsage(node, node.getUnresolvedInterfaces());
                checkGenericsUsage(node, node.getUnresolvedSuperClass());
            }
            super.visitClass(node);
        } finally {
            currentClass = previousClass;
        }
    }

    private void checkNoStaticMethodWithSameSignatureAsNonStatic(final ClassNode node) {
        ClassNode parent = node.getSuperClass();
        Map<String, MethodNode> result;
        // start with methods from the parent if any
        if (parent != null) {
            result = parent.getDeclaredMethodsMap();
        } else {
            result = new HashMap<>();
        }
        // add in unimplemented abstract methods from the interfaces
        ClassNodeUtils.addDeclaredMethodsFromInterfaces(node, result);
        for (MethodNode methodNode : node.getMethods()) {
            MethodNode mn = result.get(methodNode.getTypeDescriptor());
            if (mn != null && (mn.isStatic() ^ methodNode.isStatic()) && !methodNode.isStaticConstructor()) {
                if (!mn.isAbstract()) continue;
                ClassNode declaringClass = mn.getDeclaringClass();
                ClassNode cn = declaringClass.getOuterClass();
                if (cn == null && declaringClass.isResolved()) {
                    // in case of a precompiled class, the outerclass is unknown
                    Class<?> typeClass = declaringClass.getTypeClass();
                    typeClass = typeClass.getEnclosingClass();
                    if (typeClass != null) {
                        cn = ClassHelper.make(typeClass);
                    }
                }
                if (!Traits.isTrait(cn)) {
                    ASTNode errorNode = methodNode;
                    String name = mn.getName();
                    if (errorNode.getLineNumber() == -1) {
                        // try to get a better error message location based on the property
                        for (PropertyNode propertyNode : node.getProperties()) {
                            if (name.startsWith("set") || name.startsWith("get") || name.startsWith("is")) {
                                String propName = Verifier.capitalize(propertyNode.getField().getName());
                                String shortName = name.substring(name.startsWith("is") ? 2 : 3);
                                if (propName.equals(shortName)) {
                                    errorNode = propertyNode;
                                    break;
                                }
                            }
                        }
                    }
                    addError("The " + getDescription(methodNode) + " is already defined in " + getDescription(node) +
                            ". You cannot have both a static and an instance method with the same signature", errorNode);
                }
            }
            result.put(methodNode.getTypeDescriptor(), methodNode);
        }
    }

    private void checkInterfaceMethodVisibility(final ClassNode node) {
        if (!node.isInterface()) return;
        for (MethodNode method : node.getMethods()) {
            if (!method.isPublic()) {
                if (method.isAbstract()) {
                    addError("The method '" + method.getName() + "' must be public as it is declared abstract in " + getDescription(node) + ".", method);
                } else if (!method.isPrivate() && !method.isStaticConstructor()) {
                    // normal parsing blocks non-static protected or @PackageScope
                    addError("The method '" + method.getName() + "' is " + (method.isProtected() ? "protected" : "package-private") +
                            " but must be " + (method.isStatic() ? "public" : "default") + " or private in " + getDescription(node) + ".", method);
                }
            }
        }
    }

    private void checkAbstractMethodVisibility(final ClassNode node) {
        // we only do check abstract classes (including enums), no interfaces or non-abstract classes
        if (!node.isAbstract() || node.isInterface()) return;
        for (MethodNode method : node.getAbstractMethods()) {
            if (method.isPrivate()) {
                addError("The method '" + method.getName() + "' must not be private as it is declared abstract in " + getDescription(node) + ".", method);
            }
        }
    }

    private void checkNoAbstractMethodsNonAbstractClass(final ClassNode node) {
        if (node.isAbstract()) return;
        for (MethodNode method : node.getAbstractMethods()) {
            MethodNode sameArgsMethod = node.getMethod(method.getName(), method.getParameters());
            if (sameArgsMethod == null) {
                sameArgsMethod = ClassHelper.GROOVY_OBJECT_TYPE.getMethod(method.getName(), method.getParameters());
                if (sameArgsMethod != null && !sameArgsMethod.isAbstract() && sameArgsMethod.getReturnType().equals(method.getReturnType())) {
                    continue;
                }
            }

            String what; ASTNode where = node;
            if (sameArgsMethod == null || sameArgsMethod.getReturnType().equals(method.getReturnType())) {
                what = "Can't have an abstract method in a non-abstract class." +
                        " The " + getDescription(node) + " must be declared abstract or" +
                        " the " + getDescription(method) + " must be implemented.";
            } else {
                what = "Abstract " + getDescription(method) + " is not implemented but a " +
                        "method of the same name but different return type is defined: " +
                        (sameArgsMethod.isStatic() ? "static " : "") + getDescription(sameArgsMethod);
                where = method;
            }
            addError(what, where);
        }
    }

    private void checkClassExtendsOrImplementsSelfTypes(final ClassNode node) {
        if (node.isInterface()) return;
        for (ClassNode anInterface : GeneralUtils.getInterfacesAndSuperInterfaces(node)) {
            if (Traits.isTrait(anInterface)) {
                for (ClassNode selfType : Traits.collectSelfTypes(anInterface, new LinkedHashSet<>(), true, false)) {
                    ClassNode superClass;
                    if (selfType.isInterface() ? !node.implementsInterface(selfType) : !(node.isDerivedFrom(selfType)
                            || ((superClass = node.getNodeMetaData("super.class")) != null && superClass.isDerivedFrom(selfType)))) {
                        addError(getDescription(node) + " implements " + getDescription(anInterface) + " but does not " +
                            (selfType.isInterface() ? "implement" : "extend") + " self type " + getDescription(selfType), anInterface);
                    }
                }
            }
        }
    }

    private void checkClassForIncorrectModifiers(final ClassNode node) {
        checkClassForAbstractAndFinal(node);
        checkClassForOtherModifiers(node);
    }

    private void checkClassForAbstractAndFinal(final ClassNode node) {
        if (!node.isAbstract() || !isFinal(node.getModifiers())) return;
        if (node.isInterface()) {
            addError("The " + getDescription(node) + " must not be final. It is by definition abstract.", node);
        } else {
            addError("The " + getDescription(node) + " must not be both final and abstract.", node);
        }
    }

    private void checkClassForOtherModifiers(final ClassNode node) {
        checkClassForModifier(node, isTransient(node.getModifiers()), "transient");
        checkClassForModifier(node, isVolatile(node.getModifiers()), "volatile");
        checkClassForModifier(node, isNative(node.getModifiers()), "native");
        if (!(node instanceof InnerClassNode)) {
            checkClassForModifier(node, isStatic(node.getModifiers()), "static");
            checkClassForModifier(node, isPrivate(node.getModifiers()), "private");
        }
        // don't check synchronized here as it overlaps with ACC_SUPER
    }

    private void checkMethodForModifier(final MethodNode node, final boolean condition, final String modifierName) {
        if (!condition) return;
        addError("The " + getDescription(node) + " has an incorrect modifier " + modifierName + ".", node);
    }

    private void checkClassForModifier(final ClassNode node, final boolean condition, final String modifierName) {
        if (!condition) return;
        addError("The " + getDescription(node) + " has an incorrect modifier " + modifierName + ".", node);
    }

    private static String getDescription(final ClassNode node) {
        String kind = (node.isInterface() ? (Traits.isTrait(node) ? "trait" : "interface") : (node.isEnum() ? "enum" : "class"));
        return kind + " '" + node.getName() + "'";
    }

    private static String getDescription(final MethodNode node) {
        return "method '" + node.getTypeDescriptor() + "'";
    }

    private static String getDescription(final FieldNode node) {
        return "field '" + node.getName() + "'";
    }

    private static String getDescription(final PropertyNode node) {
        return "property '" + node.getName() + "'";
    }

    private static String getDescription(final Parameter node) {
        return "parameter '" + node.getName() + "'";
    }

    private void checkAbstractDeclaration(final MethodNode methodNode) {
        if (!methodNode.isAbstract() || currentClass.isAbstract() || methodNode.isDefault()) return;

        addError("Can't have an abstract method in a non-abstract class." +
                " The " + getDescription(currentClass) + " must be declared abstract or the method '" +
                methodNode.getTypeDescriptor() + "' must not be abstract.", methodNode);
    }

    private void checkClassForExtendingFinalOrSealed(final ClassNode cn) {
        boolean sealed = Boolean.TRUE.equals(cn.getNodeMetaData(Sealed.class));
        if (sealed && cn.getPermittedSubclasses().isEmpty()) {
            addError("Sealed " + getDescription(cn) + " has no explicit or implicit permitted subclasses.", cn);
            return;
        }
        boolean isFinal = isFinal(cn.getModifiers());
        if (sealed && isFinal) {
            addError("The " + getDescription(cn) + " cannot be both final and sealed.", cn);
            return;
        }
        boolean explicitNonSealed = nonSealed(cn);
        ClassNode superCN = cn.getSuperClass();
        boolean sealedSuper = superCN != null && superCN.isSealed();
        boolean sealedInterface = Arrays.stream(cn.getInterfaces()).anyMatch(ClassNode::isSealed);
        boolean nonSealedSuper = superCN != null && nonSealed(superCN);
        boolean nonSealedInterface = Arrays.stream(cn.getInterfaces()).anyMatch(this::nonSealed);

        if (explicitNonSealed && !(sealedSuper || sealedInterface || nonSealedSuper || nonSealedInterface)) {
            addError("The " + getDescription(cn) + " cannot be non-sealed as it has no sealed parent.", cn);
            return;
        }
        if (sealedSuper || sealedInterface) {
            if (sealed && explicitNonSealed) {
                addError("The " + getDescription(cn) + " cannot be both sealed and non-sealed.", cn);
                return;
            }
            if (isFinal && explicitNonSealed) {
                addError("The " + getDescription(cn) + " cannot be both final and non-sealed.", cn);
                return;
            }
            if (sealedSuper) {
                checkSealedParent(cn, superCN);
            }
            if (sealedInterface) {
                for (ClassNode candidate : cn.getInterfaces()) {
                    if (candidate.isSealed()) {
                        checkSealedParent(cn, candidate);
                    }
                }
            }
        }
        if (superCN == null || !isFinal(superCN.getModifiers())) return;
        addError("You are not allowed to extend the final " + getDescription(superCN) + ".", cn);
    }

    private boolean nonSealed(final ClassNode node) {
        return ClassNodeUtils.isNonSealed(node);
    }

    private void checkSealedParent(final ClassNode cn, final ClassNode parent) {
        boolean found = false;
        for (ClassNode permitted : parent.getPermittedSubclasses()) {
            if (permitted.equals(cn)) {
                found = true;
                break;
            }
        }
        if (!found) {
            addError("The " + getDescription(cn) + " is not a permitted subclass of the sealed " + getDescription(parent) + ".", cn);
        }
    }

    private void checkImplementsAndExtends(final ClassNode node) {
        if (!node.isInterface()) {
            ClassNode type = node.getUnresolvedSuperClass();
            if (type != null && type.isInterface()) {
                addError("You are not allowed to extend the " + getDescription(type) + ", use implements instead.", node);
            }
        }
        for (ClassNode type : node.getInterfaces()) {
            if (!type.isInterface()) {
                addError("You are not allowed to implement the " + getDescription(type) + ", use extends instead.", node);
            } else if (type.isSealed()) {
                checkSealedParent(node, type);
            }
        }
    }

    private void checkMethodsForIncorrectName(final ClassNode cn) {
        if (!strictNames) return;
        List<MethodNode> methods = cn.getAllDeclaredMethods();
        for (MethodNode mNode : methods) {
            if (mNode.isConstructor() || mNode.isStaticConstructor()) continue;
            String name = mNode.getName();
            // Groovy allows more characters than Character.isValidJavaIdentifier() would allow
            // if we find a good way to encode special chars we could remove (some of) these checks
            for (String ch : INVALID_NAME_CHARS) {
                if (name.contains(ch)) {
                    addError("You are not allowed to have '" + ch + "' in a method name", mNode);
                }
            }
        }
    }

    private void checkMethodsForIncorrectModifiers(final ClassNode cn) {
        if (!cn.isInterface()) return;
        for (MethodNode method : cn.getMethods()) {
            if (method.isFinal()) {
                addError("The " + getDescription(method) + " from " + getDescription(cn) +
                        " must not be final. It is by definition abstract.", method);
            }
        }
    }

    private void checkMethodsForWeakerAccess(final ClassNode cn) {
        for (MethodNode method : cn.getMethods()) {
            checkMethodForWeakerAccessPrivileges(method, cn);
        }
    }

    private void checkMethodsForOverridingFinal(final ClassNode cn) {
        for (MethodNode method : cn.getMethods()) {
            Parameter[] params = method.getParameters();
            for (MethodNode superMethod : cn.getSuperClass().getMethods(method.getName())) {
                Parameter[] superParams = superMethod.getParameters();
                if (!ParameterUtils.parametersEqual(params, superParams)) continue;
                if (!superMethod.isFinal()) break;
                addInvalidUseOfFinalError(method, params, superMethod.getDeclaringClass());
                return;
            }
        }
    }

    private void addInvalidUseOfFinalError(final MethodNode method, final Parameter[] parameters, final ClassNode superCN) {
        StringBuilder msg = new StringBuilder();
        msg.append("You are not allowed to override the final method ").append(method.getName());
        appendParamsDescription(parameters, msg);
        msg.append(" from ").append(getDescription(superCN));
        msg.append(".");
        addError(msg.toString(), method);
    }

    private void appendParamsDescription(final Parameter[] parameters, final StringBuilder msg) {
        msg.append('(');
        boolean needsComma = false;
        for (Parameter parameter : parameters) {
            if (needsComma) {
                msg.append(',');
            } else {
                needsComma = true;
            }
            msg.append(parameter.getType());
        }
        msg.append(')');
    }

    private void addWeakerAccessError(final ClassNode cn, final MethodNode method, final Parameter[] parameters, final MethodNode superMethod) {
        StringBuilder msg = new StringBuilder();
        msg.append(method.getName());
        appendParamsDescription(parameters, msg);
        msg.append(" in ");
        msg.append(cn.getName());
        msg.append(" cannot override ");
        msg.append(superMethod.getName());
        msg.append(" in ");
        msg.append(superMethod.getDeclaringClass().getName());
        msg.append("; attempting to assign weaker access privileges; was ");
        msg.append(superMethod.isPublic() ? "public" : (superMethod.isProtected() ? "protected" : "package-private"));
        addError(msg.toString(), method);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        inConstructor = false;
        inStaticConstructor = node.isStaticConstructor();
        checkAbstractDeclaration(node);
        checkRepetitiveMethod(node);
        checkOverloadingPrivateAndPublic(node);
        checkMethodModifiers(node);
        checkGenericsUsage(node, node.getParameters());
        checkGenericsUsage(node, node.getReturnType());
        for (Parameter param : node.getParameters()) {
            if (ClassHelper.isPrimitiveVoid(param.getType())) {
                addError("The " + getDescription(param) + " in " +  getDescription(node) + " has invalid type void", param);
            }
        }
        super.visitMethod(node);
    }

    private void checkMethodModifiers(final MethodNode node) {
        // don't check volatile here as it overlaps with ACC_BRIDGE
        // additional modifiers not allowed for interfaces
        if ((this.currentClass.getModifiers() & ACC_INTERFACE) != 0) {
            checkMethodForModifier(node, isStrict(node.getModifiers()), "strictfp");
            checkMethodForModifier(node, isSynchronized(node.getModifiers()), "synchronized");
            checkMethodForModifier(node, isNative(node.getModifiers()), "native");
        }
        // transient overlaps with varargs but we don't add varargs until AsmClassGenerator
        // but we might have varargs set from e.g. @Delegate of a varargs method so skip generated
        if (!AnnotatedNodeUtils.isGenerated(node)) {
            checkMethodForModifier(node, isTransient(node.getModifiers()), "transient");
        }
    }

    private void checkMethodForWeakerAccessPrivileges(final MethodNode mn, final ClassNode cn) {
        if (mn.isPublic()) return;
        Parameter[] params = mn.getParameters();
        for (MethodNode superMethod : cn.getSuperClass().getMethods(mn.getName())) {
            Parameter[] superParams = superMethod.getParameters();
            if (!ParameterUtils.parametersEqual(params, superParams)) continue;
            if ((mn.isPrivate() && !superMethod.isPrivate())
                    || (mn.isProtected() && !superMethod.isProtected() && !superMethod.isPackageScope() && !superMethod.isPrivate())
                    || (!mn.isPrivate() && !mn.isProtected() && !mn.isPublic() && (superMethod.isPublic() || superMethod.isProtected()))) {
                addWeakerAccessError(cn, mn, params, superMethod);
                return;
            }
        }
    }

    private void checkOverloadingPrivateAndPublic(final MethodNode node) {
        if (node.isStaticConstructor()) return;
        boolean hasPrivate = node.isPrivate();
        boolean hasPublic = node.isPublic();
        for (MethodNode method : currentClass.getMethods(node.getName())) {
            if (method == node) continue;
            if (!method.getDeclaringClass().equals(node.getDeclaringClass())) continue;
            if (method.isPublic() || method.isProtected()) {
                hasPublic = true;
            } else {
                hasPrivate = true;
            }
            if (hasPrivate && hasPublic) break;
        }
        if (hasPrivate && hasPublic) {
            addError("Mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden to avoid surprising behaviour. Renaming the private methods will solve the problem.", node);
        }
    }

    private void checkRepetitiveMethod(final MethodNode node) {
        if (node.isStaticConstructor()) return;
        for (MethodNode method : currentClass.getMethods(node.getName())) {
            if (method == node) continue;
            if (!method.getDeclaringClass().equals(node.getDeclaringClass())) continue;
            Parameter[] p1 = node.getParameters();
            Parameter[] p2 = method.getParameters();
            if (p1.length != p2.length) continue;
            addErrorIfParamsAndReturnTypeEqual(p2, p1, node, method);
        }
    }

    private void addErrorIfParamsAndReturnTypeEqual(final Parameter[] p2, final Parameter[] p1, final MethodNode node, final MethodNode element) {
        boolean isEqual = true;
        for (int i = 0; i < p2.length; i++) {
            isEqual &= p1[i].getType().equals(p2[i].getType());
            if (!isEqual) break;
        }
        isEqual &= node.getReturnType().equals(element.getReturnType());
        if (isEqual) {
            addError("Repetitive method name/signature for " + getDescription(node) + " in " + getDescription(currentClass) + ".", node);
        }
    }

    @Override
    public void visitField(final FieldNode node) {
        if (currentClass.getDeclaredField(node.getName()) != node) {
            addError("The " + getDescription(node) + " is declared multiple times.", node);
        }
        checkInterfaceFieldModifiers(node);
        checkInvalidFieldModifiers(node);
        checkGenericsUsage(node, node.getType());
        if (ClassHelper.isPrimitiveVoid(node.getType())) {
            addError("The " + getDescription(node) + " has invalid type void", node);
        }
        super.visitField(node);
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        if (currentClass.getProperty(node.getName()) != node) {
            addError("The " + getDescription(node) + " is declared multiple times.", node);
        }
        checkDuplicateProperties(node);
        checkGenericsUsage(node, node.getType());
        super.visitProperty(node);
    }

    private void checkDuplicateProperties(final PropertyNode node) {
        ClassNode cn = node.getDeclaringClass();
        String name = node.getName();
        String getterName = node.getGetterNameOrDefault();
        if (Character.isUpperCase(name.charAt(0))) {
            for (PropertyNode otherNode : cn.getProperties()) {
                String otherName = otherNode.getName();
                if (node != otherNode && getterName.equals(otherNode.getGetterNameOrDefault())) {
                    String msg = "The field " + name + " and " + otherName + " on the class " +
                            cn.getName() + " will result in duplicate JavaBean properties, which is not allowed";
                    addError(msg, node);
                }
            }
        }
    }

    private void checkInterfaceFieldModifiers(final FieldNode node) {
        if (currentClass.isInterface() && !(node.isPublic() && node.isStatic() && node.isFinal())) {
            addError("The " + getDescription(node) + " is not 'public static final' but is defined in " + getDescription(currentClass) + ".", node);
        }
    }

    private void checkInvalidFieldModifiers(final FieldNode node) {
        if ((node.getModifiers() & (ACC_FINAL | ACC_VOLATILE)) == (ACC_FINAL | ACC_VOLATILE)) {
            addError("Illegal combination of modifiers, final and volatile, for field '" + node.getName() + "'", node);
        }
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        if (expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET &&
                expression.getRightExpression() instanceof MapEntryExpression) {
            addError("You tried to use a map entry for an index operation, this is not allowed. " +
                    "Maybe something should be set in parentheses or a comma is missing?",
                    expression.getRightExpression());
        }
        super.visitBinaryExpression(expression);

        if (Types.isAssignment(expression.getOperation().getType())) {
            checkFinalFieldAccess(expression.getLeftExpression());
            checkSuperOrThisOnLHS(expression.getLeftExpression());
        }
    }

    private void checkSuperOrThisOnLHS(final Expression expression) {
        if (!(expression instanceof VariableExpression)) return;
        VariableExpression ve = (VariableExpression) expression;
        if (ve.isThisExpression()) {
            addError("cannot have 'this' as LHS of an assignment", expression);
        } else if (ve.isSuperExpression()) {
            addError("cannot have 'super' as LHS of an assignment", expression);
        }
    }

    private void checkFinalFieldAccess(final Expression expression) {
        if (!(expression instanceof VariableExpression) && !(expression instanceof PropertyExpression)) return;
        Variable v = null;
        if (expression instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) expression;
            v = ve.getAccessedVariable();
        } else {
            PropertyExpression propExp = ((PropertyExpression) expression);
            Expression objectExpression = propExp.getObjectExpression();
            if (objectExpression instanceof VariableExpression) {
                VariableExpression varExp = (VariableExpression) objectExpression;
                if (varExp.isThisExpression()) {
                    v = currentClass.getDeclaredField(propExp.getPropertyAsString());
                }
            }
        }
        if (v instanceof FieldNode) {
            FieldNode fn = (FieldNode) v;

            /*
             *  if it is static final but not accessed inside a static constructor, or,
             *  if it is an instance final but not accessed inside an instance constructor, it is an error
             */
            boolean isFinal = fn.isFinal();
            boolean isStatic = fn.isStatic();
            boolean error = isFinal && ((isStatic && !inStaticConstructor) || (!isStatic && !inConstructor));

            if (error) addError("cannot modify" + (isStatic ? " static" : "") + " final field '" + fn.getName() +
                    "' outside of " + (isStatic ? "static initialization block." : "constructor."), expression);
        }
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        inConstructor = true;
        inStaticConstructor = node.isStaticConstructor();
        checkGenericsUsage(node, node.getParameters());
        super.visitConstructor(node);
    }

    @Override
    public void visitCatchStatement(final CatchStatement cs) {
        if (!(cs.getExceptionType().isDerivedFrom(ClassHelper.THROWABLE_TYPE))) {
            addError("Catch statement parameter type is not a subclass of Throwable.", cs);
        }
        super.visitCatchStatement(cs);
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression mce) {
        super.visitMethodCallExpression(mce);
        Expression aexp = mce.getArguments();
        if (aexp instanceof TupleExpression) {
            TupleExpression arguments = (TupleExpression) aexp;
            for (Expression e : arguments.getExpressions()) {
                checkForInvalidDeclaration(e);
            }
        } else {
            checkForInvalidDeclaration(aexp);
        }
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);
        if (expression.isMultipleAssignmentDeclaration()) return;
        checkInvalidDeclarationModifier(expression, ACC_ABSTRACT, "abstract");
        checkInvalidDeclarationModifier(expression, ACC_NATIVE, "native");
        checkInvalidDeclarationModifier(expression, ACC_PRIVATE, "private");
        checkInvalidDeclarationModifier(expression, ACC_PROTECTED, "protected");
        checkInvalidDeclarationModifier(expression, ACC_PUBLIC, "public");
        checkInvalidDeclarationModifier(expression, ACC_STATIC, "static");
        checkInvalidDeclarationModifier(expression, ACC_STRICT, "strictfp");
        checkInvalidDeclarationModifier(expression, ACC_SYNCHRONIZED, "synchronized");
        checkInvalidDeclarationModifier(expression, ACC_TRANSIENT, "transient");
        checkInvalidDeclarationModifier(expression, ACC_VOLATILE, "volatile");
        if (ClassHelper.isPrimitiveVoid(expression.getVariableExpression().getOriginType())) {
            addError("The variable '" + expression.getVariableExpression().getName() + "' has invalid type void", expression);
        }
    }

    private void checkInvalidDeclarationModifier(final DeclarationExpression expression, int modifier, String modName) {
        if ((expression.getVariableExpression().getModifiers() & modifier) != 0) {
            addError("Modifier '" + modName + "' not allowed here.", expression);
        }
    }

    private void checkForInvalidDeclaration(final Expression exp) {
        if (!(exp instanceof DeclarationExpression)) return;
        addError("Invalid use of declaration inside method call.", exp);
    }

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        super.visitConstantExpression(expression);
        checkStringExceedingMaximumLength(expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        super.visitGStringExpression(expression);
        for (ConstantExpression ce : expression.getStrings()) {
            checkStringExceedingMaximumLength(ce);
        }
    }

    private void checkStringExceedingMaximumLength(final ConstantExpression expression) {
        Object value = expression.getValue();
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() > 65535) {
                addError("String too long. The given string is " + s.length() + " Unicode code units long, but only a maximum of 65535 is allowed.", expression);
            }
        }
    }

    private void checkGenericsUsage(final ASTNode ref, final ClassNode[] nodes) {
        for (ClassNode node : nodes) {
            checkGenericsUsage(ref, node);
        }
    }

    private void checkGenericsUsage(final ASTNode ref, final Parameter[] params) {
        for (Parameter p : params) {
            checkGenericsUsage(ref, p.getType());
        }
    }

    private void checkGenericsUsage(final ASTNode ref, final ClassNode node) {
        if (node.isArray()) {
            checkGenericsUsage(ref, node.getComponentType());
        } else if (!node.isRedirectNode() && node.isUsingGenerics()) {
            addError(
                    "A transform used a generics-containing ClassNode "+ node +
                    " for " + getRefDescriptor(ref) +
                    "directly. You are not supposed to do this. " +
                    "Please create a clean ClassNode using ClassNode#getPlainNodeReference() " +
                    "and #setGenericsTypes(GenericsType[]) on it or use GenericsUtils.makeClassSafe* " +
                    "and use the new ClassNode instead of the original one. Otherwise, " +
                    "the compiler will create incorrect descriptors potentially leading to " +
                    "NullPointerExceptions in the TypeResolver class. If this is not your own " +
                    "doing, please report this bug to the writer of the transform.",
                    ref);
        }
    }

    private static String getRefDescriptor(final ASTNode ref) {
        if (ref instanceof FieldNode) {
            FieldNode f = (FieldNode) ref;
            return "the field "+f.getName()+" ";
        } else if (ref instanceof PropertyNode) {
            PropertyNode p = (PropertyNode) ref;
            return "the property "+p.getName()+" ";
        } else if (ref instanceof ConstructorNode) {
            return "the constructor "+ref.getText()+" ";
        } else if (ref instanceof MethodNode) {
            return "the method "+ref.getText()+" ";
        } else if (ref instanceof ClassNode) {
            return "the super class "+ref+" ";
        }
        return "<unknown with class "+ref.getClass()+"> ";
    }
}
