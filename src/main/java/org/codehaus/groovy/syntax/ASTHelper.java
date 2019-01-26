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
 * A common base class of AST helper methods which can be shared across the classic and new parsers
 *
 * @author Jochen Theodorou
 * @author James Strachan
 * @author Bob McWhirter
 * @author Sam Pullara
 * @author Chris Poirier
 */
public class ASTHelper {

    /** The SourceUnit controlling us */
    private SourceUnit controller;

    /** Our ClassLoader, which provides information on external types */
    private ClassLoader classLoader;

    protected ModuleNode output;

    /** The package name in which the module sits */
    private String packageName;

    // TODO should this really be static???
    protected static final Map resolutions = new HashMap();  // cleared on build(), to be safe

    public ASTHelper(SourceUnit controller, ClassLoader classLoader) {
        this();
        this.controller = controller;
        this.classLoader = classLoader;
    }

    public ASTHelper() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        setPackage(packageName, new ArrayList<>());
    }

    public PackageNode setPackage(String packageName, List<AnnotationNode> annotations) {
        this.packageName = packageName;
        if (packageName != null && packageName.length() > 0) {
            packageName += '.';
        }
        PackageNode packageNode = new PackageNode(packageName);
        packageNode.addAnnotations(annotations);
        output.setPackage(packageNode);
        return packageNode;
    }

    /**
     * Returns our class loader (as supplied on construction).
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public SourceUnit getController() {
        return controller;
    }

    public void setController(SourceUnit controller) {
        this.controller = controller;
    }
    
    /**
     * @return Two names joined by a dot. If the base name is
     * empty, returns the name unchanged.
     * @param base typically a package
     * @param name typically a simple unqualified class name
     */
    public static String dot(String base, String name) {
        if (base != null && base.length() > 0) {
            return base + "." + name;
        }
        return name;
    }

    protected void makeModule() {
        this.output = new ModuleNode(controller);
        resolutions.clear();
    }

    /**
     * A synonym for <code>dot( base, "" )</code>.
     */
    protected String dot(String base) {
        return dot(base, "");
    }

    protected void addImport(ClassNode type, String name, String aliasName) {
        addImport(type, name, aliasName, new ArrayList<>());
    }

    protected void addImport(ClassNode type, String name, String aliasName, List<AnnotationNode> annotations) {
        if (aliasName == null) aliasName=name;
        output.addImport(aliasName, type, annotations);
    }

    protected void addStaticImport(ClassNode type, String name, String alias) {
        addStaticImport(type, name, alias, new ArrayList<>());
    }

    protected void addStaticImport(ClassNode type, String name, String alias, List<AnnotationNode> annotations) {
        if (alias == null) alias = name;
        output.addStaticImport(type, name, alias, annotations);
    }

    protected void addStaticStarImport(ClassNode type, String importClass) {
        addStaticStarImport(type, importClass, new ArrayList<>());
    }

    protected void addStaticStarImport(ClassNode type, String importClass, List<AnnotationNode> annotations) {
        output.addStaticStarImport(importClass, type, annotations);
    }

    protected void addStarImport(String importPackage) {
        addStarImport(importPackage, new ArrayList<>());
    }

    protected void addStarImport(String importPackage, List<AnnotationNode> annotations) {
        output.addStarImport( dot(importPackage), annotations );
    }
}
