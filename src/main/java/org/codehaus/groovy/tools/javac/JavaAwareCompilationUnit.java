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
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.AnnotationConstantsVisitor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Compilation Unit capable of compiling Java source files.
 *
 * @author Alex.Tkachman
 */
public class JavaAwareCompilationUnit extends CompilationUnit {
    private final List<String> javaSources;
    private final JavaStubGenerator stubGenerator;
    private JavaCompilerFactory compilerFactory = new JavacCompilerFactory();
    private final File generationGoal;
    private final boolean keepStubs;
    private final CompilerConfiguration configuration;
    private final boolean memStubEnabled;

    public JavaAwareCompilationUnit(CompilerConfiguration configuration) {
        this(configuration, null, null);
    }

    public JavaAwareCompilationUnit(CompilerConfiguration configuration, GroovyClassLoader groovyClassLoader) {
        this(configuration, groovyClassLoader, null);
    }

    public JavaAwareCompilationUnit(CompilerConfiguration configuration, GroovyClassLoader groovyClassLoader,
                                    GroovyClassLoader transformClassLoader) {
        super(configuration, null, groovyClassLoader, transformClassLoader);
        this.configuration = configuration;
        this.memStubEnabled = configuration.isMemStubEnabled();
        this.javaSources = new LinkedList<String>();
        Map options = configuration.getJointCompilationOptions();
        this.generationGoal = memStubEnabled ? null : (File) options.get("stubDir");
        boolean useJava5 = CompilerConfiguration.isPostJDK5(configuration.getTargetBytecode());
		String encoding = configuration.getSourceEncoding();

        this.stubGenerator = new JavaStubGenerator(generationGoal, false, useJava5, encoding);
        this.keepStubs = Boolean.TRUE.equals(options.get("keepStubs"));

        addPhaseOperation(new PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode node) throws CompilationFailedException {
                if (!javaSources.isEmpty()) {
                    VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(source);
                    scopeVisitor.visitClass(node);
                    new JavaAwareResolveVisitor(JavaAwareCompilationUnit.this).startResolving(node, source);
                    AnnotationConstantsVisitor acv = new AnnotationConstantsVisitor();
                    acv.visitClass(node, source);
                }
            }
        }, Phases.CONVERSION);
        addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationCollectorCodeVisitor collector =
                        new ASTTransformationCollectorCodeVisitor(source, JavaAwareCompilationUnit.this.getTransformLoader());
                collector.visitClass(classNode);
            }
        }, Phases.CONVERSION);

        addPhaseOperation(new PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                try {
                    if (!javaSources.isEmpty()) stubGenerator.generateClass(classNode);
                } catch (FileNotFoundException fnfe) {
                    source.addException(fnfe);
                }
            }
        }, Phases.CONVERSION);
    }

    public void gotoPhase(int phase) throws CompilationFailedException {
        super.gotoPhase(phase);
        // compile Java and clean up
        if (phase == Phases.SEMANTIC_ANALYSIS && !javaSources.isEmpty()) {
            for (ModuleNode module : getAST().getModules()) {
                module.setImportsResolved(false);
            }
            try {
                if (memStubEnabled) {
                    this.addJavaCompilationUnits(stubGenerator.getJavaStubCompilationUnitSet()); // add java stubs
                }

                JavaCompiler compiler = compilerFactory.createCompiler(getConfiguration());
                compiler.compile(javaSources, this);
            } finally {
                if (!keepStubs) stubGenerator.clean();
                javaSources.clear();
            }
        }
    }

    public void configure(CompilerConfiguration configuration) {
        super.configure(configuration);
        // GroovyClassLoader should be able to find classes compiled from java sources
        File targetDir = configuration.getTargetDirectory();
        if (targetDir != null) {
            final String classOutput = targetDir.getAbsolutePath();
            getClassLoader().addClasspath(classOutput);
        }
    }

    private void addJavaSource(File file) {
        String path = file.getAbsolutePath();
        for (String source : javaSources) {
            if (path.equals(source))
                return;
        }
        javaSources.add(path);
    }

    public void addSources(String[] paths) {
        for (String path : paths) {
            addJavaOrGroovySource(new File(path));
        }
    }

    public void addSources(File[] files) {
        for (File file : files) {
            addJavaOrGroovySource(file);
        }
    }

    private void addJavaOrGroovySource(File file) {
        if (file.getName().endsWith(".java"))
            addJavaSource(file);
        else
            addSource(file);
    }

    public JavaCompilerFactory getCompilerFactory() {
        return compilerFactory;
    }

    public void setCompilerFactory(JavaCompilerFactory compilerFactory) {
        this.compilerFactory = compilerFactory;
    }
}
