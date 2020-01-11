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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.codehaus.groovy.syntax.Types.BITWISE_AND;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.MINUS;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.POWER;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;

public class ExpressionUtils {

    // NOTE: values are sorted in ascending order
    private static final int[] HANDLED_TYPES = IntStream.of(
            PLUS, MINUS, MULTIPLY, DIVIDE, POWER,
            LEFT_SHIFT, RIGHT_SHIFT, RIGHT_SHIFT_UNSIGNED,
            BITWISE_OR, BITWISE_AND, BITWISE_XOR).sorted().toArray();

    private ExpressionUtils() {
    }

    /**
     * Turns expressions of the form ConstantExpression(40) + ConstantExpression(2)
     * into the simplified ConstantExpression(42) at compile time.
     *
     * @param be the binary expression
     * @param targetType the type of the result
     * @return the transformed expression or the original if no transformation was performed
     */
    public static ConstantExpression transformBinaryConstantExpression(final BinaryExpression be, final ClassNode targetType) {
        ClassNode wrapperType = ClassHelper.getWrapper(targetType);
        if (isTypeOrArrayOfType(targetType, ClassHelper.STRING_TYPE, false)) {
            if (be.getOperation().getType() == PLUS) {
                Expression left = transformInlineConstants(be.getLeftExpression(), targetType);
                Expression right = transformInlineConstants(be.getRightExpression(), targetType);
                if (left instanceof ConstantExpression && right instanceof ConstantExpression) {
                    return configure(be, new ConstantExpression((String) ((ConstantExpression) left).getValue() + ((ConstantExpression) right).getValue()));
                }
            }
        } else if (isNumberOrArrayOfNumber(wrapperType, false)) {
            int type = be.getOperation().getType();
            if (Arrays.binarySearch(HANDLED_TYPES, type) >= 0) {
                boolean isShift = (type >= LEFT_SHIFT && type <= RIGHT_SHIFT_UNSIGNED);
                Expression leftX = transformInlineConstants(be.getLeftExpression(), targetType);
                Expression rightX = transformInlineConstants(be.getRightExpression(), isShift ? ClassHelper.int_TYPE : targetType);
                if (leftX instanceof ConstantExpression && rightX instanceof ConstantExpression) {
                    Number left = safeNumber((ConstantExpression) leftX);
                    Number right = safeNumber((ConstantExpression) rightX);
                    if (left == null || right == null) return null;
                    Number result = null;
                    switch(type) {
                        case PLUS:
                            result = NumberMath.add(left, right);
                            break;
                        case MINUS:
                            result = NumberMath.subtract(left, right);
                            break;
                        case MULTIPLY:
                            result = NumberMath.multiply(left, right);
                            break;
                        case DIVIDE:
                            result = NumberMath.divide(left, right);
                            break;
                        case LEFT_SHIFT:
                            result = NumberMath.leftShift(left, right);
                            break;
                        case RIGHT_SHIFT:
                            result = NumberMath.rightShift(left, right);
                            break;
                        case RIGHT_SHIFT_UNSIGNED:
                            result = NumberMath.rightShiftUnsigned(left, right);
                            break;
                        case BITWISE_AND:
                            result = NumberMath.and(left, right);
                            break;
                        case BITWISE_OR:
                            result = NumberMath.or(left, right);
                            break;
                        case BITWISE_XOR:
                            result = NumberMath.xor(left, right);
                            break;
                        case POWER:
                            result = DefaultGroovyMethods.power(left, right);
                            break;
                    }
                    if (result != null) {
                        if (ClassHelper.Byte_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.byteValue(), true));
                        }
                        if (ClassHelper.Short_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.shortValue(), true));
                        }
                        if (ClassHelper.Long_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.longValue(), true));
                        }
                        if (ClassHelper.Integer_TYPE.equals(wrapperType) || ClassHelper.Character_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.intValue(), true));
                        }
                        if (ClassHelper.Float_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.floatValue(), true));
                        }
                        if (ClassHelper.Double_TYPE.equals(wrapperType)) {
                            return configure(be, new ConstantExpression(result.doubleValue(), true));
                        }
                        return configure(be, new ConstantExpression(result, true));
                    }
                }
            }
        }
        return null;
    }

    private static Number safeNumber(final ConstantExpression constX) {
        Object value = constX.getValue();
        if (value instanceof Number) return (Number) value;
        return null;
    }

    private static ConstantExpression configure(final Expression origX, final ConstantExpression newX) {
        newX.setSourcePosition(origX);
        return newX;
    }

    /**
     * Determine if a type matches another type (or array thereof).
     *
     * @param targetType the candidate type
     * @param type the type we are checking against
     * @param recurse true if we can have multi-dimension arrays; should be false for annotation member types
     * @return true if the type equals the targetType or array thereof
     */
    public static boolean isTypeOrArrayOfType(final ClassNode targetType, final ClassNode type, final boolean recurse) {
        if (targetType == null) return false;
        return type.equals(targetType) ||
                (targetType.isArray() && recurse
                ? isTypeOrArrayOfType(targetType.getComponentType(), type, recurse)
                : type.equals(targetType.getComponentType()));
    }

    /**
     * Determine if a type is derived from Number (or array thereof).
     *
     * @param targetType the candidate type
     * @param recurse true if we can have multi-dimension arrays; should be false for annotation member types
     * @return true if the type equals the targetType or array thereof
     */
    public static boolean isNumberOrArrayOfNumber(final ClassNode targetType, final boolean recurse) {
        if (targetType == null) return false;
        return targetType.isDerivedFrom(ClassHelper.Number_TYPE) ||
                (targetType.isArray() && recurse
                ? isNumberOrArrayOfNumber(targetType.getComponentType(), recurse)
                : targetType.isArray() && targetType.getComponentType().isDerivedFrom(ClassHelper.Number_TYPE));
    }

    /**
     * Converts simple expressions of constants into pre-evaluated simple constants.
     * Handles:
     * <ul>
     *     <li>Property expressions - referencing constants</li>
     *     <li>Simple binary expressions - String concatenation and numeric +, -, /, *</li>
     *     <li>List expressions - list of constants</li>
     *     <li>Variable expressions - referencing constants</li>
     * </ul>
     * @param exp the original expression
     * @param attrType the type that the final constant should be
     * @return the transformed type or the original if no transformation was possible
     */
    public static Expression transformInlineConstants(final Expression exp, final ClassNode attrType) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            if (pe.getObjectExpression() instanceof ClassExpression) {
                ClassExpression ce = (ClassExpression) pe.getObjectExpression();
                ClassNode type = ce.getType();
                if (type.isEnum() || !(type.isResolved() || type.isPrimaryClassNode()))
                    return exp;

                if (type.isPrimaryClassNode()) {
                    FieldNode fn = type.redirect().getField(pe.getPropertyAsString());
                    if (fn != null && fn.isStatic() && fn.isFinal()) {
                        Expression ce2 = transformInlineConstants(fn.getInitialValueExpression(), attrType);
                        if (ce2 != null) {
                            return ce2;
                        }
                    }
                } else {
                    try {
                        Field field = type.redirect().getTypeClass().getField(pe.getPropertyAsString());
                        if (field != null && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                            ConstantExpression ce3 = new ConstantExpression(field.get(null), true);
                            configure(exp, ce3);
                            return ce3;
                        }
                    } catch(Exception e) {
                        // ignore, leave property expression in place and we'll report later
                    }
                }
            }
        } else if (exp instanceof BinaryExpression) {
            ConstantExpression ce = transformBinaryConstantExpression((BinaryExpression) exp, attrType);
            if (ce != null) {
                return ce;
            }
        } else if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode fn = (FieldNode) ve.getAccessedVariable();
                if (fn.isStatic() && fn.isFinal()) {
                    Expression ce = transformInlineConstants(fn.getInitialValueExpression(), attrType);
                    if (ce != null) {
                        return ce;
                    }
                }
            }
        } else if (exp instanceof ListExpression) {
            return transformListOfConstants((ListExpression) exp, attrType);
        }
        return exp;
    }

    /**
     * Given a list of constants, transform each item in the list.
     *
     * @param origList the list to transform
     * @param attrType the target type
     * @return the transformed list or the original if nothing was changed
     */
    public static Expression transformListOfConstants(final ListExpression origList, final ClassNode attrType) {
        ListExpression newList = new ListExpression();
        boolean changed = false;
        for (Expression e : origList.getExpressions()) {
            try {
                Expression transformed = transformInlineConstants(e, attrType);
                newList.addExpression(transformed);
                if (transformed != e) changed = true;
            } catch(Exception ignored) {
                newList.addExpression(e);
            }
        }
        if (changed) {
            newList.setSourcePosition(origList);
            return newList;
        }
        return origList;
    }

    /**
     * The attribute values of annotations must be primitive, String or Enum constants.
     * In various places, such constants can be seen during type resolution but won't be
     * readily accessible in later phases, e.g. they might be embedded into constructor code.
     * This method transforms constants that would appear in annotations early so they aren't lost.
     * Subsequent processing determines whether they are valid, this method simply retains
     * the constant value as a constant expression.
     *
     * @param exp the original expression
     * @return the converted expression
     */
    public static Expression transformInlineConstants(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            if (pe.getObjectExpression() instanceof ClassExpression) {
                ClassExpression ce = (ClassExpression) pe.getObjectExpression();
                ClassNode type = ce.getType();
                FieldNode field = ClassNodeUtils.getField(type, pe.getPropertyAsString());
                if (type.isEnum() && field != null && field.isEnum()) return exp;
                Expression constant = findConstant(field);
                if (constant != null) return constant;
            }
        } else if (exp instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) exp;
            be.setLeftExpression(transformInlineConstants(be.getLeftExpression()));
            be.setRightExpression(transformInlineConstants(be.getRightExpression()));
            return be;
        } else if (exp instanceof ListExpression) {
            ListExpression origList = (ListExpression) exp;
            ListExpression newList = new ListExpression();
            boolean changed = false;
            for (Expression e : origList.getExpressions()) {
                Expression transformed = transformInlineConstants(e);
                newList.addExpression(transformed);
                if (transformed != e) changed = true;
            }
            if (changed) {
                newList.setSourcePosition(origList);
                return newList;
            }
            return origList;
        }

        return exp;
    }

    private static Expression findConstant(final FieldNode fn) {
        if (fn != null && !fn.isEnum() && fn.isStatic() && fn.isFinal()
                && fn.getInitialValueExpression() instanceof ConstantExpression) {
            return fn.getInitialValueExpression();
        }
        return null;
    }
}
