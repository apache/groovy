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

package org.codehaus.groovy.ant;

import groovy.lang.GroovyClassLoader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.javac.JavaStubCompilationUnit;

import java.io.File;

/**
 * Ant task to generate Java stubs from Groovy sources.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class GenerateStubsTask
    extends MatchingTask
{
    private final LoggingHelper log = new LoggingHelper(this);

    private Path src;
    
    private File destdir;

    private Path classpath;

    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    public void setSrcdir(final Path srcDir) {
        if (src == null) {
            src = srcDir;
        }
        else {
            src.append(srcDir);
        }
    }

    public Path getSrcdir() {
        return src;
    }

    public void setDestdir(final File dir) {
        assert dir != null;
        
        this.destdir = dir;
    }

    public void setClasspath(final Path path) {
        if (classpath == null) {
            classpath = path;
        }
        else {
            classpath.append(path);
        }
    }

    public Path getClasspath() {
        return classpath;
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }

        return classpath.createPath();
    }

    public void setClasspathRef(final Reference r) {
        createClasspath().setRefid(r);
    }

    private void validate() throws BuildException {
        if (src == null) {
            throw new BuildException("Missing attribute: srcdir (or one or more nested <src> elements).", getLocation());
        }

        if (destdir == null) {
            throw new BuildException("Missing attribute: destdir", getLocation());
        }
        
        if (!destdir.exists()) {
            throw new BuildException("Destination directory does not exist: " + destdir, getLocation());
        }
    }

    public void execute() throws BuildException {
        validate();

        CompilerConfiguration config = new CompilerConfiguration();

        ClassLoader parent = ClassLoader.getSystemClassLoader();
        GroovyClassLoader gcl = new GroovyClassLoader(parent, config);

        if (classpath != null) {
            gcl.addClasspath(classpath.toString());
        }

        JavaStubCompilationUnit compilation = new JavaStubCompilationUnit(config, gcl, destdir);

        int count = 0;
        
        String[] list = src.list();
        for (int i = 0; i < list.length; i++) {
            File basedir = getProject().resolveFile(list[i]);
            if (!basedir.exists()) {
                throw new BuildException("Source directory does not exist: " + basedir, getLocation());
            }

            DirectoryScanner scanner = getDirectoryScanner(basedir);
            String[] includes = scanner.getIncludedFiles();

            log.debug("Including files from: " + basedir);

            for (int j=0; j < includes.length; j++) {
                log.debug("    "  + includes[j]);
                
                File file = new File(basedir, includes[j]);
                compilation.addSourceFile(file);

                // Increment the count for each non/java src we found
                if (!includes[j].endsWith(".java")) {
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Generating " + count + " Java stub" + (count > 1 ? "s" : "") + " to " + destdir);

            // Generate the stubs
            compilation.compile(Phases.CONVERSION);
        }
        else {
            log.info("No sources found for stub generation");
        }
    }
}
