/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.transform.sc;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.transform.stc.TypeCheckerPluginFactory;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.*;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;

/**
 * This visitor is responsible for amending the AST with static compilation metadata or transform the AST so that
 * a class or a method can be statically compiled. It may also throw errors specific to static compilation which
 * are not considered as an error at the type check pass. For example, usage of spread operator is not allowed
 * in statically compiled portions of code, while it may be statically checked.
 *
 * Static compilation relies on static type checking, which explains why this visitor extends the type checker
 * visitor.
 *
 * @author Cedric Champeau
 */
public class StaticCompilationVisitor extends StaticTypeCheckingVisitor {
    private final TypeChooser typeChooser = new StaticTypesTypeChooser();
    
    private ClassNode classNode;
    
    public StaticCompilationVisitor(final SourceUnit unit, final ClassNode node, final TypeCheckerPluginFactory pluginFactory) {
        super(unit, node, pluginFactory);
    }

    public static boolean isStaticallyCompiled(AnnotatedNode node) {
        if (node.getNodeMetaData(STATIC_COMPILE_NODE)!=null) return true;
        if (node instanceof MethodNode) {
            return isStaticallyCompiled(node.getDeclaringClass());
        }
        if (node instanceof InnerClassNode) {
            return isStaticallyCompiled(((InnerClassNode)node).getOuterClass());
        }
        return false;
    }

    @Override
    public void visitClass(final ClassNode node) {
        ClassNode oldCN = classNode;
        classNode = node;
        Iterator<InnerClassNode> innerClasses = classNode.getInnerClasses();
        while (innerClasses.hasNext()) {
            InnerClassNode innerClassNode = innerClasses.next();
            innerClassNode.setNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
            innerClassNode.setNodeMetaData(WriterControllerFactory.class, node.getNodeMetaData(WriterControllerFactory.class));
            addPrivateBridgeMethods(innerClassNode);
        }
        super.visitClass(node);
        classNode = oldCN;
    }

    /**
     * This method is used to add "bridge" methods for private methods of an inner
     * class, so that the outer class is capable of calling them. It does basically
     * the same job as access$000 like methods in Java.
     * 
     * @param node an inner class node for which to generate bridge methods
     */
    private void addPrivateBridgeMethods(final InnerClassNode node) {
        List<MethodNode> methods = new ArrayList<MethodNode>(node.getMethods());
        Map<MethodNode, MethodNode> privateBridgeMethods = new HashMap<MethodNode, MethodNode>();
        int i=-1;
        for (MethodNode method : methods) {
            int access = method.getModifiers();
            if (method.isPrivate() && (access& Opcodes.ACC_SYNTHETIC)==0) {
                i++;
                access = (access - Opcodes.ACC_PRIVATE + Opcodes.ACC_SYNTHETIC) + Opcodes.ACC_FINAL;
                Expression arguments;
                if (method.getParameters()==null || method.getParameters().length==0) {
                    arguments = ArgumentListExpression.EMPTY_ARGUMENTS;
                } else {
                    List<Expression> args = new LinkedList<Expression>();
                    for (Parameter parameter : method.getParameters()) {
                        args.add(new VariableExpression(parameter));
                    }
                    arguments = new ArgumentListExpression(args);
                }
                Expression receiver = method.isStatic()?new ClassExpression(node):new VariableExpression("this", node);
                ExpressionStatement returnStatement = new ExpressionStatement(new MethodCallExpression(receiver, method.getName(), arguments));
                MethodNode bridge = node.addMethod("access$"+i, access, method.getReturnType(), method.getParameters(), method.getExceptions(), returnStatement);
                privateBridgeMethods.put(method, bridge);
            }
        }
        node.setNodeMetaData(StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS, privateBridgeMethods);
    }

    private void memorizeInitialExpressions(final MethodNode node) {
        // add node metadata for default parameters because they are erased by the Verifier
        if (node.getParameters()!=null) {
            for (Parameter parameter : node.getParameters()) {
                parameter.putNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION, parameter.getInitialExpression());
            }
        }
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        throw new UnsupportedOperationException("The spread operator cannot be used with static compilation because the number of arguments cannot be determined at compile time");
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target!=null) {
            call.setMethodTarget(target);
            memorizeInitialExpressions(target);
        }

        if (call.getMethodTarget()==null && call.getLineNumber()>0) {
            addError("Target method for method call expression hasn't been set", call);
        }

        // add special metadata on closure if the call is a "with" call
        if (StaticTypeCheckingSupport.isWithCall(call.getMethodAsString(), call.getArguments())) {
            // no check is required, ensured by isWithCall
            ClosureExpression closure = (ClosureExpression) ((ArgumentListExpression) call.getArguments()).getExpression(0);
            closure.setNodeMetaData(StaticCompilationMetadataKeys.WITH_CLOSURE, Boolean.TRUE);
        }
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target==null && call.getLineNumber()>0) {
            addError("Target constructor for constructor call expression hasn't been set", call);
        } else {
            if (target==null) {
                // try to find a target
                ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
                List<Expression> expressions = argumentListExpression.getExpressions();
                ClassNode[] args = new ClassNode[expressions.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = typeChooser.resolveType(expressions.get(i), classNode);
                }
                MethodNode constructor = findMethodOrFail(call, call.isSuperCall() ? classNode.getSuperClass() : classNode, "<init>", args);
                call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, constructor);
                target = constructor;
            }
        }
        if (target!=null) {
            memorizeInitialExpressions(target);
        }
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        Expression collectionExpression = forLoop.getCollectionExpression();
        if (!(collectionExpression instanceof ClosureListExpression)) {
            final ClassNode collectionType = getType(forLoop.getCollectionExpression());
            ClassNode componentType = inferLoopElementType(collectionType);
            forLoop.getVariable().setType(componentType);
        }
    }

    @Override
    protected MethodNode findMethodOrFail(final Expression expr, final ClassNode receiver, final String name, final ClassNode... args) {
        MethodNode methodNode = super.findMethodOrFail(expr, receiver, name, args);
        if (expr instanceof BinaryExpression && methodNode!=null) {
            expr.putNodeMetaData(BINARY_EXP_TARGET, new Object[] {methodNode, name});
        }
        return methodNode;
    }
}
