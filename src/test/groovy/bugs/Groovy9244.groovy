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

import groovy.transform.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9244 {

    @Test
    void testSuperConstructorsAndNullArgument1() {
        assertScript '''
            abstract class Base {
                Base(Number n) { which = 'Number' }
                Base(String s) { which = 'String' }
                public String which
            }

            class Impl extends Base {
                Impl(Number n) { super((Number) n) }
                Impl(String s) { super((String) s) }
            }

            def impl = new Impl((String) null)
            assert impl.which == 'String'
        '''
    }

    @Test
    void testSuperConstructorsAndNullArgument2() {
        assertScript '''
            abstract class Base {
                Base(Number n) { which = 'Number' }
                Base(String s) { which = 'String' }
                public String which
            }

            @groovy.transform.CompileStatic
            class Impl extends Base {
                Impl(Number n) { super(n) }
                Impl(String s) { super(s) }
            }

            def impl = new Impl((String) null)
            assert impl.which == 'String'
        '''
    }

    @Test
    void testSuperConstructorsAndNullArgument3() {
        assertScript '''
            abstract class Base {
                Base(Number n) { which = 'Number' }
                Base(String s) { which = 'String' }
                public String which
            }

            @groovy.transform.InheritConstructors
            class Impl extends Base {
            }

            def impl = new Impl((String) null)
            assert impl.which == 'String'
        '''
    }

    @Test @NotYetImplemented
    void testSuperConstructorsAndNullArgument4() {
        assertScript '''
            abstract class Base {
                Base(Number n) { which = 'Number' }
                Base(String s) { which = 'String' }
                public String which
            }

            class Impl extends Base {
                Impl(Number n) { super(n) }
                Impl(String s) { super(s) }
            }

            def impl = new Impl((String) null)
            assert impl.which == 'String'
        '''
    }

    @Test @NotYetImplemented
    void testSuperConstructorsAndNullArgument5() {
        assertScript '''
            abstract class Base {
                Base(Number n) { which = 'Number' }
                Base(String s) { which = 'String' }
                public String which
            }

            def impl = new Base((String) null) {}
            assert iompl.which == 'String'
        '''
    }
}
