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

import static groovy.test.GroovyAssert.shouldFail

/**
 * Under classic (non-indy) codegen, {@link org.codehaus.groovy.classgen.asm.OptimizingStatementWriter}
 * emits an optimizable statement twice behind a {@code __$stMC} fast/slow guard.
 * When the method has a non-empty (and non-optimizable) {@code finally} block,
 * inlining that block used to unwind the {@code CompileStack} scope permanently,
 * so the second emission resolved a try-block local as {@code this.getProperty(name)}
 * and threw {@code MissingPropertyException} at runtime (only the fast path was
 * correct). This was a regression introduced with GROOVY-4721.
 */
final class Groovy12062 {

    private static CompilerConfiguration nonIndy() {
        def config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        return config
    }

    @Test
    void testTryLocalAsMethodArgWithNonEmptyFinally() {
        new GroovyShell(nonIndy()).evaluate '''
            class C {
                String helper(String s) { 'got:' + s }
                def action() {
                    try {
                        String x = 'VALUE'   // local declared in try block
                        return helper(x)     // optimizable this-call; x passed as argument
                    } finally {
                        Math.random()        // non-empty, non-optimizable finally
                    }
                }
            }
            assert new C().action() == 'got:VALUE'
        '''
    }

    // The faulty fast/slow fork means both branches must be exercised: the slow
    // branch is taken once the static metaclass guard (__$stMC) is set, which is
    // what a metaclass mutation (e.g. Grails interception) does.
    @Test
    void testTryLocalAsMethodArgBothBranches() {
        new GroovyShell(nonIndy()).evaluate '''
            class C {
                String helper(String s) { 'got:' + s }
                def action() {
                    try {
                        String x = 'VALUE'
                        return helper(x)
                    } finally {
                        Math.random()
                    }
                }
            }
            assert new C().action() == 'got:VALUE'   // fast branch (default metaclass)
            C.metaClass.ping = { -> 'pong' }         // flip __$stMC -> slow branch
            assert new C().action() == 'got:VALUE'   // slow branch
        '''
    }

    @Test
    void testTryLocalAsMethodArgImplicitReturn() {
        new GroovyShell(nonIndy()).evaluate '''
            class C {
                String helper(String s) { 'got:' + s }
                def action() {
                    try {
                        String x = 'VALUE'
                        helper(x)            // implicit return
                    } finally {
                        Math.random()
                    }
                }
            }
            assert new C().action() == 'got:VALUE'
        '''
    }

    // GROOVY-4721 must remain in force: a try-block local is out of scope in the
    // finally block, so referencing it there falls back to (failing) property access.
    @Test
    void testTryLocalNotVisibleInFinallyStillHolds() {
        def err = shouldFail {
            new GroovyShell(nonIndy()).evaluate '''
                class C {
                    def action() {
                        try {
                            String x = 'VALUE'
                            return x
                        } finally {
                            println 'x: ' + x
                        }
                    }
                }
                new C().action()
            '''
        }
        assert err.message =~ /No such property: x for class: C/
    }
}
