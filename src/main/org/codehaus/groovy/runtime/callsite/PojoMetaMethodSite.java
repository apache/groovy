/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.NullObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * POJO call site
 *   meta class - cached
 *   method - cached
 *
 * @author Alex Tkachman
*/
public class PojoMetaMethodSite extends MetaMethodSite {

    protected final int version;

    public PojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion();
    }

    public Object invoke(Object receiver, Object[] args) {
        MetaClassHelper.unwrap(args);
        return metaMethod.doMethodInvoke(receiver,  args);
    }

    public final CallSite acceptCall(Object receiver, Object[] args) {
        return checkAcceptCall(receiver, args) ? this : createCallSite(receiver, args);
    }

    protected final boolean checkPojoMetaClass() {
        return usage.get() == 0
            && ((MetaClassImpl)metaClass).getVersion() == version;
    }

    private boolean checkAcceptCall(Object receiver, Object[] args) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // meta class match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, args);
        }
        catch (NullPointerException e) {
            if (receiver == null)
              return checkAcceptCall(NullObject.getNullObject(), args);

            throw e;
        }
    }

    public final CallSite acceptBinop(Object receiver, Object arg) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // meta class match receiver
               && checkPojoMetaClass()
           && MetaClassHelper.sameClass(params, arg) // right arguments
                ? this
                : createCallSite(receiver, new Object[]{arg});
        }
        catch (NullPointerException e) {
            if (receiver == null)
              return acceptBinop(NullObject.getNullObject(), arg);

            throw e;
        }
    }

    public static CallSite createPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        if (metaMethod instanceof CallSiteAwareMetaMethod) {
            return ((CallSiteAwareMetaMethod)metaMethod).createPojoCallSite(site, metaClass, metaMethod, params, receiver, args);
        }

        if (metaMethod.getClass() == CachedMethod.class)
          return createCachedMethodSite (site, metaClass, metaMethod, params, args);

        return createNonAwareCallSite(site, metaClass, metaMethod, params, args);
    }

    public static CallSite createCachedMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PojoCachedMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    return new PojoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, metaMethod, params);
            }
        }
        return new PojoCachedMethodSite(site, metaClass, metaMethod, params);
    }

    public static CallSite createNonAwareCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PojoMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    return new PojoMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, metaMethod, params);
            }
        }
        return new PojoMetaMethodSite(site, metaClass, metaMethod, params);
    }

    public static class PojoCachedMethodSite extends PojoMetaMethodSite {
        final Method reflect;

        public PojoCachedMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
            reflect = ((CachedMethod)metaMethod).setAccessible();
        }

        public Object invoke(Object receiver, Object[] args) {
            MetaClassHelper.unwrap(args);
            args = metaMethod.coerceArgumentsToClasses(args);
            try {
                try {
                    return reflect.invoke(receiver, args);
                } catch (IllegalArgumentException e) {
                    throw new InvokerInvocationException(e);
                } catch (IllegalAccessException e) {
                    throw new InvokerInvocationException(e);
                } catch (InvocationTargetException e) {
                    throw new InvokerInvocationException(e);
                }
            } catch (Exception e) {
                throw metaMethod.processDoMethodInvokeException(e, receiver, args);
            }
        }
    }

    public static class PojoCachedMethodSiteNoUnwrap extends PojoCachedMethodSite {

        public PojoCachedMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            args = metaMethod.coerceArgumentsToClasses(args);
            try {
                try {
                    return reflect.invoke(receiver, args);
                } catch (IllegalArgumentException e) {
                    throw new InvokerInvocationException(e);
                } catch (IllegalAccessException e) {
                    throw new InvokerInvocationException(e);
                } catch (InvocationTargetException e) {
                    throw new InvokerInvocationException(e);
                }
            } catch (Exception e) {
                throw metaMethod.processDoMethodInvokeException(e, receiver, args);
            }
        }
    }

    public static class PojoCachedMethodSiteNoUnwrapNoCoerce extends PojoCachedMethodSite {

        public PojoCachedMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            try {
                return reflect.invoke(receiver, args);
            } catch (IllegalArgumentException e) {
                throw new InvokerInvocationException(e);
            } catch (IllegalAccessException e) {
                throw new InvokerInvocationException(e);
            } catch (InvocationTargetException e) {
                throw new InvokerInvocationException(e);
            }
        }
    }

    /**
     * Call site where we know there is no need to unwrap arguments
     */
    public static class PojoMetaMethodSiteNoUnwrap extends PojoMetaMethodSite {

        public PojoMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return metaMethod.doMethodInvoke(receiver,  args);
        }
    }

    /**
     * Call site where we know there is no need neither unwrap nor coerce arguments
     */
    public static class PojoMetaMethodSiteNoUnwrapNoCoerce extends PojoMetaMethodSite {

        public PojoMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return metaMethod.invoke(receiver,  args);
        }
    }
}
