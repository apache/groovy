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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy8815 {

    @Test
    void testGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Event.groovy')
            a.write '''
                class Event<T> {
                    Event(String id, T payload) {}
                    Event<T> setReplyTo(Object replyTo) {}
                }
            '''
            def b = new File(parentDir, 'Events.groovy')
            b.write '''
                import groovy.transform.CompileStatic
                import java.util.function.Consumer
                @CompileStatic
                trait Events {
                    def <E extends Event<?>> Registration<Object, Consumer<E>> on(Class key, Closure consumer) {}
                }
            '''
            def c = new File(parentDir, 'Registration.java')
            c.write '''
                interface Registration<K, V> {
                }
            '''
            def d = new File(parentDir, 'Service.groovy')
            d.write '''
                class Service implements Events {
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d)
            cu.compile()

            def method = loader.loadClass('Service').getMethod('on', Class, Closure)

            // should not contain unresolved type parameter 'T'
            assert method.genericSignature == '<E:LEvent<*>;>(Ljava/lang/Class;Lgroovy/lang/Closure;)LRegistration<Ljava/lang/Object;Ljava/util/function/Consumer<TE;>;>;'
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
