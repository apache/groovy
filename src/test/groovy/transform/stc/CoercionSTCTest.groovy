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
        def m() {
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
        }

        m()
        '''
    }
}

