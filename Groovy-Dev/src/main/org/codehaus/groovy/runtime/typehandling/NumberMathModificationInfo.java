package org.codehaus.groovy.runtime.typehandling;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod;

import java.util.Collections;
import java.util.HashSet;

public class NumberMathModificationInfo {

    public static final NumberMathModificationInfo instance = new NumberMathModificationInfo();

    private final HashSet<String> names = new HashSet<String>();

    private NumberMathModificationInfo() {
        Collections.addAll(names, "plus", "minus", "multiply", "div", "compareTo", "or", "and", "xor", "intdiv", "mod", "leftShift", "rightShift", "rightShiftUnsigned");
    }

    public void checkIfStdMethod(MetaMethod method) {
        if (method.getClass() != NewInstanceMetaMethod.class) {
            String name = method.getName();

            if (method.getParameterTypes().length != 1)
                return;

            if (!method.getParameterTypes()[0].isNumber && method.getParameterTypes()[0].getTheClass() != Object.class)
                return;

            if (!names.contains(name))
                return;

            checkNumberOps(name, method.getDeclaringClass().getTheClass());
        }
    }

    private void checkNumberOps(String name, Class klazz) {
        if ("plus".equals(name)) {
            if (klazz == Byte.class) {
                byte_plus = true;
            }
            if (klazz == Short.class) {
                short_plus = true;
            }
            if (klazz == Integer.class) {
                int_plus = true;
            }
            if (klazz == Long.class) {
                long_plus = true;
            }
            if (klazz == Float.class) {
                float_plus = true;
            }
            if (klazz == Double.class) {
                double_plus = true;
            }
            if (klazz == Object.class) {
                byte_plus = true;
                short_plus = true;
                int_plus = true;
                long_plus = true;
                float_plus = true;
                double_plus = true;
            }
        }
        if ("minus".equals(name)) {
            if (klazz == Byte.class) {
                byte_minus = true;
            }
            if (klazz == Short.class) {
                short_minus = true;
            }
            if (klazz == Integer.class) {
                int_minus = true;
            }
            if (klazz == Long.class) {
                long_minus = true;
            }
            if (klazz == Float.class) {
                float_minus = true;
            }
            if (klazz == Double.class) {
                double_minus = true;
            }
            if (klazz == Object.class) {
                byte_minus = true;
                short_minus = true;
                int_minus = true;
                long_minus = true;
                float_minus = true;
                double_minus = true;
            }
        }
        if ("multiply".equals(name)) {
            if (klazz == Byte.class) {
                byte_multiply = true;
            }
            if (klazz == Short.class) {
                short_multiply = true;
            }
            if (klazz == Integer.class) {
                int_multiply = true;
            }
            if (klazz == Long.class) {
                long_multiply = true;
            }
            if (klazz == Float.class) {
                float_multiply = true;
            }
            if (klazz == Double.class) {
                double_multiply = true;
            }
            if (klazz == Object.class) {
                byte_multiply = true;
                short_multiply = true;
                int_multiply = true;
                long_multiply = true;
                float_multiply = true;
                double_multiply = true;
            }
        }
        if ("div".equals(name)) {
            if (klazz == Byte.class) {
                byte_div = true;
            }
            if (klazz == Short.class) {
                short_div = true;
            }
            if (klazz == Integer.class) {
                int_div = true;
            }
            if (klazz == Long.class) {
                long_div = true;
            }
            if (klazz == Float.class) {
                float_div = true;
            }
            if (klazz == Double.class) {
                double_div = true;
            }
            if (klazz == Object.class) {
                byte_div = true;
                short_div = true;
                int_div = true;
                long_div = true;
                float_div = true;
                double_div = true;
            }
        }
        if ("or".equals(name)) {
            if (klazz == Byte.class) {
                byte_or = true;
            }
            if (klazz == Short.class) {
                short_or = true;
            }
            if (klazz == Integer.class) {
                int_or = true;
            }
            if (klazz == Long.class) {
                long_or = true;
            }
            if (klazz == Float.class) {
                float_or = true;
            }
            if (klazz == Double.class) {
                double_or = true;
            }
            if (klazz == Object.class) {
                byte_or = true;
                short_or = true;
                int_or = true;
                long_or = true;
                float_or = true;
                double_or = true;
            }
        }
        if ("and".equals(name)) {
            if (klazz == Byte.class) {
                byte_and = true;
            }
            if (klazz == Short.class) {
                short_and = true;
            }
            if (klazz == Integer.class) {
                int_and = true;
            }
            if (klazz == Long.class) {
                long_and = true;
            }
            if (klazz == Float.class) {
                float_and = true;
            }
            if (klazz == Double.class) {
                double_and = true;
            }
            if (klazz == Object.class) {
                byte_and = true;
                short_and = true;
                int_and = true;
                long_and = true;
                float_and = true;
                double_and = true;
            }
        }
        if ("xor".equals(name)) {
            if (klazz == Byte.class) {
                byte_xor = true;
            }
            if (klazz == Short.class) {
                short_xor = true;
            }
            if (klazz == Integer.class) {
                int_xor = true;
            }
            if (klazz == Long.class) {
                long_xor = true;
            }
            if (klazz == Float.class) {
                float_xor = true;
            }
            if (klazz == Double.class) {
                double_xor = true;
            }
            if (klazz == Object.class) {
                byte_xor = true;
                short_xor = true;
                int_xor = true;
                long_xor = true;
                float_xor = true;
                double_xor = true;
            }
        }
        if ("intdiv".equals(name)) {
            if (klazz == Byte.class) {
                byte_intdiv = true;
            }
            if (klazz == Short.class) {
                short_intdiv = true;
            }
            if (klazz == Integer.class) {
                int_intdiv = true;
            }
            if (klazz == Long.class) {
                long_intdiv = true;
            }
            if (klazz == Float.class) {
                float_intdiv = true;
            }
            if (klazz == Double.class) {
                double_intdiv = true;
            }
            if (klazz == Object.class) {
                byte_intdiv = true;
                short_intdiv = true;
                int_intdiv = true;
                long_intdiv = true;
                float_intdiv = true;
                double_intdiv = true;
            }
        }
        if ("mod".equals(name)) {
            if (klazz == Byte.class) {
                byte_mod = true;
            }
            if (klazz == Short.class) {
                short_mod = true;
            }
            if (klazz == Integer.class) {
                int_mod = true;
            }
            if (klazz == Long.class) {
                long_mod = true;
            }
            if (klazz == Float.class) {
                float_mod = true;
            }
            if (klazz == Double.class) {
                double_mod = true;
            }
            if (klazz == Object.class) {
                byte_mod = true;
                short_mod = true;
                int_mod = true;
                long_mod = true;
                float_mod = true;
                double_mod = true;
            }
        }
        if ("leftShift".equals(name)) {
            if (klazz == Byte.class) {
                byte_leftShift = true;
            }
            if (klazz == Short.class) {
                short_leftShift = true;
            }
            if (klazz == Integer.class) {
                int_leftShift = true;
            }
            if (klazz == Long.class) {
                long_leftShift = true;
            }
            if (klazz == Float.class) {
                float_leftShift = true;
            }
            if (klazz == Double.class) {
                double_leftShift = true;
            }
            if (klazz == Object.class) {
                byte_leftShift = true;
                short_leftShift = true;
                int_leftShift = true;
                long_leftShift = true;
                float_leftShift = true;
                double_leftShift = true;
            }
        }
        if ("rightShift".equals(name)) {
            if (klazz == Byte.class) {
                byte_rightShift = true;
            }
            if (klazz == Short.class) {
                short_rightShift = true;
            }
            if (klazz == Integer.class) {
                int_rightShift = true;
            }
            if (klazz == Long.class) {
                long_rightShift = true;
            }
            if (klazz == Float.class) {
                float_rightShift = true;
            }
            if (klazz == Double.class) {
                double_rightShift = true;
            }
            if (klazz == Object.class) {
                byte_rightShift = true;
                short_rightShift = true;
                int_rightShift = true;
                long_rightShift = true;
                float_rightShift = true;
                double_rightShift = true;
            }
        }
        if ("rightShiftUnsigned".equals(name)) {
            if (klazz == Byte.class) {
                byte_rightShiftUnsigned = true;
            }
            if (klazz == Short.class) {
                short_rightShiftUnsigned = true;
            }
            if (klazz == Integer.class) {
                int_rightShiftUnsigned = true;
            }
            if (klazz == Long.class) {
                long_rightShiftUnsigned = true;
            }
            if (klazz == Float.class) {
                float_rightShiftUnsigned = true;
            }
            if (klazz == Double.class) {
                double_rightShiftUnsigned = true;
            }
            if (klazz == Object.class) {
                byte_rightShiftUnsigned = true;
                short_rightShiftUnsigned = true;
                int_rightShiftUnsigned = true;
                long_rightShiftUnsigned = true;
                float_rightShiftUnsigned = true;
                double_rightShiftUnsigned = true;
            }
        }
    }

    public boolean byte_plus;
    public boolean short_plus;
    public boolean int_plus;
    public boolean long_plus;
    public boolean float_plus;
    public boolean double_plus;
    public boolean byte_minus;
    public boolean short_minus;
    public boolean int_minus;
    public boolean long_minus;
    public boolean float_minus;
    public boolean double_minus;
    public boolean byte_multiply;
    public boolean short_multiply;
    public boolean int_multiply;
    public boolean long_multiply;
    public boolean float_multiply;
    public boolean double_multiply;
    public boolean byte_div;
    public boolean short_div;
    public boolean int_div;
    public boolean long_div;
    public boolean float_div;
    public boolean double_div;
    public boolean byte_or;
    public boolean short_or;
    public boolean int_or;
    public boolean long_or;
    public boolean float_or;
    public boolean double_or;
    public boolean byte_and;
    public boolean short_and;
    public boolean int_and;
    public boolean long_and;
    public boolean float_and;
    public boolean double_and;
    public boolean byte_xor;
    public boolean short_xor;
    public boolean int_xor;
    public boolean long_xor;
    public boolean float_xor;
    public boolean double_xor;
    public boolean byte_intdiv;
    public boolean short_intdiv;
    public boolean int_intdiv;
    public boolean long_intdiv;
    public boolean float_intdiv;
    public boolean double_intdiv;
    public boolean byte_mod;
    public boolean short_mod;
    public boolean int_mod;
    public boolean long_mod;
    public boolean float_mod;
    public boolean double_mod;
    public boolean byte_leftShift;
    public boolean short_leftShift;
    public boolean int_leftShift;
    public boolean long_leftShift;
    public boolean float_leftShift;
    public boolean double_leftShift;
    public boolean byte_rightShift;
    public boolean short_rightShift;
    public boolean int_rightShift;
    public boolean long_rightShift;
    public boolean float_rightShift;
    public boolean double_rightShift;
    public boolean byte_rightShiftUnsigned;
    public boolean short_rightShiftUnsigned;
    public boolean int_rightShiftUnsigned;
    public boolean long_rightShiftUnsigned;
    public boolean float_rightShiftUnsigned;
    public boolean double_rightShiftUnsigned;

    public static int plus(byte op1, byte op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + ((int) op2);
        }
    }

    private static int plusSlow(byte op1, byte op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    public static int plus(byte op1, short op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + ((int) op2);
        }
    }

    private static int plusSlow(byte op1, short op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    public static int plus(byte op1, int op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((int) op1) + op2;
        }
    }

    private static int plusSlow(byte op1, int op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).intValue();
    }

    public static long plus(byte op1, long op2) {
        if (instance.byte_plus) {
            return plusSlow(op1, op2);
        } else {
            return ((long) op1) + op2;
        }
    }

    private static long plusSlow(byte op1, long op2) {
        return ((Number) InvokerHelper.invokeMethod(op1, "plus", op2)).longValue();
    }

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
