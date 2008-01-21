package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public class NumberNumberMultiply extends NumberNumberMetaMethod {
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
        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public Object invoke(Object receiver, Object[] args) {
                return math.multiplyImpl((Number)receiver,(Number)args[0]);
            }

            public Object invokeBinop(Object receiver, Object arg) {
                return math.multiplyImpl((Number)receiver,(Number)arg);
            }
        };
    }
}
