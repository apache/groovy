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
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy9059 {

    @Test
    void testTypeParameterWithExtends1() {
        assertScript '''
            interface Face<T> {
                public <O extends T> O process(O o)
            }

            def impl = new Face<CharSequence>() {
                @Override
                public <Chars extends CharSequence> Chars process(Chars chars) { chars }
            }
        '''
    }

    @Test
    void testTypeParameterWithExtends2() {
        assertScript '''
            interface Face<T> {
                public <O extends T> O process(O o)
            }

            def impl = new Face<CharSequence>() {
                @Override @SuppressWarnings('unchecked')
                public CharSequence process(CharSequence chars) { chars }
            }
        '''
    }

    @Test
    void testTypeParameterWithExtends3() {
        assertScript '''
            interface Face<T> {
                public <O extends T> O process(O o)
            }

            def impl = new Face<String>() {
                @Override @SuppressWarnings('unchecked')
                public String process(String string) { string }
            }
        '''
    }
}
