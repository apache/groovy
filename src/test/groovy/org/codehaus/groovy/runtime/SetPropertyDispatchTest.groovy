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
 * Dynamic property writes to a {@link GroovyObject}: a class with a {@code setProperty}
 * of its own receives every write, and a class with the interface default has the write
 * resolved by its metaclass on behalf of the class that makes it.
 */
final class SetPropertyDispatchTest {

    private static GroovyShell shell(boolean indy) {
        def config = new CompilerConfiguration()
        config.optimizationOptions.indy = indy
        new GroovyShell(config)
    }

    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void ownSetPropertyReceivesEveryWrite(boolean indy) {
        shell(indy).evaluate '''
            class Recorder {
                def name // a property the override must take precedence over
                List<String> writes = []
                void setProperty(String property, Object value) { writes << "$property=$value".toString() }
                void writeThis(String property, Object value) { this."$property" = value }
            }
            class InheritsRecorder extends Recorder {}
            class RedeclaresRecorder extends Recorder {
                @Override
                void setProperty(String property, Object value) { writes << "sub:$property=$value".toString() }
            }

            def check(Recorder recorder, String prefix) {
                recorder.name = 'static'
                recorder."${'na' + 'me'}" = 'dynamic'
                recorder.writeThis('name', 'this')
                assert recorder.writes == ["${prefix}name=static", "${prefix}name=dynamic", "${prefix}name=this"]*.toString()
                assert recorder.name == null
            }
            check(new Recorder(), '')
            check(new InheritsRecorder(), '')
            check(new RedeclaresRecorder(), 'sub:')
        '''
    }

    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void defaultSetPropertyResolvesWriteForTheSender(boolean indy) {
        shell(indy).evaluate '''
            class Base {
                private String secret
                static void writeName(Object target, Object value) { target.secret = value }
                static void writeDynamicName(Object target, String name, Object value) { target."$name" = value }
                void writeThis(String name, Object value) { this."$name" = value }
                String readSecret() { secret }
            }
            class Sub extends Base {}

            def sub = new Sub()
            // Base's private field is visible to a write made by Base, so these succeed
            // only if the metaclass is told that Base is the sender
            Base.writeName(sub, 'a')
            assert sub.readSecret() == 'a'
            Base.writeDynamicName(sub, 'secret', 'b')
            assert sub.readSecret() == 'b'
            sub.writeThis('secret', 'c')
            assert sub.readSecret() == 'c'
            // the interface default, which has no sender to pass on, cannot see the field
            groovy.test.GroovyAssert.shouldFail(MissingPropertyException) { sub.setProperty('secret', 'd') }
        '''
    }

    /**
     * A {@code setProperty} that an expando closure or a mixin gives a class whose compiled
     * {@code setProperty} is the interface default is known to its metaclass only, not to
     * the per-class reflection answer, and still receives every write, including writes
     * through sites already linked to the class's setter.
     */
    // GROOVY-12489
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void expandoOrMixinSetPropertyReceivesEveryWrite(boolean indy) {
        shell(indy).evaluate '''
            class Plain {
                def name
                void writeThis(String property, Object value) { this."$property" = value }
            }
            class Writes {
                static List<String> log = []
            }
            class SetPropertyMixin {
                void setProperty(String property, Object value) { Writes.log << "mixin:$property=$value".toString() }
            }

            def writeAll(Plain typed, Object untyped) {
                typed.name = 'static'
                untyped.name = 'untyped'
                typed."${'na' + 'me'}" = 'dynamic'
                untyped."${'na' + 'me'}" = 'untyped dynamic'
                typed.writeThis('name', 'this')
                writeChecked(typed)
            }
            @groovy.transform.TypeChecked
            def writeChecked(Plain plain) { plain.name = 'checked' }

            def check(Plain plain, String prefix) {
                Writes.log.clear()
                writeAll(plain, plain)
                assert Writes.log == ['static', 'untyped', 'dynamic', 'untyped dynamic', 'this', 'checked'].collect { "${prefix}name=$it".toString() }
                assert plain.@name == null
            }

            // link the sites to the class's own setter first
            def plain = new Plain()
            writeAll(plain, plain)
            assert plain.name == 'checked'
            try {
                Plain.metaClass.setProperty = { String property, value -> Writes.log << "expando:$property=$value".toString() }
                check(new Plain(), 'expando:')
                GroovySystem.metaClassRegistry.removeMetaClass(Plain)

                def single = new Plain()
                single.metaClass.setProperty = { String property, value -> Writes.log << "instance:$property=$value".toString() }
                check(single, 'instance:')

                Plain.mixin(SetPropertyMixin)
                check(new Plain(), 'mixin:')
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(Plain)
            }
        '''
    }

    /**
     * A {@code setProperty} that a class takes from a default method of another interface,
     * such as one extending {@code GroovyObject}, receives every write, as one the class
     * declares does: only the {@code GroovyObject} default hands the write to the metaclass.
     */
    // GROOVY-12489
    @ParameterizedTest(name = 'indy={0}')
    @ValueSource(booleans = [true, false])
    void defaultSetPropertyOfAnotherInterfaceReceivesEveryWrite(boolean indy) {
        shell(indy).evaluate '''
            class Writes {
                static List<String> log = []
            }
            interface Recording extends GroovyObject {
                default void setProperty(String property, Object value) { Writes.log << "$property=$value".toString() }
            }
            interface ExtendsRecording extends Recording {
            }
            abstract class Base {
                def name
                void writeThis(String property, Object value) { this."$property" = value }
                def nameField() { this.@name }
            }
            class Recorder extends Base implements Recording {
            }
            class InheritsRecorder extends Recorder {
            }
            class ThroughSubInterface extends Base implements ExtendsRecording {
            }

            def writeAll(Base typed, Object untyped) {
                typed.name = 'static'
                untyped.name = 'untyped'
                typed."${'na' + 'me'}" = 'dynamic'
                untyped."${'na' + 'me'}" = 'untyped dynamic'
                typed.writeThis('name', 'this')
                writeChecked(typed)
            }
            @groovy.transform.TypeChecked
            def writeChecked(Base base) { base.name = 'checked' }

            for (type in [Recorder, InheritsRecorder, ThroughSubInterface]) {
                assert type.getMethod('setProperty', String, Object).declaringClass == Recording
                def recorder = type.newInstance()
                Writes.log.clear()
                writeAll(recorder, recorder)
                assert Writes.log == ['static', 'untyped', 'dynamic', 'untyped dynamic', 'this', 'checked'].collect { "name=$it".toString() }
                assert recorder.nameField() == null
            }
        '''
    }
}
