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
 * An interface default method whose body uses dynamic Groovy features (e.g. a
 * {@code GString} or a dynamic method call) needs the call-site array. Under
 * {@code indy=false} the bytecode prologue emitted at the top of the method
 * is {@code INVOKESTATIC $getCallSiteArray()}; the owner of that call must be
 * the synthetic helper class (not the interface itself), and the helper must
 * actually be materialised. Otherwise the JVM throws
 * {@code IncompatibleClassChangeError} at first invocation because a
 * {@code Methodref} cannot resolve to an interface owner.
 */
final class Groovy11982 {

    @Test
    void testInterfaceDefaultMethodWithGStringNonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            interface IConfig {
                String getName()
                default String getDescription() {
                    "config[name=${getName()}]"
                }
            }
            class ConfigImpl implements IConfig {
                String getName() { 'impl' }
            }
            assert new ConfigImpl().description == 'config[name=impl]'
        '''
    }

    @Test
    void testInterfaceDefaultMethodCallingOtherDefaultNonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            interface IConfig {
                String getName()
                default String getDescription() {
                    "config[name=${getName()}]"
                }
                default String greet() {
                    "greeted as ${getDescription()}"
                }
            }
            class ConfigImpl implements IConfig {
                String getName() { 'impl' }
            }
            assert new ConfigImpl().greet() == 'greeted as config[name=impl]'
        '''
    }

    @Test
    void testStaticConsumerCallsDefaultMethodNonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            interface IConfig {
                String getName()
                default String getDescription() {
                    "config[name=${getName()}]"
                }
            }
            class ConfigImpl implements IConfig {
                String getName() { 'impl' }
            }
            @groovy.transform.CompileStatic
            class StaticConsumer {
                static String describe(IConfig c) { c.description }
            }
            assert StaticConsumer.describe(new ConfigImpl()) == 'config[name=impl]'
        '''
    }
}
