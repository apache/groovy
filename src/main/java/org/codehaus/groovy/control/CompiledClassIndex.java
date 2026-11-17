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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Binary names of class files produced by an external Java compiler during
 * this compilation. A {@link CompilationUnit} that does not run javac leaves
 * the index empty. {@code module-info} is ignored. A class in the
 * unnamed package is remembered so a named module can reject it.
 * Joint compilation fills the index after javac.
 *
 * @since 7.0.0
 */
public final class CompiledClassIndex {

    private final Set<String> names = new LinkedHashSet<>();
    private final Set<String> packages = new LinkedHashSet<>();
    private boolean unnamedClass;

    /**
     * Records a class file written by javac. {@code module-info} is ignored.
     * A name with no package is remembered as an unnamed-package class.
     *
     * @param binaryName the binary name, using {@code $} to separate nested types
     */
    public void add(final String binaryName) {
        if (binaryName == null || binaryName.isEmpty() || "module-info".equals(binaryName)) {
            return;
        }
        names.add(binaryName);
        int dot = binaryName.lastIndexOf('.');
        if (dot < 0) {
            unnamedClass = true;
        } else {
            packages.add(binaryName.substring(0, dot));
        }
    }

    /**
     * Whether a class of this binary name was recorded.
     *
     * @param binaryName the binary name to look up
     * @return {@code true} when {@link #add(String)} recorded this name
     */
    public boolean contains(final String binaryName) {
        return names.contains(binaryName);
    }

    /**
     * Packages of the recorded class files.
     *
     * @return an unmodifiable set of package names
     */
    public Set<String> getPackages() {
        return Collections.unmodifiableSet(packages);
    }

    /**
     * Whether a recorded class file lives in the unnamed package.
     *
     * @return {@code true} when an unnamed-package class was recorded
     */
    public boolean hasUnnamedClass() {
        return unnamedClass;
    }
}
