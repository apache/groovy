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
class LazyTransformTest extends GroovyTestCase {

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
              class X {
                private List list = []

                List getList () {
                  [1,2,3]
                }

                List getInternalList () {
                  list
                }
              }

              new X ()
        """)

        assertEquals([1,2,3], res.list)
        assertEquals([], res.internalList)
    }

    void testNoInit() {
        def res = shell.evaluate("""
              class X {
                @Lazy private ArrayList list

                void op () {
                  list << 1 << 2 << 3
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testInit() {
        def res = shell.evaluate("""
              class X {
                @Lazy private ArrayList list = [1,2,3]

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testInitWithClosure() {
        def res = shell.evaluate("""
              class X {
                @Lazy private ArrayList list = { [1,2,3] } ()

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testSoft() {
        def res = shell.evaluate("""
              class X {
                @Lazy(soft=true) private ArrayList list = { [1,2,3] } ()

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertTrue res.@'$list' instanceof SoftReference
        assertEquals([1,2,3], res.list)
    }
}