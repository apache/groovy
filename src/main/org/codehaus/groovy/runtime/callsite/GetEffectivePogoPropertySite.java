package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;
import groovy.lang.MetaProperty;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * Created by IntelliJ IDEA.
* User: applerestore
* Date: May 21, 2008
* Time: 10:37:37 AM
* To change this template use File | Settings | File Templates.
*/
class GetEffectivePogoPropertySite extends AbstractCallSite {
    private final MetaClass metaClass;
    private final MetaProperty effective;

    public GetEffectivePogoPropertySite(CallSite site, MetaClass metaClass, MetaProperty effective) {
        super(site);
        this.metaClass = metaClass;
        this.effective = effective;
    }

    public final Object callGetProperty (Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver).getProperty(receiver);
        } else
            return effective.getProperty(receiver);
    }

    public final CallSite acceptGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    public final Object callGroovyObjectGetProperty (Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject) receiver).getMetaClass() != metaClass) {
            return createGetPropertySite(receiver).getProperty(receiver);
        } else
            return effective.getProperty(receiver);
    }

    public final CallSite acceptGroovyObjectGetProperty(Object receiver) {
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || !(receiver instanceof GroovyObject) || ((GroovyObject)receiver).getMetaClass() != metaClass) {
            return createGroovyObjectGetPropertySite(receiver);
        } else {
            return this;
        }
    }

    public final Object getProperty(Object receiver) {
        return effective.getProperty(receiver);
    }
}
