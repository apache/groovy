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
 * GROOVY-10812: angle brackets on the left of a dot in a static method
 * or property access must be rejected (they are not a method type
 * witness). A constructor of a nested type like {@code new Foo<X>.Bar<Y>()}
 * remains valid.
 */
final class Groovy10812 {

    @Test
    void testTypeArgumentsLeftOfDotOnStaticMethodAreRejected() {
        shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                def c = Comparator<String>.comparing({ String s -> s.length() } as java.util.function.Function)
            '''
        }
    }

    @Test
    void testTypeArgumentsLeftOfDotOnStaticPropertyAreRejected() {
        shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                class X {
                    static String FOO = 'foo'
                }
                def r = X<String>.FOO
            '''
        }
    }

    @Test
    void testNestedConstructorWithTypeArgumentsStillCompiles() {
        assertScript '''
            class Foo<X> {
                class Bar<Y> {}
            }
            assert new Foo<String>.Bar<Integer>() instanceof Foo.Bar
        '''
    }
}
