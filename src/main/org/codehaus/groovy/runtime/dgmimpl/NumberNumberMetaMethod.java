package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteAwareMetaMethod;
import org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.lang.reflect.Modifier;

public abstract class NumberNumberMetaMethod extends CallSiteAwareMetaMethod {
    private static final CachedClass    NUMBER_CLASS = ReflectionCache.getCachedClass(Number.class);
    private static final CachedClass [] NUMBER_CLASS_ARR = new CachedClass[] { NUMBER_CLASS };

    protected NumberNumberMetaMethod() {
        parameterTypes = NUMBER_CLASS_ARR;
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public Class getReturnType() {
        return NUMBER_CLASS.getCachedClass();
    }

    public final CachedClass getDeclaringClass() {
        return NUMBER_CLASS;
    }

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        return PogoMetaMethodSite.createPogoMetaMethodSite(site, metaClass, this, params, args);
    }

    public abstract static class NumberNumberCallSite extends PojoMetaMethodSite {

        final NumberMath math;

        public NumberNumberCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Number receiver, Number arg) {
            super(site, metaClass, metaMethod, params);
            math = NumberMath.getMath(receiver,arg);
        }

        public final boolean acceptBinop(Object receiver, Object arg) {
            return receiver.getClass() == metaClass.getTheClass() // meta class match receiver
//               && ((MetaClassImpl)metaClass).getTheCachedClass().getMetaClassForClass() == metaClass // metaClass still be valid
               && MetaClassHelper.sameClass(params, arg); // right arguments
        }
    }
}
