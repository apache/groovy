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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Coercing a null value to {@code boolean} with the {@code as} operator gives
 * {@code false}, with invokedynamic and with classic call sites.
 */
final class Groovy12834 {

    private static GroovyShell shell(boolean indy) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', indy)
        new GroovyShell(config)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    void testNullAsBoolean(boolean indy) {
        shell(indy).evaluate '''
            class Holder {
                Object value
            }
            boolean present(Holder h) {
                h.value as boolean
            }
            assert !present(new Holder())
            assert present(new Holder(value: 'x'))
            assert !present(new Holder(value: ''))
        '''
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    void testNullAsBooleanCompileStatic(boolean indy) {
        shell(indy).evaluate '''
            import groovy.transform.CompileStatic

            class Holder {
                Object value
            }
            @CompileStatic
            boolean present(Holder h) {
                h.value as boolean
            }
            assert !present(new Holder())
            assert present(new Holder(value: 'x'))
            assert !present(new Holder(value: ''))
        '''
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    void testAsPrimitiveNumber(boolean indy) {
        shell(indy).evaluate '''
            int number(Object value) {
                value as int
            }
            char character(Object value) {
                value as char
            }
            assert number('42') == 42
            assert number(4.2) == 4
            assert character('x') == 'x'
        '''
    }
}
