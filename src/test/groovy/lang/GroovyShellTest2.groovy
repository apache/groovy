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
package groovy.lang

import groovy.test.GroovyTestCase

class GroovyShellTest2 extends GroovyTestCase {
    void testBindingsInBaseScriptInitializers() {
        def shell = new GroovyShell();
        def scriptText = '''
        @groovy.transform.BaseScript CustomBaseScript baseScript

        abstract class CustomBaseScript extends Script {
            CustomBaseScript() { this(new Binding()) }
            public CustomBaseScript(Binding b) { super(b) }

            def script_args = getProperty('args')
        }

        assert script_args[0] == 'Hello Groovy'
        script_args[0]
'''

        def arg0 = 'Hello Groovy'
        def result = shell.run scriptText, 'TestBindingsInBaseScriptInitializers.groovy', [arg0]
        assert result == arg0
    }

    void testBindingsInScriptFieldInitializers() {
        def shell = new GroovyShell();
        def scriptText = '''
        @groovy.transform.Field def script_args = getProperty('args')

        assert script_args[0] == 'Rehi Groovy'
        script_args[0]
'''

        def arg0 = 'Rehi Groovy'
        def result = shell.run scriptText, 'TestBindingsInScriptFieldInitializers.groovy', [arg0]
        assert result == arg0
    }

    void testEvalBindingsInBaseScriptInitializers() {
        def context = new Binding()
        def arg0 = 'Hello Groovy Eval'
        context.setProperty("args", [arg0] as String[])
        def shell = new GroovyShell(context);
        def scriptText = '''
        @groovy.transform.BaseScript CustomBaseScript baseScript

        abstract class CustomBaseScript extends Script {
            CustomBaseScript() { this(new Binding()) }
            public CustomBaseScript(Binding b) { super(b) }

            def script_args = getProperty('args')
        }

        assert script_args[0] == 'Hello Groovy Eval'
        script_args[0]
'''
        def result = shell.evaluate scriptText
        assert result == arg0
    }

    void testEvalBindingsInScriptFieldInitializers() {
        def context = new Binding()
        def arg0 = 'Rehi Groovy Eval'
        context.setProperty("args", [arg0] as String[])
        def shell = new GroovyShell(context);
        def scriptText = '''
        @groovy.transform.Field def script_args = getProperty('args')
        assert script_args[0] == 'Rehi Groovy Eval'
        script_args[0]
'''

        def result = shell.evaluate scriptText
        assert result == arg0
    }
}
