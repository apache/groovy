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

import groovy.lang.MetaMethod;

import java.util.List;

/**
 * An extension module is a class responsible for providing a list of {@link MetaMethod meta methods} to the Groovy
 * compiler and runtime. Those methods are used to "dynamically extend" existing classes by adding methods to
 * existing classes.
 *
 * @since 2.0.0
 */
public abstract class ExtensionModule {
    private final String name;
    private final String version;

    /**
     * Constructs a new ExtensionModule with the specified name and version.
     *
     * @param moduleName the name of this extension module
     * @param moduleVersion the version of this extension module
     */
    public ExtensionModule(final String moduleName, final String moduleVersion) {
        this.name = moduleName;
        this.version = moduleVersion;
    }

    /**
     * Returns the name of this extension module.
     *
     * @return the module name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of this extension module.
     *
     * @return the module version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the list of metamethods provided by this extension module.
     * These methods are added to existing Groovy classes to extend their functionality.
     *
     * @return a list of metamethods provided by this module
     */
    public abstract List<MetaMethod> getMetaMethods();

    /**
     * Returns a string representation of this extension module.
     *
     * @return a string representation including the module name and version
     */
    @Override
    public String toString() {
        String sb = "ExtensionModule{" + "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
        return sb;
    }
}
