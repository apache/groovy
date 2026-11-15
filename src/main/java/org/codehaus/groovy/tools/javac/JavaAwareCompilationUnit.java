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
import org.apache.groovy.ast.ModuleInfoNode;
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
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.codehaus.groovy.transform.ASTTransformationVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * it is given. A later unit sees those bytes only when that loader was
 * created by {@link MemClassOutputPublication} and the host published the
 * earlier unit. Java sources in that later unit resolve the published classes
 * on javac's classpath. A class the loader has already defined is not replaced.
 * An earlier classpath entry or a parent loader hides the in-memory bytes.
 * {@code memStub} is read at the start of each {@link #compile(int)}, as is
 * in-memory class output.
 */
public class JavaAwareCompilationUnit extends CompilationUnit {

    private final Set<String> javaSources = new HashSet<>();
    private JavaCompilerFactory compilerFactory = new JavacCompilerFactory();
    private final boolean keepStubs;
    private JavaStubGenerator stubGenerator;
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
            this.stubGenerator = newStubGenerator(options, sourceEncoding);
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
                File targetDir = this.configuration.getTargetDirectory();
                Map<String, ClassFileStamp> before;
                try {
                    before = snapshotClassFiles(targetDir);
                } catch (IOException e) {
                    failIndexing(targetDir, e);
                    return;
                }
                compiler.compile(new ArrayList<>(javaSources), this);
                indexCompiledClassFiles(before);
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
     * Another compilation unit sees this output only through a
     * {@link MemClassOutputPublication} the host has published to.
     * {@code memStub} and in-memory class output are read from the
     * configuration here, so a change made since the previous compile takes
     * effect.
     *
     * @param throughPhase the last phase to run
     * @throws CompilationFailedException if compilation fails
     */
    @Override
    public void compile(final int throughPhase) throws CompilationFailedException {
        rejectMemClassOutputWithoutMemStub();
        Map<String, Object> options = getConfiguration().getJointCompilationOptions();
        if (options != null) {
            this.stubGenerator = newStubGenerator(options, getConfiguration().getSourceEncoding());
        }
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
     * Creates the stub generator for the options in force at this compile.
     * {@code memStub} selects in-memory stubs. Otherwise stubs are written
     * under {@code stubDir}.
     */
    private static JavaStubGenerator newStubGenerator(final Map<String, Object> options, final String encoding) {
        boolean memStub = Boolean.TRUE.equals(options.get(MEM_STUB));
        File goal = memStub ? null : (File) options.get("stubDir");
        return new JavaStubGenerator(goal, false, encoding);
    }

    /**
     * Warns when a class this compile published is hidden by an earlier
     * classpath entry. {@code getResource} is parent-first and then searches
     * URLs in order, and the in-memory URL is appended last. Javac's classes
     * are checked when javac returns. Groovy's own classes are checked as
     * they are written, which is later.
     */
    private void warnIfShadowed() {
        if (memClassBytes == null) {
            return;
        }
        for (String name : memClassBytes.keySet()) {
            warnIfShadowed(name);
        }
    }

    private void warnIfShadowed(final String name) {
        URL url = getClassLoader().getResource(name);
        if (url != null && !MemClassOutputClasspath.isInMemory(url)) {
            getErrorCollector().addWarning(WarningMessage.LIKELY_ERRORS,
                    "in-memory class " + name + " is hidden by " + url, null, null);
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
     * The store javac should read when it searches the classpath, or
     * {@code null} when this compile writes class files to disk.
     *
     * @return this unit's in-memory store
     */
    MemClassOutputClasspath.Store memClassOutputStore() {
        return memOutput;
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
                String name = InMemoryJavaFileManager.resourceName(groovyClass.getName());
                memClassBytes.put(name, bytes.clone());
                warnIfShadowed(name);
            }
            return;
        }
        super.writeClassFile(groovyClass);
    }

    private void addJavaOrGroovySource(final File file) {
        // module-info.java is a modular compilation unit (JLS §7.3). Routing it
        // to javac would put the joint compilation in single-module mode, where
        // the unnamed classpath (Groovy stubs and the runtime) is unreadable.
        // Groovy parses and emits module-info; javac compiles the sibling .java files.
        if (ModuleInfoNode.isModuleInfoSource(file.getName())) {
            addSource(file);
        } else if (file.getName().endsWith(".java")) {
            addJavaSource(file);
        } else {
            addSource(file);
        }
    }

    /**
     * After javac writes sibling .java classes (and Groovy stubs) to the target
     * directory, record the binary names of class files written by this
     * invocation so module verification and {@code ModulePackages} treat them
     * as members of the module being compiled. A reused target directory may
     * already contain leftover {@code .class} files; those are ignored unless
     * javac replaces them, or they are an inner class of a just-compiled source.
     */
    private void indexCompiledClassFiles(final Map<String, ClassFileStamp> before) {
        File targetDir = this.configuration.getTargetDirectory();
        if (targetDir == null || !targetDir.isDirectory()) {
            return;
        }
        Map<String, ClassFileStamp> after;
        try {
            after = snapshotClassFiles(targetDir);
        } catch (IOException e) {
            failIndexing(targetDir, e);
            return;
        }
        Set<String> compiledSources = binaryNamesFromJavaSources();
        for (Map.Entry<String, ClassFileStamp> entry : after.entrySet()) {
            String relative = entry.getKey();
            ClassFileStamp previous = before.get(relative);
            boolean written = previous == null || !previous.sameAs(entry.getValue());
            String binary = relative.substring(0, relative.length() - 6).replace('/', '.');
            if (written || isInnerClassOfCompiledSource(binary, compiledSources)) {
                getAST().addCompiledClassName(binary);
            }
        }
    }

    /**
     * Snapshots {@code .class} files under {@code targetDir}. An absent or
     * non-directory path yields an empty snapshot (nothing has been written yet).
     */
    private Map<String, ClassFileStamp> snapshotClassFiles(final File targetDir) throws IOException {
        Map<String, ClassFileStamp> stamps = new LinkedHashMap<>();
        if (targetDir == null || !targetDir.isDirectory()) {
            return stamps;
        }
        Path root = targetDir.toPath();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String relative = root.relativize(path).toString().replace(File.separatorChar, '/');
                if (!relative.endsWith(".class")) {
                    return;
                }
                try {
                    stamps.put(relative, ClassFileStamp.of(path));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return stamps;
    }

    private Set<String> binaryNamesFromJavaSources() {
        Set<String> names = new LinkedHashSet<>();
        for (String path : javaSources) {
            String binary = binaryNameFromJavaSource(new File(path));
            if (binary != null) {
                names.add(binary);
            }
        }
        return names;
    }

    private static final Pattern JAVA_PACKAGE = Pattern.compile("package\\s+([\\w.]+)\\s*;");
    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    /**
     * Top-level binary name inferred from a {@code .java} file's package
     * declaration and file name. {@code module-info.java} is skipped.
     * Inner classes are recognized separately by prefix ({@code Top$Inner}).
     */
    private static String binaryNameFromJavaSource(final File file) {
        String name = file.getName();
        if (!name.endsWith(".java")) {
            return null;
        }
        String simple = name.substring(0, name.length() - 5);
        if ("module-info".equals(simple)) {
            return null;
        }
        String pkg = packageNameFromJavaSource(file);
        if (pkg == null || pkg.isEmpty()) {
            return simple;
        }
        return pkg + "." + simple;
    }

    private static String packageNameFromJavaSource(final File file) {
        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // The snapshot of new/replaced files is the primary index; this is a supplement.
            return null;
        }
        content = BLOCK_COMMENT.matcher(content).replaceAll(" ");
        for (String line : content.split("\\R")) {
            int comment = line.indexOf("//");
            if (comment >= 0) {
                line = line.substring(0, comment);
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("@")) {
                continue;
            }
            Matcher matcher = JAVA_PACKAGE.matcher(trimmed);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            break;
        }
        return null;
    }

    private static boolean isInnerClassOfCompiledSource(final String binary, final Set<String> compiledSources) {
        for (String top : compiledSources) {
            if (binary.equals(top) || binary.startsWith(top + "$")) {
                return true;
            }
        }
        return false;
    }

    private void failIndexing(final File targetDir, final IOException e) {
        String detail = e.getMessage() == null || e.getMessage().isBlank() ? e.getClass().getSimpleName() : e.getMessage();
        getErrorCollector().addError(Message.create(
                "Failed to index class files in " + targetDir + " for joint compilation: " + detail, this));
        throw new CompilationFailedException(getPhase(), this);
    }

    /**
     * Identity of a class file used to detect replacements in a reused target directory.
     */
    private static final class ClassFileStamp {
        private final long size;
        private final FileTime lastModified;
        private final Object fileKey;

        private ClassFileStamp(final long size, final FileTime lastModified, final Object fileKey) {
            this.size = size;
            this.lastModified = lastModified;
            this.fileKey = fileKey;
        }

        static ClassFileStamp of(final Path path) throws IOException {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            return new ClassFileStamp(attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
        }

        boolean sameAs(final ClassFileStamp other) {
            if (other == null) {
                return false;
            }
            if (size != other.size || !lastModified.equals(other.lastModified)) {
                return false;
            }
            if (fileKey != null && other.fileKey != null) {
                return fileKey.equals(other.fileKey);
            }
            return true;
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
