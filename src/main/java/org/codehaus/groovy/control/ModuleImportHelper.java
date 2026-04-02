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
package org.codehaus.groovy.control;

import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared utilities for expanding JPMS module imports into package-level
 * star imports. Used by both the parser ({@code AstBuilder}) and the
 * {@link org.codehaus.groovy.control.customizers.ImportCustomizer}.
 *
 * @since 6.0.0
 */
public final class ModuleImportHelper {

    private ModuleImportHelper() { }

    /**
     * Builds a {@link ModuleFinder} that combines the system module finder
     * with one scanning the compilation classpath for modular JARs.
     *
     * @param source the source unit providing classpath and classloader
     * @return a composite module finder
     */
    public static ModuleFinder moduleFinder(final SourceUnit source) {
        return ModuleFinder.compose(ModuleFinder.ofSystem(), classpathModuleFinder(source));
    }

    /**
     * Collects the exported package names from the given module and,
     * recursively, from any modules it {@code requires transitive}.
     * This implements the transitive readability semantics specified
     * by JEP 476.
     *
     * @param moduleRef the module to inspect
     * @param finder    the finder used to resolve transitive dependencies
     * @param packageNames accumulator for discovered package names
     * @param visited   set of already-visited module names (to avoid cycles)
     */
    public static void collectModuleExports(final ModuleReference moduleRef, final ModuleFinder finder,
                                            final List<String> packageNames, final Set<String> visited) {
        ModuleDescriptor descriptor = moduleRef.descriptor();
        if (!visited.add(descriptor.name())) return;
        // Automatic modules have no exports — use packages() instead
        if (descriptor.isAutomatic()) {
            packageNames.addAll(descriptor.packages());
        } else {
            descriptor.exports().stream()
                    .filter(e -> !e.isQualified())
                    .map(ModuleDescriptor.Exports::source)
                    .forEach(packageNames::add);
        }
        // Recursively process transitive dependencies (JEP 476)
        for (ModuleDescriptor.Requires req : descriptor.requires()) {
            if (req.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE)) {
                finder.find(req.name()).ifPresent(ref ->
                        collectModuleExports(ref, finder, packageNames, visited));
            }
        }
    }

    /**
     * Resolves a module by name and collects its exported packages
     * (including transitive dependencies).
     *
     * @param moduleName the JPMS module name
     * @param finder     the module finder to use
     * @return the list of exported package names
     * @throws IllegalArgumentException if the module is not found
     */
    public static List<String> resolveModulePackages(final String moduleName, final ModuleFinder finder) {
        ModuleReference moduleRef = finder.find(moduleName).orElse(null);
        if (moduleRef == null) {
            throw new IllegalArgumentException("Unknown module: " + moduleName);
        }
        List<String> packageNames = new ArrayList<>();
        collectModuleExports(moduleRef, finder, packageNames, new HashSet<>());
        return packageNames;
    }

    /**
     * Builds a {@link ModuleFinder} that scans the compilation classpath
     * for modular JARs (those containing {@code module-info.class}).
     * Collects paths from both the compiler configuration classpath and
     * any URLClassLoaders in the classloader hierarchy.
     */
    private static ModuleFinder classpathModuleFinder(final SourceUnit source) {
        Set<Path> paths = new LinkedHashSet<>();
        for (String entry : source.getConfiguration().getClasspath()) {
            Path p = Path.of(entry);
            if (Files.exists(p)) {
                paths.add(p);
            }
        }
        ClassLoader cl = source.getClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    try {
                        Path p = Path.of(url.toURI());
                        if (Files.exists(p)) {
                            paths.add(p);
                        }
                    } catch (URISyntaxException ignore) {
                    }
                }
            }
            cl = cl.getParent();
        }
        try {
            return ModuleFinder.of(paths.toArray(Path[]::new));
        } catch (FindException e) {
            return ModuleFinder.of();
        }
    }
}
