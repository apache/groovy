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
package org.codehaus.groovy.control;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.util.URLStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Looks for source file extensions in META-INF/services/org.codehaus.groovy.source.Extensions
 */
public class SourceExtensionHandler {

    public static Set<String> getRegisteredExtensions(ClassLoader loader) {
        Set<String> extensions = new LinkedHashSet<>();
        extensions.add("groovy");
        try {
            Enumeration<URL> globalServices = loader.getResources("META-INF/groovy/org.codehaus.groovy.source.Extensions");
            if (!globalServices.hasMoreElements()) {
                globalServices = loader.getResources("META-INF/services/org.codehaus.groovy.source.Extensions");
            }
            while (globalServices.hasMoreElements()) {
                URL service = globalServices.nextElement();
                try (BufferedReader svcIn = new BufferedReader(new InputStreamReader(URLStreams.openUncachedStream(service)))) {
                    String extension = svcIn.readLine();
                    while (extension != null) {
                        extension = extension.trim();
                        if (!extension.startsWith("#") && extension.length() > 0) {
                            extensions.add(extension);
                        }
                        extension = svcIn.readLine();
                    }
                } catch (IOException ex) {
                    throw new GroovyRuntimeException("IO Exception attempting to load registered source extension " +
                            service.toExternalForm() + ". Exception: " + ex.toString());
                }
            }
        } catch (IOException ex) {
            throw new GroovyRuntimeException("IO Exception getting registered source extensions. Exception: " + ex.toString());
        }
        return extensions;
    }
}
