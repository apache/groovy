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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10478 {

    @Test
    void testIndirectInterface() {
        assertScript '''
            trait A {
                final String string = 'works'
            }
            interface B {
            }
            trait C implements A, B {
            }
            @groovy.transform.CompileStatic
            class D implements C {
            }

            // VerifyError: Bad invokespecial instruction: interface method reference is in an indirect superinterface
            //    Location: D.Atrait$super$getString()Ljava/lang/String; @37: invokespecial
            def cls = D.class
            cls.name

            String result = new D().string
            assert result == 'works'
        '''
    }
}
