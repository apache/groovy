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
package org.codehaus.groovy.ast;

import java.util.Objects;

/**
 * Represents an import statement in Groovy source code, supporting single-type imports,
 * wildcard imports, and static imports. Provides a unified representation for different import styles
 * with methods to query import type and generate display text.
 *
 * @see ClassNode
 * @see org.codehaus.groovy.ast.AnnotatedNode
 */
public class ImportNode extends AnnotatedNode {

    private ClassNode type;
    private final String alias;
    private final String fieldName;
    private final String packageName;
    private final boolean isStar;
    private final boolean isStatic;
    private transient int hashCode;

    /**
     * Creates an import for a single type (e.g., {@code import pack.Type} or {@code import pack.Type as Alias}).
     *
     * @param type the {@link ClassNode} being imported (never null)
     * @param alias an optional alias name, or null if no alias is specified
     * @throws NullPointerException if type is null
     */
    public ImportNode(final ClassNode type, final String alias) {
        this.type = Objects.requireNonNull(type);
        this.alias = alias;
        this.isStar = false;
        this.isStatic = false;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Creates an import for all types in a package (e.g., {@code import pack.*}).
     *
     * @param packageName the fully qualified package name (never null)
     * @throws NullPointerException if packageName is null
     */
    public ImportNode(final String packageName) {
        this.type = null;
        this.alias = null;
        this.isStar = true;
        this.isStatic = false;
        this.packageName = Objects.requireNonNull(packageName);
        this.fieldName = null;
    }

    /**
     * Creates a static wildcard import for all static members of a type (e.g., {@code import static pack.Type.*}).
     *
     * @param type the {@link ClassNode} whose static members are imported (never null)
     * @throws NullPointerException if type is null
     */
    public ImportNode(final ClassNode type) {
        this.type = Objects.requireNonNull(type);
        this.alias = null;
        this.isStar = true;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Creates a static import for a specific field or method of a type (e.g., {@code import static pack.Type.name}
     * or {@code import static pack.Type.name as alias}).
     *
     * @param type the {@link ClassNode} containing the static member (never null)
     * @param fieldName the name of the static field or method being imported (never null)
     * @param alias an optional alias name, or null if no alias is specified
     * @throws NullPointerException if type or fieldName is null
     */
    public ImportNode(final ClassNode type, final String fieldName, final String alias) {
        this.type = Objects.requireNonNull(type);
        this.alias = alias;
        this.isStar = false;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = Objects.requireNonNull(fieldName);
    }

    /**
     * Generates the text representation of this import statement as it would appear in source code.
     * For example: "import java.util.List", "import static java.util.Collections.*", etc.
     *
     * @return the text representation of this import
     */
    @Override
    public String getText() {
        String simpleName = getAlias();
        String memberName = getFieldName();

        if (!isStatic()) {
            if (isStar()) {
                return "import " + getPackageName() + "*";
            } else if (simpleName == null || simpleName.isEmpty()
                    || simpleName.equals(getType().getNameWithoutPackage())) {
                return "import " + getClassName();
            } else {
                return "import " + getClassName() + " as " + simpleName;
            }
        } else {
            if (isStar()) {
                return "import static " + getClassName() + ".*";
            } else if (simpleName == null || simpleName.isEmpty() || simpleName.equals(memberName)) {
                return "import static " + getClassName() + "." + memberName;
            } else {
                return "import static " + getClassName() + "." + memberName + " as " + simpleName;
            }
        }
    }

    public boolean isStar() {
        return isStar;
    }

    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Returns the alias name for this import, if specified.
     *
     * @return the alias, or null if no alias is provided
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Returns the fully qualified class name of the imported type.
     *
     * @return the class name, or null if this import refers to a package
     */
    public String getClassName() {
        return (type == null ? null : type.getName());
    }

    /**
     * Returns the name of the static field or method being imported via static import.
     *
     * @return the field name, or null if this is not a static field/method import
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the package name for a wildcard import, or the package containing
     * the imported type or static member.
     *
     * @return the package name, or null if this import references a specific type
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the {@link ClassNode} being imported or providing static members.
     *
     * @return the imported type, or null if this is a package-level wildcard import
     */
    public ClassNode getType() {
        return type;
    }

    /**
     * Updates the {@link ClassNode} being imported. This may be used during compilation phases
     * to replace placeholder types with resolved types.
     *
     * @param type the new {@link ClassNode} (never null)
     * @throws NullPointerException if type is null
     */
    public void setType(final ClassNode type) {
        this.type = Objects.requireNonNull(type);
        hashCode = 0;
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (!(that instanceof ImportNode node)) return false;

        if (!Objects.equals(type, node.type))
            return false;
        if (!Objects.equals(alias, node.alias))
            return false;
        if (!Objects.equals(fieldName, node.fieldName))
            return false;
        if (!Objects.equals(packageName, node.packageName))
            return false;
        return (isStar == node.isStar && isStatic == node.isStatic);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            hashCode = Objects.hash(type, alias, fieldName, packageName, isStar, isStatic);
        }
        return result;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
    }
}
