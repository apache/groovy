package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyRuntimeException;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

public final class NullCallSite extends AbstractCallSite {
    public NullCallSite(CallSite callSite) {
        super(callSite);
    }

    public final Object call(Object receiver, Object[] args) throws Throwable {
        if (receiver == null) {
            try{
                return InvokerHelper.invokeMethod(NullObject.getNullObject(), name, args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }
    
    public Object getProperty(Object receiver) throws Throwable {
        if (receiver == null) {
            try{
                return InvokerHelper.getProperty(NullObject.getNullObject(), name);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return acceptGetProperty(receiver).getProperty(receiver);
        }
    }
}