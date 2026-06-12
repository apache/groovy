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
package org.codehaus.groovy.control.customizers;

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Base class for compilation customizers which delegate to another customizer. The phase this
 * customizer runs at is retrieved from the phase of the delegate.
 *
 * @since 2.1.0
 */
public abstract class DelegatingCustomizer extends CompilationCustomizer implements CompilationUnitAware {
    /**
     * Customizer that receives delegated callbacks.
     */
    protected final CompilationCustomizer delegate;

    /**
     * Creates a delegating customizer backed by another customizer.
     *
     * @param delegate the customizer to delegate to
     */
    public DelegatingCustomizer(CompilationCustomizer delegate) {
        super(delegate.getPhase());
        this.delegate = delegate;
    }

    /**
     * Forwards the compilation unit to the delegate when it supports the callback.
     *
     * @param compilationUnit the active compilation unit
     */
    @Override
    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        if (delegate instanceof CompilationUnitAware) {
            ((CompilationUnitAware) delegate).setCompilationUnit(compilationUnit);
        }
    }

    /**
     * Delegates customization of the supplied class node.
     *
     * @param source the source unit being compiled
     * @param context the current generator context
     * @param classNode the class node being customized
     * @throws CompilationFailedException if the delegate fails
     */
    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        delegate.call(source, context, classNode);
    }
}
