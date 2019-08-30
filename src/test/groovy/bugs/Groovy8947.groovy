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
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy8947 {

    @Test
    void testResolvingNonStaticInnerClass() {
        assertScript '''
            public class Computer {
                public class Cpu {
                    int coreNumber
                    public Cpu(int coreNumber) {
                        this.coreNumber = coreNumber
                    }
                }
                public static newCpuInstance(int coreNumber) {
                    return new Computer().new Cpu(coreNumber)
                }
            }

            assert 4 == new Computer().new Cpu(4).coreNumber
            assert 4 == Computer.newCpuInstance(4).coreNumber
            assert 0 == new HashSet(new ArrayList()).size()
        '''
    }

    @Test
    void testResolvingNonStaticInnerClass2() {
        assertScript '''
            public class Computer {
                public class Cpu {
                    int coreNumber
                    public Cpu(int coreNumber) {
                        this.coreNumber = coreNumber
                    }
                }
                static newComputerInstance() {
                    return new Computer()
                }
                static newCpuInstance(int coreNumber) {
                    // `new Cpu(coreNumber)` is inside of the outer class `Computer`, so we can resolve `Cpu`
                    return newComputerInstance().new Cpu(coreNumber)
                }
            }

            assert 4 == Computer.newCpuInstance(4).coreNumber
        '''
    }

    @Test
    void testResolvingNonStaticInnerClass3() {
        def err = shouldFail '''
            public class Computer {
                public class Cpu {
                    int coreNumber

                    public Cpu(int coreNumber) {
                        this.coreNumber = coreNumber
                    }
                }
            }
            def newComputerInstance() {
                return new Computer()
            }

            // `new Cpu(4)` is outside of outer class `Computer` and the return type of `newComputerInstance()` can not be resolved,
            // so we does not support this syntax outside of outer class
            assert 4 == newComputerInstance().new Cpu(4).coreNumber
        '''

        assert err =~ 'unable to resolve class Cpu'
    }
}
