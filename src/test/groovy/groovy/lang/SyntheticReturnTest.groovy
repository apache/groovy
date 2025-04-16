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

import groovy.test.GroovyShellTestCase

class SyntheticReturnTest extends GroovyShellTestCase{

    // GROOVY-5980
    void testImplicitReturnWithFinallyBlockAndCastException() {
        assertEquals( 'test', evaluate("""
              s = ''
              int f() {
                 try { null } finally { s += 'test' }
              }
              try { f() } catch (ex) {}
              s
        """))
    }

    void testImplicitReturnWithFinallyBlockMultipleStmtsAndCastException() {
        assertEquals( 'test', evaluate("""
              i = 0
              s = ''
              int f() {
                 try { def t = 41 + 1; i = t; null } finally { assert i == 42; s += 'test' }
              }
              try { f() } catch (ex) {}
              s
        """))
    }

    void testImplicitReturnWithFinallyBlockAndTypeCast() {
        assertEquals( '42', evaluate("""
              s = ''
              String f() {
                 try { 42 } finally { s += 'test' }
              }
              def result = f()
              assert s == 'test'
              result
        """))
    }

    void testExpt () {
        assertEquals( 5, evaluate("""
              5
        """))
    }

    void testIfElse () {
        assertEquals( 1, evaluate("""
            if (true)
              1
            else
              0
        """))
    }

    void testIfNoElse () {
        assertEquals( 1, evaluate("""
            if (true)
              1
        """))
    }

    void testEmptyElse () {
        assertEquals( null, evaluate("""
            if (false)
              {
                2
                return 1
              }
        """))
    }

    void testEmptyBlockElse () {
        assertEquals( null, evaluate("""
            if (false)
              {
                2
                return 1
              }
            else {
            }
        """))
    }

    void testNestedIf () {
        assertEquals( 3, evaluate("""
            if (false)
              {
                2
                return 1
              }
            else {
               if (true)
                 3
            }
        """))
    }

    void testCatch () {
        assertEquals( 0, evaluate("""
            try {
                if (true) {
                    throw new NullPointerException()
                }
            }
            catch(Throwable t) {
                0
            }
            finally {
                -1
            }
        """))
    }

    void testClosure () {
        def s = 0, k = 0
        def f = {
            s++
            if ((s&1)==0)
              1
            else
              0
        }
        for (x in 0..9)
          k += f ()

        assertEquals 10, s
        assertEquals 5, k
    }

    void testSynchronized () {
        assertEquals(20, mm(10))
        assertEquals(10,  mm(1))
    }

    private def mm(x) {
        synchronized (new Object()) {
            try {
                if (x == 1)
                    throw new NullPointerException()
                else
                    2*x
            }
            catch (Throwable t) {
               10
            }
        }
    }

    void testTry () {
        def f = {
            try {
                if (true) {
                    return 1
                }
            }
            finally {
                return 0
            }
        }
        assertEquals 0, f()

        f = {
            try {
                if (true) {
                   1
                }
            }
            catch(Throwable t) {
                return 0
            }
        }
        assertEquals 1, f()

        f = {
            try {
                if (true) {
                   1
                }
            }
            finally {
                0
            }
        }
        assertEquals 1, f()
    }

}
