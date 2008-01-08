/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.vmplugin;

import org.codehaus.groovy.vmplugin.v4.Java4;

/**
 * factory class to get functionlity based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 * @author Jochen Theodorou
 */
public class VMPluginFactory {
    
    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";
    private static final String JDK5_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v5.Java5";
    private static VMPlugin plugin;
    static {
        try {
            ClassLoader.getSystemClassLoader().loadClass(JDK5_CLASSNAME_CHECK);
            plugin = (VMPlugin) VMPluginFactory.class.getClassLoader()
                                 .loadClass(JDK5_PLUGIN_NAME).newInstance();
        } catch(Exception ex) {
            plugin = new Java4();
        }
    }
    
    public static VMPlugin getPlugin() {
        return plugin;
    }
    
}
