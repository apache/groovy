/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.testng;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Integration code for running TestNG tests in Groovy.
 * 
 * @author Paul King
 *
 */
public class TestNgRunner implements GroovyRunner {

    /**
     * Utility method to check via reflection if the parsed class appears to be a TestNG
     * test, i.e.&nsbp;checks whether it appears to be using the relevant TestNG annotations.
     *
     * @param scriptClass the class we want to check
     * @param loader the GroovyClassLoader to use to find classes
     * @return true if the class appears to be a test
     */
    @SuppressWarnings("unchecked")
    public boolean canRun(Class scriptClass, GroovyClassLoader loader) {
        char version = System.getProperty("java.version").charAt(2);
        if (version < '5') {
            return false;
        }
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
                    for (Method method : methods) {
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
        return isTest;
    }

    /**
     * Utility method to run a TestNG test.
     *
     * @param scriptClass the class we want to run as a test
     * @param loader the class loader to use
     * @return the result of running the test
     */
    public Object run(Class scriptClass, GroovyClassLoader loader) {
        // invoke through reflection to eliminate mandatory TestNG jar dependency
        try {
            Class testNGClass = loader.loadClass("org.testng.TestNG");
            Object testng = InvokerHelper.invokeConstructorOf(testNGClass, new Object[]{});
            InvokerHelper.invokeMethod(testng, "setTestClasses", new Object[]{scriptClass});
            Class listenerClass = loader.loadClass("org.testng.TestListenerAdapter");
            Object listener = InvokerHelper.invokeConstructorOf(listenerClass, new Object[]{});
            InvokerHelper.invokeMethod(testng, "addListener", new Object[]{listener});
            return InvokerHelper.invokeMethod(testng, "run", new Object[]{});
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Error running TestNG test.", e);
        }
    }

}
