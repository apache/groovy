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
package org.codehaus.groovy.classgen;

import groovy.lang.*;
import groovy.util.GroovyTestCase;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.Ignore;
import org.objectweb.asm.Opcodes;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Base class for test cases
 */
@Ignore("base class for tests")
public class TestSupport extends GroovyTestCase implements Opcodes {

    protected static boolean DUMP_CLASS = false;

    // ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
    final ClassLoader parentLoader = getClass().getClassLoader();
    protected final GroovyClassLoader loader =
            AccessController.doPrivileged(
                    new PrivilegedAction<GroovyClassLoader>() {
                        @Override
                        public GroovyClassLoader run() {
                            return new GroovyClassLoader(parentLoader);
                        }
                    }
            );
    final CompileUnit unit = new CompileUnit(loader, new CompilerConfiguration());
    final ModuleNode module = new ModuleNode(unit);

    protected Class loadClass(ClassNode classNode) {
        classNode.setModule(module);
        Class fooClass = loader.defineClass(classNode, classNode.getName() + ".groovy", "groovy.testSupport");
        return fooClass;
    }

    protected void assertSetProperty(Object bean, String property, Object newValue) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getWriteMethod();
        assertTrue("has setter method", method != null);
        Object[] args = {newValue};
        Object value = invokeMethod(bean, method, args);
        assertEquals("should return null", null, value);
        assertGetProperty(bean, property, newValue);
    }

    protected void assertGetProperty(Object bean, String property, Object expected) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getReadMethod();
        assertTrue("has getter method", method != null);

        Object[] args = { };
        Object value = invokeMethod(bean, method, args);

        /*
        System.out.println("Expected: " + expected);
        System.out.println("Value: " + value);
        
        if (expected == null) { System.out.println("Expected is null"); }
        if (value == null) { System.out.println("value is null"); }
        */

        assertEquals("property value", expected, value);
    }

    protected Object invokeMethod(Object bean, Method method, Object[] args) throws Exception {
        try {
            return method.invoke(bean, args);
        }
        catch (InvocationTargetException e) {
            fail("InvocationTargetException: " + e.getTargetException());
            return null;
        }
    }

    protected PropertyDescriptor getDescriptor(Object bean, String property) throws Exception {
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(property)) {
                return descriptor;
            }
        }
        fail("Could not find property: " + property + " on bean: " + bean);
        return null;
    }

    protected void assertField(Class aClass, String name, int modifiers, ClassNode type) throws Exception {
        Field field = aClass.getDeclaredField(name);
        assertTrue("Found field called: " + name, field != null);
        assertEquals("Name", name, field.getName());
        assertEquals("Type", type.getName(), field.getType().getName());
        assertEquals("Modifiers", modifiers, field.getModifiers());
    }

    protected ExpressionStatement createPrintlnStatement(Expression expression) throws NoSuchFieldException {
        return new ExpressionStatement(
                new MethodCallExpression(
                        new FieldExpression(FieldNode.newStatic(System.class, "out")),
                        "println",
                        expression));
    }

    /**
     * Asserts that the script runs without any exceptions
     */
    protected void assertScript(String text) throws Exception {
        assertScript(text, getTestClassName());
    }

    protected void assertScript(final String text, final String scriptName) throws Exception {
        log.info("About to execute script");
        log.info(text);
        GroovyCodeSource gcs = AccessController.doPrivileged(
                new PrivilegedAction<GroovyCodeSource>() {
                    @Override
                    public GroovyCodeSource run() {
                        return new GroovyCodeSource(text, scriptName, "/groovy/testSupport");
                    }
                }
        );
        Class groovyClass = loader.parseClass(gcs);
        Script script = InvokerHelper.createScript(groovyClass, new Binding());
        script.run();
    }

    protected void assertScriptFile(String fileName) throws Exception {
        log.info("About to execute script: " + fileName);
        Class groovyClass = loader.parseClass(new GroovyCodeSource(new File(fileName)));
        Script script = InvokerHelper.createScript(groovyClass, new Binding());
        script.run();
    }

    protected GroovyObject compile(String fileName) throws Exception {
        Class groovyClass = loader.parseClass(new GroovyCodeSource(new File(fileName)));
        GroovyObject object = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
        assertTrue(object != null);
        return object;
    }
}
