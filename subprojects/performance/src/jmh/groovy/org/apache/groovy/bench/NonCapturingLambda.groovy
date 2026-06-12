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

import java.util.function.Function
import java.util.function.IntUnaryOperator
import java.util.stream.IntStream

/**
 * Helper class for benchmarking non-capturing lambda optimization (GROOVY-11905).
 */
@CompileStatic
class NonCapturingLambda {

    /**
     * Applies a non-capturing lambda that adds 1 to the input.
     * @param x the input value
     * @return x plus 1
     */
    static int applyNonCapturingLambda(int x) {
        IntUnaryOperator op = (int i) -> i + 1
        op.applyAsInt(x)
    }

    /**
     * Applies a capturing lambda that adds an offset to the input.
     * @param x the input value
     * @return x plus the captured offset
     */
    static int applyCapturingLambda(int x) {
        int offset = 1
        IntUnaryOperator op = (int i) -> i + offset
        op.applyAsInt(x)
    }

    /**
     * Maps each element using a non-capturing lambda.
     * @param input the input list
     * @return a new list with each element incremented by 1
     */
    static List<Integer> streamMapNonCapturing(List<Integer> input) {
        input.stream().map(e -> (Integer) (e + 1)).toList()
    }

    /**
     * Reduces a range using a non-capturing lambda.
     * @param n the upper bound of the range (inclusive)
     * @return the sum of doubled values from 1 to n
     */
    static int streamReduceNonCapturing(int n) {
        IntStream.rangeClosed(1, n).map((int i) -> i * 2).sum()
    }
}
