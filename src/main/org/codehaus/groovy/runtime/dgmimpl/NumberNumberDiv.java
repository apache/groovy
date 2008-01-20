package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public class NumberNumberDiv extends NumberNumberMetaMethod {
    public String getName() {
        return "div";
    }

    public Object invoke(Object object, Object[] arguments) {
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

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public Object invoke(Object receiver, Object[] args) {
                return math.divideImpl((Number)receiver,(Number)args[0]);
            }
        };
    }
}
