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

import groovy.lang.GroovyObjectSupport;
import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;

import static groovy.test.GroovyAssert.isAtLeastJdk;

public class SecurityTest extends GroovyTestCase {

    @SuppressWarnings("unused")
    public class TestClass{
        public String publicField;
        protected String protectedField;
        String packagePrivateField;
        private String privateField;

        private boolean methodCalled = false;

        public void publicMethod() {
            privateMethod();
        }

        private void privateMethod() {
            methodCalled = true;
        }

        void packagePrivateMethod() {
            privateMethod();
        }

        void protectedMethod() {
            privateMethod();
        }

        public boolean isMethodCalled() {
            return methodCalled;
        }
    }

    @SuppressWarnings("unused")
    public class TestGroovyClass extends GroovyObjectSupport{
        private String privateField;
        private boolean methodCalled = false;
        private void privateMethod() {
            methodCalled = true;
        }
        public boolean isMethodCalled() {
            return methodCalled;
        }
    }
    SecurityManager restrictiveSecurityManager;
    CachedMethod cachedMethodUnderTest;
    CachedField cachedFieldUnderTest;
    Permissions forbidden;

    public void setUp() {
        forbidden = new Permissions();
        forbidden.add(new ReflectPermission("suppressAccessChecks"));
        restrictiveSecurityManager = new SecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
                if (forbidden.implies(perm))
                    throw new AccessControlException(perm.getName());
            }
        };
    }

    public void tearDown(){
        System.setSecurityManager(null);
    }

    private CachedMethod createCachedMethod(String name) throws Exception {
        return createCachedMethod(TestClass.class, name);
    }

    private CachedMethod createCachedMethod(Class<?> cachedClass, String methodName, Class... parameters) throws NoSuchMethodException {
        Method method = cachedClass.getDeclaredMethod(methodName, parameters);
        method.setAccessible(true);
        return new CachedMethod(null, method);
    }

    private boolean invokesCachedMethod() {
        TestClass object = new TestClass();
        cachedMethodUnderTest.invoke(object, new Object[]{});
        return object.isMethodCalled();
    }

    private CachedField createCachedField(String name) throws Exception {
        Field field = TestClass.class.getDeclaredField(name);
        field.setAccessible(true);
        return new CachedField(field);
    }

    public void testInvokesPublicMethodsWithoutChecks() throws Exception {
        cachedMethodUnderTest = createCachedMethod("publicMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }

    public void testReturnsAccesiblePublicMethodsWithoutChecks() throws Exception {
        cachedMethodUnderTest = createCachedMethod("publicMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertEquals("publicMethod", cachedMethodUnderTest.setAccessible().getName());
        assertEquals("publicMethod", cachedMethodUnderTest.getCachedMethod().getName());
    }

    public void testAccessesPublicFieldsWithoutChecks() throws Exception {
        cachedFieldUnderTest = createCachedField("publicField");
        System.setSecurityManager(restrictiveSecurityManager);
        TestClass object = new TestClass();
        cachedFieldUnderTest.setProperty(object, "value");
        assertEquals("value", cachedFieldUnderTest.getProperty(object));
    }

    public void testInvokesPrivateMethodsWithoutSecurityManager() throws Exception{
        cachedMethodUnderTest = createCachedMethod("privateMethod");
        assertTrue(invokesCachedMethod());
    }

    public void testAccessesPrivateFieldsWithoutSecurityManager() throws Exception {
        cachedFieldUnderTest = createCachedField("privateField");
        System.setSecurityManager(null);
        TestClass object = new TestClass();
        cachedFieldUnderTest.setProperty(object, "value");
        assertEquals("value", cachedFieldUnderTest.getProperty(object));
    }

    public void testReturnsAccesiblePrivateMethodsWithoutSecurityManager() throws Exception {
        cachedMethodUnderTest = createCachedMethod("privateMethod");
        System.setSecurityManager(null);
        assertEquals("privateMethod", cachedMethodUnderTest.setAccessible().getName());
        assertEquals("privateMethod", cachedMethodUnderTest.getCachedMethod().getName());
    }

    public void testChecksReflectPermissionForInvokeOnPrivateMethods() throws Exception {
        cachedMethodUnderTest = createCachedMethod("privateMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        try {
            invokesCachedMethod();
            fail();
        }
        catch (InvokerInvocationException e) {
            assertEquals(CacheAccessControlException.class, e.getCause().getClass());
        }
    }

    public void testChecksReflectPermissionForFieldAccessOnPrivateFields() throws Exception {
        cachedFieldUnderTest = createCachedField("privateField");
        System.setSecurityManager(restrictiveSecurityManager);
        TestClass object = new TestClass();
        try {
            cachedFieldUnderTest.setProperty(object, "value");
            fail();
        }
        catch (CacheAccessControlException e) {
        }

        try {
            cachedFieldUnderTest.getProperty(object);
            fail();
        }
        catch (CacheAccessControlException e) {
        }
    }

    public void testChecksReflectPermissionForMethodAccessOnPrivateMethods() throws Exception {
        cachedMethodUnderTest = createCachedMethod("privateMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        try {
            cachedMethodUnderTest.setAccessible();
            fail();
        }
        catch (CacheAccessControlException e) {
        }

        try {
            cachedMethodUnderTest.getCachedMethod();
            fail();
        }
        catch (CacheAccessControlException e) {
        }
    }

    public void testInvokesPackagePrivateMethodsWithoutChecksInNonRestrictedPackages() throws Exception {
        cachedMethodUnderTest = createCachedMethod("packagePrivateMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }

    public void testChecksReflectPermissionForInvokeOnPackagePrivateMethodsInRestrictedJavaPackages() throws Exception {
        // FIX_JDK9 remove this exemption for JDK9
        if (isAtLeastJdk("9.0")) {
            return;
        }
        cachedMethodUnderTest = createCachedMethod(ClassLoader.class, "getBootstrapClassPath", new Class[0]);
        System.setSecurityManager(restrictiveSecurityManager);

        try {
            cachedMethodUnderTest.invoke(null, new Object[]{});
            fail();
        }
        catch (InvokerInvocationException e) {
            assertEquals(CacheAccessControlException.class, e.getCause().getClass());
        }
    }

    public void testInvokesProtectedMethodsWithoutChecks() throws Exception {
        cachedMethodUnderTest = createCachedMethod("protectedMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }


    public void testChecksCreateClassLoaderPermissionForClassLoaderProtectedMethodAccess() throws Exception {
        cachedMethodUnderTest = createCachedMethod(ClassLoader.class, "defineClass", new Class[]{String.class, ByteBuffer.class, ProtectionDomain.class});
        forbidden = new Permissions();
        forbidden.add(new RuntimePermission("createClassLoader"));
        System.setSecurityManager(restrictiveSecurityManager);

        ClassLoader classLoader = getClass().getClassLoader();

        try {
            cachedMethodUnderTest.invoke(classLoader, new Object[]{null, null, null});
            fail();
        }
        catch (InvokerInvocationException e) {
            assertEquals(CacheAccessControlException.class, e.getCause().getClass());
        }
    }

    public void testInvokesPrivateMethodsInGroovyObjectsWithoutChecks() throws Exception {
        cachedMethodUnderTest = createCachedMethod(TestGroovyClass.class, "privateMethod");
        TestGroovyClass object = new TestGroovyClass();
        System.setSecurityManager(restrictiveSecurityManager);
        cachedMethodUnderTest.invoke(object, new Object[]{});
        assertTrue(object.isMethodCalled());
    }

    public void testAccessesPrivateFieldsInGroovyObjectsWithoutChecks() throws Exception {
        Field field = TestGroovyClass.class.getDeclaredField("privateField");
        field.setAccessible(true);
        cachedFieldUnderTest = new CachedField(field);
        TestGroovyClass object = new TestGroovyClass();
        System.setSecurityManager(restrictiveSecurityManager);
        cachedFieldUnderTest.setProperty(object, "value");
        assertEquals("value", cachedFieldUnderTest.getProperty(object));
    }

}
