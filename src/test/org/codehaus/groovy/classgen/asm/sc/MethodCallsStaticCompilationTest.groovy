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
package org.codehaus.groovy.classgen.asm.sc;

import groovy.transform.stc.MethodCallsSTCTest
import org.codehaus.groovy.control.MultipleCompilationErrorsException

public class MethodCallsStaticCompilationTest extends MethodCallsSTCTest implements StaticCompilationTestSupport {

    void testReferenceToInaccessiblePrivateProperty() {
        shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                class Main {
                   static main(args) { Peer.xxx }
                }
                class Peer {
                    private static int xxx = 666
                }
            '''
        }
    }

    // GROOVY-7863
    void testDoublyNestedPrivateMethodAccess() {
        assertScript '''
            class Foo {
                class Bar {
                    class Baz {
                        int c() { d() }
                    }
                    int b() { new Baz().c() }
                }
                int a() {
                    new Bar().b()
                }
                private int d() { 123 }
            }
            assert new Foo().a() == 123
        '''
        assert astTrees['Foo$Bar$Baz'][1].contains('INVOKEVIRTUAL Foo.d ()I')
    }
}
