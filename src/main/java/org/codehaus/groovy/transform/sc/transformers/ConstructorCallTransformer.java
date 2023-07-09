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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

public class ConstructorCallTransformer {
    private final StaticCompilationTransformer staticCompilationTransformer;

    public ConstructorCallTransformer(final StaticCompilationTransformer staticCompilationTransformer) {
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
                        return new MapStyleConstructorCall(declaringClass, map, expr);
                    }
                }
            }
        }

        return staticCompilationTransformer.superTransform(expr);
    }

    private class MapStyleConstructorCall extends BytecodeExpression {
        private final MapExpression map;
        private final ConstructorCallExpression originalCall;
        private final boolean innerClassCall;
        private AsmClassGenerator acg;

        MapStyleConstructorCall(final ClassNode declaringClass, final MapExpression map, final ConstructorCallExpression originalCall) {
            super(declaringClass);
            this.map = map;
            this.originalCall = originalCall;
            this.copyNodeMetaData(originalCall);
            this.setSourcePosition(originalCall);
            Expression originalArgs = originalCall.getArguments();
            this.innerClassCall = (2 == ((TupleExpression) originalArgs).getExpressions().size());
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            Expression result = new MapStyleConstructorCall(getType(),
                    (MapExpression) map.transformExpression(transformer),
                    (ConstructorCallExpression) originalCall.transformExpression(transformer)
            );
            result.copyNodeMetaData(this);
            return result;
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
        public void visit(final MethodVisitor mv) {
            WriterController controller = acg.getController();
            CompileStack compileStack = controller.getCompileStack();
            OperandStack operandStack = controller.getOperandStack();

            ClassNode ctorType = getType();
            // create temporary variable to store new object instance
            int tmpObj = compileStack.defineTemporaryVariable("tmpObj", ctorType, false);
            String ctorTypeName = BytecodeHelper.getClassInternalName(ctorType);
            mv.visitTypeInsn(NEW, ctorTypeName);
            mv.visitInsn(DUP);
            String signature = "()V";
            if (innerClassCall && ctorType.getOuterClass() != null) {
                acg.visitVariableExpression(varX("this")); // GROOVY-11122
                Parameter[] params = {new Parameter(ctorType.getOuterClass(), "$p$")};
                signature = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, params);
            }
            mv.visitMethodInsn(INVOKESPECIAL, ctorTypeName, "<init>", signature, false);
            mv.visitVarInsn(ASTORE, tmpObj);

            // process property initializers
            for (MapEntryExpression entryExpression : map.getMapEntryExpressions()) {
                Expression keyExpression = staticCompilationTransformer.transform(entryExpression.getKeyExpression());
                Expression valExpression = staticCompilationTransformer.transform(entryExpression.getValueExpression());
                Expression setExpression = assignX(
                        propX(
                                bytecodeX(ctorType, v -> v.visitVarInsn(ALOAD, tmpObj)),
                                keyExpression
                        ),
                        valExpression
                );
                setExpression.setSourcePosition(entryExpression);
                setExpression.visit(acg);
                operandStack.pop();
            }

            // result object
            mv.visitVarInsn(ALOAD, tmpObj);

            // cleanup stack
            compileStack.removeVar(tmpObj);
        }
    }
}
