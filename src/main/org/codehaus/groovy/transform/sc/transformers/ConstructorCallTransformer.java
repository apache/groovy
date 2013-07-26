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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.*;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*;

public class ConstructorCallTransformer {
    private final StaticCompilationTransformer staticCompilationTransformer;

    public ConstructorCallTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformConstructorCall(final ConstructorCallExpression expr) {
        ConstructorNode node = (ConstructorNode) expr.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (node == null) return expr;
        if (node.getParameters().length == 1
                && StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(node.getParameters()[0].getType(), ClassHelper.MAP_TYPE)
                && node.getCode() == StaticTypeCheckingVisitor.GENERATED_EMPTY_STATEMENT) {
            Expression arguments = expr.getArguments();
            if (arguments instanceof TupleExpression) {
                TupleExpression tupleExpression = (TupleExpression) arguments;
                List<Expression> expressions = tupleExpression.getExpressions();
                if (expressions.size() == 1) {
                    Expression expression = expressions.get(0);
                    if (expression instanceof MapExpression) {
                        MapExpression map = (MapExpression) expression;
                        // check that the node doesn't belong to the list of declared constructors
                        ClassNode declaringClass = node.getDeclaringClass();
                        for (ConstructorNode constructorNode : declaringClass.getDeclaredConstructors()) {
                            if (constructorNode == node) {
                                return staticCompilationTransformer.superTransform(expr);
                            }
                        }
                        // replace this call with a call to <init>() + appropriate setters
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
        private StaticCompilationTransformer staticCompilationTransformer;
        private AsmClassGenerator acg;
        private ClassNode declaringClass;
        private MapExpression map;
        private ConstructorCallExpression orginalCall;

        public MapStyleConstructorCall(
                final StaticCompilationTransformer transformer,
                final ClassNode declaringClass,
                final MapExpression map,
                ConstructorCallExpression orginalCall) {
            this.staticCompilationTransformer = transformer;
            this.declaringClass = declaringClass;
            this.map = map;
            this.orginalCall = orginalCall;
            this.setSourcePosition(orginalCall);
            this.copyNodeMetaData(orginalCall);
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                acg = (AsmClassGenerator) visitor;
            } else {
                orginalCall.visit(visitor);
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
            mv.visitMethodInsn(INVOKESPECIAL, classInternalName, "<init>", "()V");
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