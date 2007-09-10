package org.codehaus.groovy.reflection;

import java.lang.reflect.Method;

public class ParameterTypes
{
  private Class [] nativeParamTypes;
  private CachedClass [] parameterTypes;

    public ParameterTypes () {
    }

    public ParameterTypes(Class pt []) {
        nativeParamTypes = pt;
    }

    public ParameterTypes(CachedClass[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isArray (int index) {
      return getParameterTypes()[index].isArray;
    }

  public CachedClass[] getParameterTypes() {
      if (parameterTypes == null) {
          if (nativeParamTypes == null)
            nativeParamTypes = getPT();
          parameterTypes = new CachedClass [nativeParamTypes.length];
          for (int i = 0; i != nativeParamTypes.length; ++i)
            parameterTypes[i] = ReflectionCache.getCachedClass(nativeParamTypes[i]);
      }

      return parameterTypes;
  }

    public Class[] getNativeParameterTypes() {
        if (nativeParamTypes == null) {
            if (parameterTypes != null) {
               nativeParamTypes = new Class [parameterTypes.length];
                for (int i = 0; i != parameterTypes.length; ++i)
                  nativeParamTypes[i] = parameterTypes[i].getCachedClass();
            }
            else
              nativeParamTypes = getPT ();
        }
        return nativeParamTypes;
    }

    Class[] getPT() { return null; }
}
