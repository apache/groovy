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

import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import static groovy.test.GroovyAssert.shouldFail

/**
 * Generated and hand-written closures must keep the same call semantics after
 * the MethodHandle fast path is used for {@code doCall} / {@code call} targets.
 * Handle-thrown exceptions surface as the body threw them; {@code Method.invoke}
 * is used only when a target cannot be adapted, and only then is its wrapper
 * {@link InvocationTargetException} unwrapped.
 */
final class ClosureCallHandleTest {

    @Test
    void 'gdk each collect findAll and inject still dispatch'() {
        assert [1, 2, 3].collect { it * 2 } == [2, 4, 6]
        assert [1, 2, 3].findAll { it > 1 } == [2, 3]
        def sum = 0
        [1, 2, 3].each { sum += it }
        assert sum == 6
        assert [1, 2, 3].inject(0) { a, b -> a + b } == 6
    }

    @Test
    void 'typed doCall falls through to the metaclass when a guard fails'() {
        def c = new Closure(this) {
            def doCall(String s) { s.toUpperCase() }
        }
        assert c.call('ab') == 'AB'
        // GROOVY-12164: GString is not a String, so the MH/guard path must not fire
        assert c.call("${'ab'}") == 'AB'
    }

    @Test
    void 'exceptions from doCall are rethrown unwrapped'() {
        def c = { throw new IllegalStateException('boom') }
        def e = shouldFail(IllegalStateException) {
            c.call()
        }
        assert e.message == 'boom'
    }

    @Test
    void 'InvocationTargetException from the body is not treated as a reflection wrapper'() {
        def inner = new RuntimeException('inner')
        def c = { throw new InvocationTargetException(inner) }
        def e = shouldFail(InvocationTargetException) {
            c.call()
        }
        assert e.cause.is(inner)
    }

    @Test
    void 'IllegalAccessException from the body is not wrapped as GroovyRuntimeException'() {
        // Invoke the Java varargs entry reflectively: GroovyAssert.shouldFail and Groovy
        // call sites unwrap GroovyRuntimeException, which would hide the wrap-as-GRE bug.
        def c = { throw new IllegalAccessException('nope') }
        try {
            // Method.invoke is itself varargs: wrap the Object[] argument so we
            // pass one parameter to call(Object...), not zero.
            Closure.getMethod('call', Object[].class).invoke(c, new Object[]{new Object[0]})
            assert false: 'expected InvocationTargetException'
        } catch (InvocationTargetException ite) {
            assert ite.cause instanceof IllegalAccessException
            assert !(ite.cause instanceof GroovyRuntimeException)
            assert ite.cause.message == 'nope'
        }
    }

    @Test
    void 'checked exceptions from doCall surface as thrown'() {
        def c = { throw new IOException('io') }
        def e = shouldFail(IOException) {
            c.call()
        }
        assert e.message == 'io'
    }

    @Test
    void 'errors from doCall surface as thrown'() {
        def c = { throw new Error('err') }
        def e = shouldFail(Error) {
            c.call()
        }
        assert e.message == 'err'
    }

    @Test
    void 'zero through four argument call forms keep their results'() {
        def zero = { 42 }
        assert zero.call() == 42
        def one = { x -> x }
        assert one.call('x') == 'x'
        def two = { a, b -> a + b }
        assert two.call(1, 2) == 3
        def three = { a, b, c -> a + b + c }
        assert three.call(1, 2, 3) == 6
        def four = { a, b, c, d -> a + b + c + d }
        assert four.call(1, 2, 3, 4) == 10
        assert [a: 1, b: 2].inject(0) { acc, k, v -> acc + v } == 3
        def seen = []
        [x: 1].eachWithIndex { k, v, i -> seen << "$k:$v:$i".toString() }
        assert seen == ['x:1:0']
        assert handleFor(zero, 0) != null
        assert handleFor(one, 1) != null
        assert handleFor(two, 2) != null
        assert handleFor(three, 3) != null
        assert handleFor(four, 4) != null
    }

    @Test
    void 'arity at the cache limit is not cached'() {
        def five = { a, b, c, d, e -> a + b + c + d + e }
        assert five.call(1, 2, 3, 4, 5) == 15
        assert cachedTarget(five, 5) == null
        assert handleFor(five, 5) == null
    }

    @Test
    void 'void and primitive doCall adapt through the handle'() {
        def side = []
        def empty = new Closure(this) {
            void doCall() { side << 1 }
        }
        assert empty.call() == null
        assert side == [1]

        def boxed = new Closure(this) {
            int doCall(int x) { x + 1 }
        }
        assert boxed.call(41) == 42
        assert [1, 2].collect(boxed) == [2, 3]
    }

    @Test
    void 'null Object argument reaches an all-Object doCall'() {
        def c = { it }
        assert c.call((Object) null) == null
    }

    @Test
    void 'call-form override is invoked via the handle and the re-entry latch still holds'() {
        def wrapped = new Closure(this) {
            @Override
            Object call(Object arg) {
                return 'wrapped:' + arg
            }
        }
        // Java/GDK entry: call(Object...) must agree with virtual call(Object)
        assert wrapped.call(new Object[]{'x'}) == 'wrapped:x'
        assert ['a'].collect(wrapped) == ['wrapped:a']
        assert handleFor(wrapped, 1) != null

        def reenter = new Closure(this) {
            @Override
            Object call() {
                return call(new Object[0])
            }
            Object doCall() { 99 }
        }
        assert reenter.call(new Object[0]) == 99
        assert handleFor(reenter, 0) != null
    }

    @Test
    void 'curried and method-pointer closures stay on the metaclass path'() {
        def add = { a, b -> a + b }
        assert add.curry(10).call(2) == 12
        assert 'abc'.&substring.call(1) == 'bc'
    }

    @Test
    void 'invokeCached falls back to Method invoke when the handle is null'() {
        def c = { x -> "r:$x" }
        Method method = findDoCall(c.getClass(), 1)
        method.accessible = true
        assert invokeCachedDirect(null, method, c, ['z'] as Object[]) == 'r:z'
    }

    @Test
    void 'invokeCached unwraps InvocationTargetException from Method invoke'() {
        def c = { throw new IllegalStateException('via-reflect') }
        Method method = findDoCall(c.getClass(), 0)
        method.accessible = true
        def e = shouldFail(IllegalStateException) {
            invokeCachedDirect(null, method, c, new Object[0])
        }
        assert e.message == 'via-reflect'
    }

    @Test
    void 'invokeCached wraps IllegalAccessException from Method invoke'() {
        def c = new Closure(this) {
            private Object doCall() { 'secret' }
        }
        Method method = c.getClass().getDeclaredMethod('doCall')
        method.accessible = false
        Method invokeCached = Closure.getDeclaredMethod('invokeCached', MethodHandle, Method, Closure, Object[])
        invokeCached.accessible = true
        try {
            invokeCached.invoke(null, null, method, c, new Object[0])
            assert false: 'expected InvocationTargetException wrapping GroovyRuntimeException'
        } catch (InvocationTargetException ite) {
            // catch the reflective wrapper so GroovyAssert does not unwrap GRE
            assert ite.cause instanceof GroovyRuntimeException
            assert ite.cause.cause instanceof IllegalAccessException
        }
    }

    @Test
    void 'invokeHandle spreader covers arity beyond the specialised switch'() {
        def c = new Closure(this) {
            def doCall(a, b, d, e, f) { [a, b, d, e, f] }
        }
        Method method = findDoCall(c.getClass(), 5)
        method.accessible = true
        MethodHandle handle = MethodHandles.lookup().unreflect(method)
                .asType(MethodType.genericMethodType(6))
        assert invokeHandleDirect(handle, c, [1, 2, 3, 4, 5] as Object[]) == [1, 2, 3, 4, 5]
    }

    @Test
    void 'unreflect returns null when the method cannot be adapted'() {
        def hidden = new Object() {
            private void secret() {}
        }
        Method method = hidden.getClass().getDeclaredMethod('secret')
        Class<?> callOverride = Class.forName('groovy.lang.Closure$CallOverride')
        Method unreflect = callOverride.getDeclaredMethod('unreflect', Method)
        unreflect.accessible = true
        assert unreflect.invoke(null, method) == null
    }

    private static Object callOverrideField(Closure closure, String field, int arity) {
        Class<?> callOverride = Class.forName('groovy.lang.Closure$CallOverride')
        def table = Closure.getDeclaredField('CALL_OVERRIDES')
        table.accessible = true
        def override = table.get(null).get(closure.getClass())
        def f = callOverride.getDeclaredField(field)
        f.accessible = true
        Object[] slots = (Object[]) f.get(override)
        return (arity < slots.length) ? slots[arity] : null
    }

    private static MethodHandle handleFor(Closure closure, int arity) {
        return (MethodHandle) callOverrideField(closure, 'handles', arity)
    }

    private static Method cachedTarget(Closure closure, int arity) {
        return (Method) callOverrideField(closure, 'byArity', arity)
    }

    private static Method findDoCall(Class<?> type, int arity) {
        Method found = type.getDeclaredMethods().find { Method m ->
            m.name == 'doCall' && m.parameterCount == arity && !m.bridge
        }
        assert found != null: "no doCall/${arity} on ${type.name}"
        return found
    }

    private static Object invokeCachedDirect(MethodHandle handle, Method target, Closure self, Object[] args) {
        Method m = Closure.getDeclaredMethod('invokeCached', MethodHandle, Method, Closure, Object[])
        m.accessible = true
        try {
            return m.invoke(null, handle, target, self, args)
        } catch (InvocationTargetException ite) {
            throw ite.cause
        }
    }

    private static Object invokeHandleDirect(MethodHandle handle, Closure self, Object[] args) {
        Method m = Closure.getDeclaredMethod('invokeHandle', MethodHandle, Closure, Object[])
        m.accessible = true
        try {
            return m.invoke(null, handle, self, args)
        } catch (InvocationTargetException ite) {
            throw ite.cause
        }
    }
}
