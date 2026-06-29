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
package typing

import groovy.transform.CompileStatic
import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

/**
 * Documentation snippets for the {@code @ClassTag} section of the static type checking
 * documentation (GROOVY-12115).
 */
final class ClassTagSpecTest extends StaticTypeCheckingTestCase {

    @Test
    void testAdditiveInjection() {
        assertScript '''
            // tag::classtag_additive[]
            List<String> names = []
            List<String> checked = names.asChecked()        // <1>
            checked << 'Alice'                              // <2>
            // end::classtag_additive[]
            assert names == ['Alice']
            boolean threw = false
            try {
                ((List) checked).add(42)
            } catch (ClassCastException expected) {
                threw = true
            }
            assert threw
        '''
    }

    @Test
    void testDeclaringTaggedApi() {
        assertScript '''
            // tag::classtag_declare[]
            import groovy.transform.stc.ClassTag

            class Registry<T> {
                Map<String, T> byName = [:]
                T create(String name, @ClassTag Class<T> type) {    // <1>
                    T instance = type.getDeclaredConstructor().newInstance()
                    byName[name] = instance
                    instance
                }
            }

            Registry<StringBuilder> registry = new Registry<>()
            registry.create('first')                                // <2>
            // end::classtag_declare[]
            assert registry.byName['first'] instanceof StringBuilder
        '''
    }

    @Test
    void testPreemptiveUpgrade() {
        assertScript '''
            // tag::classtag_preempt[]
            Map<String, Integer> counts = [:]
            def wordCounts = counts.withDefault { 0 }       // <1>
            assert wordCounts['alpha'] == 0                 // <2>
            // end::classtag_preempt[]
            boolean threw = false
            try {
                ((Map) wordCounts).get(42)                  // wrong-typed key rejected by the checked view
            } catch (ClassCastException expected) {
                threw = true
            }
            assert threw
        '''
    }

    @Test
    void testGlobalDisable() {
        // tag::classtag_disable[]
        def config = new CompilerConfiguration()
        config.classTagPreemptionDisabled = true            // <1>
        // end::classtag_disable[]
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        def lenient = new GroovyShell(config).evaluate '''
            Map<String, Integer> counts = [:]
            def wordCounts = counts.withDefault { 0 }
            ((Map) wordCounts).get(42) == 0                 // lenient again: no checked view
        '''
        assert lenient
    }
}
