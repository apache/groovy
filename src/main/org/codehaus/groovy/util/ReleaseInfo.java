/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Exposes the Groovy release information 
 * 
 * @author Roshan Dawrani
 */
public class ReleaseInfo {
    private static final Properties releaseInfo = new Properties();
    private static final String RELEASE_INFO_FILE = "META-INF/groovy-release-info.properties";
    private static final String KEY_IMPLEMENTATION_VERSION = "ImplementationVersion";
    private static final String KEY_BUNDLE_VERSION = "BundleVersion";
    private static final String KEY_BUILD_DATE = "BuildDate";
    private static final String KEY_BUILD_TIME = "BuildTime";

    static {
        URL url = null;
        ClassLoader cl = ReleaseInfo.class.getClassLoader();
        // we need no security check for getting the system class
        // loader since if we do the invoker has a null loader,
        // in which case no security check will be done
        if (cl == null) cl = ClassLoader.getSystemClassLoader();
        if (cl instanceof URLClassLoader) {
            // this avoids going through the parent classloaders/bootstarp
            url = ((URLClassLoader) cl).findResource(RELEASE_INFO_FILE);
        } else {
            // fallback option as ClassLoader#findResource() is protected
            url = cl.getResource(RELEASE_INFO_FILE);
        }
        if (url != null) {
            try {
                InputStream is = url.openStream();
                if(is != null) {
                    releaseInfo.load(is);
                }
            } catch(IOException ioex) {
                // ignore. In case of some exception, release info is not available
            }
        }
    }
    
    public static String getVersion() {
        return get(KEY_IMPLEMENTATION_VERSION);
    }
    
    public static Properties getAllProperties() {
        return releaseInfo; 
    }
    
    private static String get(String propName) {
        String propValue = releaseInfo.getProperty(propName);
        return (propValue == null ? "" : propValue);
    }
}
