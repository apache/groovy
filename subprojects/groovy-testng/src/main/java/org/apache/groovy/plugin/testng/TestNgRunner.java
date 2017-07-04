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
package org.apache.groovy.plugin.testng;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Integration code for running TestNG tests in Groovy.
 */
public class TestNgRunner implements GroovyRunner {

    /** Path to the desired test-output directory, else null */
    private static final String OUTPUT_DIRECTORY = getTestOutputDirectory();

    private static String getTestOutputDirectory() {
        try {
            return System.getProperty("groovy.plugin.testng.output");
        } catch (SecurityException ignore) {
            return null;
        }
    }

    /**
     * Utility method to check via reflection if the parsed class appears to be a TestNG
     * test, i.e. checks whether it appears to be using the relevant TestNG annotations.
     *
     * @param scriptClass the class we want to check
     * @param loader the GroovyClassLoader to use to find classes
     * @return true if the class appears to be a test
     */
    @Override
    public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> testAnnotationClass =
                    (Class<? extends Annotation>) loader.loadClass("org.testng.annotations.Test");
            if (scriptClass.isAnnotationPresent(testAnnotationClass)) {
                return true;
            } else {
                Method[] methods = scriptClass.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(testAnnotationClass)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            // fall through
        }
        return false;
    }

    /**
     * Utility method to run a TestNG test.
     *
     * @param scriptClass the class we want to run as a test
     * @param loader the class loader to use
     * @return the result of running the test
     */
    @Override
    public Object run(Class<?> scriptClass, GroovyClassLoader loader) {
        try {
            Class<?> testNGClass = loader.loadClass("org.testng.TestNG");
            Object testng = InvokerHelper.invokeConstructorOf(testNGClass, new Object[]{});
            InvokerHelper.invokeMethod(testng, "setTestClasses", new Object[]{scriptClass});
            Class<?> listenerClass = loader.loadClass("org.testng.TestListenerAdapter");
            Object listener = InvokerHelper.invokeConstructorOf(listenerClass, new Object[]{});
            InvokerHelper.invokeMethod(testng, "addListener", new Object[]{listener});
            if (OUTPUT_DIRECTORY != null) {
                InvokerHelper.invokeMethod(testng, "setOutputDirectory", new Object[]{OUTPUT_DIRECTORY});
            }
            return InvokerHelper.invokeMethod(testng, "run", new Object[]{});
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Error running TestNG test.", e);
        }
    }

}
