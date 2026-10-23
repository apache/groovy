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
import static groovy.test.GroovyAssert.shouldFail

/**
 * A split property definition is a field and a property with the same name
 * and type. The field part may have an explicit visibility modifier or be
 * annotated with {@code @PackageScope}, in either order, and only one part
 * may have an initial value.
 */
final class Groovy12475 {

    private static final String CHECK = '''
        import java.lang.reflect.Modifier
        def check(int modifiers) {
            def field = C.getDeclaredField('foo')
            assert (field.modifiers & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == modifiers
            assert Modifier.isPublic(C.getMethod('getFoo').modifiers)
            assert C.declaredFields.findAll { !it.synthetic }*.name == ['foo']
        }
    '''

    private static void assertSplit(String declarations, Object value, int modifiers = 0) {
        assertScript """
            class C {
                $declarations
            }
            $CHECK
            check($modifiers)
            def c = new C()
            assert c.foo == ${value == null ? 'null' : "'$value'"}
            assert c.@foo == c.foo
            assert c.getFoo() == c.foo
            c.foo = 'baz'
            assert c.@foo == 'baz'
        """
    }

    @Test
    void testPackageScopeFieldAfterProperty() {
        assertSplit '''
            String foo
            @groovy.transform.PackageScope String foo
        ''', null
        assertSplit '''
            String foo = 'bar'
            @groovy.transform.PackageScope String foo
        ''', 'bar'
        assertSplit '''
            String foo
            @groovy.transform.PackageScope String foo = 'bar'
        ''', 'bar'
    }

    @Test
    void testPackageScopeFieldBeforeProperty() {
        assertSplit '''
            @groovy.transform.PackageScope String foo
            String foo
        ''', null
        assertSplit '''
            @groovy.transform.PackageScope String foo
            String foo = 'bar'
        ''', 'bar'
        assertSplit '''
            @groovy.transform.PackageScope String foo = 'bar'
            String foo
        ''', 'bar'
    }

    @Test
    void testDefAndObjectAreTheSameType() {
        assertSplit '''
            def foo = 'bar'
            @groovy.transform.PackageScope Object foo
        ''', 'bar'
        assertSplit '''
            @groovy.transform.PackageScope def foo
            Object foo = 'bar'
        ''', 'bar'
        assertSplit '''
            var foo = 'bar'
            @groovy.transform.PackageScope Object foo
        ''', 'bar'
        assertSplit '''
            def foo = 'bar'
            protected Object foo
        ''', 'bar', 4 // protected
        assertSplit '''
            protected var foo
            java.lang.Object foo = 'bar'
        ''', 'bar', 4
    }

    @Test
    void testValAndObjectAreTheSameType() {
        assertScript """
            class C {
                val foo = 'bar'
                @groovy.transform.PackageScope Object foo
            }
            $CHECK
            check(0)
            assert new C().foo == 'bar'
        """
        assertScript """
            class C {
                protected Object foo
                val foo = 'bar'
            }
            $CHECK
            check(4)
            assert new C().foo == 'bar'
        """
    }

    @Test
    void testInitialValueOnBothParts() {
        def err = shouldFail '''
            class C {
                String foo = 'bar'
                @groovy.transform.PackageScope String foo = 'baz'
            }
        '''
        assert err.message.contains("The split property definition named 'foo' must not have an initial value for both the field and the property")
    }

    @Test
    void testDifferentTypes() {
        for (declarations in ['''
                String foo
                @groovy.transform.PackageScope Integer foo
            ''', '''
                @groovy.transform.PackageScope Integer foo
                String foo
            ''', '''
                def foo
                protected String foo
            ''']) {
            def err = shouldFail """
                class C {
                    $declarations
                }
            """
            assert err.message.contains("The split property definition named 'foo' must not have different types for the field and the property")
        }
    }

    @Test
    void testSynchronizedAccessors() {
        assertScript '''
            import groovy.transform.Synchronized
            import java.lang.reflect.Modifier

            class C {
                private String foo
                @Synchronized String foo
            }
            def c = new C(foo: 'bar')
            assert c.foo == 'bar'
            assert Modifier.isPrivate(C.getDeclaredField('foo').modifiers)
            assert C.declaredFields.count { it.name == 'foo' } == 1
        '''
    }
}
