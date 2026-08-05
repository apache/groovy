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
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

/**
 * Follow-up to GROOVY-11982: under {@code indy=false} the call-site array
 * prologue ({@code INVOKESTATIC $getCallSiteArray()}) is emitted at the top
 * of every method body before the body is visited, so an interface method
 * whose body registers no call sites (e.g. {@code return null}) still
 * references the synthetic helper class ({@code MyInterface$1}) that owns
 * the prologue for interfaces. The helper must be materialised whenever a
 * prologue was emitted — not only when named call sites were registered —
 * otherwise the first invocation throws
 * {@code NoClassDefFoundError: MyInterface$1}.
 */
final class Groovy12235 {

    @Test
    void testInterfaceDefaultMethodWithoutDynamicCodeNonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            interface MyInterface {
                default Object defaultValue() {
                    return null
                }
            }
            class MyImpl implements MyInterface {
            }
            assert new MyImpl().defaultValue() == null
        '''
    }

    @Test
    void testInterfaceStaticMethodWithoutDynamicCodeNonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            interface Util {
                static Object nothing() {
                    return null
                }
            }
            assert Util.nothing() == null
        '''
    }
}
