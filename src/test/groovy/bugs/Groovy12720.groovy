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

import java.beans.Transient

import static groovy.test.GroovyAssert.shouldFail

final class Groovy12720 {

    @Test
    void testOverrideStaysOnTheScriptRunMethod() {
        assert new GroovyShell().evaluate('''
            @Override
            Object run() {
                'done'
            }
        ''') == 'done'
    }

    @Test
    void testOverrideBesideAnAnnotationThatMovesToTheScriptClass() {
        def err = shouldFail '''
            @Override
            @groovy.transform.CompileStatic
            Object run() {
                helper()
            }

            def helper() {
                int n = 'abc'
            }
        '''
        assert err.message.contains('[Static type checking] - Cannot assign value of type java.lang.String to variable of type int')

        assert new GroovyShell().evaluate('''
            @Override
            @groovy.transform.CompileStatic
            Object run() {
                helper()
            }

            def helper() {
                'done'
            }
        ''') == 'done'
    }

    @Test
    void testAnotherMethodOnlyAnnotationStaysOnTheScriptRunMethod() {
        assert new GroovyShell().evaluate('''
            @java.beans.Transient
            Object run() {
                getClass().getMethod('run').isAnnotationPresent(java.beans.Transient)
                    && !getClass().isAnnotationPresent(java.beans.Transient)
            }
        ''')
    }

    @Test
    void testOverrideOnAnotherScriptMethodIsStillChecked() {
        def err = shouldFail '''
            @Override
            Object other() {
            }
        '''
        assert err.message.contains("Method 'other' from class ") && err.message.contains(" does not override method from its superclass or interfaces but is annotated with @Override.")
    }

    @Test
    void testMethodOnlyAnnotationStaysOnTheMainMethod() {
        def loader = new GroovyClassLoader()
        ['void main() {}', 'static void main(String[] args) {}'].each { main ->
            def c = loader.parseClass("@java.beans.Transient\n$main")
            assert !c.isAnnotationPresent(Transient)
            assert c.declaredMethods.find { it.name == 'main' }.isAnnotationPresent(Transient)
        }
    }

    @Test
    void testImportedMethodOnlyAnnotationStaysOnTheScriptRunMethod() {
        ['import java.beans.Transient', 'import java.beans.Transient as Tr'].each { imp ->
            def anno = imp.endsWith('Tr') ? 'Tr' : 'Transient'
            assert new GroovyShell().evaluate("""
                $imp
                @$anno
                Object run() {
                    getClass().getMethod('run').isAnnotationPresent(java.beans.Transient)
                        && !getClass().isAnnotationPresent(java.beans.Transient)
                }
            """)
        }
    }
}
