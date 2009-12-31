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
    private static Properties releaseInfo = new Properties();
    private static String RELEASE_INFO_FILE = "META-INF/groovy-release-info.properties";
    private static String KEY_IMPLEMENTATION_VERSION = "ImplementationVersion";
    private static String KEY_BUNDLE_VERSION = "BundleVersion";
    private static String KEY_BUILD_DATE = "BuildDate";
    private static String KEY_BUILD_TIME = "BuildTime";
    private static boolean loaded = false;

    public static String getVersion() {
        return get(KEY_IMPLEMENTATION_VERSION);
    }
    
    public static Properties getAllProperties() {
        loadInfo();
        return releaseInfo; 
    }
    
    private static String get(String propName) {
        loadInfo();
        String propValue = releaseInfo.getProperty(propName);
        return (propValue == null ? "" : propValue);
    }
    
    private static void loadInfo() {
        if(!loaded) {
            URL url = null;
            ClassLoader cl = ReleaseInfo.class.getClassLoader();
            if(cl instanceof URLClassLoader) {
                // this avoids going through the parent classloaders/bootstarp
                url = ((URLClassLoader) cl).findResource(RELEASE_INFO_FILE);
            } else {
                // fallback option as ClassLoader#findResource() is protected
                url = cl.getResource(RELEASE_INFO_FILE);
            }
            try {
                InputStream is = url.openStream();
                if(is != null) {
                    releaseInfo.load(is);
                }
            } catch(IOException ioex) {
                // ignore. In case of some exception, release info is not available
            }
            loaded = true;
        }
    }
}
