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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Stateless objects used to perform math on the various Number subclasses.
 * Instances are required so that polymorphic calls work properly, but each
 * subclass creates a singleton instance to minimize garbage.  All methods
 * must be thread-safe.
 *
 * The design goals of this class are as follows:
 * <ol>
 * <li>Support a 'least surprising' math model to scripting language users.  This
 * means that exact, or decimal math should be used for default calculations.  This
 * scheme assumes that by default, groovy literals with decimal points are instantiated
 * as BigDecimal objects rather than binary floating points (Float, Double).
 * <li>Do not force the appearance of exactness on a number that is by definition not
 * guaranteed to be exact.  In particular this means that if an operand in a NumberMath
 * operation is a binary floating point number, ensure that the result remains a binary floating point
 * number (i.e. never automatically promote a binary floating point number to a BigDecimal).
 * This has the effect of preserving the expectations of binary floating point users and helps performance.
 * <li>Provide an implementation that is as close as practical to the Java 1.5 BigDecimal math model which implements
 * precision based floating point decimal math (ANSI X3.274-1996 and ANSI X3.274-1996/AM 1-2000 (section 7.4).
 * </ol>
 */
public abstract class NumberMath {

    /**
     * Computes the absolute value of a number using its specific type's math operations.
     *
     * @param number the number (must not be null)
     * @return the absolute value using the appropriate NumberMath instance
     */
    public static Number abs(Number number) {
        return getMath(number).absImpl(number);
    }

    /**
     * Adds two numbers using type promotion rules.
     *
     * @param left the left operand
     * @param right the right operand
     * @return the sum using the appropriate NumberMath instance
     */
    public static Number add(Number left, Number right) {
        return getMath(left, right).addImpl(left, right);
    }

    /**
     * Subtracts two numbers using type promotion rules.
     *
     * @param left the minuend
     * @param right the subtrahend
     * @return the difference using the appropriate NumberMath instance
     */
    public static Number subtract(Number left, Number right) {
        return getMath(left, right).subtractImpl(left, right);
    }

    /**
     * Multiplies two numbers using type promotion rules.
     *
     * @param left the first multiplicand
     * @param right the second multiplicand
     * @return the product using the appropriate NumberMath instance
     */
    public static Number multiply(Number left, Number right) {
        return getMath(left, right).multiplyImpl(left, right);
    }

    /**
     * Divides two numbers using type promotion rules.
     * <p>
     * For floating-point operands, performs binary floating-point division.
     * For integral types, delegates to BigDecimal division for exact results.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the quotient using the appropriate NumberMath instance
     * @throws ArithmeticException if right is zero
     */
    public static Number divide(Number left, Number right) {
        return getMath(left, right).divideImpl(left, right);
    }

    /**
     * Compares two numbers using type promotion rules.
     *
     * @param left the first number
     * @param right the second number
     * @return a negative, zero, or positive int as left is less than, equal to, or greater than right
     */
    public static int compareTo(Number left, Number right) {
        return getMath(left, right).compareToImpl(left, right);
    }

    /**
     * Bitwise OR operation on two integral numbers.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise OR result
     * @throws UnsupportedOperationException if operands are not integral types
     */
    public static Number or(Number left, Number right) {
        return getMath(left, right).orImpl(left, right);
    }

    /**
     * Bitwise AND operation on two integral numbers.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise AND result
     * @throws UnsupportedOperationException if operands are not integral types
     */
    public static Number and(Number left, Number right) {
        return getMath(left, right).andImpl(left, right);
    }

    /**
     * Bitwise XOR operation on two integral numbers.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise XOR result
     * @throws UnsupportedOperationException if operands are not integral types
     */
    public static Number xor(Number left, Number right) {
        return getMath(left, right).xorImpl(left, right);
    }

    /**
     * Integer division (without remainder) of two numbers.
     * <p>
     * For BigInteger and integral types, performs exact integer division.
     * For floating-point operands, delegates to FloatingPointMath.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the integer quotient
     * @throws UnsupportedOperationException if operands don't support integer division
     */
    public static Number intdiv(Number left, Number right) {
        return getMath(left, right).intdivImpl(left, right);
    }

    /**
     * Modulo operation on two numbers (for backwards compatibility).
     * <p>
     * This method is retained for backwards compatibility
     *
     * @param left the dividend
     * @param right the divisor
     * @return the modulo result
     */
    public static Number mod(Number left, Number right) {
        return getMath(left, right).modImpl(left, right);
    }

    /**
     * Remainder operation on two numbers.
     * <p>
     * Returns the remainder after integer division.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the remainder
     * @throws UnsupportedOperationException if operands don't support remainder operation
     */
    public static Number remainder(Number left, Number right) {
        return getMath(left, right).remainderImpl(left, right);
    }

    /**
     * Left shift operation on integral numbers.
     * <p>
     * The shift distance (right operand) must be an integral type.
     * The value to shift (left operand) must also be integral.
     *
     * @param left the value to shift (must be integral)
     * @param right the shift distance (must be integral)
     * @return the left-shifted value
     * @throws UnsupportedOperationException if shift distance is floating-point or BigDecimal
     */
    public static Number leftShift(Number left, Number right) {
        if (isFloatingPoint(right) || isBigDecimal(right)) {
            throw new UnsupportedOperationException("Shift distance must be an integral type, but " + right + " (" + right.getClass().getName() + ") was supplied");
        }
        return getMath(left).leftShiftImpl(left, right);
    }

    /**
     * Right shift operation on integral numbers.
     * <p>
     * The shift distance (right operand) must be an integral type.
     * The value to shift (left operand) must also be integral.
     *
     * @param left the value to shift (must be integral)
     * @param right the shift distance (must be integral)
     * @return the right-shifted value
     * @throws UnsupportedOperationException if shift distance is floating-point or BigDecimal
     */
    public static Number rightShift(Number left, Number right) {
        if (isFloatingPoint(right) || isBigDecimal(right)) {
            throw new UnsupportedOperationException("Shift distance must be an integral type, but " + right + " (" + right.getClass().getName() + ") was supplied");
        }
        return getMath(left).rightShiftImpl(left, right);
    }

    /**
     * Unsigned right shift operation on integral numbers.
     * <p>
     * The shift distance (right operand) must be an integral type.
     * The value to shift (left operand) must also be integral.
     *
     * @param left the value to shift (must be integral)
     * @param right the shift distance (must be integral)
     * @return the unsigned right-shifted value
     * @throws UnsupportedOperationException if shift distance is floating-point or BigDecimal
     */
    public static Number rightShiftUnsigned(Number left, Number right) {
        if (isFloatingPoint(right) || isBigDecimal(right)) {
            throw new UnsupportedOperationException("Shift distance must be an integral type, but " + right + " (" + right.getClass().getName() + ") was supplied");
        }
        return getMath(left).rightShiftUnsignedImpl(left, right);
    }

    /**
     * Bitwise negation (complement) of an integral number.
     *
     * @param left the operand (must be integral)
     * @return the bitwise negation
     * @throws UnsupportedOperationException if operand is not integral
     */
    public static Number bitwiseNegate(Number left) {
        return getMath(left).bitwiseNegateImpl(left);
    }

    /**
     * Unary negation of a number.
     *
     * @param left the operand
     * @return the negation
     */
    public static Number unaryMinus(Number left) {
        return getMath(left).unaryMinusImpl(left);
    }

    /**
     * Unary plus (identity) operation on a number.
     *
     * @param left the operand
     * @return the same value
     */
    public static Number unaryPlus(Number left) {
        return getMath(left).unaryPlusImpl(left);
    }

    /**
     * Tests whether a number is a floating-point type (Float or Double).
     *
     * @param number the number to test
     * @return true if the number is Float or Double
     */
    public static boolean isFloatingPoint(Number number) {
        return number instanceof Double || number instanceof Float;
    }

    /**
     * Tests whether a number is an Integer.
     *
     * @param number the number to test
     * @return true if the number is an Integer
     */
    public static boolean isInteger(Number number) {
        return number instanceof Integer;
    }

    /**
     * Tests whether a number is a Short.
     *
     * @param number the number to test
     * @return true if the number is a Short
     */
    public static boolean isShort(Number number) {
        return number instanceof Short;
    }

    /**
     * Tests whether a number is a Byte.
     *
     * @param number the number to test
     * @return true if the number is a Byte
     */
    public static boolean isByte(Number number) {
        return number instanceof Byte;
    }

    /**
     * Tests whether a number is a Long.
     *
     * @param number the number to test
     * @return true if the number is a Long
     */
    public static boolean isLong(Number number) {
        return number instanceof Long;
    }

    /**
     * Tests whether a number is a BigDecimal.
     *
     * @param number the number to test
     * @return true if the number is a BigDecimal
     */
    public static boolean isBigDecimal(Number number) {
        return number instanceof BigDecimal;
    }

    /**
     * Tests whether a number is a BigInteger.
     *
     * @param number the number to test
     * @return true if the number is a BigInteger
     */
    public static boolean isBigInteger(Number number) {
        return number instanceof BigInteger;
    }

    /**
     * Converts a number to a BigDecimal.
     * <p>
     * Handles BigDecimal, BigInteger, integral types, and floating-point types.
     * Floating-point numbers are converted via their string representation to preserve precision.
     *
     * @param n the number to convert (must not be null)
     * @return a BigDecimal representation of the number
     */
    public static BigDecimal toBigDecimal(Number n) {
        if (n instanceof BigDecimal) {
            return (BigDecimal) n;
        }
        if (n instanceof BigInteger) {
            return new BigDecimal((BigInteger) n);
        }
        if (n instanceof Integer || n instanceof Long || n instanceof Byte || n instanceof Short) {
            return BigDecimal.valueOf(n.longValue());
        }
        try {
            return new BigDecimal(n.toString());
        } catch(NumberFormatException nfe) {
            return BigDecimal.valueOf(n.doubleValue());
        }
    }

    /**
     * Converts a number to a BigInteger.
     * <p>
     * Handles BigInteger, integral types, floating-point types, and BigDecimal.
     * Floating-point numbers are first converted to BigDecimal before extraction.
     *
     * @param n the number to convert (must not be null)
     * @return a BigInteger representation of the number
     */
    public static BigInteger toBigInteger(Number n) {
        if (n instanceof BigInteger) {
            return (BigInteger) n;
        }
        if (n instanceof Integer || n instanceof Long || n instanceof Byte || n instanceof Short) {
            return BigInteger.valueOf(n.longValue());
        }

        if (n instanceof Float || n instanceof Double) {
            BigDecimal bd = new BigDecimal(n.toString());
            return bd.toBigInteger();
        }
        if (n instanceof BigDecimal) {
            return ((BigDecimal) n).toBigInteger();
        }

        return new BigInteger(n.toString());
    }

    /**
     * Determines which NumberMath instance to use for binary operations on two operands.
     * <p>
     * Implements the type promotion matrix:
     * <pre>
     *    bD bI  D  F  L  I
     * bD bD bD  D  D bD bD
     * bI bD bI  D  D bI bI
     *  D  D  D  D  D  D  D
     *  F  D  D  D  D  D  D
     *  L bD bI  D  D  L  L
     *  I bD bI  D  D  L  I
     * </pre>
     * <p>
     * Where: bD=BigDecimal, bI=BigInteger, D=Double, F=Float, L=Long, I=Integer
     * <p>
     * Note: Byte, Character, and Short operands are pre-promoted to Integer.
     * For division, if either operand is floating-point, the result is floating-point.
     * Otherwise, the result is BigDecimal.
     *
     * @param left the left operand
     * @param right the right operand
     * @return the appropriate NumberMath instance for these operands
     */
    public static NumberMath getMath(Number left, Number right) {
        // FloatingPointMath wins according to promotion Matrix
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return FloatingPointMath.INSTANCE;
        }
        NumberMath leftMath = getMath(left);
        NumberMath rightMath = getMath(right);

        if (leftMath == BigDecimalMath.INSTANCE || rightMath == BigDecimalMath.INSTANCE) {
            return BigDecimalMath.INSTANCE;
        }
        if (leftMath == BigIntegerMath.INSTANCE || rightMath == BigIntegerMath.INSTANCE) {
            return BigIntegerMath.INSTANCE;
        }
        if (leftMath == LongMath.INSTANCE || rightMath == LongMath.INSTANCE) {
            return LongMath.INSTANCE;
        }
        if (leftMath == IntegerMath.INSTANCE || rightMath == IntegerMath.INSTANCE) {
            return IntegerMath.INSTANCE;
        }
        // also for custom Number implementations
        return BigDecimalMath.INSTANCE;
    }

    /**
     * Determines which NumberMath instance to use for a single number.
     * <p>
     * Matches the number's type to the corresponding math instance.
     * Custom Number implementations default to BigDecimalMath.
     *
     * @param number the number (must not be null)
     * @return the appropriate NumberMath instance for this type
     */
    /* package private */ static NumberMath getMath(Number number) {
        if (isLong(number)) {
            return LongMath.INSTANCE;
        }
        if (isFloatingPoint(number)) {
            return FloatingPointMath.INSTANCE;
        }
        if (isBigDecimal(number)) {
            return BigDecimalMath.INSTANCE;
        }
        if (isBigInteger(number)) {
            return BigIntegerMath.INSTANCE;
        }
        if (isInteger(number) || isShort(number) || isByte(number)) {
            return IntegerMath.INSTANCE;
        }
        // also for custom Number implementations
        return BigDecimalMath.INSTANCE;
    }

    /**
     * Computes the absolute value of a number.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param number the operand
     * @return the absolute value
     */
    protected abstract Number absImpl(Number number);

    /**
     * Adds two numbers.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the left operand
     * @param right the right operand
     * @return the sum
     */
    public abstract Number addImpl(Number left, Number right);

    /**
     * Subtracts two numbers.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the minuend
     * @param right the subtrahend
     * @return the difference
     */
    public abstract Number subtractImpl(Number left, Number right);

    /**
     * Multiplies two numbers.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the first multiplicand
     * @param right the second multiplicand
     * @return the product
     */
    public abstract Number multiplyImpl(Number left, Number right);

    /**
     * Divides two numbers.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the quotient
     */
    public abstract Number divideImpl(Number left, Number right);

    /**
     * Compares two numbers.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the first number
     * @param right the second number
     * @return negative, zero, or positive as left is less than, equal to, or greater than right
     */
    public abstract int compareToImpl(Number left, Number right);

    /**
     * Negates a number.
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the operand
     * @return the negation
     */
    protected abstract Number unaryMinusImpl(Number left);

    /**
     * Returns the number as-is (unary plus/identity).
     * <p>
     * Subclasses must implement this method according to type promotion hierarchy rules.
     *
     * @param left the operand
     * @return the same value
     */
    protected abstract Number unaryPlusImpl(Number left);

    /**
     * Bitwise negation of a number.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bitwise operations must override.
     *
     * @param left the operand
     * @return the bitwise negation
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number bitwiseNegateImpl(Number left) {
        throw createUnsupportedException("bitwiseNegate()", left);
    }

    /**
     * Bitwise OR of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bitwise operations must override.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise OR result
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number orImpl(Number left, Number right) {
        throw createUnsupportedException("or()", left);
    }

    /**
     * Bitwise AND of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bitwise operations must override.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise AND result
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number andImpl(Number left, Number right) {
        throw createUnsupportedException("and()", left);
    }

    /**
     * Bitwise XOR of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bitwise operations must override.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the bitwise XOR result
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number xorImpl(Number left, Number right) {
        throw createUnsupportedException("xor()", left);
    }

    /**
     * Remainder of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting remainder must override.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the remainder
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number remainderImpl(Number left, Number right) {
        throw createUnsupportedException("remainder()", left);
    }

    /**
     * Modulo of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting modulo must override.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the modulo result
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number modImpl(Number left, Number right) {
        throw createUnsupportedException("mod()", left);
    }

    /**
     * Integer division of two numbers.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting integer division must override.
     *
     * @param left the dividend
     * @param right the divisor
     * @return the integer quotient
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number intdivImpl(Number left, Number right) {
        throw createUnsupportedException("intdiv()", left);
    }

    /**
     * Left shift of a number.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bit shifting must override.
     *
     * @param left the value to shift
     * @param right the shift distance
     * @return the left-shifted value
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number leftShiftImpl(Number left, Number right) {
        throw createUnsupportedException("leftShift()", left);
    }

    /**
     * Right shift of a number.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bit shifting must override.
     *
     * @param left the value to shift
     * @param right the shift distance
     * @return the right-shifted value
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number rightShiftImpl(Number left, Number right) {
        throw createUnsupportedException("rightShift()", left);
    }

    /**
     * Unsigned right shift of a number.
     * <p>
     * Default implementation throws UnsupportedOperationException.
     * Subclasses supporting bit shifting must override.
     *
     * @param left the value to shift
     * @param right the shift distance
     * @return the unsigned right-shifted value
     * @throws UnsupportedOperationException if not supported for this type
     */
    protected Number rightShiftUnsignedImpl(Number left, Number right) {
        throw createUnsupportedException("rightShiftUnsigned()", left);
    }

    /**
     * Creates an UnsupportedOperationException for an unsupported operation.
     *
     * @param operation the name of the unsupported operation
     * @param left the operand
     * @return a new UnsupportedOperationException
     */
    protected UnsupportedOperationException createUnsupportedException(String operation, Number left) {
        return new UnsupportedOperationException("Cannot use " + operation + " on this number type: " + left.getClass().getName() + " with value: " + left);
    }
}
