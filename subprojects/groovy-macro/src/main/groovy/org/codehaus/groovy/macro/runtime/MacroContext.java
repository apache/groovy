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
package org.codehaus.groovy.macro.runtime;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

/**
 * @author Sergei Egorov <bsideup@gmail.com>
 */

@Incubating
public class MacroContext {

    private final MethodCallExpression call;

    private final SourceUnit sourceUnit;

    private final CompilationUnit compilationUnit;

    public MacroContext(CompilationUnit compilationUnit, SourceUnit sourceUnit, MethodCallExpression call) {
        this.compilationUnit = compilationUnit;
        this.sourceUnit = sourceUnit;
        this.call = call;
    }

    public MethodCallExpression getCall() {
        return call;
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}
