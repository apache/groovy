package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import groovy.lang.GroovyRuntimeException;
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

    public final Object call(Object receiver, Object[] args) throws Throwable {
        if(receiver == metaClass.getTheClass()) {
            try {
                return metaClass.invokeStaticMethod(receiver, name, args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    public final Object callStatic(Class receiver, Object[] args) throws Throwable {
        if(receiver == metaClass.getTheClass()) {
            try {
                return metaClass.invokeStaticMethod(receiver, name, args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCallStatic(this, receiver, args);
        }
    }
}
