/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.vmplugin.v5;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Java 5 code for working with TestNG tests.
 * 
 * @author Paul King
 *
 */
public class TestNgUtils {

    /**
     * Utility method to check via reflection if the parsed class appears to be a TestNG test.
     *
     * @param scriptClass the class we want to check
     * @param loader the GroovyClassLoader to use to find classes
     * @return true if the class appears to be a test
     */
    static Boolean realIsTestNgTest(Class scriptClass, GroovyClassLoader loader) {
        // check if there are appropriate class or method annotations
        // that suggest we have a TestNG test
        boolean isTest = false;
        try {
            try {
                Class testAnnotationClass = loader.loadClass("org.testng.annotations.Test");
                Annotation annotation = scriptClass.getAnnotation(testAnnotationClass);
                if (annotation != null) {
                    isTest = true;
                } else {
                    Method[] methods = scriptClass.getMethods();
                    for (int i = 0; i < methods.length; i++) {
                        Method method = methods[i];
                        annotation = method.getAnnotation(testAnnotationClass);
                        if (annotation != null) {
                            isTest = true;
                            break;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // fall through
            }
        } catch (Throwable e) {
            // fall through
        }
        return isTest ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Utility method to run a TestNG test.
     *
     * @param scriptClass the class we want to run as a test
     * @return the result of running the test
     */
    static Object realRunTestNgTest(Class scriptClass) {
        // invoke through reflection to eliminate mandatory TestNG jar dependency

        try {
            Object testng = InvokerHelper.invokeConstructorOf("org.testng.TestNG", new Object[]{});
            InvokerHelper.invokeMethod(testng, "setTestClasses", new Object[]{scriptClass});
            Object listener = InvokerHelper.invokeConstructorOf("org.testng.TestListenerAdapter", new Object[]{});
            InvokerHelper.invokeMethod(testng, "addListener", new Object[]{listener});
            Object result = InvokerHelper.invokeMethod(testng, "run", new Object[]{});
            return result;
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Error running TestNG test.");
        }
    }
}
