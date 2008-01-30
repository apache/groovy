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
    public StaticMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public final Object invoke(Object receiver, Object [] args) {
        return metaClass.invokeStaticMethod(receiver, name, args);
    }

    public CallSite acceptStatic(Object receiver, Object[] args) {
        if(receiver == metaClass.getTheClass())
          return this;
        else
          return createCallStaticSite((Class) receiver, args);
    }
}
