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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.GroovyTestCase;
import org.codehaus.groovy.lang.Closure;

/**
 * Test the property access of the Invoker class
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class PropertyTest extends GroovyTestCase {

    protected Invoker invoker = new Invoker();

    public void testMapProperties() throws Exception {
        Map map = new HashMap();
        map.put("foo", "abc");
        map.put("bar", new Integer(123));
        
        assertGetSetProperty(map, "foo", "abc", "def");
        assertGetSetProperty(map, "bar", new Integer(123), new Double(12.34));
    }
 
    public void testBeanProperties() throws Exception {
        DummyBean bean = new DummyBean();
        
        assertGetSetProperty(bean, "name", "James", "Bob");
        assertGetSetProperty(bean, "i", new Integer(123), new Integer(455));
        
        // dynamic properties
        assertGetSetProperty(bean, "dynamicFoo", null, "aValue");
        assertGetSetProperty(bean, "dynamicFoo", "aValue", "NewValue");
    }
 
    public void testUsingMethodProperty() throws Exception {
        DummyBean bean = new DummyBean();
        
        assertGetSetProperty(bean, "name", "James", "Bob");

        Object value = InvokerHelper.getProperty(bean, "getName");
        assertTrue("Should have returned a closure: " + value, value instanceof Closure);
        Closure closure = (Closure) value;
        Object result = closure.call(null);
        assertEquals("Result of call to closure", "Bob", result);    
    }

    public void testStaticProperty() throws Exception {
        Object value = InvokerHelper.getProperty(System.class, "out");
        assertEquals("static property out", System.out, value);
    }
    
    public void testClassProperty() throws Exception {
        Class c = String.class;
        Object value = InvokerHelper.getProperty(c, "name");
        assertEquals("class name property", c.getName(), value);
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------

    protected void assertGetSetProperty(Object object, String property, Object currentValue, Object newValue) {
        assertGetProperty(object, property, currentValue);
        
        InvokerHelper.setProperty(object, property, newValue);

        assertGetProperty(object, property, newValue);
    }

    protected void assertGetProperty(Object object, String property, Object expected) {
        Object value = InvokerHelper.getProperty(object, property);
        
        assertEquals("property: " + property + " of: " + object, expected, value);
    }
}
