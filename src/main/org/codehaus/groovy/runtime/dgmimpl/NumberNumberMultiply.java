package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public final class NumberNumberMultiply extends NumberNumberMetaMethod {
    public String getName() {
        return "multiply";
    }

    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.multiply((Number) object, (Number) arguments[0]);
    }

    /**
     * Multiply two Numbers.
     *
     * @param left  a Number
     * @param right another Number
     * @return the multiplication of both
     */
    //Note:  This method is NOT called if left AND right are both BigIntegers or BigDecimals because
    //those classes implement a method with a better exact match.
    public static Number multiply(Number left, Number right) {
        return NumberMath.multiply(left, right);
    }

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        NumberMath m = NumberMath.getMath((Number)receiver, (Number)args[0]);


        if (receiver instanceof Integer) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Integer(((Integer) receiver).intValue() * ((Integer) arg).intValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Integer(((Integer) receiver).intValue() * ((Integer) args[0]).intValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Integer(((Integer) receiver).intValue() * ((Integer) arg).intValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Long)
                                && checkMetaClass()
                              ? new Long(((Integer) receiver).longValue() * ((Long) arg).longValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Integer) receiver).longValue() * ((Long) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Integer) receiver).longValue() * ((Long) arg).longValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Integer) receiver).doubleValue() * ((Float) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() * ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Integer) receiver).doubleValue() * ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Integer && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Integer) receiver).doubleValue() * ((Double) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() * ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Integer) receiver).doubleValue() * ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Long) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Long(((Long) receiver).longValue() * ((Integer) arg).longValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Long) receiver).longValue() * ((Integer) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Long) receiver).longValue() * ((Integer) arg).longValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Long)
                                && checkMetaClass()
                              ? new Long(((Long) receiver).longValue() * ((Long) arg).longValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Long) receiver).longValue() * ((Long) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Long) receiver).longValue() * ((Long) arg).longValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Long) receiver).doubleValue() * ((Float) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() * ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() * ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Long && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Long) receiver).doubleValue() * ((Double) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() * ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() * ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Float) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() * ((Integer) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() * ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() * ((Integer) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Long)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() * ((Long) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() * ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() * ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() * ((Float) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() * ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() * ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Float && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Float) receiver).doubleValue() * ((Double) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() * ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() * ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Double) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Integer)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() * ((Integer) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() * ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() * ((Integer) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Long)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() * ((Long) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() * ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() * ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Float)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() * ((Float) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() * ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() * ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object multiply(Object receiver, Object arg) {
                        return (receiver instanceof Double && arg instanceof Double)
                                && checkMetaClass()
                              ? new Double(((Double) receiver).doubleValue() * ((Double) arg).doubleValue())
                              : super.multiply(receiver,arg);
                    }

                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() * ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() * ((Double) arg).doubleValue());
                    }
                };
            }

        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public final Object invoke(Object receiver, Object[] args) {
                return math.multiplyImpl((Number)receiver,(Number)args[0]);
            }

            public final Object invokeBinop(Object receiver, Object arg) {
                return math.multiplyImpl((Number)receiver,(Number)arg);
            }
        };
    }
}
