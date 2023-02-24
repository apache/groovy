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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.sc.StaticPropertyAccessHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.apache.groovy.ast.tools.ExpressionUtils.isNullConstant;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisOrSuper;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public class BinaryExpressionTransformer {
    private static final MethodNode COMPARE_TO_METHOD = ClassHelper.COMPARABLE_TYPE.getMethods("compareTo").get(0);
    private static final ConstantExpression CONSTANT_MINUS_ONE = constX(-1, true);
    private static final ConstantExpression CONSTANT_ZERO = constX(0, true);
    private static final ConstantExpression CONSTANT_ONE = constX(1, true);

    private int tmpVarCounter;

    private final StaticCompilationTransformer staticCompilationTransformer;

    public BinaryExpressionTransformer(final StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    public Expression transformBinaryExpression(final BinaryExpression bin) {
        Expression leftExpression = bin.getLeftExpression();
        Expression rightExpression = bin.getRightExpression();

        if (bin instanceof DeclarationExpression
                && leftExpression instanceof VariableExpression
                && rightExpression instanceof ConstantExpression
                && !((ConstantExpression) rightExpression).isNullExpression()) {
            ClassNode declarationType = ((VariableExpression) leftExpression).getOriginType();
            // for "char x = 'c'" change 'c' from String to char
            if (declarationType.equals(ClassHelper.char_TYPE)) {
                Character c = tryCharConstant(rightExpression);
                if (c != null)
                    return transformCharacterInitialization(bin, c);
            }
            // for "int|long|short|byte|char|float|double|BigDecimal|BigInteger x = n" change n's type
            if (!declarationType.equals(rightExpression.getType())
                    && WideningCategories.isDoubleCategory(ClassHelper.getUnwrapper(declarationType))
                    && ClassHelper.getWrapper(rightExpression.getType()).isDerivedFrom(ClassHelper.Number_TYPE)) {
                return transformNumericalInitialization(bin, (Number) ((ConstantExpression) rightExpression).getValue(), declarationType);
            }
        }

        boolean equal = false;
        switch (bin.getOperation().getType()) {
          case Types.ASSIGN:
            optimizeArrayCollectionAssignment(bin); // GROOVY-10029
            Expression expr = transformAssignmentToSetterCall(bin);
            if (expr != null) return expr;
            if (leftExpression instanceof TupleExpression
                    && rightExpression instanceof ListExpression) {
                return transformMultipleAssignment(bin);
            }
            break;
          case Types.KEYWORD_IN:
            equal = true; //fallthrough
          case Types.COMPARE_NOT_IN:
            return transformInOperation(bin, equal);
          case Types.COMPARE_EQUAL:
          case Types.COMPARE_IDENTICAL:
            equal = true; //fallthrough
          case Types.COMPARE_NOT_EQUAL:
          case Types.COMPARE_NOT_IDENTICAL:
            expr = transformEqualityComparison(bin, equal);
            if (expr != null) return expr; else break;
          case Types.COMPARE_TO:
            expr = transformRelationComparison(bin);
            if (expr != null) return expr;
        }

        Object[] array = bin.getNodeMetaData(StaticCompilationMetadataKeys.BINARY_EXP_TARGET);
        if (array != null) {
            return transformToTargetMethodCall(bin, (MethodNode) array[0], (String) array[1]);
        }

        return staticCompilationTransformer.superTransform(bin);
    }

    private Expression transformCharacterInitialization(final BinaryExpression bin, final Character rhs) {
        Expression ce = constX(rhs, true);
        ce.setSourcePosition(bin.getRightExpression());

        bin.setRightExpression(ce);
        return bin;
    }

    private Expression transformNumericalInitialization(final BinaryExpression bin, final Number rhs, final ClassNode lhsType) {
        Expression ce = constX(convertConstant(rhs, ClassHelper.getWrapper(lhsType)), true);
        ce.setSourcePosition(bin.getRightExpression());
        ce.setType(lhsType);

        bin.setRightExpression(ce);
        return bin;
    }

    /**
     * Adds "?.toArray(new T[0])" to "T[] array = collectionOfT" assignments.
     */
    private void optimizeArrayCollectionAssignment(final BinaryExpression bin) {
        Expression rightExpression = bin.getRightExpression();
        ClassNode leftType = findType(bin.getLeftExpression()), rightType = findType(rightExpression);
        if (leftType.isArray() && !(rightExpression instanceof ListExpression) && isOrImplements(rightType, ClassHelper.COLLECTION_TYPE)) {
            ArrayExpression emptyArray = new ArrayExpression(leftType.getComponentType(), null, Collections.singletonList(CONSTANT_ZERO));
            rightExpression = callX(rightExpression, "toArray", args(emptyArray));
            rightExpression.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, leftType);
            ((MethodCallExpression) rightExpression).setMethodTarget(
                    rightType.getMethod("toArray", new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE.makeArray(), "a")}));
            ((MethodCallExpression) rightExpression).setImplicitThis(false);
            ((MethodCallExpression) rightExpression).setSafe(true);
            bin.setRightExpression(rightExpression);
        }
    }

    private Expression transformAssignmentToSetterCall(final BinaryExpression bin) {
        MethodNode directMCT = bin.getLeftExpression().getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (directMCT != null) {
            Expression left = staticCompilationTransformer.transform(bin.getLeftExpression());
            Expression right = staticCompilationTransformer.transform(bin.getRightExpression());
            if (left instanceof PropertyExpression) {
                // transform "a.x = val" into "def tmp = val; a.setX(tmp); tmp"
                PropertyExpression pe = (PropertyExpression) left;
                return transformAssignmentToSetterCall(
                        pe.getObjectExpression(), // "a"
                        directMCT, // "setX"
                        right, // "val"
                        false,
                        pe.isSafe(),
                        pe.getProperty(), // "x"
                        bin);
            }
            if (left instanceof VariableExpression) {
                // transform "x = val" into "def tmp = val; this.setX(tmp); tmp"
                return transformAssignmentToSetterCall(
                        varX("this"),
                        directMCT, // "setX"
                        right, // "val"
                        true,
                        false,
                        left, // "x"
                        bin);
            }
        }
        return null;
    }

    /**
     * Adapter for {@link StaticPropertyAccessHelper#transformToSetterCall}.
     */
    private static Expression transformAssignmentToSetterCall(
            final Expression receiver,
            final MethodNode setterMethod,
            final Expression valueExpression,
            final boolean implicitThis,
            final boolean safeNavigation,
            final Expression nameExpression,
            final Expression sourceExpression) {
        // expression that will transfer assignment and name positions
        Expression pos = new PropertyExpression(null, nameExpression);
        pos.setSourcePosition(sourceExpression);

        return StaticPropertyAccessHelper.transformToSetterCall(
                receiver,
                setterMethod,
                valueExpression,
                implicitThis,
                safeNavigation,
                false, // spreadSafe
                true, // TODO: replace with a proper test whether a return value is required or not
                pos);
    }

    private Expression transformInOperation(final BinaryExpression bin, final boolean in) {
        Expression leftExpression = bin.getLeftExpression(), rightExpression = bin.getRightExpression();

        // transform "left [!]in right" into "right.is[Not]Case(left)"
        MethodCallExpression call = callX(rightExpression, in ? "isCase" : "isNotCase", leftExpression);
        call.setImplicitThis(false); call.setSourcePosition(bin); call.copyNodeMetaData(bin);
        call.setMethodTarget(bin.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET));
        // GROOVY-7473: no null test for simple cases
        if (rightExpression instanceof ListExpression
                || rightExpression instanceof MapExpression
                || rightExpression instanceof RangeExpression
                || rightExpression instanceof ClassExpression
                ||(rightExpression instanceof ConstantExpression
                            && !isNullConstant(rightExpression))
                // rightExpression instanceof VariableExpression
                || isThisOrSuper(rightExpression))//GROOVY-10909
            return staticCompilationTransformer.transform(call);

        // GROOVY-6137, GROOVY-7473: null safety and one-time evaluation
        call.setObjectExpression(rightExpression = transformRepeatedReference(rightExpression));
        Expression safe = ternaryX(new CompareToNullExpression(rightExpression,true), new CompareToNullExpression(leftExpression,in), call);
        safe.putNodeMetaData("classgen.callback", classgenCallback(call.getObjectExpression()));
        return staticCompilationTransformer.transform(safe);
    }

    private Expression transformRepeatedReference(final Expression exp) {
        if (exp instanceof ConstantExpression || exp instanceof VariableExpression
                && ((VariableExpression) exp).getAccessedVariable() instanceof Parameter) {
            return exp;
        }
        return new TemporaryVariableExpression(exp);
    }

    private Expression transformEqualityComparison(final BinaryExpression bin, final boolean eq) {
        Expression leftExpression = bin.getLeftExpression(), rightExpression = bin.getRightExpression();
        if (isNullConstant(rightExpression)) {
            Expression ctn = new CompareToNullExpression(staticCompilationTransformer.transform(leftExpression), eq);
            ctn.setSourcePosition(bin);
            return ctn;
        }
        if (isNullConstant(leftExpression)) {
            Expression ctn = new CompareToNullExpression(staticCompilationTransformer.transform(rightExpression), eq);
            ctn.setSourcePosition(bin);
            return ctn;
        }
        if (bin.getOperation().getText().length() == 3
                && !ClassHelper.isPrimitiveType(findType(leftExpression))
                && !ClassHelper.isPrimitiveType(findType(rightExpression))) {
            Expression cid = new CompareIdentityExpression(staticCompilationTransformer.transform(leftExpression), eq, staticCompilationTransformer.transform(rightExpression));
            cid.setSourcePosition(bin);
            return cid;
        }
        return null;
    }

    private Expression transformRelationComparison(final BinaryExpression bin) {
        Expression leftExpression = bin.getLeftExpression(), rightExpression = bin.getRightExpression();
        ClassNode leftType = findType(leftExpression), rightType = findType(rightExpression);

        // same-type primitive compare
        if (leftType.equals(rightType)
                && ClassHelper.isPrimitiveType(leftType)
                || ClassHelper.isPrimitiveType(rightType)) {
            ClassNode wrapperType = ClassHelper.getWrapper(leftType);
            Expression leftAndRight = args(
                staticCompilationTransformer.transform(leftExpression),
                staticCompilationTransformer.transform(rightExpression)
            );
            // transform "a <=> b" into "[Integer|Long|Short|Byte|Double|Float|...].compare(a,b)"
            MethodCallExpression call = callX(classX(wrapperType), "compare", leftAndRight);
            call.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
            call.setMethodTarget(wrapperType.getMethods("compare").get(0));
            call.setImplicitThis(false);
            call.setSourcePosition(bin);
            return call;
        }

        if (leftType.implementsInterface(ClassHelper.COMPARABLE_TYPE)
                && rightType.implementsInterface(ClassHelper.COMPARABLE_TYPE)) {
            // GROOVY-5644, GROOVY-6137, GROOVY-7473, GROOVY-10394: null safety and one-time evaluation
            Expression left = transformRepeatedReference(staticCompilationTransformer.transform(leftExpression));
            Expression right = transformRepeatedReference(staticCompilationTransformer.transform(rightExpression));

            MethodCallExpression call = callX(left, "compareTo", args(right));
            call.setMethodTarget(COMPARE_TO_METHOD);
            call.setImplicitThis(false);
            call.setSourcePosition(bin);

            // right == null ? 1 : left.compareTo(right)
            Expression expr = ternaryX(new CompareToNullExpression(right, true), CONSTANT_ONE, call);
            expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);

            // left == null ? -1 : (right == null ? 1 : left.compareTo(right))
            expr = ternaryX(new CompareToNullExpression(left, true), CONSTANT_MINUS_ONE, expr);
            expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);

            // left === right ? 0 : (left == null ? -1 : (right == null ? 1 : left.compareTo(right)))
            expr = ternaryX(new CompareIdentityExpression(left, right), CONSTANT_ZERO, expr);
            expr.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.int_TYPE);
            expr.putNodeMetaData("classgen.callback", // pop temporary variables
                    classgenCallback(right).andThen(classgenCallback(left)));

            return expr;
        }

        return null;
    }

    private Expression transformMultipleAssignment(final BinaryExpression bin) {
        ListOfExpressionsExpression list = new ListOfExpressionsExpression();
        List<Expression> leftExpressions = ((TupleExpression) bin.getLeftExpression()).getExpressions();
        List<Expression> rightExpressions = ((ListExpression) bin.getRightExpression()).getExpressions();
        Iterator<Expression> leftIt = leftExpressions.iterator();
        Iterator<Expression> rightIt = rightExpressions.iterator();
        if (bin instanceof DeclarationExpression) {
            while (leftIt.hasNext()) {
                Expression left = leftIt.next();
                if (rightIt.hasNext()) {
                    Expression right = rightIt.next();
                    BinaryExpression bexp = new DeclarationExpression(left, bin.getOperation(), right);
                    bexp.setSourcePosition(right);
                    list.addExpression(bexp);
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
            for (int i = 0, n = Math.min(size, leftExpressions.size()); i < n; i += 1) {
                Expression left = leftIt.next();
                Expression right = rightIt.next();
                VariableExpression tmpVar = varX("$tmpVar$" + tmpVarCounter++);
                BinaryExpression bexp = new DeclarationExpression(tmpVar, bin.getOperation(), right);
                bexp.setSourcePosition(right);
                tmpAssignments.add(bexp);
                bexp = binX(left, bin.getOperation(), varX(tmpVar));
                bexp.setSourcePosition(left);
                finalAssignments.add(bexp);
            }
            for (Expression tmpAssignment : tmpAssignments) {
                list.addExpression(tmpAssignment);
            }
            for (Expression finalAssignment : finalAssignments) {
                list.addExpression(finalAssignment);
            }
        }
        return staticCompilationTransformer.transform(list);
    }

    private Expression transformToTargetMethodCall(final BinaryExpression bin, final MethodNode node, final String name) {
        Token operation = bin.getOperation(); int operationType = operation.getType();
        Expression left = staticCompilationTransformer.transform(bin.getLeftExpression());
        Expression right = staticCompilationTransformer.transform(bin.getRightExpression());

        Expression expr = tryOptimizeCharComparison(left, right, bin);
        if (expr != null) {
            expr.removeNodeMetaData(StaticCompilationMetadataKeys.BINARY_EXP_TARGET);
            expr.removeNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
            return expr;
        }

        MethodCallExpression call;
        // replace the binary expression with a method call to ScriptBytecodeAdapter or something else
        MethodNode adapter = StaticCompilationTransformer.BYTECODE_BINARY_ADAPTERS.get(operationType);
        if (adapter != null) {
            Expression sba = classX(StaticCompilationTransformer.BYTECODE_ADAPTER_CLASS);
            call = callX(sba, adapter.getName(), args(left, right));
            call.setMethodTarget(adapter);
        } else {
            call = callX(left, name, args(right));
            call.setMethodTarget(node);
        }
        call.setImplicitThis(false);
        if (Types.isAssignment(operationType)) { // +=, -=, /=, ...
            // call handles the operation, so we must add the assignment now
            expr = binX(left, Token.newSymbol(Types.ASSIGN, operation.getStartLine(), operation.getStartColumn()), call);
            // GROOVY-5746: one execution of receiver and subscript
            if (left instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) left;
                if (be.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                    be.setLeftExpression(transformRepeatedReference(be.getLeftExpression()));
                    be.setRightExpression(transformRepeatedReference(be.getRightExpression()));
                    expr.putNodeMetaData("classgen.callback", classgenCallback(be.getRightExpression())
                                                     .andThen(classgenCallback(be.getLeftExpression()))
                    );
                }
            }
        } else {
            expr = call;
        }
        expr.setSourcePosition(bin);
        return expr;
    }

    private BinaryExpression tryOptimizeCharComparison(final Expression left, final Expression right, final BinaryExpression bin) {
        int op = bin.getOperation().getType();
        if (StaticTypeCheckingSupport.isCompareToBoolean(op) || op == Types.COMPARE_EQUAL || op == Types.COMPARE_NOT_EQUAL) {
            Character cLeft = tryCharConstant(left);
            Character cRight = tryCharConstant(right);
            if (cLeft != null || cRight != null) {
                Expression oLeft = (cLeft == null ? left : constX(cLeft, true));
                if (oLeft instanceof PropertyExpression && !hasCharType(oLeft)) return null;
                oLeft.setSourcePosition(left);
                Expression oRight = (cRight == null ? right : constX(cRight, true));
                if (oRight instanceof PropertyExpression && !hasCharType(oRight)) return null;
                oRight.setSourcePosition(right);
                bin.setLeftExpression(oLeft);
                bin.setRightExpression(oRight);
                return bin;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------

    private ClassNode findType(final Expression e) {
        return staticCompilationTransformer.getTypeChooser().resolveType(e, staticCompilationTransformer.getClassNode());
    }

    private static boolean hasCharType(final Expression e) {
        ClassNode inferredType = e.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE); //TODO:findType(e);
        return inferredType != null && ClassHelper.getWrapper(inferredType).equals(ClassHelper.Character_TYPE);
    }

    private static Character tryCharConstant(final Expression e) {
        if (e instanceof ConstantExpression && ClassHelper.STRING_TYPE.equals(e.getType())) {
            String value = (String) ((ConstantExpression) e).getValue();
            if (value != null && value.length() == 1) {
                return value.charAt(0);
            }
        }
        return null;
    }

    private static Object convertConstant(final Number source, final ClassNode target) {
        if (ClassHelper.isWrapperInteger(target)) {
            return source.intValue();
        }
        if (ClassHelper.isWrapperLong(target)) {
            return source.longValue();
        }
        if (ClassHelper.isWrapperByte(target)) {
            return source.byteValue();
        }
        if (ClassHelper.isWrapperShort(target)) {
            return source.shortValue();
        }
        if (ClassHelper.isWrapperFloat(target)) {
            return source.floatValue();
        }
        if (ClassHelper.isWrapperDouble(target)) {
            return source.doubleValue();
        }
        if (ClassHelper.isWrapperCharacter(target)) {
            return (char) source.intValue();
        }
        if (ClassHelper.isBigDecimalType(target)) {
            return DefaultGroovyMethods.asType(source, BigDecimal.class);
        }
        if (ClassHelper.isBigIntegerType(target)) {
            return DefaultGroovyMethods.asType(source, BigInteger.class);
        }
        throw new IllegalArgumentException("Unsupported conversion: " + target.getText());
    }

    private static Consumer<WriterController> classgenCallback(final Expression source) {
        return (source instanceof TemporaryVariableExpression ? ((TemporaryVariableExpression) source)::remove : wc -> {});
    }
}
