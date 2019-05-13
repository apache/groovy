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
package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.control.CompilationFailedException

class GroovyshParsersTest extends GroovyTestCase {

    void testIgnoreSyntaxErrorForLineEnding() {
        assert !RigidParser.ignoreSyntaxErrorForLineEnding('foo')
        assert RigidParser.ignoreSyntaxErrorForLineEnding('foo {')
    }

    void testIsAnnotationExpression() {
        def mock = new MockFor(CompilationFailedException)
        mock.demand.getMessage(1) { 'unexpected token: @' }
        mock.use {
            CompilationFailedException mcee = new CompilationFailedException(0, null)
            assert RigidParser.isAnnotationExpression(mcee, '@Override')
        }
    }

    void testHasUnmatchedOpenBracketOrParen() {
        assert RigidParser.hasUnmatchedOpenBracketOrParen('a = [')
        assert !RigidParser.hasUnmatchedOpenBracketOrParen('a = [1,2,3]')
        assert !RigidParser.hasUnmatchedOpenBracketOrParen('a = 1,2,3]')

        assert RigidParser.hasUnmatchedOpenBracketOrParen('myfunc(3')
        assert !RigidParser.hasUnmatchedOpenBracketOrParen('myfunc(1,2,3)')
        assert !RigidParser.hasUnmatchedOpenBracketOrParen('a = 1,2,3)')
    }

}
