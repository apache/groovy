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

import groovy.lang.Closure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * This is a scratch class used to experiment with ASM to see what kind of 
 * stuff is output for normal Java code
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DumpClass {

    private String bar;
    private String result;
    private Object x;

    public String getResult() {
        return result;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String value) {
        this.bar = value;
    }

    public void iterateOverList() {
        // equivalent of
        //        for i in ["a", "b", "c"] {
        //            System.out.println(i);
        //        }
        List list = InvokerHelper.createList(new Object[] { "a", "b", "c" });
        for (Iterator iter = InvokerHelper.asIterator(list); iter.hasNext();) {
            Object i = iter.next();
            InvokerHelper.invokeMethod(System.out, "println", i);
        }
    }

    public void iterateOverMap() {
        Map map = InvokerHelper.createMap(new Object[] { "a", "x", "b", "y", "c", "z" });
        for (Iterator iter = InvokerHelper.asIterator(map); iter.hasNext();) {
            Object i = iter.next();
            InvokerHelper.invokeMethod(System.out, "println", i);
        }
    }

    public void printValues(Object collection) {
        for (Iterator iter = InvokerHelper.asIterator(collection); iter.hasNext();) {
            Object foo = iter.next();
            InvokerHelper.invokeMethod(System.out, "println", foo);
        }
    }

    public Object emptyMethod() {
        return null;
    }

    public void emptyVoidMethod() {
    }

    //    public void testAssertion() {
    //        assert bar == null;
    //        assert result == null : "message";
    //    }

    public void testGroovyAssertion2() {
        if (InvokerHelper.compareEqual(bar, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "message");
        }

        bar = "abc";

        if (InvokerHelper.compareNotEqual(bar, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "not null");
        }

        if (InvokerHelper.compareEqual(bar, "abc")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "not null");
        }
    }

    public void testFieldSet() {
        if (InvokerHelper.compareNotEqual(x, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "message");
        }

        x = "foo";

        if (InvokerHelper.compareEqual(x, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "message");
        }
        if (InvokerHelper.compareNotEqual(x, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "message");
        }
    }
    public void assertFailed() {
        StringBuffer buffer = new StringBuffer("Exception: ");
        buffer.append("x = ");
        buffer.append(x);
        InvokerHelper.assertFailed(buffer, "message");
    }
    
    public void setLocalVar() {
        Object x = null;
        Object i = null;
        for (Iterator iter = InvokerHelper.asIterator(InvokerHelper.createRange(new Integer(0), new Integer(10))); iter.hasNext(); ) {
            i = iter.next();
            x = i;
        }
    }

    public void testGroovyAssertion() {
        x = "abc";
        if (InvokerHelper.compareEqual(x, "foo")) {
        }
        else {
            InvokerHelper.assertFailed("expression", "message");
        }
    }

    
    public void doPlus() {
        Object z = "abcd";
        x = InvokerHelper.invokeMethod(z, "length", null);
    }
    
    public void setBoolean() {
        x = Boolean.TRUE;
    }
    
    public void tryCatch() {
        try {
            InvokerHelper.invokeMethod(this, "testGroovyAssertion", null);
        }
        catch (AssertionError e) {
            InvokerHelper.invokeMethod(this, "onException", e);
        }
        finally {
            InvokerHelper.invokeMethod(this, "finallyBlock", null);
        }
        InvokerHelper.invokeMethod(this, "afterTryCatch", null);
    }
    
    public void doPrintln() {
        Object value = InvokerHelper.getProperty(System.class, "out");
        InvokerHelper.invokeMethod(value, "println", "Hello");
    }
    
    public void doClosure() {
        x = new Closure() {
            public Object call(Object arguments) {
                System.out.println();
                return null;
            }
        };
    }
    
    public Object ifDemo() {
        if (InvokerHelper.compareEqual(bar, "abc")) {
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }


}
