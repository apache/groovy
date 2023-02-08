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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ReflectPermission;

@SuppressWarnings("removal") // TODO future Groovy versions should deprecate then remove this class
final class AccessPermissionChecker {

    private static final ReflectPermission REFLECT_PERMISSION = new ReflectPermission("suppressAccessChecks");

    private AccessPermissionChecker() {
    }

    private static void checkAccessPermission(final Class<?> declaringClass, final int modifiers, final boolean accessible) {
        SecurityManager securityManager;
        if (accessible && (securityManager = System.getSecurityManager()) != null) {
            if (((modifiers & Modifier.PRIVATE) != 0
                        || ((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && declaringClass.getName().startsWith("java.")))
                    && !GroovyObject.class.isAssignableFrom(declaringClass)) {
                securityManager.checkPermission(REFLECT_PERMISSION);
            } else if ((modifiers & (Modifier.PROTECTED)) != 0 && declaringClass.equals(ClassLoader.class)) {
                securityManager.checkCreateClassLoader();
            }
        }
    }

    static void checkAccessPermission(Method method) {
        try {
            checkAccessPermission(method.getDeclaringClass(), method.getModifiers(), method.isAccessible());
        } catch (java.security.AccessControlException e) {
            throw createCacheAccessControlExceptionOf(method, e);
        }
    }

    static void checkAccessPermission(Constructor constructor) {
        try {
            checkAccessPermission(constructor.getDeclaringClass(), constructor.getModifiers(), constructor.isAccessible());
        } catch (java.security.AccessControlException e) {
            throw createCacheAccessControlExceptionOf(constructor, e);
        }
    }

    private static CacheAccessControlException createCacheAccessControlExceptionOf(Method method, java.security.AccessControlException e) {
        return new CacheAccessControlException(
                "Groovy object can not access method " + method.getName()
                        + " cacheAccessControlExceptionOf class " + method.getDeclaringClass().getName()
                        + " with modifiers \"" + Modifier.toString(method.getModifiers()) + "\"", e);
    }

    private static CacheAccessControlException createCacheAccessControlExceptionOf(Constructor constructor, java.security.AccessControlException e) {
        return new CacheAccessControlException(
                "Groovy object can not access constructor " + constructor.getName()
                        + " cacheAccessControlExceptionOf class " + constructor.getDeclaringClass().getName()
                        + " with modifiers \"" + Modifier.toString(constructor.getModifiers()) + "\"", e);
    }

    static void checkAccessPermission(Field field) {
        try {
            checkAccessPermission(field.getDeclaringClass(), field.getModifiers(), field.isAccessible());
        } catch (java.security.AccessControlException e) {
            throw createCacheAccessControlExceptionOf(field, e);
        }
    }

    private static CacheAccessControlException createCacheAccessControlExceptionOf(Field field, java.security.AccessControlException e) {
        return new CacheAccessControlException(
                "Groovy object can not access field " + field.getName()
                        + " cacheAccessControlExceptionOf class " + field.getDeclaringClass().getName()
                        + " with modifiers \"" + Modifier.toString(field.getModifiers()) + "\"", e);
    }

}
