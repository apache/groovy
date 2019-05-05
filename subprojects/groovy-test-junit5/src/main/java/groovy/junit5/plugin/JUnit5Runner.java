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
package groovy.junit5.plugin;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Integration code for running JUnit5 tests in Groovy.
 */
public class JUnit5Runner implements GroovyRunner {

    /**
     * Utility method to check via reflection if the parsed class appears to be a JUnit5
     * test, i.e. checks whether it appears to be using the relevant annotations.
     *
     * @param scriptClass the class we want to check
     * @param loader the GroovyClassLoader to use to find classes
     * @return true if the class appears to be a compatible test
     */
    @Override
    public boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
        if (!tryLoadClass("org.junit.jupiter.api.Test", loader)) {
            return false;
        }
        if (isJUnit5AnnotationPresent(scriptClass.getAnnotations(), loader)) {
            return true;
        }
        Method[] methods = scriptClass.getMethods();
        for (Method method : methods) {
            if (isJUnit5AnnotationPresent(method.getAnnotations(), loader)) {
                return true;
            }
        }
        return false;
    }

    private boolean isJUnit5AnnotationPresent(Annotation[] annotations, GroovyClassLoader loader) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            String name = type.getName();
            if (name.startsWith("org.junit.jupiter.api.") && tryLoadClass(name, loader)) {
                return true;
            }
            if (isJUnit5TestableMetaAnnotationPresent(type) && tryLoadClass(name, loader)) {
                return true;
            }
        }
        return false;
    }

    private boolean isJUnit5TestableMetaAnnotationPresent(Class<? extends Annotation> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if ("org.junit.platform.commons.annotation.Testable".equals(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean tryLoadClass(String name, GroovyClassLoader loader) {
        try {
            loader.loadClass(name);
            return true;
        } catch (ClassNotFoundException ignore) {
            // fall through
        }
        return false;
    }

    /**
     * Utility method to run a JUnit 5 test.
     *
     * @param scriptClass the class we want to run as a test
     * @param loader the class loader to use
     * @return the result of running the test
     */
    @Override
    public Object run(Class<?> scriptClass, GroovyClassLoader loader) {
        try {
            try {
                loader.loadClass("org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder");
            } catch (ClassNotFoundException ignored) {
                // subsequent steps will bomb out but try to give some more friendly information first
                System.err.println("WARNING: Required dependency: org.junit.platform:junit-platform-launcher doesn't appear to be on the classpath");
            }
            Class<?> helper = loader.loadClass("groovy.junit5.plugin.GroovyJUnitRunnerHelper");
            Throwable ifFailed = (Throwable) InvokerHelper.invokeStaticMethod(helper, "execute", new Object[]{scriptClass});
            if (ifFailed != null) {
                throw new GroovyRuntimeException(ifFailed);
            }
            return null;
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Error running JUnit 5 test.", e);
        }
    }

}
