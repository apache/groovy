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
package org.apache.groovy.contracts.generation;

import org.apache.groovy.ast.tools.ImmutablePropertyUtils;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapEntryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;

/**
 * <p>Central place where code generation for the <tt>old</tt> closure variable
 * takes place.</p>
 */
public class OldVariableGenerationUtility {

    /**
     * Synthetic helper method name used to compute the {@code old} variable map for postconditions.
     */
    public static final String OLD_VARIABLES_METHOD = "$_gc_computeOldVariables";

    /**
     * Creates a synthetic method handling generation of the <tt>old</tt> variable map. If a super class declares
     * the same synthetic method it will be called and the results will be merged.
     *
     * @param classNode which contains postconditions, so an old variable generating method makes sense here.
     */
    public static void addOldVariableMethodNode(final ClassNode classNode) {
        if (classNode.getDeclaredMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null) return;

        final BlockStatement methodBlockStatement = block();

        final MapExpression oldVariablesMap = mapX();

        // create variable assignments for old variables
        for (final FieldNode fieldNode : classNode.getFields()) {
            if (fieldNode.getName().startsWith("$")) continue;

            final ClassNode fieldType = ClassHelper.getWrapper(fieldNode.getType());

            if (isOldSnapshotType(fieldType)) {

                // if a clone classNode is available, the value is cloned
                if (isCloneable(fieldType)) {

                    final MethodCallExpression cloneField = callX(fieldX(fieldNode), "clone");
                    // return null if field is null
                    cloneField.setSafe(true);

                    VariableExpression oldVariable = localVarX("$old$" + fieldNode.getName(), fieldNode.getType());
                    Statement oldVariableAssignment = declS(oldVariable, cloneField);

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(mapEntryX(constX(oldVariable.getName().substring("$old$".length())), oldVariable));

                } else if (ClassHelper.isPrimitiveType(fieldType)
                        || ClassHelper.isNumberType(fieldType)
                        || fieldType.getName().startsWith("java.math")
                        || "groovy.lang.GString".equals(fieldType.getName())
                        || "java.lang.String".equals(fieldType.getName())) {

                    VariableExpression oldVariable = localVarX("$old$" + fieldNode.getName(), fieldNode.getType());
                    Statement oldVariableAssignment = declS(oldVariable, fieldX(fieldNode));

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(mapEntryX(constX(oldVariable.getName().substring("$old$".length())), oldVariable));
                }
            }
        }

        VariableExpression oldVariable = localVarX("old", new ClassNode(Map.class));
        methodBlockStatement.addStatement(declS(oldVariable, oldVariablesMap));
        VariableExpression mergedOldVariables = null;

        // let's ask the super class for old variables...
        if (classNode.getSuperClass() != null && classNode.getSuperClass().getMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null) {
            mergedOldVariables = localVarX("mergedOldVariables", new ClassNode(Map.class));
            methodBlockStatement.addStatement(declS(mergedOldVariables,
                    callX(oldVariable, "plus", args(callSuperX(OLD_VARIABLES_METHOD)))));
        }

        methodBlockStatement.addStatement(returnS(mergedOldVariables != null ? mergedOldVariables : oldVariable));

        final MethodNode preconditionMethodNode = classNode.addMethod(OLD_VARIABLES_METHOD, Opcodes.ACC_PROTECTED, new ClassNode(Map.class), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodBlockStatement);
        preconditionMethodNode.setSynthetic(true);

    }

    /**
     * Returns an expression that snapshots {@code value} (of declared type {@code type}) for storage in
     * the {@code old} map. The snapshot is only a defensive copy when one is needed and possible:
     * <ul>
     *   <li>a value of a known-immutable type (per {@link ImmutablePropertyUtils}: primitives/wrappers,
     *       {@link String}, {@code BigInteger}/{@code BigDecimal}, the {@code java.time.*} types, enums,
     *       {@code @Immutable}/{@code @KnownImmutable} types, ...) is stored by reference, since it cannot
     *       change;</li>
     *   <li>a mutable {@link Cloneable} value is defensively copied with a null-safe {@code clone()}, so an
     *       in-place change in the method body is not also seen through {@code old};</li>
     *   <li>any other value (e.g. a non-cloneable collection or a user type) cannot be generically copied,
     *       so its reference is stored as a best effort.</li>
     * </ul>
     * Reuses the same immutability/clone detection as {@code @Immutable} rather than an ad-hoc list.
     *
     * @param type  the declared type of the value being snapshotted
     * @param value the expression producing the value to snapshot
     * @return the snapshot expression
     */
    public static Expression snapshotExpression(final ClassNode type, final Expression value) {
        if (ImmutablePropertyUtils.isKnownImmutableType(type, List.of())) {
            return value;
        }
        if (ImmutablePropertyUtils.implementsCloneable(type)) {
            final MethodCallExpression clone = callX(value, "clone");
            clone.setSafe(true); // leave a null value as null
            return clone;
        }
        return value;
    }

    /**
     * Reports whether the (wrapper) type participates in {@code old} snapshotting: the value-like JDK
     * types ({@code java.lang} / {@code java.math} / {@code java.util} / {@code java.sql}), {@link String},
     * {@link groovy.lang.GString} and the primitives.
     */
    private static boolean isOldSnapshotType(final ClassNode wrapperType) {
        final String name = wrapperType.getName();
        return name.startsWith("java.lang") // includes java.lang.String
                || ClassHelper.isPrimitiveType(wrapperType)
                || name.startsWith("java.math")
                || name.startsWith("java.util")
                || name.startsWith("java.sql")
                || "groovy.lang.GString".equals(name);
    }

    /**
     * Reports whether the (wrapper) type is {@link Cloneable} and exposes a {@code clone()} method, so its
     * value can be defensively copied for the {@code old} map.
     */
    private static boolean isCloneable(final ClassNode wrapperType) {
        return wrapperType.getMethod("clone", Parameter.EMPTY_ARRAY) != null
                && wrapperType.implementsInterface(ClassHelper.make("java.lang.Cloneable"));
    }
}
