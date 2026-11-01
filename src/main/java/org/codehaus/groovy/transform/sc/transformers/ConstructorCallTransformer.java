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
import org.codehaus.groovy.ast.expr.ConstantExpression;
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
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.SWAP;

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
                    // a map that is not a literal has its entries set through the metaclass at runtime
                    if (!(expression instanceof MapExpression)) {
                        expression = staticCompilationTransformer.transform(expression);
                    }
                    return new MapStyleConstructorCall(declaringClass, expression, expr);
                }
            }
        }

        return staticCompilationTransformer.superTransform(expr);
    }

    private class MapStyleConstructorCall extends BytecodeExpression {
        private final Expression map;
        private final ConstructorCallExpression originalCall;
        private final boolean innerClassCall;
        private AsmClassGenerator acg;

        MapStyleConstructorCall(final ClassNode declaringClass, final Expression map, final ConstructorCallExpression originalCall) {
            super(declaringClass);
            this.map = map;
            this.originalCall = originalCall;
            this.copyNodeMetaData(originalCall);
            this.setSourcePosition(originalCall);
            var originalArgs = (TupleExpression) originalCall.getArguments();
            this.innerClassCall = (2 == originalArgs.getExpressions().size());
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            Expression result = new MapStyleConstructorCall(getType(),
                    map.transformExpression(transformer),
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
            CompileStack compileStack = acg.getController().getCompileStack();

            int tmpMap = -1;
            if (!(map instanceof MapExpression)) {
                // evaluate the argument before the object is created
                map.visit(acg);
                tmpMap = compileStack.defineTemporaryVariable("tmpMap", ClassHelper.MAP_TYPE, true);
            }

            ClassNode ctorType = getType();
            if (tmpMap != -1) {
                // a null map selects no constructor, as for a dynamic call
                Label nonNull = new Label();
                mv.visitVarInsn(ALOAD, tmpMap);
                mv.visitJumpInsn(IFNONNULL, nonNull);
                mv.visitTypeInsn(NEW, "groovy/lang/GroovyRuntimeException");
                mv.visitInsn(DUP);
                String argTypes = (innerClassCall && ctorType.getOuterClass() != null ? ctorType.getOuterClass().getName() + ", " : "") + "null";
                mv.visitLdcInsn("Could not find matching constructor for: " + ctorType.getName() + "(" + argTypes + ")");
                mv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/GroovyRuntimeException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(nonNull);
            }
            // create temporary variable to store new object instance
            int tmpObj = compileStack.defineTemporaryVariable("tmpObj", ctorType, false);
            String ctorTypeName = BytecodeHelper.getClassInternalName(ctorType);
            mv.visitTypeInsn(NEW, ctorTypeName);
            mv.visitInsn(DUP);
            String signature = "()V";
            if (innerClassCall && ctorType.getOuterClass() != null) {
                acg.visitVariableExpression(varX("this")); // GROOVY-11122
                Parameter[] params = {new Parameter(ctorType.getOuterClass(),"$p$")};
                signature = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, params);
            }
            mv.visitMethodInsn(INVOKESPECIAL, ctorTypeName, "<init>", signature, false);
            mv.visitVarInsn(ASTORE, tmpObj);

            if (map instanceof MapExpression) {
                setProperties((MapExpression) map, tmpObj);
            } else {
                setProperties(mv, tmpMap, tmpObj);
            }

            // result object
            mv.visitVarInsn(ALOAD, tmpObj);

            // cleanup stack
            compileStack.removeVar(tmpObj);
            if (tmpMap != -1) compileStack.removeVar(tmpMap);
        }

        private void setProperties(final MapExpression map, final int tmpObj) {
            OperandStack operandStack = acg.getController().getOperandStack();
            ClassNode ctorType = getType();
            Expression objExpression = bytecodeX(ctorType, v -> v.visitVarInsn(ALOAD, tmpObj));

            // process property initializers
            for (MapEntryExpression entryExpression : map.getMapEntryExpressions()) {
                Expression keyExpression = entryExpression.getKeyExpression();
                Expression varExpression = propX(objExpression, keyExpression);
                Expression valExpression = entryExpression.getValueExpression();

                objExpression.setSourcePosition(keyExpression); // GROOVY-11956

                if (keyExpression instanceof ConstantExpression) { // a dynamic key's target is its own (GROOVY-12456)
                    varExpression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET,
                            keyExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
                }

                Expression setExpression = staticCompilationTransformer.transform(
                        assignX(varExpression, valExpression) // tmp.key = value
                );
                setExpression.setSourcePosition(entryExpression);
                // the transformed expression may or may not write a line number (a setter
                // call of a compiled class does not), so write the entry's own line first
                acg.onLineNumber(entryExpression, "visitMapEntryExpression");
                setExpression.visit(acg);
                operandStack.pop();
            }
        }

        private void setProperties(final MethodVisitor mv, final int tmpMap, final int tmpObj) {
            CompileStack compileStack = acg.getController().getCompileStack();
            // for (Map.Entry entry : map.entrySet())
            //     InvokerHelper.getMetaClass(tmp).setProperty(tmp, entry.getKey().toString(), entry.getValue())
            mv.visitVarInsn(ALOAD, tmpMap);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "entrySet", "()Ljava/util/Set;", true);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;", true);
            int tmpIter = compileStack.defineTemporaryVariable("tmpIter", ClassHelper.Iterator_TYPE, false);
            mv.visitVarInsn(ASTORE, tmpIter);

            Label loop = new Label(), done = new Label();
            mv.visitLabel(loop);
            mv.visitVarInsn(ALOAD, tmpIter);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            mv.visitJumpInsn(IFEQ, done);

            mv.visitVarInsn(ALOAD, tmpObj);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "getMetaClass", "(Ljava/lang/Object;)Lgroovy/lang/MetaClass;", false);
            mv.visitVarInsn(ALOAD, tmpObj);
            mv.visitVarInsn(ALOAD, tmpIter);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;", true);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;", true);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V", true);
            mv.visitJumpInsn(GOTO, loop);
            mv.visitLabel(done);

            compileStack.removeVar(tmpIter);
        }
    }
}
