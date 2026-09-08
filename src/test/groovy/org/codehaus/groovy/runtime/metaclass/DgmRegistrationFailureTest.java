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
package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When the default Groovy methods cannot be registered, the registry says so
 * loudly rather than carrying on with an unpredictable subset (GROOVY-12365).
 */
final class DgmRegistrationFailureTest {

    /**
     * A registry whose DGM records are unreadable still constructs, registers
     * no DGM adapters, and reports the problem at SEVERE with the cause.
     */
    @Test
    void unreadableRecordsAreReportedNotSwallowed() throws Exception {
        Path shadow = Files.createTempDirectory("dgminfo-corrupt");
        Path dgminfo = shadow.resolve("META-INF/dgminfo");
        Files.createDirectories(dgminfo.getParent());
        Files.write(dgminfo, new byte[]{0, 9, 'x'}); // a UTF length of 9 followed by one byte: EOFException

        URL classes = DefaultGroovyMethods.class.getProtectionDomain().getCodeSource().getLocation();
        List<LogRecord> logged = new ArrayList<>();
        Handler capture = new Handler() {
            @Override public void publish(LogRecord record) { logged.add(record); }
            @Override public void flush() { }
            @Override public void close() { }
        };
        Logger logger = Logger.getLogger(MetaClassRegistryImpl.class.getName()); // the same JUL logger in every loader
        logger.addHandler(capture);
        try (URLClassLoader loader = new URLClassLoader(new URL[]{shadow.toUri().toURL(), classes}, ClassLoader.getPlatformClassLoader())) {
            Class<?> registryClass = Class.forName(MetaClassRegistryImpl.class.getName(), true, loader);
            Object registry = registryClass.getConstructor().newInstance();

            Object instanceMethods = registryClass.getMethod("getInstanceMethods").invoke(registry); // a FastArray of that loader
            int size = (Integer) instanceMethods.getClass().getMethod("size").invoke(instanceMethods);
            Object[] methods = (Object[]) instanceMethods.getClass().getMethod("getArray").invoke(instanceMethods);
            assertTrue(size > 0, "the plugin methods, registered by reflection, are still there");
            for (int i = 0; i < size; i++) {
                assertFalse(methods[i].getClass().getName().endsWith("GeneratedMetaMethod$Proxy"),
                        "no DGM adapter can have been registered from unreadable records");
            }

            // one report per registry constructed in that loader (GroovySystem's default one included), each SEVERE
            List<LogRecord> severe = logged.stream().filter(r -> r.getLevel() == Level.SEVERE).toList();
            assertFalse(severe.isEmpty(), "the failure must be reported");
            assertEquals(logged.size(), severe.size(), "reported at SEVERE, not as warnings: " + logged);
            for (LogRecord report : severe) {
                assertTrue(report.getMessage().startsWith("No default Groovy methods registered"), report.getMessage());
                assertTrue(report.getThrown() instanceof IOException, String.valueOf(report.getThrown()));
            }
        } finally {
            logger.removeHandler(capture);
        }
    }
}
