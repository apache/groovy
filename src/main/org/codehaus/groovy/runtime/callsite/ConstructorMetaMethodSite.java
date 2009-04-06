package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site for invoking static methods
*   meta class  - cached
*   method - not cached
*
* @author Alex Tkachman
*/
public class ConstructorMetaMethodSite extends MetaMethodSite {

    private final int version;

    public ConstructorMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod method, Class [] params) {
        super(site, metaClass, method, params);
        this.version = metaClass.getVersion();
    }

    public final Object invoke(Object receiver, Object [] args) throws Throwable{
        MetaClassHelper.unwrap(args);
        try {
            return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args) )  
        {
            MetaClassHelper.unwrap(args);
            try {
                return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }
}
