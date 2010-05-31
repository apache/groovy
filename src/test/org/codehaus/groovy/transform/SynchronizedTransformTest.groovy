/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform

/**
 * @author Paul King
 */
class SynchronizedTransformTest extends GroovyShellTestCase {

    void testSynchronized() {
        assertScript """
            class Count {
              private val = 0
              @groovy.transform.Synchronized
              void incDec() {
                assert val == 0; val++; assert val == 1
                sleep 500
                assert val == 1; val--; assert val == 0
              }
            }

            def c = new Count()
            def t = Thread.start{ c.incDec() }
            sleep 100
            c.incDec()
            t.join()
        """
    }

}