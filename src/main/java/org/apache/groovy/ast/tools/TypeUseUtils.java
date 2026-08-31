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

import groovy.transform.Internal;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;

/**
 * Classification of a {@link ClassNode} as a type <em>use</em> (JLS 4.5, 4.7,
 * 4.8). Distinct from a generic class <em>declaration</em>, whose
 * {@link ClassNode#getGenericsTypes()} are the formals.
 *
 * @since 6.0.0
 */
@Internal
public final class TypeUseUtils {

    private TypeUseUtils() {
    }

    /**
     * True when {@code type} is a parameterized usage ({@code Cell<String>}), as
     * opposed to the generic type name ({@code Cell}) or the class declaration
     * node (whose {@link ClassNode#getGenericsTypes()} are the formals).
     */
    public static boolean isParameterizedTypeUsage(final ClassNode type) {
        if (type == null || type.isGenericsPlaceHolder()) return false;
        GenericsType[] gt = type.getGenericsTypes();
        return gt != null && gt.length > 0 && type.isRedirectNode();
    }

    /**
     * True when {@code type} or any enclosing rare type is a parameterized
     * usage. Used for class literals (JLS 15.8.2): {@code Outer<String>.Inner.class}.
     * Declaration formals on {@code Map} must not make {@code Map.Entry} a
     * class-literal error.
     */
    public static boolean isParameterizedTypeUsageIncludingEnclosing(final ClassNode type) {
        if (type == null) return false;
        if (isParameterizedTypeUsage(type)) return true;
        ClassNode oc = type.getOuterClassType();
        return oc != null && (isParameterizedTypeUsage(oc) || isParameterizedTypeUsageIncludingEnclosing(oc));
    }

    /**
     * True when {@code type} carries type arguments, including wildcards
     * ({@code Outer<?>}). Raw names ({@code Outer}) are not parameterized.
     * Unlike {@link #isParameterizedTypeUsage(ClassNode)}, this does not
     * require a redirect node: an enclosing type on a nested use may still
     * be the parser's unresolved node.
     */
    public static boolean isParameterizedEnclosingType(final ClassNode type) {
        if (type == null || type.isGenericsPlaceHolder()) return false;
        GenericsType[] gt = type.getGenericsTypes();
        return gt != null && gt.length > 0;
    }

    /**
     * True when {@code type} is a raw use of a generic declaration ({@code Outer}
     * for {@code class Outer<T>}).
     */
    public static boolean isRawGenericType(final ClassNode type) {
        return type != null
                && type.getGenericsTypes() == null
                && type.redirect().getGenericsTypes() != null;
    }

    /**
     * JLS 4.7: types available in full at run time. Used for array creation
     * (JLS 15.10.1) and {@code instanceof} (JLS 15.20.2).
     */
    public static boolean isReifiable(ClassNode type) {
        if (type == null) return false;
        while (type.isArray()) {
            type = type.getComponentType();
        }
        if (type.isGenericsPlaceHolder()) return false;
        if (isPrimitiveType(type)) return true;
        GenericsType[] gt = type.getGenericsTypes();
        if (gt != null) {
            if (gt.length == 0) return false;
            for (GenericsType t : gt) {
                if (!isUnboundedWildcard(t)) return false;
            }
        }
        ClassNode oc = type.getOuterClassType();
        return oc == null || isReifiable(oc);
    }

    /**
     * Nested interfaces and enums are implicitly static; otherwise the
     * {@code ACC_STATIC} bit on the declaration is authoritative.
     */
    public static boolean isStaticMemberType(final ClassNode type) {
        ClassNode declared = type.redirect();
        return declared.isStatic()
                || declared.isInterface()
                || declared.isEnum();
    }

    /**
     * True when a type argument of {@code type} or an enclosing rare type is a
     * wildcard.
     */
    public static boolean hasWildcardTypeArgument(final ClassNode type) {
        if (type == null) return false;
        GenericsType[] gt = type.getGenericsTypes();
        if (gt != null) {
            for (GenericsType t : gt) {
                if (t.isWildcard()) return true;
            }
        }
        ClassNode oc = type.getOuterClassType();
        return oc != null && hasWildcardTypeArgument(oc);
    }

    /**
     * Source-like display of a type use, including rare enclosing arguments
     * ({@code Outer<String>.Inner}) and array brackets.
     */
    public static String describeTypeUse(final ClassNode type) {
        if (type == null) return "null";
        if (type.isArray()) {
            return describeTypeUse(type.getComponentType()) + "[]";
        }
        if (type.isGenericsPlaceHolder()) {
            return type.getUnresolvedName();
        }
        StringBuilder sb = new StringBuilder();
        ClassNode outer = type.getOuterClassType();
        if (outer != null) {
            sb.append(describeTypeUse(outer)).append('.');
            String name = type.getName();
            int sep = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
            sb.append(sep < 0 ? name : name.substring(sep + 1));
        } else {
            sb.append(type.getNameWithoutPackage());
        }
        GenericsType[] generics = type.getGenericsTypes();
        if (generics != null && generics.length > 0 && !type.isGenericsPlaceHolder()) {
            sb.append('<');
            for (int i = 0; i < generics.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(generics[i]);
            }
            sb.append('>');
        }
        return sb.toString();
    }

    /**
     * Indicates whether the wildcard has no explicit bounds.
     */
    private static boolean isUnboundedWildcard(final GenericsType gt) {
        if (gt.isWildcard() && gt.getLowerBound() == null) {
            ClassNode[] upperBounds = gt.getUpperBounds();
            return (upperBounds == null || upperBounds.length == 0 || (upperBounds.length == 1
                    && isObjectType(upperBounds[0]) && !upperBounds[0].isGenericsPlaceHolder()));
        }
        return false;
    }
}
