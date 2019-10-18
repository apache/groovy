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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy8444 {

    @Test
    void testAccessingEnumConstantInSwitchCase() {
        assertScript '''\
            enum SomeEnum {
                A, B
            }
            @groovy.transform.CompileStatic
            def meth(SomeEnum e) {
                switch (e) {
                    case A: return 1
                    case B: return 2
                }
            }
            assert 1 == meth(SomeEnum.A)
            assert 2 == meth(SomeEnum.B)
        '''
    }

    @Test
    void testAccessingEnumConstantInNestedSwitchCase() {
        assertScript '''\
            enum SomeEnum {
                A, B
            }
            @groovy.transform.CompileStatic
            def meth(SomeEnum e) {
                switch (e) {
                    case A: 
                        switch(e) {
                            case A: return 1.1
                            case B: return 1.2
                        }
                    case B:
                        switch(e) {
                            case A: return 2.1
                            case B: return 2.2
                        }
                }
            }
            assert 1.1 == meth(SomeEnum.A)
            assert 2.2 == meth(SomeEnum.B)
        '''
    }

    @Test
    void testAccessingEnumConstantInNestedSwitchCase2() {
        assertScript '''\
            enum SomeEnum {
                A, B
            }
            enum OtherEnum {
                C, D
            }
            @groovy.transform.CompileStatic
            def meth(SomeEnum e, OtherEnum e2) {
                switch (e) {
                    case A: 
                        switch(e2) {
                            case C: return 1.1
                            case D: return 1.2
                        }
                    case B:
                        switch(e2) {
                            case C: return 2.1
                            case D: return 2.2
                        }
                }
            }
            assert 1.1 == meth(SomeEnum.A, OtherEnum.C)
            assert 1.2 == meth(SomeEnum.A, OtherEnum.D)
            assert 2.1 == meth(SomeEnum.B, OtherEnum.C)
            assert 2.2 == meth(SomeEnum.B, OtherEnum.D)
        '''
    }
}
