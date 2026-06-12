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
*/
public class ASTTransformationsContext {
    /**
     * Class loader used to load global and local AST transformations.
     */
    protected final GroovyClassLoader transformLoader;

    /**
     * Compilation unit that receives the transformations.
     */
    protected final CompilationUnit compilationUnit;
    /**
     * Names of the global transformations applied so far.
     */
    protected final Set<String> globalTransformNames = new HashSet<String>();

    /**
     * Creates a context for tracking AST transformations during compilation.
     *
     * @param compilationUnit the compilation unit being transformed
     * @param transformLoader the class loader used for transformation classes
     */
    public ASTTransformationsContext(final CompilationUnit compilationUnit, final GroovyClassLoader transformLoader) {
        this.compilationUnit = compilationUnit;
        this.transformLoader = transformLoader;
    }

    /**
     * Returns the compilation unit associated with this context.
     *
     * @return the owning compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    /**
     * Returns the names of applied global AST transformations.
     *
     * @return the recorded transformation names
     */
    public Set<String> getGlobalTransformNames() {
        return globalTransformNames;
    }

    /**
     * Returns the class loader used to load transformations.
     *
     * @return the transformation class loader
     */
    public GroovyClassLoader getTransformLoader() {
        return transformLoader;
    }
}
