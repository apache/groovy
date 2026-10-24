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
package org.codehaus.groovy.runtime

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Dynamic property reads from a {@link GroovyObject}: a class with a {@code getProperty}
 * of its own answers every read, and a class with the interface default has the read
 * resolved by its metaclass on behalf of the class that makes it.
 */
final class GetPropertyDispatchTest {

    private static GroovyShell shell(boolean indy) {
        def config = new CompilerConfiguration()
        config.optimizationOptions.indy = indy
        new GroovyShell(config)
    }

    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void ownGetPropertyAnswersEveryRead(boolean indy) {
        shell(indy).evaluate '''
            class Reader {
                def name = 'field' // a property the override must take precedence over
                Object getProperty(String property) { 'own:' + property }
                Object readThis(String property) { this."$property" }
            }
            class InheritsReader extends Reader {}
            class RedeclaresReader extends Reader {
                @Override
                Object getProperty(String property) { 'sub:' + property }
            }
            class InheritsExpando extends Expando {} // getProperty inherited from a Java class

            def check(object, String expected) {
                assert object.name == expected
                assert object."${'na' + 'me'}" == expected
            }
            check(new Reader(), 'own:name')
            check(new InheritsReader(), 'own:name')
            check(new RedeclaresReader(), 'sub:name')
            assert new Reader().readThis('name') == 'own:name'
            def expando = new InheritsExpando()
            expando.name = 'mapped' // kept by Expando, which has no such property otherwise
            check(expando, 'mapped')
        '''
    }

    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void defaultGetPropertyResolvesReadForTheSender(boolean indy) {
        shell(indy).evaluate '''
            class Base {
                private String secret = 'hidden'
                static Object readName(Object target) { target.secret }
                static Object readDynamicName(Object target, String name) { target."$name" }
                Object readThis(String name) { this."$name" }
            }
            class Sub extends Base {}

            def sub = new Sub()
            // Base's private field is visible to a read made by Base, so these succeed
            // only if the metaclass is told that Base is the sender
            assert Base.readName(sub) == 'hidden'
            assert Base.readDynamicName(sub, 'secret') == 'hidden'
            assert sub.readThis('secret') == 'hidden'
            // the interface default, which has no sender to pass on, cannot see the field
            groovy.test.GroovyAssert.shouldFail(MissingPropertyException) { sub.getProperty('secret') }
        '''
    }

    /**
     * Property-read call sites take a {@code getProperty} marked {@code @Internal} (or a
     * synthetic one) for a generated implementation, as they take the interface default,
     * and leave the read to the metaclass; {@code ScriptBytecodeAdapter.getProperty}, which
     * reads a property whose name is computed, counts any implementation but the default.
     */
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void getPropertyMarkedInternalIsLeftToTheMetaClassByCallSites(boolean indy) {
        shell(indy).evaluate '''
            class Marked {
                def x = 'field'
                @groovy.transform.Internal
                Object getProperty(String property) { 'own:' + property }
            }

            def marked = new Marked()
            assert marked.x == 'field'
            assert marked."${'x'}" == 'own:x'
        '''
    }
}
