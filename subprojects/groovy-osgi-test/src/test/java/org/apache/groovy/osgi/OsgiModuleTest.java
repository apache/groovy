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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.osgi.OsgiTestSupport.classLoaderOf;
import static org.apache.groovy.osgi.OsgiTestSupport.stateName;
import static org.apache.groovy.osgi.OsgiTestSupport.withContextClassLoaderOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Optional Groovy modules are fragments of the {@code groovy} bundle. These tests install
 * them alongside code-free consumer bundles and exercise them through the consumers, the way
 * applications use them, once without and once with a ServiceLoader Mediator (Aries SPI Fly).
 * <p>
 * groovy-json looks up its {@code FastStringService} with {@code ServiceLoader.load(Class)}
 * (context class loader, so mediation matters), while groovy-reactor's
 * {@code AwaitableAdapter} is looked up by core with an explicit class loader.
 */
class OsgiModuleTest {

    enum Mediator { NONE, SPIFLY }

    private static final String FAST_STRING_SERVICE_FACTORY = "org.apache.groovy.json.FastStringServiceFactory";
    private static final String JSON = "{\"a\":[1,2]}";
    // By name only: these classes need groovy-json, which is not on the test runtime classpath
    private static final String RECORDING_FACTORY = "org.apache.groovy.osgi.provider.RecordingFastStringServiceFactory";
    private static final String RECORDING_SERVICE = RECORDING_FACTORY + "$RecordingFastStringService";

    private OsgiTestSupport osgi;
    private final List<Bundle> toStart = new ArrayList<>();
    private final List<Bundle> installed = new ArrayList<>();

    @BeforeEach
    void startFramework() throws Exception {
        osgi = new OsgiTestSupport();
    }

    @AfterEach
    void stopFramework() throws Exception {
        osgi.close();
    }

    // ---- groovy-json ----

    @ParameterizedTest
    @EnumSource(Mediator.class)
    void groovyJsonWorksViaImportPackage(Mediator mediator) throws Exception {
        installMediator(mediator);
        Bundle json = install(osgi.installFromProperty("groovy.json.bundle.path"));
        Bundle groovy = installStarted(osgi.installFromProperty("groovy.bundle.path"));
        Bundle consumer = installStarted(osgi.installConsumer("consumer.imports", "Import-Package", "groovy.json"));
        resolveAndStart();

        assertAttached(json);
        assertEquals(Bundle.ACTIVE, groovy.getState());
        assertEquals(JSON, parseAndSerialize(consumer));
    }

    @ParameterizedTest
    @EnumSource(Mediator.class)
    void groovyJsonViaRequireBundle(Mediator mediator) throws Exception {
        installMediator(mediator);
        Bundle json = install(osgi.installFromProperty("groovy.json.bundle.path"));
        installStarted(osgi.installFromProperty("groovy.bundle.path"));
        Bundle consumer = installStarted(osgi.installConsumer("consumer.requires", "Require-Bundle", "groovy"));
        resolveAndStart();

        assertAttached(json);
        if (osgi.isFelix()) {
            // Felix does not offer packages exported by an attached fragment to Require-Bundle
            // consumers of the host (Equinox does); this is framework behaviour, reproducible
            // without Groovy. Felix users should use Import-Package.
            assertThrows(ClassNotFoundException.class, () -> classLoaderOf(consumer).loadClass("groovy.json.JsonSlurper"));
        } else {
            assertEquals(JSON, parseAndSerialize(consumer));
        }
    }

    // GROOVY-11578: a consumer using groovy-json while the host is resolved but not yet
    // started gets no mediated service (SPI Fly only registers providers of started bundles)
    @ParameterizedTest
    @EnumSource(Mediator.class)
    void groovyJsonWorksBeforeHostIsStarted(Mediator mediator) throws Exception {
        installMediator(mediator);
        Bundle json = install(osgi.installFromProperty("groovy.json.bundle.path"));
        Bundle groovy = install(osgi.installFromProperty("groovy.bundle.path"));
        Bundle consumer = install(osgi.installConsumer("consumer.imports", "Import-Package", "groovy.json"));
        resolveAndStart();

        assertAttached(json);
        assertEquals(Bundle.RESOLVED, groovy.getState());
        assertEquals(JSON, parseAndSerialize(consumer));
    }

    // With a mediator, a FastStringService provided by another fragment is found and used;
    // without one, groovy-json falls back to its default service.
    @ParameterizedTest
    @EnumSource(Mediator.class)
    void fragmentProvidedFastStringServiceIsMediated(Mediator mediator) throws Exception {
        installMediator(mediator);
        Bundle json = install(osgi.installFromProperty("groovy.json.bundle.path"));
        Bundle provider = install(installRecordingProvider());
        Bundle groovy = installStarted(osgi.installFromProperty("groovy.bundle.path"));
        Bundle consumer = installStarted(osgi.installConsumer("consumer.imports", "Import-Package", "groovy.json"));
        resolveAndStart();

        assertAttached(json);
        assertAttached(provider);
        assertEquals(JSON, parseAndSerialize(consumer));
        int calls = groovy.loadClass(RECORDING_FACTORY).getField("calls").getInt(null);
        if (mediator == Mediator.SPIFLY) {
            assertTrue(calls > 0, "fragment-provided FastStringService should have been used via SPI Fly");
        } else {
            assertEquals(0, calls, "without a mediator the default FastStringService should be used");
        }
    }

    // ---- groovy-reactor ----

    @ParameterizedTest
    @EnumSource(Mediator.class)
    void groovyReactorAwaitableAdapterIsDiscovered(Mediator mediator) throws Exception {
        installMediator(mediator);
        for (Bundle bundle : osgi.installAllFromProperty("osgi.reactor.bundles")) installStarted(bundle);
        Bundle reactor = install(osgi.installFromProperty("groovy.reactor.bundle.path"));
        installStarted(osgi.installFromProperty("groovy.bundle.path"));
        Bundle consumer = installStarted(osgi.installConsumer("consumer.reactor", "Import-Package", "groovy.concurrent,reactor.core.publisher"));
        resolveAndStart();

        assertAttached(reactor);
        Object result = withContextClassLoaderOf(consumer, () -> {
            ClassLoader loader = classLoaderOf(consumer);
            Object mono = loader.loadClass("reactor.core.publisher.Mono").getMethod("just", Object.class).invoke(null, 42);
            Class<?> awaitable = loader.loadClass("groovy.concurrent.Awaitable");
            Object pending = awaitable.getMethod("from", Object.class).invoke(null, mono);
            return awaitable.getMethod("get").invoke(pending);
        });
        assertEquals(42, result);
    }

    // ---- helpers ----

    private void installMediator(Mediator mediator) throws BundleException {
        if (mediator == Mediator.SPIFLY) {
            for (Bundle bundle : osgi.installAllFromProperty("osgi.spifly.bundles")) installStarted(bundle);
        }
    }

    private Bundle install(Bundle bundle) {
        installed.add(bundle);
        return bundle;
    }

    private Bundle installStarted(Bundle bundle) {
        toStart.add(bundle);
        return install(bundle);
    }

    /** Resolves everything installed, then starts the requested non-fragment bundles in install order. */
    private void resolveAndStart() throws BundleException {
        osgi.framework().adapt(FrameworkWiring.class).resolveBundles(installed);
        for (Bundle bundle : toStart) {
            if (bundle.getHeaders().get("Fragment-Host") == null) bundle.start();
        }
    }

    private Bundle installRecordingProvider() throws IOException, BundleException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Fragment-Host", "groovy");
        headers.put("Require-Capability", "osgi.extender;filter:=\"(osgi.extender=osgi.serviceloader.registrar)\";resolution:=optional");
        headers.put("Provide-Capability", "osgi.serviceloader;osgi.serviceloader=\"" + FAST_STRING_SERVICE_FACTORY + "\"");
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("META-INF/services/" + FAST_STRING_SERVICE_FACTORY,
                (RECORDING_FACTORY + "\n").getBytes(StandardCharsets.UTF_8));
        addClassFile(entries, RECORDING_FACTORY);
        addClassFile(entries, RECORDING_SERVICE);
        return osgi.installGenerated("recording.faststring.provider", headers, entries);
    }

    private static void addClassFile(Map<String, byte[]> entries, String className) throws IOException {
        String name = className.replace('.', '/') + ".class";
        try (InputStream in = OsgiModuleTest.class.getClassLoader().getResourceAsStream(name)) {
            entries.put(name, in.readAllBytes());
        }
    }

    private static void assertAttached(Bundle fragment) {
        assertEquals(Bundle.RESOLVED, fragment.getState(),
                fragment.getSymbolicName() + " should attach to the groovy bundle. State: " + stateName(fragment.getState()));
    }

    /** Equivalent of {@code JsonOutput.toJson(new JsonSlurper().parseText(JSON))}, run from the consumer bundle. */
    private static String parseAndSerialize(Bundle consumer) throws Exception {
        return withContextClassLoaderOf(consumer, () -> {
            ClassLoader loader = classLoaderOf(consumer);
            Class<?> slurper = loader.loadClass("groovy.json.JsonSlurper");
            Object parsed = slurper.getMethod("parseText", String.class).invoke(slurper.getConstructor().newInstance(), JSON);
            return (String) loader.loadClass("groovy.json.JsonOutput").getMethod("toJson", Object.class).invoke(null, parsed);
        });
    }
}
