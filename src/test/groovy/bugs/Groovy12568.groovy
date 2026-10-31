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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * A dynamic {@code new} of a protected constructor of another package, in a
 * subclass, is selected through the meta-class. When the JVM refuses access to
 * the constructor, as for an encapsulated JDK class, the call fails with the
 * same Groovy exception with and without invokedynamic.
 */
final class Groovy12568 {

    private static GroovyShell shell(final boolean indy) {
        def config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.INVOKEDYNAMIC] = indy
        new GroovyShell(Groovy12568.classLoader, new Binding(), config)
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testProtectedConstructorOfEncapsulatedJdkClass(final boolean indy) {
        assumeFalse(ObjectInputStream.module.isOpen('java.io', Groovy12568.module),
            'the JVM opens java.base/java.io, so the constructor is reflectively reachable')

        def err = shouldFail(GroovyRuntimeException) {
            shell(indy).evaluate '''
                class C extends ObjectInputStream {
                    static ObjectInputStream make() {
                        new ObjectInputStream()
                    }
                }
                C.make()
            '''
        }
        // was, with invokedynamic: GroovyBugError "BUG! UNCAUGHT EXCEPTION: member is protected"
        assert err.class == GroovyRuntimeException
        assert err.message.startsWith('could not access constructor: protected java.io.ObjectInputStream()')
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testProtectedConstructorOfClassOfAnotherPackage(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class A {
                final String value
                protected A(String value) { this.value = value }
            }
            true
        '''
        assert shell.evaluate('''package q
            class C extends p.A {
                C() { super('c') }
                static p.A make() {
                    new p.A('x')
                }
            }
            C.make().value
        ''') == 'x'
    }

    // GROOVY-12568
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void testSuperCallOfPackagePrivateMethodOfClassOfAnotherClassLoader(final boolean indy) {
        def shell = shell(indy)
        shell.evaluate '''package p
            class M {
                @groovy.transform.PackageScope String pm() { 'pm' }
            }
            true
        '''
        assert shell.evaluate('''package p
            class B extends M { // same package name as M, but another class loader
                def test() {
                    super.pm()
                }
            }
            @groovy.transform.CompileStatic
            class C extends M {
                def test() {
                    super.pm()
                }
            }
            [new B().test(), new C().test()]
        ''') == ['pm', 'pm']
    }
}
