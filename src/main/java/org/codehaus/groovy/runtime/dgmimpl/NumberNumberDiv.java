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
package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public final class NumberNumberDiv extends NumberNumberMetaMethod {
    @Override
    public String getName() {
        return "div";
    }

    @Override
    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.divide((Number) object, (Number) arguments[0]);
    }

    /**
     * Divide two Numbers.
     *
     * Note: Method name different from 'divide' to avoid collision with BigInteger method that has
     * different semantics.  We want a BigDecimal result rather than a BigInteger.
     *
     * @param left  a Number
     * @param right another Number
     * @return a Number resulting of the divide operation
     */
    public static Number div(Number left, Number right) {
        return NumberMath.divide(left, right);
    }


    @Override
    public CallSite createIntegerInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return createNumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createIntegerLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return createNumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createIntegerFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new IntegerFloat(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createIntegerDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new IntegerDouble(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createLongInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return createNumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createLongLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return createNumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createLongFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new LongFloat(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createLongDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new LongDouble(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createFloatInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new FloatInteger(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createFloatLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new FloatLong(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createFloatFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new FloatFloat(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createFloatDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new FloatDouble(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createDoubleInteger(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new DoubleInteger(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createDoubleLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new DoubleLong(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createDoubleFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new DoubleFloat(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createDoubleDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new DoubleDouble(site, metaClass, metaMethod, params, receiver, args);
    }

    @Override
    public CallSite createNumberNumber(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new NumberNumber(site, metaClass, metaMethod, params, receiver, args);
    }

    private static class IntegerFloat extends NumberNumberCallSite {
        public IntegerFloat(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Integer) receiver).doubleValue() / ((Float) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return (Integer) receiver / (Double) arg;
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Long) receiver).doubleValue() / ((Float) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Long) receiver).doubleValue() / (Double) arg;
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Float) receiver).doubleValue() / ((Integer) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Float) receiver).doubleValue() / ((Long) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Float) receiver).doubleValue() / ((Float) arg).doubleValue();
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }

        @Override
        public final Object invoke(Object receiver, Object[] args) {
            return ((Float) receiver).doubleValue() / ((Float) args[0]).doubleValue();
        }

        public final Object invoke(Object receiver, Object arg) {
            return ((Float) receiver).doubleValue() / ((Float) arg).doubleValue();
        }
    }

    private static class FloatDouble extends NumberNumberCallSite {
        public FloatDouble(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return ((Float) receiver).doubleValue() / (Double) arg;
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return (Double) receiver / ((Integer) arg).doubleValue();
                }
            }
            catch (ClassCastException e) {//
            }
            return super.call(receiver, arg);
        }

        @Override
        public final Object invoke(Object receiver, Object[] args) {
            return (Double) receiver / ((Integer) args[0]).doubleValue();
        }

        public final Object invoke(Object receiver, Object arg) {
            return (Double) receiver / ((Integer) arg).doubleValue();
        }
    }

    private static class DoubleLong extends NumberNumberCallSite {
        public DoubleLong(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            super(site, metaClass, metaMethod, params, (Number) receiver, (Number) args[0]);
        }

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return (Double) receiver / ((Long) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return (Double) receiver / ((Float) arg).doubleValue();
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

        @Override
        public final Object call(Object receiver, Object arg) throws Throwable {
            try {
                if (checkCall(receiver, arg)) {
                    return (Double) receiver / (Double) arg;
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

        @Override
        public final Object invoke(Object receiver, Object[] args) {
            return math.divideImpl((Number)receiver,(Number)args[0]);
        }

        public final Object invoke(Object receiver, Object arg) {
            return math.divideImpl((Number)receiver,(Number)arg);
        }
    }
}
