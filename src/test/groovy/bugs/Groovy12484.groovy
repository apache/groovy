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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * A method or constructor call on a type that is not accessible from the
 * calling class is rejected by the type checker, as a field reference is,
 * instead of compiling to a direct reference that fails at runtime with
 * {@code IllegalAccessError}.
 */
final class Groovy12484 {

    private GroovyShell shell

    @BeforeEach
    void setUp() {
        def loader = new GroovyClassLoader()
        loader.parseClass '''
            package p
            import groovy.transform.PackageScope
            class Outer {
                @PackageScope enum PropertyKeys { localValueSet, other }
                @PackageScope static class Impl {
                    Impl() {}
                    static String hello() { 'hello' }
                }
            }
            @PackageScope class Top { static String greet() { 'greet' } }
            @PackageScope class Base { static String inherited() { 'inherited' } }
            class Pub extends Base {}
        '''
        shell = new GroovyShell(loader)
    }

    private static String compileStatic(String expression) {
        "@groovy.transform.CompileStatic def f() { $expression }\nf()"
    }

    @Test
    void testStaticMethodCallRejected() {
        for (expression in ['p.Outer.PropertyKeys.values()', "p.Outer.PropertyKeys.valueOf('other')"]) {
            def err = shouldFail MultipleCompilationErrorsException, {
                shell.evaluate compileStatic(expression)
            }
            assert err.message.contains('of class: p.Outer$PropertyKeys from class: ')
            assert err.message.contains('Cannot access method: value')
        }
        def err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate compileStatic('p.Outer.Impl.hello()')
        }
        assert err.message.contains('Cannot access method: hello of class: p.Outer$Impl from class: ')

        err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate compileStatic('p.Top.greet()')
        }
        assert err.message.contains('Cannot access method: greet of class: p.Top from class: ')
    }

    @Test
    void testConstructorCallRejected() {
        def err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate compileStatic('new p.Outer.Impl()')
        }
        assert err.message.contains('Cannot access constructor of class: p.Outer$Impl from class: ')
    }

    @Test
    void testConstructorCallRejectedBeforeLookup() {
        // no constructor takes a String, but the type is out of reach before that matters
        def err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate compileStatic("new p.Outer.Impl('x')")
        }
        assert err.message.contains('Cannot access constructor of class: p.Outer$Impl from class: ')
        assert !err.message.contains('Cannot find matching constructor')
    }

    @Test
    void testInheritedStaticMethodViaAccessibleSubclass() {
        // the qualifying type is what must be accessible, not the declaring type
        assert shell.evaluate(compileStatic('p.Pub.inherited()')) == 'inherited'
    }

    @Test
    void testClassLiteralAndClassMethodsUnaffected() {
        assert shell.evaluate(compileStatic('p.Outer.PropertyKeys')).simpleName == 'PropertyKeys'
        assert shell.evaluate(compileStatic('p.Outer.PropertyKeys.getSimpleName()')) == 'PropertyKeys'
    }

    @Test
    void testDynamicAccessUnaffected() {
        assert shell.evaluate('p.Outer.PropertyKeys.values()*.name()') == ['localValueSet', 'other']
        assert shell.evaluate('p.Outer.Impl.hello()') == 'hello'
        assert shell.evaluate('new p.Outer.Impl()') != null
    }
}
