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
package org.apache.groovy.plugin;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Provides access to built-in {@link GroovyRunner} instances
 * for the registry.  These instances should be accessed via
 * the registry and not used directly.
 */
final class DefaultRunners {

    /*
     * These runners were originally included directly in GroovyShell.
     * Since they are part of core they are added directly to the
     * GroovyRunnerRegistry rather than via a provider configuration
     * file in META-INF/services. If any of these runners are moved
     * out to a submodule then they should be registered using the
     * provider configuration file (see groovy-testng).
     *
     * These are internal classes and not meant to be referenced
     * outside of the GroovyRunnerRegistry.
     */

    private static final GroovyRunner JUNIT3_TEST = new Junit3TestRunner();
    private static final GroovyRunner JUNIT3_SUITE = new Junit3SuiteRunner();
    private static final GroovyRunner JUNIT4_TEST = new Junit4TestRunner();

    private DefaultRunners() {
    }

    static GroovyRunner junit3TestRunner() {
        return JUNIT3_TEST;
    }

    static GroovyRunner junit3SuiteRunner() {
        return JUNIT3_SUITE;
    }

    static GroovyRunner junit4TestRunner() {
        return JUNIT4_TEST;
    }

    private static class Junit3TestRunner implements GroovyRunner {
        /**
         * Utility method to check through reflection if the class appears to be a
         * JUnit 3.8.x test, i.e. checks if it extends JUnit 3.8.x's TestCase.
         *
         * @param scriptClass the class we want to check
         * @param loader the class loader
         * @return true if the class appears to be a test
         */
        @Override
        public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
            try {
                Class<?> testCaseClass = loader.loadClass("junit.framework.TestCase");
                return testCaseClass.isAssignableFrom(scriptClass);
            } catch (Throwable e) {
                return false;
            }
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
            try {
                Object testSuite = InvokerHelper.invokeConstructorOf("junit.framework.TestSuite", new Object[]{scriptClass});
                return InvokerHelper.invokeStaticMethod("junit.textui.TestRunner", "run", new Object[]{testSuite});
            } catch (ClassNotFoundException e) {
                throw new GroovyRuntimeException("Failed to run the unit test. JUnit is not on the Classpath.", e);
            }
        }
    }

    private static class Junit3SuiteRunner implements GroovyRunner {
        /**
         * Utility method to check through reflection if the class appears to be a
         * JUnit 3.8.x test suite, i.e. checks if it extends JUnit 3.8.x's TestSuite.
         *
         * @param scriptClass the class we want to check
         * @param loader the class loader
         * @return true if the class appears to be a test
         */
        @Override
        public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
            try {
                Class<?> testSuiteClass = loader.loadClass("junit.framework.TestSuite");
                return testSuiteClass.isAssignableFrom(scriptClass);
            } catch (Throwable e) {
                return false;
            }
        }

        /**
         * Run the specified class extending TestSuite as a unit test.
         * This is done through reflection, to avoid adding a dependency to the JUnit framework.
         * Otherwise, developers embedding Groovy and using GroovyShell to load/parse/compile
         * groovy scripts and classes would have to add another dependency on their classpath.
         *
         * @param scriptClass the class to be run as a unit test
         * @param loader the class loader
         */
        @Override
        public Object run(Class<?> scriptClass, GroovyClassLoader loader) {
            try {
                Object testSuite = InvokerHelper.invokeStaticMethod(scriptClass, "suite", new Object[]{});
                return InvokerHelper.invokeStaticMethod("junit.textui.TestRunner", "run", new Object[]{testSuite});
            } catch (ClassNotFoundException e) {
                throw new GroovyRuntimeException("Failed to run the unit test. JUnit is not on the Classpath.", e);
            }
        }
    }

    private static class Junit4TestRunner implements GroovyRunner {
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
            return hasRunWithAnnotation(scriptClass, loader)
                    || hasTestAnnotatedMethod(scriptClass, loader);
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

        private static boolean hasRunWithAnnotation(Class<?> scriptClass, ClassLoader loader) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> runWithAnnotationClass =
                        (Class<? extends Annotation>)loader.loadClass("org.junit.runner.RunWith");
                return scriptClass.isAnnotationPresent(runWithAnnotationClass);
            } catch (Throwable e) {
                return false;
            }
        }

        private static boolean hasTestAnnotatedMethod(Class<?> scriptClass, ClassLoader loader) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> testAnnotationClass =
                        (Class<? extends Annotation>) loader.loadClass("org.junit.Test");
                Method[] methods = scriptClass.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(testAnnotationClass)) {
                        return true;
                    }
                }
            } catch (Throwable e) {
                // fall through
            }
            return false;
        }
    }

}
