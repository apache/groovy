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
package org.apache.groovy.groovyfx.beans;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.tools.GenericsUtils;

import java.util.Set;

/**
 * Maps the declared type of a Groovy property to the JavaFX property type
 * that backs it and the concrete {@code Simple*Property} that implements it.
 * <p>
 * The mapping is by type name so that it gives the same answer before and
 * after type resolution; the stubber runs before names are resolved.
 */
final class FXPropertyTypes {

    private static final String PROPERTY_PACKAGE = "javafx.beans.property.";
    private static final Set<String> INTEGER_NAMES = Set.of("int", "Integer", "short", "Short", "byte", "Byte", "char", "Character");

    private FXPropertyTypes() {
    }

    /**
     * The JavaFX property interface and implementation for one Groovy property.
     */
    static final class Mapping {
        final ClassNode propertyType;
        final ClassNode implementationType;

        private Mapping(ClassNode propertyType, ClassNode implementationType) {
            this.propertyType = propertyType;
            this.implementationType = implementationType;
        }

        /**
         * Returns a mapping whose class nodes are backed by the real classes
         * when the loader can see them, so that statically compiled callers
         * can look up their methods. Falls back to the by-name nodes otherwise.
         */
        Mapping resolve(ClassLoader loader) {
            return new Mapping(resolve(propertyType, loader), resolve(implementationType, loader));
        }

        private static ClassNode resolve(ClassNode node, ClassLoader loader) {
            if (node.isResolved()) {
                return node;
            }
            try {
                ClassNode resolved = ClassHelper.make(Class.forName(node.getName(), false, loader));
                GenericsType[] generics = node.getGenericsTypes();
                return generics == null ? resolved : GenericsUtils.makeClassSafeWithGenerics(resolved, generics);
            } catch (ClassNotFoundException | LinkageError e) {
                return node;
            }
        }
    }

    static Mapping forType(ClassNode type) {
        String simpleName = simpleName(type);
        if (ClassHelper.isPrimitiveBoolean(type) || "boolean".equals(simpleName) || "Boolean".equals(simpleName)) {
            return plain("Boolean");
        }
        if (INTEGER_NAMES.contains(simpleName)) {
            return plain("Integer");
        }
        if ("long".equals(simpleName) || "Long".equals(simpleName)) {
            return plain("Long");
        }
        if ("double".equals(simpleName) || "Double".equals(simpleName)) {
            return plain("Double");
        }
        if ("float".equals(simpleName) || "Float".equals(simpleName)) {
            return plain("Float");
        }
        if ("String".equals(simpleName)) {
            return plain("String");
        }
        if ("ObservableList".equals(simpleName)) {
            return generic("List", type.getGenericsTypes());
        }
        if ("ObservableMap".equals(simpleName)) {
            return generic("Map", type.getGenericsTypes());
        }
        if ("ObservableSet".equals(simpleName)) {
            return generic("Set", type.getGenericsTypes());
        }
        return generic("Object", new GenericsType[]{new GenericsType(ClassHelper.getWrapper(type))});
    }

    /**
     * Whether the declared type is itself a JavaFX property, which must not be wrapped again.
     */
    static boolean isJavaFXProperty(ClassNode type) {
        String name = type.getName();
        return name.startsWith(PROPERTY_PACKAGE) && (name.endsWith("Property") || name.endsWith("Wrapper"));
    }

    private static String simpleName(ClassNode type) {
        String name = type.getName();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(dot + 1);
    }

    private static Mapping plain(String kind) {
        return new Mapping(make(kind + "Property"), make("Simple" + kind + "Property"));
    }

    private static Mapping generic(String kind, GenericsType[] generics) {
        ClassNode propertyType = make(kind + "Property");
        ClassNode implementationType = make("Simple" + kind + "Property");
        if (generics != null && generics.length > 0) {
            propertyType = GenericsUtils.makeClassSafeWithGenerics(propertyType, generics);
            implementationType = GenericsUtils.makeClassSafeWithGenerics(implementationType, generics);
        }
        return new Mapping(propertyType, implementationType);
    }

    private static ClassNode make(String simpleName) {
        return ClassHelper.make(PROPERTY_PACKAGE + simpleName);
    }
}
