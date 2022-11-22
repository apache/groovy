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

final class Groovy7584 {

    @Test
    void testTraitFieldModifiersAreRetained() {
        assertScript '''
            import groovy.transform.ToString
            import static java.lang.reflect.Modifier.*

            trait User {
                final String name = 'Foo'
                final private transient int answer = 42
                final public static boolean LOGGED_IN = true
            }

            @ToString(allProperties=true, includeNames=true)
            class Person implements User {
            }

            assert Person.User__LOGGED_IN
            assert new Person().toString() == 'Person(name:Foo)'

            def loggedInField = Person.declaredFields.find {
                it.name.contains('LOGGED_IN')
            }
            assert isFinal(loggedInField.modifiers)
            assert isPublic(loggedInField.modifiers)
            assert isStatic(loggedInField.modifiers)

            def answerField = Person.declaredFields.find {
                it.name.contains('answer')
            }
            assert isFinal(answerField.modifiers)
            assert isPrivate(answerField.modifiers)
            assert isTransient(answerField.modifiers)
        '''
    }
}
