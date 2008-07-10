package org.codehaus.groovy.runtime.dgmimpl.arrays;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;

public class DoubleArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(double[].class);

        public Class getReturnType() {
            return Double.class;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final double[] objects = (double[]) object;
            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
        }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        public Object call(Object receiver, Object arg) throws Throwable {
            if ((receiver instanceof double[] && arg instanceof Integer)
                    && checkPojoMetaClass()) {
                final double[] objects = (double[]) receiver;
                return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
            }
            else
              return super.call(receiver,arg);
        }
    }
}
