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

import groovy.test.GroovyTestCase

class Groovy8833Bug extends GroovyTestCase {
    void testEqualsAndHashCodeAndToStringWithCompileStatic() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            @Canonical
            class Person1 {
                String first, last
            }

            @Canonical
            class Person2 {
                String first, last
            }

            @CompileStatic
            @ToString
            @TupleConstructor
            @EqualsAndHashCode
            class Person3 {
                String first, last
            }

            def h1 = new Person1("a", "b").hashCode()
            def h2 = new Person2("a", "b").hashCode()
            def h3 = new Person3("a", "b").hashCode()
            assert h1 == h2 && h2 == h3
        '''
    }
}
