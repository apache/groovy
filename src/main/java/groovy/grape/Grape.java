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

import org.codehaus.groovy.tools.GrapeUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Facade to GrapeEngine.
 */
public class Grape {

    private static final System.Logger LOGGER = System.getLogger(Grape.class.getName());

    /**
     * Argument key for the auto-download setting.
     */
    public static final String AUTO_DOWNLOAD_SETTING = "autoDownload";
    /**
     * Argument key for the disable-checksums setting.
     */
    public static final String DISABLE_CHECKSUMS_SETTING = "disableChecksums";
    /**
     * Argument key for additional system properties.
     */
    public static final String SYSTEM_PROPERTIES_SETTING = "systemProperties";
    /**
     * System property selecting what happens when a resolver root uses a plaintext protocol:
     * {@code fail} to reject the resolver, {@code warn} (the default) to log a warning and add
     * it anyway, or {@code ignore} to skip the check. An unrecognised value is treated as
     * {@code warn}. Values mirror Maven's checksum-policy vocabulary.
     */
    public static final String INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY = "groovy.grape.insecureProtocolPolicy";
    private static final String INSECURE_PROTOCOL_POLICY_FAIL = "fail";
    private static final String INSECURE_PROTOCOL_POLICY_WARN = "warn";
    private static final String INSECURE_PROTOCOL_POLICY_IGNORE = "ignore";
    private static final String GRAPE_IMPL_SYSTEM_PROPERTY = "groovy.grape.impl";
    private static final String DEFAULT_GRAPE_ENGINE = "groovy.grape.ivy.GrapeIvy";
    private static final URI[] EMPTY_URI_ARRAY = new URI[0];
    private static final Map[] EMPTY_MAP_ARRAY = new Map[0];

    private static boolean enableGrapes = Boolean.parseBoolean(System.getProperty("groovy.grape.enable", "true"));
    private static boolean enableAutoDownload = Boolean.parseBoolean(System.getProperty("groovy.grape.autoDownload", "true"));
    private static boolean disableChecksums = Boolean.parseBoolean(System.getProperty("groovy.grape.disableChecksums", "false"));
    /** Resolver roots already reported as plaintext, so each is warned about only once. */
    private static final Set<String> WARNED_INSECURE_ROOTS = ConcurrentHashMap.newKeySet();
    /** Unrecognised insecure-protocol policy values already reported. */
    private static final Set<String> WARNED_POLICY_VALUES = ConcurrentHashMap.newKeySet();
    /** Dotted-quad IPv4 literal, capturing the first octet. */
    private static final Pattern IPV4_LITERAL = Pattern.compile("(\\d{1,3})(?:\\.\\d{1,3}){3}");
    /**
     * Lazily created grape engine instance.
     */
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

    /**
     * Returns the shared grape engine instance.
     *
     * @return the shared engine, or {@code null} if grapes are unavailable
     */
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

    /**
     * Grabs a dependency expressed as a single string.
     * <p>
     * Recognized forms:
     * <ul>
     *   <li>Maven shorthand: {@code group:module:version[:classifier][@ext]}</li>
     *   <li>Ivy shorthand:   {@code group#module;version}</li>
     *   <li>Endorsed module: a bare module name (legacy; resolves under the
     *       {@code groovy.endorsed} group at the current Groovy version)</li>
     * </ul>
     *
     * @param endorsed the dependency notation
     */
    public static void grab(String endorsed) {
        if (enableGrapes) {
            GrapeEngine instance = getInstance();
            if (instance != null) {
                if (endorsed != null && (endorsed.indexOf(':') >= 0 || endorsed.indexOf('#') >= 0)) {
                    Map<String, Object> parts = GrapeUtil.getIvyParts(endorsed);
                    if (parts.get("group") != null && parts.get("module") != null) {
                        grab(parts);
                        return;
                    }
                }
                instance.grab(endorsed);
            }
        }
    }

    /**
     * Grabs a single dependency.
     *
     * @param dependency the dependency descriptor
     */
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

    /**
     * Grabs one or more dependencies using the supplied arguments.
     *
     * @param args grab arguments
     * @param dependencies dependency descriptors
     */
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

    /**
     * Enumerates locally available grapes.
     *
     * @return grapes grouped by organization and module
     */
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

    /**
     * Resolves dependency coordinates to URIs.
     *
     * @param args resolve arguments
     * @param dependencies dependency descriptors
     * @return the resolved artifact URIs
     */
    public static URI[] resolve(Map<String, Object> args, Map... dependencies) {
        return resolve(args, null, dependencies);
    }

    /**
     * Resolves dependency coordinates to URIs while optionally collecting dependency information.
     *
     * @param args resolve arguments
     * @param depsInfo optional dependency metadata sink
     * @param dependencies dependency descriptors
     * @return the resolved artifact URIs
     */
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

    /**
     * Lists dependencies associated with the supplied class loader.
     *
     * @param cl the class loader to inspect
     * @return the dependency descriptors
     */
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

    /**
     * Adds a resolver to the shared grape engine.
     * <p>
     * A resolver root using a plaintext protocol is subject to
     * {@value #INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY}: {@code warn} (the default) logs a
     * warning and adds the resolver, {@code fail} rejects it, and {@code ignore} skips the
     * check. Roots naming a loopback host are exempt under every policy.
     *
     * @param args the resolver descriptor
     * @throws RuntimeException under the {@code fail} policy, if the root is a plaintext remote root
     */
    public static void addResolver(Map<String, Object> args) {
        if (enableGrapes) {
            checkResolverRootProtocol(args);
            GrapeEngine instance = getInstance();
            if (instance != null) {
                instance.addResolver(args);
            }
        }
    }

    /**
     * Applies {@value #INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY} to a resolver descriptor.
     *
     * @param args the resolver descriptor
     * @throws RuntimeException under the {@code fail} policy, if the root is a plaintext remote root
     */
    // package-private so tests can exercise the policy directly, without mutating the global engine
    static void checkResolverRootProtocol(Map<String, Object> args) {
        if (args == null) {
            return;
        }
        String policy = insecureProtocolPolicy();
        if (INSECURE_PROTOCOL_POLICY_IGNORE.equals(policy)) {
            return;
        }
        Object value = args.get("root");
        if (value == null) value = args.get("value");
        if (!(value instanceof CharSequence)) {
            return;
        }
        String root = value.toString();
        if (!isInsecureResolverRoot(root)) {
            return;
        }
        Object name = args.get("name");
        Object label = name != null ? name : root;
        if (INSECURE_PROTOCOL_POLICY_FAIL.equals(policy)) {
            throw new RuntimeException("Grape resolver '" + label + "' uses the plaintext root '" + root
                    + "' and was rejected because -D" + INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY + "="
                    + INSECURE_PROTOCOL_POLICY_FAIL + " is set. Use an https root, or relax the policy to '"
                    + INSECURE_PROTOCOL_POLICY_WARN + "' or '" + INSECURE_PROTOCOL_POLICY_IGNORE + "'.");
        }
        // Warn once per distinct root; a script may add the same resolver repeatedly. The key is
        // trimmed to match the classifier, so surrounding whitespace does not defeat the de-dup.
        if (WARNED_INSECURE_ROOTS.add(root.trim())) {
            LOGGER.log(WARNING,
                    "Grape resolver ''{0}'' uses the plaintext root ''{1}''; artifacts fetched from it can be"
                            + " read or modified in transit. Prefer https, or set -D{2}={3} to silence this warning.",
                    label, root, INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY, INSECURE_PROTOCOL_POLICY_IGNORE);
        }
    }

    /**
     * Returns the configured insecure-protocol policy, defaulting to {@code warn}. An
     * unrecognised value falls back to {@code warn} rather than to the laxer {@code ignore},
     * so that a typo cannot silently disable the check; the fallback is reported once per
     * offending value.
     *
     * @return one of {@code fail}, {@code warn} or {@code ignore}
     */
    static String insecureProtocolPolicy() {
        String policy = System.getProperty(INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY, INSECURE_PROTOCOL_POLICY_WARN)
                .trim().toLowerCase(Locale.ROOT);
        if (INSECURE_PROTOCOL_POLICY_FAIL.equals(policy)
                || INSECURE_PROTOCOL_POLICY_WARN.equals(policy)
                || INSECURE_PROTOCOL_POLICY_IGNORE.equals(policy)) {
            return policy;
        }
        if (WARNED_POLICY_VALUES.add(policy)) {
            LOGGER.log(WARNING, "Unrecognised -D{0} value ''{1}''; using ''{2}''. Expected one of {3}, {4}, {5}.",
                    INSECURE_PROTOCOL_POLICY_SYSTEM_PROPERTY, policy, INSECURE_PROTOCOL_POLICY_WARN,
                    INSECURE_PROTOCOL_POLICY_FAIL, INSECURE_PROTOCOL_POLICY_WARN, INSECURE_PROTOCOL_POLICY_IGNORE);
        }
        return INSECURE_PROTOCOL_POLICY_WARN;
    }

    /**
     * Returns whether the given resolver root fetches over a plaintext protocol from a host
     * other than loopback.
     * <p>
     * Only schemes known to be plaintext are classified as insecure, currently {@code http}
     * and {@code ftp}. This is deliberately an allow-list of bad schemes rather than a
     * deny-list of good ones: transports such as {@code s3} and {@code gs} are encrypted in
     * practice and would otherwise be reported falsely. The consequence is that an exotic
     * plaintext scheme is not reported, so {@code fail} means "reject known-plaintext roots",
     * not "reject anything not proven safe".
     * <p>
     * {@code file:} roots are never insecure. They cross no network, and a {@code file:} root
     * on a network mount cannot be distinguished from a local one by inspecting the URI.
     * Integrity for such repositories is the job of checksum verification, which applies to
     * every transport rather than only to remote ones. Roots which are not valid URIs, or
     * which name no scheme at all, are likewise left to the engine.
     *
     * @param root the resolver root
     * @return true if the root is a plaintext remote root
     */
    static boolean isInsecureResolverRoot(String root) {
        if (root == null) {
            return false;
        }
        String scheme;
        String host;
        try {
            URI uri = new URI(root.trim());
            scheme = uri.getScheme();
            host = uri.getHost();
        } catch (URISyntaxException e) {
            return false; // not a URI we can reason about; leave it to the engine
        }
        if (scheme == null) {
            return false;
        }
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"ftp".equals(scheme)) {
            return false;
        }
        return !isLoopbackHost(host);
    }

    private static boolean isLoopbackHost(String host) {
        if (host == null) {
            return false;
        }
        String name = host.toLowerCase(Locale.ROOT);
        if (name.startsWith("[") && name.endsWith("]")) { // IPv6 literal
            name = name.substring(1, name.length() - 1);
        }
        if ("localhost".equals(name) || "::1".equals(name)) {
            return true;
        }
        // 127.0.0.0/8, matched as a dotted quad so that a host merely beginning with "127."
        // (such as 127.example.com) is not mistaken for a loopback address. An out-of-range quad
        // such as 127.999.999.999 cannot reach here: URI.getHost() returns null for it, so
        // isInsecureResolverRoot already treats it as insecure without consulting this method.
        Matcher ipv4 = IPV4_LITERAL.matcher(name);
        return ipv4.matches() && "127".equals(ipv4.group(1));
    }

}
