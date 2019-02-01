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

import junit.framework.TestCase;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Base class for every TestCase that uses an AST
 */
public abstract class ASTTest extends TestCase {

    public ModuleNode getAST(String source, int untilPhase) {
        SourceUnit unit = SourceUnit.create("Test", source);
        CompilationUnit compUnit = new CompilationUnit();
        compUnit.addSource(unit);
        compUnit.compile(untilPhase);
        return unit.getAST();
    }

    public ModuleNode getAST(String source) {
        return getAST(source, Phases.SEMANTIC_ANALYSIS);
    }
}
