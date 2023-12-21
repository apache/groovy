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

import java.util.Map;

/**
 * Factory class to get functionality based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 */
public class VMPluginFactory {

    private static final Map<Integer,String> PLUGIN_MAP = Maps.of(
        // NOTE: Declare the vm plugin entries in *descending* order!
        16, "org.codehaus.groovy.vmplugin.v16.Java16",
        10, "org.codehaus.groovy.vmplugin.v10.Java10"
    );

    private static final VMPlugin PLUGIN = createPlugin();

    private static VMPlugin createPlugin() {
        return doPrivileged(() -> {
            ClassLoader loader = VMPluginFactory.class.getClassLoader();
            int specVer = Runtime.version().feature();
            for (Map.Entry<Integer,String> entry : PLUGIN_MAP.entrySet()) {
                if (specVer >= entry.getKey()) {
                    String fullName = entry.getValue();
                    try {
                        return (VMPlugin) loader.loadClass(fullName).getDeclaredConstructor().newInstance();
                    } catch (Throwable t) {
                        var log = java.util.logging.Logger.getLogger(VMPluginFactory.class.getName());
                        if (log.isLoggable(java.util.logging.Level.FINE)) {
                            log.fine("Trying to create VM plugin `" + fullName + "`, but failed:\n" + DefaultGroovyMethods.asString(t));
                        }
                        return null;
                    }
                }
            }
            return null;
        });
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should perform the operation not as a privileged action
    private static <T> T doPrivileged(java.security.PrivilegedAction<T> action) {
        return java.security.AccessController.doPrivileged(action);
    }

    public static VMPlugin getPlugin() {
        return PLUGIN;
    }
}
