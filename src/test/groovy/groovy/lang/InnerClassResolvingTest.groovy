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
package groovy.lang

import groovy.test.GroovyTestCase

class InnerClassResolvingTest extends GroovyTestCase {
    void testInnerClass() {
        // Thread.UncaughtExceptionHandler was added in Java 1.5
        def script = '''
            def caught = false
            def t = Thread.start {
                Thread.setDefaultUncaughtExceptionHandler(
                    {thread,ex -> caught=true} as Thread.UncaughtExceptionHandler)
                throw new Exception("huhu")
            }
            t.join()
            assert caught==true
        '''
        new GroovyShell().evaluate(script)
    }

    void testInnerClassWithPartialMatchOnImport() {
        def script = '''
            import java.lang.Thread as X
            X.UncaughtExceptionHandler y = null
        '''
        new GroovyShell().evaluate(script)
    }

    // GROOVY-8362
    void 'test do not resolve nested class via inner class with package name'() {
        shouldFail '''
            package bugs

            class Current {
                static class bugs {
                    static class Target {}
                }
                static usage() {
                    new Target()
                }
            }
            assert Current.usage()
        '''
    }
}
