package org.codehaus.groovy.reflection;

import groovy.lang.MetaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class GeneratedMetaMethod extends MetaMethod{
    private final String name;
    private final CachedClass declaringClass;
    private final Class returnType;

    public GeneratedMetaMethod() {
        final Method[] methods = getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("$markerMethod$")) {
                this.name = method.getName().substring("$markerMethod$".length());
                this.returnType = method.getReturnType();
                final Class[] params = method.getParameterTypes();
                this.declaringClass = ReflectionCache.getCachedClass(params[0]);
                Class myParams [] = new Class [params.length-1];
                System.arraycopy(params, 1, myParams, 0, myParams.length);
                nativeParamTypes = myParams;
                return;
            }
        }

        throw new NoSuchMethodError();
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public String getName() {
        return name;
    }

    public Class getReturnType() {
        return returnType;
    }

    public CachedClass getDeclaringClass() {
        return declaringClass;
    }
}
