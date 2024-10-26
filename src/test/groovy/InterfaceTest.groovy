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
package groovy

import gls.CompilableTestSupport
import org.codehaus.groovy.control.CompilationUnit

final class InterfaceTest extends CompilableTestSupport {

    void testGenericsInInterfaceMembers() {
        // control
        shouldCompile '''
            interface I {
                def <T>                      T m1(T x)
                def <U extends CharSequence> U m2(U x)
                def <V, W>                   V m3(W x)
                def <N extends Number>    void m4(   )
            }
        '''
        // erroneous
        shouldNotCompile 'interface I { def <?> m(x) }'
        shouldNotCompile 'interface I { def <? extends CharSequence> m(x) }'
    }

    // GROOVY-5106
    void testReImplementsInterface1() {
        new CompilationUnit(new GroovyClassLoader(this.class.classLoader)).with {
            addSource 'X', '''
                interface I<T> {}
                interface J<T> extends I<T> {}
                class X implements I<String>, J<Number> {}
            '''
            compile()

            assert errorCollector.errorCount == 0
            assert errorCollector.warningCount == 1
            assert errorCollector.warnings[0].message == 'The interface I is implemented more than once with different arguments: I <String> and I <java.lang.Number>'
        }
    }

    // GROOVY-5106
    void testReImplementsInterface2() {
        new CompilationUnit(new GroovyClassLoader(this.class.classLoader)).with {
            addSource 'X', '''
                interface I<T> {}
                class X implements I<Number> {}
                class Y extends X implements I<String> {}
            '''
            compile()

            assert errorCollector.errorCount == 0
            assert errorCollector.warningCount == 1
            assert errorCollector.warnings[0].message == 'The interface I is implemented more than once with different arguments: I <String> and I <java.lang.Number>'
        }
    }
}
