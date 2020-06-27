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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy7300 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testUseSuperToBypassOverride1() {
        assertScript '''
            abstract class A {
                protected x = 1
                def getX() { 2 }
            }
            class B extends A {
                @Override
                def getX() { super.x }
            }
            assert new B().getX() == 1 // TODO: Why use A#x and not A#getX?
        '''
    }

    void testUseSuperToBypassOverride1a() {
        assertScript '''
            abstract class A {
                protected x = 1
                def getX() { 2 }
            }
            class B extends A {
                @Override
                def getX() { super.@x }
            }
            assert new B().getX() == 1
        '''
    }

    void testUseSuperToBypassOverride2() {
        assertScript '''
            abstract class A {
                private x = 1
                def getX() { 2 }
            }
            class B extends A {
                @Override
                def getX() { super.x }
            }
            assert new B().getX() == 2
        '''
    }

    void testUseSuperToBypassOverride2a() {
        shouldFailWithMessages '''
            abstract class A {
                private x = 1
                def getX() { 2 }
            }
            class B extends A {
                @Override
                def getX() { super.@x }
            }
            assert false
        ''', 'The field A.x is not accessible'
    }
}
