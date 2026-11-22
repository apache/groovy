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

import org.codehaus.groovy.runtime.InvokerInvocationException
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationTargetException

/**
 * GROOVY-4662: a Groovy {@link Closure} (and a closure coerced to
 * {@link Runnable}) that throws a checked exception used to wrap it
 * in {@link InvokerInvocationException} when invoked from Java.
 * Groovy methods already unwrapped the same exception. The original
 * report is a Java caller of {@code Closure#call()} and
 * {@code Runnable#run()}.
 */
final class Groovy4662 {

    static class GroovyLib {
        void boom() throws IOException { throw new IOException('declared') }
        void undeclaredBoom() { throw new IOException('undeclared') }
        Closure closure = { throw new IOException('closure') }
        Runnable runnable = { throw new IOException('runnable') }
    }

    @Test
    void testGroovyMethodThatThrowsDeclaredCheckedException() {
        assertThrowsIoException('declared') { new GroovyLib().boom() }
    }

    @Test
    void testGroovyMethodThatThrowsUndeclaredCheckedException() {
        assertThrowsIoException('undeclared') { new GroovyLib().undeclaredBoom() }
    }

    @Test
    void testJavaCallerOfClosureCallThrowsCheckedException() {
        // Method.invoke is a Java caller, matching the ticket's Java test.
        def closure = new GroovyLib().closure
        assertThrowsIoException('closure') {
            invokeJava(Closure, 'call', closure)
        }
    }

    @Test
    void testJavaCallerOfRunnableRunThrowsCheckedException() {
        def runnable = new GroovyLib().runnable
        assertThrowsIoException('runnable') {
            invokeJava(Runnable, 'run', runnable)
        }
    }

    private static void invokeJava(Class type, String name, Object target) throws Throwable {
        try {
            type.getMethod(name).invoke(target)
        } catch (InvocationTargetException ite) {
            throw ite.cause ?: ite
        }
    }

    private static void assertThrowsIoException(String message, Closure c) {
        try {
            c.call()
            assert false, "expected IOException($message)"
        } catch (IOException e) {
            assert e.message == message
            assert !(e instanceof InvokerInvocationException)
        } catch (InvokerInvocationException e) {
            assert false, "InvokerInvocationException still leaked: ${e.cause}"
        }
    }
}
