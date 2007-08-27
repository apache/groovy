package org.codehaus.groovy.reflection;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
* User: Alex.Tkachman
* Date: Aug 5, 2007
* Time: 5:01:34 PM
* To change this template use File | Settings | File Templates.
*/
public class CachedClass
{
  private CachedClass cachedSuperClass;

  private CachedMethod[] methods;
  private Class cachedClass;

  public final boolean isArray;
  public final boolean isPrimitive;
  public final int modifiers;
  int distance = -1;

  CachedClass(Class klazz) {
    cachedClass = klazz;
    isArray = klazz.isArray();
    isPrimitive = klazz.isPrimitive();
    modifiers = klazz.getModifiers();
  }

  public synchronized CachedClass getCachedSuperClass()
  {
    if ( cachedSuperClass == null )
      cachedSuperClass = ReflectionCache.getCachedClass(cachedClass.getSuperclass());

    return cachedSuperClass;
  }

  public synchronized CachedMethod[] getMethods()
  {
    if (methods == null) {
      final Method[] declaredMethods = cachedClass.getDeclaredMethods();
      methods = new CachedMethod[declaredMethods.length];
      for (int i = 0; i != methods.length; ++i)
        methods [i] = new CachedMethod(CachedClass.this,declaredMethods[i]);
    }
    return methods;
  }

  public Method searchMethods(String name, Class[] parameterTypes)
  {
    CachedMethod[] methods = getMethods();

    Method res = null;
    for (int i = 0; i < methods.length; i++) {
      CachedMethod m = methods[i];
      if (m.method.getName().equals(name)
         && ReflectionCache.arrayContentsEq(parameterTypes, m.getParameterTypes())
         && (res == null || res.getReturnType().isAssignableFrom(m.method.getReturnType())))
      res = m.method;
    }

    return res;
  }

  public Class getCachedClass()
  {
    return cachedClass;
  }

  public int getModifiers()
  {
    return modifiers;
  }
}
