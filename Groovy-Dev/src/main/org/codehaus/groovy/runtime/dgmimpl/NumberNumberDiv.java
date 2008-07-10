/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public final class NumberNumberDiv extends NumberNumberMetaMethod {
    public String getName() {
        return "div";
    }

    public final Object invoke(Object object, Object[] arguments) {
        return NumberMath.divide((Number) object, (Number) arguments[0]);
    }

    /**
     * Divide two Numbers.
     *
     * @param left  a Number
     * @param right another Number
     * @return a Number resulting of the divide operation
     */
    //Method name changed from 'divide' to avoid collision with BigInteger method that has
    //different semantics.  We want a BigDecimal result rather than a BigInteger.
    public static Number div(Number left, Number right) {
        return NumberMath.divide(left, right);
    }

    public final CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        NumberMath m = NumberMath.getMath((Number)receiver, (Number)args[0]);

        if (receiver instanceof Integer) {
            if (args[0] instanceof Float)
                return new IntegerFloat(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Double)
                return new IntegerDouble(site, metaClass, metaMethod, params, receiver, args);
            }

        if (receiver instanceof Long) {
            if (args[0] instanceof Float)
                return new LongFloat(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Double)
                return new LongDouble(site, metaClass, metaMethod, params, receiver, args);
            }

        if (receiver instanceof Float) {
            if (args[0] instanceof Integer)
                return new FloatInteger(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Long)
                return new FloatLong(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Float)
                return new FloatFloat(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Double)
                return new FloatDouble(site, metaClass, metaMethod, params, receiver, args);
            }

        if (receiver instanceof Double) {
            if (args[0] instanceof Integer)
                return new DoubleInteger(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Long)
                return new DoubleLong(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Float)
                return new DoubleFloat(site, metaClass, metaMethod, params, receiver, args);

            if (args[0] instanceof Double)
                return new DoubleDouble(site, metaClass, metaMethod, params, receiver, args);
            }

        return new NumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    private static class IntegerFloat extends NumberNumberCallSite {
        public IntegerFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Integer) receiver).doubleValue() / ((Float) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class IntegerDouble extends NumberNumberCallSite {
        public IntegerDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Integer) receiver).intValue() / ((Double) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class LongFloat extends NumberNumberCallSite {
        public LongFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Long) receiver).doubleValue() / ((Float) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class LongDouble extends NumberNumberCallSite {
        public LongDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Long) receiver).doubleValue() / ((Double) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class FloatInteger extends NumberNumberCallSite {
        public FloatInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Float) receiver).doubleValue() / ((Integer) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class FloatLong extends NumberNumberCallSite {
        public FloatLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Float) receiver).doubleValue() / ((Long) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class FloatFloat extends NumberNumberCallSite {
        public FloatFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Float) receiver).doubleValue() / ((Float) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return new Double(((Float) receiver).doubleValue() / ((Float) args[0]).doubleValue());
        }

        public final Object invoke(Object receiver, Object arg) {
            return new Double(((Float) receiver).doubleValue() / ((Float) arg).doubleValue());
        }
    }

    private static class FloatDouble extends NumberNumberCallSite {
        public FloatDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Float) receiver).doubleValue() / ((Double) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class DoubleInteger extends NumberNumberCallSite {
        public DoubleInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Double) receiver).doubleValue() / ((Integer) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return new Double(((Double) receiver).doubleValue() / ((Integer) args[0]).doubleValue());
        }

        public final Object invoke(Object receiver, Object arg) {
            return new Double(((Double) receiver).doubleValue() / ((Integer) arg).doubleValue());
        }
    }

    private static class DoubleLong extends NumberNumberCallSite {
        public DoubleLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Double) receiver).doubleValue() / ((Long) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class DoubleFloat extends NumberNumberCallSite {
        public DoubleFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Double) receiver).doubleValue() / ((Float) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class DoubleDouble extends NumberNumberCallSite {
        public DoubleDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkPojoMetaClass()) {
                    return new Double(((Double) receiver).doubleValue() / ((Double) arg).doubleValue());
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }
    }

    private static class NumberNumber extends NumberNumberCallSite {
        public NumberNumber(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return math.divideImpl((Number)receiver,(Number)args[0]);
        }

        public final Object invoke(Object receiver, Object arg) {
            return math.divideImpl((Number)receiver,(Number)arg);
        }
    }
}
