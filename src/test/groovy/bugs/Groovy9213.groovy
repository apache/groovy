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
package groovy.bugs

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Ignore
import org.junit.Test

import static org.codehaus.groovy.control.ParserPluginFactory.antlr2
import static org.codehaus.groovy.control.ParserPluginFactory.antlr4

@CompileStatic @Ignore
final class Groovy9213 {

    @Test(timeout=15000L)
    @Deprecated
    void testUnmatchedParenInLongScript2() {
        def config = new CompilerConfiguration()
        config.pluginFactory = antlr2()

        new GroovyShell(config).evaluate('''
            int a = 0
            (
        ''' + ('a = 0\n' * 50))
    }

    @Test(timeout=15000L)
    void testUnmatchedParenInLongScript4() {
        def config = new CompilerConfiguration()
        config.pluginFactory = antlr4(config)

        new GroovyShell(config).evaluate('''
            int a = 0
            (
        ''' + ('a = 0\n' * 50))
    }
}
