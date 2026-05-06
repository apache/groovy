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

import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Tracks modifications to arithmetic operations on numeric types.
 * <p>
 * This class monitors which arithmetic operations have been modified for each
 * numeric type (Byte, Short, Integer, Long, Float, Double). It maintains a set
 * of standard operation names and provides a singleton instance for global tracking.
 * <p>
 * Used internally by the Groovy compiler/runtime to detect when standard numeric
 * operations have been overridden or modified, which may require special handling
 * in code generation.
 */
public class NumberMathModificationInfo {

    /**
     * Singleton instance of NumberMathModificationInfo.
     */
    public static final NumberMathModificationInfo instance = new NumberMathModificationInfo();

    private static final HashSet<String> NAMES = new HashSet<String>();

    static {
        Collections.addAll(NAMES, "plus", "minus", "multiply", "div", "compareTo", "or", "and", "xor", "intdiv", "mod", "remainder", "leftShift", "rightShift", "rightShiftUnsigned");
    }

    private NumberMathModificationInfo() { }

    /**
     * Checks whether a method represents a modification to a standard numeric operation.
     * <p>
     * Examines the method's declaring class, name, and parameter types to determine
     * if it overrides a standard numeric operation.
     *
     * @param method the method to check
     */
    public void checkIfStdMethod(MetaMethod method) {
        if (method.getClass() != NewInstanceMetaMethod.class) {
            String name = method.getName();

            if (method.getParameterTypes().length != 1)
                return;

            if (!method.getParameterTypes()[0].isNumber && method.getParameterTypes()[0].getTheClass() != Object.class)
                return;

            if (!NAMES.contains(name))
                return;

            checkNumberOps(name, method.getDeclaringClass().getTheClass());
        }
    }

    private void checkNumberOps(String name, Class klazz) {
        if ("plus".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_plus = true, e -> short_plus = true, e -> int_plus = true, e -> long_plus = true, e -> float_plus = true, e -> double_plus = true);
        } else if ("minus".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_minus = true, e -> short_minus = true, e -> int_minus = true, e -> long_minus = true, e -> float_minus = true, e -> double_minus = true);
        } else if ("multiply".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_multiply = true, e -> short_multiply = true, e -> int_multiply = true, e -> long_multiply = true, e -> float_multiply = true, e -> double_multiply = true);
        } else if ("div".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_div = true, e -> short_div = true, e -> int_div = true, e -> long_div = true, e -> float_div = true, e -> double_div = true);
        } else if ("or".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_or = true, e -> short_or = true, e -> int_or = true, e -> long_or = true, e -> float_or = true, e -> double_or = true);
        } else if ("and".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_and = true, e -> short_and = true, e -> int_and = true, e -> long_and = true, e -> float_and = true, e -> double_and = true);
        } else if ("xor".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_xor = true, e -> short_xor = true, e -> int_xor = true, e -> long_xor = true, e -> float_xor = true, e -> double_xor = true);
        } else if ("intdiv".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_intdiv = true, e -> short_intdiv = true, e -> int_intdiv = true, e -> long_intdiv = true, e -> float_intdiv = true, e -> double_intdiv = true);
        } else if ("remainder".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_remainder = true, e -> short_remainder = true, e -> int_remainder = true, e -> long_remainder = true, e -> float_remainder = true, e -> double_remainder = true);
        } else if ("mod".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_mod = true, e -> short_mod = true, e -> int_mod = true, e -> long_mod = true, e -> float_mod = true, e -> double_mod = true);
        } else if ("leftShift".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_leftShift = true, e -> short_leftShift = true, e -> int_leftShift = true, e -> long_leftShift = true, e -> float_leftShift = true, e -> double_leftShift = true);
        } else if ("rightShift".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_rightShift = true, e -> short_rightShift = true, e -> int_rightShift = true, e -> long_rightShift = true, e -> float_rightShift = true, e -> double_rightShift = true);
        } else if ("rightShiftUnsigned".equals(name)) {
            doCheckNumberOps(klazz, e -> byte_rightShiftUnsigned = true, e -> short_rightShiftUnsigned = true, e -> int_rightShiftUnsigned = true, e -> long_rightShiftUnsigned = true, e -> float_rightShiftUnsigned = true, e -> double_rightShiftUnsigned = true);
        }
    }

    private void doCheckNumberOps(Class klazz, Consumer<Class> byteConsumer, Consumer<Class> shortConsumer, Consumer<Class> intConsumer, Consumer<Class> longConsumer, Consumer<Class> floatConsumer, Consumer<Class> doubleConsumer) {
        if (klazz == Byte.class) {
            byteConsumer.accept(klazz);
        } else if (klazz == Short.class) {
            shortConsumer.accept(klazz);
        } else if (klazz == Integer.class) {
            intConsumer.accept(klazz);
        } else if (klazz == Long.class) {
            longConsumer.accept(klazz);
        } else if (klazz == Float.class) {
            floatConsumer.accept(klazz);
        } else if (klazz == Double.class) {
            doubleConsumer.accept(klazz);
        } else if (klazz == Object.class) {
            byteConsumer.accept(klazz);
            shortConsumer.accept(klazz);
            intConsumer.accept(klazz);
            longConsumer.accept(klazz);
            floatConsumer.accept(klazz);
            doubleConsumer.accept(klazz);
        }
    }


    /**
     * Flags indicating whether the plus (+) operation has been modified for each numeric type.
     */
    public boolean byte_plus;
    /**
     * Flags indicating whether the plus (+) operation has been modified for Short type.
     */
    public boolean short_plus;
    /**
     * Flags indicating whether the plus (+) operation has been modified for Integer type.
     */
    public boolean int_plus;
    /**
     * Flags indicating whether the plus (+) operation has been modified for Long type.
     */
    public boolean long_plus;
    /**
     * Flags indicating whether the plus (+) operation has been modified for Float type.
     */
    public boolean float_plus;
    /**
     * Flags indicating whether the plus (+) operation has been modified for Double type.
     */
    public boolean double_plus;

    /**
     * Flags indicating whether the minus (-) operation has been modified for Byte type.
     */
    public boolean byte_minus;
    /**
     * Flags indicating whether the minus (-) operation has been modified for Short type.
     */
    public boolean short_minus;
    /**
     * Flags indicating whether the minus (-) operation has been modified for Integer type.
     */
    public boolean int_minus;
    /**
     * Flags indicating whether the minus (-) operation has been modified for Long type.
     */
    public boolean long_minus;
    /**
     * Flags indicating whether the minus (-) operation has been modified for Float type.
     */
    public boolean float_minus;
    /**
     * Flags indicating whether the minus (-) operation has been modified for Double type.
     */
    public boolean double_minus;

    /**
     * Flags indicating whether the multiply (*) operation has been modified for Byte type.
     */
    public boolean byte_multiply;
    /**
     * Flags indicating whether the multiply (*) operation has been modified for Short type.
     */
    public boolean short_multiply;
    /**
     * Flags indicating whether the multiply (*) operation has been modified for Integer type.
     */
    public boolean int_multiply;
    /**
     * Flags indicating whether the multiply (*) operation has been modified for Long type.
     */
    public boolean long_multiply;
    /**
     * Flags indicating whether the multiply (*) operation has been modified for Float type.
     */
    public boolean float_multiply;
    /**
     * Flags indicating whether the multiply (*) operation has been modified for Double type.
     */
    public boolean double_multiply;

    /**
     * Flags indicating whether the divide (/) operation has been modified for Byte type.
     */
    public boolean byte_div;
    /**
     * Flags indicating whether the divide (/) operation has been modified for Short type.
     */
    public boolean short_div;
    /**
     * Flags indicating whether the divide (/) operation has been modified for Integer type.
     */
    public boolean int_div;
    /**
     * Flags indicating whether the divide (/) operation has been modified for Long type.
     */
    public boolean long_div;
    /**
     * Flags indicating whether the divide (/) operation has been modified for Float type.
     */
    public boolean float_div;
    /**
     * Flags indicating whether the divide (/) operation has been modified for Double type.
     */
    public boolean double_div;

    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Byte type.
     */
    public boolean byte_or;
    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Short type.
     */
    public boolean short_or;
    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Integer type.
     */
    public boolean int_or;
    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Long type.
     */
    public boolean long_or;
    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Float type.
     */
    public boolean float_or;
    /**
     * Flags indicating whether the bitwise OR (|) operation has been modified for Double type.
     */
    public boolean double_or;

    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Byte type.
     */
    public boolean byte_and;
    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Short type.
     */
    public boolean short_and;
    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Integer type.
     */
    public boolean int_and;
    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Long type.
     */
    public boolean long_and;
    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Float type.
     */
    public boolean float_and;
    /**
     * Flags indicating whether the bitwise AND (&amp;) operation has been modified for Double type.
     */
    public boolean double_and;

    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Byte type.
     */
    public boolean byte_xor;
    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Short type.
     */
    public boolean short_xor;
    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Integer type.
     */
    public boolean int_xor;
    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Long type.
     */
    public boolean long_xor;
    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Float type.
     */
    public boolean float_xor;
    /**
     * Flags indicating whether the bitwise XOR (^) operation has been modified for Double type.
     */
    public boolean double_xor;

    /**
     * Flags indicating whether the integer division (//) operation has been modified for Byte type.
     */
    public boolean byte_intdiv;
    /**
     * Flags indicating whether the integer division (//) operation has been modified for Short type.
     */
    public boolean short_intdiv;
    /**
     * Flags indicating whether the integer division (//) operation has been modified for Integer type.
     */
    public boolean int_intdiv;
    /**
     * Flags indicating whether the integer division (//) operation has been modified for Long type.
     */
    public boolean long_intdiv;
    /**
     * Flags indicating whether the integer division (//) operation has been modified for Float type.
     */
    public boolean float_intdiv;
    /**
     * Flags indicating whether the integer division (//) operation has been modified for Double type.
     */
    public boolean double_intdiv;

    /**
     * Flags indicating whether the modulo (%) operation has been modified for Byte type.
     */
    public boolean byte_mod;
    /**
     * Flags indicating whether the modulo (%) operation has been modified for Short type.
     */
    public boolean short_mod;
    /**
     * Flags indicating whether the modulo (%) operation has been modified for Integer type.
     */
    public boolean int_mod;
    /**
     * Flags indicating whether the modulo (%) operation has been modified for Long type.
     */
    public boolean long_mod;
    /**
     * Flags indicating whether the modulo (%) operation has been modified for Float type.
     */
    public boolean float_mod;
    /**
     * Flags indicating whether the modulo (%) operation has been modified for Double type.
     */
    public boolean double_mod;

    /**
     * Flags indicating whether the remainder operation has been modified for Byte type.
     */
    public boolean byte_remainder;
    /**
     * Flags indicating whether the remainder operation has been modified for Short type.
     */
    public boolean short_remainder;
    /**
     * Flags indicating whether the remainder operation has been modified for Integer type.
     */
    public boolean int_remainder;
    /**
     * Flags indicating whether the remainder operation has been modified for Long type.
     */
    public boolean long_remainder;
    /**
     * Flags indicating whether the remainder operation has been modified for Float type.
     */
    public boolean float_remainder;
    /**
     * Flags indicating whether the remainder operation has been modified for Double type.
     */
    public boolean double_remainder;

    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Byte type.
     */
    public boolean byte_leftShift;
    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Short type.
     */
    public boolean short_leftShift;
    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Integer type.
     */
    public boolean int_leftShift;
    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Long type.
     */
    public boolean long_leftShift;
    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Float type.
     */
    public boolean float_leftShift;
    /**
     * Flags indicating whether the left shift (&lt;&lt;) operation has been modified for Double type.
     */
    public boolean double_leftShift;

    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Byte type.
     */
    public boolean byte_rightShift;
    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Short type.
     */
    public boolean short_rightShift;
    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Integer type.
     */
    public boolean int_rightShift;
    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Long type.
     */
    public boolean long_rightShift;
    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Float type.
     */
    public boolean float_rightShift;
    /**
     * Flags indicating whether the right shift (>>) operation has been modified for Double type.
     */
    public boolean double_rightShift;

    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Byte type.
     */
    public boolean byte_rightShiftUnsigned;
    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Short type.
     */
    public boolean short_rightShiftUnsigned;
    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Integer type.
     */
    public boolean int_rightShiftUnsigned;
    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Long type.
     */
    public boolean long_rightShiftUnsigned;
    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Float type.
     */
    public boolean float_rightShiftUnsigned;
    /**
     * Flags indicating whether the unsigned right shift (>>>) operation has been modified for Double type.
     */
    public boolean double_rightShiftUnsigned;

    /**
     * Performs addition on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static int plus(byte op1, byte op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static int plusSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static int plus(byte op1, short op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static int plusSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static int plus(byte op1, int op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static int plusSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static long plus(byte op1, long op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static long plusSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (byte and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(byte op1, float op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(byte op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (byte and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(byte op1, double op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + op2;
        }
    }

    private static double plusSlow(byte op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static int plus(short op1, byte op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + ((int) op2);
        }
    }

    private static int plusSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static int plus(short op1, short op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + ((int) op2);
        }
    }

    private static int plusSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static int plus(short op1, int op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + op2;
        }
    }

    private static int plusSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static long plus(short op1, long op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((long) op1) + op2;
        }
    }

    private static long plusSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (short and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(short op1, float op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(short op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (short and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(short op1, double op2) {
        if (instance.short_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + op2;
        }
    }

    private static double plusSlow(short op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static int plus(int op1, byte op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((int) op2);
        }
    }

    private static int plusSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static int plus(int op1, short op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((int) op2);
        }
    }

    private static int plusSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static int plus(int op1, int op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static int plusSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    /**
     * Performs addition on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static long plus(int op1, long op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((long) op1) + op2;
        }
    }

    private static long plusSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (int and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(int op1, float op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(int op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (int and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(int op1, double op2) {
        if (instance.int_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + op2;
        }
    }

    private static double plusSlow(int op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static long plus(long op1, byte op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((long) op2);
        }
    }

    private static long plusSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static long plus(long op1, short op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((long) op2);
        }
    }

    private static long plusSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static long plus(long op1, int op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((long) op2);
        }
    }

    private static long plusSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static long plus(long op1, long op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static long plusSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

    /**
     * Performs addition on two numeric values (long and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(long op1, float op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(long op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (long and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(long op1, double op2) {
        if (instance.long_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + op2;
        }
    }

    private static double plusSlow(long op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static double plus(float op1, byte op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(float op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static double plus(float op1, short op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(float op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static double plus(float op1, int op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(float op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static double plus(float op1, long op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(float op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(float op1, float op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + ((double) op2);
        }
    }

    private static double plusSlow(float op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (float and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(float op1, double op2) {
        if (instance.float_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((double) op1) + op2;
        }
    }

    private static double plusSlow(float op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second byte operand
     * @return the result of addition
     */
    public static double plus(double op1, byte op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((double) op2);
        }
    }

    private static double plusSlow(double op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second short operand
     * @return the result of addition
     */
    public static double plus(double op1, short op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((double) op2);
        }
    }

    private static double plusSlow(double op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second int operand
     * @return the result of addition
     */
    public static double plus(double op1, int op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((double) op2);
        }
    }

    private static double plusSlow(double op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second long operand
     * @return the result of addition
     */
    public static double plus(double op1, long op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((double) op2);
        }
    }

    private static double plusSlow(double op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second float operand
     * @return the result of addition
     */
    public static double plus(double op1, float op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + ((double) op2);
        }
    }

    private static double plusSlow(double op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs addition on two numeric values (double and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second double operand
     * @return the result of addition
     */
    public static double plus(double op1, double op2) {
        if (instance.double_plus) {
            return plusSlow(op1, op2);
        } else {
            return op1 + op2;
        }
    }

    private static double plusSlow(double op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static int minus(byte op1, byte op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - ((int) op2);
        }
    }

    private static int minusSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static int minus(byte op1, short op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - ((int) op2);
        }
    }

    private static int minusSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static int minus(byte op1, int op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - op2;
        }
    }

    private static int minusSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static long minus(byte op1, long op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((long) op1) - op2;
        }
    }

    private static long minusSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(byte op1, float op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(byte op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (byte and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(byte op1, double op2) {
        if (instance.byte_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - op2;
        }
    }

    private static double minusSlow(byte op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static int minus(short op1, byte op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - ((int) op2);
        }
    }

    private static int minusSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static int minus(short op1, short op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - ((int) op2);
        }
    }

    private static int minusSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static int minus(short op1, int op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((int) op1) - op2;
        }
    }

    private static int minusSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static long minus(short op1, long op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((long) op1) - op2;
        }
    }

    private static long minusSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (short and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(short op1, float op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(short op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (short and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(short op1, double op2) {
        if (instance.short_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - op2;
        }
    }

    private static double minusSlow(short op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static int minus(int op1, byte op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((int) op2);
        }
    }

    private static int minusSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static int minus(int op1, short op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((int) op2);
        }
    }

    private static int minusSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static int minus(int op1, int op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - op2;
        }
    }

    private static int minusSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).intValue();
    }

    /**
     * Performs subtraction on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static long minus(int op1, long op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((long) op1) - op2;
        }
    }

    private static long minusSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (int and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(int op1, float op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(int op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (int and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(int op1, double op2) {
        if (instance.int_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - op2;
        }
    }

    private static double minusSlow(int op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static long minus(long op1, byte op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((long) op2);
        }
    }

    private static long minusSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static long minus(long op1, short op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((long) op2);
        }
    }

    private static long minusSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static long minus(long op1, int op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((long) op2);
        }
    }

    private static long minusSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static long minus(long op1, long op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - op2;
        }
    }

    private static long minusSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).longValue();
    }

    /**
     * Performs subtraction on two numeric values (long and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(long op1, float op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(long op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (long and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(long op1, double op2) {
        if (instance.long_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - op2;
        }
    }

    private static double minusSlow(long op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static double minus(float op1, byte op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(float op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static double minus(float op1, short op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(float op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static double minus(float op1, int op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(float op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static double minus(float op1, long op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(float op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(float op1, float op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - ((double) op2);
        }
    }

    private static double minusSlow(float op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (float and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(float op1, double op2) {
        if (instance.float_minus) {
            return minusSlow(op1, op2);
        } else {
            return ((double) op1) - op2;
        }
    }

    private static double minusSlow(float op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second byte operand
     * @return the result of subtraction
     */
    public static double minus(double op1, byte op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((double) op2);
        }
    }

    private static double minusSlow(double op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second short operand
     * @return the result of subtraction
     */
    public static double minus(double op1, short op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((double) op2);
        }
    }

    private static double minusSlow(double op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second int operand
     * @return the result of subtraction
     */
    public static double minus(double op1, int op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((double) op2);
        }
    }

    private static double minusSlow(double op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second long operand
     * @return the result of subtraction
     */
    public static double minus(double op1, long op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((double) op2);
        }
    }

    private static double minusSlow(double op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second float operand
     * @return the result of subtraction
     */
    public static double minus(double op1, float op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - ((double) op2);
        }
    }

    private static double minusSlow(double op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs subtraction on two numeric values (double and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second double operand
     * @return the result of subtraction
     */
    public static double minus(double op1, double op2) {
        if (instance.double_minus) {
            return minusSlow(op1, op2);
        } else {
            return op1 - op2;
        }
    }

    private static double minusSlow(double op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "minus", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static int multiply(byte op1, byte op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * ((int) op2);
        }
    }

    private static int multiplySlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static int multiply(byte op1, short op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * ((int) op2);
        }
    }

    private static int multiplySlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static int multiply(byte op1, int op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * op2;
        }
    }

    private static int multiplySlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static long multiply(byte op1, long op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((long) op1) * op2;
        }
    }

    private static long multiplySlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(byte op1, float op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(byte op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (byte and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(byte op1, double op2) {
        if (instance.byte_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * op2;
        }
    }

    private static double multiplySlow(byte op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static int multiply(short op1, byte op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * ((int) op2);
        }
    }

    private static int multiplySlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static int multiply(short op1, short op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * ((int) op2);
        }
    }

    private static int multiplySlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static int multiply(short op1, int op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((int) op1) * op2;
        }
    }

    private static int multiplySlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static long multiply(short op1, long op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((long) op1) * op2;
        }
    }

    private static long multiplySlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (short and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(short op1, float op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(short op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (short and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(short op1, double op2) {
        if (instance.short_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * op2;
        }
    }

    private static double multiplySlow(short op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static int multiply(int op1, byte op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((int) op2);
        }
    }

    private static int multiplySlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static int multiply(int op1, short op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((int) op2);
        }
    }

    private static int multiplySlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static int multiply(int op1, int op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * op2;
        }
    }

    private static int multiplySlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).intValue();
    }

    /**
     * Performs multiplication on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static long multiply(int op1, long op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((long) op1) * op2;
        }
    }

    private static long multiplySlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (int and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(int op1, float op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(int op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (int and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(int op1, double op2) {
        if (instance.int_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * op2;
        }
    }

    private static double multiplySlow(int op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static long multiply(long op1, byte op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((long) op2);
        }
    }

    private static long multiplySlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static long multiply(long op1, short op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((long) op2);
        }
    }

    private static long multiplySlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static long multiply(long op1, int op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((long) op2);
        }
    }

    private static long multiplySlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static long multiply(long op1, long op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * op2;
        }
    }

    private static long multiplySlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).longValue();
    }

    /**
     * Performs multiplication on two numeric values (long and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(long op1, float op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(long op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (long and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(long op1, double op2) {
        if (instance.long_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * op2;
        }
    }

    private static double multiplySlow(long op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, byte op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(float op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, short op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(float op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, int op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(float op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, long op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(float op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, float op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * ((double) op2);
        }
    }

    private static double multiplySlow(float op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (float and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(float op1, double op2) {
        if (instance.float_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return ((double) op1) * op2;
        }
    }

    private static double multiplySlow(float op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second byte operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, byte op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((double) op2);
        }
    }

    private static double multiplySlow(double op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second short operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, short op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((double) op2);
        }
    }

    private static double multiplySlow(double op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second int operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, int op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((double) op2);
        }
    }

    private static double multiplySlow(double op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second long operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, long op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((double) op2);
        }
    }

    private static double multiplySlow(double op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second float operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, float op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * ((double) op2);
        }
    }

    private static double multiplySlow(double op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs multiplication on two numeric values (double and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second double operand
     * @return the result of multiplication
     */
    public static double multiply(double op1, double op2) {
        if (instance.double_multiply) {
            return multiplySlow(op1, op2);
        } else {
            return op1 * op2;
        }
    }

    private static double multiplySlow(double op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "multiply", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static int div(byte op1, byte op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int divSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static int div(byte op1, short op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int divSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static int div(byte op1, int op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / op2;
        }
    }

    private static int divSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static long div(byte op1, long op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long divSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (byte and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(byte op1, float op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(byte op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (byte and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(byte op1, double op2) {
        if (instance.byte_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / op2;
        }
    }

    private static double divSlow(byte op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static int div(short op1, byte op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int divSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static int div(short op1, short op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int divSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static int div(short op1, int op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((int) op1) / op2;
        }
    }

    private static int divSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static long div(short op1, long op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long divSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (short and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(short op1, float op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(short op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (short and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(short op1, double op2) {
        if (instance.short_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / op2;
        }
    }

    private static double divSlow(short op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static int div(int op1, byte op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((int) op2);
        }
    }

    private static int divSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static int div(int op1, short op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((int) op2);
        }
    }

    private static int divSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static int div(int op1, int op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / op2;
        }
    }

    private static int divSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).intValue();
    }

    /**
     * Performs division on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static long div(int op1, long op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long divSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (int and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(int op1, float op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(int op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (int and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(int op1, double op2) {
        if (instance.int_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / op2;
        }
    }

    private static double divSlow(int op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static long div(long op1, byte op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long divSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static long div(long op1, short op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long divSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static long div(long op1, int op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long divSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static long div(long op1, long op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / op2;
        }
    }

    private static long divSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).longValue();
    }

    /**
     * Performs division on two numeric values (long and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(long op1, float op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(long op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (long and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(long op1, double op2) {
        if (instance.long_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / op2;
        }
    }

    private static double divSlow(long op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static double div(float op1, byte op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(float op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static double div(float op1, short op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(float op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static double div(float op1, int op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(float op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static double div(float op1, long op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(float op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(float op1, float op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / ((double) op2);
        }
    }

    private static double divSlow(float op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (float and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first float operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(float op1, double op2) {
        if (instance.float_div) {
            return divSlow(op1, op2);
        } else {
            return ((double) op1) / op2;
        }
    }

    private static double divSlow(float op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second byte operand
     * @return the result of division
     */
    public static double div(double op1, byte op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((double) op2);
        }
    }

    private static double divSlow(double op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second short operand
     * @return the result of division
     */
    public static double div(double op1, short op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((double) op2);
        }
    }

    private static double divSlow(double op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second int operand
     * @return the result of division
     */
    public static double div(double op1, int op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((double) op2);
        }
    }

    private static double divSlow(double op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second long operand
     * @return the result of division
     */
    public static double div(double op1, long op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((double) op2);
        }
    }

    private static double divSlow(double op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and float).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second float operand
     * @return the result of division
     */
    public static double div(double op1, float op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / ((double) op2);
        }
    }

    private static double divSlow(double op1, float op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs division on two numeric values (double and double).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first double operand
     * @param op2 the second double operand
     * @return the result of division
     */
    public static double div(double op1, double op2) {
        if (instance.double_div) {
            return divSlow(op1, op2);
        } else {
            return op1 / op2;
        }
    }

    private static double divSlow(double op1, double op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "div", op2)).doubleValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise OR
     */
    public static int or(byte op1, byte op2) {
        if (instance.byte_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | ((int) op2);
        }
    }

    private static int orSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise OR
     */
    public static int or(byte op1, short op2) {
        if (instance.byte_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | ((int) op2);
        }
    }

    private static int orSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise OR
     */
    public static int or(byte op1, int op2) {
        if (instance.byte_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | op2;
        }
    }

    private static int orSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise OR
     */
    public static long or(byte op1, long op2) {
        if (instance.byte_or) {
            return orSlow(op1, op2);
        } else {
            return ((long) op1) | op2;
        }
    }

    private static long orSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise OR
     */
    public static int or(short op1, byte op2) {
        if (instance.short_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | ((int) op2);
        }
    }

    private static int orSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise OR
     */
    public static int or(short op1, short op2) {
        if (instance.short_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | ((int) op2);
        }
    }

    private static int orSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise OR
     */
    public static int or(short op1, int op2) {
        if (instance.short_or) {
            return orSlow(op1, op2);
        } else {
            return ((int) op1) | op2;
        }
    }

    private static int orSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise OR
     */
    public static long or(short op1, long op2) {
        if (instance.short_or) {
            return orSlow(op1, op2);
        } else {
            return ((long) op1) | op2;
        }
    }

    private static long orSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise OR
     */
    public static int or(int op1, byte op2) {
        if (instance.int_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | ((int) op2);
        }
    }

    private static int orSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise OR
     */
    public static int or(int op1, short op2) {
        if (instance.int_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | ((int) op2);
        }
    }

    private static int orSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise OR
     */
    public static int or(int op1, int op2) {
        if (instance.int_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | op2;
        }
    }

    private static int orSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).intValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise OR
     */
    public static long or(int op1, long op2) {
        if (instance.int_or) {
            return orSlow(op1, op2);
        } else {
            return ((long) op1) | op2;
        }
    }

    private static long orSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise OR
     */
    public static long or(long op1, byte op2) {
        if (instance.long_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | ((long) op2);
        }
    }

    private static long orSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise OR
     */
    public static long or(long op1, short op2) {
        if (instance.long_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | ((long) op2);
        }
    }

    private static long orSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise OR
     */
    public static long or(long op1, int op2) {
        if (instance.long_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | ((long) op2);
        }
    }

    private static long orSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise OR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise OR
     */
    public static long or(long op1, long op2) {
        if (instance.long_or) {
            return orSlow(op1, op2);
        } else {
            return op1 | op2;
        }
    }

    private static long orSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "or", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise AND
     */
    public static int and(byte op1, byte op2) {
        if (instance.byte_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & ((int) op2);
        }
    }

    private static int andSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise AND
     */
    public static int and(byte op1, short op2) {
        if (instance.byte_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & ((int) op2);
        }
    }

    private static int andSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise AND
     */
    public static int and(byte op1, int op2) {
        if (instance.byte_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & op2;
        }
    }

    private static int andSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise AND
     */
    public static long and(byte op1, long op2) {
        if (instance.byte_and) {
            return andSlow(op1, op2);
        } else {
            return ((long) op1) & op2;
        }
    }

    private static long andSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise AND
     */
    public static int and(short op1, byte op2) {
        if (instance.short_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & ((int) op2);
        }
    }

    private static int andSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise AND
     */
    public static int and(short op1, short op2) {
        if (instance.short_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & ((int) op2);
        }
    }

    private static int andSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise AND
     */
    public static int and(short op1, int op2) {
        if (instance.short_and) {
            return andSlow(op1, op2);
        } else {
            return ((int) op1) & op2;
        }
    }

    private static int andSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise AND
     */
    public static long and(short op1, long op2) {
        if (instance.short_and) {
            return andSlow(op1, op2);
        } else {
            return ((long) op1) & op2;
        }
    }

    private static long andSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise AND
     */
    public static int and(int op1, byte op2) {
        if (instance.int_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & ((int) op2);
        }
    }

    private static int andSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise AND
     */
    public static int and(int op1, short op2) {
        if (instance.int_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & ((int) op2);
        }
    }

    private static int andSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise AND
     */
    public static int and(int op1, int op2) {
        if (instance.int_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & op2;
        }
    }

    private static int andSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).intValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise AND
     */
    public static long and(int op1, long op2) {
        if (instance.int_and) {
            return andSlow(op1, op2);
        } else {
            return ((long) op1) & op2;
        }
    }

    private static long andSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise AND
     */
    public static long and(long op1, byte op2) {
        if (instance.long_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & ((long) op2);
        }
    }

    private static long andSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise AND
     */
    public static long and(long op1, short op2) {
        if (instance.long_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & ((long) op2);
        }
    }

    private static long andSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise AND
     */
    public static long and(long op1, int op2) {
        if (instance.long_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & ((long) op2);
        }
    }

    private static long andSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise AND on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise AND
     */
    public static long and(long op1, long op2) {
        if (instance.long_and) {
            return andSlow(op1, op2);
        } else {
            return op1 & op2;
        }
    }

    private static long andSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "and", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise XOR
     */
    public static int xor(byte op1, byte op2) {
        if (instance.byte_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ ((int) op2);
        }
    }

    private static int xorSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise XOR
     */
    public static int xor(byte op1, short op2) {
        if (instance.byte_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ ((int) op2);
        }
    }

    private static int xorSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise XOR
     */
    public static int xor(byte op1, int op2) {
        if (instance.byte_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ op2;
        }
    }

    private static int xorSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise XOR
     */
    public static long xor(byte op1, long op2) {
        if (instance.byte_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((long) op1) ^ op2;
        }
    }

    private static long xorSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise XOR
     */
    public static int xor(short op1, byte op2) {
        if (instance.short_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ ((int) op2);
        }
    }

    private static int xorSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise XOR
     */
    public static int xor(short op1, short op2) {
        if (instance.short_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ ((int) op2);
        }
    }

    private static int xorSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise XOR
     */
    public static int xor(short op1, int op2) {
        if (instance.short_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((int) op1) ^ op2;
        }
    }

    private static int xorSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise XOR
     */
    public static long xor(short op1, long op2) {
        if (instance.short_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((long) op1) ^ op2;
        }
    }

    private static long xorSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise XOR
     */
    public static int xor(int op1, byte op2) {
        if (instance.int_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ ((int) op2);
        }
    }

    private static int xorSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise XOR
     */
    public static int xor(int op1, short op2) {
        if (instance.int_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ ((int) op2);
        }
    }

    private static int xorSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise XOR
     */
    public static int xor(int op1, int op2) {
        if (instance.int_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ op2;
        }
    }

    private static int xorSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).intValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise XOR
     */
    public static long xor(int op1, long op2) {
        if (instance.int_xor) {
            return xorSlow(op1, op2);
        } else {
            return ((long) op1) ^ op2;
        }
    }

    private static long xorSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of bitwise XOR
     */
    public static long xor(long op1, byte op2) {
        if (instance.long_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ ((long) op2);
        }
    }

    private static long xorSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of bitwise XOR
     */
    public static long xor(long op1, short op2) {
        if (instance.long_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ ((long) op2);
        }
    }

    private static long xorSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of bitwise XOR
     */
    public static long xor(long op1, int op2) {
        if (instance.long_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ ((long) op2);
        }
    }

    private static long xorSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs bitwise XOR on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of bitwise XOR
     */
    public static long xor(long op1, long op2) {
        if (instance.long_xor) {
            return xorSlow(op1, op2);
        } else {
            return op1 ^ op2;
        }
    }

    private static long xorSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "xor", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of integer division
     */
    public static int intdiv(byte op1, byte op2) {
        if (instance.byte_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int intdivSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of integer division
     */
    public static int intdiv(byte op1, short op2) {
        if (instance.byte_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int intdivSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of integer division
     */
    public static int intdiv(byte op1, int op2) {
        if (instance.byte_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / op2;
        }
    }

    private static int intdivSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of integer division
     */
    public static long intdiv(byte op1, long op2) {
        if (instance.byte_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long intdivSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of integer division
     */
    public static int intdiv(short op1, byte op2) {
        if (instance.short_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int intdivSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of integer division
     */
    public static int intdiv(short op1, short op2) {
        if (instance.short_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / ((int) op2);
        }
    }

    private static int intdivSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of integer division
     */
    public static int intdiv(short op1, int op2) {
        if (instance.short_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((int) op1) / op2;
        }
    }

    private static int intdivSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of integer division
     */
    public static long intdiv(short op1, long op2) {
        if (instance.short_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long intdivSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of integer division
     */
    public static int intdiv(int op1, byte op2) {
        if (instance.int_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / ((int) op2);
        }
    }

    private static int intdivSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of integer division
     */
    public static int intdiv(int op1, short op2) {
        if (instance.int_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / ((int) op2);
        }
    }

    private static int intdivSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of integer division
     */
    public static int intdiv(int op1, int op2) {
        if (instance.int_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / op2;
        }
    }

    private static int intdivSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).intValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of integer division
     */
    public static long intdiv(int op1, long op2) {
        if (instance.int_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return ((long) op1) / op2;
        }
    }

    private static long intdivSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of integer division
     */
    public static long intdiv(long op1, byte op2) {
        if (instance.long_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long intdivSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of integer division
     */
    public static long intdiv(long op1, short op2) {
        if (instance.long_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long intdivSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of integer division
     */
    public static long intdiv(long op1, int op2) {
        if (instance.long_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / ((long) op2);
        }
    }

    private static long intdivSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs integer division on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of integer division
     */
    public static long intdiv(long op1, long op2) {
        if (instance.long_intdiv) {
            return intdivSlow(op1, op2);
        } else {
            return op1 / op2;
        }
    }

    private static long intdivSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "intdiv", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of modulo
     */
    public static int mod(byte op1, byte op2) {
        if (instance.byte_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int modSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of modulo
     */
    public static int mod(byte op1, short op2) {
        if (instance.byte_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int modSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of modulo
     */
    public static int mod(byte op1, int op2) {
        if (instance.byte_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % op2;
        }
    }

    private static int modSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of modulo
     */
    public static long mod(byte op1, long op2) {
        if (instance.byte_mod) {
            return modSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long modSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of modulo
     */
    public static int mod(short op1, byte op2) {
        if (instance.short_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int modSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of modulo
     */
    public static int mod(short op1, short op2) {
        if (instance.short_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int modSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of modulo
     */
    public static int mod(short op1, int op2) {
        if (instance.short_mod) {
            return modSlow(op1, op2);
        } else {
            return ((int) op1) % op2;
        }
    }

    private static int modSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of modulo
     */
    public static long mod(short op1, long op2) {
        if (instance.short_mod) {
            return modSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long modSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of modulo
     */
    public static int mod(int op1, byte op2) {
        if (instance.int_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % ((int) op2);
        }
    }

    private static int modSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of modulo
     */
    public static int mod(int op1, short op2) {
        if (instance.int_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % ((int) op2);
        }
    }

    private static int modSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of modulo
     */
    public static int mod(int op1, int op2) {
        if (instance.int_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % op2;
        }
    }

    private static int modSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).intValue();
    }

    /**
     * Performs modulo on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of modulo
     */
    public static long mod(int op1, long op2) {
        if (instance.int_mod) {
            return modSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long modSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of modulo
     */
    public static long mod(long op1, byte op2) {
        if (instance.long_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long modSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of modulo
     */
    public static long mod(long op1, short op2) {
        if (instance.long_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long modSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of modulo
     */
    public static long mod(long op1, int op2) {
        if (instance.long_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long modSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs modulo on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of modulo
     */
    public static long mod(long op1, long op2) {
        if (instance.long_mod) {
            return modSlow(op1, op2);
        } else {
            return op1 % op2;
        }
    }

    private static long modSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "mod", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (byte and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second byte operand
     * @return the result of remainder
     */
    public static int remainder(byte op1, byte op2) {
        if (instance.byte_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int remainderSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (byte and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second short operand
     * @return the result of remainder
     */
    public static int remainder(byte op1, short op2) {
        if (instance.byte_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int remainderSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (byte and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second int operand
     * @return the result of remainder
     */
    public static int remainder(byte op1, int op2) {
        if (instance.byte_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % op2;
        }
    }

    private static int remainderSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (byte and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first byte operand
     * @param op2 the second long operand
     * @return the result of remainder
     */
    public static long remainder(byte op1, long op2) {
        if (instance.byte_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long remainderSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (short and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second byte operand
     * @return the result of remainder
     */
    public static int remainder(short op1, byte op2) {
        if (instance.short_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int remainderSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (short and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second short operand
     * @return the result of remainder
     */
    public static int remainder(short op1, short op2) {
        if (instance.short_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % ((int) op2);
        }
    }

    private static int remainderSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (short and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second int operand
     * @return the result of remainder
     */
    public static int remainder(short op1, int op2) {
        if (instance.short_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((int) op1) % op2;
        }
    }

    private static int remainderSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (short and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first short operand
     * @param op2 the second long operand
     * @return the result of remainder
     */
    public static long remainder(short op1, long op2) {
        if (instance.short_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long remainderSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (int and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second byte operand
     * @return the result of remainder
     */
    public static int remainder(int op1, byte op2) {
        if (instance.int_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % ((int) op2);
        }
    }

    private static int remainderSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (int and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second short operand
     * @return the result of remainder
     */
    public static int remainder(int op1, short op2) {
        if (instance.int_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % ((int) op2);
        }
    }

    private static int remainderSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (int and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second int operand
     * @return the result of remainder
     */
    public static int remainder(int op1, int op2) {
        if (instance.int_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % op2;
        }
    }

    private static int remainderSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).intValue();
    }

    /**
     * Performs remainder on two numeric values (int and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first int operand
     * @param op2 the second long operand
     * @return the result of remainder
     */
    public static long remainder(int op1, long op2) {
        if (instance.int_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return ((long) op1) % op2;
        }
    }

    private static long remainderSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (long and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second byte operand
     * @return the result of remainder
     */
    public static long remainder(long op1, byte op2) {
        if (instance.long_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long remainderSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (long and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second short operand
     * @return the result of remainder
     */
    public static long remainder(long op1, short op2) {
        if (instance.long_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long remainderSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (long and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second int operand
     * @return the result of remainder
     */
    public static long remainder(long op1, int op2) {
        if (instance.long_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % ((long) op2);
        }
    }

    private static long remainderSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs remainder on two numeric values (long and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first long operand
     * @param op2 the second long operand
     * @return the result of remainder
     */
    public static long remainder(long op1, long op2) {
        if (instance.long_remainder) {
            return remainderSlow(op1, op2);
        } else {
            return op1 % op2;
        }
    }

    private static long remainderSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "remainder", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of left shift
     */
    public static int leftShift(byte op1, byte op2) {
        if (instance.byte_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << ((int) op2);
        }
    }

    private static int leftShiftSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of left shift
     */
    public static int leftShift(byte op1, short op2) {
        if (instance.byte_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << ((int) op2);
        }
    }

    private static int leftShiftSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of left shift
     */
    public static int leftShift(byte op1, int op2) {
        if (instance.byte_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << op2;
        }
    }

    private static int leftShiftSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of left shift
     */
    public static long leftShift(byte op1, long op2) {
        if (instance.byte_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((long) op1) << op2;
        }
    }

    private static long leftShiftSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of left shift
     */
    public static int leftShift(short op1, byte op2) {
        if (instance.short_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << ((int) op2);
        }
    }

    private static int leftShiftSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of left shift
     */
    public static int leftShift(short op1, short op2) {
        if (instance.short_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << ((int) op2);
        }
    }

    private static int leftShiftSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of left shift
     */
    public static int leftShift(short op1, int op2) {
        if (instance.short_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((int) op1) << op2;
        }
    }

    private static int leftShiftSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of left shift
     */
    public static long leftShift(short op1, long op2) {
        if (instance.short_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((long) op1) << op2;
        }
    }

    private static long leftShiftSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of left shift
     */
    public static int leftShift(int op1, byte op2) {
        if (instance.int_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << ((int) op2);
        }
    }

    private static int leftShiftSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of left shift
     */
    public static int leftShift(int op1, short op2) {
        if (instance.int_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << ((int) op2);
        }
    }

    private static int leftShiftSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of left shift
     */
    public static int leftShift(int op1, int op2) {
        if (instance.int_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << op2;
        }
    }

    private static int leftShiftSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).intValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of left shift
     */
    public static long leftShift(int op1, long op2) {
        if (instance.int_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return ((long) op1) << op2;
        }
    }

    private static long leftShiftSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of left shift
     */
    public static long leftShift(long op1, byte op2) {
        if (instance.long_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << ((long) op2);
        }
    }

    private static long leftShiftSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of left shift
     */
    public static long leftShift(long op1, short op2) {
        if (instance.long_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << ((long) op2);
        }
    }

    private static long leftShiftSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of left shift
     */
    public static long leftShift(long op1, int op2) {
        if (instance.long_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << ((long) op2);
        }
    }

    private static long leftShiftSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs left shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of left shift
     */
    public static long leftShift(long op1, long op2) {
        if (instance.long_leftShift) {
            return leftShiftSlow(op1, op2);
        } else {
            return op1 << op2;
        }
    }

    private static long leftShiftSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "leftShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of right shift
     */
    public static int rightShift(byte op1, byte op2) {
        if (instance.byte_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> ((int) op2);
        }
    }

    private static int rightShiftSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of right shift
     */
    public static int rightShift(byte op1, short op2) {
        if (instance.byte_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> ((int) op2);
        }
    }

    private static int rightShiftSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of right shift
     */
    public static int rightShift(byte op1, int op2) {
        if (instance.byte_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> op2;
        }
    }

    private static int rightShiftSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of right shift
     */
    public static long rightShift(byte op1, long op2) {
        if (instance.byte_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((long) op1) >> op2;
        }
    }

    private static long rightShiftSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of right shift
     */
    public static int rightShift(short op1, byte op2) {
        if (instance.short_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> ((int) op2);
        }
    }

    private static int rightShiftSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of right shift
     */
    public static int rightShift(short op1, short op2) {
        if (instance.short_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> ((int) op2);
        }
    }

    private static int rightShiftSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of right shift
     */
    public static int rightShift(short op1, int op2) {
        if (instance.short_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((int) op1) >> op2;
        }
    }

    private static int rightShiftSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of right shift
     */
    public static long rightShift(short op1, long op2) {
        if (instance.short_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((long) op1) >> op2;
        }
    }

    private static long rightShiftSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of right shift
     */
    public static int rightShift(int op1, byte op2) {
        if (instance.int_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> ((int) op2);
        }
    }

    private static int rightShiftSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of right shift
     */
    public static int rightShift(int op1, short op2) {
        if (instance.int_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> ((int) op2);
        }
    }

    private static int rightShiftSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of right shift
     */
    public static int rightShift(int op1, int op2) {
        if (instance.int_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> op2;
        }
    }

    private static int rightShiftSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).intValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of right shift
     */
    public static long rightShift(int op1, long op2) {
        if (instance.int_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return ((long) op1) >> op2;
        }
    }

    private static long rightShiftSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of right shift
     */
    public static long rightShift(long op1, byte op2) {
        if (instance.long_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> ((long) op2);
        }
    }

    private static long rightShiftSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of right shift
     */
    public static long rightShift(long op1, short op2) {
        if (instance.long_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> ((long) op2);
        }
    }

    private static long rightShiftSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of right shift
     */
    public static long rightShift(long op1, int op2) {
        if (instance.long_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> ((long) op2);
        }
    }

    private static long rightShiftSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of right shift
     */
    public static long rightShift(long op1, long op2) {
        if (instance.long_rightShift) {
            return rightShiftSlow(op1, op2);
        } else {
            return op1 >> op2;
        }
    }

    private static long rightShiftSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShift", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(byte op1, byte op2) {
        if (instance.byte_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(byte op1, short op2) {
        if (instance.byte_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(byte op1, int op2) {
        if (instance.byte_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> op2;
        }
    }

    private static int rightShiftUnsignedSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(byte op1, long op2) {
        if (instance.byte_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((long) op1) >>> op2;
        }
    }

    private static long rightShiftUnsignedSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(short op1, byte op2) {
        if (instance.short_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(short op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(short op1, short op2) {
        if (instance.short_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(short op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(short op1, int op2) {
        if (instance.short_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((int) op1) >>> op2;
        }
    }

    private static int rightShiftUnsignedSlow(short op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(short op1, long op2) {
        if (instance.short_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((long) op1) >>> op2;
        }
    }

    private static long rightShiftUnsignedSlow(short op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(int op1, byte op2) {
        if (instance.int_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(int op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(int op1, short op2) {
        if (instance.int_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> ((int) op2);
        }
    }

    private static int rightShiftUnsignedSlow(int op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of unsigned right shift
     */
    public static int rightShiftUnsigned(int op1, int op2) {
        if (instance.int_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> op2;
        }
    }

    private static int rightShiftUnsignedSlow(int op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).intValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(int op1, long op2) {
        if (instance.int_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return ((long) op1) >>> op2;
        }
    }

    private static long rightShiftUnsignedSlow(int op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and byte).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second byte operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(long op1, byte op2) {
        if (instance.long_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> ((long) op2);
        }
    }

    private static long rightShiftUnsignedSlow(long op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and short).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second short operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(long op1, short op2) {
        if (instance.long_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> ((long) op2);
        }
    }

    private static long rightShiftUnsignedSlow(long op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and int).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second int operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(long op1, int op2) {
        if (instance.long_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> ((long) op2);
        }
    }

    private static long rightShiftUnsignedSlow(long op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }

    /**
     * Performs unsigned right shift on two numeric values (op1 and long).
     * <p>
     * Delegates to the fast path implementation if the operation has not been modified,
     * otherwise invokes the overridden implementation through the meta-programming system.
     *
     * @param op1 the first op1 operand
     * @param op2 the second long operand
     * @return the result of unsigned right shift
     */
    public static long rightShiftUnsigned(long op1, long op2) {
        if (instance.long_rightShiftUnsigned) {
            return rightShiftUnsignedSlow(op1, op2);
        } else {
            return op1 >>> op2;
        }
    }

    private static long rightShiftUnsignedSlow(long op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "rightShiftUnsigned", op2)).longValue();
    }
}
