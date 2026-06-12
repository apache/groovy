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
package org.codehaus.groovy.groovydoc;

import java.util.List;
import java.util.Map;

/**
 * Root object for a Groovydoc run, exposing the discovered packages, classes, and diagnostics APIs.
 */
public interface GroovyRootDoc extends GroovyDoc, GroovyDocErrorReporter {
    /**
     * Resolves a class name from the perspective of another documented class.
     *
     * @param groovyClassDoc the class providing the lookup context
     * @param name the class name to resolve
     * @return the matching class documentation, or {@code null} if it cannot be resolved
     */
    GroovyClassDoc classNamed(GroovyClassDoc groovyClassDoc, String name);

    /**
     * Returns all classes known to this documentation run.
     *
     * @return the discovered classes
     */
    GroovyClassDoc[] classes();

    /**
     * Returns the effective tool options associated with this documentation run.
     *
     * @return the configured options
     */
    String[][] options();

    /**
     * Resolves a package by name.
     *
     * @param arg0 the package name to resolve
     * @return the matching package documentation, or {@code null} if none exists
     */
    GroovyPackageDoc packageNamed(String arg0);

    /**
     * Returns the classes explicitly specified as documentation targets.
     *
     * @return the specified classes
     */
    GroovyClassDoc[] specifiedClasses();

    /**
     * Returns the packages explicitly specified as documentation targets.
     *
     * @return the specified packages
     */
    GroovyPackageDoc[] specifiedPackages();

    /**
     * Returns the classes visible from a set of imports.
     *
     * @param importedClassesAndPackages the imported classes and packages to evaluate
     * @return a map of visible class names to class documentation
     */
    Map<String, GroovyClassDoc> getVisibleClasses(List importedClassesAndPackages);

    /**
     * Returns the classes that have already been resolved for the current documentation run.
     *
     * @return a map of resolved class names to class documentation
     */
    Map<String, GroovyClassDoc> getResolvedClasses();
}
