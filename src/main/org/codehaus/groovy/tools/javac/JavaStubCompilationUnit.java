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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Compilation unit to <em>only</em> generate Java stubs for Groovy sources.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JavaStubCompilationUnit
    extends CompilationUnit
{
    private final List javaSources = new LinkedList();

    public JavaStubCompilationUnit(final CompilerConfiguration config, final GroovyClassLoader classLoader, final File outputDirectory) {
        super(config, null, classLoader);

        addPhaseOperation(new JavaResolverOperation(), Phases.CONVERSION);
        addPhaseOperation(new StubGeneratorOperation(outputDirectory), Phases.CONVERSION);
    }

    public void gotoPhase(final int phase) throws CompilationFailedException {
        super.gotoPhase(phase);

        if (phase == Phases.SEMANTIC_ANALYSIS) {
            javaSources.clear();
        }
    }

    public void addSourceFile(final File file) {
        if (file.getName().endsWith(".java")) {
            addJavaSource(file);
        }
        else {
            addSource(file);
        }
    }

    private void addJavaSource(final File file) {
        //
        // FIXME: Um... not really sure what this is doing...
        //        So either document what its job is... or whack it ;-)
        //

        String path = file.getAbsolutePath();
        Iterator iter = javaSources.iterator();

        while (iter.hasNext()) {
            if (path.equals(iter.next())) {
                return;
            }
        }

        javaSources.add(path);
    }

    private boolean haveJavaSources() {
        return !javaSources.isEmpty();
    }

    //
    // Custom Operations
    //

    /**
     * Operation to resolve Java sources.
     */
    private class JavaResolverOperation
        extends PrimaryClassNodeOperation
    {
        public void call(final SourceUnit source, final GeneratorContext context, final ClassNode node) throws CompilationFailedException {
            if (haveJavaSources()) {
                ResolveVisitor v = new JavaAwareResolveVisitor(JavaStubCompilationUnit.this);
                v.startResolving(node, source);
            }
        }
    }

    /**
     * Operation to generate Java stubs from Groovy sources.
     */
    private class StubGeneratorOperation
        extends PrimaryClassNodeOperation
    {
        private final JavaStubGenerator generator;

        public StubGeneratorOperation(final File outputDirectory) {
            outputDirectory.mkdirs();

            boolean java5 = false;
            String target = JavaStubCompilationUnit.this.getConfiguration().getTargetBytecode();
            
            // Enable java5 mode if the configuration lets us
            if (target != null && target.trim().equals("1.5")) {
                java5 = true;
            }

            generator = new JavaStubGenerator(outputDirectory, true, java5);
        }

        public void call(final SourceUnit source, final GeneratorContext context, final ClassNode node) throws CompilationFailedException {
            if (haveJavaSources()) {
                try {
                    generator.generateClass(node);
                }
                catch (Exception e) {
                    source.addException(e);
                }
            }
        }
    }
}