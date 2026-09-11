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
package org.apache.groovy.parser.antlr4

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertInstanceOf

/**
 * GROOVY-12398: supplementary-plane letters classify as
 * {@code CapitalizedIdentifier} from the decoded code point, so
 * {@code className} (which accepts only that token) can use them.
 */
final class Groovy12398Test {

    // DESERET CAPITAL LETTER LONG I / DESERET SMALL LETTER LONG I
    private static final String UPPER = "\uD801\uDC00"
    private static final String LOWER = "\uD801\uDC28"

    @Test
    void 'supplementary uppercase is a type-parameter class name'() {
        ModuleNode ast = module("class C<${UPPER}> {}")
        ClassNode c = ast.classes.find { it.nameWithoutPackage == 'C' }
        assertEquals 1, c.genericsTypes.length
        assertEquals UPPER, c.genericsTypes[0].name
    }

    @Test
    void 'supplementary lowercase is not a type-parameter class name'() {
        shouldFail(CompilationFailedException) {
            module("class C<${LOWER}> {}")
        }
    }

    @Test
    void 'supplementary uppercase local variable is a declaration'() {
        ModuleNode ast = module("${UPPER} x = 1")
        def stmt = ast.statementBlock.statements[0]
        assertInstanceOf(ExpressionStatement, stmt)
        assertInstanceOf(DeclarationExpression, stmt.expression)
    }

    @Test
    void 'supplementary lowercase leading identifier is a command expression'() {
        ModuleNode ast = module("${LOWER} x")
        def stmt = ast.statementBlock.statements[0]
        assertInstanceOf(ExpressionStatement, stmt)
        assertInstanceOf(MethodCallExpression, stmt.expression)
    }

    private static ModuleNode module(String src) {
        def cu = new CompilationUnit()
        cu.addSource('test.groovy', src)
        cu.compile(Phases.CONVERSION)
        return cu.AST.modules[0]
    }
}
