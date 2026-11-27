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

import groovy.lang.GroovyObject;
import org.apache.groovy.ast.ModuleInfoNode;
import org.apache.groovy.control.ModuleOptions;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompiledClassIndex;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.System.Logger.Level.INFO;

/**
 * {@link JavaCompiler} implementation that delegates to the JDK system Java
 * compiler.
 */
public class JavacJavaCompiler implements JavaCompiler {

    private static final System.Logger LOGGER = System.getLogger(JavacJavaCompiler.class.getName());

    private final CompilerConfiguration config;

    /**
     * Creates a javac-backed compiler for the supplied configuration.
     *
     * @param config the compiler configuration to use
     */
    public JavacJavaCompiler(final CompilerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    /**
     * Compiles the supplied Java source files with javac and reports any
     * errors back to the Groovy compilation unit.
     *
     * @param files the Java source files to compile
     * @param cu the owning Groovy compilation unit
     */
    @Override
    public void compile(final List<String> files, final CompilationUnit cu) {
        List<String> javacParameters = makeParameters(cu.getClassLoader());
        var javacOutput = new StringBuilderWriter();
        int javacReturnValue = 0;
        try {
            boolean successful = doCompileWithSystemJavaCompiler(cu, files, javacParameters, javacOutput);
            if (!successful) {
                javacReturnValue = 1;
            }
        } catch (IllegalArgumentException e) {
            javacReturnValue = 2; // invalid options
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        } catch (IOException e) {
            javacReturnValue = 1;
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        } catch (Exception e) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        }

        if (javacReturnValue != 0) {
            addJavacError(switch (javacReturnValue) {
                case  1 -> "Compile error during compilation with javac.";
                case  2 -> "Invalid commandline usage for javac.";
                default -> "unexpected return value by javac.";
            }, javacOutput.toString(), cu);
        } else {
            LOGGER.log(INFO, javacOutput.toString()); // javac errors/warnings
        }
    }

    private boolean doCompileWithSystemJavaCompiler(final CompilationUnit cu, final List<String> files, final List<String> javacParameters, final Writer javacOutput) throws IOException {
        Locale locale = Locale.ENGLISH;
        Charset charset = Charset.forName(config.getSourceEncoding());
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standard = compiler.getStandardFileManager(null, locale, charset);
        JavaFileManager activeManager = standard;
        try {
            activeManager = wrapInMemoryFileManager(cu, standard);

            Set<JavaFileObject> compilationUnitSet = cu.getJavaCompilationUnitSet(); // java stubs already added
            Map<String, Object> options = config.getJointCompilationOptions();
            boolean memStub = Boolean.TRUE.equals(options.get(CompilerConfiguration.MEM_STUB));
            Set<FileObject> stubSiblings = Collections.newSetFromMap(new IdentityHashMap<>());
            Path stubRoot = null;
            if (memStub) {
                stubSiblings.addAll(compilationUnitSet);
            } else {
                // clear the java stubs in the source set of Java compilation
                compilationUnitSet = new HashSet<>();
                stubRoot = configureStubSourcePath(options, javacParameters);
            }

            Set<Path> explicitSourcePaths = addExplicitJavaSources(files, standard, compilationUnitSet);
            RecordingJavaFileManager recordingManager = new RecordingJavaFileManager(
                    activeManager, stubSiblings, stubRoot, explicitSourcePaths, cu.getAST().getModuleInfo() != null);
            activeManager = recordingManager;

            javax.tools.JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                    javacOutput,
                    recordingManager,
                    null,
                    javacParameters,
                    null,
                    compilationUnitSet
            );
            compilationTask.setLocale(locale);

            boolean successful = compilationTask.call();
            if (successful) {
                CompiledClassIndex index = cu.getCompiledClasses();
                for (String className : recordingManager.recordedClasses()) {
                    index.add(className);
                }
            }
            return successful;
        } finally {
            activeManager.close();
        }
    }

    private JavaFileManager wrapInMemoryFileManager(final CompilationUnit cu, final StandardJavaFileManager standard) {
        if (!config.isMemClassOutput()) {
            return standard;
        }
        if (!(cu instanceof JavaAwareCompilationUnit jacu) || jacu.memClassOutputBytes() == null) {
            throw new IllegalStateException("memClassOutput requires JavaAwareCompilationUnit");
        }
        return new InMemoryJavaFileManager(standard, jacu.memClassOutputBytes(),
                cu.getJavaCompilationUnitSet(), jacu.memClassOutputStore());
    }

    private static Path configureStubSourcePath(final Map<String, Object> options, final List<String> javacParameters) {
        if (options.get("stubDir") instanceof File stubDir) {
            javacParameters.add("-sourcepath");
            javacParameters.add(stubDir.getAbsolutePath());
            return canonicalPath(stubDir.toPath());
        }
        throw new GroovyBugError("stubDir not specified");
    }

    private static Set<Path> addExplicitJavaSources(final List<String> files, final StandardJavaFileManager standard,
            final Set<JavaFileObject> compilationUnitSet) {
        List<File> javaFiles = new ArrayList<>(files.size());
        Set<Path> explicitSourcePaths = new HashSet<>();
        for (String path : files) {
            File file = new File(path);
            javaFiles.add(file);
            explicitSourcePaths.add(canonicalPath(file.toPath()));
        }
        standard.getJavaFileObjectsFromFiles(javaFiles).forEach(compilationUnitSet::add);
        return explicitSourcePaths;
    }

    static Path canonicalPath(final Path path) {
        try {
            return path.toRealPath();
        } catch (IOException ignored) {
            return path.toAbsolutePath().normalize();
        }
    }

    /**
     * Records binary names of non-stub {@code .class} files javac actually emits
     * during this invocation, regardless of whether {@code CLASS_OUTPUT} targets
     * disk or memory. When Groovy is compiling a module declaration, hides any
     * stale {@code module-info.class} left in {@code CLASS_OUTPUT} from a prior
     * build so javac does not reject Java sources outside {@code -sourcepath}.
     */
    static final class RecordingJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Set<FileObject> stubSiblings;
        private final Path stubRoot;
        private final Set<Path> explicitSourcePaths;
        private final boolean hideStaleModuleInfo;
        private final Set<String> recorded = new LinkedHashSet<>();

        RecordingJavaFileManager(final JavaFileManager fileManager, final Set<FileObject> stubSiblings,
                final Path stubRoot, final Set<Path> explicitSourcePaths, final boolean hideStaleModuleInfo) {
            super(fileManager);
            this.stubSiblings = stubSiblings;
            this.stubRoot = stubRoot;
            this.explicitSourcePaths = explicitSourcePaths;
            this.hideStaleModuleInfo = hideStaleModuleInfo;
        }

        Set<String> recordedClasses() {
            return recorded;
        }

        @Override
        public JavaFileObject getJavaFileForInput(final Location location, final String className,
                final JavaFileObject.Kind kind) throws IOException {
            if (hideStaleModuleInfo
                    && location == StandardLocation.CLASS_OUTPUT
                    && kind == JavaFileObject.Kind.CLASS
                    && ModuleInfoNode.CLASS_FILE_NAME.equals(className)) {
                return null;
            }
            return super.getJavaFileForInput(location, className, kind);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(final Location location, final String className,
                final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT && kind == JavaFileObject.Kind.CLASS && !isStubSibling(sibling)) {
                recorded.add(className);
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        @Override
        public FileObject getFileForOutput(final Location location, final String packageName,
                final String relativeName, final FileObject sibling) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT
                    && relativeName != null
                    && relativeName.endsWith(JavaFileObject.Kind.CLASS.extension)
                    && !isStubSibling(sibling)) {
                String simple = relativeName.substring(0, relativeName.length() - JavaFileObject.Kind.CLASS.extension.length())
                        .replace('/', '.');
                String binary = (packageName == null || packageName.isEmpty()) ? simple : packageName + "." + simple;
                recorded.add(binary);
            }
            return super.getFileForOutput(location, packageName, relativeName, sibling);
        }

        private boolean isStubSibling(final FileObject sibling) {
            if (sibling == null) {
                return false;
            }
            if (stubSiblings.contains(sibling)) {
                return true;
            }
            if (stubRoot == null) {
                return false;
            }
            URI uri = sibling.toUri();
            if (uri == null || !"file".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            try {
                Path path = canonicalPath(Path.of(uri));
                return path.startsWith(stubRoot) && !explicitSourcePaths.contains(path);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
    }

    private static void addJavacError(final String head, final String text, final CompilationUnit unit) {
        String message;
        if (text != null && !text.trim().isEmpty()) {
            message = head + "\n" + text;
        } else {
            message = head +
                    "\nThis javac version does not support compile(String[],PrintWriter), " +
                    "so no further details of the error are available. The message error text " +
                    "should be found on System.err.\n";
        }
        unit.getErrorCollector().addFatalError(new SimpleMessage(message, unit));
    }

    private List<String> makeParameters(final ClassLoader classLoader) {
        List<String> params = new ArrayList<>();

        // memClassOutput captures CLASS_OUTPUT in InMemoryJavaFileManager.
        // -d would name a directory that mode does not write.
        // Annotation processing is off unless the joint options ask for it,
        // so a processor discovered on the classpath cannot write into the
        // working directory.
        if (!config.isMemClassOutput()) {
            File targetDir = config.getTargetDirectory();
            if (targetDir == null) targetDir = new File(".");

            params.add("-d");
            params.add(targetDir.getAbsolutePath());
        }

        Map<String, Object> options = config.getJointCompilationOptions();
        boolean classpath = false;
        boolean release = false;
        boolean processing = false;

        if (options.get("flags") instanceof String[] flags) {
            for (String flag : flags) {
                if (flag == null) continue;
                if (!release) release = isReleaseParameter(flag.split("=")[0]);
                processing |= requestsAnnotationProcessing(flag);

                params.add("-" + flag);
            }
        }

        if (options.get("namedValues") instanceof String[] namedValues) {
            for (int i = 0, n = namedValues.length; i < n; i += 2) {
                var name = namedValues[i];
                if (name == null) continue;
                if (!release && isReleaseParameter(name)) release = true;
                else if (!classpath) classpath = isClasspathParameter(name);
                processing |= requestsAnnotationProcessing(name);

                params.add("-" + name);
                params.add(namedValues[i + 1]);
            }
        }

        if (config.isMemClassOutput() && !processing) {
            params.add("-proc:none");
        }

        // GROOVY-11790
        if (!release) {
            params.add("--release");
            params.add(config.getTargetBytecode());
        }

        // append classpath if not already defined
        if (!classpath) {
            // add class paths of compilation unit
            List<String> paths = new ArrayList<>(config.getClasspath());
            for (URL u : ModuleOptions.moduleClassLoaderUrls(config)) {
                addFileUrlPath(paths, u);
            }
            for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
                if (loader instanceof URLClassLoader ucl) {
                    for (URL u : ucl.getURLs()) {
                        // Not a directory or jar. The file manager serves these
                        // bytes on CLASS_PATH; javac cannot open the URL itself.
                        if (MemClassOutputClasspath.isInMemory(u)) {
                            continue;
                        }
                        addFileUrlPath(paths, u);
                    }
                }
            }

            try {
                var codeSource = getCodeSource();
                if (codeSource != null) { // add class path of groovy runtime
                    paths.add(new File(codeSource.getLocation().toURI()).getPath());
                }
            } catch (URISyntaxException ignore) {
            }

            params.add("-classpath");
            params.add(String.join(File.pathSeparator, paths));
        }

        return params;
    }

    private static void addFileUrlPath(final List<String> paths, final URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return;
        }
        try {
            String path = new File(url.toURI()).getPath();
            if (!paths.contains(path)) {
                paths.add(path);
            }
        } catch (URISyntaxException | IllegalArgumentException ignore) {
            // Ignore malformed or non-hierarchical URLs that cannot map to a filesystem path.
        }
    }

    private static boolean requestsAnnotationProcessing(final String name) {
        String option = name.startsWith("-") ? name.substring(1) : name;
        return option.equals("processor")
                || option.equals("processorpath")
                || option.equals("processor-module-path")
                || option.equals("proc")
                || option.startsWith("proc:");
    }

    private static boolean isClasspathParameter(final String param) {
        return param.equals("cp") || param.equals("classpath") || param.equals("-class-path");
    }

    private static boolean isReleaseParameter(final String param) {
        return param.equals("source")  || param.equals("-source")
            || param.equals("target")  || param.equals("-target")
            || param.equals("release") || param.equals("-release");
    }

    private static CodeSource getCodeSource() {
        return GroovyObject.class.getProtectionDomain().getCodeSource();
    }
}
