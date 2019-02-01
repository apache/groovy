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
                    
    protected Number absImpl(Number number) {
        return Math.abs(number.intValue());
    }
    
    public Number addImpl(Number left, Number right) {
        return left.intValue() + right.intValue();
    }

    public Number subtractImpl(Number left, Number right) {
        return left.intValue() - right.intValue();
    }

    public Number multiplyImpl(Number left, Number right) {
        return left.intValue() * right.intValue();
    }

    public Number divideImpl(Number left, Number right) {
        return BigDecimalMath.INSTANCE.divideImpl(left, right);
    }
    
    public int compareToImpl(Number left, Number right) {
        int leftVal = left.intValue();
        int rightVal = right.intValue();
        return Integer.compare(leftVal, rightVal);
    }

    protected Number orImpl(Number left, Number right) {
        return left.intValue() | right.intValue();
    }

    protected Number andImpl(Number left, Number right) {
        return left.intValue() & right.intValue();
    }

    protected Number xorImpl(Number left, Number right) {
        return left.intValue() ^ right.intValue();
    }

    protected Number intdivImpl(Number left, Number right) {
        return left.intValue() / right.intValue();
    }
    
    protected Number modImpl(Number left, Number right) {
        return left.intValue() % right.intValue();
    }

    protected Number unaryMinusImpl(Number left) {
        return -left.intValue();
    }

    protected Number unaryPlusImpl(Number left) {
        return left.intValue();
    }

    protected Number bitwiseNegateImpl(Number left) {
        return ~left.intValue();
    }

    protected Number leftShiftImpl(Number left, Number right) {
        return left.intValue() << right.intValue();
    }

    protected Number rightShiftImpl(Number left, Number right) {
        return left.intValue() >> right.intValue();
    }

    protected Number rightShiftUnsignedImpl(Number left, Number right) {
        return left.intValue() >>> right.intValue();
    }
}
