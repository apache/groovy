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
package org.codehaus.groovy.runtime;

import java.lang.reflect.Method;


/**
 * Utility class to call methods through reflection, and falls through using the <code>Invoker</code> to call the method if it fails.
 * The class is particularly useful for Groovy classes implementing <code>GroovyInterceptable</code>,
 * since it is not possible to call any method from this class,
 * because it is intercepted by the <code>invokeMethod()</code> method.
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
