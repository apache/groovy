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
package org.codehaus.groovy.vmplugin;

import org.apache.groovy.util.Maps;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.vmplugin.v17.Java17;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class to get functionality based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 */
public class VMPluginFactory {

    // Constructor references rather than class names: the plugin is then
    // reachable by static analysis (GraalVM native image needs no reflection
    // metadata for it) and its class loads only when the JDK version selects it.
    private static final Map<Integer, Supplier<VMPlugin>> PLUGIN_MAP = Maps.of(
        17, Java17::new
    );

    private static final VMPlugin PLUGIN = createPlugin();

    private static VMPlugin createPlugin() {
        int specVer = Runtime.version().feature();
        for (Map.Entry<Integer, Supplier<VMPlugin>> entry : PLUGIN_MAP.entrySet()) {
            if (specVer >= entry.getKey()) {
                try {
                    return entry.getValue().get();
                } catch (Throwable t) {
                    var log = Logger.getLogger(VMPluginFactory.class.getName());
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("Trying to create VM plugin for Java " + entry.getKey() + ", but failed:\n" + DefaultGroovyMethods.asString(t));
                    }
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns the VM plugin selected for the current runtime.
     *
     * @return the active VM plugin, or {@code null} if initialization failed
     */
    public static VMPlugin getPlugin() {
        return PLUGIN;
    }
}
