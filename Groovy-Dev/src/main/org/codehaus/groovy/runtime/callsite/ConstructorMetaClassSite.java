package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

public class ConstructorMetaClassSite extends MetaClassSite {
    public ConstructorMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass())
          return metaClass.invokeConstructor(args);
        else
          return CallSiteArray.defaultCallConstructor(this, (Class)receiver, args);
    }
}
