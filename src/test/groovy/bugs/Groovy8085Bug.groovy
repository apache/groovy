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

class Groovy8085Bug extends GroovyTestCase {
    void testTryCatchFinally() {
        assertScript '''
            try {
                try {
                    true
                } finally {
                    99 / 0
                }
            } catch (Exception e) {
                System.out.println("catch!!!");
            }
        '''
    }

    void testTryCatchFinally2() {
        assertScript '''
            def visitSequence = []
            try {
                try {
                    true
                } finally {
                    visitSequence << 'innerFinally'
                    99 / 0
                }
            } catch (Exception e) {
                visitSequence << 'outerCatch'
                System.out.println("catch!!!");
            } finally {
                visitSequence << 'outerFinally'
            }
            
            assert ['innerFinally', 'outerCatch', 'outerFinally'] == visitSequence
        '''
    }

    void testTryCatchFinallyWithExplicitReturn() {
        assertScript '''
            try {
                try {
                    return true
                } finally {
                    99 / 0
                }
            } catch (Exception e) {
                System.out.println("catch!!!");
            }
        '''
    }

    void testTryCatchFinallyWithExplicitReturn2() {
        assertScript '''
            def visitSequence = []
            try {
                try {
                    return true
                } finally {
                    visitSequence << 'innerFinally'
                    99 / 0
                }
            } catch (Exception e) {
                visitSequence << 'outerCatch'
                System.out.println("catch!!!");
            } finally {
                visitSequence << 'outerFinally'
            }
            
            assert ['innerFinally', 'outerCatch', 'outerFinally'] == visitSequence
        '''
    }
}
