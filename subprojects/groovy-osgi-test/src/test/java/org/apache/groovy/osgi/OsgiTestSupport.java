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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.wiring.BundleWiring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Launches an embedded OSGi framework (whichever {@link FrameworkFactory} is on the
 * test classpath: Felix for {@code test}, Equinox for {@code testEquinox}) and
 * installs the Groovy bundles and generated helper bundles into it.
 */
final class OsgiTestSupport implements AutoCloseable {

    private final Framework framework;
    private final Path workDir;

    OsgiTestSupport() throws Exception {
        workDir = Files.createTempDirectory("osgi-test");
        Map<String, String> config = new HashMap<>();
        config.put("org.osgi.framework.storage", workDir.resolve("cache").toString());
        config.put("org.osgi.framework.storage.clean", "onFirstInit");
        config.put("org.osgi.framework.bootdelegation", "java.*,javax.*,sun.*,com.sun.*,jdk.*");
        FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        framework = factory.newFramework(config);
        framework.start();
    }

    Framework framework() {
        return framework;
    }

    BundleContext context() {
        return framework.getBundleContext();
    }

    boolean isFelix() {
        return "org.apache.felix.framework".equals(framework.getSymbolicName());
    }

    /** Installs the bundle whose path is given by the named system property. */
    Bundle installFromProperty(String property) throws BundleException {
        String path = System.getProperty(property);
        assertNotNull(path, "System property '" + property + "' must be set");
        File file = new File(path);
        assertTrue(file.exists(), "Bundle not found: " + path);
        return context().installBundle(file.toURI().toString());
    }

    /** Installs every bundle in the path list given by the named system property. */
    List<Bundle> installAllFromProperty(String property) throws BundleException {
        String paths = System.getProperty(property);
        assertNotNull(paths, "System property '" + property + "' must be set");
        List<Bundle> bundles = new ArrayList<>();
        for (String path : paths.split(File.pathSeparator)) {
            if (!path.isEmpty()) bundles.add(context().installBundle(new File(path).toURI().toString()));
        }
        return bundles;
    }

    /**
     * Builds and installs a bundle from the given main manifest attributes and entries
     * (entry name to content). {@code Bundle-ManifestVersion} and {@code Bundle-Version}
     * are added.
     */
    Bundle installGenerated(String symbolicName, Map<String, String> headers, Map<String, byte[]> entries) throws IOException, BundleException {
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();
        main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        main.putValue("Bundle-ManifestVersion", "2");
        main.putValue("Bundle-SymbolicName", symbolicName);
        main.putValue("Bundle-Version", "1.0.0");
        headers.forEach(main::putValue);
        File jar = workDir.resolve(symbolicName + ".jar").toFile();
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar), manifest)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        return context().installBundle(jar.toURI().toString());
    }

    /** Builds and installs a code-free bundle that consumes Groovy through one manifest header. */
    Bundle installConsumer(String symbolicName, String header, String value) throws IOException, BundleException {
        return installGenerated(symbolicName, Map.of(header, value), Map.of());
    }

    /**
     * Runs the action with the thread context class loader set to the given bundle's class
     * loader, so that results do not depend on whatever context loader the framework leaves
     * on the thread (Equinox, for instance, installs its own {@code ContextFinder}).
     */
    static <T> T withContextClassLoaderOf(Bundle bundle, Callable<T> action) throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoaderOf(bundle));
        try {
            return action.call();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    static ClassLoader classLoaderOf(Bundle bundle) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        assertNotNull(wiring, bundle.getSymbolicName() + " is not resolved");
        return wiring.getClassLoader();
    }

    static String stateName(int state) {
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

    @Override
    public void close() throws Exception {
        framework.stop();
        framework.waitForStop(10_000);
    }
}
