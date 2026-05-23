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

/**
 * Private-method counterpart to {@link StaticMethodCallIndy}.
 * <p>
 * Dispatch stays dynamic at the call site, but the selected target is lexically fixed because
 * the helper methods are {@code private}. This makes it a good probe for eager relink decisions
 * that should not require an exact-final receiver type.
 */
class PrivateInstanceMethodCallIndy {

    private int instanceAdd(int a, int b) {
        return a + b
    }

    int instanceSum(int n) {
        int sum = 0
        for (int i = 0; i < n; i++) {
            sum = instanceAdd(sum, i)
        }
        return sum
    }

    private int instanceFib0(int n) {
        if (n < 2) return n
        return instanceFib0(n - 1) + instanceFib0(n - 2)
    }

    int instanceFib(int n) {
        return instanceFib0(n)
    }

    private int instanceSquare(int x) { return x * x }

    private int instanceIncrement(int x) { return x + 1 }

    private int instanceDouble(int x) { return x * 2 }

    int instanceChain(int x) {
        return instanceDouble(instanceIncrement(instanceSquare(x)))
    }
}
