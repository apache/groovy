package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;

/**
 * Call site for invoking static methods
*   meta class  - cached
*   method - not cached
*
* @author Alex Tkachman
*/
public class ConstructorMetaMethodSite extends MetaMethodSite {

    public ConstructorMetaMethodSite(CallSite site, MetaClass metaClass, MetaMethod method, Class [] params) {
        super(site, metaClass, method, params);
    }

    public final Object invoke(Object receiver, Object [] args) {
        MetaClassHelper.unwrap(args);
        return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
    }

    public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (receiver == metaClass.getTheClass() // meta class match receiver
           && MetaClassHelper.sameClasses(params, args) )  { // right arguments
            MetaClassHelper.unwrap(args);
            return metaMethod.doMethodInvoke(metaClass.getTheClass(), args);
        }
        else
          return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }
}
