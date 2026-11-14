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
package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.Objects;

/**
 * Publishes in-memory joint-compilation output from one compilation so a
 * later one can resolve it.
 * <p>
 * {@code groovyc} and the Ant task already build a new
 * {@link GroovyClassLoader} for each run, so a single
 * {@link JavaAwareCompilationUnit} on that loader is enough for them. Ant's
 * {@code includeDestClasses} keeps earlier class files visible by putting the
 * destination directory on the classpath; a later run still uses a new loader,
 * and a class file that run does not write again stays in the directory.
 * {@code parseClass} keeps a long-lived loader for resources and defines each
 * parse on a new {@link GroovyClassLoader.InnerLoader}. The language server
 * reuses one loader while its settings are unchanged and discards the previous
 * javac output directory, so overlapping names are a new snapshot rather than
 * a merge. None of those hosts turns {@link CompilerConfiguration#MEM_CLASS_OUTPUT}
 * on for a series of units. This type is the embedder-facing form of the same
 * split.
 * <p>
 * The loader passed to the constructor is the long-lived parent. It keeps
 * dependency classes and any classpath the embedder already installed.
 * {@link #newCompilationLoader()} returns a child that defines one
 * compilation's classes. Construct
 * {@code new JavaAwareCompilationUnit(configuration, child)} with that child
 * and with {@code memStub} and {@code memClassOutput} both enabled. The
 * child's in-memory URL is appended when the unit compiles, not to the parent,
 * and it is appended after classpath entries the unit or the configuration
 * added, so an earlier entry still hides these bytes. The parent is not given
 * a URL and does not define the compiled classes. {@code loadClass} on the
 * parent does not return them.
 * <p>
 * {@link #publish(GroovyClassLoader)} copies classes that compilation has
 * committed. A later child resolves a name from its own stage, then from its
 * own committed output, then from what has been published. An overlapping
 * name therefore uses this compilation. A name this compilation does not
 * produce stays as it was published, which is the in-memory form of leaving
 * an unrewritten class file in the destination directory. It is not a
 * recompile of the union of sources. Publishing is explicit: a compilation
 * that is not published is invisible to the next child, which is what a host
 * wants when each compile replaces the previous snapshot. A failed compile
 * discards its stage, so publishing it does not replace a name the stage
 * held. A compile that has not reached output has nothing committed to publish.
 * <p>
 * A class a loader has already defined is not replaced. Load a recompiled
 * class from the child that compiled it, not from a loader that defined the
 * previous one, and not from the parent. One child serves one compilation
 * unit. The same unit may compile again. A second unit on that child throws
 * {@link IllegalStateException}, including when in-memory output is turned
 * off and later turned back on. Use the publication from one thread at a
 * time, as a compilation unit is used. Close a child when its classes are
 * no longer needed. Published bytes live as long as this publication does.
 *
 * @see CompilerConfiguration#MEM_CLASS_OUTPUT
 * @see JavaAwareCompilationUnit
 * @since 7.0.0
 */
public final class MemClassOutputPublication {

    private final ClassLoader parent;
    private final CompilerConfiguration configuration;
    private final MemClassOutputClasspath.PublishedOutput published = MemClassOutputClasspath.newPublishedOutput();

    /**
     * Creates a publication whose compilation loaders delegate to {@code parent}.
     * The child loaders use a default {@link CompilerConfiguration}.
     *
     * @param parent the long-lived loader that defines dependency classes
     */
    public MemClassOutputPublication(final ClassLoader parent) {
        this(parent, null);
    }

    /**
     * Creates a publication whose compilation loaders delegate to {@code parent}
     * and take their classpath and encoding from {@code configuration}.
     * Joint-compilation options are read from the configuration passed to
     * {@link JavaAwareCompilationUnit}, which may be the same object.
     *
     * @param parent the long-lived loader that defines dependency classes
     * @param configuration classpath and encoding for each child, or {@code null}
     *                      for a default configuration
     */
    public MemClassOutputPublication(final ClassLoader parent, final CompilerConfiguration configuration) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.configuration = configuration != null ? configuration : new CompilerConfiguration();
    }

    /**
     * Returns a loader for one joint compilation. Pass it to
     * {@link JavaAwareCompilationUnit#JavaAwareCompilationUnit(CompilerConfiguration, GroovyClassLoader)}.
     * Classes it defines are not defined by the parent. Published classes are
     * visible to it once that unit compiles, and {@code loadClass} on it
     * defines them there.
     *
     * @return the child loader for the next compilation
     */
    public GroovyClassLoader newCompilationLoader() {
        return MemClassOutputClasspath.newCompilationLoader(parent, configuration, published);
    }

    /**
     * Copies classes {@code loader} has committed into this publication.
     * Names already published are replaced. Names absent from {@code loader}
     * stay. The open stage is not copied. {@code loader} must be a loader
     * returned by {@link #newCompilationLoader()} on this publication, and its
     * unit must already have compiled with in-memory class output.
     *
     * @param loader the compilation loader whose committed output to publish
     * @throws IllegalArgumentException if {@code loader} was not created by this publication
     * @throws IllegalStateException if {@code loader} has not compiled in-memory class output
     */
    public void publish(final GroovyClassLoader loader) {
        Objects.requireNonNull(loader, "loader");
        if (!(loader instanceof MemClassOutputClasspath.CompilationLoader compilation)
                || compilation.publishedOutput() != published) {
            throw new IllegalArgumentException("loader was not created by this publication");
        }
        MemClassOutputClasspath.Store own = compilation.ownStore();
        if (own == null) {
            throw new IllegalStateException("compilation has no in-memory class output");
        }
        own.copyCommittedInto(published);
    }
}
