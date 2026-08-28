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
 * GROOVY-12310: a member of an inaccessible declaring type -- here a public
 * enum constant of a package-private nested enum, the {@code jakarta.faces}
 * {@code UIInput.PropertyKeys} shape -- must be rejected by the type checker
 * instead of compiling to a direct reference that fails at runtime with
 * {@code IllegalAccessError}.
 */
final class Groovy12310 {

    private GroovyShell shell

    @BeforeEach
    void setUp() {
        def loader = new GroovyClassLoader()
        loader.parseClass '''
            package p
            class Outer {
                @groovy.transform.PackageScope
                enum PropertyKeys { localValueSet, other }
            }
        '''
        shell = new GroovyShell(loader)
    }

    @Test
    void testDynamicAccessUnaffected() {
        assert shell.evaluate('p.Outer.PropertyKeys.localValueSet').name() == 'localValueSet'
        assert shell.evaluate('p.Outer.PropertyKeys.@localValueSet').name() == 'localValueSet'
        assert shell.evaluate("p.Outer.PropertyKeys['localValueSet']").name() == 'localValueSet'
    }

    @Test
    void testStaticClassReferenceAndSubscriptUnaffected() {
        assert shell.evaluate('''
            @groovy.transform.CompileStatic
            def f() { p.Outer.PropertyKeys }
            f()
        ''').simpleName == 'PropertyKeys'
        assert shell.evaluate('''
            @groovy.transform.CompileStatic
            def f() { p.Outer.PropertyKeys['localValueSet'] }
            f()
        ''').name() == 'localValueSet'
    }

    @Test
    void testStaticPropertyAccessRejected() {
        // was: compiled to a direct field reference, then at runtime
        // "IllegalAccessError: failed to access class p.Outer$PropertyKeys"
        def err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate '''
                @groovy.transform.CompileStatic
                def f() { p.Outer.PropertyKeys.localValueSet }
            '''
        }
        assert err.message.contains('Cannot access field: localValueSet of class: p.Outer$PropertyKeys')
    }

    @Test
    void testStaticAttributeAccessRejected() {
        def err = shouldFail MultipleCompilationErrorsException, {
            shell.evaluate '''
                @groovy.transform.CompileStatic
                def f() { p.Outer.PropertyKeys.@localValueSet }
            '''
        }
        assert err.message.contains('Cannot access field: localValueSet of class: p.Outer$PropertyKeys')
    }

    @Test
    void testMakeDynamicExtensionEscapeHatch() {
        // an extension may resolve the reference dynamically; the accessibility
        // rejection must leave that route open (the identifier stays in source,
        // the emission goes through the dynamic runtime)
        def extDir = File.createTempDir()
        new File(extDir, 'Groovy12310Extension.groovy').text = '''
            unresolvedProperty { pexp ->
                if (pexp.propertyAsString == 'localValueSet') {
                    makeDynamic(pexp)
                    handled = true
                }
            }
        '''
        shell.classLoader.addClasspath(extDir.absolutePath)
        def result = shell.evaluate '''
            @groovy.transform.CompileStatic(extensions='Groovy12310Extension.groovy')
            def f() { p.Outer.PropertyKeys.localValueSet }
            f()
        '''
        assert result.name() == 'localValueSet'
    }
}
