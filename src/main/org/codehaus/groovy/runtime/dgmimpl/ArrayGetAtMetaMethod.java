package org.codehaus.groovy.runtime.dgmimpl;

public abstract class ArrayGetAtMetaMethod extends ArrayMetaMethod {
    protected ArrayGetAtMetaMethod() {
        parameterTypes = INTEGER_CLASS_ARR;
    }

    public String getName() {
        return "getAt";
    }
}
