/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

import java.lang.ref.SoftReference

/**
 * @author Alex Tkachman
 */
class DelegateTransformTest extends GroovyTestCase {

    GroovyShell shell;

    protected void setUp() {
        super.setUp();
        shell = new GroovyShell();
    }

    protected void tearDown() {
        shell = null;
        super.tearDown();
    }

    void testProp () {
        def res = shell.evaluate("""
              import java.util.concurrent.locks.*

              class LockableMap {
                 @Delegate private Map map = [:]

                 @Delegate private Lock lock = new ReentrantLock ()

                 @Delegate(interfaces=false) private List list = new ArrayList ()
              }

              new LockableMap ()
        """)

        res.lock ()
        try {
            res [0] = 0
            res [1] = 1
            res [2] = 2

            res.add("in list")
        }
        finally {
           res.unlock ()
        }

        assertEquals( [0:0,1:1,2:2], res.@map)
        assertEquals( "in list", res.@list[0])

        assertTrue res instanceof Map
        assertTrue res instanceof java.util.concurrent.locks.Lock
        assertFalse res instanceof List
    }
}