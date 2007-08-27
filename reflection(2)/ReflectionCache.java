package org.codehaus.groovy.reflection;

import groovy.util.ComplexKeyHashMap;
import groovy.util.DoubleKeyHashMap;
import groovy.util.TripleKeyHashMap;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ReflectionCache
{
  private static Map primitiveTypesMap = new HashMap();

  static {
      primitiveTypesMap.put(byte.class, Byte.class);
      primitiveTypesMap.put(boolean.class, Boolean.class);
      primitiveTypesMap.put(char.class, Character.class);
      primitiveTypesMap.put(double.class, Double.class);
      primitiveTypesMap.put(float.class,Float.class);
      primitiveTypesMap.put(int.class, Integer.class);
      primitiveTypesMap.put(long.class, Long.class);
      primitiveTypesMap.put(short.class, Short.class);
    }

  public static Class autoboxType(Class type) {
    final Class res = (Class) primitiveTypesMap.get(type);
    return res == null ? type : res;
/*
    final String name = type.getName();
    switch (name.charAt(0)) {
       case 'b':
           if ("boolean".equals(name))
             return Boolean.class;
           else
             if ("byte".equals(name))
               return Byte.class;
             else
               return null;

       case 'c':
         return "char".equals(name) ? Character.class : null;

       case 'd':
          return "double".equals(name) ? Double.class : null;

      case 'f':
        return "float".equals(name) ? Float.class : null;

      case 'i':
        return "int".equals(name) ? Integer.class : null;

      case 'l':
        return "long".equals(name) ? Long.class : null;

      case 's':
        return "short".equals(name) ? Short.class : null;

       default:
         return null;
    }
*/
  }

  static TripleKeyHashMap mopNames = new TripleKeyHashMap() {
    public boolean checkEquals(Entry e, Object key1, Object key2, Object key3)
    {
      MopNameEntry ee = (MopNameEntry) e;
      return ee.key1 == key1 && ee.key2.equals(key2) && ee.key3 == key3;
    }

    public Entry createEntry()
    {
      return new MopNameEntry();
    }
  };

  private static class MopNameEntry extends TripleKeyHashMap.Entry
  {
    String value;
  }

  public static String getMOPMethodName(Class declaringClass, String name, boolean useThis) {
    MopNameEntry mopNameEntry = (MopNameEntry) mopNames.getOrPut(declaringClass, name, Boolean.valueOf(useThis));
    if (mopNameEntry.value == null) {
      mopNameEntry.value = new StringBuffer().append(useThis ? "this$" : "super$").append(getSuperClassDistance(declaringClass)).append("$").append(name).toString();
    }
    return mopNameEntry.value;
  }

  static final Map /*<Class,SoftReference<CachedClass>>*/ cachedClassMap = new WeakHashMap();

  public static boolean isArray(Class klazz) {
    CachedClass cachedClass = getCachedClass(klazz);
    return cachedClass.isArray;
  }

  static DoubleKeyHashMap assignableMap = new DoubleKeyHashMap() {
    public boolean checkEquals(ComplexKeyHashMap.Entry e, Object key1, Object key2)
    {
      IsAssignableEntry ee = (IsAssignableEntry) e;
      return ee.key1 == key1 && ee.key2 == key2;
    }

    public Entry createEntry()
    {
      return new IsAssignableEntry();
    }
  };

  public static class IsAssignableEntry extends DoubleKeyHashMap.Entry{
    Boolean value;
  }

  public static boolean isAssignableFrom(Class klazz, Class aClass)
  {
    IsAssignableEntry val = (IsAssignableEntry) assignableMap.getOrPut(klazz, aClass);
    if (val.value == null) {
      val.value = Boolean.valueOf(klazz.isAssignableFrom(aClass));
    }
    return val.value.booleanValue();
  }

  static boolean arrayContentsEq(Object[] a1, Object[] a2) {
      if (a1 == null) {
          return a2 == null || a2.length == 0;
      }

      if (a2 == null) {
          return a1.length == 0;
      }

      if (a1.length != a2.length) {
          return false;
      }

      for (int i = 0; i < a1.length; i++) {
          if (a1[i] != a2[i]) {
              return false;
          }
      }

      return true;
  }

  static int getSuperClassDistance(Class klazz)
  {
    CachedClass cachedClass = getCachedClass(klazz);

    synchronized(cachedClass) {
      if (cachedClass.distance == -1) {
        int distance = 0;
        for ( ; klazz != null; klazz = klazz.getSuperclass() )
          distance++;
        cachedClass.distance = distance;
      }
      return cachedClass.distance;
    }
  }

  private static final CachedClass OBJECT_CLASS = new CachedClass(Object.class) {
    public synchronized CachedClass getCachedSuperClass()
    {
      return null;
    }
  };

  public static CachedClass getCachedClass(Class klazz)
  {
    if (klazz == null)
      return null;

    if (klazz == Object.class)
      return OBJECT_CLASS;

    CachedClass cachedClass;
    synchronized(cachedClassMap) {
      SoftReference ref = (SoftReference) cachedClassMap.get(klazz);
      if (ref == null || (cachedClass = (CachedClass) ref.get()) == null) {
        cachedClass = new CachedClass(klazz);
        cachedClassMap.put(klazz, new SoftReference(cachedClass));
      }
    }
    return cachedClass;
  }

  public static Method getDeclaredMethodCached(final Class klazz, String mName, Class[] parms)
  {
    return getCachedClass(klazz).searchMethods(mName, parms);
  }

  public static CachedMethod [] getDeclaredMethodsCached(final Class klazz)
  {
    return getCachedClass(klazz).getMethods();
  }
}
