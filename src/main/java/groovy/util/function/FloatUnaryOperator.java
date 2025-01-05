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
package groovy.util.function;

import java.util.Objects;

/**
 * Represents an operation that takes a {@code float}-valued operand and produces
 * a {@code float}-valued result.  This is a specialization of
 * {@link java.util.function.UnaryOperator} for {@code float}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsFloat(float)}.
 *
 * @see java.util.function.UnaryOperator
 * @since 5.0.0
 */
@FunctionalInterface
public interface FloatUnaryOperator {
    /**
     * Applies this operator.
     *
     * @param operand the operand
     * @return the operator result
     */
    float applyAsFloat(float operand);

    /**
     * Creates a "backward" composed operator that first applies the {@code before}
     * operator to its input, and then applies this operator to the result.
     *
     * @param before the operator to apply before this operator is applied
     * @return the composed operator
     * @throws NullPointerException if before is null
     *
     * @see #andThen(FloatUnaryOperator)
     */
    default FloatUnaryOperator compose(FloatUnaryOperator before) {
        Objects.requireNonNull(before);
        return (float f) -> applyAsFloat(before.applyAsFloat(f));
    }

    /**
     * Creates a "forward" composed operator that first applies this operator to
     * its input, and then applies the {@code after} operator to the result.
     *
     * @param after the operator to apply after this operator is applied
     * @return the composed operator
     * @throws NullPointerException if after is null
     *
     * @see #compose(FloatUnaryOperator)
     */
    default FloatUnaryOperator andThen(FloatUnaryOperator after) {
        Objects.requireNonNull(after);
        return (float f) -> after.applyAsFloat(applyAsFloat(f));
    }

    /**
     * Returns the identity float unary operator.
     *
     * @return a unary operator that always returns its input argument
     */
    static FloatUnaryOperator identity() {
        return f -> f;
    }
}
