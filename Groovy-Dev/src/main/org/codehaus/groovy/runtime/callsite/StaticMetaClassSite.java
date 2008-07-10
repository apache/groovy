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

    public final Object call(Object receiver, Object[] args) {
        if(receiver == metaClass.getTheClass())
          return metaClass.invokeStaticMethod(receiver, name, args);
        else
          return CallSiteArray.defaultCall(this, receiver, args);
    }

    public final Object callStatic(Class receiver, Object[] args) {
        if(receiver == metaClass.getTheClass())
          return metaClass.invokeStaticMethod(receiver, name, args);
        else
          return CallSiteArray.defaultCallStatic(this, receiver, args);
    }
}
