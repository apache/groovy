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
    protected final CompilationCustomizer delegate;

    public DelegatingCustomizer(CompilationCustomizer delegate) {
        super(delegate.getPhase());
        this.delegate = delegate;
    }

    @Override
    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        if (delegate instanceof CompilationUnitAware) {
            ((CompilationUnitAware) delegate).setCompilationUnit(compilationUnit);
        }
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        delegate.call(source, context, classNode);
    }
}
