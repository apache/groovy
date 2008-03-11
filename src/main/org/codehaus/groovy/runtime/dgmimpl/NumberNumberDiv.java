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
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Integer) receiver).doubleValue() / ((Float) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() / ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Integer) receiver).doubleValue() / ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double((double) ((Integer) receiver).intValue() / ((Double) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() / ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double((double)((Integer) receiver).intValue() / ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Long) {
            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Long) receiver).doubleValue() / ((Float) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() / ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() / ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Long) receiver).doubleValue() / ((Double) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() / ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() / ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Float) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() / ((Integer) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() / ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() / ((Integer) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Long)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() / ((Long) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() / ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() / ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() / ((Float) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() / ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() / ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() / ((Double) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() / ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() / ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Double) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() / ((Integer) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() / ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() / ((Integer) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Long)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() / ((Long) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() / ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() / ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() / ((Float) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() / ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() / ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object callBinop(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() / ((Double) arg).doubleValue())
                              : super.callBinop(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() / ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() / ((Double) arg).doubleValue());
                    }
                };
            }

        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public final Object invoke(Object receiver, Object[] args) {
                return math.divideImpl((Number)receiver,(Number)args[0]);
            }

            public final Object invokeBinop(Object receiver, Object arg) {
                return math.divideImpl((Number)receiver,(Number)arg);
            }
        };
    }
}
