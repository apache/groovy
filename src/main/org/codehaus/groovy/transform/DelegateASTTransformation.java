/*
 * Copyright 2008-2009 the original author or authors.
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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
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
    private static final ClassNode DEPRECATED_TYPE = new ClassNode(Deprecated.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];

        if (parent instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) parent;
            final ClassNode type = fieldNode.getType();
            final Map<String, MethodNode> fieldMethods = type.getDeclaredMethodsMap();
            final ClassNode owner = fieldNode.getOwner();
            final Expression deprecatedElement = node.getMember("deprecated");
            final boolean deprecated = (deprecatedElement instanceof ConstantExpression && ((ConstantExpression) deprecatedElement).getValue().equals(true));

            final Map<String, MethodNode> ownMethods = owner.getDeclaredMethodsMap();
            for (Map.Entry<String, MethodNode> e : fieldMethods.entrySet()) {
                addDelegateMethod(fieldNode, owner, ownMethods, e, deprecated);
            }

            for (PropertyNode prop : type.getProperties()) {
                if (prop.isStatic() || !prop.isPublic())
                    continue;
                String name = prop.getName();
                addGetterIfNeeded(fieldNode, owner, prop, name);
                addSetterIfNeeded(fieldNode, owner, prop, name);
            }

            final Expression interfacesElement = node.getMember("interfaces");
            if (interfacesElement instanceof ConstantExpression && ((ConstantExpression) interfacesElement).getValue().equals(false))
                return;

            final Set<ClassNode> allInterfaces = type.getAllInterfaces();
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

    private void addSetterIfNeeded(FieldNode fieldNode, ClassNode owner, PropertyNode prop, String name) {
        String setterName = "set" + Verifier.capitalize(name);
        if ((prop.getModifiers() & ACC_FINAL) != 0 && owner.getSetterMethod(setterName) == null) {
            owner.addMethod(setterName,
                    ACC_PUBLIC,
                    ClassHelper.VOID_TYPE,
                    new Parameter[]{new Parameter(nonGeneric(prop.getType()), "value")},
                    null,
                    new ExpressionStatement(
                            new BinaryExpression(
                                    new PropertyExpression(
                                            new FieldExpression(fieldNode),
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
                                    new FieldExpression(fieldNode),
                                    name)));
        }
    }

    private void addDelegateMethod(FieldNode fieldNode, ClassNode owner, Map<String, MethodNode> ownMethods, Map.Entry<String, MethodNode> e, boolean deprecated) {
        MethodNode method = e.getValue();

        if (!method.isPublic() || method.isStatic() || 0 != (method.getModifiers () & Opcodes.ACC_SYNTHETIC))
            return;

        if (!method.getAnnotations(DEPRECATED_TYPE).isEmpty() && !deprecated)
            return;

        MethodNode existingNode = ownMethods.get(e.getKey());
        // TODO work out why the code was null for super interfaces
        if (existingNode == null || existingNode.getCode() == null) {
            final ArgumentListExpression args = new ArgumentListExpression();
            final Parameter[] params = method.getParameters();
            final Parameter[] newParams = new Parameter[params.length];
            for (int i = 0; i < newParams.length; i++) {
                Parameter newParam = new Parameter(nonGeneric(params[i].getType()), params[i].getName());
                newParams[i] = newParam;
                args.addExpression(new VariableExpression(newParam));
            }
            owner.addMethod(method.getName(),
                    method.getModifiers() & (~ACC_ABSTRACT),
                    nonGeneric(method.getReturnType()),
                    newParams,
                    method.getExceptions(),
                    new ExpressionStatement(
                            new MethodCallExpression(
                                    new FieldExpression(fieldNode),
                                    method.getName(),
                                    args)));
        }
    }

    private ClassNode nonGeneric(ClassNode type) {
        if (type.isUsingGenerics()) {
            final ClassNode nonGen = ClassHelper.makeWithoutCaching(type.getName());
            nonGen.setRedirect(type);
            nonGen.setGenericsTypes(null);
            nonGen.setUsingGenerics(false);
            return nonGen;
        } else {
            return type;
        }
    }
}