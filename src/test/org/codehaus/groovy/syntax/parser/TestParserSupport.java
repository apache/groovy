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
package org.codehaus.groovy.syntax.parser;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/**
 * An abstract base class useful for AST parser related test cases
 */
public abstract class TestParserSupport extends GroovyTestCase {
    public ModuleNode parse(String text, String description) {
        SourceUnit unit = SourceUnit.create(description, text);
        CompilationUnit compUnit = new CompilationUnit();
        compUnit.addSource(unit);
        compUnit.compile(Phases.CONVERSION);
        return unit.getAST();
    }
}
