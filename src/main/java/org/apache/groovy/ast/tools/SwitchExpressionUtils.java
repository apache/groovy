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
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;

import java.util.List;

/**
 * Shared classification of switch-expression labels for static compilation.
 * Intrinsic dispatch ({@code tableswitch} / {@code lookupswitch} / enum name)
 * is used only when every case label is a compile-time constant of a type
 * {@code javac} would switch on. Mixed labels fall back to {@code isCase}.
 *
 * @since 6.0.0
 */
public final class SwitchExpressionUtils {

    private SwitchExpressionUtils() {
    }

    /**
     * True when the selector type and every case label permit tableswitch,
     * lookupswitch, or enum-name dispatch. An empty case list, a {@code null}
     * label, or a single non-constant label (a range, a variable, …) makes
     * the whole switch an {@code isCase} switch.
     *
     * @param selectorType the inferred type of the selector
     * @param caseStatements the case arms
     * @return {@code true} if codegen can emit an intrinsic switch
     */
    public static boolean isOptimizedSwitch(final ClassNode selectorType, final List<CaseStatement> caseStatements) {
        if (selectorType == null || caseStatements == null || caseStatements.isEmpty()) {
            return false;
        }
        return isOptimizedIntSwitch(selectorType, caseStatements)
                || isOptimizedStringSwitch(selectorType, caseStatements)
                || isOptimizedEnumSwitch(selectorType, caseStatements);
    }

    /**
     * True when the selector is an integral primitive or wrapper and every
     * label is an int-family constant.
     *
     * @param selectorType the inferred type of the selector
     * @param caseStatements the case arms
     * @return {@code true} if int tableswitch / lookupswitch applies
     */
    public static boolean isOptimizedIntSwitch(final ClassNode selectorType, final List<CaseStatement> caseStatements) {
        if (!isIntegralSelector(selectorType) || caseStatements == null || caseStatements.isEmpty()) {
            return false;
        }
        for (CaseStatement caseStatement : caseStatements) {
            if (intConstant(caseStatement.getExpression()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * True when the selector is {@link String} and every label is a string constant.
     *
     * @param selectorType the inferred type of the selector
     * @param caseStatements the case arms
     * @return {@code true} if string lookupswitch applies
     */
    public static boolean isOptimizedStringSwitch(final ClassNode selectorType, final List<CaseStatement> caseStatements) {
        if (selectorType == null || !ClassHelper.isStringType(selectorType)
                || caseStatements == null || caseStatements.isEmpty()) {
            return false;
        }
        for (CaseStatement caseStatement : caseStatements) {
            if (stringConstant(caseStatement.getExpression()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * True when the selector is an enum and every label names a constant of that enum.
     *
     * @param selectorType the inferred type of the selector
     * @param caseStatements the case arms
     * @return {@code true} if enum-name dispatch applies
     */
    public static boolean isOptimizedEnumSwitch(final ClassNode selectorType, final List<CaseStatement> caseStatements) {
        ClassNode enumType = unwrapEnumType(selectorType);
        if (enumType == null || !enumType.isEnum() || caseStatements == null || caseStatements.isEmpty()) {
            return false;
        }
        for (CaseStatement caseStatement : caseStatements) {
            if (enumConstantName(caseStatement.getExpression(), enumType) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * True for {@code byte}, {@code short}, {@code char}, {@code int} and their wrappers.
     *
     * @param type the selector type
     * @return {@code true} if the type can feed a JVM int switch
     */
    public static boolean isIntegralSelector(final ClassNode type) {
        return isIntegralType(type) || isIntegralWrapper(type);
    }

    /**
     * True for the integral primitive types {@code javac} switches on.
     *
     * @param type the type to test
     * @return {@code true} if the type is {@code int}, {@code byte}, {@code short}, or {@code char}
     */
    public static boolean isIntegralType(final ClassNode type) {
        return type != null && (ClassHelper.isPrimitiveInt(type) || ClassHelper.isPrimitiveByte(type)
                || ClassHelper.isPrimitiveShort(type) || ClassHelper.isPrimitiveChar(type));
    }

    /**
     * True for the wrappers of the integral primitive types {@code javac} switches on.
     *
     * @param type the type to test
     * @return {@code true} if the type is {@code Integer}, {@code Byte}, {@code Short}, or {@code Character}
     */
    public static boolean isIntegralWrapper(final ClassNode type) {
        return type != null && (ClassHelper.isWrapperInteger(type) || ClassHelper.isWrapperByte(type)
                || ClassHelper.isWrapperShort(type) || ClassHelper.isWrapperCharacter(type));
    }

    /**
     * Extracts an int-family constant from a case label, or {@code null}.
     *
     * @param expression the case label
     * @return the constant int value, or {@code null} if the label is not an int-family constant
     */
    public static Integer intConstant(final Expression expression) {
        if (!(expression instanceof ConstantExpression constant)) return null;
        Object value = constant.getValue();
        if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
            return ((Number) value).intValue();
        }
        if (value instanceof Character) {
            return (int) (Character) value;
        }
        return null;
    }

    /**
     * Extracts a string constant from a case label, or {@code null}.
     *
     * @param expression the case label
     * @return the string value, or {@code null} if the label is not a string constant
     */
    public static String stringConstant(final Expression expression) {
        if (expression instanceof ConstantExpression constant && constant.getValue() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Returns the enum type behind {@code type}, or {@code null} if it is not an enum.
     *
     * @param type the selector type
     * @return the enum {@link ClassNode}, or {@code null}
     */
    public static ClassNode unwrapEnumType(final ClassNode type) {
        if (type == null) return null;
        if (type.isEnum()) return type;
        return type.redirect().isEnum() ? type.redirect() : null;
    }

    /**
     * Extracts the enum constant name from a case label of {@code enumType}, or {@code null}.
     * Accepts {@code EnumType.NAME}, an unqualified name that resolves to that constant,
     * or a constant expression holding the enum value.
     *
     * @param expression the case label
     * @param enumType the selector enum type
     * @return the constant name, or {@code null} if the label is not a constant of {@code enumType}
     */
    public static String enumConstantName(final Expression expression, final ClassNode enumType) {
        if (expression instanceof PropertyExpression property
                && property.getObjectExpression() instanceof ClassExpression classExpression
                && classExpression.getType().equals(enumType)
                && property.getProperty() instanceof ConstantExpression name) {
            return name.getText();
        }
        if (expression instanceof VariableExpression variable) {
            var accessed = variable.getAccessedVariable();
            if (accessed instanceof FieldNode field && field.isEnum()
                    && (field.getDeclaringClass() == null || field.getDeclaringClass().equals(enumType))) {
                return field.getName();
            }
            if (accessed == null || accessed instanceof DynamicVariable) {
                // a real local or parameter named like a constant is not a constant label
                FieldNode field = enumType.getField(variable.getName());
                if (field != null && field.isEnum()) {
                    return variable.getName();
                }
            }
        }
        if (expression instanceof ConstantExpression constant && enumType.isResolved()
                && constant.getValue() instanceof Enum<?> e) {
            Class<?> declaring = e.getDeclaringClass();
            Class<?> enumClass = enumType.getTypeClass();
            if (declaring == enumClass || enumClass.isAssignableFrom(declaring)) {
                return e.name();
            }
        }
        return null;
    }
}
