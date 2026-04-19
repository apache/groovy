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
package org.codehaus.groovy.tools.groovydoc;

/**
 * Represents a link pair (href, packages) with optional JPMS module.
 * The packages are comma separated. When a module is specified, the
 * module name is inserted between the base href and the package path,
 * matching the JPMS-era Javadoc layout (e.g. {@code .../api/java.base/java/lang/Object.html}).
 */
public class LinkArgument {
    private String href = "";
    private String packages = "";
    private String module = "";

    /**
     * Get the packages attribute.
     *
     * @return the packages attribute.
     */
    public String getPackages() {
        return packages;
    }

    /**
     * Set the packages attribute.
     *
     * @param packages the comma separated package prefixes corresponding to this link
     */
    public void setPackages(String packages) {
        this.packages = packages;
    }

    /**
     * Get the href attribute, with the module name appended when set.
     * For pre-JPMS external docs, returns the raw href; for JPMS-aware
     * external docs (e.g. JDK 9+), returns {@code href + module + "/"}.
     *
     * @return the effective base URL for this link
     */
    public String getHref() {
        if (module == null || module.isEmpty()) return href;
        String base = href.endsWith("/") ? href : href + "/";
        return base + module + "/";
    }

    /**
     * Set the href attribute.
     *
     * @param hr a <code>String</code> value representing the URL to use for this link
     */
    public void setHref(String hr) {
        href = hr;
    }

    /**
     * Get the module attribute.
     *
     * @return the module attribute, or an empty string if not set
     */
    public String getModule() {
        return module;
    }

    /**
     * Set the module attribute. When set, the module name is inserted
     * between the base href and the package path, yielding JPMS-era
     * Javadoc URLs such as {@code .../api/java.base/java/lang/Object.html}.
     *
     * @param module the JPMS module name (e.g. {@code java.base})
     */
    public void setModule(String module) {
        this.module = module;
    }

}
