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
package org.codehaus.groovy.vmplugin.v9;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.vmplugin.v5.Java5;
import org.codehaus.groovy.vmplugin.v8.Java8;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Additional Java 9 based functions will be added here as needed.
 */
public class Java9 extends Java8 {

    private static class LookupHolder {
        private static final Method PRIVATE_LOOKUP;
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;
        static {
            Constructor<MethodHandles.Lookup> lookup = null;
            Method privateLookup = null;
            try { // java 9
                privateLookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            } catch (final NoSuchMethodException | RuntimeException e) { // java 8 or fallback if anything else goes wrong
                try {
                    lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookup.isAccessible()) {
                        ReflectionUtils.trySetAccessible(lookup);
                    }
                } catch (final NoSuchMethodException ex) {
                    throw new IllegalStateException("Incompatible JVM", e);
                }
            }
            PRIVATE_LOOKUP = privateLookup;
            LOOKUP_Constructor = lookup;
        }
    }

    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        return LookupHolder.LOOKUP_Constructor;
    }

    private static Method getPrivateLookup() {
        return LookupHolder.PRIVATE_LOOKUP;
    }

    public static MethodHandles.Lookup of(final Class<?> declaringClass) {
        try {
            if (getPrivateLookup() != null) {
                return MethodHandles.Lookup.class.cast(getPrivateLookup().invoke(null, declaringClass, MethodHandles.lookup()));
            }
            return getLookupConstructor().newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).in(declaringClass);
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVersion() {
        return 9;
    }

    @Override
    public Object getInvokeSpecialHandle(Method method, Object receiver) {
        if (getLookupConstructor() != null) {
            Class declaringClass = method.getDeclaringClass();
            try {
                return of(declaringClass).unreflectSpecial(method, receiver.getClass()).bindTo(receiver);
            } catch (ReflectiveOperationException e) {
                throw new GroovyBugError(e);
            }
        }
        return super.getInvokeSpecialHandle(method, receiver);
    }

    /**
     * This method may be used by a caller in class C to check whether to enable access to a member of declaring class D successfully
     * if {@link Java5#checkCanSetAccessible(java.lang.reflect.AccessibleObject, java.lang.Class)} returns true and any of the following hold:
     *
     * 1) C and D are in the same module.
     * 2) The member is public and D is public in a package that the module containing D exports to at least the module containing C.
     * 3) The member is protected static, D is public in a package that the module containing D exports to at least the module containing C, and C is a subclass of D.
     * 4) D is in a package that the module containing D opens to at least the module containing C. All packages in unnamed and open modules are open to all modules and so this method always succeeds when D is in an unnamed or open module.
     *
     * @param accessibleObject the accessible object to check
     * @param caller the caller to invoke {@code setAccessible}
     * @return the check result
     */
    public boolean checkCanSetAccessible(AccessibleObject accessibleObject,
                                                Class<?> caller) {

        if (!super.checkCanSetAccessible(accessibleObject, caller)) return false;

        if (caller == MethodHandle.class) {
            throw new IllegalCallerException();   // should not happen
        }

        if (!(accessibleObject instanceof Member)) {
            throw new IllegalArgumentException("accessibleObject should be a member of type: " + accessibleObject);   // should not happen
        }

        Class<?> declaringClass = ((Member) accessibleObject).getDeclaringClass();

        Module callerModule = caller.getModule();
        Module declaringModule = declaringClass.getModule();

        if (callerModule == declaringModule) return true;
        if (callerModule == Object.class.getModule()) return true;
        if (!declaringModule.isNamed()) return true;

        int modifiers;
        if (accessibleObject instanceof Executable) {
            modifiers = ((Executable) accessibleObject).getModifiers();
        } else {
            modifiers = ((Field) accessibleObject).getModifiers();
        }

        return checkAccessible(caller, declaringClass, modifiers, true);
    }


    @Override
    public boolean trySetAccessible(AccessibleObject ao) {
        return ao.trySetAccessible();
    }

    @Override
    public MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod, Class<?>[] params, Class<?> caller) {
        if (!(metaMethod instanceof CachedMethod)) {
            return metaMethod;
        }

        CachedMethod cachedMethod = (CachedMethod) metaMethod;
        CachedClass methodDeclaringClass = cachedMethod.getDeclaringClass();

        if (null == methodDeclaringClass) {
            return metaMethod;
        }

        Class<?> declaringClass = methodDeclaringClass.getTheClass();
        Class theClass = metaClass.getTheClass();

        if (declaringClass == theClass) {
            return metaMethod;
        }

        int modifiers = cachedMethod.getModifiers();

        // if caller can access the method,
        // no need to transform the meta method
        if (checkAccessible(caller, declaringClass, modifiers, false)) {
            return metaMethod;
        }

        // if caller can not access the method,
        // try to find the corresponding method in its derived class
        if (declaringClass.isAssignableFrom(theClass)) {
            Optional<Method> optionalMethod = ReflectionUtils.getMethod(theClass, metaMethod.getName(), params);
            if (optionalMethod.isPresent()) {
                return new CachedMethod(optionalMethod.get());
            }
        }

        return metaMethod;
    }

    private static boolean checkAccessible(Class<?> caller, Class<?> declaringClass, int modifiers, boolean allowIllegalAccess) {
        Module callerModule = caller.getModule();
        Module declaringModule = declaringClass.getModule();
        String pn = declaringClass.getPackageName();

        boolean unnamedModuleAccessNamedModule = !callerModule.isNamed() && declaringModule.isNamed();
        boolean illegalAccess = !allowIllegalAccess && unnamedModuleAccessNamedModule;

        // class is public and package is exported to caller
        boolean isClassPublic = Modifier.isPublic(declaringClass.getModifiers());
        if (isClassPublic && declaringModule.isExported(pn, callerModule)) {
            // member is public
            if (Modifier.isPublic(modifiers)) {
                if (illegalAccess) {
                    return false;
                }

                return true;
            }

            // member is protected-static
            if (Modifier.isProtected(modifiers)
                    && Modifier.isStatic(modifiers)
                    && isSubclassOf(caller, declaringClass)) {
                if (illegalAccess) {
                    return false;
                }

                return true;
            }
        }

        // package is open to caller
        if (declaringModule.isOpen(pn, callerModule)) {
            if (illegalAccess) {
                return false;
            }

            return true;
        }

        return false;
    }

    private static boolean isSubclassOf(Class<?> queryClass, Class<?> ofClass) {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }
}
