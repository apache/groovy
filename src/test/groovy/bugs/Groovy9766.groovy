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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-9766: compiling a class that uses {@code @ListenerList} used to fail
 * during instruction selection with {@code NoClassDefFoundError:
 * groovyjarjaropenbeans/Transient}. The compiler must use
 * {@code java.beans.Transient} and the snippet must compile and run.
 */
final class Groovy9766 {

    @Test
    void testListenerListCompilesWithoutShadedOpenbeansTransient() {
        assertScript '''
            import groovy.beans.ListenerList

            class SomethingHappenedEvent {
                def source
            }

            interface SomethingHappenedListener {
                void somethingHappened(SomethingHappenedEvent e)
            }

            class EventSource {
                @ListenerList
                List<SomethingHappenedListener> listeners
            }

            def src = new EventSource()
            def seen = []
            src.addSomethingHappenedListener { e -> seen << e.source }
            src.fireSomethingHappened(new SomethingHappenedEvent(source: 'ok'))
            assert seen == ['ok']
        '''
    }
}
