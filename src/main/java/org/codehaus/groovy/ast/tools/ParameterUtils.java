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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.function.BiPredicate;

/**
 * Utility methods for working with method and constructor parameters.
 *
 * <p>Provides predicates and comparison utilities for {@link Parameter} arrays, supporting
 * parameter equality checks, compatibility analysis, and varargs detection. Used extensively
 * in method resolution and signature matching during compilation and reflection.
 *
 * @see Parameter for the parameter node type
 * @see ClassHelper for related type utilities
 */
public class ParameterUtils {

    /**
     * Checks if the last parameter in the array is a varargs parameter (array type).
     *
     * @param parameters the parameter array to check, may be null
     * @return true if the array is non-empty and ends with an array-typed parameter, false otherwise
     * @since 4.0.4
     */
    public static boolean isVargs(final Parameter[] parameters) {
        if (parameters == null || parameters.length == 0) return false;
        return (parameters[parameters.length - 1].getType().isArray());
    }

    /**
     * Checks if two parameter arrays have equal types (exact type matching).
     *
     * @param a the first parameter array, may be null
     * @param b the second parameter array, may be null
     * @return true if both arrays have the same length and parameter types match exactly, false otherwise
     * @since 2.5.0
     * @see #parametersEqualWithWrapperType(Parameter[], Parameter[])
     */
    public static boolean parametersEqual(final Parameter[] a, final Parameter[] b) {
        return parametersEqual(a, b, false);
    }

    /**
     * Checks if two parameter arrays have equal types, treating wrapper and primitive types as equal.
     *
     * <p>For example, {@code int.class} and {@code Integer.class} are considered equal.
     *
     * @param a the first parameter array, may be null
     * @param b the second parameter array, may be null
     * @return true if both arrays have the same length and parameter types match (with wrapper equivalence), false otherwise
     * @since 3.0.0
     * @see #parametersEqual(Parameter[], Parameter[])
     */
    public static boolean parametersEqualWithWrapperType(final Parameter[] a, final Parameter[] b) {
        return parametersEqual(a, b, true);
    }

    /**
     * Checks if source parameters are compatible with target parameters using type assignability.
     * Each parameter type in source must be assignable to the corresponding target parameter type.
     *
     * @param source the source parameter array (may be null)
     * @param target the target parameter array (may be null)
     * @return true if both arrays have the same length and all source types are assignable to target types, false otherwise
     * @since 3.0.0
     * @see StaticTypeCheckingSupport#isAssignableTo(ClassNode, ClassNode)
     */
    public static boolean parametersCompatible(final Parameter[] source, final Parameter[] target) {
        return parametersMatch(source, target, StaticTypeCheckingSupport::isAssignableTo);
    }

    //--------------------------------------------------------------------------

    private static boolean parametersEqual(final Parameter[] a, final Parameter[] b, final boolean wrapType) {
        return parametersMatch(a, b, (aType, bType) -> {
            if (wrapType) {
                aType = ClassHelper.getWrapper(aType);
                bType = ClassHelper.getWrapper(bType);
            }
            return aType.equals(bType);
        });
    }

    private static boolean parametersMatch(final Parameter[] a, final Parameter[] b, final BiPredicate<ClassNode, ClassNode> typeChecker) {
        if (a.length == b.length) {
            for (int i = 0, n = a.length; i < n; i += 1) {
                ClassNode aType = a[i].getType();
                ClassNode bType = b[i].getType();

                if (!typeChecker.test(aType, bType)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
