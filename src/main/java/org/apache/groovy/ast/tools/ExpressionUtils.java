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
import org.codehaus.groovy.ast.expr.CastExpression;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor.inferLoopElementType;

public final class ExpressionUtils {

    public static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).isNullExpression();
    }

    public static boolean isThisExpression(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isThisExpression();
    }

    public static boolean isSuperExpression(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isSuperExpression();
    }

    public static boolean isThisOrSuper(final Expression expression) {
        return isThisExpression(expression) || isSuperExpression(expression);
    }

    /**
     * Determines if a type matches another type (or array thereof).
     *
     * @param targetType the candidate type
     * @param type the type we are checking against
     * @param recurse true if we can have multi-dimension arrays; should be false for annotation member types
     * @return true if the type equals the targetType or array thereof
     */
    public static boolean isTypeOrArrayOfType(final ClassNode targetType, final ClassNode type, final boolean recurse) {
        if (targetType == null) return false;
        if (type.equals(targetType)) return true;
        return (targetType.isArray() && recurse ? isTypeOrArrayOfType(targetType.getComponentType(), type, recurse) : type.equals(targetType.getComponentType()));
    }

    /**
     * Determines if a type is derived from Number (or array thereof).
     *
     * @param targetType the candidate type
     * @param recurse true if we can have multi-dimension arrays; should be false for annotation member types
     * @return true if the type equals the targetType or array thereof
     */
    public static boolean isNumberOrArrayOfNumber(final ClassNode targetType, final boolean recurse) {
        if (targetType == null) return false;
        if (targetType.isDerivedFrom(ClassHelper.Number_TYPE)) return true;
        return (targetType.isArray() && recurse ? isNumberOrArrayOfNumber(targetType.getComponentType(), recurse) : targetType.isArray() && targetType.getComponentType().isDerivedFrom(ClassHelper.Number_TYPE));
    }

    //--------------------------------------------------------------------------

    private static final int[] HANDLED_TYPES = {
            PLUS, MINUS, MULTIPLY, DIVIDE, POWER,
            BITWISE_OR, BITWISE_AND, BITWISE_XOR,
            LEFT_SHIFT, RIGHT_SHIFT, RIGHT_SHIFT_UNSIGNED
    };
    static {
        Arrays.sort(HANDLED_TYPES);
    }

    /**
     * Converts expressions like ConstantExpression(40) + ConstantExpression(2)
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
                    Object leftV = ((ConstantExpression) left).getValue();
                    if (leftV == null) leftV = "null";
                    if (leftV instanceof String) {
                        return configure(be, new ConstantExpression(((String)leftV) + ((ConstantExpression) right).getValue()));
                    }
                }
            }
        } else if (isNumberOrArrayOfNumber(wrapperType, false)) {
            int type = be.getOperation().getType();
            if (Arrays.binarySearch(HANDLED_TYPES, type) >= 0) {
                Expression leftX = be.getLeftExpression();
                if (!(leftX instanceof ConstantExpression)) {
                    leftX = transformInlineConstants(leftX, targetType);
                }
                Expression rightX = be.getRightExpression();
                if (!(rightX instanceof ConstantExpression)) {
                    boolean isShift = (type >= LEFT_SHIFT && type <= RIGHT_SHIFT_UNSIGNED); // GROOVY-9336
                    rightX = transformInlineConstants(rightX, isShift ? ClassHelper.int_TYPE : targetType);
                }
                if (leftX instanceof ConstantExpression && rightX instanceof ConstantExpression) {
                    Number left  = safeNumber((ConstantExpression) leftX);
                    Number right = safeNumber((ConstantExpression) rightX);
                    if (left == null || right == null) return null;
                    Number result = null;
                    switch (type) {
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
                        if (ClassHelper.isWrapperInteger(wrapperType)) {
                            return configure(be, new ConstantExpression(result.intValue(), true));
                        }
                        if (ClassHelper.isWrapperByte(wrapperType)) {
                            return configure(be, new ConstantExpression(result.byteValue(), true));
                        }
                        if (ClassHelper.isWrapperLong(wrapperType)) {
                            return configure(be, new ConstantExpression(result.longValue(), true));
                        }
                        if (ClassHelper.isWrapperShort(wrapperType)) {
                            return configure(be, new ConstantExpression(result.shortValue(), true));
                        }
                        if (ClassHelper.isWrapperFloat(wrapperType)) {
                            return configure(be, new ConstantExpression(result.floatValue(), true));
                        }
                        if (ClassHelper.isWrapperDouble(wrapperType)) {
                            return configure(be, new ConstantExpression(result.doubleValue(), true));
                        }
                        if (ClassHelper.isWrapperCharacter(wrapperType)) {
                            return configure(be, new ConstantExpression((char) result.intValue(), true));
                        }
                        return configure(be, new ConstantExpression(result, true));
                    }
                }
            }
        }
        return null;
    }

    /**
     * Transforms constants that would appear in annotations so they aren't lost.
     * Subsequent processing determines whether they are valid, this retains the
     * constant value as a constant expression.
     * <p>
     * The attribute values of annotations must be primitive, string, annotation
     * or enumeration constants. In various places such constants can be seen
     * during type resolution but won't be readily accessible in later phases,
     * e.g. they might be embedded into constructor code.
     *
     * @param exp the original expression
     * @return original or transformed expression
     */
    public static Expression transformInlineConstants(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            if (pe.getObjectExpression() instanceof ClassExpression) {
                ClassNode clazz = pe.getObjectExpression().getType();
                FieldNode field = ClassNodeUtils.getField(clazz, pe.getPropertyAsString());
                if (field != null && !field.isEnum() && field.isFinal() && field.isStatic()) {
                    Expression value = transformInlineConstants(field.getInitialValueExpression(), field.getType()); // GROOVY-10750, GROOVY-10068
                    if (value instanceof ConstantExpression) {
                        return configure(exp, new ConstantExpression(((ConstantExpression) value).getValue()));
                    }
                }
            }
        } else if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode field = (FieldNode) ve.getAccessedVariable();
                if (!field.isEnum() && field.isFinal() && field.isStatic()) {
                    Expression value = transformInlineConstants(field.getInitialValueExpression(), field.getType()); // GROOVY-11207, GROOVY-10068
                    if (value instanceof ConstantExpression) {
                        return configure(exp, new ConstantExpression(((ConstantExpression) value).getValue()));
                    }
                }
            }
        } else if (exp instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) exp;
            Expression lhs = transformInlineConstants(be.getLeftExpression());
            Expression rhs = transformInlineConstants(be.getRightExpression());
            if (be.getOperation().getType() == PLUS // GROOVY-9855: inline string concat
                    && lhs instanceof ConstantExpression && rhs instanceof ConstantExpression
                    && ClassHelper.isStringType(lhs.getType()) && ClassHelper.isStringType(rhs.getType())) {
                return configure(exp, new ConstantExpression(lhs.getText() + rhs.getText()));
            }
            be.setLeftExpression(lhs);
            be.setRightExpression(rhs);

        } else if (exp instanceof ListExpression) {
            List<Expression> list = ((ListExpression) exp).getExpressions();
            for (ListIterator<Expression> it = list.listIterator(); it.hasNext();) {
                Expression e = transformInlineConstants(it.next());
                it.set(e);
            }
        }

        return exp;
    }

    /**
     * Converts simple expressions of constants into pre-evaluated simple constants.
     * Handles:
     * <ul>
     *     <li>Property expressions - referencing constants</li>
     *     <li>Variable expressions - referencing constants</li>
     *     <li>Typecast expressions - referencing constants</li>
     *     <li>Binary expressions - string concatenation and numeric +, -, /, *</li>
     *     <li>List expressions - list of constants</li>
     * </ul>
     * @param exp the original expression
     * @param attrType the type that the final constant should be
     * @return the transformed type or the original if no transformation was possible
     */
    public static Expression transformInlineConstants(final Expression exp, final ClassNode attrType) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            ClassNode type = pe.getObjectExpression().getType();
            if (pe.getObjectExpression() instanceof ClassExpression && !type.isEnum()) {
                if (type.isPrimaryClassNode()) {
                    FieldNode fn = type.getField(pe.getPropertyAsString());
                    if (fn != null && fn.isStatic() && fn.isFinal()) {
                        Expression e = transformInlineConstants(fn.getInitialValueExpression(), attrType);
                        if (e != null) {
                            return e;
                        }
                    }
                } else if (type.isResolved()) {
                    try {
                        Field field = type.redirect().getTypeClass().getField(pe.getPropertyAsString());
                        if (field != null && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                            ConstantExpression ce = new ConstantExpression(field.get(null), true);
                            configure(exp, ce);
                            return ce;
                        }
                    } catch (Exception | LinkageError e) {
                        // ignore, leave property expression in place and we'll report later
                    }
                }
            }
        } else if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode fn = (FieldNode) ve.getAccessedVariable();
                if (fn.isStatic() && fn.isFinal()) {
                    Expression e = transformInlineConstants(fn.getInitialValueExpression(), attrType);
                    if (e != null) {
                        return e;
                    }
                }
            }
        } else if (exp instanceof ConstantExpression) {
            Object value = ((ConstantExpression) exp).getValue();
            ClassNode targetType = ClassHelper.getWrapper(attrType);
            if (value instanceof Integer) {
                Integer integer = (Integer) value;
                if (ClassHelper.isWrapperByte(targetType)) {
                    return configure(exp, new ConstantExpression(integer.byteValue(), true));
                }
                if (ClassHelper.isWrapperLong(targetType)) {
                    return configure(exp, new ConstantExpression(integer.longValue(), true));
                }
                if (ClassHelper.isWrapperShort(targetType)) {
                    return configure(exp, new ConstantExpression(integer.shortValue(), true));
                }
                if (ClassHelper.isWrapperFloat(targetType)) {
                    return configure(exp, new ConstantExpression(integer.floatValue(), true));
                }
                if (ClassHelper.isWrapperDouble(targetType)) {
                    return configure(exp, new ConstantExpression(integer.doubleValue(), true));
                }
                if (ClassHelper.isWrapperCharacter(targetType)) {
                    return configure(exp, new ConstantExpression((char) integer.intValue(), true));
                }
            } else if (value instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) value;
                if (ClassHelper.isWrapperFloat(targetType)) {
                    return configure(exp, new ConstantExpression(decimal.floatValue(), true));
                }
                if (ClassHelper.isWrapperDouble(targetType)) {
                    return configure(exp, new ConstantExpression(decimal.doubleValue(), true));
                }
            } else if (value instanceof String) {
                String string = (String) value;
                if (ClassHelper.isWrapperCharacter(targetType) && string.length() == 1) {
                    return configure(exp, new ConstantExpression(string.charAt(0), true));
                }
            }
        } else if (exp instanceof CastExpression) {
            Expression e = transformInlineConstants(((CastExpression) exp).getExpression(), exp.getType());
            if (ClassHelper.getWrapper(e.getType()).isDerivedFrom(ClassHelper.getWrapper(attrType))) {
                return e;
            }
        } else if (exp instanceof BinaryExpression) {
            ConstantExpression ce = transformBinaryConstantExpression((BinaryExpression) exp, attrType);
            if (ce != null) {
                return ce;
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
    public static Expression transformListOfConstants(final ListExpression origList, ClassNode attrType) {
        ListExpression newList = new ListExpression();
        attrType = inferLoopElementType(attrType);
        boolean changed = false;
        for (Expression e : origList.getExpressions()) {
            try {
                Expression transformed = transformInlineConstants(e, attrType);
                newList.addExpression(transformed);
                if (transformed != e) changed = true;
            } catch (Exception ignored) {
                newList.addExpression(e);
            }
        }
        if (changed) {
            newList.setSourcePosition(origList);
            return newList;
        }
        return origList;
    }

    //--------------------------------------------------------------------------

    private static ConstantExpression configure(final Expression origX, final ConstantExpression newX) {
        newX.setSourcePosition(origX);
        return newX;
    }

    private static Number safeNumber(final ConstantExpression constX) {
        Object value = constX.getValue();
        return value instanceof Number ? (Number) value : null;
    }

    private ExpressionUtils() {
        assert false;
    }
}
