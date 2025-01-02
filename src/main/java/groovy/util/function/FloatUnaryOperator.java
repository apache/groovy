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
import java.util.function.UnaryOperator;

/**
 * Represents an operation on a single {@code float}-valued operand that produces
 * a {@code float}-valued result.  This is the primitive type specialization of
 * {@link UnaryOperator} for {@code float}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsFloat(float)}.
 *
 * @see UnaryOperator
 * @since 5.0.0
 */
@FunctionalInterface
public interface FloatUnaryOperator {

    float applyAsFloat(float operand);

    default FloatUnaryOperator andThen(FloatUnaryOperator after) {
        Objects.requireNonNull(after);
        return (float f) -> after.applyAsFloat(applyAsFloat(f));
    }

    default FloatUnaryOperator compose(FloatUnaryOperator before) {
        Objects.requireNonNull(before);
        return (float f) -> applyAsFloat(before.applyAsFloat(f));
    }

    static FloatUnaryOperator identity() {
        return f -> f;
    }
}
