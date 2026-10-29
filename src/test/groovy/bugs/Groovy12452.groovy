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
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * The accessor of a record component whose name looks like a getter, such as
 * {@code isActive} or {@code getName}, must resolve under static type checking
 * when the record is compiled in the same compilation unit. A precompiled record
 * was never affected: its accessor is an ordinary method on the loaded class.
 */
final class Groovy12452 {

    @Test
    void testAccessorWithIsPrefix() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            record Flags(boolean isActive) {
                boolean check() { isActive() }
            }

            @CompileStatic
            class Main {
                static boolean test() { new Flags(true).isActive() }
            }

            assert Main.test()
            assert new Flags(true).check()
        '''
    }

    @Test
    void testAccessorWithGetPrefix() {
        assertScript '''
            import groovy.transform.TypeChecked

            record Person(String getName) {}

            @TypeChecked
            String test() { new Person('fred').getName() }

            assert test() == 'fred'
        '''
    }

    @Test
    void testAccessorWithIsPrefixAndEmulatedRecord() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.RecordOptions
            import groovy.transform.RecordTypeMode

            @RecordOptions(mode=RecordTypeMode.EMULATE)
            record Flags(boolean isActive) {}

            @CompileStatic
            boolean test() { new Flags(true).isActive() }

            assert test()
        '''
    }

    @Test
    void testAccessorOfSimilarlyNamedComponents() {
        assertScript '''
            import groovy.transform.CompileStatic

            record R(String active, int isActive) {}

            @CompileStatic
            def test() {
                def r = new R('a', 7)
                String s = r.active()
                int n = r.isActive()
                [s, n]
            }

            assert test() == ['a', 7]
        '''
    }

    @Test
    void testNoIsserForBooleanComponent() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            import groovy.transform.CompileStatic

            record R(boolean active) {}

            @CompileStatic
            boolean test() { new R(true).isActive() }
        '''
        assert err.message.contains('Cannot find matching method R#isActive()')
    }
}
