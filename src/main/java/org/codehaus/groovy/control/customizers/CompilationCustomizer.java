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

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;

/**
 * Users wanting to customize the configuration process such as adding imports, restricting the
 * language features or apply AST transformations by default should implement this class, then
 * call the {@link org.codehaus.groovy.control.CompilerConfiguration#addCompilationCustomizers(CompilationCustomizer...)}
 * method.
 *
 * @since 1.8.0
 */
public abstract class CompilationCustomizer extends CompilationUnit.PrimaryClassNodeOperation {
    private final CompilePhase phase;

    public CompilationCustomizer(CompilePhase phase) {
        this.phase = phase;
    }

    public CompilePhase getPhase() {
        return phase;
    }
}
