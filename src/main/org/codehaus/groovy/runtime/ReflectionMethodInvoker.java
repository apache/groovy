/*
 $Id: ReflectionMethodInvoker.java 2910 2005-10-03 19:07:37 +0100 (Mon, 03 Oct 2005) tug $

 Copyright 2005 (C) Guillaume Laforge. All Rights Reserved.

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

import java.lang.reflect.Method;


/**
 * Utility class to call methods through reflection, and falls through using the <code>Invoker</code> to call the method if it fails.
 * The class is particularly useful for Groovy classes implementing <code>GroovyIntercpetable</code>,
 * since it is not possible to call any method from this class,
 * because it is intercepted by the <code>invokeMethod()</code> method.
 *
 * @author Guillaume Laforge
 */
public class ReflectionMethodInvoker {

    /**
     * Invoke a method through reflection.
     * Falls through to using the Invoker to call the method in case the reflection call fails..
     *
     * @param object the object on which to invoke a method
     * @param methodName the name of the method to invoke
     * @param parameters the parameters of the method call
     * @return the result of the method call
     */
    public static Object invoke(Object object, String methodName, Object[] parameters) {
        try {
            Class[] classTypes = new Class[parameters.length];
            for (int i = 0; i < classTypes.length; i++) {
                classTypes[i] = parameters[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, classTypes);
            return method.invoke(object, parameters);
        } catch (Throwable t) {
            return InvokerHelper.invokeMethod(object, methodName,  parameters);
        }
    }

}
