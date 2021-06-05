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

final class Groovy8048 {

    @Test
    void testFinalFieldInPreCompiledTrait() {
        assertScript """
            class C implements ${getClass().getName()}.Foo, Bar {
                static void main(args) {
                    assert this.otherItems1 && this.otherItems1[0] == 'bar1'
                    assert this.items1 && this.items1[0] == 'foo1'
                    new C().with {
                        assert otherItems2 && otherItems2[0] == 'bar2'
                        assert items2 && items2[0] == 'foo2'
                    }
                }
            }

            trait Bar {
                final static List otherItems1 = ['bar1']
                final List otherItems2 = ['bar2']
            }
        """
    }

    trait Foo {
        final static List items1 = ['foo1']
        final List items2 = ['foo2']
    }
}
