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
package org.codehaus.groovy.control;

import groovy.lang.GroovyClassLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores state information about global AST transformations applied to a compilation unit.
 *
 * @author Cedric Champeau
*/
public class ASTTransformationsContext {
    protected final GroovyClassLoader transformLoader;  // Classloader for global and local transforms

    protected final CompilationUnit compilationUnit; // The compilation unit global AST transformations are applied on
    protected final Set<String> globalTransformNames = new HashSet<>(); // collected AST transformation names

    public ASTTransformationsContext(final CompilationUnit compilationUnit, final GroovyClassLoader transformLoader) {
        this.compilationUnit = compilationUnit;
        this.transformLoader = transformLoader;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public Set<String> getGlobalTransformNames() {
        return globalTransformNames;
    }

    public GroovyClassLoader getTransformLoader() {
        return transformLoader;
    }
}
