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

import org.apache.groovy.util.SystemUtil;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * BigDecimal NumberMath operations
 */
public final class BigDecimalMath extends NumberMath {

    // This is an arbitrary value, picked as a reasonable choice for a precision
    // for typical user math when a non-terminating result would otherwise occur.
    public static final int DIVISION_EXTRA_PRECISION = SystemUtil.getIntegerSafe("groovy.division.extra.precision", 10);

    //This is an arbitrary value, picked as a reasonable choice for a rounding point
    //for typical user math.
    public static final int DIVISION_MIN_SCALE = SystemUtil.getIntegerSafe("groovy.division.min.scale", 10);

    public static final BigDecimalMath INSTANCE = new BigDecimalMath();

    private BigDecimalMath() {}

    protected Number absImpl(Number number) {
        return toBigDecimal(number).abs();
    }

    public Number addImpl(Number left, Number right) {
        return toBigDecimal(left).add(toBigDecimal(right));
    }

    public Number subtractImpl(Number left, Number right) {
        return toBigDecimal(left).subtract(toBigDecimal(right));
    }

    public Number multiplyImpl(Number left, Number right) {
        return toBigDecimal(left).multiply(toBigDecimal(right));
    }

    public Number divideImpl(Number left, Number right) {
        BigDecimal bigLeft = toBigDecimal(left);
        BigDecimal bigRight = toBigDecimal(right);
        try {
            return bigLeft.divide(bigRight);
        } catch (ArithmeticException e) {
            // set a DEFAULT precision if otherwise non-terminating
            int precision = Math.max(bigLeft.precision(), bigRight.precision()) + DIVISION_EXTRA_PRECISION;
            BigDecimal result = bigLeft.divide(bigRight, new MathContext(precision));
            int scale = Math.max(Math.max(bigLeft.scale(), bigRight.scale()), DIVISION_MIN_SCALE);
            if (result.scale() > scale) result = result.setScale(scale, BigDecimal.ROUND_HALF_UP);
            return result;
        }
    }

    public int compareToImpl(Number left, Number right) {
        return toBigDecimal(left).compareTo(toBigDecimal(right));
    }

    protected Number unaryMinusImpl(Number left) {
        return toBigDecimal(left).negate();
    }

    protected Number unaryPlusImpl(Number left) {
        return toBigDecimal(left);
    }
}
