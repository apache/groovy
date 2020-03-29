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

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class to get functionality based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 */
public class VMPluginFactory {
    private static final Logger LOGGER = Logger.getLogger(VMPluginFactory.class.getName());

    private static final String JDK8_CLASSNAME_CHECK = "java.util.Optional";
    private static final String JDK9_CLASSNAME_CHECK = "java.lang.Module";

    private static final String JDK8_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v8.Java8";
    private static final String JDK9_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v9.Java9";

    private static final VMPlugin PLUGIN;

    static {
        VMPlugin target = createPlugin(JDK9_CLASSNAME_CHECK, JDK9_PLUGIN_NAME);
        if (target == null) {
            target = createPlugin(JDK8_CLASSNAME_CHECK, JDK8_PLUGIN_NAME);
        }

        PLUGIN = target;
    }

    public static VMPlugin getPlugin() {
        return PLUGIN;
    }

    private static VMPlugin createPlugin(final String classNameCheck, final String pluginName) {
        return AccessController.doPrivileged((PrivilegedAction<VMPlugin>) () -> {
            try {
                ClassLoader loader = VMPluginFactory.class.getClassLoader();
                loader.loadClass(classNameCheck);
                return (VMPlugin) loader.loadClass(pluginName).getDeclaredConstructor().newInstance();
            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Trying to create VM plugin `" + pluginName + "` by checking `" + classNameCheck
                            + "`, but failed:\n" + DefaultGroovyMethods.asString(t)
                    );
                }

                return null;
            }
        });
    }
}
