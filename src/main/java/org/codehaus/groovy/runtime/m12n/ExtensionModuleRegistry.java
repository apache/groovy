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
package org.codehaus.groovy.runtime.m12n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The extension method registry is responsible for collecting methods (or static methods) which are added to existing
 * classes and may be called like regular methods.
 * <p>
 * In previous Groovy versions, such methods could only be defined in a single class called {@link org.codehaus.groovy.runtime.DefaultGroovyMethods}
 * for instance methods, and {@link org.codehaus.groovy.runtime.DefaultGroovyStaticMethods} for static methods.
 *
 * @author Cedric Champeau
 * @since 2.0.0
 */
public class ExtensionModuleRegistry {
    private final List<ExtensionModule> modules = new LinkedList<>();

    public ExtensionModuleRegistry() {
    }

    public void addModule(ExtensionModule module) {
        modules.add(module);
    }

    public void removeModule(ExtensionModule module) {
        modules.remove(module);
    }

    public List<ExtensionModule> getModules() {
        return new ArrayList<>(modules);
    }

    public boolean hasModule(final String moduleName) {
        if (null == getModule(moduleName)) {
            return false;
        }

        return true;
    }

    public ExtensionModule getModule(final String moduleName) {
        for (ExtensionModule module : modules) {
            if (module.getName().equals(moduleName)) return module;
        }
        return null;
    }
}
