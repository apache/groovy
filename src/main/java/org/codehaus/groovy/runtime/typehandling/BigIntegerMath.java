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
 * BigInteger NumberMath operations
 */
public final class BigIntegerMath extends NumberMath {

    public static final BigIntegerMath INSTANCE = new BigIntegerMath();
    
    private BigIntegerMath() {}

    protected Number absImpl(Number number) {
        return toBigInteger(number).abs();
    }
    
    public Number addImpl(Number left, Number right) {
        return toBigInteger(left).add(toBigInteger(right));
    }
    public Number subtractImpl(Number left, Number right) {
        return toBigInteger(left).subtract(toBigInteger(right));
    }

    public Number multiplyImpl(Number left, Number right) {
        return toBigInteger(left).multiply(toBigInteger(right));
    }

    public Number divideImpl(Number left, Number right) {
        return BigDecimalMath.INSTANCE.divideImpl(left, right);
    }
    
    public int compareToImpl(Number left, Number right) {
        return toBigInteger(left).compareTo(toBigInteger(right));
    }

    protected Number intdivImpl(Number left, Number right) {
        return toBigInteger(left).divide(toBigInteger(right));
    }
    
    protected Number modImpl(Number left, Number right) {
        return toBigInteger(left).mod(toBigInteger(right));
    }
    
    protected Number unaryMinusImpl(Number left) {
        return toBigInteger(left).negate();
    }

    protected Number unaryPlusImpl(Number left) {
        return toBigInteger(left);
    }

    protected Number bitwiseNegateImpl(Number left) {
        return toBigInteger(left).not();
    }

    protected Number orImpl(Number left, Number right) {
        return toBigInteger(left).or(toBigInteger(right));
    }

    protected Number andImpl(Number left, Number right) {
        return toBigInteger(left).and(toBigInteger(right));
    }
    
    protected Number xorImpl(Number left, Number right) {
        return toBigInteger(left).xor(toBigInteger(right));
    }

    protected Number leftShiftImpl(Number left, Number right) {
        return toBigInteger(left).shiftLeft(right.intValue());
    }

    protected Number rightShiftImpl(Number left, Number right) {
        return toBigInteger(left).shiftRight(right.intValue());
    }
}
