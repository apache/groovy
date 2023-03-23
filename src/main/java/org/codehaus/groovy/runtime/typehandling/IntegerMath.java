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
package org.codehaus.groovy.runtime.typehandling;

/**
 * Integer NumberMath operations
 */
public final class IntegerMath extends NumberMath {

    public static final IntegerMath INSTANCE = new IntegerMath();

    private IntegerMath() {}

    @Override
    public Number absImpl(Number number) {
        return Math.abs(number.intValue());
    }

    @Override
    public Number addImpl(Number left, Number right) {
        return left.intValue() + right.intValue();
    }

    @Override
    public Number subtractImpl(Number left, Number right) {
        return left.intValue() - right.intValue();
    }

    @Override
    public Number multiplyImpl(Number left, Number right) {
        return left.intValue() * right.intValue();
    }

    @Override
    public Number divideImpl(Number left, Number right) {
        return BigDecimalMath.INSTANCE.divideImpl(left, right);
    }

    @Override
    public int compareToImpl(Number left, Number right) {
        int leftVal = left.intValue();
        int rightVal = right.intValue();
        return Integer.compare(leftVal, rightVal);
    }

    @Override
    public Number orImpl(Number left, Number right) {
        return left.intValue() | right.intValue();
    }

    @Override
    public Number andImpl(Number left, Number right) {
        return left.intValue() & right.intValue();
    }

    @Override
    public Number xorImpl(Number left, Number right) {
        return left.intValue() ^ right.intValue();
    }

    @Override
    public Number intdivImpl(Number left, Number right) {
        return left.intValue() / right.intValue();
    }

    @Override
    public Number modImpl(Number left, Number right) {
        return toBigInteger(left).mod(toBigInteger(right)).intValue();
    }

    @Override
    public Number remainderImpl(Number left, Number right) {
        return left.intValue() % right.intValue();
    }

    @Override
    public Number unaryMinusImpl(Number left) {
        return -left.intValue();
    }

    @Override
    public Number unaryPlusImpl(Number left) {
        return left.intValue();
    }

    @Override
    public Number bitwiseNegateImpl(Number left) {
        return ~left.intValue();
    }

    @Override
    public Number leftShiftImpl(Number left, Number right) {
        return left.intValue() << right.intValue();
    }

    @Override
    public Number rightShiftImpl(Number left, Number right) {
        return left.intValue() >> right.intValue();
    }

    @Override
    public Number rightShiftUnsignedImpl(Number left, Number right) {
        return left.intValue() >>> right.intValue();
    }
}
