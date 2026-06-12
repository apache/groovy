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
 * Helper class providing Fibonacci function implementation for benchmarking.
 */
class Fibo {

    /**
     * Computes the nth Fibonacci number recursively.
     * @param n the position in the Fibonacci sequence
     * @return the Fibonacci value at position n
     */
    static int fib(int n) {
        if (n < 2) return 1
        return fib(n - 2) + fib(n - 1)
    }

}
