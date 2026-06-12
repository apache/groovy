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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete compilation step involving one or more {@link ModuleNode} instances.
 * A single {@link CompileUnit} is shared across all modules and classes compiled in one invocation,
 * serving as a central repository for class metadata, import resolution, and compilation state.
 *
 * <p>The compile unit maintains mappings from fully qualified class names to {@link ClassNode} instances,
 * tracks which classes require compilation, stores source file locations, and manages generated inner classes.
 * It is attached to {@link MethodNode} and {@link ClassNode} instances to enable
 * fully qualified name resolution, import lookup, and other compilation-time queries.
 *
 * @see ModuleNode
 * @see ClassNode
 * @see SourceUnit
 * @see CompilerConfiguration
 */
public class CompileUnit implements NodeMetaDataHandler {

    private final CompilerConfiguration config;
    private final GroovyClassLoader loader;
    private final CodeSource codeSource;
    private volatile Map<?, ?> metaDataMap;

    private final List<ModuleNode> modules = new ArrayList<>();
    private final Map<String, ClassNode> classes = new LinkedHashMap<>();
    private final Map<String, ClassNode> classesToCompile = new LinkedHashMap<>();
    private final Map<String, SourceUnit> classNameToSource = new LinkedHashMap<>();
    private final Map<String, InnerClassNode> generatedInnerClasses = new LinkedHashMap<>();

    /**
     * Creates a compile unit with the given classloader and compiler configuration.
     * The code source is set to {@code null}.
     *
     * @param classLoader the {@link GroovyClassLoader} to use for loading classes
     * @param config the {@link CompilerConfiguration} defining compilation behavior
     */
    public CompileUnit(final GroovyClassLoader classLoader, final CompilerConfiguration config) {
        this(classLoader, null, config);
    }

    /**
     * Creates a compile unit with the given classloader, code source, and compiler configuration.
     *
     * @param classLoader the {@link GroovyClassLoader} to use for loading classes
     * @param codeSource the {@link CodeSource} for code permissions or {@code null}
     * @param config the {@link CompilerConfiguration} defining compilation behavior
     */
    public CompileUnit(final GroovyClassLoader classLoader, final CodeSource codeSource, final CompilerConfiguration config) {
        this.loader = classLoader;
        this.codeSource = codeSource;
        this.config = config;
    }

    /**
     * Returns the compiler configuration for this compilation.
     *
     * @return the {@link CompilerConfiguration}
     */
    public CompilerConfiguration getConfig() {
        return config;
    }

    /**
     * Returns the Groovy classloader used for loading classes during compilation.
     *
     * @return the {@link GroovyClassLoader}
     */
    public GroovyClassLoader getClassLoader() {
        return loader;
    }

    /**
     * Returns the code source used for Java security permissions.
     *
     * @return the {@link CodeSource} or {@code null}
     */
    public CodeSource getCodeSource() {
        return codeSource;
    }

    /**
     * Returns the metadata map for storing compilation-phase metadata.
     * Implements {@link NodeMetaDataHandler} for consistency with AST nodes.
     *
     * @return the metadata map or {@code null}
     */
    @Override
    public Map<?, ?> getMetaDataMap() {
        return metaDataMap;
    }

    /**
     * Sets the metadata map for storing compilation-phase metadata.
     *
     * @param metaDataMap the metadata map or {@code null}
     */
    @Override
    public void setMetaDataMap(final Map<?, ?> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    /**
     * Returns all modules being compiled in this unit.
     *
     * @return a list of {@link ModuleNode} instances
     */
    public List<ModuleNode> getModules() {
        return modules;
    }

    /**
     * Returns all classes from all modules in this compilation unit.
     *
     * @return a list of all {@link ClassNode} instances across all modules
     */
    public List<ClassNode> getClasses() {
        List<ClassNode> answer = new ArrayList<>();
        for (ModuleNode module : modules) {
            answer.addAll(module.getClasses());
        }
        return answer;
    }

    /**
     * Looks up a class by its fully qualified name, checking both compiled classes and classes pending compilation.
     * Returns {@code null} if the name does not exist in the current compilation unit (does not check .class files on classpath).
     *
     * @param name the fully qualified class name
     * @return the {@link ClassNode} or {@code null} if not found in this unit
     */
    public ClassNode getClass(final String name) {
        ClassNode cn = classes.get(name);
        if (cn == null)
            cn = classesToCompile.get(name);
        return cn;
    }

    /**
     * Returns the map of classes pending compilation.
     * These are typically forward references or generated classes queued for later compilation phases.
     *
     * @return a map from fully qualified names to {@link ClassNode} instances pending compilation
     */
    public Map<String, ClassNode> getClassesToCompile() {
        return classesToCompile;
    }

    /**
     * Retrieves the source file location for a class by name.
     * Useful for mapping compiled classes back to their source {@link SourceUnit}.
     *
     * @param className the fully qualified class name
     * @return the {@link SourceUnit} where this class was defined, or {@code null} if not tracked
     */
    public SourceUnit getScriptSourceLocation(final String className) {
        return classNameToSource.get(className);
    }

    /**
     * Returns an unmodifiable view of all generated inner classes.
     * These are typically inner classes generated by Groovy transformations or the compiler.
     *
     * @return an unmodifiable map from inner class names to {@link InnerClassNode} instances
     */
    public Map<String, InnerClassNode> getGeneratedInnerClasses() {
        return Collections.unmodifiableMap(generatedInnerClasses);
    }

    /**
     * Retrieves a generated inner class by name.
     *
     * @param name the fully qualified inner class name
     * @return the {@link InnerClassNode} or {@code null} if not found
     */
    public InnerClassNode getGeneratedInnerClass(final String name) {
        return generatedInnerClasses.get(name);
    }

    /**
     * Checks if there are any classes pending compilation.
     *
     * @return {@code true} if {@code classesToCompile} is not empty
     * @deprecated Use {@link #getClassesToCompile()} instead
     */
    @Deprecated
    public boolean hasClassNodeToCompile() {
        return !classesToCompile.isEmpty();
    }

    /**
     * Returns an iterator over the names of classes pending compilation.
     *
     * @return an iterator over fully qualified class names
     * @deprecated Use {@link #getClassesToCompile()} instead
     */
    @Deprecated
    public Iterator<String> iterateClassNodeToCompile() {
        return classesToCompile.keySet().iterator();
    }

    /**
     * Adds a module to this compilation unit along with all its classes.
     * If the module is {@code null} (indicating a parsing error), it is silently ignored.
     * Sets this compile unit as the module's owning unit.
     *
     * @param node the {@link ModuleNode} to add, or {@code null}
     */
    public void addModule(final ModuleNode node) {
        // null means a compilation error prevented groovy from building an AST
        if (node != null) {
            modules.add(node);
            node.setUnit(this);
            addClasses(node.getClasses());
        }
    }

    /**
     * Adds all classes in the given list to this compilation unit.
     *
     * @param list the list of {@link ClassNode} instances to add
     */
    public void addClasses(final List<ClassNode> list) {
        for (ClassNode node : list) {
            addClass(node);
        }
    }

    /**
     * Adds a single class to this compilation unit, checking for duplicates and reporting errors.
     * If a duplicate is detected, emits a {@link SyntaxException} describing the conflict and its possible resolution.
     * Removes any pending compilation entry for the class if it exists.
     *
     * @param node the {@link ClassNode} to add
     */
    public void addClass(ClassNode node) {
        node = node.redirect();
        String name = node.getName();
        ClassNode stored = classes.get(name);
        if (stored != null && stored != node) {
            // we have a duplicate class!
            // One possibility for this is, that we declared a script and a
            // class in the same file and named the class like the file
            SourceUnit nodeSource = node.getModule().getContext();
            SourceUnit storedSource = stored.getModule().getContext();
            String txt = "Invalid duplicate class definition of class " + node.getName() + " : ";
            if (nodeSource == storedSource) {
                // same class in same source
                txt += "The source " + nodeSource.getName() + " contains at least two definitions of the class " + node.getName() + ".\n";
                if (node.isScriptBody() || stored.isScriptBody()) {
                    txt += "One of the classes is an explicit generated class using the class statement, the other is a class generated from" +
                            " the script body based on the file name. Solutions are to change the file name or to change the class name.\n";
                }
            } else {
                txt += "The sources " + nodeSource.getName() + " and " + storedSource.getName() + " each contain a class with the name " + node.getName() + ".\n";
            }
            nodeSource.addErrorAndContinue(new SyntaxException(txt, node));
        }
        classes.put(name, node);

        ClassNode cn = classesToCompile.remove(name);
        if (cn != null) cn.setRedirect(node);
    }

    /**
     * Marks a class for compilation and associates it with its source location.
     * This method does not perform actual compilation—it is only a marker that the class
     * should be compiled by the {@link org.codehaus.groovy.control.CompilationUnit} at the end of a parse step.
     * No marked class should remain by the end of compilation.
     *
     * @param node the {@link ClassNode} to compile
     * @param location the {@link SourceUnit} where this class is defined
     */
    public void addClassNodeToCompile(final ClassNode node, final SourceUnit location) {
        String nodeName = node.getName();
        classesToCompile.put(nodeName, node);
        classNameToSource.put(nodeName, location);
    }

    /**
     * Adds a generated inner class to the map of generated inner classes.
     * These are inner classes created during compilation, typically by transformations.
     *
     * @param icn the {@link InnerClassNode} to register as generated
     */
    public void addGeneratedInnerClass(final InnerClassNode icn) {
        generatedInnerClasses.put(icn.getName(), icn);
    }
}
