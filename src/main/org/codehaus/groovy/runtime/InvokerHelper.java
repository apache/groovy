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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.lang.Range;
import org.codehaus.groovy.lang.Tuple;

/**
 * A static helper class to make bytecode generation easier and act as a facade over the Invoker
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokerHelper {
    private static final Invoker singleton = new Invoker();

    public static Invoker getInstance() {
        return singleton;
    }

    public static Object invokeMethod(Object object, String methodName, Object arguments) {
        return getInstance().invokeMethod(object, methodName, arguments);
    }

    public static Iterator asIterator(Object collection) {
        return getInstance().asIterator(collection);
    }

    public static Collection asCollection(Object collection) {
        return getInstance().asCollection(collection);
    }

    public static String toString(Object arguments) {
        return getInstance().toString(arguments);
    }

    public static void setProperty(Object object, String property, Object newValue) {
        getInstance().setProperty(object, property, newValue);
    }

    public static Object getProperty(Object object, String property) {
        return getInstance().getProperty(object, property);
    }

    public static boolean asBool(Object object) {
        return false;
    }

    public static boolean compareIdentical(Object left, Object right) {
        return left == right;
    }

    public static boolean compareEqual(Object left, Object right) {
        return getInstance().objectsEqual(left, right);
    }

    public static boolean compareNotEqual(Object left, Object right) {
        return !getInstance().objectsEqual(left, right);
    }

    public static boolean compareLessThan(Object left, Object right) {
        return getInstance().compareTo(left, right) < 0;
    }
    
    public static boolean compareLessThanEqual(Object left, Object right) {
        return getInstance().compareTo(left, right) <= 0;
    }
    
    public static boolean compareGreaterThan(Object left, Object right) {
        return getInstance().compareTo(left, right) > 0;
    }
    
    public static boolean compareGreaterThanEqual(Object left, Object right) {
        return getInstance().compareTo(left, right) >= 0;
    }

    public static Tuple createTuple(Object[] array) {
        return new Tuple(array);
    }

    public static List createList(Object[] values) {
        ArrayList answer = new ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            answer.add(values[i]);
        }
        return answer;
    }

    public static Map createMap(Object[] values) {
        Map answer = new HashMap(values.length / 2);
        int i = 0; 
        while (i < values.length) {
            answer.put(values[i++], values[i++]);
        }
        return answer;
    }

    public static Range createRange(Object from, Object to) {
        return new Range(asInt(from), asInt(to));
    }
    
    public static int asInt(Object value) {
        return getInstance().asInt(value);
    }
    
    public static void assertFailed(String expression, Object message) {
        if (message == null || "".equals(message)) {
            throw new AssertionError("Expression: " + expression);
        }
        else {
            throw new AssertionError("" + message + ". Expression: " + expression);
        }
    }
}
