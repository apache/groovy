package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

/**
 * Call site for invoking static methods
 *   meta class  - cached
 *   method - not cached
 *
 * @author Alex Tkachman
*/
public class StaticMetaClassSite extends MetaClassSite {
    public StaticMetaClassSite(String name, MetaClass metaClass) {
        super(name, metaClass);
    }

    public final Object call(Object receiver, Object [] args) {
        return metaClass.invokeStaticMethod(receiver, name, args);
    }

    public boolean accept(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass();
    }
}
