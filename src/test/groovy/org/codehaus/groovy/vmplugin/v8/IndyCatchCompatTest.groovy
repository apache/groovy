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
package org.codehaus.groovy.vmplugin.v8

import groovy.lang.GroovyObjectSupport
import groovy.lang.MissingMethodException
import groovy.lang.MissingPropertyException
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack
import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12387: the plain-Java replacements for {@code MethodHandles.catchException}
 * that the Selector uses on Android must behave exactly like the JDK combinator,
 * including for subclasses of the declared exception type, which ART's exact-class
 * check lets through; the runtime's no-stack exceptions are such subclasses.
 */
final class IndyCatchCompatTest {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup()

    // ---- targets standing in for a metaclass invocation: (Object, String, Object[])Object

    static Object succeed(Object receiver, String name, Object[] args) { name + '(' + args.join(',') + ')' }
    static Object missingNoStack(Object receiver, String name, Object[] args) {
        throw new MissingMethodExceptionNoStack(name, receiver.getClass(), args)
    }
    static Object explode(Object receiver, String name, Object[] args) { throw new IllegalStateException('boom') }

    static class Fallback extends GroovyObjectSupport {
        @Override Object invokeMethod(String name, Object args) { 'fallback:' + name }
    }

    // fixed arity: Groovy flags a trailing array parameter as varargs, and invokeWithArguments
    // would otherwise collect the Object[] argument into a fresh array
    private static MethodHandle target(String method) {
        LOOKUP.findStatic(IndyCatchCompatTest, method, MethodType.methodType(Object, Object, String, Object[])).asFixedArity()
    }

    @Test
    void groovyObjectFallbackRunsForANoStackMissingMethod() {
        def handle = IndyCatchCompat.withGroovyObjectFallback(target('missingNoStack'))
        assertEquals('fallback:foo', handle.invokeWithArguments(new Fallback(), 'foo', [1, 2] as Object[]))
    }

    @Test
    void groovyObjectFallbackIsTransparentOtherwise() {
        def ok = IndyCatchCompat.withGroovyObjectFallback(target('succeed'))
        assertEquals('foo(1,2)', ok.invokeWithArguments(new Fallback(), 'foo', [1, 2] as Object[]))
        def other = IndyCatchCompat.withGroovyObjectFallback(target('explode'))
        assertThrows(IllegalStateException) { other.invokeWithArguments(new Fallback(), 'foo', new Object[0]) }
    }

    @Test
    void groovyObjectFallbackRethrowsWhenTheReceiverDoesNotMatch() {
        // the invoker only delegates when the exception names the receiver's own class
        def handle = IndyCatchCompat.withGroovyObjectFallback(target('missingNoStack'))
        def receiver = new Fallback()
        def wrong = target('missingForOther')
        def e = assertThrows(MissingMethodException) {
            IndyCatchCompat.withGroovyObjectFallback(wrong).invokeWithArguments(receiver, 'foo', new Object[0])
        }
        assertEquals('foo', e.method)
        assertEquals('fallback:foo', handle.invokeWithArguments(receiver, 'foo', new Object[0]))
    }

    static Object missingForOther(Object receiver, String name, Object[] args) {
        throw new MissingMethodExceptionNoStack(name, String, args)
    }

    // ---- unwrapping around arbitrary call-site shapes

    static boolean check(int n, String s) { if (n < 0) throw new MissingPropertyExceptionNoStack(s, Integer); n > s.length() }
    static void act(String s) { throw new InvokerInvocationException(new IllegalArgumentException(s)) }
    static Object noStack(Object o) { throw new MissingMethodExceptionNoStack('bar', o.getClass(), new Object[0]) }

    @Test
    void unwrappingKeepsTheCallSiteTypeAndPassesArguments() {
        def target = LOOKUP.findStatic(IndyCatchCompatTest, 'check', MethodType.methodType(boolean, int, String))
        def handle = IndyCatchCompat.unwrapping(target)
        assertEquals(target.type(), handle.type())
        assertTrue(handle.invokeWithArguments(5, 'abc'))
        assertFalse(handle.invokeWithArguments(1, 'abc'))
    }

    @Test
    void unwrappingTurnsNoStackExceptionsIntoTheirStackfulKind() {
        def property = IndyCatchCompat.unwrapping(LOOKUP.findStatic(IndyCatchCompatTest, 'check', MethodType.methodType(boolean, int, String)))
        def mpe = assertThrows(MissingPropertyException) { property.invokeWithArguments(-1, 'p') }
        assertFalse(mpe instanceof MissingPropertyExceptionNoStack)
        assertEquals('p', mpe.property)
        assertTrue(mpe.stackTrace.length > 0)

        def method = IndyCatchCompat.unwrapping(LOOKUP.findStatic(IndyCatchCompatTest, 'noStack', MethodType.methodType(Object, Object)))
        def mme = assertThrows(MissingMethodException) { method.invokeWithArguments('x') }
        assertFalse(mme instanceof MissingMethodExceptionNoStack)
        assertEquals('bar', mme.method)
    }

    @Test
    void unwrappingUnwrapsInvokerInvocationExceptionsAndSupportsVoid() {
        def voidTarget = LOOKUP.findStatic(IndyCatchCompatTest, 'act', MethodType.methodType(void, String))
        def handle = IndyCatchCompat.unwrapping(voidTarget)
        assertEquals(voidTarget.type(), handle.type())
        def e = assertThrows(IllegalArgumentException) { handle.invokeWithArguments('why') }
        assertEquals('why', e.message)
        def passThrough = IndyCatchCompat.unwrapping(target('succeed'))
        assertEquals('n(7)', passThrough.invokeWithArguments(null, 'n', [7] as Object[]))
    }

    @Test
    void nothingOfTheClassIsLookedUpByName() {
        // GROOVY-12395: a shrinker (R8) renames members it cannot see reflected on; the wrappers
        // must not obtain handles to this class's own members by name, only to a JDK member
        def bytes = IndyCatchCompat.getResourceAsStream('IndyCatchCompat.class').bytes
        def ownMembers = IndyCatchCompat.declaredMethods*.name as Set
        def byName = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    void visitLdcInsn(Object value) {
                        if (value instanceof String && value in ownMembers) byName << "$name: '$value'"
                    }
                }
            }
        }, 0)
        assertEquals([], byName)
    }
}
