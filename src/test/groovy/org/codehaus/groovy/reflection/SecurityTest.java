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
import groovy.test.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.nio.ByteBuffer;
import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;

import static groovy.test.GroovyAssert.isAtLeastJdk;

public class SecurityTest extends GroovyTestCase {

    private final boolean skip = Runtime.version().feature() >= 24;

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
    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager
    SecurityManager restrictiveSecurityManager;
    CachedMethod cachedMethodUnderTest;
    CachedField cachedFieldUnderTest;
    Permissions forbidden;

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager & AccessControlException
    public void setUp() {
        if (skip) return;
        // Forbidding suppressAccessChecks in the test will make the internal implementation of some JDK fail,
        // so load vm plugin before security manager is installed:
        /*
         *     Caused by: java.security.AccessControlException: suppressAccessChecks
         *         at org.codehaus.groovy.reflection.SecurityTest$1.checkPermission(SecurityTest.java:92)
         *         at java.base/java.lang.reflect.AccessibleObject.checkPermission(AccessibleObject.java:83)
         *         at java.base/java.lang.reflect.Constructor.setAccessible(Constructor.java:180)
         *         at java.base/java.lang.invoke.InnerClassLambdaMetafactory$1.run(InnerClassLambdaMetafactory.java:207)
         *         at java.base/java.lang.invoke.InnerClassLambdaMetafactory$1.run(InnerClassLambdaMetafactory.java:200)
         *         at java.base/java.security.AccessController.doPrivileged(Native Method)
         *         at java.base/java.lang.invoke.InnerClassLambdaMetafactory.buildCallSite(InnerClassLambdaMetafactory.java:199)
         *         at java.base/java.lang.invoke.LambdaMetafactory.metafactory(LambdaMetafactory.java:329)
         *         at java.base/java.lang.invoke.BootstrapMethodInvoker.invoke(BootstrapMethodInvoker.java:127)
         */
        VMPluginFactory.getPlugin();

        forbidden = new Permissions();
        forbidden.add(new ReflectPermission("suppressAccessChecks"));
        restrictiveSecurityManager = new SecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
                if (forbidden.implies(perm))
                    throw new java.security.AccessControlException(perm.getName());
            }
        };
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void tearDown(){
        if (skip) return;
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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testInvokesPublicMethodsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod("publicMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testReturnsAccesiblePublicMethodsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod("publicMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertEquals("publicMethod", cachedMethodUnderTest.setAccessible().getName());
        assertEquals("publicMethod", cachedMethodUnderTest.getName());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testAccessesPublicFieldsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testAccessesPrivateFieldsWithoutSecurityManager() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedFieldUnderTest = createCachedField("privateField");
        System.setSecurityManager(null);
        TestClass object = new TestClass();
        cachedFieldUnderTest.setProperty(object, "value");
        assertEquals("value", cachedFieldUnderTest.getProperty(object));
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testReturnsAccesiblePrivateMethodsWithoutSecurityManager() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod("privateMethod");
        System.setSecurityManager(null);
        assertEquals("privateMethod", cachedMethodUnderTest.setAccessible().getName());
        assertEquals("privateMethod", cachedMethodUnderTest.getName());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testChecksReflectPermissionForInvokeOnPrivateMethods() throws Exception {
        if (isAtLeastJdk("18.0")) return;
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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testChecksReflectPermissionForFieldAccessOnPrivateFields() throws Exception {
        if (isAtLeastJdk("18.0")) return;
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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testChecksReflectPermissionForMethodAccessOnPrivateMethods() throws Exception {
        if (isAtLeastJdk("18.0")) return;
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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testInvokesPackagePrivateMethodsWithoutChecksInNonRestrictedPackages() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod("packagePrivateMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK9+
    public void testChecksReflectPermissionForInvokeOnPackagePrivateMethodsInRestrictedJavaPackages() throws Exception {
        // FIX_JDK9 remove this exemption for JDK9
        if (isAtLeastJdk("9.0")) return;
        cachedMethodUnderTest = createCachedMethod(ClassLoader.class, "getBootstrapClassPath");
        System.setSecurityManager(restrictiveSecurityManager);

        try {
            cachedMethodUnderTest.invoke(null, new Object[]{});
            fail();
        }
        catch (InvokerInvocationException e) {
            assertEquals(CacheAccessControlException.class, e.getCause().getClass());
        }
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testInvokesProtectedMethodsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod("protectedMethod");
        System.setSecurityManager(restrictiveSecurityManager);
        assertTrue(invokesCachedMethod());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK16+
    public void testChecksCreateClassLoaderPermissionForClassLoaderProtectedMethodAccess() throws Exception {
        // Illegal access to java.lang.ClassLoader.defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
        if (isAtLeastJdk("16.0")) return;

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

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testInvokesPrivateMethodsInGroovyObjectsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        cachedMethodUnderTest = createCachedMethod(TestGroovyClass.class, "privateMethod");
        TestGroovyClass object = new TestGroovyClass();
        System.setSecurityManager(restrictiveSecurityManager);
        cachedMethodUnderTest.invoke(object, new Object[]{});
        assertTrue(object.isMethodCalled());
    }

    @SuppressWarnings("removal") // TODO in a future Groovy version remove reference to SecurityManager, for now not run for JDK18+
    public void testAccessesPrivateFieldsInGroovyObjectsWithoutChecks() throws Exception {
        if (isAtLeastJdk("18.0")) return;
        Field field = TestGroovyClass.class.getDeclaredField("privateField");
        field.setAccessible(true);
        cachedFieldUnderTest = new CachedField(field);
        TestGroovyClass object = new TestGroovyClass();
        System.setSecurityManager(restrictiveSecurityManager);
        cachedFieldUnderTest.setProperty(object, "value");
        assertEquals("value", cachedFieldUnderTest.getProperty(object));
    }
}
