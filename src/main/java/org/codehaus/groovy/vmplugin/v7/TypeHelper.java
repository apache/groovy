/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.vmplugin.v7;


import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class contains helper methods for converting and comparing types.
 * WARNING: This class is for internal use only. do not use it outside of its
 * package and not outside groovy-core.
 */
public class TypeHelper {
    /**
     * Get wrapper class for a given class.
     * If the class is for a primitive number type, then the wrapper class
     * will be returned. If it is no primitive number type, we return the
     * class itself.
     */
    protected static Class getWrapperClass(Class c) {
        if (c == Integer.TYPE) {
            c = Integer.class;
        } else if (c == Byte.TYPE) {
            c = Byte.class;
        } else if (c == Long.TYPE) {
            c = Long.class;
        } else if (c == Double.TYPE) {
            c = Double.class;
        } else if (c == Float.TYPE) {
            c = Float.class;
        } else if (c == Boolean.TYPE) {
            c = Boolean.class;
        } else if (c == Character.TYPE) {
            c = Character.class;
        } else if (c == Short.TYPE) {
            c = Short.class;
        }
        return c;
    }

    /**
     * Realizes an unsharp equal for the class.
     * In general we return true if the provided arguments are the same. But
     * we will also return true if our argument class is a wrapper for
     * the parameter class. For example the parameter is an int and the
     * argument class is a wrapper.
     */
    protected static boolean argumentClassIsParameterClass(Class argumentClass, Class parameterClass) {
        if (argumentClass == parameterClass) return true;
        return getWrapperClass(parameterClass) == argumentClass;
    }

    /**
     * Replaces the types in the callSiteType parameter if more specific types
     * given through the arguments. This is in general the case, unless
     * the argument is null.
     */
    protected static MethodType replaceWithMoreSpecificType(Object[] args, MethodType callSiteType) {
        for (int i = 0; i < args.length; i++) {
            // if argument null, take the static type
            if (args[i] == null) continue;
            if (callSiteType.parameterType(i).isPrimitive()) continue;
            Class argClass = args[i].getClass();
            callSiteType = callSiteType.changeParameterType(i, argClass);
        }
        return callSiteType;
    }

    protected static boolean isIntCategory(Class x) {
        return x == Integer.class || x == int.class ||
                x == Byte.class || x == byte.class ||
                x == Short.class || x == short.class;
    }

    protected static boolean isLongCategory(Class x) {
        return x == Long.class || x == long.class ||
                isIntCategory(x);
    }

    private static boolean isBigIntCategory(Class x) {
        return x == BigInteger.class || isLongCategory(x);
    }

    protected static boolean isBigDecCategory(Class x) {
        return x == BigDecimal.class || isBigIntCategory(x);
    }

    protected static boolean isDoubleCategory(Class x) {
        return x == Float.class || x == float.class ||
                x == Double.class || x == double.class ||
                isBigDecCategory(x);
    }
}
