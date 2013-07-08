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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.*;

public class BinaryExpressionTransformer {
    private final static MethodNode COMPARE_TO_METHOD = ClassHelper.COMPARABLE_TYPE.getMethods("compareTo").get(0);

    private static final ConstantExpression CONSTANT_ZERO = new ConstantExpression(0, true);
    private static final ConstantExpression CONSTANT_MINUS_ONE = new ConstantExpression(-1, true);
    private static final ConstantExpression CONSTANT_ONE = new ConstantExpression(1, true);

    static {
        CONSTANT_ZERO.setType(ClassHelper.int_TYPE);
        CONSTANT_ONE.setType(ClassHelper.int_TYPE);
        CONSTANT_MINUS_ONE.setType(ClassHelper.int_TYPE);
    }

    private int tmpVarCounter = 0;

    private final StaticCompilationTransformer staticCompilationTransformer;

    public BinaryExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformBinaryExpression(final BinaryExpression bin) {
        Object[] list = (Object[]) bin.getNodeMetaData(BINARY_EXP_TARGET);
        Token operation = bin.getOperation();
        int operationType = operation.getType();
        Expression rightExpression = bin.getRightExpression();
        Expression leftExpression = bin.getLeftExpression();
        if (operationType==Types.COMPARE_EQUAL || operationType == Types.COMPARE_NOT_EQUAL) {
            // let's check if one of the operands is the null constant
            CompareToNullExpression compareToNullExpression = null;
            if (isNullConstant(leftExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(rightExpression), operationType==Types.COMPARE_EQUAL);
            } else if (isNullConstant(rightExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(leftExpression), operationType==Types.COMPARE_EQUAL);
            }
            if (compareToNullExpression != null) {
                compareToNullExpression.setSourcePosition(bin);
                return compareToNullExpression;
            }
        } else if (operationType==Types.KEYWORD_IN) {
            MethodCallExpression call = new MethodCallExpression(
                    rightExpression,
                    "isCase",
                    leftExpression
            );
            call.setMethodTarget((MethodNode) bin.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET));
            call.setSourcePosition(bin);
            call.copyNodeMetaData(bin);
            TernaryExpression tExp = new TernaryExpression(
                    new BooleanExpression(
                            new BinaryExpression(rightExpression, Token.newSymbol("==",-1,-1), new ConstantExpression(null))
                    ),
                    new BinaryExpression(leftExpression, Token.newSymbol("==", -1, -1), new ConstantExpression(null)),
                    call
            );
            return staticCompilationTransformer.transform(tExp);
        }
        if (list != null) {
            if (operationType == Types.COMPARE_TO) {
                StaticTypesTypeChooser typeChooser = staticCompilationTransformer.getTypeChooser();
                ClassNode classNode = staticCompilationTransformer.getClassNode();
                ClassNode leftType = typeChooser.resolveType(leftExpression, classNode);
                if (leftType.implementsInterface(ClassHelper.COMPARABLE_TYPE)) {
                    ClassNode rightType = typeChooser.resolveType(rightExpression, classNode);
                    if (rightType.implementsInterface(ClassHelper.COMPARABLE_TYPE)) {
                        Expression left = staticCompilationTransformer.transform(leftExpression);
                        Expression right = staticCompilationTransformer.transform(rightExpression);
                        MethodCallExpression call = new MethodCallExpression(left, "compareTo", new ArgumentListExpression(right));
                        call.setImplicitThis(false);
                        call.setMethodTarget(COMPARE_TO_METHOD);

                        CompareIdentityExpression compareIdentity = new CompareIdentityExpression(
                                left, right
                        );
                        compareIdentity.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, ClassHelper.boolean_TYPE);
                        TernaryExpression result = new TernaryExpression(
                                new BooleanExpression(compareIdentity), // a==b
                                CONSTANT_ZERO,
                                new TernaryExpression(
                                        new BooleanExpression(new CompareToNullExpression(left, true)), // a==null
                                        CONSTANT_MINUS_ONE,
                                        new TernaryExpression(
                                                new BooleanExpression(new CompareToNullExpression(right, true)), // b==null
                                                CONSTANT_ONE,
                                                call
                                        )
                                )
                        );
                        compareIdentity.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, ClassHelper.int_TYPE);
                        result.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
                        TernaryExpression expr = (TernaryExpression) result.getFalseExpression();
                        expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
                        expr.getFalseExpression().putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
                        return result;
                    }
                }
            }
            boolean isAssignment = StaticTypeCheckingSupport.isAssignment(operationType);
            MethodCallExpression call;
            MethodNode node = (MethodNode) list[0];
            String name = (String) list[1];
            Expression left = staticCompilationTransformer.transform(leftExpression);
            Expression right = staticCompilationTransformer.transform(rightExpression);
            call = new MethodCallExpression(
                    left,
                    name,
                    new ArgumentListExpression(right)
            );
            call.setImplicitThis(false);
            call.setMethodTarget(node);
            MethodNode adapter = StaticCompilationTransformer.BYTECODE_BINARY_ADAPTERS.get(operationType);
            if (adapter != null) {
                ClassExpression sba = new ClassExpression(StaticCompilationTransformer.BYTECODE_ADAPTER_CLASS);
                // replace with compareEquals
                call = new MethodCallExpression(sba,
                        "compareEquals",
                        new ArgumentListExpression(left, right));
                call.setMethodTarget(adapter);
                call.setImplicitThis(false);
            }
            if (!isAssignment) return call;
            // case of +=, -=, /=, ...
            // the method represents the operation type only, and we must add an assignment
            return new BinaryExpression(left, Token.newSymbol("=", operation.getStartLine(), operation.getStartColumn()), call);
        }
        if (bin.getOperation().getType() == Types.EQUAL && leftExpression instanceof TupleExpression && rightExpression instanceof ListExpression) {
            // multiple assignment
            ListOfExpressionsExpression cle = new ListOfExpressionsExpression();
            boolean isDeclaration = bin instanceof DeclarationExpression;
            List<Expression> leftExpressions = ((TupleExpression) leftExpression).getExpressions();
            List<Expression> rightExpressions = ((ListExpression) rightExpression).getExpressions();
            Iterator<Expression> leftIt = leftExpressions.iterator();
            Iterator<Expression> rightIt = rightExpressions.iterator();
            if (isDeclaration) {
            while (leftIt.hasNext()) {
                Expression left = leftIt.next();
                if (rightIt.hasNext()) {
                    Expression right = rightIt.next();
                    BinaryExpression bexp = new DeclarationExpression(left, bin.getOperation(), right);
                    bexp.setSourcePosition(right);
                    cle.addExpression(bexp);
                }
            }
            } else {
                // (next, result) = [ result, next+result ]
                // -->
                // def tmp1 = result
                // def tmp2 = next+result
                // next = tmp1
                // result = tmp2
                int size = rightExpressions.size();
                List<Expression> tmpAssignments = new ArrayList<Expression>(size);
                List<Expression> finalAssignments = new ArrayList<Expression>(size);
                for (int i=0; i<Math.min(size, leftExpressions.size());i++ ) {
                    Expression left = leftIt.next();
                    Expression right = rightIt.next();
                    VariableExpression tmpVar = new VariableExpression("$tmpVar$"+tmpVarCounter++);
                    BinaryExpression bexp = new DeclarationExpression(tmpVar, bin.getOperation(), right);
                    bexp.setSourcePosition(right);
                    tmpAssignments.add(bexp);
                    bexp = new BinaryExpression(left, bin.getOperation(), new VariableExpression(tmpVar));
                    bexp.setSourcePosition(left);
                    finalAssignments.add(bexp);
                }
                for (Expression tmpAssignment : tmpAssignments) {
                    cle.addExpression(tmpAssignment);
                }
                for (Expression finalAssignment : finalAssignments) {
                    cle.addExpression(finalAssignment);
                }
            }
            return staticCompilationTransformer.transform(cle);
        }
        return staticCompilationTransformer.superTransform(bin);
    }

    protected static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue()==null;
    }

}