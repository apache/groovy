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
package org.apache.groovy.osgi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal OSGi integration test using embedded Apache Felix.
 * <p>
 * Verifies that Groovy bundles have correct OSGi metadata for
 * resolution and class loading in an OSGi container.
 */
class OsgiBundleTest {

    private Framework framework;

    @BeforeEach
    void startFramework() throws Exception {
        Path cacheDir = Files.createTempDirectory("osgi-cache");

        Map<String, String> config = new HashMap<>();
        config.put("org.osgi.framework.storage", cacheDir.toAbsolutePath().toString());
        config.put("org.osgi.framework.storage.clean", "onFirstInit");
        config.put("org.osgi.framework.bootdelegation", "java.*,javax.*,sun.*,com.sun.*,jdk.*");

        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        framework = factory.newFramework(config);
        framework.start();
    }

    @AfterEach
    void stopFramework() throws Exception {
        if (framework != null) {
            framework.stop();
            framework.waitForStop(10_000);
        }
    }

    // ---- Groovy core bundle tests ----

    @Test
    void groovyBundleInstalls() throws Exception {
        Bundle bundle = installGroovyBundle();
        assertNotNull(bundle);
        assertTrue(bundle.getState() >= Bundle.INSTALLED);
    }

    @Test
    void groovyBundleHasCorrectSymbolicName() throws Exception {
        Bundle bundle = installGroovyBundle();
        assertEquals("groovy", bundle.getSymbolicName());
    }

    @Test
    void groovyBundleHasCorrectHeaders() throws Exception {
        Bundle bundle = installGroovyBundle();
        assertNotNull(bundle.getHeaders().get("Bundle-Version"));
        assertNotNull(bundle.getHeaders().get("Export-Package"));
        assertNotNull(bundle.getHeaders().get("Import-Package"));
        assertEquals("org.apache.groovy", bundle.getHeaders().get("Automatic-Module-Name"));
    }

    @Test
    void groovyBundleResolves() throws Exception {
        Bundle bundle = installGroovyBundle();

        FrameworkWiring wiring = framework.adapt(FrameworkWiring.class);
        boolean resolved = wiring.resolveBundles(Collections.singleton(bundle));

        assertTrue(resolved, "Groovy bundle should resolve. State: " + stateName(bundle.getState()));
    }

    @Test
    void groovyBundleStartsAndLoadsClasses() throws Exception {
        Bundle bundle = installGroovyBundle();

        FrameworkWiring wiring = framework.adapt(FrameworkWiring.class);
        wiring.resolveBundles(Collections.singleton(bundle));

        bundle.start();
        assertEquals(Bundle.ACTIVE, bundle.getState(), "Bundle should be ACTIVE");

        assertClassLoadable(bundle, "groovy.lang.GroovyObject");
        assertClassLoadable(bundle, "groovy.lang.GroovyShell");
        assertClassLoadable(bundle, "groovy.lang.Closure");
        assertClassLoadable(bundle, "groovy.lang.Script");
        assertClassLoadable(bundle, "org.codehaus.groovy.runtime.InvokerHelper");
    }

    // ---- Groovy JSON fragment tests ----

    @Test
    @org.junit.jupiter.api.Disabled("Fragment resolution needs investigation — see GROOVY-5092")
    void groovyJsonFragmentResolvesAndLoadsClasses() throws Exception {
        String jsonPath = System.getProperty("groovy.json.bundle.path");
        if (jsonPath == null || !new File(jsonPath).exists()) {
            System.out.println("Skipping JSON test — groovy.json.bundle.path not set or jar not found");
            return;
        }

        // Install fragment before host so Felix sees it during resolution
        BundleContext ctx = framework.getBundleContext();
        Bundle jsonBundle = ctx.installBundle("file:" + jsonPath);
        Bundle groovyBundle = installGroovyBundle();

        FrameworkWiring wiring = framework.adapt(FrameworkWiring.class);
        wiring.resolveBundles(Arrays.asList(groovyBundle, jsonBundle));

        assertTrue(groovyBundle.getState() >= Bundle.RESOLVED,
                "Groovy host should resolve. State: " + stateName(groovyBundle.getState()));
        assertTrue(jsonBundle.getState() >= Bundle.RESOLVED,
                "groovy-json fragment should resolve. State: " + stateName(jsonBundle.getState()));

        groovyBundle.start();

        // Fragment classes are loaded through the host bundle
        assertClassLoadable(groovyBundle, "groovy.json.JsonSlurper");
        assertClassLoadable(groovyBundle, "groovy.json.JsonOutput");
    }

    @Test
    @org.junit.jupiter.api.Disabled("Fragment resolution needs investigation — see GROOVY-5092")
    void groovyJsonOutputProducesCorrectResult() throws Exception {
        String jsonPath = System.getProperty("groovy.json.bundle.path");
        if (jsonPath == null || !new File(jsonPath).exists()) {
            System.out.println("Skipping JSON test — groovy.json.bundle.path not set or jar not found");
            return;
        }

        BundleContext ctx = framework.getBundleContext();
        Bundle jsonBundle = ctx.installBundle("file:" + jsonPath);
        Bundle groovyBundle = installGroovyBundle();

        FrameworkWiring wiring = framework.adapt(FrameworkWiring.class);
        wiring.resolveBundles(Arrays.asList(groovyBundle, jsonBundle));
        groovyBundle.start();

        // Use JsonOutput via reflection — we can't compile against it directly
        Class<?> jsonOutputClass = groovyBundle.loadClass("groovy.json.JsonOutput");
        java.lang.reflect.Method toJson = jsonOutputClass.getMethod("toJson", Object.class);
        java.lang.reflect.Method prettyPrint = jsonOutputClass.getMethod("prettyPrint", String.class);

        // Equivalent of: JsonOutput.prettyPrint(JsonOutput.toJson([one: 1, two: 2]))
        Map<String, Integer> data = new java.util.LinkedHashMap<>();
        data.put("one", 1);
        data.put("two", 2);

        String jsonStr = (String) toJson.invoke(null, data);
        String pretty = (String) prettyPrint.invoke(null, jsonStr);
        String expected = "{\n    \"one\": 1,\n    \"two\": 2\n}";
        assertEquals(expected, pretty);
    }

    // ---- helpers ----

    private Bundle installGroovyBundle() throws BundleException {
        String bundlePath = System.getProperty("groovy.bundle.path");
        assertNotNull(bundlePath, "System property 'groovy.bundle.path' must be set");
        assertTrue(new File(bundlePath).exists(), "Groovy jar not found: " + bundlePath);

        BundleContext ctx = framework.getBundleContext();
        return ctx.installBundle("file:" + bundlePath);
    }

    private void assertClassLoadable(Bundle bundle, String className) {
        try {
            Class<?> clazz = bundle.loadClass(className);
            assertNotNull(clazz, "loadClass returned null for " + className);
        } catch (ClassNotFoundException e) {
            fail("Class not found in bundle: " + className + " — " + e.getMessage());
        }
    }

    private static String stateName(int state) {
        return switch (state) {
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.ACTIVE -> "ACTIVE";
            default -> "UNKNOWN(" + state + ")";
        };
    }
}
