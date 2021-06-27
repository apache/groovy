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

import java.math.BigDecimal;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.isAtLeast;

/**
 * Factory class to get functionality based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 */
public class VMPluginFactory {
    private static final Logger LOGGER = Logger.getLogger(VMPluginFactory.class.getName());
    private static final Map<BigDecimal, String> PLUGIN_MAP = Maps.of(
            // Note: list the vm plugin entries in *descending* order:
            new BigDecimal("16"), "org.codehaus.groovy.vmplugin.v16.Java16",
            new BigDecimal("10"), "org.codehaus.groovy.vmplugin.v10.Java10",
            new BigDecimal("9"), "org.codehaus.groovy.vmplugin.v9.Java9",
            new BigDecimal("1.8"), "org.codehaus.groovy.vmplugin.v8.Java8"
    );

    private static final VMPlugin PLUGIN;

    static {
        PLUGIN = createPlugin();
    }

    public static VMPlugin getPlugin() {
        return PLUGIN;
    }

    private static VMPlugin createPlugin() {
        return AccessController.doPrivileged((PrivilegedAction<VMPlugin>) () -> {
            final BigDecimal specVer = new BigDecimal(System.getProperty("java.specification.version"));
            ClassLoader loader = VMPluginFactory.class.getClassLoader();
            for (Map.Entry<BigDecimal, String> entry : PLUGIN_MAP.entrySet()) {
                if (isAtLeast(specVer, entry.getKey())) {
                    final String pluginName = entry.getValue();
                    try {
                        return (VMPlugin) loader.loadClass(pluginName).getDeclaredConstructor().newInstance();
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Trying to create VM plugin `" + pluginName + "`, but failed:\n" + DefaultGroovyMethods.asString(t)
                            );
                        }

                        return null;
                    }
                }
            }

            return null;
        });
    }
}
