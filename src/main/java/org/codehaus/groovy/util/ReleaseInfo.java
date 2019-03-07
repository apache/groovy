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
package org.codehaus.groovy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly;

/**
 * Exposes the Groovy release information 
 */
public class ReleaseInfo {
    private static final Properties RELEASE_INFO = new Properties();
    private static final String RELEASE_INFO_FILE = "META-INF/groovy-release-info.properties";
    private static final String KEY_IMPLEMENTATION_VERSION = "ImplementationVersion";

    static {
        URL url = null;
        ClassLoader cl = ReleaseInfo.class.getClassLoader();
        // we need no security check for getting the system class
        // loader since if we do the invoker has a null loader,
        // in which case no security check will be done
        if (cl == null) cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            // this avoids going through the parent classloaders/bootstrap
            url = ((URLClassLoader) cl).findResource(RELEASE_INFO_FILE);
        } else {
            // fallback option as ClassLoader#findResource() is protected
            url = cl.getResource(RELEASE_INFO_FILE);
        }
        if (url != null) {
            InputStream is = null;
            try {
                is = URLStreams.openUncachedStream(url);
                if (is != null) {
                    RELEASE_INFO.load(is);
                }
            } catch (IOException ioex) {
                // ignore. In case of some exception, release info is not available
            } finally {
                closeQuietly(is);
            }
        }
    }
    
    public static String getVersion() {
        return get(KEY_IMPLEMENTATION_VERSION);
    }
    
    public static Properties getAllProperties() {
        return RELEASE_INFO;
    }
    
    private static String get(String propName) {
        String propValue = RELEASE_INFO.getProperty(propName);
        return (propValue == null ? "" : propValue);
    }
}
