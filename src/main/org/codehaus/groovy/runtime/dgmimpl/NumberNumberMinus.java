package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public class NumberNumberMinus extends NumberNumberMetaMethod {
    public String getName() {
        return "minus";
    }

    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.subtract((Number) object, (Number) arguments[0]);
    }

    /**
     * Substraction of two Numbers.
     *
     * @param left  a Number
     * @param right another Number to substract to the first one
     * @return the substraction
     */
    public static Number minus(Number left, Number right) {
        return NumberMath.subtract(left, right);
    }

    public CallSite createPojoCallSite(CallSite index, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new NumberNumberCallSite (index, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public Object invoke(Object receiver, Object[] args) {
                return math.subtractImpl((Number)receiver,(Number)args[0]);
            }
        };
    }
}
