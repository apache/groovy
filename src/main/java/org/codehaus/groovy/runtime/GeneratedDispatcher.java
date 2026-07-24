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
package org.codehaus.groovy.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A per-class table of compiler-generated dispatch targets, reached by a compact
 * integer id instead of a {@link java.lang.invoke.MethodHandle}.
 * <p>
 * The compiler emits per class: an array-shaped table ({@code $packedDispatch$(int, Object[])},
 * covering every target) plus array-free per-arity tables ({@code $packedDispatch1$},
 * {@code $packedDispatch2$}) for the hot shapes taking one or two values beyond the receiver —
 * avoiding the packed argument array, which cannot be scalar-replaced when the dispatch call
 * does not inline. Each table is a switch over the id whose case casts the arguments to the
 * target's declared parameter types and invokes it directly, so the whole chain is ordinary,
 * JIT-friendly bytecode. A single {@code invokedynamic} accessor per class links all three
 * through {@link #bootstrap} into one constant {@link Bundle}, created lazily on first use.
 * <p>
 * Adapters such as {@link PackedClosure} hold {@code (dispatchers, id)} and make a plain
 * interface call per invocation. Unlike a {@code MethodHandle} held in an instance field —
 * which the JIT cannot treat as a constant, so every call runs the (comparatively slow)
 * method-handle invoker — this executes as a cheap indirect call, and inlines fully when the
 * call site is monomorphic. The id space is not limited to packed closure bodies: any
 * statically-known target the compiler wants to reach through a shared adapter (for example a
 * statically-resolved method reference) can claim an id in the same tables.
 *
 * @since 6.0.0
 */
@FunctionalInterface
public interface GeneratedDispatcher {

    /** Name of the generated array-shaped dispatch table method. */
    String TABLE_METHOD = "$packedDispatch$";
    /** Name of the generated one-value dispatch table method. */
    String TABLE1_METHOD = "$packedDispatch1$";
    /** Name of the generated two-value dispatch table method. */
    String TABLE2_METHOD = "$packedDispatch2$";

    /**
     * Invokes the dispatch target registered under {@code id} in the hosting class.
     *
     * @param id   the compile-time-assigned index of the target in the hosting class's table
     * @param args the packed argument array: {@code args[0]} is the receiver (the adapter's
     *             owner — ignored by static targets), followed by any captured values, then
     *             the call arguments, all in the target's declared parameter order
     * @return the target's return value ({@code null} for a void target)
     */
    Object dispatch(int id, Object[] args);

    /**
     * Array-free companion for targets taking exactly one value beyond the receiver.
     * A separate single-method interface so each table is adapted by one
     * {@code LambdaMetafactory} linkage.
     */
    @FunctionalInterface
    interface Arity1 {
        /**
         * Invokes the target registered under {@code id} whose captured-plus-argument
         * count is exactly one.
         *
         * @param id    the compile-time-assigned index of the target
         * @param owner the receiver (ignored by static targets)
         * @param a     the single captured value or call argument
         * @return the target's return value
         */
        Object dispatch1(int id, Object owner, Object a);
    }

    /**
     * Array-free companion for targets taking exactly two values beyond the receiver.
     * @see Arity1
     */
    @FunctionalInterface
    interface Arity2 {
        /**
         * Invokes the target registered under {@code id} whose captured-plus-argument
         * count is exactly two.
         *
         * @param id    the compile-time-assigned index of the target
         * @param owner the receiver (ignored by static targets)
         * @param a     the first captured value or call argument
         * @param b     the second captured value or call argument
         * @return the target's return value
         */
        Object dispatch2(int id, Object owner, Object a, Object b);
    }

    /**
     * The hosting class's three dispatch shapes, linked once by {@link #bootstrap} and shared
     * by all of that class's adapters.
     */
    final class Bundle {
        final GeneratedDispatcher dispatcher;
        final Arity1 arity1;
        final Arity2 arity2;

        Bundle(final GeneratedDispatcher dispatcher, final Arity1 arity1, final Arity2 arity2) {
            this.dispatcher = dispatcher;
            this.arity1 = arity1;
            this.arity2 = arity2;
        }

        /** The array-shaped dispatcher, covering every target. */
        public GeneratedDispatcher dispatcher() { return dispatcher; }
        /** The one-value dispatcher. */
        public Arity1 arity1() { return arity1; }
        /** The two-value dispatcher. */
        public Arity2 arity2() { return arity2; }
    }

    /**
     * Invokedynamic bootstrap for the hosting class's dispatcher accessor: adapts the class's
     * three private static dispatch tables to their functional interfaces (one hidden class
     * each, via {@code LambdaMetafactory} with the caller's full-privilege lookup) and returns
     * them as one constant {@link Bundle}. Linked once per class, on first adapter creation.
     *
     * @param caller the hosting class's lookup (supplied by the JVM)
     * @param name   the invoked name (unused)
     * @param type   the accessor's type: {@code () -> Bundle}
     * @return a constant call site producing the bundle
     * @throws Throwable if the tables cannot be found or linked (a compiler bug)
     */
    /**
     * Constant-dynamic bootstrap for a closure literal's declared parameter types: decodes a
     * method descriptor (which, unlike {@code Class} constants, can carry primitive types) with
     * the hosting class's loader into a {@code Class[]} resolved once per literal site and shared
     * by every adapter created there — closure creation then loads one constant instead of
     * allocating and filling an array. Sharing matches generated closure classes, whose
     * parameter-type arrays are likewise cached per class.
     *
     * @param caller     the hosting class's lookup (supplied by the JVM)
     * @param name       the constant's name (unused)
     * @param type       the constant's type: {@code Class[]}
     * @param descriptor a method descriptor whose parameter types are the closure's declared
     *                   parameter types (the return type is ignored)
     * @return the declared parameter types
     */
    static Class<?>[] paramTypes(final MethodHandles.Lookup caller, final String name, final Class<?> type, final String descriptor) {
        return MethodType.fromMethodDescriptorString(descriptor, caller.lookupClass().getClassLoader()).parameterArray();
    }

    static CallSite bootstrap(final MethodHandles.Lookup caller, final String name, final MethodType type) throws Throwable {
        Class<?> host = caller.lookupClass();
        MethodType arrayType = MethodType.methodType(Object.class, int.class, Object[].class);
        MethodType oneType = MethodType.methodType(Object.class, int.class, Object.class, Object.class);
        MethodType twoType = MethodType.methodType(Object.class, int.class, Object.class, Object.class, Object.class);
        GeneratedDispatcher dispatcher = (GeneratedDispatcher) LambdaMetafactory.metafactory(
                caller, "dispatch", MethodType.methodType(GeneratedDispatcher.class),
                arrayType, caller.findStatic(host, TABLE_METHOD, arrayType), arrayType).getTarget().invokeExact();
        Arity1 arity1 = (Arity1) LambdaMetafactory.metafactory(
                caller, "dispatch1", MethodType.methodType(Arity1.class),
                oneType, caller.findStatic(host, TABLE1_METHOD, oneType), oneType).getTarget().invokeExact();
        Arity2 arity2 = (Arity2) LambdaMetafactory.metafactory(
                caller, "dispatch2", MethodType.methodType(Arity2.class),
                twoType, caller.findStatic(host, TABLE2_METHOD, twoType), twoType).getTarget().invokeExact();
        return new ConstantCallSite(MethodHandles.constant(Bundle.class, new Bundle(dispatcher, arity1, arity2)));
    }
}
