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
package org.apache.groovy.groovysh.util

import groovy.test.GroovyTestCase

/**
 * Unit tests for the {@link ScriptVariableAnalyzer} class.
 */
class ScriptVariableAnalyzerTest extends GroovyTestCase {

    void testEmptyScript() {
        assert [] as Set == ScriptVariableAnalyzer.getBoundVars('', Thread.currentThread().contextClassLoader)
    }

    void testEmptyScriptNullLOader() {
        assert [] as Set == ScriptVariableAnalyzer.getBoundVars('', null)
    }

    void testBound() {
        def scriptText = '''
   int a = 6
   String b = "7"
'''
        assert ['a', 'b'] as Set == ScriptVariableAnalyzer.getBoundVars(scriptText, Thread.currentThread().contextClassLoader)
    }

    void testUnBound() {
        def scriptText = '''
   a = 6
   b = "7"
'''
        assert [] as Set == ScriptVariableAnalyzer.getBoundVars(scriptText, Thread.currentThread().contextClassLoader)
    }

    void testMixed() {
        def scriptText = '''
   def foo(args) { args && !d }
   Closure c = { 4 }
   int b = 6
   try {
       println(a + 5 - b + c() / foo(a))
   } catch (Throwable t) {
       println b
   } finally {
       throw d
   }
   assert b
'''
        assert ['b', 'c'] as Set == ScriptVariableAnalyzer.getBoundVars(scriptText, Thread.currentThread().contextClassLoader)
    }
}
