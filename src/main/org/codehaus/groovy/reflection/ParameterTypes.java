package org.codehaus.groovy.reflection;

import java.lang.reflect.Method;

public class ParameterTypes
{
  private Class [] parameterTypes;
  private boolean [] isArray;

  public ParameterTypes(Method method) {
    parameterTypes = method.getParameterTypes();
  }

  public ParameterTypes(Class [] pt) {
    parameterTypes = pt;
  }

  public boolean isArray (int index) {
    if (isArray == null) {
      isArray = new boolean[parameterTypes.length];
      for (int i = 0; i != parameterTypes.length; ++i)
        isArray [i] = parameterTypes[i].isArray();
    }

    return isArray [index];
  }

  public Class[] getParameterTypes() {
      return parameterTypes;
  }
}
