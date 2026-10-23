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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * A script whose base class has no no-arg constructor can use
 * {@code @InheritConstructors} to take on the base class constructors.
 */
final class Groovy11154 {

    private static final String SETUP = '''
        import groovy.transform.InheritConstructors
        import org.codehaus.groovy.control.CompilerConfiguration
        import org.codehaus.groovy.control.MultipleCompilationErrorsException
        import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

        abstract class NamedScript extends Script {
            String name
            NamedScript(String name) { this.name = name }
        }
        abstract class NamedBindingScript extends Script {
            String name
            NamedBindingScript(String name, Binding binding) { super(binding); this.name = name }
        }

        Class compile(Class base, String text, boolean inherit = true) {
            def config = new CompilerConfiguration(scriptBaseClass: base.name)
            if (inherit) config.addCompilationCustomizers(new ASTTransformationCustomizer(InheritConstructors))
            new GroovyClassLoader(this.class.classLoader, config).parseClass(text)
        }
    '''

    @Test
    void testScriptBaseClassConstructorWithArgument() {
        assertScript SETUP + '''
            Class c = compile(NamedScript, '"hello $name"')
            assert c.declaredConstructors*.parameterTypes == [[String] as Class[]]
            assert c.newInstance('world').run() == 'hello world'
        '''
    }

    @Test
    void testScriptBaseClassConstructorWithArgumentAndField() {
        assertScript SETUP + '''
            Class c = compile(NamedScript, '@groovy.transform.Field int x = 42; "$name $x"')
            assert c.newInstance('answer').run() == 'answer 42'
        '''
    }

    @Test
    void testScriptBaseClassConstructorWithArgumentAndBinding() {
        assertScript SETUP + '''
            Class c = compile(NamedBindingScript, '"$name $foo"')
            assert c.declaredConstructors*.parameterTypes == [[String, Binding] as Class[]]
            assert c.newInstance('bar', new Binding(foo: 'baz')).run() == 'bar baz'
        '''
    }

    @Test
    void testScriptBaseClassWithoutInheritConstructors() {
        assertScript SETUP + '''
            def err = groovy.test.GroovyAssert.shouldFail(MultipleCompilationErrorsException) {
                compile(NamedScript, '1', false)
            }
            assert err.message.contains('Implicit super constructor NamedScript() is undefined for generated constructor')
        '''
    }

    @Test
    void testBaseScriptInSameSource() {
        assertScript SETUP + '''
            Class c = compile(Script, \'\'\'
                abstract class Base extends Script {
                    String name
                    Base(String name) { this.name = name }
                }
                @groovy.transform.BaseScript Base base
                "base $name"
            \'\'\')
            assert c.declaredConstructors*.parameterTypes == [[String] as Class[]]
            assert c.newInstance('script').run() == 'base script'
        '''
    }
}
