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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class MethodCallExpressionTransformer {
    private static final ClassNode DGM_CLASSNODE = ClassHelper.make(DefaultGroovyMethods.class);

    private final StaticCompilationTransformer staticCompilationTransformer;

    public MethodCallExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformMethodCallExpression(final MethodCallExpression expr) {
        Expression trn = tryTransformIsToCompareIdentity(expr);
        if (trn!=null) {
            return trn;
        }
        ClassNode superCallReceiver = expr.getNodeMetaData(StaticTypesMarker.SUPER_MOP_METHOD_REQUIRED);
        if (superCallReceiver!=null) {
            return transformMethodCallExpression(transformToMopSuperCall(superCallReceiver, expr));
        }
        Expression objectExpression = expr.getObjectExpression();
        ClassNode type = staticCompilationTransformer.getTypeChooser().resolveType(objectExpression, staticCompilationTransformer.getClassNode());
        if (isCallOnClosure(expr) && staticCompilationTransformer.getClassNode() != null) {
            FieldNode field = staticCompilationTransformer.getClassNode().getField(expr.getMethodAsString());
            if (field != null) {
                VariableExpression vexp = new VariableExpression(field);
                MethodCallExpression result = new MethodCallExpression(
                        vexp,
                        "call",
                        staticCompilationTransformer.transform(expr.getArguments())
                );
                result.setImplicitThis(false);
                result.setSourcePosition(expr);
                result.setSafe(expr.isSafe());
                result.setSpreadSafe(expr.isSpreadSafe());
                result.setMethodTarget(StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS);
                result.copyNodeMetaData(expr);
                return result;
            }
        }
        if (type != null && type.isArray()) {
            String method = expr.getMethodAsString();
            ClassNode componentType = type.getComponentType();
            if ("getAt".equals(method)) {
                Expression arguments = expr.getArguments();
                if (arguments instanceof TupleExpression) {
                    List<Expression> argList = ((TupleExpression) arguments).getExpressions();
                    if (argList.size() == 1) {
                        Expression indexExpr = argList.get(0);
                        ClassNode argType = staticCompilationTransformer.getTypeChooser().resolveType(indexExpr, staticCompilationTransformer.getClassNode());
                        ClassNode indexType = ClassHelper.getWrapper(argType);
                        if (componentType.isEnum() && ClassHelper.Number_TYPE == indexType) {
                            // workaround for generated code in enums which use .next() returning a Number
                            indexType = ClassHelper.Integer_TYPE;
                        }
                        if (argType != null && ClassHelper.Integer_TYPE == indexType) {
                            BinaryExpression binaryExpression = new BinaryExpression(
                                    objectExpression,
                                    Token.newSymbol("[", indexExpr.getLineNumber(), indexExpr.getColumnNumber()),
                                    indexExpr
                            );
                            binaryExpression.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, componentType);
                            return staticCompilationTransformer.transform(binaryExpression);
                        }
                    }
                }
            } else if ("putAt".equals(method)) {
                Expression arguments = expr.getArguments();
                if (arguments instanceof TupleExpression) {
                    List<Expression> argList = ((TupleExpression) arguments).getExpressions();
                    if (argList.size() == 2) {
                        Expression indexExpr = argList.get(0);
                        Expression objExpr = argList.get(1);
                        ClassNode argType = staticCompilationTransformer.getTypeChooser().resolveType(indexExpr, staticCompilationTransformer.getClassNode());
                        if (argType != null && ClassHelper.Integer_TYPE == ClassHelper.getWrapper(argType)) {
                            BinaryExpression arrayGet = new BinaryExpression(
                                    objectExpression,
                                    Token.newSymbol("[", indexExpr.getLineNumber(), indexExpr.getColumnNumber()),
                                    indexExpr
                            );
                            arrayGet.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, componentType);
                            BinaryExpression assignment = new BinaryExpression(
                                    arrayGet,
                                    Token.newSymbol("=", objExpr.getLineNumber(), objExpr.getColumnNumber()),
                                    objExpr
                            );
                            return staticCompilationTransformer.transform(assignment);
                        }
                    }
                }
            }
        }
        return staticCompilationTransformer.superTransform(expr);
    }

    private static MethodCallExpression transformToMopSuperCall(final ClassNode superCallReceiver, final MethodCallExpression expr) {
        MethodNode mn = expr.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        String mopName = MopWriter.getMopMethodName(mn, false);
        MethodNode direct = new MethodNode(
                mopName,
                ACC_PUBLIC | ACC_SYNTHETIC,
                mn.getReturnType(),
                mn.getParameters(),
                mn.getExceptions(),
                EmptyStatement.INSTANCE
        );
        direct.setDeclaringClass(superCallReceiver);
        MethodCallExpression result = new MethodCallExpression(
                new VariableExpression("this"),
                mopName,
                expr.getArguments()
        );
        result.setImplicitThis(true);
        result.setSpreadSafe(false);
        result.setSafe(false);
        result.setSourcePosition(expr);
        result.setMethodTarget(direct);
        return result;
    }

    private static boolean isCallOnClosure(final MethodCallExpression expr) {
        MethodNode target = expr.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        return expr.isImplicitThis()
                && !"call".equals(expr.getMethodAsString())
                && (target == StaticTypeCheckingVisitor.CLOSURE_CALL_VARGS
                    || target == StaticTypeCheckingVisitor.CLOSURE_CALL_NO_ARG
                    || target == StaticTypeCheckingVisitor.CLOSURE_CALL_ONE_ARG);
    }

    /**
     * Identifies a method call expression on {@link DefaultGroovyMethods#is(Object, Object)} and if recognized, transforms it into a {@link CompareIdentityExpression}.
     * @param call a method call to be transformed
     * @return null if the method call is not DGM#is, or {@link CompareIdentityExpression}
     */
    private static Expression tryTransformIsToCompareIdentity(MethodCallExpression call) {
        if (call.isSafe()) return null;
        MethodNode methodTarget = call.getMethodTarget();
        if (methodTarget instanceof ExtensionMethodNode && "is".equals(methodTarget.getName()) && methodTarget.getParameters().length==1) {
            methodTarget = ((ExtensionMethodNode) methodTarget).getExtensionMethodNode();
            ClassNode owner = methodTarget.getDeclaringClass();
            if (DGM_CLASSNODE.equals(owner)) {
                Expression args = call.getArguments();
                if (args instanceof ArgumentListExpression) {
                    ArgumentListExpression arguments = (ArgumentListExpression) args;
                    List<Expression> exprs = arguments.getExpressions();
                    if (exprs.size() == 1) {
                        CompareIdentityExpression cid = new CompareIdentityExpression(call.getObjectExpression(), exprs.get(0));
                        cid.setSourcePosition(call);
                        return cid;
                    }
                }
            }
        }
        return null;
    }
}