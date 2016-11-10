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

class Groovy6276Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testOuterClassMethodCall() {
        assertScript '''class Outer {
    private int outerField = 1

    private int outerMethod() { 2 }
    int outerProperty = 3
    class Inner {
        void assertions() {
            assert outerField == 1            // #1
            assert outerMethod() == 2         // #2
            assert outerProperty == 3         // #3
            assert getOuterProperty() == 3    // #4
        }
    }

    void test() {
        new Inner().assertions()
    }
}

new Outer().test()
    '''
    }

    void testAccessPrivateMethodFromClosure() {
        assertScript '''
    class Outer {
        private int foo(int x) {
            2*x
        }

        public int bar() {
            (Integer) [1,2,3].collect {
                foo(it)
            }.sum()
        }
    }
    new Outer().bar()
'''
    }
}
