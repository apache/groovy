package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

public class ConstructorMetaClassSite extends MetaClassSite {
    public ConstructorMetaClassSite(MetaClass metaClass) {
        super("", metaClass);
    }

    public Object call(Object receiver, Object[] args) {
        return metaClass.invokeConstructor(args);
    }

    public boolean accept(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass();
    }
}
