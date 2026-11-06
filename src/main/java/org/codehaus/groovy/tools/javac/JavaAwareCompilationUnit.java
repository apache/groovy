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
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.codehaus.groovy.transform.ASTTransformationVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * is required. The default {@link JavacJavaCompiler} honors that option;
 * a replacement {@link JavaCompilerFactory} must publish class bytes itself.
 */
public class JavaAwareCompilationUnit extends CompilationUnit {

    private final JavaStubGenerator stubGenerator;
    private final Set<String> javaSources = new HashSet<>();
    private JavaCompilerFactory compilerFactory = new JavacCompilerFactory();
    private final File generationGoal;
    private final boolean keepStubs;
    private final boolean memStubEnabled;
    private Map<String, byte[]> memClassBytes;
    private InMemoryJavaFileManager memFileManager;

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
            if (this.configuration.isMemClassOutput()) {
                // One map per loader, so a later compile on the same loader replaces bytes in place.
                this.memClassBytes = MemClassOutputClasspath.install(getClassLoader());
                setClassNodeResolver(getClassNodeResolver());
            }
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
            JavaCompiler compiler = compilerFactory.createCompiler(configuration);
            try {
                addJavaCompilationUnits(stubGenerator.getJavaStubCompilationUnitSet()); // add java stubs
                compiler.compile(new ArrayList<>(javaSources), this);
            } finally {
                if (compiler instanceof JavacJavaCompiler javac) {
                    memFileManager = javac.getInMemoryJavaFileManager();
                } else {
                    memFileManager = null;
                }
                if (!keepStubs) stubGenerator.clean();
                javaSources.clear();
            }
        }
    }

    /**
     * Compiles this unit. A repeat compile on the same class loader drops
     * class files left by the previous in-memory compile first, so replaced
     * types stay visible through that loader.
     *
     * @param throughPhase the last phase to run
     * @throws CompilationFailedException if compilation fails
     */
    @Override
    public void compile(final int throughPhase) throws CompilationFailedException {
        if (memClassBytes != null) {
            memClassBytes.clear();
            if (getClassNodeResolver() instanceof MemClassNodeResolver memResolver) {
                memResolver.clear();
            }
        }
        super.compile(throughPhase);
    }

    /**
     * Keeps in-memory class bytes visible to resolution when a caller replaces
     * the resolver. The wrapper delegates every other lookup.
     *
     * @param classNodeResolver the resolver to install
     */
    @Override
    public void setClassNodeResolver(final ClassNodeResolver classNodeResolver) {
        ClassNodeResolver resolver = classNodeResolver;
        if (memClassBytes != null && !(resolver instanceof MemClassNodeResolver)) {
            resolver = new MemClassNodeResolver(memClassBytes, resolver);
        }
        super.setClassNodeResolver(resolver);
    }

    /**
     * Returns the file manager that captured javac class files for the most
     * recent joint compile, or {@code null} when class output went to disk
     * or compilation did not reach javac.
     * <p>
     * javac compiles in-memory stubs as well as {@code .java} sources, so this
     * manager can contain stub class files for Groovy types. The class loader
     * is updated again at the output phase with the bytecode Groovy itself generated.
     *
     * @return the in-memory file manager, or {@code null}
     * @since 7.0.0
     */
    public InMemoryJavaFileManager getInMemoryJavaFileManager() {
        return memFileManager;
    }

    /**
     * Resource names ({@code com/example/Type.class}) of class bytes kept in
     * memory for this loader, or {@code null} when class files are written to disk.
     *
     * @return the live resource map, or {@code null}
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
