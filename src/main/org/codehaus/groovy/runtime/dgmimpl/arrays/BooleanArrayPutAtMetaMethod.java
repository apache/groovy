package org.codehaus.groovy.runtime.dgmimpl.arrays;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;

/**
 * Created by IntelliJ IDEA.
* User: applerestore
* Date: Mar 16, 2008
* Time: 3:38:02 PM
* To change this template use File | Settings | File Templates.
*/
public class BooleanArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
        private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(boolean[].class);
        private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

        public BooleanArrayPutAtMetaMethod() {
            parameterTypes = PARAM_CLASS_ARR;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final boolean[] objects = (boolean[]) object;
            final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
            objects[index] = ((Boolean)args[1]).booleanValue();
            return null;
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer) || !(args [1] instanceof Boolean))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
        }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        public Object call(Object receiver, Object[] args) {
            if ((receiver instanceof boolean[] && args[0] instanceof Integer && args[1] instanceof Boolean )
                    && checkPojoMetaClass()) {
                final boolean[] objects = (boolean[]) receiver;
                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Boolean)args[1]).booleanValue();
                return null;
            }
            else
              return super.call(receiver,args);
        }
    }
}
