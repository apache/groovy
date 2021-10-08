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
package groovy.transform.stc

/**
 * Unit tests for static type checking : coercions.
 */
class CoercionSTCTest extends StaticTypeCheckingTestCase {

    void testCoerceToArray() {
        assertScript '''
            try {
                throw new Exception()
            } catch (Throwable t) {
                def newTrace = []
                def clean = newTrace.toArray(newTrace as StackTraceElement[])
                // doing twice, because bug showed that the more you call the array coercion, the more the error
                // gets stupid :
                // Cannot call java.util.List#toArray([Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // Cannot call java.util.List#toArray([[Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // Cannot call java.util.List#toArray([[[Ljava.lang.Object;) with arguments [[Ljava.lang.StackTraceElement; -> [Ljava.lang.StackTraceElement;]
                // ...
                clean = newTrace.toArray(newTrace as StackTraceElement[])
            }
        '''
    }

    // GROOVY-6802
    void testCoerceToBool1() {
        assertScript '''
            boolean b = [new Object()]
            assert b
        '''
        assertScript '''
            boolean b = [false]
            assert b
        '''
        assertScript '''
            boolean b = [true]
            assert b
        '''
        assertScript '''
            boolean b = ['x']
            assert b
        '''
        assertScript '''
            boolean b = [:]
            assert !b
        '''
        assertScript '''
            boolean b = []
            assert !b
        '''
    }

    void testCoerceToBool2() {
        assertScript '''
            Boolean b = [new Object()]
            assert b
        '''
        assertScript '''
            Boolean b = [false]
            assert b
        '''
        assertScript '''
            Boolean b = [true]
            assert b
        '''
        assertScript '''
            Boolean b = ['x']
            assert b
        '''
        assertScript '''
            Boolean b = [:]
            assert !b
        '''
        assertScript '''
            Boolean b = []
            assert !b
        '''
    }

    void testCoerceToClass() {
        assertScript '''
            Class c = 'java.lang.String'
            assert String.class
        '''
        shouldFailWithMessages '''
            Class c = []
        ''', 'No matching constructor found: java.lang.Class()'
        shouldFailWithMessages '''
            Class c = [:]
        ''', 'No matching constructor found: java.lang.Class(java.util.LinkedHashMap)'
    }

    // GROOVY-6803
    void testCoerceToString() {
        assertScript '''
            String s = ['x']
            assert s == '[x]'
        '''
        assertScript '''
            String s = [:]
            assert s == '[:]'
        '''
        assertScript '''
            String s = []
            assert s == '[]'
        '''
    }
}
