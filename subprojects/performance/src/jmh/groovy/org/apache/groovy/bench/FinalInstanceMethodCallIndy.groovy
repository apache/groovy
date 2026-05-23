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
 * Final-instance counterpart to {@link StaticMethodCallIndy}.
 * <p>
 * The receiver type is exact and final at the indy call site, which makes it a good probe for
 * earlier relink heuristics that are still too broad to fire on the first hit.
 */
final class FinalInstanceMethodCallIndy {

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
}
