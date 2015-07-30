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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.*;
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
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;

import static java.lang.reflect.Modifier.*;
import static org.objectweb.asm.Opcodes.*;
/**
 * ClassCompletionVerifier
 */
public class ClassCompletionVerifier extends ClassCodeVisitorSupport {

    private ClassNode currentClass;
    private SourceUnit source;
    private boolean inConstructor = false;
    private boolean inStaticConstructor = false;

    public ClassCompletionVerifier(SourceUnit source) {
        this.source = source;
    }

    public ClassNode getClassNode() {
        return currentClass;
    }

    public void visitClass(ClassNode node) {
        ClassNode oldClass = currentClass;
        currentClass = node;
        checkImplementsAndExtends(node);
        if (source != null && !source.getErrorCollector().hasErrors()) {
            checkClassForIncorrectModifiers(node);
            checkInterfaceMethodVisibility(node);
            checkClassForOverwritingFinal(node);
            checkMethodsForIncorrectModifiers(node);
            checkMethodsForWeakerAccess(node);
            checkMethodsForOverridingFinal(node);
            checkNoAbstractMethodsNonabstractClass(node);
            checkClassExtendsAllSelfTypes(node);
            checkNoStaticMethodWithSameSignatureAsNonStatic(node);
            checkGenericsUsage(node, node.getUnresolvedInterfaces());
            checkGenericsUsage(node, node.getUnresolvedSuperClass());
        }
        super.visitClass(node);
        currentClass = oldClass;
    }

    private void checkNoStaticMethodWithSameSignatureAsNonStatic(final ClassNode node) {
        Map<String, MethodNode> result = new HashMap<String, MethodNode>();
        // add in unimplemented abstract methods from the interfaces
        for (ClassNode iface : node.getInterfaces()) {
            Map<String, MethodNode> ifaceMethodsMap = iface.getDeclaredMethodsMap();
            for (String methSig : ifaceMethodsMap.keySet()) {
                if (!result.containsKey(methSig)) {
                    MethodNode methNode = ifaceMethodsMap.get(methSig);
                    result.put(methSig, methNode);
                }
            }
        }
        for (MethodNode methodNode : node.getMethods()) {
            MethodNode mn = result.get(methodNode.getTypeDescriptor());
            if (mn!=null && methodNode.isStatic() && !methodNode.isStaticConstructor()) {
                ClassNode declaringClass = mn.getDeclaringClass();
                ClassNode cn = declaringClass.getOuterClass();
                if (cn==null && declaringClass.isResolved()) {
                    // in case of a precompiled class, the outerclass is unknown
                    Class typeClass = declaringClass.getTypeClass();
                    typeClass = typeClass.getEnclosingClass();
                    if (typeClass!=null) {
                        cn = ClassHelper.make(typeClass);
                    }
                }
                if (cn==null || !Traits.isTrait(cn)) {
                    addError("Method '" + mn.getName() + "' is already defined in " + getDescription(node) + ". You cannot have " +
                            "both a static and a non static method with the same signature", methodNode);
                }
            }
            result.put(methodNode.getTypeDescriptor(), methodNode);
        }
    }

    private void checkInterfaceMethodVisibility(ClassNode node) {
        if (!node.isInterface()) return;
        for (MethodNode method : node.getMethods()) {
            if (method.isPrivate()) {
                addError("Method '" + method.getName() + "' is private but should be public in " + getDescription(currentClass) + ".", method);
            } else if (method.isProtected()) {
                addError("Method '" + method.getName() + "' is protected but should be public in " + getDescription(currentClass) + ".", method);
            }
        }
    }

    private void checkNoAbstractMethodsNonabstractClass(ClassNode node) {
        if (isAbstract(node.getModifiers())) return;
        List<MethodNode> abstractMethods = node.getAbstractMethods();
        if (abstractMethods == null) return;
        for (MethodNode method : abstractMethods) {
            MethodNode sameArgsMethod = node.getMethod(method.getName(), method.getParameters());
            if (sameArgsMethod==null || method.getReturnType().equals(sameArgsMethod.getReturnType())) {
                addError("Can't have an abstract method in a non-abstract class." +
                        " The " + getDescription(node) + " must be declared abstract or" +
                        " the " + getDescription(method) + " must be implemented.", node);
            } else {
                addError("Abstract "+getDescription(method)+" is not implemented but a " +
                                "method of the same name but different return type is defined: "+
                                (sameArgsMethod.isStatic()?"static ":"")+
                                getDescription(sameArgsMethod), method
                );
            }
        }
    }

    private void checkClassExtendsAllSelfTypes(ClassNode node) {
        int modifiers = node.getModifiers();
        if (!isInterface(modifiers)) {
            for (ClassNode anInterface : GeneralUtils.getInterfacesAndSuperInterfaces(node)) {
                if (Traits.isTrait(anInterface)) {
                    LinkedHashSet<ClassNode> selfTypes = new LinkedHashSet<ClassNode>();
                    for (ClassNode type : Traits.collectSelfTypes(anInterface, selfTypes, true, false)) {
                        if (type.isInterface() && !node.implementsInterface(type)) {
                            addError(getDescription(node)
                                    + " implements " + getDescription(anInterface)
                                    + " but does not implement self type " + getDescription(type),
                                    anInterface);
                        } else if (!type.isInterface() && !node.isDerivedFrom(type)) {
                            addError(getDescription(node)
                                            + " implements " + getDescription(anInterface)
                                            + " but does not extend self type " + getDescription(type),
                                    anInterface);
                        }
                    }
                }
            }
        }
    }

    private void checkClassForIncorrectModifiers(ClassNode node) {
        checkClassForAbstractAndFinal(node);
        checkClassForOtherModifiers(node);
    }

    private void checkClassForAbstractAndFinal(ClassNode node) {
        if (!isAbstract(node.getModifiers())) return;
        if (!isFinal(node.getModifiers())) return;
        if (node.isInterface()) {
            addError("The " + getDescription(node) + " must not be final. It is by definition abstract.", node);
        } else {
            addError("The " + getDescription(node) + " must not be both final and abstract.", node);
        }
    }

    private void checkClassForOtherModifiers(ClassNode node) {
        checkClassForModifier(node, isTransient(node.getModifiers()), "transient");
        checkClassForModifier(node, isVolatile(node.getModifiers()), "volatile");
        checkClassForModifier(node, isNative(node.getModifiers()), "native");
        if (!(node instanceof InnerClassNode)) {
            checkClassForModifier(node, isStatic(node.getModifiers()), "static");
            checkClassForModifier(node, isPrivate(node.getModifiers()), "private");
        }
        // don't check synchronized here as it overlaps with ACC_SUPER
    }

    private void checkMethodForModifier(MethodNode node, boolean condition, String modifierName) {
        if (!condition) return;
        addError("The " + getDescription(node) + " has an incorrect modifier " + modifierName + ".", node);
    }

    private void checkClassForModifier(ClassNode node, boolean condition, String modifierName) {
        if (!condition) return;
        addError("The " + getDescription(node) + " has an incorrect modifier " + modifierName + ".", node);
    }

    private String getDescription(ClassNode node) {
        return (node.isInterface() ? (Traits.isTrait(node)?"trait":"interface") : "class") + " '" + node.getName() + "'";
    }

    private String getDescription(MethodNode node) {
        return "method '" + node.getTypeDescriptor() + "'";
    }

    private String getDescription(FieldNode node) {
        return "field '" + node.getName() + "'";
    }

    private void checkAbstractDeclaration(MethodNode methodNode) {
        if (!methodNode.isAbstract()) return;
        if (isAbstract(currentClass.getModifiers())) return;
        addError("Can't have an abstract method in a non-abstract class." +
                " The " + getDescription(currentClass) + " must be declared abstract or the method '" +
                methodNode.getTypeDescriptor() + "' must not be abstract.", methodNode);
    }

    private void checkClassForOverwritingFinal(ClassNode cn) {
        ClassNode superCN = cn.getSuperClass();
        if (superCN == null) return;
        if (!isFinal(superCN.getModifiers())) return;
        StringBuilder msg = new StringBuilder();
        msg.append("You are not allowed to overwrite the final ");
        msg.append(getDescription(superCN));
        msg.append(".");
        addError(msg.toString(), cn);
    }

    private void checkImplementsAndExtends(ClassNode node) {
        ClassNode cn = node.getSuperClass();
        if (cn.isInterface() && !node.isInterface()) {
            addError("You are not allowed to extend the " + getDescription(cn) + ", use implements instead.", node);
        }
        for (ClassNode anInterface : node.getInterfaces()) {
            cn = anInterface;
            if (!cn.isInterface()) {
                addError("You are not allowed to implement the " + getDescription(cn) + ", use extends instead.", node);
            }
        }
    }

    private void checkMethodsForIncorrectModifiers(ClassNode cn) {
        if (!cn.isInterface()) return;
        for (MethodNode method : cn.getMethods()) {
            if (method.isFinal()) {
                addError("The " + getDescription(method) + " from " + getDescription(cn) +
                        " must not be final. It is by definition abstract.", method);
            }
            if (method.isStatic() && !isConstructor(method)) {
                addError("The " + getDescription(method) + " from " + getDescription(cn) +
                        " must not be static. Only fields may be static in an interface.", method);
            }
        }
    }

    private void checkMethodsForWeakerAccess(ClassNode cn) {
        for (MethodNode method : cn.getMethods()) {
            checkMethodForWeakerAccessPrivileges(method, cn);
        }
    }

    private boolean isConstructor(MethodNode method) {
        return method.getName().equals("<clinit>");
    }

    private void checkMethodsForOverridingFinal(ClassNode cn) {
        for (MethodNode method : cn.getMethods()) {
            Parameter[] params = method.getParameters();
            for (MethodNode superMethod : cn.getSuperClass().getMethods(method.getName())) {
                Parameter[] superParams = superMethod.getParameters();
                if (!hasEqualParameterTypes(params, superParams)) continue;
                if (!superMethod.isFinal()) break;
                addInvalidUseOfFinalError(method, params, superMethod.getDeclaringClass());
                return;
            }
        }
    }

    private void addInvalidUseOfFinalError(MethodNode method, Parameter[] parameters, ClassNode superCN) {
        StringBuilder msg = new StringBuilder();
        msg.append("You are not allowed to override the final method ").append(method.getName());
        msg.append("(");
        boolean needsComma = false;
        for (Parameter parameter : parameters) {
            if (needsComma) {
                msg.append(",");
            } else {
                needsComma = true;
            }
            msg.append(parameter.getType());
        }
        msg.append(") from ").append(getDescription(superCN));
        msg.append(".");
        addError(msg.toString(), method);
    }

    private void addWeakerAccessError(ClassNode cn, MethodNode method, Parameter[] parameters, MethodNode superMethod) {
        StringBuilder msg = new StringBuilder();
        msg.append(method.getName());
        msg.append("(");
        boolean needsComma = false;
        for (Parameter parameter : parameters) {
            if (needsComma) {
                msg.append(",");
            } else {
                needsComma = true;
            }
            msg.append(parameter.getType());
        }
        msg.append(") in ");
        msg.append(cn.getName());
        msg.append(" cannot override ");
        msg.append(superMethod.getName());
        msg.append(" in ");
        msg.append(superMethod.getDeclaringClass().getName());
        msg.append("; attempting to assign weaker access privileges; was ");
        msg.append(superMethod.isPublic() ? "public" : "protected");
        addError(msg.toString(), method);
    }

    private boolean hasEqualParameterTypes(Parameter[] first, Parameter[] second) {
        if (first.length != second.length) return false;
        for (int i = 0; i < first.length; i++) {
            String ft = first[i].getType().getName();
            String st = second[i].getType().getName();
            if (ft.equals(st)) continue;
            return false;
        }
        return true;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitMethod(MethodNode node) {
        inConstructor = false;
        inStaticConstructor = node.isStaticConstructor();
        checkAbstractDeclaration(node);
        checkRepetitiveMethod(node);
        checkOverloadingPrivateAndPublic(node);
        checkMethodModifiers(node);
        checkGenericsUsage(node, node.getParameters());
        checkGenericsUsage(node, node.getReturnType());
        super.visitMethod(node);
    }

    private void checkMethodModifiers(MethodNode node) {
        // don't check volatile here as it overlaps with ACC_BRIDGE
        // additional modifiers not allowed for interfaces
        if ((this.currentClass.getModifiers() & ACC_INTERFACE) != 0) {
            checkMethodForModifier(node, isStrict(node.getModifiers()), "strictfp");
            checkMethodForModifier(node, isSynchronized(node.getModifiers()), "synchronized");
            checkMethodForModifier(node, isNative(node.getModifiers()), "native");
        }
    }

    private void checkMethodForWeakerAccessPrivileges(MethodNode mn, ClassNode cn) {
        if (mn.isPublic()) return;
        Parameter[] params = mn.getParameters();
        for (MethodNode superMethod : cn.getSuperClass().getMethods(mn.getName())) {
            Parameter[] superParams = superMethod.getParameters();
            if (!hasEqualParameterTypes(params, superParams)) continue;
            if ((mn.isPrivate() && !superMethod.isPrivate()) ||
                    (mn.isProtected() && superMethod.isPublic())) {
                addWeakerAccessError(cn, mn, params, superMethod);
                return;
            }
        }
    }

    private void checkOverloadingPrivateAndPublic(MethodNode node) {
        if (isConstructor(node)) return;
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

    private void checkRepetitiveMethod(MethodNode node) {
        if (isConstructor(node)) return;
        for (MethodNode method : currentClass.getMethods(node.getName())) {
            if (method == node) continue;
            if (!method.getDeclaringClass().equals(node.getDeclaringClass())) continue;
            Parameter[] p1 = node.getParameters();
            Parameter[] p2 = method.getParameters();
            if (p1.length != p2.length) continue;
            addErrorIfParamsAndReturnTypeEqual(p2, p1, node, method);
        }
    }

    private void addErrorIfParamsAndReturnTypeEqual(Parameter[] p2, Parameter[] p1,
                                                    MethodNode node, MethodNode element) {
        boolean isEqual = true;
        for (int i = 0; i < p2.length; i++) {
            isEqual &= p1[i].getType().equals(p2[i].getType());
            if (!isEqual) break;
        }
        isEqual &= node.getReturnType().equals(element.getReturnType());
        if (isEqual) {
            addError("Repetitive method name/signature for " + getDescription(node) +
                    " in " + getDescription(currentClass) + ".", node);
        }
    }

    public void visitField(FieldNode node) {
        if (currentClass.getDeclaredField(node.getName()) != node) {
            addError("The " + getDescription(node) + " is declared multiple times.", node);
        }
        checkInterfaceFieldModifiers(node);
        checkGenericsUsage(node, node.getType());
        super.visitField(node);
    }

    public void visitProperty(PropertyNode node) {
        checkDuplicateProperties(node);
        checkGenericsUsage(node, node.getType());
        super.visitProperty(node);
    }

    private void checkDuplicateProperties(PropertyNode node) {
        ClassNode cn = node.getDeclaringClass();
        String name = node.getName();
        String getterName = "get" + MetaClassHelper.capitalize(name);
        if (Character.isUpperCase(name.charAt(0))) {
            for (PropertyNode propNode : cn.getProperties()) {
                String otherName = propNode.getField().getName();
                String otherGetterName = "get" + MetaClassHelper.capitalize(otherName);
                if (node != propNode && getterName.equals(otherGetterName)) {
                    String msg = "The field " + name + " and " + otherName + " on the class " +
                            cn.getName() + " will result in duplicate JavaBean properties, which is not allowed";
                    addError(msg, node);
                }
            }
        }
    }

    private void checkInterfaceFieldModifiers(FieldNode node) {
        if (!currentClass.isInterface()) return;
        if ((node.getModifiers() & (ACC_PUBLIC | ACC_STATIC | ACC_FINAL)) == 0 ||
                (node.getModifiers() & (ACC_PRIVATE | ACC_PROTECTED)) != 0) {
            addError("The " + getDescription(node) + " is not 'public static final' but is defined in " +
                    getDescription(currentClass) + ".", node);
        }
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        if (expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET &&
                expression.getRightExpression() instanceof MapEntryExpression) {
            addError("You tried to use a map entry for an index operation, this is not allowed. " +
                    "Maybe something should be set in parentheses or a comma is missing?",
                    expression.getRightExpression());
        }
        super.visitBinaryExpression(expression);

        switch (expression.getOperation().getType()) {
            case Types.EQUAL: // = assignment
            case Types.BITWISE_AND_EQUAL:
            case Types.BITWISE_OR_EQUAL:
            case Types.BITWISE_XOR_EQUAL:
            case Types.PLUS_EQUAL:
            case Types.MINUS_EQUAL:
            case Types.MULTIPLY_EQUAL:
            case Types.DIVIDE_EQUAL:
            case Types.INTDIV_EQUAL:
            case Types.MOD_EQUAL:
            case Types.POWER_EQUAL:
            case Types.LEFT_SHIFT_EQUAL:
            case Types.RIGHT_SHIFT_EQUAL:
            case Types.RIGHT_SHIFT_UNSIGNED_EQUAL:
                checkFinalFieldAccess(expression.getLeftExpression());
                break;
            default:
                break;
        }
    }

    private void checkFinalFieldAccess(Expression expression) {
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
             *  if it is an instance final but not accessed inside a instance constructor, it is an error
             */
            boolean isFinal = fn.isFinal();
            boolean isStatic = fn.isStatic();
            boolean error = isFinal && ((isStatic && !inStaticConstructor) || (!isStatic && !inConstructor));

            if (error) addError("cannot modify" + (isStatic ? " static" : "") + " final field '" + fn.getName() +
                    "' outside of " + (isStatic ? "static initialization block." : "constructor."), expression);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        inConstructor = true;
        inStaticConstructor = node.isStaticConstructor();
        checkGenericsUsage(node, node.getParameters());
        super.visitConstructor(node);
    }

    public void visitCatchStatement(CatchStatement cs) {
        if (!(cs.getExceptionType().isDerivedFrom(ClassHelper.make(Throwable.class)))) {
            addError("Catch statement parameter type is not a subclass of Throwable.", cs);
        }
        super.visitCatchStatement(cs);
    }

    public void visitMethodCallExpression(MethodCallExpression mce) {
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
    public void visitDeclarationExpression(DeclarationExpression expression) {
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
    }

    private void checkInvalidDeclarationModifier(DeclarationExpression expression, int modifier, String modName) {
        if ((expression.getVariableExpression().getModifiers() & modifier) != 0) {
            addError("Modifier '" + modName + "' not allowed here.", expression);
        }
    }

    private void checkForInvalidDeclaration(Expression exp) {
        if (!(exp instanceof DeclarationExpression)) return;
        addError("Invalid use of declaration inside method call.", exp);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        super.visitConstantExpression(expression);
        checkStringExceedingMaximumLength(expression);
    }

    public void visitGStringExpression(GStringExpression expression) {
        super.visitGStringExpression(expression);
        for (ConstantExpression ce : expression.getStrings()) {
            checkStringExceedingMaximumLength(ce);
        }
    }

    private void checkStringExceedingMaximumLength(ConstantExpression expression) {
        Object value = expression.getValue();
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() > 65535) {
                addError("String too long. The given string is " + s.length() + " Unicode code units long, but only a maximum of 65535 is allowed.", expression);
            }
        }
    }

    private void checkGenericsUsage(ASTNode ref, ClassNode[] nodes) {
        for (ClassNode node : nodes) {
            checkGenericsUsage(ref, node);
        }
    }
    
    private void checkGenericsUsage(ASTNode ref, Parameter[] params) {
        for (Parameter p : params) {
            checkGenericsUsage(ref, p.getType());
        }
    }
    
    private void checkGenericsUsage(ASTNode ref, ClassNode node) {
        if (node.isArray()) {
            checkGenericsUsage(ref, node.getComponentType());
        } else if (!node.isRedirectNode() && node.isUsingGenerics()) {
            addError(   
                    "A transform used a generics containing ClassNode "+ node + " " +
                    "for "+getRefDescriptor(ref) + 
                    "directly. You are not supposed to do this. " +
                    "Please create a new ClassNode referring to the old ClassNode " +
                    "and use the new ClassNode instead of the old one. Otherwise " +
                    "the compiler will create wrong descriptors and a potential " +
                    "NullPointerException in TypeResolver in the OpenJDK. If this is " +
                    "not your own doing, please report this bug to the writer of the " +
                    "transform.",
                    ref);
        }
    }

    private String getRefDescriptor(ASTNode ref) {
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
