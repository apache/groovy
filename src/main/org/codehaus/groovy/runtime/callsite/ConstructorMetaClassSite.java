package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

public class ConstructorMetaClassSite extends MetaClassSite {
    public ConstructorMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public Object invoke(Object receiver, Object[] args) {
        return metaClass.invokeConstructor(args);
    }

    public CallSite acceptConstructor(Object receiver, Object[] args) {
        if (receiver == metaClass.getTheClass())
          return this;
        else
          return createCallConstructorSite((Class)receiver, args);
    }
}
