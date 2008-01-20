package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public class NumberNumberPlus extends NumberNumberMetaMethod {
    public String getName() {
        return "plus";
    }

    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.add((Number) object, (Number) arguments[0]);
    }

    /**
     * Add two numbers and return the result.
     *
     * @param left  a Number
     * @param right another Number to add
     * @return the addition of both Numbers
     */
    public static Number plus(Number left, Number right) {
        return NumberMath.add(left, right);
    }

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public Object invoke(Object receiver, Object[] args) {
                return math.addImpl((Number)receiver,(Number)args[0]);
            }
        };
    }
}
