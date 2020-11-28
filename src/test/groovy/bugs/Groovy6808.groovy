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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy6808 {

    @Test // GROOVY-6808
    void testInnerClassTable1() {
        assertScript '''
            class Foo {
                Closure cl = { println 'foo' }
            }

            final Class c = Class.forName('Foo$_closure1')
            assert !c.isAnonymousClass()
            assert  c.isMemberClass()
            assert !c.isLocalClass()
        '''
    }

    @Test // GROOVY-9842
    void testInnerClassTable2() {
        assertScript '''
            class Foo {
                static class Bar {
                    static class Baz {
                    }
                }
            }

            final Class foo = Class.forName('Foo')
            assert foo.getEnclosingClass() == null
            assert foo.getModifiers() == 1
            assert !foo.isAnonymousClass()
            assert !foo.isMemberClass()
            assert !foo.isLocalClass()

            final Class bar = Class.forName('Foo$Bar')
            assert bar.getEnclosingClass() == foo
            assert bar.getModifiers() == 9
            assert !bar.isAnonymousClass()
            assert bar.isMemberClass()
            assert !bar.isLocalClass()

            final Class baz = Class.forName('Foo$Bar$Baz')
            assert baz.getEnclosingClass() == bar
            assert baz.getModifiers() == 9
            assert !baz.isAnonymousClass()
            assert baz.isMemberClass()
            assert !baz.isLocalClass()
        '''
    }
}
