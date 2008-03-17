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
* Time: 3:38:52 PM
* To change this template use File | Settings | File Templates.
*/
public class CharacterArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(char[].class);

        public Class getReturnType() {
            return Character.class;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final char[] objects = (char[]) object;
            return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                    public Object invoke(Object receiver, Object[] args) {
                        final char[] objects = (char[]) receiver;
                        return objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)];
                    }

                    public Object callBinop(Object receiver, Object arg) {
                        if ((receiver instanceof char[] && arg instanceof Integer)
                                && checkMetaClass()) {
                            final char[] objects = (char[]) receiver;
                            return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                        }
                        else
                          return super.callBinop(receiver,arg);
                    }

                    public Object invokeBinop(Object receiver, Object arg) {
                        final char[] objects = (char[]) receiver;
                        return objects[normaliseIndex(((Integer) arg).intValue(), objects.length)];
                    }
                };
        }
     }
