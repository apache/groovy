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
package org.apache.groovy.plugin.junit;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A plugin to GroovyShell that is able to run classes that
 * contain JUnit4 Tests.
 *
 * @since 2.5.0
 */
public class Junit4TestRunner implements GroovyRunner {

    /**
     * Utility method to check via reflection if the parsed class appears to be a JUnit4
     * test, i.e. checks whether it appears to be using the relevant JUnit 4 annotations.
     *
     * @param scriptClass the class we want to check
     * @param loader the class loader
     * @return true if the class appears to be a test
     */
    @Override
    public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
        // check if there are appropriate class or method annotations
        // that suggest we have a JUnit 4 test
        boolean isTest = false;
        try {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> runWithAnnotationClass =
                        (Class<? extends Annotation>)loader.loadClass("org.junit.runner.RunWith");
                Annotation annotation = scriptClass.getAnnotation(runWithAnnotationClass);
                if (annotation != null) {
                    isTest = true;
                } else {
                    @SuppressWarnings("unchecked")
                    Class<? extends Annotation> testAnnotationClass =
                            (Class<? extends Annotation>) loader.loadClass("org.junit.Test");
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
     * Run the specified class extending TestCase as a unit test.
     * This is done through reflection, to avoid adding a dependency to the JUnit framework.
     * Otherwise, developers embedding Groovy and using GroovyShell to load/parse/compile
     * groovy scripts and classes would have to add another dependency on their classpath.
     *
     * @param scriptClass the class to be run as a unit test
     * @param loader the class loader
     */
    @Override
    public Object run(Class<?> scriptClass, GroovyClassLoader loader) {
        // invoke through reflection to eliminate mandatory JUnit 4 jar dependency
        try {
            Class<?> junitCoreClass = loader.loadClass("org.junit.runner.JUnitCore");
            Object result = InvokerHelper.invokeStaticMethod(junitCoreClass,
                    "runClasses", new Object[]{scriptClass});
            System.out.print("JUnit 4 Runner, Tests: " + InvokerHelper.getProperty(result, "runCount"));
            System.out.print(", Failures: " + InvokerHelper.getProperty(result, "failureCount"));
            System.out.println(", Time: " + InvokerHelper.getProperty(result, "runTime"));
            List<?> failures = (List<?>) InvokerHelper.getProperty(result, "failures");
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
