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

import org.codehaus.groovy.runtime.InvokerInvocationException
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
        // Java/GDK entry: Groovy c.call(...) is indy and binds to doCall, skipping guards
        assert javaVarargsCall(c, 'ab') == 'AB'
        // GROOVY-12164: GString is not a String, so the MH/guard path must not fire
        assert javaVarargsCall(c, "${'ab'}") == 'AB'
        assert handleFor(c, 1) != null
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
        assert javaVarargsCall(five, 1, 2, 3, 4, 5) == 15
        assert cachedTarget(five, 5) == null
        assert handleFor(five, 5) == null
    }

    @Test
    void 'void and primitive doCall adapt through the handle'() {
        def side = []
        def empty = new Closure(this) {
            void doCall() { side << 1 }
        }
        assert javaVarargsCall(empty) == null
        assert side == [1]
        assert handleFor(empty, 0) != null

        def boxed = new Closure(this) {
            int doCall(int x) { x + 1 }
        }
        assert javaVarargsCall(boxed, 41) == 42
        assert [1, 2].collect(boxed) == [2, 3]
        assert handleFor(boxed, 1) != null
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
        def curried = add.curry(10)
        assert javaVarargsCall(curried, 2) == 12
        assert javaVarargsCall('abc'.&substring, 1) == 'bc'
        assert handleFor(curried, 1) == null
        assert handleFor('abc'.&substring, 1) == null
    }

    @Test
    void 'invokeCached catch propagates throwers built as MethodHandles'() {
        def c = { 1 }
        Method method = findDoCall(c.getClass(), 0)
        method.accessible = true
        MethodHandle throwing = MethodHandles.dropArguments(
                MethodHandles.throwException(Object, IllegalStateException)
                        .bindTo(new IllegalStateException('mh-throw')),
                0, Object)
        def e = shouldFail(IllegalStateException) {
            invokeCachedDirect(throwing, method, c, new Object[0])
        }
        assert e.message == 'mh-throw'
    }

    @Test
    void 'invokeCached catch propagates NullPointerException from invokeHandle'() {
        def c = { 1 }
        Method method = findDoCall(c.getClass(), 0)
        method.accessible = true
        MethodHandle handle = handleFor(c, 0)
        shouldFail(NullPointerException) {
            invokeCachedDirect(handle, method, c, null)
        }
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
    void 'java varargs call hits invokeHandle for arities zero through four'() {
        // Groovy c.call(a,b,...) is indy and binds to doCall; the MH switch is on
        // Closure.call(Object...), which Java/GDK uses.
        def zero = { 42 }
        assert javaVarargsCall(zero) == 42
        def one = { x -> x }
        assert javaVarargsCall(one, 'x') == 'x'
        def two = { a, b -> a + b }
        assert javaVarargsCall(two, 1, 2) == 3
        def three = { a, b, c -> a + b + c }
        assert javaVarargsCall(three, 1, 2, 3) == 6
        def four = { a, b, c, d -> a + b + c + d }
        assert javaVarargsCall(four, 1, 2, 3, 4) == 10
    }

    @Test
    void 'java varargs call rethrows body throwables from the handle'() {
        def boom = { throw new IllegalStateException('via-java-call') }
        def e = shouldFail(IllegalStateException) {
            javaVarargsCall(boom)
        }
        assert e.message == 'via-java-call'

        def inner = new RuntimeException('inner')
        def iteBody = { throw new InvocationTargetException(inner) }
        def ite = shouldFail(InvocationTargetException) {
            javaVarargsCall(iteBody)
        }
        assert ite.cause.is(inner)

        def io = { throw new IOException('via-java-call-io') }
        def ioe = shouldFail(IOException) {
            javaVarargsCall(io)
        }
        assert ioe.message == 'via-java-call-io'

        def err = { throw new Error('via-java-call-err') }
        def error = shouldFail(Error) {
            javaVarargsCall(err)
        }
        assert error.message == 'via-java-call-err'
    }

    @Test
    void 'public call uses Method invoke when the cached handle is cleared'() {
        def c = { x -> "r:$x" }
        assert javaVarargsCall(c, 'a') == 'r:a'
        assert handleFor(c, 1) != null
        clearHandle(c, 1)
        assert handleFor(c, 1) == null
        assert javaVarargsCall(c, 'b') == 'r:b'
    }

    @Test
    void 'public call unwraps InvocationTargetException when the handle is cleared'() {
        def c = { throw new IllegalStateException('cleared-handle') }
        try {
            javaVarargsCall(c)
        } catch (IllegalStateException ignored) {
            // warms CallOverride so byArity[0] is populated
        }
        clearHandle(c, 0)
        def e = shouldFail(IllegalStateException) {
            javaVarargsCall(c)
        }
        assert e.message == 'cleared-handle'
    }

    @Test
    void 'subclass with no doCall or call override resolves to CallOverride NONE'() {
        def c = new Closure(this) {}
        assert callOverrideOf(c).is(callOverrideNone())
        shouldFail(MissingMethodException) {
            javaVarargsCall(c)
        }
        Method lookup = Class.forName('groovy.lang.Closure$CallOverride')
                .getDeclaredMethod('lookup', Class)
        lookup.accessible = true
        assert lookup.invoke(null, Closure).is(callOverrideNone())
    }

    @Test
    void 'null arguments array skips the cached handle and uses the metaclass'() {
        def c = { 7 }
        assert javaVarargsCall(c) == 7
        Method call = Closure.getMethod('call', Object[].class)
        assert call.invoke(c, new Object[]{null}) == 7
    }

    @Test
    void 'lookup skips static array and ambiguous doCall shapes'() {
        def skipped = new Closure(this) {
            static Object doCall(String ignored) { 'static' }
            static Object call(Object ignored) { 'static-call' }
            Object extra() { 'not-doCall' }
            def doCall(Object[] args) { args }
            def doCall() { 1 }
        }
        assert javaVarargsCall(skipped) == 1
        assert handleFor(skipped, 0) != null
        assert cachedTarget(skipped, 1) == null

        def mixed = new Closure(this) {
            def doCall(Object a, String b) { "$a:$b" }
        }
        assert javaVarargsCall(mixed, 1, 'x') == '1:x'
        assert javaVarargsCall(mixed, 1, "${'x'}") == '1:x'
        assert handleFor(mixed, 2) != null

        def ambiguous = new Closure(this) {
            def doCall(String s) { "s:$s" }
            def doCall(Integer i) { "i:$i" }
        }
        assert javaVarargsCall(ambiguous, 'a') == 's:a'
        assert javaVarargsCall(ambiguous, 2) == 'i:2'
        assert cachedTarget(ambiguous, 1) == null
        assert handleFor(ambiguous, 1) == null
    }

    @Test
    void 'lookup keeps the most-derived doCall and boxes every primitive guard'() {
        def child = new ChildDoCall()
        assert javaVarargsCall(child, 'z') == 'child:z'
        assert handleFor(child, 1) != null

        def covariant = new StringDoCallClosure()
        assert StringDoCallClosure.declaredMethods.any { it.name == 'doCall' && it.bridge }
        assert javaVarargsCall(covariant, 7) == '7'
        assert handleFor(covariant, 1) != null
        assert !cachedTarget(covariant, 1).bridge

        def primitives = new PrimitiveDoCalls()
        assert javaVarargsCall(primitives, 1) == 2
        assert javaVarargsCall(primitives, 1L, 2L) == 3L
        assert javaVarargsCall(primitives, true, false, true) == true
        assert javaVarargsCall(primitives, 1.0d, 2.0d, 3.0d, 4.0d) == 10.0d
        assert handleFor(primitives, 1) != null
        assert handleFor(primitives, 2) != null
        assert handleFor(primitives, 3) != null
        assert handleFor(primitives, 4) != null

        def more = new MorePrimitiveDoCalls()
        assert javaVarargsCall(more, (char) 'A') == (char) 'A'
        assert javaVarargsCall(more, (byte) 1, (byte) 2) == (byte) 3
        assert javaVarargsCall(more, (short) 1, (short) 2, (short) 3) == (short) 6
        assert javaVarargsCall(more, 1.0f, 2.0f, 3.0f, 4.0f) == 10.0f
        assert handleFor(more, 1) != null
        assert handleFor(more, 2) != null
        assert handleFor(more, 3) != null
        assert handleFor(more, 4) != null
    }

    @Test
    void 'InvokerInvocationException from the metaclass is unwrapped'() {
        def c = { 1 }
        c.metaClass = new DelegatingMetaClass(c.metaClass) {
            @Override
            Object invokeMethod(Object object, String methodName, Object[] arguments) {
                throw new InvokerInvocationException(new IllegalStateException('via-mop'))
            }
        }
        def e = shouldFail(IllegalStateException) {
            javaVarargsCall(c)
        }
        assert e.message == 'via-mop'
    }

    @Test
    void 'an active category or replaced metaclass skips the cached handle'() {
        def c = { x -> "v:$x" }
        assert javaVarargsCall(c, 'a') == 'v:a'
        use(HandleTestCategory) {
            assert javaVarargsCall(c, 'b') == 'v:b'
        }
        c.metaClass = c.metaClass
        assert javaVarargsCall(c, 'c') == 'v:c'
    }

    @Test
    void 'lookup helpers handle non-closure types and unboxable leftovers'() {
        Class<?> callOverride = Class.forName('groovy.lang.Closure$CallOverride')
        Method lookup = callOverride.getDeclaredMethod('lookup', Class)
        lookup.accessible = true
        assert lookup.invoke(null, String).is(callOverrideNone())

        Method findOverride = callOverride.getDeclaredMethod('findOverride', Class, Class[])
        findOverride.accessible = true
        assert findOverride.invoke(null, Closure, [String] as Class[]) == null

        Method wrapperOf = callOverride.getDeclaredMethod('wrapperOf', Class)
        wrapperOf.accessible = true
        assert wrapperOf.invoke(null, void) == void
        assert wrapperOf.invoke(null, String) == String
        assert wrapperOf.invoke(null, float) == Float
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

    private static Object javaVarargsCall(Closure c, Object... args) {
        Method m = Closure.getMethod('call', Object[].class)
        try {
            return m.invoke(c, new Object[]{args})
        } catch (InvocationTargetException ite) {
            throw ite.cause
        }
    }

    private static Object callOverrideNone() {
        def none = Class.forName('groovy.lang.Closure$CallOverride').getDeclaredField('NONE')
        none.accessible = true
        return none.get(null)
    }

    private static Object callOverrideOf(Closure closure) {
        def table = Closure.getDeclaredField('CALL_OVERRIDES')
        table.accessible = true
        return table.get(null).get(closure.getClass())
    }

    private static void clearHandle(Closure closure, int arity) {
        Object[] slots = handleSlots(closure)
        slots[arity] = null
    }

    private static Object[] handleSlots(Closure closure) {
        Class<?> callOverride = Class.forName('groovy.lang.Closure$CallOverride')
        def table = Closure.getDeclaredField('CALL_OVERRIDES')
        table.accessible = true
        def override = table.get(null).get(closure.getClass())
        def f = callOverride.getDeclaredField('handles')
        f.accessible = true
        return (Object[]) f.get(override)
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

class HandleTestCategory {
    static Object identity(Object self) { self }
}

class ParentDoCall extends Closure {
    ParentDoCall() { super(null) }

    def doCall(Object o) { "parent:$o" }
}

class ChildDoCall extends ParentDoCall {
    def doCall(Object o) { "child:$o" }
}

class PrimitiveDoCalls extends Closure {
    PrimitiveDoCalls() { super(null) }

    int doCall(int x) { x + 1 }

    long doCall(long a, long b) { a + b }

    boolean doCall(boolean a, boolean b, boolean c) { a || b || c }

    double doCall(double a, double b, double c, double d) { a + b + c + d }
}

class MorePrimitiveDoCalls extends Closure {
    MorePrimitiveDoCalls() { super(null) }

    char doCall(char x) { x }

    byte doCall(byte a, byte b) { (byte) (a + b) }

    short doCall(short a, short b, short c) { (short) (a + b + c) }

    float doCall(float a, float b, float c, float d) { a + b + c + d }
}
