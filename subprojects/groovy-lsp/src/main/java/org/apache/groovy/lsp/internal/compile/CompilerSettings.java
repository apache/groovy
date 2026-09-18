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
     * Settings without extra plugin keys.
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
