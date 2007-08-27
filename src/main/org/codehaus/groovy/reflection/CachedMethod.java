package org.codehaus.groovy.reflection;

import org.codehaus.groovy.reflection.ParameterTypes;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
* User: Alex.Tkachman
* Date: Aug 5, 2007
* Time: 5:01:50 PM
* To change this template use File | Settings | File Templates.
*/
public class CachedMethod extends ParameterTypes
{
  CachedClass clazz;

  public final Method method;

  public CachedMethod(CachedClass clazz, Method method)
  {
    super(method);
    this.method = method;
    this.clazz = clazz;
  }

  public static CachedMethod find(Method method)
  {
    CachedMethod[] methods = ReflectionCache.getCachedClass(method.getDeclaringClass()).getMethods();
    for (int i = 0; i < methods.length; i++)
    {
      CachedMethod cachedMethod = methods[i];
      if (cachedMethod.method.equals(method))
        return cachedMethod;
    }
    return null;
  }
}
