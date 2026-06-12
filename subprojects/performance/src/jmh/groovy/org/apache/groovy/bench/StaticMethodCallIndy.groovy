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

package org.apache.groovy.bench

import groovy.transform.CompileStatic

/**
 * Helper class for {@link StaticMethodCallIndyBench}.
 * <p>
 * Dynamic methods emit {@code invokedynamic}
 * call sites whose targets are resolved through {@code IndyInterface}.
 * Static method calls benefit from the early {@code setTarget} optimization
 * because the receiver is always a {@code Class} object, allowing the JIT
 * compiler to inline the target sooner.
 */
class StaticMethodCallIndy {

    // ---------- Dynamic Groovy — invokedynamic call sites ----------

    /**
     * Adds two integers.
     * @param a the first operand
     * @param b the second operand
     * @return a plus b
     */
    static int staticAdd(int a, int b) {
        return a + b
    }

    /**
     * Computes sum from 0 to n-1 by repeated static method calls.
     * @param n the upper bound (exclusive)
     * @return the sum
     */
    static int staticSum(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = staticAdd(sum, i)
        }
        return sum
    }

    /**
     * Computes the nth Fibonacci number recursively.
     * @param n the position in the sequence
     * @return the Fibonacci value
     */
    static int staticFib(int n) {
        if (n < 2) return n
        return staticFib(n - 1) + staticFib(n - 2)
    }

    /**
     * Squares an integer.
     * @param x the input value
     * @return x squared
     */
    static int staticSquare(int x) { return x * x }

    /**
     * Increments an integer by 1.
     * @param x the input value
     * @return x plus 1
     */
    static int staticIncrement(int x) { return x + 1 }

    /**
     * Doubles an integer.
     * @param x the input value
     * @return x times 2
     */
    static int staticDouble(int x) { return x * 2 }

    /**
     * Chains three static method calls.
     * @param x the input value
     * @return double(increment(square(x)))
     */
    static int staticChain(int x) {
        return staticDouble(staticIncrement(staticSquare(x)))
    }

    // ---------- Instance methods — no early setTarget ----------

    /**
     * Adds two integers (instance method).
     * @param a the first operand
     * @param b the second operand
     * @return a plus b
     */
    int instanceAdd(int a, int b) {
        return a + b
    }

    /**
     * Computes sum from 0 to n-1 by repeated instance method calls.
     * @param n the upper bound (exclusive)
     * @return the sum
     */
    int instanceSum(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = instanceAdd(sum, i)
        }
        return sum
    }

    /**
     * Computes the nth Fibonacci number recursively (instance method).
     * @param n the position in the sequence
     * @return the Fibonacci value
     */
    int instanceFib(int n) {
        if (n < 2) return n
        return instanceFib(n - 1) + instanceFib(n - 2)
    }

    /**
     * Squares an integer (instance method).
     * @param x the input value
     * @return x squared
     */
    int instanceSquare(int x) { return x * x }

    /**
     * Increments an integer by 1 (instance method).
     * @param x the input value
     * @return x plus 1
     */
    int instanceIncrement(int x) { return x + 1 }

    /**
     * Doubles an integer (instance method).
     * @param x the input value
     * @return x times 2
     */
    int instanceDouble(int x) { return x * 2 }

    /**
     * Chains three instance method calls.
     * @param x the input value
     * @return double(increment(square(x)))
     */
    int instanceChain(int x) {
        return instanceDouble(instanceIncrement(instanceSquare(x)))
    }

    // ---------- @CompileStatic — invokestatic, no invokedynamic ----------

    /**
     * Adds two integers ({@code @CompileStatic}).
     * @param a the first operand
     * @param b the second operand
     * @return a plus b
     */
    @CompileStatic
    static int staticAddCS(int a, int b) {
        return a + b
    }

    /**
     * Computes sum from 0 to n-1 ({@code @CompileStatic}).
     * @param n the upper bound (exclusive)
     * @return the sum
     */
    @CompileStatic
    static int staticSumCS(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = staticAddCS(sum, i)
        }
        return sum
    }

    /**
     * Computes the nth Fibonacci number recursively ({@code @CompileStatic}).
     * @param n the position in the sequence
     * @return the Fibonacci value
     */
    @CompileStatic
    static int staticFibCS(int n) {
        if (n < 2) return n
        return staticFibCS(n - 1) + staticFibCS(n - 2)
    }

    /**
     * Squares an integer ({@code @CompileStatic}).
     * @param x the input value
     * @return x squared
     */
    @CompileStatic
    static int staticSquareCS(int x) { return x * x }

    /**
     * Increments an integer by 1 ({@code @CompileStatic}).
     * @param x the input value
     * @return x plus 1
     */
    @CompileStatic
    static int staticIncrementCS(int x) { return x + 1 }

    /**
     * Doubles an integer ({@code @CompileStatic}).
     * @param x the input value
     * @return x times 2
     */
    @CompileStatic
    static int staticDoubleCS(int x) { return x * 2 }

    /**
     * Chains three static method calls ({@code @CompileStatic}).
     * @param x the input value
     * @return double(increment(square(x)))
     */
    @CompileStatic
    static int staticChainCS(int x) {
        return staticDoubleCS(staticIncrementCS(staticSquareCS(x)))
    }
}
