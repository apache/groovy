/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.classgen;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.Script;
import groovy.util.GroovyTestCase;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.asm.Constants;

/**
 * Base class for test cases
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TestSupport extends GroovyTestCase implements Constants {

    protected static boolean DUMP_CLASS = false;

    ClassLoader parentLoader = getClass().getClassLoader();
    protected GroovyClassLoader loader = (DUMP_CLASS) ? new DumpingClassLoader(parentLoader): new GroovyClassLoader(parentLoader);
    
    protected Class loadClass(ClassNode classNode) {
        Class fooClass = loader.defineClass(classNode, classNode.getName() + ".groovy");
        return fooClass;
    }

    protected void assertSetProperty(Object bean, String property, Object newValue) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getWriteMethod();
        assertTrue("has setter method", method != null);

        Object[] args = { newValue };
        Object value = invokeMethod(bean, method, args);

        assertEquals("should return null", null, value);

        assertGetProperty(bean, property, newValue);
    }

    protected void assertGetProperty(Object bean, String property, Object expected) throws Exception {
        PropertyDescriptor descriptor = getDescriptor(bean, property);
        Method method = descriptor.getReadMethod();
        assertTrue("has getter method", method != null);

        Object[] args = {
        };
        Object value = invokeMethod(bean, method, args);

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
        for (int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if (descriptor.getName().equals(property)) {
                return descriptor;
            }
        }
        fail("Could not find property: " + property + " on bean: " + bean);
        return null;
    }

    protected void assertField(Class aClass, String name, int modifiers, String typeName) throws Exception {
        Field field = aClass.getDeclaredField(name);

        assertTrue("Found field called: " + name, field != null);
        assertEquals("Name", name, field.getName());
        assertEquals("Type", typeName, field.getType().getName());
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
     * @param script
     */
    protected void assertScript(String text) throws Exception {
        log.info("About to execute script");
        log.info(text);
        
        Class groovyClass = loader.parseClass(new ByteArrayInputStream(text.getBytes()), getTestClassName());
        Script script = InvokerHelper.createScript(groovyClass, new Binding());
        script.run();
    }
    
    protected GroovyObject compile(String fileName) throws Exception {
        Class groovyClass = loader.parseClass(fileName);

        GroovyObject object = (GroovyObject) groovyClass.newInstance();

        assertTrue(object != null);

        return object;
    }
}
