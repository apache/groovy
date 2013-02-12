/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ArraysAndCollectionsSTCTest

/**
 * Unit tests for static type checking : miscellaneous tests.
 *
 * @author Cedric Champeau
 */
@Mixin(StaticCompilationTestSupport)
class ArraysAndCollectionsStaticCompileTest extends ArraysAndCollectionsSTCTest {

    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testListStarWithMethodReturningVoid() {
        assertScript '''
            class A { void m() {} }
            List<A> elems = [new A(), new A(), new A()]
            List result = elems*.m()
            assert result == [null,null,null]
        '''
    }

    void testListStarWithMethodWithNullInList() {
        assertScript '''
            List<String> elems = ['a',(String)null,'C']
            List<String> result = elems*.toUpperCase()
            assert result == ['A',null,'C']
        '''
    }

    void testShouldNotThrowVerifyError() {
        assertScript '''
            def al = new ArrayList<Double>()
            al.add(2.0d)
            assert al.get(0) + 1 == 3.0d
        '''
    }

    // GROOVY-5654
    void testShouldNotThrowForbiddenAccessWithMapProperty() {
        assertScript '''
            Map<String, Integer> m = ['abcd': 1234]
            assert m['abcd'] == 1234
            assert m.abcd == 1234
        '''
    }

    // GROOVY-5988
    void testMapArraySetPropertyAssignment() {
        assertScript '''
            Map<String, String> props(Object p) {
                Map<String, Object> props = [:]

                for(String property in p.properties.keySet()){
                    props[property] = 'TEST'
                    // I need to use calling put directy to make it work
                    // props.put property, 'TEST'
                }
                props
            }
            def map = props('SOME RANDOM STRING')
            assert map['class'] == 'TEST'
            assert map['bytes'] == 'TEST'
        '''
    }
}

