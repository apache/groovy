package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MissingMethodException;

public class MissingMethodExceptionNoStack extends MissingMethodException {

    public MissingMethodExceptionNoStack(String method, Class type, Object[] arguments) {
        this(method,type,arguments,false);
    }

    public MissingMethodExceptionNoStack(String method, Class type, Object[] arguments, boolean isStatic) {
        super (method, type, arguments, isStatic);
    }

    public Throwable fillInStackTrace() {
        return this;
    }
}