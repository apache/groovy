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
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.AnnotationConstantsVisitor;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.codehaus.groovy.transform.ASTTransformationVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.control.CompilerConfiguration.MEM_STUB;

/**
 * Compilation unit capable of compiling Java source files.
 * <p>
 * With {@link CompilerConfiguration#MEM_STUB} and
 * {@link CompilerConfiguration#MEM_CLASS_OUTPUT} both enabled, stubs and
 * javac class files stay in memory. No stub directory or target directory
 * is required. Class bytes are published on this unit's class loader, and
 * the ordinary class-node resolver reads them from there.
 * <p>
 * Only {@link JavacJavaCompiler} supports that option. Another
 * {@link JavaCompiler} fails the compile instead of writing class files to a
 * directory the class loader is not using. Each unit publishes on the loader
 * it is given; a later unit does not share that publication. A class the
 * loader has already defined is not replaced; run recompiled code with a
 * fresh loader. An earlier classpath entry or a parent loader hides the
 * in-memory bytes.
 */
public class JavaAwareCompilationUnit extends CompilationUnit {

    private final JavaStubGenerator stubGenerator;
    private final Set<String> javaSources = new HashSet<>();
    private JavaCompilerFactory compilerFactory = new JavacCompilerFactory();
    private final File generationGoal;
    private final boolean keepStubs;
    private final boolean memStubEnabled;
    private MemClassOutputClasspath.Store memOutput;
    private Map<String, byte[]> memClassBytes;

    /**
     * Creates a joint compilation unit with the default configuration.
     */
    public JavaAwareCompilationUnit() {
        this(null, null, null);
    }

    /**
     * Creates a joint compilation unit with the supplied configuration.
     *
     * @param configuration the compiler configuration to use
     */
    public JavaAwareCompilationUnit(final CompilerConfiguration configuration) {
        this(configuration, null, null);
    }

    /**
     * Creates a joint compilation unit with the supplied configuration and
     * class loader.
     *
     * @param configuration the compiler configuration to use
     * @param groovyClassLoader the Groovy class loader to use
     */
    public JavaAwareCompilationUnit(final CompilerConfiguration configuration, final GroovyClassLoader groovyClassLoader) {
        this(configuration, groovyClassLoader, null);
    }

    /**
     * Creates a joint compilation unit with explicit Groovy and transform class
     * loaders.
     *
     * @param configuration the compiler configuration to use
     * @param groovyClassLoader the Groovy class loader to use
     * @param transformClassLoader the class loader used for AST transforms
     */
    public JavaAwareCompilationUnit(final CompilerConfiguration configuration, final GroovyClassLoader groovyClassLoader, final GroovyClassLoader transformClassLoader) {
        super(configuration, /*codeSource*/null, groovyClassLoader, transformClassLoader);

        {
            Map<String, Object> options = this.configuration.getJointCompilationOptions();
            String sourceEncoding = this.configuration.getSourceEncoding();

            this.keepStubs = Boolean.TRUE.equals(options.get("keepStubs"));
            this.memStubEnabled = Boolean.TRUE.equals(options.get(MEM_STUB));
            this.generationGoal = memStubEnabled ? null : (File) options.get("stubDir");
            this.stubGenerator = new JavaStubGenerator(generationGoal, false, sourceEncoding);
        }

        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            if (!javaSources.isEmpty()) {
                ResolveVisitor resolveVisitor = new JavaAwareResolveVisitor(this);
                resolveVisitor.setClassNodeResolver(new ClassNodeResolver() {
                    @Override
                    public LookupResult resolveName(final String name, final CompilationUnit unit) {
                        LookupResult result = super.resolveName(name, unit);
                        if (result == null) { // GROOVY-8184, GROOVY-10571: look for java source
                            String spec = File.separator + name.replace('.', File.separatorChar) + ".java";
                            if (javaSources.stream().anyMatch(path -> path.endsWith(spec))) {
                                result = new LookupResult(null, NO_CLASS); // truthy value
                            }
                        }
                        return result;
                    }
                });
                resolveVisitor.startResolving(classNode, source);
                // then resolve constants in annotation instances
                new AnnotationConstantsVisitor().visitClass(classNode, source);
            }
        }, Phases.CONVERSION);

        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            GroovyClassVisitor visitor = new ASTTransformationCollectorCodeVisitor(source, getTransformLoader());
            visitor.visitClass(classNode);
        }, Phases.CONVERSION);

        // GEP-21: invoke any local AST transforms registered for the CONVERSION phase so
        // they can contribute to the joint-compilation stub before it is written out.
        // Uses the standard ASTTransformationVisitor to walk the class (so field-level
        // and method-level annotations are dispatched to their actual parents, not the
        // enclosing class).
        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            if (!javaSources.isEmpty()) {
                ASTTransformationVisitor.invokeTransformsForClass(
                        CompilePhase.CONVERSION, getASTTransformationsContext(), source, classNode);
            }
        }, Phases.CONVERSION);

        addPhaseOperation((final SourceUnit source, final GeneratorContext context, final ClassNode classNode) -> {
            try {
                if (!javaSources.isEmpty()) stubGenerator.generateClass(classNode);
            } catch (FileNotFoundException fnfe) {
                source.addException(fnfe);
            }
        }, Phases.CONVERSION);
    }

    /**
     * Advances the compilation unit to the requested phase and triggers javac
     * when the semantic analysis boundary is reached.
     *
     * @param phase the target compilation phase
     * @throws CompilationFailedException if compilation fails
     */
    @Override
    public void gotoPhase(final int phase) throws CompilationFailedException {
        super.gotoPhase(phase);
        // compile Java and clean up
        if (phase == Phases.SEMANTIC_ANALYSIS && !javaSources.isEmpty()) {
            for (ModuleNode module : getAST().getModules()) {
                module.setImportsResolved(false);
            }
            try {
                JavaCompiler compiler = compilerFactory.createCompiler(configuration);
                if (configuration.isMemClassOutput() && !(compiler instanceof JavacJavaCompiler)) {
                    getErrorCollector().addFatalError(new SimpleMessage(
                            "memClassOutput requires the default javac joint compiler", this));
                }
                addJavaCompilationUnits(stubGenerator.getJavaStubCompilationUnitSet()); // add java stubs
                compiler.compile(new ArrayList<>(javaSources), this);
                warnIfShadowed();
            } finally {
                if (!keepStubs) stubGenerator.clean();
                javaSources.clear();
            }
        }
    }

    /**
     * Compiles this unit.
     * <p>
     * With in-memory class output, bytes produced by this compile are staged
     * until output completes. A later {@code compile} on this unit that has
     * not reached output keeps that stage, so a compile that stopped at
     * semantic analysis does not discard the classes javac already produced.
     * A failed compile discards the stage and leaves classes this unit
     * already committed. Classes this compile does not produce again are not
     * removed. A class the loader has already defined is not replaced.
     * Another compilation unit does not share this publication.
     *
     * @param throughPhase the last phase to run
     * @throws CompilationFailedException if compilation fails
     */
    @Override
    public void compile(final int throughPhase) throws CompilationFailedException {
        rejectMemClassOutputWithoutMemStub();
        boolean staging = bindMemOutput();
        boolean ok = false;
        try {
            super.compile(throughPhase);
            ok = true;
        } finally {
            if (staging) {
                if (!ok) {
                    memOutput.abort();
                } else if (getPhase() > Phases.OUTPUT) {
                    memOutput.commit();
                }
            }
        }
    }

    private void rejectMemClassOutputWithoutMemStub() throws CompilationFailedException {
        Map<String, Object> options = getConfiguration().getJointCompilationOptions();
        if (options != null
                && Boolean.TRUE.equals(options.get(CompilerConfiguration.MEM_CLASS_OUTPUT))
                && !Boolean.TRUE.equals(options.get(MEM_STUB))) {
            getErrorCollector().addFatalError(new SimpleMessage("memClassOutput requires memStub", this));
        }
    }

    /**
     * Opens this unit's store on its class loader, then the stage for this
     * compile. The store stays with this unit. A compile that has not reached
     * output reopens the same stage. Turning the option off discards an open
     * stage and stops this unit publishing further classes there.
     *
     * @return {@code true} when this compile is publishing in memory
     */
    private boolean bindMemOutput() {
        if (!getConfiguration().isMemClassOutput()) {
            if (memOutput != null) {
                memOutput.abort();
                memOutput = null;
                memClassBytes = null;
            }
            return false;
        }
        if (memOutput == null) {
            memOutput = MemClassOutputClasspath.open(getClassLoader());
        }
        memClassBytes = memOutput.begin();
        return true;
    }

    /**
     * Warns when a class just published is hidden by an earlier classpath entry.
     * {@code getResource} is parent-first and then searches URLs in order, and
     * the in-memory URL is appended last.
     */
    private void warnIfShadowed() {
        if (memClassBytes == null) {
            return;
        }
        for (String name : memClassBytes.keySet()) {
            URL url = getClassLoader().getResource(name);
            if (url != null && !MemClassOutputClasspath.isInMemory(url)) {
                getErrorCollector().addWarning(WarningMessage.LIKELY_ERRORS,
                        "in-memory class " + name + " is hidden by " + url, null, null);
            }
        }
    }

    /**
     * Resource names ({@code com/example/Type.class}) of the stage opened for
     * the current compile, or {@code null} when class files are written to disk.
     *
     * @return the stage map, or {@code null}
     */
    Map<String, byte[]> memClassOutputBytes() {
        return memClassBytes;
    }

    /**
     * Configures the compilation unit and ensures the target directory is on
     * the Groovy class loader classpath.
     * <p>
     * When in-memory class output is enabled the target directory is not added.
     * Compiled classes are published from memory, and a directory already on
     * the classpath would hide those bytes with whatever was previously written there.
     *
     * @param configuration the compiler configuration to apply
     */
    @Override
    public void configure(final CompilerConfiguration configuration) {
        super.configure(configuration);
        if (this.configuration.isMemClassOutput()) {
            return;
        }
        // GroovyClassLoader should be able to find classes compiled from java sources
        File targetDir = this.configuration.getTargetDirectory();
        if (targetDir != null) {
            final String classOutput = targetDir.getAbsolutePath();
            getClassLoader().addClasspath(classOutput);
        }
    }

    /**
     * Keeps generated Groovy classes in memory when {@link CompilerConfiguration#isMemClassOutput()}
     * is enabled. Otherwise writes them to the target directory.
     *
     * @param groovyClass the generated class
     * @throws IOException if the class file cannot be written
     */
    @Override
    protected void writeClassFile(final GroovyClass groovyClass) throws IOException {
        if (memClassBytes != null) {
            byte[] bytes = groovyClass.getBytes();
            if (bytes != null && bytes.length > 0) {
                memClassBytes.put(InMemoryJavaFileManager.resourceName(groovyClass.getName()), bytes.clone());
            }
            return;
        }
        super.writeClassFile(groovyClass);
    }

    private void addJavaOrGroovySource(final File file) {
        if (file.getName().endsWith(".java")) {
            addJavaSource(file);
        } else {
            addSource(file);
        }
    }

    private void addJavaSource(final File file) {
        javaSources.add(file.getAbsolutePath());
    }

    /**
     * Adds Groovy or Java sources from the supplied path strings.
     *
     * @param paths the source paths to add
     */
    @Override
    public void addSources(final String[] paths) {
        for (String path : paths) {
            addJavaOrGroovySource(new File(path));
        }
    }

    /**
     * Adds Groovy or Java sources from the supplied files.
     *
     * @param files the source files to add
     */
    @Override
    public void addSources(final File[] files) {
        for (File file : files) {
            addJavaOrGroovySource(file);
        }
    }

    /**
     * Returns the factory used to create the backing Java compiler.
     *
     * @return the Java compiler factory
     */
    public JavaCompilerFactory getCompilerFactory() {
        return compilerFactory;
    }

    /**
     * Sets the factory used to create the backing Java compiler.
     *
     * @param compilerFactory the Java compiler factory to use
     */
    public void setCompilerFactory(final JavaCompilerFactory compilerFactory) {
        this.compilerFactory = compilerFactory;
    }
}
