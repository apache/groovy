package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

public class ConstructorMetaClassSite extends MetaClassSite {
    public ConstructorMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public Object invoke(Object receiver, Object[] args) {
        return metaClass.invokeConstructor(args);
    }

    public boolean accept(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass();
    }
}
