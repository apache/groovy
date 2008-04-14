/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyClassLoader;

import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Created by IntelliJ IDEA. 
 * User: Alex.Tkachman 
 * Date: May 31, 2007 Time: 6:48:28 PM 
 */
public class JavaAwareCompilationUnit extends CompilationUnit {
    private LinkedList javaSources; // java sources
    private JavaStubGenerator stubGenerator;
    private JavaCompilerFactory compilerFactory = new JavacCompilerFactory();
    private File generationGoal;
    
    public JavaAwareCompilationUnit(CompilerConfiguration configuration) {
        this(configuration,null);
    }
    
    public JavaAwareCompilationUnit(CompilerConfiguration configuration, GroovyClassLoader groovyClassLoader) {
        super(configuration,null,groovyClassLoader);
        javaSources = new LinkedList();
        generationGoal = (File) configuration.getJointCompilationOptions().get("stubDir");
        boolean useJava5 = configuration.getTargetBytecode().equals(CompilerConfiguration.POST_JDK5);
        stubGenerator = new JavaStubGenerator(generationGoal,false,useJava5);
        
        addPhaseOperation(new PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode node) throws CompilationFailedException {
                if (javaSources.size() != 0) new JavaAwareResolveVisitor(JavaAwareCompilationUnit.this).startResolving(node,source);
            }
        },Phases.CONVERSION);

        addPhaseOperation(new PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                try {
                    if (javaSources.size() != 0) stubGenerator.generateClass(classNode);
                } catch (FileNotFoundException fnfe) {
                    source.addException(fnfe);
                }
            }
        },Phases.CONVERSION);
    }

    public void gotoPhase(int phase) throws CompilationFailedException {
        super.gotoPhase(phase);
        // compile Java and clean up
        if (phase==Phases.SEMANTIC_ANALYSIS && javaSources.size()>0) {
            Iterator modules = getAST().getModules().iterator();
            while (modules.hasNext()) {
                ModuleNode module = (ModuleNode) modules.next();
                module.setImportsResolved(false);
            }
            try {
                JavaCompiler compiler = compilerFactory.createCompiler(getConfiguration());
                compiler.compile(javaSources, this);
            } finally {
                stubGenerator.clean();
                javaSources.clear();
            }
        }
    }
    
    public void configure(CompilerConfiguration configuration) {
        super.configure(configuration);
        // GroovyClassLoader should be able to find classes compiled from java
        // sources
        File targetDir = configuration.getTargetDirectory();
        if (targetDir != null) {
            final String classOutput = targetDir.getAbsolutePath();
            getClassLoader().addClasspath(classOutput);
        }
    }

    private void addJavaSource(File file) {
        String path = file.getAbsolutePath();
        for (Iterator iter = javaSources.iterator(); iter.hasNext();) {
            String su = (String) iter.next();
            if (path.equals(su))
                return;
        }
        javaSources.add(path);
    }

    public void addSources(String[] paths) {
        for (int i = 0; i < paths.length; i++) {
            File file = new File(paths[i]);
            if (file.getName().endsWith(".java"))
                addJavaSource(file);
            else
                addSource(file);
        }
    }

    public void addSources(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".java"))
                addJavaSource(files[i]);
            else
                addSource(files[i]);
        }
    }

    public JavaCompilerFactory getCompilerFactory() {
        return compilerFactory;
    }

    public void setCompilerFactory(JavaCompilerFactory compilerFactory) {
        this.compilerFactory = compilerFactory;
    }
}
