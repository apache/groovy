package org.codehaus.groovy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Exposes the Groovy release information 
 * 
 * @author Roshan Dawrani
 */
public class ReleaseInfo {
    private static Properties releaseInfo = new Properties();
    private static String RELEASE_INFO_FILE = "META-INF/release-info.properties";
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
            InputStream is = ReleaseInfo.class.getClassLoader().getResourceAsStream(RELEASE_INFO_FILE);
            if(is != null) {
                try {
                    releaseInfo.load(is);
                } catch(IOException ioex) {
                    // ignore. In case of some exception, release info is not available
                }
            }
            loaded = true;
        }
    }
}
