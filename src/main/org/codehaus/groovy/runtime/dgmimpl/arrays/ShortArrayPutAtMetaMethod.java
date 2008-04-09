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
* Time: 3:39:40 PM
* To change this template use File | Settings | File Templates.
*/
public class ShortArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
        private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(short[].class);
        private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

        public ShortArrayPutAtMetaMethod() {
            parameterTypes = PARAM_CLASS_ARR;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final short[] objects = (short[]) object;
            final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
            Object newValue = args[1];
            if (!(newValue instanceof Short)) {
                Number n = (Number) newValue;
                objects[index] = ((Number)newValue).shortValue();
            }
            else
              objects[index] = ((Short)args[1]).shortValue();
            return null;
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer) || !(args [1] instanceof Short))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                    public Object call(Object receiver, Object[] args) {
                        if ((receiver instanceof short[] && args[0] instanceof Integer && args[1] instanceof Short )
                                && checkPojoMetaClass()) {
                            final short[] objects = (short[]) receiver;
                            objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Short)args[1]).shortValue();
                            return null;
                        }
                        else
                          return super.call(receiver,args);
                    }
                };
        }
    }
