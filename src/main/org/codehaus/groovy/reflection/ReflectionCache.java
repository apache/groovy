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

import org.codehaus.groovy.util.TripleKeyHashMap;

import java.util.HashMap;
import java.util.Map;

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
    }

    static TripleKeyHashMap mopNames = new TripleKeyHashMap();

    public static String getMOPMethodName(CachedClass declaringClass, String name, boolean useThis) {
        TripleKeyHashMap.Entry mopNameEntry = mopNames.getOrPut(declaringClass, name, Boolean.valueOf(useThis));
        if (mopNameEntry.value == null) {
            mopNameEntry.value = new StringBuffer().append(useThis ? "this$" : "super$").append(declaringClass.getSuperClassDistance()).append("$").append(name).toString();
        }
        return (String) mopNameEntry.value;
    }
    
    static final CachedClass STRING_CLASS = getCachedClass(String.class);

    public static boolean isArray(Class klazz) {
      return klazz.getName().charAt(0) == '[';
    }

    static void setAssignableFrom(Class klazz, Class aClass) {
//        SoftDoubleKeyMap.Entry val = (SoftDoubleKeyMap.Entry) assignableMap.getOrPut(klazz, aClass, null);
//        if (val.getValue() == null) {
//            val.setValue(Boolean.TRUE);
//        }
    }

    public static boolean isAssignableFrom(Class klazz, Class aClass) {
        if (klazz == aClass)
          return true;

//        SoftDoubleKeyMap.Entry val = (SoftDoubleKeyMap.Entry) assignableMap.getOrPut(klazz, aClass, null);
//        if (val.getValue() == null) {
//            val.setValue(Boolean.valueOf(klazz.isAssignableFrom(aClass)));
//        }
//        return ((Boolean)val.getValue()).booleanValue();
        return klazz.isAssignableFrom(aClass);
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

    public static final CachedClass OBJECT_CLASS = getCachedClass(Object.class);

    public static final CachedClass OBJECT_ARRAY_CLASS = getCachedClass(Object[].class);

    public static CachedClass getCachedClass(Class klazz) {
        if (klazz == null)
          return null;
        
        return ClassInfo.getClassInfo(klazz).getCachedClass ();
    }

}
