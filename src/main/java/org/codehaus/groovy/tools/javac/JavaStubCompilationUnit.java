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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

/**
 * Compilation unit to only generate stubs.
 */
public class JavaStubCompilationUnit extends CompilationUnit {

    private final JavaStubGenerator stubGenerator;

    private int stubCount;

    /**
     * Creates a compilation unit that generates Java stubs into the supplied
     * destination directory.
     *
     * @param config the compiler configuration
     * @param gcl the Groovy class loader to use
     * @param destDir the destination directory for generated stubs
     */
    public JavaStubCompilationUnit(final CompilerConfiguration config, final GroovyClassLoader gcl, File destDir) {
        super(config, null, gcl);

        if (destDir == null) {
            Map<String, Object> options = configuration.getJointCompilationOptions();
            destDir = (File) options.get("stubDir");
        }
        String encoding = configuration.getSourceEncoding();
        stubGenerator = new JavaStubGenerator(destDir, false, encoding);

        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            new VariableScopeVisitor(source).visitClass(classNode);
            new JavaAwareResolveVisitor(this).startResolving(classNode, source);
        }, Phases.CONVERSION);

        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            try {
                stubGenerator.generateClass(classNode);
                stubCount += 1;
            } catch (FileNotFoundException e) {
                source.addException(e);
            }
        }, Phases.CONVERSION);
    }

    /**
     * Creates a compilation unit that generates Java stubs into the configured
     * stub directory.
     *
     * @param config the compiler configuration
     * @param gcl the Groovy class loader to use
     */
    public JavaStubCompilationUnit(final CompilerConfiguration config, final GroovyClassLoader gcl) {
        this(config, gcl, null);
    }

    /**
     * Returns the number of stubs generated during the last compilation run.
     *
     * @return the generated stub count
     */
    public int getStubCount() {
        return stubCount;
    }

    /**
     * Compiles sources through the conversion phase to generate stubs only.
     *
     * @throws CompilationFailedException if stub generation fails
     */
    @Override
    public void compile() throws CompilationFailedException {
        stubCount = 0;
        super.compile(Phases.CONVERSION);
    }

    /**
     * Configures the compilation unit and makes the target directory visible to
     * the Groovy class loader.
     *
     * @param config the compiler configuration to apply
     */
    @Override
    public void configure(final CompilerConfiguration config) {
        super.configure(config);
        // GroovyClassLoader should be able to find classes compiled from java sources
        File targetDir = configuration.getTargetDirectory();
        if (targetDir != null) {
            final String classOutput = targetDir.getAbsolutePath();
            getClassLoader().addClasspath(classOutput);
        }
    }

    /**
     * Adds a source file when its extension is accepted for stub generation.
     *
     * @param file the source file to add
     * @return the created source unit, or {@code null} if the extension is not
     * accepted
     */
    @Override
    public SourceUnit addSource(final File file) {
        if (hasAcceptedFileExtension(file.getName())) {
            return super.addSource(file);
        }
        return null;
    }

    /**
     * Adds a source URL when its extension is accepted for stub generation.
     *
     * @param url the source URL to add
     * @return the created source unit, or {@code null} if the extension is not
     * accepted
     */
    @Override
    public SourceUnit addSource(URL url) {
        if (hasAcceptedFileExtension(url.getPath())) {
            return super.addSource(url);
        }
        return null;
    }

    private boolean hasAcceptedFileExtension(String name) {
        String lowerCasedName = name.toLowerCase();
        String extension = lowerCasedName.substring(lowerCasedName.lastIndexOf('.') + 1);
        return configuration.getScriptExtensions().contains(extension);
    }
}
