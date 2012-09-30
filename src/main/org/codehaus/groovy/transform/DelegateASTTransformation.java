/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import groovy.lang.GroovyObject;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles generation of code for the <code>@Delegate</code> annotation
 *
 * @author Alex Tkachman
 * @author Guillaume Laforge
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class DelegateASTTransformation implements ASTTransformation, Opcodes {
    private static final ClassNode DEPRECATED_TYPE = ClassHelper.make(Deprecated.class);
    private static final ClassNode GROOVYOBJECT_TYPE = ClassHelper.make(GroovyObject.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) parent;
            final ClassNode type = fieldNode.getType();
            final ClassNode owner = fieldNode.getOwner();
            if (type.equals(ClassHelper.OBJECT_TYPE) || type.equals(GROOVYOBJECT_TYPE)) {
                addError("@Delegate field '" + fieldNode.getName() + "' has an inappropriate type: " + type.getName() +
                        ". Please add an explicit type but not java.lang.Object or groovy.lang.GroovyObject.", parent, source);
                return;
            }
            if (type.equals(owner)) {
                addError("@Delegate field '" + fieldNode.getName() + "' has an inappropriate type: " + type.getName() +
                        ". Delegation to own type not supported. Please use a different type.", parent, source);
                return;
            }
            final List<MethodNode> fieldMethods = getAllMethods(type);
            for (ClassNode next : type.getAllInterfaces()) {
                fieldMethods.addAll(getAllMethods(next));
            }
            final Expression deprecatedElement = node.getMember("deprecated");
            final Expression interfacesElement = node.getMember("interfaces");
            final boolean skipInterfaces = hasBooleanValue(interfacesElement, false);
            final boolean includeDeprecated = hasBooleanValue(deprecatedElement, true) || (type.isInterface() && !skipInterfaces);

            final List<MethodNode> ownerMethods = getAllMethods(owner);
            for (MethodNode mn : fieldMethods) {
                addDelegateMethod(fieldNode, owner, ownerMethods, mn, includeDeprecated);
            }

            for (PropertyNode prop : type.getProperties()) {
                if (prop.isStatic() || !prop.isPublic())
                    continue;
                String name = prop.getName();
                addGetterIfNeeded(fieldNode, owner, prop, name);
                addSetterIfNeeded(fieldNode, owner, prop, name);
            }

            if (skipInterfaces) return;

            final Set<ClassNode> allInterfaces = getInterfacesAndSuperInterfaces(type);
            final Set<ClassNode> ownerIfaces = owner.getAllInterfaces();
            for (ClassNode iface : allInterfaces) {
                if (Modifier.isPublic(iface.getModifiers()) && !ownerIfaces.contains(iface)) {
                    final ClassNode[] ifaces = owner.getInterfaces();
                    final ClassNode[] newIfaces = new ClassNode[ifaces.length + 1];
                    System.arraycopy(ifaces, 0, newIfaces, 0, ifaces.length);
                    newIfaces[ifaces.length] = iface;
                    owner.setInterfaces(newIfaces);
                }
            }
        }
    }

    private Set<ClassNode> getInterfacesAndSuperInterfaces(ClassNode type) {
        Set<ClassNode> res = new HashSet<ClassNode>();
        if (type.isInterface()) {
            res.add(type);
            return res;
        }
        ClassNode next = type;
        while (next != null) {
            Collections.addAll(res, next.getInterfaces());
            next = next.getSuperClass();
        }
        return res;
    }

    private List<MethodNode> getAllMethods(ClassNode type) {
        ClassNode node = type;
        List<MethodNode> result = new ArrayList<MethodNode>();
        while (node != null) {
            result.addAll(node.getMethods());
            node = node.getSuperClass();
        }
        return result;
    }

    private boolean hasBooleanValue(Expression expression, boolean bool) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue().equals(bool);
    }

    private void addSetterIfNeeded(FieldNode fieldNode, ClassNode owner, PropertyNode prop, String name) {
        String setterName = "set" + Verifier.capitalize(name);
        if ((prop.getModifiers() & ACC_FINAL) == 0 && owner.getSetterMethod(setterName) == null) {
            owner.addMethod(setterName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    new Parameter[]{new Parameter(nonGeneric(prop.getType()), "value")},
                    null,
                    new ExpressionStatement(
                            new BinaryExpression(
                                    new PropertyExpression(
                                            new VariableExpression(fieldNode),
                                            name),
                                    Token.newSymbol(Types.EQUAL, -1, -1),
                                    new VariableExpression("value"))));
        }
    }

    private void addGetterIfNeeded(FieldNode fieldNode, ClassNode owner, PropertyNode prop, String name) {
        String getterName = "get" + Verifier.capitalize(name);
        if (owner.getGetterMethod(getterName) == null) {
            owner.addMethod(getterName,
                    ACC_PUBLIC,
                    nonGeneric(prop.getType()),
                    Parameter.EMPTY_ARRAY,
                    null,
                    new ReturnStatement(
                            new PropertyExpression(
                                    new VariableExpression(fieldNode),
                                    name)));
        }
    }

    private void addDelegateMethod(FieldNode fieldNode, ClassNode owner, List<MethodNode> ownMethods, MethodNode candidate, boolean includeDeprecated) {
        if (!candidate.isPublic() || candidate.isStatic() || 0 != (candidate.getModifiers () & Opcodes.ACC_SYNTHETIC))
            return;

        if (!candidate.getAnnotations(DEPRECATED_TYPE).isEmpty() && !includeDeprecated)
            return;

        // ignore methods from GroovyObject
        for (MethodNode mn : GROOVYOBJECT_TYPE.getMethods()) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                return;
            }
        }

        // ignore methods already in owner
        for (MethodNode mn : owner.getMethods()) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor())) {
                return;
            }
        }

        // give precedence to methods of self (but not abstract or static superclass methods)
        // also allows abstract or static self methods to be selected for overriding but they are ignored later
        MethodNode existingNode = null;
        for (MethodNode mn : ownMethods) {
            if (mn.getTypeDescriptor().equals(candidate.getTypeDescriptor()) && !mn.isAbstract() && !mn.isStatic()) {
                existingNode = mn;
                break;
            }
        }
        if (existingNode == null || existingNode.getCode() == null) {
            final ArgumentListExpression args = new ArgumentListExpression();
            final Parameter[] params = candidate.getParameters();
            final Parameter[] newParams = new Parameter[params.length];
            for (int i = 0; i < newParams.length; i++) {
                Parameter newParam = new Parameter(nonGeneric(params[i].getType()), params[i].getName());
                newParam.setInitialExpression(params[i].getInitialExpression());
                newParams[i] = newParam;
                args.addExpression(new VariableExpression(newParam));
            }
            // addMethod will ignore attempts to override abstract or static methods with same signature on self
            MethodNode newMethod = owner.addMethod(candidate.getName(),
                    candidate.getModifiers() & (~ACC_ABSTRACT) & (~ACC_NATIVE),
                    nonGeneric(candidate.getReturnType()),
                    newParams,
                    candidate.getExceptions(),
                    new ExpressionStatement(
                            new MethodCallExpression(
                                    new VariableExpression(fieldNode),
                                    candidate.getName(),
                                    args)));
            newMethod.setGenericsTypes(candidate.getGenericsTypes());
        }
    }

    private ClassNode nonGeneric(ClassNode type) {
        if (type.isUsingGenerics()) {
            final ClassNode nonGen = ClassHelper.makeWithoutCaching(type.getName());
            nonGen.setRedirect(type);
            nonGen.setGenericsTypes(null);
            nonGen.setUsingGenerics(false);
            return nonGen;
        } else if (type.isArray() && type.getComponentType().isUsingGenerics()) {
            return type.getComponentType().getPlainNodeReference().makeArray();
        } else {
            return type;
        }
    }

    public void addError(String msg, ASTNode expr, SourceUnit source) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }
}
