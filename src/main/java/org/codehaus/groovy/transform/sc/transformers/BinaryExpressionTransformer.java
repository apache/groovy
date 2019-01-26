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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.sc.StaticPropertyAccessHelper;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isCompareToBoolean;

public class BinaryExpressionTransformer {
    private static final MethodNode COMPARE_TO_METHOD = ClassHelper.COMPARABLE_TYPE.getMethods("compareTo").get(0);

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
        if (bin instanceof DeclarationExpression) {
            Expression optimized = transformDeclarationExpression(bin);
            if (optimized!=null) {
                return optimized;
            }
        }
        Object[] list = bin.getNodeMetaData(BINARY_EXP_TARGET);
        Token operation = bin.getOperation();
        int operationType = operation.getType();
        Expression rightExpression = bin.getRightExpression();
        Expression leftExpression = bin.getLeftExpression();
        if (bin instanceof DeclarationExpression && leftExpression instanceof VariableExpression) {
            ClassNode declarationType = ((VariableExpression) leftExpression).getOriginType();
            if (rightExpression instanceof ConstantExpression) {
                ClassNode unwrapper = ClassHelper.getUnwrapper(declarationType);
                ClassNode wrapper = ClassHelper.getWrapper(declarationType);
                if (!rightExpression.getType().equals(declarationType)
                        && wrapper.isDerivedFrom(ClassHelper.Number_TYPE)
                        && WideningCategories.isDoubleCategory(unwrapper)) {
                    ConstantExpression constant = (ConstantExpression) rightExpression;
                    if (constant.getValue()!=null) {
                        return optimizeConstantInitialization(bin, operation, constant, leftExpression, declarationType);
                    }
                }
            }
        }
        if (operationType == Types.EQUAL && leftExpression instanceof PropertyExpression) {
            MethodNode directMCT = leftExpression.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
            if (directMCT != null) {
                return transformPropertyAssignmentToSetterCall((PropertyExpression) leftExpression, rightExpression, directMCT);
            }
        }
        if (operationType == Types.COMPARE_EQUAL || operationType == Types.COMPARE_NOT_EQUAL) {
            // let's check if one of the operands is the null constant
            CompareToNullExpression compareToNullExpression = null;
            if (isNullConstant(leftExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(rightExpression), operationType == Types.COMPARE_EQUAL);
            } else if (isNullConstant(rightExpression)) {
                compareToNullExpression = new CompareToNullExpression(staticCompilationTransformer.transform(leftExpression), operationType == Types.COMPARE_EQUAL);
            }
            if (compareToNullExpression != null) {
                compareToNullExpression.setSourcePosition(bin);
                return compareToNullExpression;
            }
        } else if (operationType == Types.KEYWORD_IN) {
            return convertInOperatorToTernary(bin, rightExpression, leftExpression);
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
                        call.setSourcePosition(bin);

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
            BinaryExpression optimized = tryOptimizeCharComparison(left, right, bin);
            if (optimized!=null) {
                optimized.removeNodeMetaData(BINARY_EXP_TARGET);
                return transformBinaryExpression(optimized);
            }
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
            call.setSourcePosition(bin);
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
                List<Expression> tmpAssignments = new ArrayList<>(size);
                List<Expression> finalAssignments = new ArrayList<>(size);
                for (int i = 0; i < Math.min(size, leftExpressions.size()); i++) {
                    Expression left = leftIt.next();
                    Expression right = rightIt.next();
                    VariableExpression tmpVar = new VariableExpression("$tmpVar$" + tmpVarCounter++);
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

    private static BinaryExpression tryOptimizeCharComparison(final Expression left, final Expression right, final BinaryExpression bin) {
        int op = bin.getOperation().getType();
        if (isCompareToBoolean(op) || op == COMPARE_EQUAL || op == COMPARE_NOT_EQUAL) {
            Character cLeft = tryCharConstant(left);
            Character cRight = tryCharConstant(right);
            if (cLeft != null || cRight != null) {
                Expression oLeft = cLeft == null ? left : new ConstantExpression(cLeft, true);
                oLeft.setSourcePosition(left);
                Expression oRight = cRight == null ? right : new ConstantExpression(cRight, true);
                oRight.setSourcePosition(right);
                bin.setLeftExpression(oLeft);
                bin.setRightExpression(oRight);
                return bin;
            }
        }
        return null;
    }

    private static Character tryCharConstant(final Expression expr) {
        if (expr instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) expr;
            if (ClassHelper.STRING_TYPE.equals(ce.getType())) {
                String val = (String) ce.getValue();
                if (val!=null && val.length()==1) {
                    return val.charAt(0);
                }
            }
        }
        return null;
    }

    private static Expression transformDeclarationExpression(final BinaryExpression bin) {
        Expression leftExpression = bin.getLeftExpression();
        if (leftExpression instanceof VariableExpression) {
            if (ClassHelper.char_TYPE.equals(((VariableExpression) leftExpression).getOriginType())) {
                Expression rightExpression = bin.getRightExpression();
                if (rightExpression instanceof ConstantExpression && ClassHelper.STRING_TYPE.equals(rightExpression.getType())) {
                    String text = (String) ((ConstantExpression) rightExpression).getValue();
                    if (text.length() == 1) {
                        // optimize char initialization
                        ConstantExpression ce = new ConstantExpression(
                                text.charAt(0),
                                true
                        );
                        ce.setSourcePosition(rightExpression);
                        bin.setRightExpression(ce);
                        return bin;
                    }
                }
            }
        }
        return null;
    }

    private Expression convertInOperatorToTernary(final BinaryExpression bin, final Expression rightExpression, final Expression leftExpression) {
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
                        new BinaryExpression(rightExpression, Token.newSymbol("==", -1, -1), new ConstantExpression(null))
                ),
                new BinaryExpression(leftExpression, Token.newSymbol("==", -1, -1), new ConstantExpression(null)),
                call
        );
        return staticCompilationTransformer.transform(tExp);
    }

    private static DeclarationExpression optimizeConstantInitialization(
            final BinaryExpression originalDeclaration,
            final Token operation,
            final ConstantExpression constant,
            final Expression leftExpression,
            final ClassNode declarationType) {
        ConstantExpression cexp = new ConstantExpression(
                convertConstant((Number) constant.getValue(), ClassHelper.getWrapper(declarationType)), true);
        cexp.setType(declarationType);
        cexp.setSourcePosition(constant);
        DeclarationExpression result = new DeclarationExpression(
                leftExpression,
                operation,
                cexp
        );
        result.setSourcePosition(originalDeclaration);
        result.copyNodeMetaData(originalDeclaration);
        return result;
    }

    private static Object convertConstant(Number source, ClassNode target) {
        if (ClassHelper.Byte_TYPE.equals(target)) {
            return source.byteValue();
        }
        if (ClassHelper.Short_TYPE.equals(target)) {
            return source.shortValue();
        }
        if (ClassHelper.Integer_TYPE.equals(target)) {
            return source.intValue();
        }
        if (ClassHelper.Long_TYPE.equals(target)) {
            return source.longValue();
        }
        if (ClassHelper.Float_TYPE.equals(target)) {
            return source.floatValue();
        }
        if (ClassHelper.Double_TYPE.equals(target)) {
            return source.doubleValue();
        }
        if (ClassHelper.BigInteger_TYPE.equals(target)) {
            return DefaultGroovyMethods.asType(source, BigInteger.class);
        }
        if (ClassHelper.BigDecimal_TYPE.equals(target)) {
            return DefaultGroovyMethods.asType(source, BigDecimal.class);
        }
        throw new IllegalArgumentException("Unsupported conversion");
    }

    private Expression transformPropertyAssignmentToSetterCall(final PropertyExpression leftExpression, final Expression rightExpression, final MethodNode directMCT) {
        // transform "a.x = b" into "def tmp = b; a.setX(tmp); tmp"
        Expression arg = staticCompilationTransformer.transform(rightExpression);
        return StaticPropertyAccessHelper.transformToSetterCall(
                leftExpression.getObjectExpression(),
                directMCT,
                arg,
                false,
                leftExpression.isSafe(),
                false,
                true, // to be replaced with a proper test whether a return value should be used or not
                leftExpression
        );
    }

    protected static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue() == null;
    }

}