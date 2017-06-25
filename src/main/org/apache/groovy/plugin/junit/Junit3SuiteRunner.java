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
import org.codehaus.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A plugin to GroovyShell that is able to run classes that
 * contain JUnit3 Test Suites.
 *
 * @since 2.5.0
 */
public class Junit3SuiteRunner implements GroovyRunner {

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
        // check if the parsed class is a TestSuite,
        // so that it is possible to run it as a JUnit test
        boolean isUnitTestSuite = false;
        try {
            try {
                Class<?> testSuiteClass = loader.loadClass("junit.framework.TestSuite");
                // if scriptClass extends TestSuiteClass
                if (testSuiteClass.isAssignableFrom(scriptClass)) {
                    isUnitTestSuite = true;
                }
            } catch (ClassNotFoundException e) {
                // fall through
            }
        } catch (Throwable e) {
            // fall through
        }
        return isUnitTestSuite;
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
