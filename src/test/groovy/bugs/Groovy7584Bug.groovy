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

import groovy.test.GroovyTestCase

class Groovy7584Bug extends GroovyTestCase {
    void testTraitFieldModifiersAreRetained() {
        assertScript """
            import static java.lang.reflect.Modifier.*

            trait User {
                final String name = 'Foo'
                public static final boolean loggedIn = true
                private transient final int ANSWER = 42
            }

            @groovy.transform.ToString(includeFields=true, includeNames=true)
            class Person implements User { }

            def tos = new Person().toString()
            assert tos.contains('name:Foo')
            assert tos.contains('User__ANSWER:42')
            assert tos.contains('User__name:Foo')

            def loggedInField = Person.getDeclaredFields().find {
                it.name.contains('loggedIn')
            }
            assert isPublic(loggedInField.modifiers)
            assert isFinal(loggedInField.modifiers)
            assert isStatic(loggedInField.modifiers)
            assert Person.User__loggedIn

            def answerField = Person.getDeclaredFields().find {
                it.name.contains('ANSWER')
            }
            assert isPrivate(answerField.modifiers)
            assert isTransient(answerField.modifiers)
            assert isFinal(answerField.modifiers)
        """
    }
}
