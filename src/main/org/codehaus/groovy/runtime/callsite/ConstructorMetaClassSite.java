package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;

public class ConstructorMetaClassSite extends MetaClassSite {
    public ConstructorMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass()) {
            try {
                return metaClass.invokeConstructor(args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCallConstructor(this, (Class)receiver, args);
        }
    }
}
