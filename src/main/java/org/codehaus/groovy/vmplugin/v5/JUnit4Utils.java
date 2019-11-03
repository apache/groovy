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
package org.codehaus.groovy.vmplugin.v5;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Java 5 code for working with JUnit 4 tests.
 *
 * @deprecated use {@link org.apache.groovy.plugin.GroovyRunnerRegistry}
 */
@Deprecated
public class JUnit4Utils {

    /**
     * Utility method to check via reflection if the parsed class appears to be a JUnit4 test.
     *
     * @param scriptClass the class we want to check
     * @param loader the GroovyClassLoader to use to find classes
     * @return true if the class appears to be a test
     */
    static Boolean realIsJUnit4Test(Class scriptClass, GroovyClassLoader loader) {
        // check if there are appropriate class or method annotations
        // that suggest we have a JUnit 4 test
        boolean isTest = false;
        try {
            try {
                Class runWithAnnotationClass = loader.loadClass("org.junit.runner.RunWith");
                Annotation annotation = scriptClass.getAnnotation(runWithAnnotationClass);
                if (annotation != null) {
                    isTest = true;
                } else {
                    Class testAnnotationClass = loader.loadClass("org.junit.Test");
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
        return isTest ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Utility method to run a JUnit4 test.
     *
     * @param scriptClass the class we want to run as a test
     * @return the result of running the test
     */
    static Object realRunJUnit4Test(Class scriptClass, GroovyClassLoader loader) {
        // invoke through reflection to eliminate mandatory JUnit 4 jar dependency

        try {
            Class junitCoreClass = loader.loadClass("org.junit.runner.JUnitCore");
            Object result = InvokerHelper.invokeStaticMethod(junitCoreClass,
                    "runClasses", new Object[]{scriptClass});
            System.out.print("JUnit 4 Runner, Tests: " + InvokerHelper.getProperty(result, "runCount"));
            System.out.print(", Failures: " + InvokerHelper.getProperty(result, "failureCount"));
            System.out.println(", Time: " + InvokerHelper.getProperty(result, "runTime"));
            List failures = (List) InvokerHelper.getProperty(result, "failures");
            for (Object f : failures) {
                System.out.println("Test Failure: " + InvokerHelper.getProperty(f, "description"));
                System.out.println(InvokerHelper.getProperty(f, "trace"));
            }
            return result;
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Error running JUnit 4 test.", e);
        }
    }
}
