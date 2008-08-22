/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.reflection;

import groovy.lang.Closure;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.codehaus.groovy.reflection.stdclasses.*; 

public class ReflectionCache {
    private static Map primitiveTypesMap = new HashMap();

    static {
        primitiveTypesMap.put(byte.class, Byte.class);
        primitiveTypesMap.put(boolean.class, Boolean.class);
        primitiveTypesMap.put(char.class, Character.class);
        primitiveTypesMap.put(double.class, Double.class);
        primitiveTypesMap.put(float.class, Float.class);
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

    static TripleKeyHashMap mopNames = new TripleKeyHashMap();

    public static String getMOPMethodName(CachedClass declaringClass, String name, boolean useThis) {
      synchronized (mopNames) {
        TripleKeyHashMap.Entry mopNameEntry = mopNames.getOrPut(declaringClass, name, Boolean.valueOf(useThis));
        if (mopNameEntry.value == null) {
            mopNameEntry.value = new StringBuffer().append(useThis ? "this$" : "super$").append(declaringClass.getSuperClassDistance()).append("$").append(name).toString();
        }
        return (String) mopNameEntry.value;
      }
    }

    static final Map /*<Class,SoftReference<CachedClass>>*/ CACHED_CLASS_MAP = new WeakHashMap();

    static WeakDoubleKeyHashMap assignableMap = new WeakDoubleKeyHashMap();

    private static final CachedClass STRING_CLASS = new StringCachedClass();

    public static boolean isArray(Class klazz) {
//        CachedClass cachedClass = getCachedClass(klazz);
//        return cachedClass.isArray;
      return klazz.getName().charAt(0) == '[';
    }

    static void setAssignableFrom(Class klazz, Class aClass) {
      synchronized (assignableMap) {
        WeakDoubleKeyHashMap.Entry val = assignableMap.getOrPut(klazz, aClass);
        if (val.value == null) {
            val.value = Boolean.TRUE;
        }
      }
    }

    public static boolean isAssignableFrom(Class klazz, Class aClass) {
        if (klazz == aClass)
          return true;

      synchronized (assignableMap) {
        WeakDoubleKeyHashMap.Entry val = assignableMap.getOrPut(klazz, aClass);
        if (val.value == null) {
            val.value = Boolean.valueOf(klazz.isAssignableFrom(aClass));
        }
        return ((Boolean)val.value).booleanValue();
//        return klazz.isAssignableFrom(aClass);
      }
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

    public static final CachedClass OBJECT_CLASS = new CachedClass(Object.class) {
        public synchronized CachedClass getCachedSuperClass() {
            return null;
        }

        public boolean isAssignableFrom(Class argument) {
            return true;
        }
    };

    public static final CachedClass OBJECT_ARRAY_CLASS = getCachedClass(Object[].class);

    /**
     * Get the CachedClass for a the given Class (which may be a Type).
     *
     * @param klazz
     * @return the class
     */
    public static CachedClass getCachedClass(Class klazz) {
        if (klazz == null)
            return null;

        if (klazz == Object.class)
            return OBJECT_CLASS;

        if (klazz == String.class)
            return STRING_CLASS;

        CachedClass cachedClass;
        SoftReference ref;

        synchronized (CACHED_CLASS_MAP) {
            ref = (SoftReference) CACHED_CLASS_MAP.get(klazz);
        }

        if (ref == null || (cachedClass = (CachedClass) ref.get()) == null) {
            if (Number.class.isAssignableFrom(klazz) || klazz.isPrimitive()) {
                if (klazz == Number.class) {
                	cachedClass = new NumberCachedClass(klazz);
                } else if (klazz == Integer.class || klazz ==  Integer.TYPE) {
                	cachedClass = new IntegerCachedClass(klazz, klazz==Integer.class);
                } else if (klazz == Double.class || klazz == Double.TYPE) {
                	cachedClass = new DoubleCachedClass(klazz, klazz==Double.class);
                } else if (klazz == BigDecimal.class) {
                	cachedClass = new BigDecimalCachedClass(klazz);
                } else if (klazz == Long.class || klazz == Long.TYPE) {
                	cachedClass = new LongCachedClass(klazz, klazz==Long.class);
                } else if (klazz == Float.class || klazz == Float.TYPE) { 
                	cachedClass = new FloatCachedClass(klazz, klazz==Float.class);
                } else if (klazz == Short.class || klazz == Short.TYPE) {
                	cachedClass = new ShortCachedClass(klazz, klazz==Short.class);
                } else if (klazz == Boolean.TYPE) {
                	cachedClass = new BooleanCachedClass(klazz, false);
                } else if (klazz == Character.TYPE) { 
                	cachedClass = new CharacterCachedClass(klazz, false);
                } else if (klazz == BigInteger.class) {
                	cachedClass = new BigIntegerCachedClass(klazz);
                } else if (klazz == Byte.class || klazz == Byte.TYPE) {
                	cachedClass = new ByteCachedClass(klazz, klazz==Byte.class);
                } else {
                	cachedClass = new CachedClass(klazz);
                }
            } else {
                if (klazz.getName().charAt(0) == '[')
                    cachedClass = new ArrayCachedClass(klazz);
                else if (klazz == Boolean.class) {
                	cachedClass = new BooleanCachedClass(klazz, true);
                } else if (klazz == Character.class) {
                	cachedClass = new CharacterCachedClass(klazz, true);
                } else if (Closure.class.isAssignableFrom(klazz)) {
                	cachedClass = new CachedClosureClass (klazz);
                } else {
                	cachedClass = new CachedClass(klazz);
                }
            }


            CachedClass fasterCachedClass = null;

            // Double-check put.
            synchronized (CACHED_CLASS_MAP) {
                ref = (SoftReference) CACHED_CLASS_MAP.get(klazz);

                if (ref == null || (fasterCachedClass = (CachedClass) ref.get()) == null) {
                    CACHED_CLASS_MAP.put(klazz, new SoftReference(cachedClass));
                } else {
                    // We must use the one that there first, we should be able to safely toss the one we made.
                    // By locking Class we would eliminate this race, but until the design is corrected we risk
                    // deadlock.
                    cachedClass = fasterCachedClass;
                }
            }

            if (null == fasterCachedClass) {
                // We've got a new CacheClass, now get loaded into the assignableMap.
            	cachedClass.initialize();
            }
        }

        return cachedClass;
    }
}
