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

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Facade to GrapeEngine.
 */
public class Grape {

    private static final System.Logger LOGGER = System.getLogger(Grape.class.getName());

    public static final String AUTO_DOWNLOAD_SETTING = "autoDownload";
    public static final String DISABLE_CHECKSUMS_SETTING = "disableChecksums";
    public static final String SYSTEM_PROPERTIES_SETTING = "systemProperties";
    private static final String GRAPE_IMPL_SYSTEM_PROPERTY = "groovy.grape.impl";
    private static final String DEFAULT_GRAPE_ENGINE = "groovy.grape.ivy.GrapeIvy";
    private static final URI[] EMPTY_URI_ARRAY = new URI[0];
    private static final Map[] EMPTY_MAP_ARRAY = new Map[0];

    private static boolean enableGrapes = Boolean.parseBoolean(System.getProperty("groovy.grape.enable", "true"));
    private static boolean enableAutoDownload = Boolean.parseBoolean(System.getProperty("groovy.grape.autoDownload", "true"));
    private static boolean disableChecksums = Boolean.parseBoolean(System.getProperty("groovy.grape.disableChecksums", "false"));
    protected static GrapeEngine instance;

    /**
     * This is a static access kill-switch.  All the static shortcut
     * methods in this class will be disabled if this property is set to false.
     * By default, it is set to true.
     */
    public static boolean getEnableGrapes() {
        return enableGrapes;
    }

    /**
     * This is a static access kill-switch.  All the static shortcut
     * methods in this class will be disabled if this property is set to false.
     * By default, it is set to true.
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
     * By default, it is set to true.
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
     * if found. By default, it is set to true.
     */
    public static void setEnableAutoDownload(boolean enableAutoDownload) {
        Grape.enableAutoDownload = enableAutoDownload;
    }

    /**
     * Global flag to ignore checksums.
     * By default, it is set to false.
     */
    public static boolean getDisableChecksums() {
        return disableChecksums;
    }

    /**
     * Set global flag to ignore checksums.
     * By default, it is set to false.
     */
    public static void setDisableChecksums(boolean disableChecksums) {
        Grape.disableChecksums = disableChecksums;
    }

    public static synchronized GrapeEngine getInstance() {
        if (instance == null) {
            String configuredImpl = System.getProperty(GRAPE_IMPL_SYSTEM_PROPERTY);
            ServiceLoader.Provider<GrapeEngine> provider = findProvider(configuredImpl);
            if (provider != null) {
                instance = createEngineFromProvider(provider);
            }
            if (instance == null) {
                LOGGER.log(WARNING, "Grapes disabled");
            }
        }
        return instance;
    }

    private static ServiceLoader.Provider<GrapeEngine> findProvider(final String configuredImpl) {
        List<ServiceLoader.Provider<GrapeEngine>> providers;
        try {
            ClassLoader grapeClassLoader = Grape.class.getClassLoader();
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            // Keep deterministic order while avoiding duplicate provider types when both
            // class loaders expose the same service entry.
            Map<String, ServiceLoader.Provider<GrapeEngine>> discovered = new LinkedHashMap<>();
            ServiceLoader.load(GrapeEngine.class, grapeClassLoader).stream()
                    .forEach(p -> discovered.putIfAbsent(p.type().getName(), p));
            if (contextClassLoader != null && contextClassLoader != grapeClassLoader) {
                ServiceLoader.load(GrapeEngine.class, contextClassLoader).stream()
                        .forEach(p -> discovered.putIfAbsent(p.type().getName(), p));
            }
            providers = discovered.values().stream().toList();
        } catch (ServiceConfigurationError sce) {
            LOGGER.log(ERROR, "Failed to discover service providers for {0}: {1}", GrapeEngine.class.getName(), sce.getMessage());
            return null;
        }

        if (configuredImpl != null) {
            for (ServiceLoader.Provider<GrapeEngine> provider : providers) {
                if (provider.type().getName().equals(configuredImpl)) {
                    providers.stream()
                            .filter(p -> !p.type().getName().equals(configuredImpl))
                            .forEach(p -> LOGGER.log(DEBUG, "Ignoring provider ''{0}'' (''{1}'' configured via -D{2})",
                                    p.type().getName(), configuredImpl, GRAPE_IMPL_SYSTEM_PROPERTY));
                    return provider;
                }
            }
            LOGGER.log(WARNING, "Configured implementation ''{0}'' not found via service loader", configuredImpl);
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        if (providers.size() > 1) {
            for (ServiceLoader.Provider<GrapeEngine> provider : providers) {
                if (provider.type().getName().equals(DEFAULT_GRAPE_ENGINE)) {
                    providers.stream()
                            .filter(p -> !p.type().getName().equals(DEFAULT_GRAPE_ENGINE))
                            .forEach(p -> LOGGER.log(DEBUG, "Ignoring provider ''{0}'' in favour of default ''{1}'' (set -D{2} to override)",
                                    p.type().getName(), DEFAULT_GRAPE_ENGINE, GRAPE_IMPL_SYSTEM_PROPERTY));
                    return provider;
                }
            }
            // Multiple providers discovered but the default is not among them.
            List<String> names = providers.stream().map(p -> p.type().getName()).toList();
            LOGGER.log(WARNING, "{0} providers discovered {1} but default ''{2}'' is not among them; set -D{3} to select one",
                    providers.size(), names, DEFAULT_GRAPE_ENGINE, GRAPE_IMPL_SYSTEM_PROPERTY);
        }

        // No system property set: empty list means security lockdown — return null silently.
        return null;
    }

    private static GrapeEngine createEngineFromProvider(final ServiceLoader.Provider<GrapeEngine> provider) {
        try {
            return provider.get();
        } catch (ServiceConfigurationError sce) {
            LOGGER.log(ERROR, "Failed to instantiate service provider ''{0}'': {1}", provider.type().getName(), sce.getMessage());
            return null;
        }
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
            GrapeEngine instance1 = getInstance();
            if (instance1 != null) {
                if (!args.containsKey(AUTO_DOWNLOAD_SETTING)) {
                    args.put(AUTO_DOWNLOAD_SETTING, enableAutoDownload);
                }
                if (!args.containsKey(DISABLE_CHECKSUMS_SETTING)) {
                    args.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
                }
                if (!args.containsKey(GrapeEngine.CALLEE_DEPTH)) {
                    args.put(GrapeEngine.CALLEE_DEPTH, GrapeEngine.DEFAULT_CALLEE_DEPTH + 2);
                }
                instance1.grab(args, dependencies);
            }
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
        }
        return grapes;
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
        }
        return uris;
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
        }
        return maps;
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
