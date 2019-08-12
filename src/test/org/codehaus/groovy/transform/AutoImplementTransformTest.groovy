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
package org.codehaus.groovy.transform

class AutoImplementTransformTest extends GroovyShellTestCase {

    void testGenericReturnTypes() {
        assertScript '''
            interface HasXs<T> {
                T[] x()
            }

            abstract class HasXsY<E> implements HasXs<Long> {
                abstract E y()
            }

            interface MyIt<T> extends Iterator<T> {}

            @groovy.transform.AutoImplement
            class Foo extends HasXsY<Integer> implements MyIt<String> { }

            def publicMethods = Foo.methods.findAll{ it.modifiers == 1 }.collect{ "$it.returnType.simpleName $it.name" }*.toString()
            assert ['boolean hasNext', 'String next', 'Long[] x', 'Integer y'].every{ publicMethods.contains(it) }
            '''
    }

    void testException() {
        shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException)
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    void testExceptionWithMessage() {
        def message = shouldFail UnsupportedOperationException, '''
            import groovy.transform.*

            @AutoImplement(exception=UnsupportedOperationException, message='Not supported by Foo')
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
        assert message.contains('Not supported by Foo')
    }

    void testClosureBody() {
        shouldFail IllegalStateException, '''
            import groovy.transform.*

            @AutoImplement(code={ throw new IllegalStateException()})
            class Foo implements Iterator<String> { }

            new Foo().hasNext()
        '''
    }

    void testInheritedMethodNotOverwritten() {
        assertScript '''
            class WithNext {
                String next() { 'foo' }
            }

            @groovy.transform.AutoImplement
            class Foo extends WithNext implements Iterator<String> { }
            assert new Foo().next() == 'foo'
        '''
    }

    void testExistingMethodNotOverwritten() {
        assertScript '''
            @groovy.transform.AutoImplement
            class Foo implements Iterator<String> {
                String next() { 'foo' }
            }

            assert new Foo().next() == 'foo'
        '''
    }

}
