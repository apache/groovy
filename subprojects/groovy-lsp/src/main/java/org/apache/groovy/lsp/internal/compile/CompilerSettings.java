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
package org.apache.groovy.lsp.internal.compile;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Compilation options for the language server. Grape and {@code @ASTTest}
 * are off by default so opening a workspace does not execute or download
 * untrusted compile-time code.
 */
public record CompilerSettings(List<String> classpath, List<String> sourcePaths, int throughPhase,
                               boolean grapeEnabled, boolean astTestEnabled, Map<String, String> extra) {

    private static final String KEY_CLASSPATH = "classpath";
    private static final String KEY_SOURCE_PATHS = "sourcePaths";
    private static final String KEY_GRAPE_ENABLED = "grapeEnabled";
    private static final String KEY_AST_TEST_ENABLED = "astTestEnabled";
    private static final Set<String> CLIENT_KEYS = Set.of(
            KEY_CLASSPATH, KEY_SOURCE_PATHS, KEY_GRAPE_ENABLED, KEY_AST_TEST_ENABLED);

    /**
     * Creates settings with the supplied classpath and source paths.
     *
     * @param classpath jar and directory entries
     * @param sourcePaths extra Groovy source roots
     * @param throughPhase {@link Phases} constant to compile through
     * @param grapeEnabled whether {@code @Grab} runs
     * @param astTestEnabled whether {@code @ASTTest} closures run
     * @param extra other {@code groovy.*} keys the core does not interpret
     */
    public CompilerSettings {
        classpath = classpath == null ? List.of() : List.copyOf(classpath);
        sourcePaths = sourcePaths == null ? List.of() : List.copyOf(sourcePaths);
        extra = extra == null || extra.isEmpty() ? Map.of() : Map.copyOf(extra);
    }

    /**
     * Settings without extra {@code groovy.*} keys.
     *
     * @param classpath jar and directory entries
     * @param sourcePaths extra Groovy source roots
     * @param throughPhase {@link Phases} constant to compile through
     * @param grapeEnabled whether {@code @Grab} runs
     * @param astTestEnabled whether {@code @ASTTest} closures run
     */
    public CompilerSettings(final List<String> classpath, final List<String> sourcePaths, final int throughPhase,
                            final boolean grapeEnabled, final boolean astTestEnabled) {
        this(classpath, sourcePaths, throughPhase, grapeEnabled, astTestEnabled, Map.of());
    }

    /**
     * @return defaults: semantic analysis, no Grape, no {@code @ASTTest}
     */
    public static CompilerSettings defaults() {
        return new CompilerSettings(List.of(), List.of(), Phases.SEMANTIC_ANALYSIS, false, false, Map.of());
    }

    public List<String> getClasspath() {
        return classpath;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public int getThroughPhase() {
        return throughPhase;
    }

    public boolean isGrapeEnabled() {
        return grapeEnabled;
    }

    public boolean isAstTestEnabled() {
        return astTestEnabled;
    }

    /**
     * @return extra {@code groovy.*} settings, never {@code null}
     */
    public Map<String, String> getExtra() {
        return extra;
    }

    /**
     * Merges a client {@code groovy} settings map into {@code current}.
     * Missing keys keep the current value. An empty {@code classpath} or
     * {@code sourcePaths} list also keeps the current list, matching
     * {@code workspace/didChangeConfiguration}.
     *
     * @param map client map, possibly {@code null}
     * @param current settings to merge into, possibly {@code null}
     * @return merged settings, never {@code null}
     */
    public static CompilerSettings fromClientMap(final Map<?, ?> map, final CompilerSettings current) {
        CompilerSettings base = current == null ? defaults() : current;
        if (map == null || map.isEmpty()) {
            return base;
        }
        List<String> classpath = stringList(map.get(KEY_CLASSPATH));
        List<String> sourcePaths = stringList(map.get(KEY_SOURCE_PATHS));
        boolean grape = map.containsKey(KEY_GRAPE_ENABLED)
                ? Boolean.TRUE.equals(map.get(KEY_GRAPE_ENABLED)) : base.isGrapeEnabled();
        boolean astTest = map.containsKey(KEY_AST_TEST_ENABLED)
                ? Boolean.TRUE.equals(map.get(KEY_AST_TEST_ENABLED)) : base.isAstTestEnabled();
        return new CompilerSettings(
                classpath.isEmpty() ? base.getClasspath() : classpath,
                sourcePaths.isEmpty() ? base.getSourcePaths() : sourcePaths,
                base.getThroughPhase(), grape, astTest, extraFrom(map, base));
    }

    private static Map<String, String> extraFrom(final Map<?, ?> map, final CompilerSettings current) {
        Map<String, String> extra = new LinkedHashMap<>(current.getExtra());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                String key = entry.getKey().toString();
                if (!CLIENT_KEYS.contains(key)) {
                    extra.put(key, extraValue(entry.getValue()));
                }
            }
        }
        return extra;
    }

    private static String extraValue(final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List<?> list) {
            return stringList(list).toString();
        }
        return value.toString();
    }

    private static List<String> stringList(final Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }

    /**
     * Builds a {@link CompilerConfiguration} for a tooling compile.
     *
     * @return the configuration
     */
    public CompilerConfiguration toConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTolerance(0);
        configuration.setTargetBytecode(CompilerConfiguration.JDK17);
        if (!classpath.isEmpty()) {
            configuration.setClasspathList(new ArrayList<>(classpath));
        }
        Set<String> disabled = new LinkedHashSet<>();
        if (configuration.getDisabledGlobalASTTransformations() != null) {
            disabled.addAll(configuration.getDisabledGlobalASTTransformations());
        }
        if (!grapeEnabled) {
            disabled.add("groovy.grape.GrabAnnotationTransformation");
        }
        if (!astTestEnabled) {
            disabled.add("org.codehaus.groovy.transform.ASTTestTransformation");
        }
        configuration.setDisabledGlobalASTTransformations(disabled);
        configuration.getOptimizationOptions().put(CompilerConfiguration.GROOVYDOC, Boolean.TRUE);
        return configuration;
    }

    /**
     * Creates a class loader covering the configured classpath.
     *
     * @param parent parent loader
     * @return a loader
     */
    public URLClassLoader createClassLoader(final ClassLoader parent) {
        List<URL> urls = new ArrayList<>();
        for (String entry : classpath) {
            try {
                Path path = Path.of(entry);
                File file = path.toFile();
                if (file.exists()) {
                    urls.add(file.toURI().toURL());
                }
            } catch (MalformedURLException ignored) {
                // skip unusable entries
            }
        }
        return new URLClassLoader(urls.toArray(URL[]::new), parent);
    }
}
