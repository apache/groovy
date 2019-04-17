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
 * Unit tests for static type checking : closure parameter type inference for {@link org.codehaus.groovy.runtime.SocketGroovyMethods}.
 */
class SocketGMClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {
    void testAccept() {
        assertScript '''
            void foo(ServerSocket s) {
                s.accept {
                    assert !it.closed
                }
            }
            void bar(ServerSocket s) {
                s.accept(true) {
                    assert !it.closed
                }
            }
            ServerSocket s = [isBound:{true}, accept: {[:] as Socket}] as ServerSocket
            foo(s)
            bar(s)
        '''
    }

    void testWithObjectStreams() {
        assertScript '''
            Socket s = null
            s?.withObjectStreams { inS, out ->
                out.writeInt(inS.readInt())
            }
        '''
    }

    void testWithStreams() {
        assertScript '''
            Socket s = null
            s?.withStreams { inS, out ->
                out.write(inS.read())
            }
        '''
    }
}
