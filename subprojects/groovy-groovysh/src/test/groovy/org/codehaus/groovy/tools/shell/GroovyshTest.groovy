/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class GroovyshTest
extends GroovyTestCase {

    IO testio
    ByteArrayOutputStream mockOut
    ByteArrayOutputStream mockErr

    void setUp() {
        super.setUp()

        mockOut = new ByteArrayOutputStream();

        mockErr = new ByteArrayOutputStream();

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
    }

    void testCompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("x = 3")
        assertEquals(" 3\n", mockOut.toString()[-3..-1])
    }

    void testIncompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("def x() {")
        assertEquals("", mockOut.toString())
    }

    void testBadExpr() {
        Groovysh groovysh = new Groovysh(testio)
        try {
            groovysh.execute("x}")
            fail()
        } catch (MultipleCompilationErrorsException e) {
            assertEquals("", mockOut.toString())
        }
    }

    void testDisplayBuffer() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.displayBuffer(["foo", "bar"])
        assertEquals(34, mockOut.toString().length())
        assertTrue(mockOut.toString().contains("foo\n"))
        assertTrue(mockOut.toString().contains("bar\n"))
    }

    void testDefaultErrorHook() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultErrorHook(new Throwable() {
            StackTraceElement[] stackTrace = [
                    new StackTraceElement("fooClass", "fooMethod", "fooFile", 42),
                    new StackTraceElement(Interpreter.SCRIPT_FILENAME, "run", "scriptFile", 42)]
        })
        assertEquals("", mockOut.toString())
        assertTrue(mockErr.toString(), mockErr.toString().contains("foo"))
        assertFalse(mockErr.toString(), mockErr.toString().contains(Interpreter.SCRIPT_FILENAME))
        assertFalse(mockErr.toString(), mockErr.toString().contains("..."))
    }

}