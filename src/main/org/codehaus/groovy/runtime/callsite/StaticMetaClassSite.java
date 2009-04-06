package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;

import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * Call site for invoking static methods
 *   meta class  - cached
 *   method - not cached
 *
 * @author Alex Tkachman
 */
public class StaticMetaClassSite extends MetaClassSite {

    private final ClassInfo classInfo;
    private final int version;

    public StaticMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
        classInfo = ClassInfo.getClassInfo(metaClass.getTheClass());
        version = classInfo.getVersion();
    }

    private final boolean checkCall(Object receiver) {
        return receiver == metaClass.getTheClass()
            && version == classInfo.getVersion(); // metaClass still be valid
    }

    public final Object call(Object receiver, Object[] args) throws Throwable {
        if (checkCall(receiver)) {
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
        if (checkCall(receiver)) {
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
