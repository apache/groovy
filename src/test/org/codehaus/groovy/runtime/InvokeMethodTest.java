/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

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

package org.codehaus.groovy.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.codehaus.groovy.GroovyTestCase;

/**
 * Tests method invocation
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokeMethodTest extends GroovyTestCase {

    protected Invoker invoker = new Invoker();

    // Method invocation tests
    //-------------------------------------------------------------------------
    
    public void testInvokeMethodNoParams() throws Throwable {
        Object value = invoke(this, "mockCallWithNoParams", null);
        assertEquals("return value", "NoParams", value);
    }

    public void testInvokeMethodOneParam() throws Throwable {
        Object value = invoke(this, "mockCallWithOneParam", "abc");
        assertEquals("return value", "OneParam", value);
    }

    public void testInvokeMethodOneParamWhichIsNull() throws Throwable {
        Object value = invoke(this, "mockCallWithOneNullParam", null);
        assertEquals("return value", "OneParamWithNull", value);
    }

    public void testInvokeMethodOneCollectionParameter() throws Throwable {
        Object[] foo = {"a", "b", "c"};
        
        Object value = invoke(this, "mockCallWithOneCollectionParam", foo);
        assertEquals("return value", new Integer(3), value);
        
        List list = new ArrayList();
        list.add("a");
        list.add("b");
        value = invoke(this, "mockCallWithOneCollectionParam", list);
        assertEquals("return value", new Integer(2), value);
    }

    public void testInvokePrintlnMethod() throws Throwable {
        Object value = invoke(System.out, "println", "testing System.out.println...");
        assertEquals("return value", null, value);
    }

    public void testMethodChooserNull() throws Throwable {
        assertMethodChooser("Object", null);
    }

    public void testMethodChooserNoParams() throws Throwable {
        assertMethodChooser("void", new Object[0]);
        assertMethodChooser("void", new ArrayList());
    }

    public void testMethodChooserObject() throws Throwable {
        assertMethodChooser("Object", new Object());
        assertMethodChooser("Object", new Date());
    }

    public void testMethodChooserString() throws Throwable {
        assertMethodChooser("String", "foo");

        /** @todo some type cooercion would be cool here... */
        //assertMethodChooser("String", new StringBuffer());
        //assertMethodChooser("String", new Character('a');
    }

    public void testMethodChooserTwoParams() throws Throwable {
        List list = new ArrayList();
        list.add("foo");
        list.add("bar");
        assertMethodChooser("Object,Object", list);
        
        Object[] blah = { "a", "b" };
        assertMethodChooser("Object,Object", blah);
        
        assertMethodChooser("Object,Object", new Object[2]);
    }

    public void testCollectionMethods() throws Throwable {
        Object list = InvokerHelper.createList(new Object[] {"a", "b"});
        
        Object value = invoke(list, "size", null);
        assertEquals("size of collection", new Integer(2), value);
        
        value = invoke(list, "contains", "a");
        assertEquals("contains method", Boolean.TRUE, value);
    }
    
    public void testNewMethods() throws Throwable {
        Object value = invoke("hello", "size", null);
        assertEquals("size of string", new Integer(5), value);
    }

    public void testStaticMethod() throws Throwable {
        Object value = invoke(DummyBean.class, "dummyStaticMethod", "abc");
        assertEquals("size of string", "ABC", value);
    }
    
    public void testBaseClassMethod() throws Throwable {
        Object object = new DummyBean();
        Object value = invoke(object, "toString", null);
        assertEquals("toString", object.toString(), value);
    }
    
    public void testBaseFailMethod() throws Throwable {
        Object value;
        try {
            value = invoke(this, "fail", "hello");
        }
        catch (AssertionFailedError e) {
            // worked
        }
    }
    
    public void testInvokeUnknownMethod() throws Throwable {
        try {
            Object value = invoke(this, "unknownMethod", "abc");
            fail("Should have thrown an exception");
        }
        catch (InvokerException e) {
            // worked
        }
    }

    public void testClassMethod() throws Throwable {
        Class c = String.class;
        Object value = invoke(c, "getName", null);
        assertEquals("Class.getName()", c.getName(), value);
    }
   

    public void testInvokeMethodWithWrongNumberOfParameters() throws Throwable {
        try {
            Object[] args = { "a", "b" };
            Object value = invoke(this, "unknownMethod", args);
            fail("Should have thrown an exception");
        }
        catch (InvokerException e) {
            // worked
        }
    }

    public void testInvokeMethodOnNullObject() throws Throwable {
        try {
            invoke(null, "mockCallWithNoParams", null);
            fail("Should have thrown an exception");
        }
        catch (NullPointerException e) {
            // worked
        }
    }

    // Mock methods used for testing
    //-------------------------------------------------------------------------

    public Object mockCallWithNoParams() {
        return "NoParams";
    }

    public Object mockCallWithOneParam(Object value) {
        assertEquals("Method not passed in the correct value", "abc", value);
        return "OneParam";
    }

    public Object mockCallWithOneNullParam(Object value) {
        assertEquals("Method not passed in the correct value", null, value);
        return "OneParamWithNull";
    }

    public Integer mockCallWithOneCollectionParam(Object collection) {
        Collection coll = InvokerHelper.asCollection(collection);
        return new Integer(coll.size());
    }
    
    public Object mockOverloadedMethod() {
        return "void";
    }
    
    public Object mockOverloadedMethod(Object object) {
        return "Object";
    }
    
    public Object mockOverloadedMethod(String object) {
        return "String";
    }
    
    public Object mockOverloadedMethod(Object object, Object bar) {
        return "Object,Object";
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------
    
    /**
     * Asserts that invoking the method chooser finds the right overloaded method implementation
     * 
     * @param expected is the expected value of the method
     * @param arguments the argument(s) to the method invocation
     */
    protected void assertMethodChooser(Object expected, Object arguments) throws Throwable {
        Object value = invoke(this, "mockOverloadedMethod", arguments);
        
        assertEquals("Invoking overloaded method for arguments: " + InvokerHelper.toString(arguments), expected, value);
    }

    protected Object invoke(Object object, String method, Object args) throws Throwable {
        try {
            return invoker.invokeMethod(object, method, args);
        }
        catch (InvokerInvocationException e) {
            throw e.getCause();
        }
    }
}
