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
package groovy

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ImportTest {

    @Test
    void testImportDefaults() {
        assertScript '''
            Object file = new File('x')
            assert file instanceof File
        '''

        assertScript '''
            def map = [foo:'bar']
            assert map instanceof Map
        '''

        assertScript '''
            def list = [1, 2, 3]
            assert list instanceof List
        '''

        assertScript '''
            def decimal = new BigDecimal(0)
            def integer = new BigInteger(0)
        '''
    }

    // GROOVY-8254
    @Test
    void testImportConflicts() {
        assertScript '''
            import java.util.List
            import java.util.List
        '''

        assertScript '''
            import java.util.List
            import java.util.List as List
        '''

        assertScript '''
            import java.util.List
            import java.util.List as MyList
        '''

        assertScript '''
            import java.util.Map.Entry
            import static java.util.Map.Entry
            import static java.util.Map.Entry as Entry
        '''

        assertScript '''
            import java.util.Map.Entry
            class Main {
                static class Entry { }
                static main(array) { }
            }
        '''

        assertScript '''package p
            import p.Main // okay
            class Main {
                static main(array) { }
            }
        '''

        def err = shouldFail '''
            import java.net.Proxy
            import groovy.util.Proxy
        '''
        assert err.message =~ /The name Proxy is already declared/

        GroovyShell shell = GroovyShell.withConfig {
            imports { normal 'java.net.Proxy' }
        }
        err = shouldFail shell, '''
            import groovy.util.Proxy
        '''
        assert err.message =~ /The name Proxy is already declared/

        err = shouldFail '''
            import java.lang.Object as Foo
            import java.lang.Number as Foo
        '''
        assert err.message =~ /The name Foo is already declared/

        err = shouldFail '''
            import java.util.Map.Entry
            import java.lang.Object as Entry
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.lang.Object as Entry
            import static java.util.Map.Entry
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.util.Map.Entry
            class Entry { }
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.util.Map.Entry as Pair
            class Pair { }
        '''
        assert err.message =~ /The name Pair is already declared/

        shell = GroovyShell.withConfig {
            imports { normal 'java.util.Map.Entry' }
        }
        err = shouldFail shell, '''
            class Entry { }
        '''
        assert err.message =~ /The name Entry is already declared/
    }

    // GROOVY-5103
    @Test
    void testImportStaticInnerClass() {
        assertScript '''
            import java.util.Map.Entry
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import static java.util.Map.Entry
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import java.util.Map.*
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import static java.util.Map.*
            Entry entry = [foo:'bar'].entrySet().first()
        '''
    }
}
