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
package groovy.grape;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Facade to GrapeEngine.
 */
public class Grape {

    public static final String AUTO_DOWNLOAD_SETTING = "autoDownload";
    public static final String DISABLE_CHECKSUMS_SETTING = "disableChecksums";
    public static final String SYSTEM_PROPERTIES_SETTING = "systemProperties";
    private static final URI[] EMPTY_URI_ARRAY = new URI[0];
    private static final Map[] EMPTY_MAP_ARRAY = new Map[0];

    private static boolean enableGrapes = Boolean.valueOf(System.getProperty("groovy.grape.enable", "true"));
    private static boolean enableAutoDownload = Boolean.valueOf(System.getProperty("groovy.grape.autoDownload", "true"));
    private static boolean disableChecksums = Boolean.valueOf(System.getProperty("groovy.grape.disableChecksums", "false"));
    protected static GrapeEngine instance;

    /**
     * This is a static access kill-switch.  All of the static shortcut
     * methods in this class will not work if this property is set to false.
     * By default it is set to true.
     */
    public static boolean getEnableGrapes() {
        return enableGrapes;
    }

    /**
     * This is a static access kill-switch.  All of the static shortcut
     * methods in this class will not work if this property is set to false.
     * By default it is set to true.
     */
    public static void setEnableGrapes(boolean enableGrapes) {
        Grape.enableGrapes = enableGrapes;
    }

    /**
     * This is a static access auto download enabler.  It will set the
     * 'autoDownload' value to the passed in arguments map if not already set.
     * If 'autoDownload' is set the value will not be adjusted.
     * <p>
     * This applies to the grab and resolve calls.
     * <p>
     * If it is set to false, only previously downloaded grapes
     * will be used.  This may cause failure in the grape call
     * if the library has not yet been downloaded
     * <p>
     * If it is set to true, then any jars not already downloaded will
     * automatically be downloaded.  Also, any versions expressed as a range
     * will be checked for new versions and downloaded (with dependencies)
     * if found.
     * <p>
     * By default it is set to true.
     */
    public static boolean getEnableAutoDownload() {
        return enableAutoDownload;
    }

    /**
     * This is a static access auto download enabler.  It will set the
     * 'autoDownload' value to the passed in arguments map if not already
     * set.  If 'autoDownload' is set the value will not be adjusted.
     * <p>
     * This applies to the grab and resolve calls.
     * <p>
     * If it is set to false, only previously downloaded grapes
     * will be used.  This may cause failure in the grape call
     * if the library has not yet been downloaded.
     * <p>
     * If it is set to true, then any jars not already downloaded will
     * automatically be downloaded.  Also, any versions expressed as a range
     * will be checked for new versions and downloaded (with dependencies)
     * if found. By default it is set to true.
     */
    public static void setEnableAutoDownload(boolean enableAutoDownload) {
        Grape.enableAutoDownload = enableAutoDownload;
    }

    /**
     * Global flag to ignore checksums.
     * By default it is set to false.
     */
    public static boolean getDisableChecksums() {
        return disableChecksums;
    }

    /**
     * Set global flag to ignore checksums.
     * By default it is set to false.
     */
    public static void setDisableChecksums(boolean disableChecksums) {
        Grape.disableChecksums = disableChecksums;
    }

    public static synchronized GrapeEngine getInstance() {
        if (instance == null) {
            try {
                // by default use GrapeIvy
                //TODO META-INF/services resolver?
                instance = (GrapeEngine) Class.forName("groovy.grape.GrapeIvy").getDeclaredConstructor().newInstance();
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                //LOGME
            }
        }
        return instance;
    }

    public static void grab(String endorsed) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                instance.grab(endorsed);
            }
        }
    }

    public static void grab(Map<String, Object> dependency) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (!dependency.containsKey(AUTO_DOWNLOAD_SETTING)) {
                    dependency.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
                }
                if (!dependency.containsKey(DISABLE_CHECKSUMS_SETTING)) {
                    dependency.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
                }
                instance.grab(dependency);
            }
        }
    }

    public static void grab(final Map<String, Object> args, final Map... dependencies) {
        if (enableGrapes) {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                GrapeEngine instance = getInstance();
                if (instance != null) {
                    if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
                        args.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
                    }
                    if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
                        args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
                    }
                    if (!args.containsKey(GrapeEngine.CALLEE_DEPTH)) {
                        args.put(GrapeEngine.CALLEE_DEPTH, GrapeEngine.DEFAULT_CALLEE_DEPTH + 2);
                    }
                    instance.grab(args, dependencies);
                }
                return null;
            });
        }
    }

    public static Map<String, Map<String, List<String>>> enumerateGrapes() {
        Map<String, Map<String, List<String>>> grapes = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                grapes = instance.enumerateGrapes();
            }
        }
        if (grapes == null) {
            return Collections.emptyMap();
        } else {
            return grapes;
        }
    }

    public static URI[] resolve(Map<String, Object> args, Map... dependencies) {
        return resolve(args, null, dependencies);
    }

    public static URI[] resolve(Map<String, Object> args, List depsInfo, Map... dependencies) {
        URI[] uris = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
                    args.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
                }
                if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
                    args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
                }
                uris = instance.resolve(args, depsInfo, dependencies);
            }
        }
        if (uris == null) {
            return EMPTY_URI_ARRAY;
        } else {
            return uris;
        }
    }

    public static Map[] listDependencies(ClassLoader cl) {
        Map[] maps = null;
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                maps = instance.listDependencies(cl);
            }
        }
        if (maps == null) {
            return EMPTY_MAP_ARRAY;
        } else {
            return maps;
        }

    }

    public static void addResolver(Map<String, Object> args) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                instance.addResolver(args);
            }
        }
    }
}
