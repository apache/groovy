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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;

public class ConstructorCallTransformer {
    private final StaticCompilationTransformer staticCompilationTransformer;

    public ConstructorCallTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformConstructorCall(final ConstructorCallExpression expr) {
        ConstructorNode node = expr.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (node == null) return expr;
        Parameter[] params = node.getParameters();
        if ((params.length == 1 || params.length == 2) // 2 is for inner class case
                && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(params[params.length - 1].getType(), ClassHelper.MAP_TYPE)
                && node.getCode() == StaticTypeCheckingVisitor.GENERATED_EMPTY_STATEMENT) {
            Expression arguments = expr.getArguments();
            if (arguments instanceof TupleExpression) {
                TupleExpression tupleExpression = (TupleExpression) arguments;
                List<Expression> expressions = tupleExpression.getExpressions();
                if (expressions.size() == 1 || expressions.size() == 2) { // 2 = inner class case
                    Expression expression = expressions.get(expressions.size() - 1);
                    if (expression instanceof MapExpression) {
                        MapExpression map = (MapExpression) expression;
                        // check that the node doesn't belong to the list of declared constructors
                        ClassNode declaringClass = node.getDeclaringClass();
                        for (ConstructorNode constructorNode : declaringClass.getDeclaredConstructors()) {
                            if (constructorNode == node) {
                                return staticCompilationTransformer.superTransform(expr);
                            }
                        }
                        // replace call to <init>(Map) or <init>(this, Map)
                        // with a call to <init>() or <init>(this) + appropriate setters
                        // for example, foo(x:1, y:2) is replaced with:
                        // { def tmp = new Foo(); tmp.x = 1; tmp.y = 2; return tmp }()
                        MapStyleConstructorCall result = new MapStyleConstructorCall(
                                staticCompilationTransformer,
                                declaringClass,
                                map,
                                expr
                        );

                        return result;
                    }
                }
            }

        }
        return staticCompilationTransformer.superTransform(expr);
    }

    private static class MapStyleConstructorCall extends BytecodeExpression implements Opcodes {
        private final StaticCompilationTransformer staticCompilationTransformer;
        private AsmClassGenerator acg;
        private final ClassNode declaringClass;
        private final MapExpression map;
        private final ConstructorCallExpression originalCall;
        private final boolean innerClassCall;

        public MapStyleConstructorCall(
                final StaticCompilationTransformer transformer,
                final ClassNode declaringClass,
                final MapExpression map,
                final ConstructorCallExpression originalCall) {
            this.staticCompilationTransformer = transformer;
            this.declaringClass = declaringClass;
            this.map = map;
            this.originalCall = originalCall;
            this.setSourcePosition(originalCall);
            this.copyNodeMetaData(originalCall);
            List<Expression> originalExpressions = originalCall.getArguments() instanceof TupleExpression ?
                    ((TupleExpression)originalCall.getArguments()).getExpressions() : null;
            this.innerClassCall = originalExpressions != null && originalExpressions.size() == 2;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                acg = (AsmClassGenerator) visitor;
            } else {
                originalCall.visit(visitor);
            } 
            super.visit(visitor);
        }
        @Override
        public ClassNode getType() {
            return declaringClass;
        }

        @Override
        public void visit(final MethodVisitor mv) {
            final WriterController controller = acg.getController();
            final OperandStack operandStack = controller.getOperandStack();
            final CompileStack compileStack = controller.getCompileStack();

            // create a temporary variable to store the constructed object
            final int tmpObj = compileStack.defineTemporaryVariable("tmpObj", declaringClass, false);
            String classInternalName = BytecodeHelper.getClassInternalName(declaringClass);
            mv.visitTypeInsn(NEW, classInternalName);
            mv.visitInsn(DUP);
            String desc = "()V";
            if (innerClassCall && declaringClass.isRedirectNode() && declaringClass.redirect() instanceof InnerClassNode) {
                // load "this"
                mv.visitVarInsn(ALOAD, 0);
                InnerClassNode icn = (InnerClassNode) declaringClass.redirect();
                Parameter[] params = { new Parameter(icn.getOuterClass(), "$p$") };
                desc = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, params);
            }
            mv.visitMethodInsn(INVOKESPECIAL, classInternalName, "<init>", desc, false);
            mv.visitVarInsn(ASTORE, tmpObj); // store it into tmp variable

            // load every field
            for (MapEntryExpression entryExpression : map.getMapEntryExpressions()) {
                int line = entryExpression.getLineNumber();
                int col = entryExpression.getColumnNumber();
                Expression keyExpression = staticCompilationTransformer.transform(entryExpression.getKeyExpression());
                Expression valueExpression = staticCompilationTransformer.transform(entryExpression.getValueExpression());
                BinaryExpression bexp = new BinaryExpression(new PropertyExpression(new BytecodeExpression() {
                            @Override
                            public void visit(final MethodVisitor mv) {
                                mv.visitVarInsn(ALOAD, tmpObj);
                            }

                            @Override
                            public ClassNode getType() {
                                return declaringClass;
                            }
                        }, keyExpression),
                        Token.newSymbol("=", line, col),
                        valueExpression
                );
                bexp.setSourcePosition(entryExpression);
                bexp.visit(acg);
                operandStack.pop(); // consume argument
            }

            // load object
            mv.visitVarInsn(ALOAD, tmpObj);

            // cleanup stack
            compileStack.removeVar(tmpObj);

        }
    }

}
