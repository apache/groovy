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
package org.codehaus.groovy.syntax;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common base class providing shared AST helper methods used by both the classic and new parsers.
 * Manages module-level information including the package, imports, and related AST construction utilities.
 */
public class ASTHelper {

    /** The SourceUnit controlling this helper. */
    private SourceUnit controller;

    /** The ClassLoader providing information on external types. */
    private ClassLoader classLoader;

    /** The output module node being constructed. */
    protected ModuleNode output;

    /** The package name in which the module is located. */
    private String packageName;

    /** Static map for type resolutions, cleared on each build for safety. */
    protected static final Map resolutions = new HashMap();

    /**
     * Constructs an ASTHelper with a source unit and class loader.
     *
     * @param controller the {@link SourceUnit} that controls this helper
     * @param classLoader the {@link ClassLoader} for resolving external types
     */
    public ASTHelper(SourceUnit controller, ClassLoader classLoader) {
        this();
        this.controller = controller;
        this.classLoader = classLoader;
    }

    /**
     * Constructs an ASTHelper with no controller or class loader.
     * These must be set later via {@link #setController(SourceUnit)} and {@link #setClassLoader(ClassLoader)}.
     */
    public ASTHelper() {
    }

    /**
     * Returns the package name in which this module is located.
     *
     * @return the package name, or {@code null} if not set
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name for this module (without trailing dot).
     * Convenience method equivalent to {@code setPackage(packageName, new ArrayList<>())}.
     *
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        setPackage(packageName, new ArrayList<>());
    }

    /**
     * Sets the package for this module with optional annotations.
     *
     * @param packageName the package name (will have a dot appended if non-empty)
     * @param annotations annotations to apply to the package
     * @return the created {@link PackageNode}
     */
    public PackageNode setPackage(String packageName, List<AnnotationNode> annotations) {
        this.packageName = packageName;
        if (packageName != null && !packageName.isEmpty()) {
            packageName += '.';
        }
        PackageNode packageNode = new PackageNode(packageName);
        packageNode.addAnnotations(annotations);
        output.setPackage(packageNode);
        return packageNode;
    }

    /**
     * Returns the class loader used for resolving external types.
     *
     * @return the {@link ClassLoader}, or {@code null} if not set
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the class loader to use for resolving external types.
     *
     * @param classLoader the {@link ClassLoader} to use
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns the source unit controlling this helper.
     *
     * @return the {@link SourceUnit}, or {@code null} if not set
     */
    public SourceUnit getController() {
        return controller;
    }

    /**
     * Sets the source unit that controls this helper.
     *
     * @param controller the {@link SourceUnit} to use
     */
    public void setController(SourceUnit controller) {
        this.controller = controller;
    }

    /**
     * Joins two names with a dot, returning the base name unchanged if empty.
     * Useful for constructing fully qualified names.
     *
     * @param base the base name (typically a package), or {@code null}
     * @param name the name to append (typically a class name)
     * @return the joined name, or {@code name} if base is {@code null} or empty
     */
    public static String dot(String base, String name) {
        if (base != null && !base.isEmpty()) {
            return base + "." + name;
        }
        return name;
    }

    /**
     * Initializes the output module node and clears resolution cache.
     * Called at the start of parsing.
     */
    protected void makeModule() {
        this.output = new ModuleNode(controller);
        resolutions.clear();
    }

    /**
     * Equivalent to {@code dot(base, "")}. Returns the base with a trailing dot, or empty string.
     * Useful for constructing package names.
     *
     * @param base the base name (typically a package)
     * @return the base with a trailing dot, or empty string if base is empty
     */
    protected String dot(String base) {
        return dot(base, "");
    }

    /**
     * Adds a type import to the module.
     * Equivalent to {@code addImport(type, name, aliasName, new ArrayList<>())}.
     *
     * @param type the {@link ClassNode} to import
     * @param name the original name
     * @param aliasName the alias name to use (or the same as name if not aliased)
     */
    protected void addImport(ClassNode type, String name, String aliasName) {
        addImport(type, name, aliasName, new ArrayList<>());
    }

    /**
     * Adds a type import to the module with optional annotations.
     *
     * @param type the {@link ClassNode} to import
     * @param name the original name
     * @param aliasName the alias name to use (or the same as name if not aliased)
     * @param annotations annotations for this import
     */
    protected void addImport(ClassNode type, String name, String aliasName, List<AnnotationNode> annotations) {
        if (aliasName == null) aliasName=name;
        output.addImport(aliasName, type, annotations);
    }

    /**
     * Adds a static import to the module.
     * Equivalent to {@code addStaticImport(type, name, alias, new ArrayList<>())}.
     *
     * @param type the {@link ClassNode} containing the static member
     * @param name the static member name
     * @param alias the alias to use in the code
     */
    protected void addStaticImport(ClassNode type, String name, String alias) {
        addStaticImport(type, name, alias, new ArrayList<>());
    }

    /**
     * Adds a static import to the module with optional annotations.
     *
     * @param type the {@link ClassNode} containing the static member
     * @param name the static member name
     * @param alias the alias to use in the code
     * @param annotations annotations for this import
     */
    protected void addStaticImport(ClassNode type, String name, String alias, List<AnnotationNode> annotations) {
        if (alias == null) alias = name;
        output.addStaticImport(type, name, alias, annotations);
    }

    /**
     * Adds a static star import (imports all static members) to the module.
     * Equivalent to {@code addStaticStarImport(type, importClass, new ArrayList<>())}.
     *
     * @param type the {@link ClassNode} containing the static members
     * @param importClass the class name
     */
    protected void addStaticStarImport(ClassNode type, String importClass) {
        addStaticStarImport(type, importClass, new ArrayList<>());
    }

    /**
     * Adds a static star import (imports all static members) to the module with optional annotations.
     *
     * @param type the {@link ClassNode} containing the static members
     * @param importClass the class name
     * @param annotations annotations for this import
     */
    protected void addStaticStarImport(ClassNode type, String importClass, List<AnnotationNode> annotations) {
        output.addStaticStarImport(importClass, type, annotations);
    }

    /**
     * Adds a star import (imports all classes in a package) to the module.
     * Equivalent to {@code addStarImport(importPackage, new ArrayList<>())}.
     *
     * @param importPackage the package name
     */
    protected void addStarImport(String importPackage) {
        addStarImport(importPackage, new ArrayList<>());
    }

    /**
     * Adds a star import (imports all classes in a package) to the module with optional annotations.
     *
     * @param importPackage the package name
     * @param annotations annotations for this import
     */
    protected void addStarImport(String importPackage, List<AnnotationNode> annotations) {
        output.addStarImport( dot(importPackage), annotations );
    }
}
