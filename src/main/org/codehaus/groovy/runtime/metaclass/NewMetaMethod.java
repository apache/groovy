package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ParameterTypes;

/**
 * Base class for NewInstanceMetaMethod and NewStaticMetaMethod
 */
public class NewMetaMethod extends ReflectionMetaMethod {
    protected static final CachedClass[] EMPTY_TYPE_ARRAY = {};
    protected CachedClass[] bytecodeParameterTypes ;
    protected ParameterTypes paramTypes;

    public NewMetaMethod(CachedMethod method) {
        super(method);
        bytecodeParameterTypes = method.getParameterTypes();

        int size = bytecodeParameterTypes.length;
        CachedClass[] logicalParameterTypes;
        if (size <= 1) {
            logicalParameterTypes = EMPTY_TYPE_ARRAY;
        } else {
            logicalParameterTypes = new CachedClass[--size];
            System.arraycopy(bytecodeParameterTypes, 1, logicalParameterTypes, 0, size);
        }
        paramTypes = new ParameterTypes(logicalParameterTypes);
    }

    public ParameterTypes getParamTypes() {
        return paramTypes;
    }

    public CachedClass getDeclaringClass() {
        return getBytecodeParameterTypes()[0];
    }

    public CachedClass[] getBytecodeParameterTypes() {
        return bytecodeParameterTypes;
    }

    public CachedClass getOwnerClass() {
        return getBytecodeParameterTypes()[0];
    }
}
