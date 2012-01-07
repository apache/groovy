/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.tools.groovydoc;

/**
 * Represents a link pair (href, packages).
 * The packages are comma separated.
 */
public class LinkArgument {
    private String href = "";
    private String packages = "";

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
     * @param packages the comma separated package prefixs corresponding to this link
     */
    public void setPackages(String packages) {
        this.packages = packages;
    }

    /**
     * Get the href attribute.
     *
     * @return the href attribute.
     */
    public String getHref() {
        return href;
    }

    /**
     * Set the href attribute.
     *
     * @param hr a <code>String</code> value representing the URL to use for this link
     */
    public void setHref(String hr) {
        href = hr;
    }

}