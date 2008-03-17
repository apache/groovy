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
* Time: 3:44:03 PM
* To change this template use File | Settings | File Templates.
*/
public class FloatArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
        private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(float[].class);
        private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

        public FloatArrayPutAtMetaMethod() {
            parameterTypes = PARAM_CLASS_ARR;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final float[] objects = (float[]) object;
            final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
            Object newValue = args[1];
            if (!(newValue instanceof Float)) {
                Number n = (Number) newValue;
                objects[index] = ((Number)newValue).floatValue();
            }
            else
              objects[index] = ((Float)args[1]).floatValue();
            return null;
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer) || !(args [1] instanceof Float))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                    public Object call(Object receiver, Object[] args) {
                        if ((receiver instanceof float[] && args[0] instanceof Integer && args[1] instanceof Float )
                                && checkMetaClass()) {
                            final float[] objects = (float[]) receiver;
                            objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Float)args[1]).floatValue();
                            return null;
                        }
                        else
                          return super.call(receiver,args);
                    }
                };
        }
    }
