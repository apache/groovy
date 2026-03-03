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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import groovy.lang.Script;
import groovy.test.GroovyTestCase;
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
import org.objectweb.asm.Opcodes;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base class for test cases.
 */
public abstract class TestSupport extends GroovyTestCase implements Opcodes {

    /**
     * Asserts the script runs without any exceptions.
     */
    protected final void assertScript(String scriptText) throws Exception {
        Class<?> scriptClass = loader.parseClass(doPrivileged(() ->
            new GroovyCodeSource(scriptText, getTestClassName(), "/groovy/testSupport")
        ));
        Script script = InvokerHelper.createScript(scriptClass, new Binding());
        script.run();
    }

    protected final void assertScriptFile(String fileName) throws Exception {
        Class<?> scriptClass = loader.parseClass(doPrivileged(() ->
            new GroovyCodeSource(new File(fileName)))
        );
        Script script = InvokerHelper.createScript(scriptClass, new Binding());
        script.run();
    }

    protected final GroovyObject compile (String fileName) throws Exception {
        Class<?> groovyClass = loader.parseClass(doPrivileged(() ->
            new GroovyCodeSource(new File(fileName)))
        );
        GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
        assertNotNull(groovyObject);
        return groovyObject;
    }

    protected final void assertGetProperty(Object bean, String property, Object expected) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getReadMethod();
        assertNotNull("has getter method", method);

        Object[] args = {};
        Object value = invokeMethod(bean, method, args);

        assertEquals("property value", expected, value);
    }

    protected final void assertSetProperty(Object bean, String property, Object newValue) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getWriteMethod();
        assertNotNull("has setter method", method);
        Object[] args = {newValue};
        Object value = invokeMethod(bean, method, args);
        assertEquals("should return null", null, value);
        assertGetProperty(bean, property, newValue);
    }

    protected final void assertField(Class<?> clazz, String name, int modifiers, ClassNode type) throws Exception {
        Field field = clazz.getDeclaredField(name);
        assertNotNull("Found field called: " + name, field);
        assertEquals("Name", name, field.getName());
        assertEquals("Type", type.getName(), field.getType().getName());
        assertEquals("Modifiers", modifiers, field.getModifiers());
    }

    protected final ExpressionStatement createPrintlnStatement(Expression expression) throws NoSuchFieldException {
        FieldExpression systemDotOut = new FieldExpression(FieldNode.newStatic(System.class, "out"));
        return new ExpressionStatement(new MethodCallExpression(systemDotOut, "println", expression));
    }

    //--------------------------------------------------------------------------

    protected GroovyClassLoader loader;

    protected void setUp() throws Exception {
        ClassLoader parentLoader = getClass().getClassLoader();
        loader = doPrivileged(() -> new GroovyClassLoader(parentLoader));
    }

    protected void tearDown() throws Exception {
        loader.close();
        loader = null;
    }

    protected final Class<?> loadClass(ClassNode classNode) {
        var unit = new CompileUnit(loader, new CompilerConfiguration());
        var module = new ModuleNode(unit);
        classNode.setModule(module);

        return loader.defineClass(classNode, classNode.getName() + ".groovy", "groovy.testSupport");
    }

    private PropertyDescriptor getDescriptor(Object bean, String property) throws Exception {
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

    private Object invokeMethod(Object bean, Method method, Object[] args) throws Exception {
        try {
            return method.invoke(bean, args);
        }
        catch (InvocationTargetException e) {
            fail("InvocationTargetException: " + e.getTargetException());
            return null;
        }
    }

    @SuppressWarnings("removal") // TODO: a future Groovy version should perform the operation not as a privileged action
    private static <T> T doPrivileged(java.security.PrivilegedExceptionAction<T> action) throws Exception {
        return java.security.AccessController.doPrivileged(action);
    }
}
