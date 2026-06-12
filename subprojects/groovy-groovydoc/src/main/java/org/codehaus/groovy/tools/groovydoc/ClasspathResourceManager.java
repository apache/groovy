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

import org.codehaus.groovy.runtime.IOGroovyMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Loads documentation resources from the classpath.
 */
public class ClasspathResourceManager implements ResourceManager {
    /**
     * Class loader used to resolve resources.
     */
    ClassLoader classLoader;
    /**
     * Creates a resource manager backed by this class's class loader.
     */
    public ClasspathResourceManager() {
        classLoader = getClass().getClassLoader();
    }

    /**
     * Creates a resource manager backed by the supplied class loader.
     *
     * @param classLoader the class loader to use for resource lookups
     */
    public ClasspathResourceManager(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Opens the named resource as an input stream.
     *
     * @param resourceName the classpath resource to load
     * @return the resource stream, or {@code null} if the resource does not exist
     */
    public InputStream getInputStream(String resourceName) {
        return classLoader.getResourceAsStream(resourceName);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getReader(String resourceName) throws IOException {
        return IOGroovyMethods.newReader(getInputStream(resourceName));
    }

}
