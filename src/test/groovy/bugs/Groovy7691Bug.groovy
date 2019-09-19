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

class Groovy7691Bug extends GroovyTestCase {
//    @NotYetImplemented
    void testCovariantGenericField() {
        assertScript '''
            @groovy.transform.CompileStatic
            abstract class AbstractNumberWrapper<S extends Number> {
                protected final S number;

                AbstractNumberWrapper(S number) {
                    this.number = number
                }
            }
            @groovy.transform.CompileStatic
            class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {
                LongWrapper(S longNumber) {
                    super(longNumber)
                }

                S getValue() {
                    return number;
                }
            }
            assert new LongWrapper<Long>(42L).value == 42L
        '''
    }

    void testCovariantGenericProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            abstract class AbstractNumberWrapper<S extends Number> {
                def S number;

                AbstractNumberWrapper(S number) {
                    this.number = number
                }
            }
            @groovy.transform.CompileStatic
            class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {
                LongWrapper(S longNumber) {
                    super(longNumber)
                }

                S getValue() {
                    return number;
                }
            }
            assert new LongWrapper<Long>(42L).value == 42L
        '''
    }

    // this test can pass even if GROOVY-7691 is not resolved, just for ensuring the common case will pass test all the time!
    void testCovariantGenericMethod() {
        assertScript '''
            @groovy.transform.CompileStatic
            abstract class AbstractNumberWrapper<S extends Number> {
                protected final S number;

                AbstractNumberWrapper(S number) {
                    this.number = number
                }
                
                protected S getNumber() { return number; }
            }
            @groovy.transform.CompileStatic
            class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {
                LongWrapper(S longNumber) {
                    super(longNumber)
                }

                S getValue() {
                    return getNumber();
                }
            }
            assert new LongWrapper<Long>(42L).value == 42L
        '''
    }
}
