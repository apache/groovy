package org.codehaus.groovy.reflection;

import groovy.lang.MetaMethod;

/**
 * MetaMethod for mixed in classes
 */
public class MixinInstanceMetaMethod extends MetaMethod{
    private final MetaMethod method;
    private final MixinInMetaClass mixinInMetaClass;

    public MixinInstanceMetaMethod(MetaMethod method, MixinInMetaClass mixinInMetaClass) {
        this.method = method;
        this.mixinInMetaClass = mixinInMetaClass;
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public String getName() {
        return method.getName();
    }

    public Class getReturnType() {
        return method.getReturnType();
    }

    public CachedClass getDeclaringClass() {
        return mixinInMetaClass.getInstanceClass();
    }

    public Object invoke(Object object, Object[] arguments) {
        return method.invoke(mixinInMetaClass.getMixinInstance(object), arguments);
    }

    protected Class[] getPT() {
        return method.getNativeParameterTypes();
    }
}
