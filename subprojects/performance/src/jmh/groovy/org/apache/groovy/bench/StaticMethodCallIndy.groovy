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

    static int staticAdd(int a, int b) {
        return a + b
    }

    static int staticSum(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = staticAdd(sum, i)
        }
        return sum
    }

    static int staticFib(int n) {
        if (n < 2) return n
        return staticFib(n - 1) + staticFib(n - 2)
    }

    static int staticSquare(int x) { return x * x }
    static int staticIncrement(int x) { return x + 1 }
    static int staticDouble(int x) { return x * 2 }

    static int staticChain(int x) {
        return staticDouble(staticIncrement(staticSquare(x)))
    }

    // ---------- Instance methods — no early setTarget ----------

    int instanceAdd(int a, int b) {
        return a + b
    }

    int instanceSum(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = instanceAdd(sum, i)
        }
        return sum
    }

    int instanceFib(int n) {
        if (n < 2) return n
        return instanceFib(n - 1) + instanceFib(n - 2)
    }

    int instanceSquare(int x) { return x * x }
    int instanceIncrement(int x) { return x + 1 }
    int instanceDouble(int x) { return x * 2 }

    int instanceChain(int x) {
        return instanceDouble(instanceIncrement(instanceSquare(x)))
    }

    // ---------- @CompileStatic — invokestatic, no invokedynamic ----------

    @CompileStatic
    static int staticAddCS(int a, int b) {
        return a + b
    }

    @CompileStatic
    static int staticSumCS(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = staticAddCS(sum, i)
        }
        return sum
    }

    @CompileStatic
    static int staticFibCS(int n) {
        if (n < 2) return n
        return staticFibCS(n - 1) + staticFibCS(n - 2)
    }

    @CompileStatic
    static int staticSquareCS(int x) { return x * x }

    @CompileStatic
    static int staticIncrementCS(int x) { return x + 1 }

    @CompileStatic
    static int staticDoubleCS(int x) { return x * 2 }

    @CompileStatic
    static int staticChainCS(int x) {
        return staticDoubleCS(staticIncrementCS(staticSquareCS(x)))
    }
}
